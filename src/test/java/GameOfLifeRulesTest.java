import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameOfLifeRulesTest {

    private static int next(int[][] g) {
        return GameOfLifeLogic.getNextCellState(g, 2, 2);
    }
    static int[][] empty(int width, int height) {
        return new int[height][width];
    }

    private static int[][] withNeighbors(int centre, int n) {
        int[][] g = empty(5, 5);
        int[][] offsets = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1},
                {0, 1}, {1, -1}, {1, 0}, {1, 1}};
        for (int i = 0; i < n; i++) {
            g[2 + offsets[i][0]][2 + offsets[i][1]] = 1;
        }
        g[2][2] = centre;
        return g;
    }

    @Test
    void aliveCellWithNoNeighborsDies() {
        assertEquals(0, next(withNeighbors(1, 0)));
    }

    @Test
    void aliveCellWithOneNeighborDies() {
        assertEquals(0, next(withNeighbors(1, 1)));
    }

    @Test
    void aliveCellWithTwoNeighborsSurvives() {
        assertEquals(1, next(withNeighbors(1, 2)));
    }

    @Test
    void aliveCellWithThreeNeighborsSurvives() {
        assertEquals(1, next(withNeighbors(1, 3)));
    }

    @Test
    void aliveCellWithFourToEightNeighborsDies() {
        for (int n = 4; n <= 8; n++) {
            assertEquals(0, next(withNeighbors(1, n)),
                    "alive cell with " + n + " neighbors");
        }
    }

    @Test
    void deadCellWithZeroToTwoNeighborsStaysDead() {
        for (int n = 0; n <= 2; n++) {
            assertEquals(0, next(withNeighbors(0, n)),
                    "dead cell with " + n + " neighbors");
        }
    }

    @Test
    void deadCellWithExactlyThreeNeighborsIsBorn() {
        assertEquals(1, next(withNeighbors(0, 3)));
    }

    @Test
    void deadCellWithFourToEightNeighborsStaysDead() {
        for (int n = 4; n <= 8; n++) {
            assertEquals(0, next(withNeighbors(0, n)),
                    "dead cell with " + n + " neighbors");
        }
    }


    @Test
    void cellFarAwayDoesNotAffectResult() {
        int[][] g = withNeighbors(0, 2);
        g[0][0] = 1;
        g[4][4] = 1;
        assertEquals(0, next(g));
    }


    @Test
    void emptyGridStaysEmpty() {
        assertArrayEquals(empty(6, 4),
                GameOfLifeLogic.nextGeneration(empty(6, 4)));
    }

}
