import java.io.IOException;
import java.nio.file.Path;
import javax.swing.SwingUtilities;

public final class GameOfLifeMain {
    private GameOfLifeMain() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(
                    () -> new GameOfLifeUI().setVisible(true)
            );
            return;
        }

        if (args.length != 2) {
            System.err.println(
                    "Usage: java GameOfLifeMain "
                            + "[<input-file> <output-file>]"
            );
            System.exit(2);
            return;
        }

        try {
            GameOfLifeIO.InputData input =
                    GameOfLifeIO.readInput(Path.of(args[0]));

            GameOfLifeLogic game =
                    new GameOfLifeLogic(input.grid());

            int[][] result =
                    game.simulate(input.generations());

            GameOfLifeIO.writeOutput(Path.of(args[1]), result);
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println(
                    "Game of Life error: " + ex.getMessage()
            );
            System.exit(1);
        }
    }
}