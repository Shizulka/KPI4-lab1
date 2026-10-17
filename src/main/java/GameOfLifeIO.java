import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GameOfLifeIO {
    private static final Path DEFAULT_PATH =
            Path.of("saves", "game.txt");

    private GameOfLifeIO() {
    }

    public record InputData(int generations, int[][] grid) {
        public InputData {
            if (generations < 0) {
                throw new IllegalArgumentException(
                        "Generations cannot be negative."
                );
            }

            GameOfLifeLogic.validateGrid(grid);
            grid = GameOfLifeLogic.copyGrid(grid);
        }

        @Override
        public int[][] grid() {
            return GameOfLifeLogic.copyGrid(grid);
        }
    }

    public static Path defaultPath() {
        return DEFAULT_PATH;
    }

    public static Path resolvePath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException(
                    "Path cannot be null."
            );
        }

        return path.toAbsolutePath().normalize();
    }

    public static InputData readInput(Path path)
            throws IOException {
        Path resolved = resolvePath(path);

        if (!Files.exists(resolved)) {
            throw new IOException(
                    "Input file does not exist: " + resolved
            );
        }

        List<String> lines = Files.readAllLines(
                resolved, StandardCharsets.UTF_8
        );

        List<String> content = new ArrayList<>();

        for (String line : lines) {
            if (!line.isBlank()) {
                content.add(line.trim());
            }
        }

        if (!content.isEmpty()
                && content.stream().allMatch(l -> l.matches("[.x]+"))) {
            return new InputData(0, parseRows(content, 0, content.size()));
        }

        if (content.size() < 2) {
            throw new IOException(
                    "Input must contain generations and field size."
            );
        }

        int generations = parseNonNegative(
                content.get(0), "generations"
        );

        int width;
        int height;
        int first;
        String[] size = content.get(1).split("\\s+");

        if (size.length == 2) {
            width = parsePositive(size[0], "width");
            height = parsePositive(size[1], "height");
            first = 2;
        } else {
            if (content.size() < 3) {
                throw new IOException(
                        "Input must contain generations, width and height."
                );
            }
            width = parsePositive(content.get(1), "width");
            height = parsePositive(content.get(2), "height");
            first = 3;
        }

        if (content.size() != height + first) {
            throw new IOException(
                    "Expected " + height + " grid rows, but found "
                            + (content.size() - first) + "."
            );
        }

        for (int row = 0; row < height; row++) {
            if (content.get(row + first).length() != width) {
                throw new IOException(
                        "Row " + (row + 1)
                                + " must contain exactly " + width
                                + " characters."
                );
            }
        }

        return new InputData(
                generations, parseRows(content, first, content.size())
        );
    }

    private static int[][] parseRows(
            List<String> content, int from, int to
    ) throws IOException {
        int height = to - from;
        int width = content.get(from).length();
        int[][] grid = new int[height][width];

        for (int row = 0; row < height; row++) {
            String line = content.get(from + row);

            if (line.length() != width) {
                throw new IOException(
                        "Row " + (row + 1)
                                + " must contain exactly " + width
                                + " characters."
                );
            }

            for (int col = 0; col < width; col++) {
                char ch = line.charAt(col);

                if (ch == 'x') {
                    grid[row][col] = 1;
                } else if (ch == '.') {
                    grid[row][col] = 0;
                } else {
                    throw new IOException(
                            "Invalid character '" + ch
                                    + "' at row " + (row + 1) + "."
                    );
                }
            }
        }

        return grid;
    }

    public static void writeOutput(Path path, int[][] grid)
            throws IOException {
        GameOfLifeLogic.validateGrid(grid);
        writeText(path, rows(grid));
    }

    private static String rows(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row) {
                sb.append(cell == 1 ? 'x' : '.');
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static void writeText(Path path, String text)
            throws IOException {
        Path resolved = resolvePath(path);
        Path parent = resolved.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(resolved, text, StandardCharsets.UTF_8);
    }

    public static void writeOutput(
            Path path, int[][] grid, int generations
    ) throws IOException {
        GameOfLifeLogic.validateGrid(grid);

        if (generations < 0) {
            throw new IllegalArgumentException(
                    "Generations cannot be negative."
            );
        }

        Path resolved = resolvePath(path);
        Path parent = resolved.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder output = new StringBuilder();

        output.append(generations)
                .append(System.lineSeparator());
        output.append(grid[0].length)
                .append(System.lineSeparator());
        output.append(grid.length)
                .append(System.lineSeparator());

        for (int[] row : grid) {
            for (int cell : row) {
                output.append(cell == 1 ? 'x' : '.');
            }

            output.append(System.lineSeparator());
        }

        Files.writeString(
                resolved, output.toString(), StandardCharsets.UTF_8
        );
    }

    private static int parsePositive(
            String value, String name
    ) throws IOException {
        int number = parseNonNegative(value, name);

        if (number == 0) {
            throw new IOException(
                    name + " must be greater than zero."
            );
        }

        return number;
    }

    private static int parseNonNegative(
            String value, String name
    ) throws IOException {
        try {
            int number = Integer.parseInt(value);

            if (number < 0) {
                throw new IOException(
                        name + " cannot be negative."
                );
            }

            return number;
        } catch (NumberFormatException ex) {
            throw new IOException(
                    "Invalid " + name + ": " + value, ex
            );
        }
    }
}