package org.example;

import javax.swing.*;
import java.awt.*;
import mpi.MPI;

public class WelcomePanel extends JPanel {


        private final JTextField xField;
        private final JTextField yField;
        private final JTextField trainsField;
        private static int GAclickCounter = 0;
        private boolean fixedDataSent = false;

        public WelcomePanel(JPanel root, CardLayout cardLayout) {

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(20));

            JLabel titleLabel = new JLabel("Welcome to Railroads");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(titleLabel);
            add(Box.createVerticalStrut(20));
            JPanel dimPanel = new JPanel();
            JLabel dimLabel = new JLabel("Grid dimensions (X × Y): ");
            xField = new JTextField(3);
            yField = new JTextField(3);

            dimPanel.add(dimLabel);
            dimPanel.add(new JLabel("X:"));
            dimPanel.add(xField);
            dimPanel.add(new JLabel("Y:"));
            dimPanel.add(yField);
            add(dimPanel);

            JPanel trainsPanel = new JPanel();
            JLabel trainsLabel = new JLabel("Number of trains: ");
            trainsField = new JTextField(5);
            trainsPanel.add(trainsLabel);
            trainsPanel.add(trainsField);
            add(trainsPanel);

            add(Box.createVerticalStrut(20));

            JButton okButton = new JButton("OK");
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(okButton);

            add(Box.createVerticalStrut(20));

            okButton.addActionListener(e -> {
                String xText = xField.getText().trim();
                String yText = yField.getText().trim();
                String trainsText = trainsField.getText().trim();

                if (!xText.matches("\\d+") || !yText.matches("\\d+") || !trainsText.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "Enter valid dimensions and number of trains.");
                    return;
                }

                int cols = Integer.parseInt(xText);
                int rows = Integer.parseInt(yText);
                int numTrains = Integer.parseInt(trainsText);


                if (cols <= 0 || rows <= 0 || numTrains <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid values. Must be positive.");
                    return;
                }



                int totalCells = rows * cols;
                int maxAllowedTrainsSpatial = (totalCells - 1)/2;
                int maxLimit = 15;
                int maxAllowedTrains = Math.min(maxAllowedTrainsSpatial, maxLimit);

                if (numTrains > maxAllowedTrains){
                    JOptionPane.showMessageDialog(this, "Too many trains for this grid.\n"+
                            "Grid size: "+rows+" X "+cols+"="+totalCells+" cells. \n"+
                            "Each train needs a destination tile. \n");
                    //"Maximum allowed trains: "+maxAllowedTrains);
                    return;
                }

                Rectangle bounds = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getMaximumWindowBounds();

                int maxCols = (bounds.width - 2 * Configurations.GRID_PADDING) / Configurations.TILE_SIZE;
                int maxRows = (bounds.height - 2 * Configurations.GRID_PADDING) / Configurations.TILE_SIZE;

                maxCols = Math.max(1, maxCols);
                maxRows = Math.max(1, maxRows);

                if (cols > maxCols || rows > maxRows) {
                    JOptionPane.showMessageDialog(this,
                            "Your screen cannot display that many tiles" + ".\n" +
                                    "Maximum allowed dimensions: \n" + "X: " + maxCols + "\n" + "Y: " + maxRows + "\n" +
                                    "Please enter smaller dimensions.");
                    return;
                }

                BoardPanel boardPanel;
                try{
                    boardPanel = new BoardPanel(rows, cols, numTrains);
                } catch (IllegalStateException ex) {
                    JOptionPane.showMessageDialog(this, "Too many trains for this grid.\n"+
                            "Grid size: "+rows+" X "+cols+"="+totalCells+" cells. \n"+
                            "Each train needs a destination tile. \n");
                    //"Maximum allowed trains: "+maxAllowedTrains);
                    return;
                }

                JButton showInformation = new JButton("Show Train Information");
                showInformation.addActionListener(ev -> {
                    String infoText = boardPanel.getTrainsInfo();
                    JFrame informationFrame = new JFrame("Train - Destination Info");
                    informationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JTextArea ta = new JTextArea(infoText);
                    informationFrame.add(new JScrollPane(ta));

                    informationFrame.setSize(400, 300);
                    informationFrame.setLocationRelativeTo(null);
                    informationFrame.setVisible(true);
                });

                JButton evaluatingButton = new JButton("Evaluator");
                evaluatingButton.addActionListener(eve -> {



                        boardPanel.evaluateCurrentBoard();

                        boardPanel.testPathsForALLtrains();

            });
                JButton GAbutton = new JButton("Genetic Algorithm");
                GAbutton.addActionListener(x -> {
                    new Thread(() -> {
                        int[][] boardCopy = boardPanel.getCopyOfBoard();
                        int[][] initialCopy = boardPanel.getCopyOfInitialBoard();
                        int rowss=boardCopy.length;
                        int colss=boardCopy[0].length;
                        int trainPairCount = boardPanel.getTrainPairCount();
                        int[] settings = {rowss, colss, trainPairCount};

                        int[] flatInitialBoard=new int[rowss * colss];
                        for(int r=0; r<rowss;r++){
                            for(int c=0;c<colss;c++){
                                flatInitialBoard[r*colss+c] = initialCopy[r][c];
                            }
                        }

                        Train[] trains = boardPanel.getTrains();
                        int[] flatTrains = new int[trainPairCount*4];

                        for(int i=0;i<trainPairCount;i++){
                            flatTrains[i*4] = trains[i].getStartingRow();
                            flatTrains[i*4+1]=trains[i].getStartingColumn();
                            flatTrains[i*4+2]=trains[i].getDestinationRow();
                            flatTrains[i*4+3]=trains[i].getDestinationColumn();
                        }

                        if(!fixedDataSent) {
                            for (int worker = 1; worker < MPI.COMM_WORLD.Size(); worker++) {
                                MPI.COMM_WORLD.Send(settings, 0, 3, MPI.INT, worker, 0);

                                MPI.COMM_WORLD.Send(flatInitialBoard, 0, flatInitialBoard.length, MPI.INT, worker, 1);

                                MPI.COMM_WORLD.Send(flatTrains, 0, flatTrains.length, MPI.INT, worker, 2);
                            }
                            fixedDataSent = true;
                        }




                        GAclickCounter++;
                        long seed = Configurations.RANDOM_SEED + GAclickCounter;
                        Algo ga = new Algo(boardPanel.getEvaluator(), boardPanel.getTrains(), boardPanel.getTrainPairCount());
                        ga.run(boardCopy, initialCopy, boardPanel);
                    }).start();

                });

                JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
                controls.setBorder(BorderFactory.createEmptyBorder(6,8,8,8));
                controls.add(showInformation);
                controls.add(evaluatingButton);
                controls.add(GAbutton);

                JPanel boardCard = new JPanel(new BorderLayout());
                boardCard.add(boardPanel, BorderLayout.CENTER);
                boardCard.add(controls, BorderLayout.SOUTH);

                //boardPanel.add(showInformation);
                //boardPanel.add(evaluatingButton);





                root.add(boardCard, "BOARD");
                cardLayout.show(root, "BOARD");
                //boardPanel.add(GAbutton);

                //tuka imas nov layout poso redis board panel
                //i sega na card layout najgore ti e BOARD
                //i zato na 142 moras da naprajs pack
                //windowAncestor od root ti e samo frameot
                SwingUtilities.getWindowAncestor(root).pack();
            });
        }
    }

