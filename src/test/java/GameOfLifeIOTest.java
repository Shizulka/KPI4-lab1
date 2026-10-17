import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameOfLifeIOTest {
    static int[][] empty() {
        return new int[2][3];
    }

    static int[][] of(String... rows) {
        int[][] grid = new int[rows.length][];
        for (int r = 0; r < rows.length; r++) {
            grid[r] = new int[rows[r].length()];
            for (int c = 0; c < rows[r].length(); c++) {
                grid[r][c] = rows[r].charAt(c) == 'x' ? 1 : 0;
            }
        }
        return grid;
    }

    @TempDir
    Path tmp;

    private Path write(String name, String content) throws IOException {
        Path p = tmp.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    private static int[][] readField(Path p) throws IOException {
        java.util.List<String> rows = new java.util.ArrayList<>();
        for (String line : Files.readAllLines(p)) {
            if (!line.isBlank() && line.trim().matches("[.x]+")) {
                rows.add(line.trim());
            }
        }
        return of(rows.toArray(new String[0]));
    }

    private void assertBadInput(String content) throws IOException {
        Path p = write("bad.txt", content);
        assertThrows(IOException.class, () -> GameOfLifeIO.readInput(p),
                "should reject: " + content.replace("\n", "\\n"));
    }



    @Test
    void readsZeroGenerations() throws IOException {
        GameOfLifeIO.InputData d =
                GameOfLifeIO.readInput(write("z.txt", "0\n2\n1\nx.\n"));
        assertEquals(0, d.generations());
    }

    @Test
    void readsLargeGenerationCount() throws IOException {
        GameOfLifeIO.InputData d =
                GameOfLifeIO.readInput(write("l.txt", "1000000\n1\n1\nx\n"));
        assertEquals(1_000_000, d.generations());
    }

    @Test
    void readsOneByOneField() throws IOException {
        GameOfLifeIO.InputData d =
                GameOfLifeIO.readInput(write("one.txt", "1\n1\n1\nx\n"));
        assertArrayEquals(new int[][]{{1}}, d.grid());
    }

    @Test
    void readsWindowsLineEndings() throws IOException {
        GameOfLifeIO.InputData d = GameOfLifeIO.readInput(
                write("crlf.txt", "1\r\n3\r\n2\r\nx..\r\n.xx\r\n"));
        assertArrayEquals(of("x..", ".xx"), d.grid());
    }


    @Test
    void readsRowsIntoCorrectOrientationForNonSquareField()
            throws IOException {
        GameOfLifeIO.InputData d = GameOfLifeIO.readInput(
                write("rect.txt", "0\n4\n2\nx...\n...x\n"));
        assertEquals(2, d.grid().length);
        assertEquals(4, d.grid()[0].length);
        assertEquals(1, d.grid()[0][0]);
        assertEquals(1, d.grid()[1][3]);
    }

    @Test
    void readsAllDeadAndAllAliveFields() throws IOException {
        assertArrayEquals(empty(), GameOfLifeIO.readInput(
                write("dead.txt", "0\n3\n2\n...\n...\n")).grid());
        assertArrayEquals(new int[][]{{1, 1, 1}, {1, 1, 1}},
                GameOfLifeIO.readInput(
                        write("alive.txt", "0\n3\n2\nxxx\nxxx\n")).grid());
    }

    @Test
    void rejectsEmptyFile() throws IOException {
        assertBadInput("");
    }

    @Test
    void rejectsFileWithOnlyBlankLines() throws IOException {
        assertBadInput("\n\n\n");
    }

    @Test
    void rejectsMissingSizes() throws IOException {
        assertBadInput("3\n");
        assertBadInput("3\n8\n");
    }

    @Test
    void rejectsNonNumericValues() throws IOException {
        assertBadInput("abc\n2\n1\n..\n");
        assertBadInput("1\nabc\n1\n..\n");
        assertBadInput("1\n2\nabc\n..\n");
        assertBadInput("1.5\n2\n1\n..\n");
    }

    @Test
    void rejectsZeroOrNegativeSize() throws IOException {
        assertBadInput("1\n0\n1\n\n");
        assertBadInput("1\n2\n0\n");
        assertBadInput("1\n-2\n1\n..\n");
        assertBadInput("1\n2\n-1\n..\n");
    }

    @Test
    void rejectsNegativeGenerations() throws IOException {
        assertBadInput("-1\n2\n1\n..\n");
    }

    @Test
    void rejectsNumberTooBigForInt() throws IOException {
        assertBadInput("99999999999\n2\n1\n..\n");
    }

    @Test
    void rejectsRowTooShortOrTooLong() throws IOException {
        assertBadInput("1\n3\n1\nxx\n");
        assertBadInput("1\n3\n1\nxxxx\n");
    }

    @Test
    void rejectsTooFewOrTooManyRows() throws IOException {
        assertBadInput("1\n2\n3\n..\n..\n");
        assertBadInput("1\n2\n1\n..\n..\n");
    }

    @Test
    void rejectsUnknownCharacters() throws IOException {
        assertBadInput("1\n2\n1\n.a\n");
        assertBadInput("1\n2\n1\nX.\n");
        assertBadInput("1\n2\n1\n.1\n");
        assertBadInput("1\n2\n1\n*.\n");
        assertBadInput("1\n2\n1\n0.\n");
    }


    @Test
    void rejectsMissingFileAndNullPath() {
        assertThrows(IOException.class,
                () -> GameOfLifeIO.readInput(tmp.resolve("nope.txt")));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeIO.readInput(null));
    }

    @Test
    void rejectsDirectoryAsInput() {
        assertThrows(IOException.class, () -> GameOfLifeIO.readInput(tmp));
    }


    @Test
    void writtenFileContainsTheWholeGridInOrder() throws IOException {
        int[][] grid = of("x.x.", ".xx.", "...x");
        Path p = tmp.resolve("rt.txt");
        GameOfLifeIO.writeOutput(p, grid, 12);
        assertArrayEquals(grid, readField(p));
    }

    @Test
    void writeOverwritesExistingFile() throws IOException {
        Path p = write("ow.txt", "garbage garbage garbage garbage\n");
        GameOfLifeIO.writeOutput(p, of("x"), 0);
        assertFalse(Files.readString(p).contains("garbage"));
    }

    @Test
    void writeProducesOnlyXAndDotInGridRows() throws IOException {
        Path p = tmp.resolve("chars.txt");
        GameOfLifeIO.writeOutput(p, of("x.x", ".x."), 1);
        String[] lines = Files.readString(p).trim().split("\\R");
        assertEquals("x.x", lines[lines.length - 2]);
        assertEquals(".x.", lines[lines.length - 1]);
    }

    @Test
    void writeRejectsInvalidArguments() {
        Path p = tmp.resolve("x.txt");
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeIO.writeOutput(p, new int[][]{{2}}, 0));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeIO.writeOutput(p, null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeIO.writeOutput(p, of("x"), -1));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeIO.writeOutput(null, of("x"), 0));
        assertFalse(Files.exists(p));
    }

    @Test
    void defaultPathPointsToSavesDirectory() {
        assertEquals(Path.of("saves", "game.txt"), GameOfLifeIO.defaultPath());
    }

}
