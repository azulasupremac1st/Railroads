package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BoardPanel extends JPanel {


        private final int rows;
        private final int cols;
        private final int numTrains;

        private static final int EMPTY = -1;
        private static final int TRAIN = -2;
        private static final int DESTINATION = -3;
        private final JPopupMenu menu;
        private int lastClickedR = -1;
        private int lastClickedC = -1;

        public final int[] trainRow;
        public final int[] trainColumn;
        public final int[] destinationRow;
        public final int[] destinationColumn;
        public int trainCount;
        public int destinationCount;
        public final Train[] trains;
        public int trainPairCount;
        private final int[][] initialBoardTypes;
        public final GameEvaluator evaluator;

        public GameEvaluator getEvaluator(){
            return evaluator;
        }
        public int getTrainPairCount() {
            return trainPairCount;
        }

        // one image
        //private final Image tileImage;

        // random cell where the image will be drawn
        //private  int tileRow;
        //private  int tileCol;

        private static final int NUM_OF_TILE_TYPES = 11;
        private static final Image[] TILE_IMAGES = new Image[]{
                new ImageIcon(Main.class.getResource("/grid/tile0.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile1.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile2.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile3.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile4.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile5.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile6.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile7.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile8.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile9.png")).getImage(),
                new ImageIcon(Main.class.getResource("/grid/tile10.png")).getImage()
        };

        private static final Image TRAIN_IMAGE = new ImageIcon(Main.class.getResource("/trains/train.png")).getImage();
        private static final Image DESTINATION_IMAGE = new ImageIcon(Main.class.getResource("/destiations/destination.png")).getImage();

        private static final String[] TILE_NAMES = {
                "Cross",
                "Vertical",
                "Horizontal",
                "Down-Right",
                "Down-Left",
                "Up-Left",
                "Up-Right",
                "Left-Down-Right",
                "Up-Right-Down",
                "Left-Up-Right",
                "Up-Left-Down",
        };


        private final int[][] boardTypes;

        public int getRowCount(){
            return rows;
        }

        public int getColumnCount(){
            return cols;
        }
        public Train[] getTrains() { return trains;}

        public void onTileClicked(int row, int col){
            int tileType = boardTypes[row][col];
            if (tileType == TRAIN || tileType == DESTINATION){
                JOptionPane.showMessageDialog(this, "You can't change train or destination tiles!", "No", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            //System.out.println("tile clicked at row = "+row+" and column: " +col);
            lastClickedR = row;
            lastClickedC = col;
            int tileX = Configurations.GRID_PADDING + col*Configurations.TILE_SIZE;
            int tileY = Configurations.GRID_PADDING + row*Configurations.TILE_SIZE;
            menu.show(this, tileX, tileY);
        }

        public String getTrainsInfo(){
            String text = "Train and Destination pairs: \n\n";
            for(int i=0 ;i<trainPairCount; i++){
                Train t = trains[i];
                text += "T" + (i+1) + ": ("+ t.getStartingRow()+","+t.getStartingColumn()+") -> ("+t.getDestinationRow()+","+t.getDestinationColumn()+")\n";

            }
            return text;
        }

        public void evaluateCurrentBoard() {
            int score = evaluator.evaluateTiles(initialBoardTypes, boardTypes, trains, trainPairCount);
            System.out.println("Score = "+score);
        }

        public void testPathsForALLtrains(){
            for(int i=0; i<trainPairCount; i++){
                Train t = trains[i];
                boolean result = evaluator.existsPath(boardTypes, t.getStartingRow(), t.startingColumn, t.destinationRow, t.getDestinationColumn());
                System.out.println("Train "+(i+1)+": path exists? "+result);
            }
        }

        public int[][] getCopyOfBoard(){
            int[][] copy =  new int[rows][cols];
            for(int i=0;i<rows;i++){
                for (int j=0;j<cols;j++){
                    copy[i][j] = boardTypes[i][j];
                }
            }
            return copy;
        }

        public int[][] getCopyOfInitialBoard(){
            int[][] copy = new int[initialBoardTypes.length][initialBoardTypes[0].length];
            for(int r=0;r< initialBoardTypes.length;r++){
                for(int c=0;c<initialBoardTypes[0].length; c++){
                    copy[r][c] = initialBoardTypes[r][c];
                }
            }
            return copy;
        }

        public void applyBoard(int[][] newBoard){
            for(int r=0;r<rows;r++){
                for(int c=0;c<cols;c++){
                    boardTypes[r][c] = newBoard[r][c];
                }
            }
            repaint();

        }



        public BoardPanel(int rows, int cols, int numTrains) {
            this.rows = rows;
            this.cols = cols;
            this.numTrains = numTrains;
            trainRow = new int[numTrains];
            trainColumn = new int[numTrains];
            destinationRow = new int[numTrains];
            destinationColumn = new int[numTrains];
            trainCount=0;
            destinationCount=0;
            trains = new Train[numTrains];
            trainPairCount=0;
            this.evaluator = new GameEvaluator();





            Random rng = new Random(Configurations.RANDOM_SEED);
            //int tileRow = rng.nextInt(rows);
            //int tileCol = rng.nextInt(cols);

            int width = cols * Configurations.TILE_SIZE + Configurations.GRID_PADDING * 2;
            int height = rows * Configurations.TILE_SIZE + Configurations.GRID_PADDING * 2;

            setPreferredSize(new Dimension(width, height));
            setBackground(Color.WHITE);

            boardTypes = new int[rows][cols];
            boolean[][] reserved = new boolean[rows][cols];

            for(int i=0; i<numTrains; i++){
                int tries=0;
                while(true){
                    tries++;
                    if(tries>rows*cols*50){
                        throw new IllegalStateException("PLACEMENT FAILED");
                    }

                    int r=rng.nextInt(rows);
                    int c=rng.nextInt(cols);
                    if(!reserved[r][c] && safeReservation(r,c,reserved)){
                        reserved[r][c]=true;
                        boardTypes[r][c]=TRAIN;
                        trainRow[i]=r;
                        trainColumn[i]=c;
                        trainCount++;
                        break;
                    }
                }
            }

            for(int i=0;i<numTrains;i++){
                int tries=0;
                while(true){
                    tries++;
                    if(tries>rows*cols*50){
                        throw new IllegalStateException("PLACEMENT FAILED");
                    }

                    int r=rng.nextInt(rows);
                    int c=rng.nextInt(cols);

                    if(!reserved[r][c] && safeReservation(r,c,reserved)){
                        reserved[r][c] = true;
                        boardTypes[r][c]=DESTINATION;
                        destinationRow[i]=r;
                        destinationColumn[i]=c;
                        destinationCount++;
                        break;
                    }

                }
            }

            for(int r=0;r<rows;r++){
                for(int c=0;c<cols;c++){
                    if(!reserved[r][c]){
                        boardTypes[r][c] = rng.nextInt(TILE_IMAGES.length);
                    }
                }
            }

            initialBoardTypes=new int[rows][cols];
            for(int r=0;r<rows;r++){
                for (int c=0;c<cols;c++){
                    initialBoardTypes[r][c] = boardTypes[r][c];
                }
            }

            for(int i=0;i<numTrains; i++){
                trains[i] = new Train(trainRow[i], trainColumn[i], destinationRow[i], destinationColumn[i]);
            }

            trainPairCount=numTrains;

            menu = new JPopupMenu();
            for(int i=0;i<NUM_OF_TILE_TYPES;i++){
                final int tileType=i;
                JMenuItem item = new JMenuItem(TILE_NAMES[tileType]);
                item.addActionListener(e-> {
                    if (lastClickedR<0 || lastClickedC <0) return;
                    boardTypes[lastClickedR][lastClickedC] = tileType;
                    repaint();
                });
                menu.add(item);
            }

            BoardClick boardClick = new BoardClick(this);
            addMouseListener(boardClick);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            int startX = Configurations.GRID_PADDING;
            int startY = Configurations.GRID_PADDING;

            // bkg behind the grid
            g2.setColor(new Color(230, 230, 230));
            g2.fillRect(startX, startY, cols * Configurations.TILE_SIZE, rows * Configurations.TILE_SIZE);


            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {

                    int x = startX + c * Configurations.TILE_SIZE;
                    int y = startY + r * Configurations.TILE_SIZE;

                    int tileType = boardTypes[r][c];

                    if(tileType>=0){
                        Image image = TILE_IMAGES[tileType];
                        g2.drawImage(image, x, y, Configurations.TILE_SIZE, Configurations.TILE_SIZE, this);
                    } else if (tileType == TRAIN) {
                        g2.drawImage(TRAIN_IMAGE, x, y, Configurations.TILE_SIZE, Configurations.TILE_SIZE, this);
                    } else if (tileType == DESTINATION) {
                        g2.drawImage(DESTINATION_IMAGE, x, y, Configurations.TILE_SIZE, Configurations.TILE_SIZE, this);
                    }



                    g2.setColor(Color.DARK_GRAY);
                    g2.drawRect(x, y, Configurations.TILE_SIZE, Configurations.TILE_SIZE);
                }
            }


            g2.setColor(Color.BLACK);
            g2.drawString("Grid: " + cols + " × " + rows + " | Trains: " + numTrains,
                    10, getHeight() - 10);



        }

        private boolean hasFreeNeighbour(int r, int c, boolean[][] reserved){
            int free=0;
            if(r>0 && !reserved[r-1][c]) free++;
            if(r<rows-1 && !reserved[r+1][c]) free++;
            if(c>0 && !reserved[r][c-1]) free++;
            if(c<cols-1 && !reserved[r][c+1]) free++;

            return free >=1;
        }

        private boolean safeReservation(int r, int c, boolean[][] reserved){
            if(reserved[r][c]) return false;
            reserved[r][c] = true;
            boolean okej = true;
            okej &= hasFreeAfter(r,c,reserved);

            if(r>0 && reserved[r-1][c]) okej &= hasFreeAfter(r-1, c, reserved);
            if(r<rows-1 && reserved[r+1][c]) okej &= hasFreeAfter(r+1, c, reserved);
            if(c>0 && reserved[r][c-1]) okej &= hasFreeAfter(r,c-1, reserved);
            if(c<cols-1 && reserved[r][c+1]) okej&= hasFreeAfter(r,c+1,reserved);

            reserved[r][c] = false;
            return okej;

        }

        private boolean hasFreeAfter(int r, int c, boolean[][] reserved){
            int free=0;
            if(r>0 && !reserved[r-1][c]) free++;
            if(r<rows-1 && !reserved[r+1][c]) free++;
            if(c>0 && !reserved[r][c-1]) free++;
            if(c<cols-1 && !reserved[r][c+1]) free++;

            return free>=1;
        }

}
