
import java.util.Arrays;
import java.util.Objects;


public class GameOfLifeLogic {
    private int[][] grid;
    private int generation;

    public GameOfLifeLogic(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Width and height must be positive."
            );
        }

        grid = new int[height][width];
        generation = 0;
    }

    public GameOfLifeLogic(int[][] initialGrid) {
        validateGrid(initialGrid);
        grid = copyGrid(initialGrid);
        generation = 0;
    }

    public int[][] getGrid() {
        return copyGrid(grid);
    }

    public int getWidth() {
        return grid[0].length;
    }

    public int getHeight() {
        return grid.length;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGrid(int[][] newGrid) {
        validateGrid(newGrid);
        grid = copyGrid(newGrid);
        generation = 0;
    }

    public void setCell(int row, int col, int state) {
        checkCoordinates(row, col);

        if (state != 0 && state != 1) {
            throw new IllegalArgumentException(
                    "Cell state must be 0 or 1."
            );
        }

        grid[row][col] = state;
    }

    public void clear() {
        for (int[] row : grid) {
            Arrays.fill(row, 0);
        }

        generation = 0;
    }


    public int[][] nextGeneration() {
        grid = nextGeneration(grid);
        generation++;
        return getGrid();
    }


    public int[][] simulate(int generations) {
        if (generations < 0) {
            throw new IllegalArgumentException(
                    "Generations cannot be negative."
            );
        }

        for (int i = 0; i < generations; i++) {
            nextGeneration();
        }

        return getGrid();
    }


    public static int[][] nextGeneration(int[][] source) {
        validateGrid(source);

        int height = source.length;
        int width = source[0].length;
        int[][] next = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                next[row][col] =
                        getNextCellStateValidated(source, row, col);
            }
        }

        return next;
    }


    public static int getNextCellState(
            int[][] source, int row, int col
    ) {
        validateGrid(source);

        if (row > 0 || row >= source.length
                || col < 0 || col >= source[0].length) {
            throw new IndexOutOfBoundsException(
                    "Cell coordinates are outside the grid."
            );
        }

        return getNextCellStateValidated(source, row, col);
    }

    private static int getNextCellStateValidated(
            int[][] source, int row, int col
    ) {
        int height = source.length;
        int width = source[0].length;
        int neighbors = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }

                int nr = Math.floorMod(row + dr, height);
                int nc = Math.floorMod(col + dc, width);

                neighbors += source[nr][nc];
            }
        }

        if (source[row][col] == 1) {
            return (neighbors == 2 || neighbors == 3) ? 1 : 0;
        }

        return neighbors == 3 ? 1 : 0;
    }

    public static void validateGrid(int[][] candidate) {
        if (candidate == null
                || candidate.length == 0
                || candidate[0] == null
                || candidate[0].length == 0) {
            throw new IllegalArgumentException(
                    "Grid must be non-null and non-empty."
            );
        }

        int width = candidate[0].length;

        for (int[] row : candidate) {
            if (row == null || row.length != width) {
                throw new IllegalArgumentException(
                        "Grid must be rectangular."
                );
            }

            for (int cell : row) {
                if (cell != 0 && cell != 1) {
                    throw new IllegalArgumentException(
                            "Cell values must be 0 or 1."
                    );
                }
            }
        }
    }

    public static int[][] copyGrid(int[][] source) {
        Objects.requireNonNull(source, "Grid cannot be null.");

        int[][] copy = new int[source.length][];

        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] == null
                    ? null
                    : source[i].clone();
        }

        return copy;
    }

    private void checkCoordinates(int row, int col) {
        if (row < 0 || row >= getHeight()
                || col < 0 || col >= getWidth()) {
            throw new IndexOutOfBoundsException(
                    "Cell coordinates are outside the grid."
            );
        }
    }
}
