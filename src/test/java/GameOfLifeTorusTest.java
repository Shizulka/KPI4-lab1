import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class GameOfLifeTorusTest {

    private static int[][] empty(int width, int height) {
        return new int[height][width];
    }

    @Test
    void leftAndRightEdgesAreNeighbors() {
        int[][] g = empty(5, 5);
        g[1][4] = 1;
        g[2][4] = 1;
        g[3][4] = 1;
        assertEquals(1, GameOfLifeLogic.getNextCellState(g, 2, 0));
    }

    @Test
    void topAndBottomEdgesAreNeighbors() {
        int[][] g = empty(5, 5);
        g[4][1] = 1;
        g[4][2] = 1;
        g[4][3] = 1;
        assertEquals(1, GameOfLifeLogic.getNextCellState(g, 0, 2));
    }

    @Test
    void cornersAreNeighborsDiagonallyAcrossTheBoard() {
        int[][] g = empty(5, 5);
        g[0][4] = 1;
        g[4][0] = 1;
        g[4][4] = 1;
        assertEquals(1, GameOfLifeLogic.getNextCellState(g, 0, 0));
    }

    @Test
    void aliveCornerWithOnlyWrappedNeighborDies() {
        int[][] g = empty(5, 5);
        g[0][0] = 1;
        g[4][4] = 1;
        assertEquals(0, GameOfLifeLogic.getNextCellState(g, 0, 0));
    }

    @Test
    void blinkerWrappingAcrossEdgeOscillates() {
        int[][] g = empty(5, 5);
        g[4][2] = 1;
        g[0][2] = 1;
        g[1][2] = 1;

        int[][] expected = empty(5, 5);
        expected[0][1] = 1;
        expected[0][2] = 1;
        expected[0][3] = 1;

        int[][] gen1 = GameOfLifeLogic.nextGeneration(g);
        assertArrayEquals(expected, gen1);
        assertArrayEquals(g, GameOfLifeLogic.nextGeneration(gen1));
    }


    @Test
    void oneByOneAliveCellDiesBecauseItIsItsOwnEightNeighbors() {
        assertArrayEquals(new int[][]{{0}},
                GameOfLifeLogic.nextGeneration(new int[][]{{1}}));
    }

    @Test
    void fullTorusDiesOfOvercrowding() {
        int[][] full = new int[4][4];
        for (int[] row : full) java.util.Arrays.fill(row, 1);
        assertArrayEquals(empty(4, 4), GameOfLifeLogic.nextGeneration(full));
    }
}