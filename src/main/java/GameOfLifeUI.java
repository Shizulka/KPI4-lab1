import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameOfLifeUI extends JFrame {
    private static final int CELL_SIZE = 18;

    private final BoardPanel board = new BoardPanel();
    private final JLabel generationLabel =
            new JLabel("Generation: 0");
    private final JButton startButton = new JButton("Start");
    private final Timer timer;

    private GameOfLifeLogic game =
            new GameOfLifeLogic(30, 20);

    public GameOfLifeUI() {
        super("Conway's Game of Life");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(board, BorderLayout.CENTER);

        JPanel controls = new JPanel(
                new GridLayout(1, 5, 6, 6)
        );

        JButton clearButton = new JButton("Clear");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");

        controls.add(startButton);
        controls.add(clearButton);
        controls.add(saveButton);
        controls.add(loadButton);
        controls.add(generationLabel);

        add(controls, BorderLayout.SOUTH);

        timer = new Timer(150, event -> {
            game.nextGeneration();
            updateStatus();
            board.repaint();
        });

        startButton.addActionListener(
                event -> toggleSimulation()
        );

        clearButton.addActionListener(event -> clearGame());

        saveButton.addActionListener(
                event -> saveToFile(GameOfLifeIO.defaultPath())
        );

        loadButton.addActionListener(
                event -> loadToUI(GameOfLifeIO.defaultPath())
        );

        board.setPreferredSize(new Dimension(
                game.getWidth() * CELL_SIZE,
                game.getHeight() * CELL_SIZE
        ));

        pack();
        setLocationRelativeTo(null);
    }

    private void toggleSimulation() {
        if (timer.isRunning()) {
            stopSimulation();
        } else {
            timer.start();
            startButton.setText("Stop");
        }
    }

    private void stopSimulation() {
        timer.stop();
        startButton.setText("Start");
    }

    private void updateStatus() {
        generationLabel.setText(
                "Generation: " + game.getGeneration()
        );
    }

    private void loadToUI(Path path) {
        try {
            loadFromFile(path);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Load error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    void saveToFile(Path path) {
        try {
            GameOfLifeIO.writeOutput(
                    path, game.getGrid(), game.getGeneration()
            );
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Save error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    void loadFromFile(Path path) throws IOException {
        GameOfLifeIO.InputData input =
                GameOfLifeIO.readInput(path);

        GameOfLifeLogic replacement =
                new GameOfLifeLogic(input.grid());

        stopSimulation();
        game = replacement;

        board.setPreferredSize(new Dimension(
                game.getWidth() * CELL_SIZE,
                game.getHeight() * CELL_SIZE
        ));

        updateStatus();
        board.revalidate();
        board.repaint();
        pack();
    }

    void clearGame() {
        stopSimulation();
        game.clear();
        updateStatus();
        board.repaint();
    }

    private final class BoardPanel extends JPanel {
        private int lastRow = -1;
        private int lastCol = -1;
        private int targetState = -1;

        BoardPanel() {
            setBackground(Color.WHITE);

            java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent event) {
                    handleMouse(event);
                }

                @Override
                public void mouseDragged(java.awt.event.MouseEvent event) {
                    handleMouse(event);
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent event) {
                    lastRow = -1;
                    lastCol = -1;
                    targetState = -1;
                }

                private void handleMouse(java.awt.event.MouseEvent event) {
                    int col = event.getX() / CELL_SIZE;
                    int row = event.getY() / CELL_SIZE;

                    if (row >= 0 && row < game.getHeight()
                            && col >= 0 && col < game.getWidth()) {

                        if (row != lastRow || col != lastCol) {
                            int[][] current = game.getGrid();

                            if (targetState == -1) {
                                targetState = current[row][col] == 1 ? 0 : 1;
                            }

                            game.setCell(row, col, targetState);
                            lastRow = row;
                            lastCol = col;
                            repaint();
                        }
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            int[][] current = game.getGrid();

            for (int row = 0; row < current.length; row++) {
                for (int col = 0;
                     col < current[row].length; col++) {
                    int x = col * CELL_SIZE;
                    int y = row * CELL_SIZE;

                    if (current[row][col] == 1) {
                        graphics.setColor(
                                new Color(55, 120, 75)
                        );
                        graphics.fillRect(
                                x + 1, y + 1,
                                CELL_SIZE - 2, CELL_SIZE - 2
                        );
                    }

                    graphics.setColor(
                            new Color(220, 220, 220)
                    );
                    graphics.drawRect(
                            x, y, CELL_SIZE, CELL_SIZE
                    );
                }
            }
        }
    }
}