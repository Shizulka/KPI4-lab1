import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameOfLifeEvolutionTest {

    private static int[][] empty(int width, int height) {
        return new int[height][width];
    }

    private static int[][] blockAt(int[][] g, int r, int c) {
        g[r][c] = g[r][c + 1] = g[r + 1][c] = g[r + 1][c + 1] = 1;
        return g;
    }

    @Test
    void blockIsStillLife() {
        int[][] g = blockAt(empty(6, 6), 2, 2);
        assertArrayEquals(g, GameOfLifeLogic.nextGeneration(g));
    }

    @Test
    void blinkerOscillatesWithPeriodTwo() {
        int[][] horizontal = empty(5, 5);
        horizontal[2][1] = horizontal[2][2] = horizontal[2][3] = 1;
        int[][] vertical = empty(5, 5);
        vertical[1][2] = vertical[2][2] = vertical[3][2] = 1;

        assertArrayEquals(vertical, GameOfLifeLogic.nextGeneration(horizontal));
        assertArrayEquals(horizontal, GameOfLifeLogic.nextGeneration(vertical));
    }

    @Test
    void gliderMovesDiagonallyAfterFourGenerations() {
        int[][] g = empty(8, 8);
        g[0][1] = g[1][2] = g[2][0] = g[2][1] = g[2][2] = 1;

        int[][] expected = empty(8, 8);
        expected[1][2] = expected[2][3] = expected[3][1]
                = expected[3][2] = expected[3][3] = 1;

        assertArrayEquals(expected, new GameOfLifeLogic(g).simulate(4));
    }

    @Test
    void lonelyCellsDisappearInOneStep() {
        int[][] g = empty(7, 7);
        g[1][1] = 1;
        g[5][5] = 1;
        assertArrayEquals(empty(7, 7), GameOfLifeLogic.nextGeneration(g));
    }

    @Test
    void nextGenerationDoesNotModifySourceGrid() {
        int[][] g = empty(5, 5);
        g[2][1] = g[2][2] = g[2][3] = 1;
        int[][] snapshot = GameOfLifeLogic.copyGrid(g);

        GameOfLifeLogic.nextGeneration(g);

        assertArrayEquals(snapshot, g);
    }

    @Test
    void updateIsSimultaneous() {
        int[][] g = empty(5, 5);
        g[1][1] = g[1][2] = g[1][3] = g[2][2] = 1;

        int[][] expected = empty(5, 5);
        expected[0][2] = expected[1][1] = expected[1][2]
                = expected[1][3] = expected[2][1] = expected[2][2]
                = expected[2][3] = 1;
        int[][] actual = GameOfLifeLogic.nextGeneration(g);
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                assertEquals(
                        GameOfLifeLogic.getNextCellState(g, r, c),
                        actual[r][c],
                        "cell " + r + "," + c);
            }
        }
    }

    @Test
    void instanceNextGenerationIncrementsGenerationAndUpdatesGrid() {
        GameOfLifeLogic logic = new GameOfLifeLogic(5, 5);
        logic.setCell(2, 1, 1);
        logic.setCell(2, 2, 1);
        logic.setCell(2, 3, 1);

        int[][] result = logic.nextGeneration();

        assertEquals(1, logic.getGeneration());
        assertArrayEquals(result, logic.getGrid());
        assertEquals(1, result[1][2]);
        assertEquals(1, result[2][2]);
        assertEquals(1, result[3][2]);
        assertEquals(0, result[2][1]);
    }

    @Test
    void simulateZeroGenerationsKeepsGrid() {
        int[][] g = blockAt(empty(5, 5), 1, 1);
        GameOfLifeLogic logic = new GameOfLifeLogic(g);
        assertArrayEquals(g, logic.simulate(0));
        assertEquals(0, logic.getGeneration());
    }

    @Test
    void simulateNegativeGenerationsThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameOfLifeLogic(3, 3).simulate(-1));
    }

    @Test
    void clearAndSetGridResetGeneration() {
        GameOfLifeLogic logic = new GameOfLifeLogic(blockAt(empty(5, 5), 1, 1));
        logic.simulate(3);
        logic.clear();
        assertEquals(0, logic.getGeneration());
        assertArrayEquals(empty(5, 5), logic.getGrid());

        logic.simulate(2);
        logic.setGrid(empty(3, 3));
        assertEquals(0, logic.getGeneration());
    }

    @Test
    void invalidGridsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeLogic.nextGeneration(new int[][]{{0, 1}, {1}}));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeLogic.nextGeneration(new int[][]{{0, 2}}));
        assertThrows(IllegalArgumentException.class,
                () -> GameOfLifeLogic.nextGeneration(new int[0][0]));
    }
}