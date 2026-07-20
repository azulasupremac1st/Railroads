package org.example;

import java.sql.Array;
import java.util.Stack;

public class GameEvaluator {
    public final int[] tilePenalty = {
            3, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3
    };

    public final int[] thisTileOpensUp = {
            1, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1
    };

    public final int[] thisTileOpensRight = {
            1, 0, 1, 1, 0, 0, 1, 1, 1, 1, 0
    };

    public final int[] thisTileOpensDown = {
            1, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1
    };

    public final int[] thisTileOpensLeft = {
            1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1
    };

    private static final int EMPTY = -1;
    private static final int TRAIN = -2;
    private static final int DESTINATION = -3;

    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;

    private static class PathResult {
        boolean reached;
        int smallestDistance;

        PathResult(boolean reached, int smallestDistance){
            this.reached = reached;
            this.smallestDistance = smallestDistance;
        }
    }

    //daj initial board i praj score
    public int evaluateTiles(int[][] initialBoard, int[][] currentBoard, Train[] trains, int trainPairCount ){
        int rows = initialBoard.length;
        int columns = initialBoard[0].length;
        int changedTilesCount = 0;
        int score = 100;
        int swappingCost = 1;

        for(int r=0; r<rows; r++){
            for (int c=0;c<columns;c++){
                if (initialBoard[r][c] != currentBoard[r][c]){
                    score -= swappingCost;
                    int currentType = currentBoard[r][c];
                    if(currentType >= 0 && currentType < tilePenalty.length){
                        score -= tilePenalty[currentType];
                    }

                }
            }
        }

        int rewardForConnection = 1;
        for(int r=0;r<rows;r++){
            for (int c=0;c<columns;c++){
                int tile = currentBoard[r][c];
                if (tile<0) continue;

                if (r>0){
                    int neighbour = currentBoard[r-1][c];
                    if (neighbour>=0 && isConnectionValid(tile, neighbour, DIRECTION_UP)){
                        score += rewardForConnection;
                    }
                }

                if (c<columns-1){
                    int neighbour = currentBoard[r][c+1];
                    if (neighbour >= 0 && isConnectionValid(tile, neighbour, DIRECTION_RIGHT)){
                        score += rewardForConnection;
                    }
                }

                if (r<rows-1){
                    int neighbour = currentBoard[r+1][c];
                    if(neighbour>=0 && isConnectionValid(tile, neighbour, DIRECTION_DOWN)){
                        score += rewardForConnection;
                    }
                }
            }
        }
        int unreachableCount = 0;
        for(int i=0; i<trainPairCount; i++){
            Train t = trains[i];
            if(t==null){continue;}

            int startingRow = t.getStartingRow();
            int startingColumn = t.getStartingColumn();
            int destRow = t.getDestinationRow();
            int destColumn = t.getDestinationColumn();

            PathResult pathResult = DFS(currentBoard, startingRow, startingColumn, destRow, destColumn);
            if(pathResult.reached){
                score+=200;
            } else {
                unreachableCount++;
                score -= pathResult.smallestDistance*10;
            }



            /* if (pathExists){
                score += 50;
            } else {
                int m = Math.abs(startRow - destinationRow) + (startColumn - destinationColumn);
                score -= m;
            }

             */


        }
        score -= unreachableCount * 300;


        return score;
    }

    public boolean isConnectionValid(int currentTile, int neighbourTile, int direction){
        boolean currentOpens = opensInDirection(currentTile, direction);
        int opposite = oppositeDirection(direction);
        boolean neighbourOpens = opensInDirection(neighbourTile, opposite);
        return currentOpens && neighbourOpens;
    }

    private boolean opensInDirection(int tile, int direction){
        if(tile==TRAIN || tile == DESTINATION){
            return true;
        }
        if (tile<0 || tile >= thisTileOpensUp.length){
            return false;
        }
        if (direction == DIRECTION_UP){
            return thisTileOpensUp[tile] == 1;
        } else if (direction == DIRECTION_RIGHT){
            return thisTileOpensRight[tile] == 1;
        } else if (direction == DIRECTION_DOWN){
            return thisTileOpensDown[tile] == 1;
        } else if (direction == DIRECTION_LEFT ){
            return thisTileOpensLeft[tile] == 1;
        }
        return false;
    }

    private int oppositeDirection(int direction){
        if (direction == DIRECTION_UP) return  DIRECTION_DOWN;
        if (direction == DIRECTION_RIGHT) return DIRECTION_LEFT;
        if (direction == DIRECTION_DOWN) return DIRECTION_UP;
        if (direction == DIRECTION_LEFT) return DIRECTION_RIGHT;
        return -1;
    }

    public PathResult DFS (int[][] board, int startR, int startC, int destR, int destC){
        Stack<int[]> dfsStack = new Stack<>();
        int smallestDistance = board.length + board[0].length;
        boolean[][] visitedArr = new boolean[board.length][board[0].length];
        dfsStack.push(new int[] { startR, startC});

        while (!dfsStack.isEmpty()){
            int[] current = dfsStack.pop();
            int currentRow = current[0];
            int currentColumn = current[1];

            int currentDistance = Math.abs((destR - currentRow)+(destC-currentColumn));

            if(currentDistance<smallestDistance){ smallestDistance = currentDistance; }

            if (currentRow==destR && currentColumn==destC){
                return new PathResult(true, 0);
            }
            if (visitedArr[currentRow][currentColumn]){
                continue;
            }

            visitedArr[currentRow][currentColumn] = true;

            //neighbour above blok
            int newRow = currentRow - 1;
            int newColumn = currentColumn;
            int CURRENTtileType = board[currentRow][currentColumn];

            if(newRow >= 0){

                int NEIGHBOURtileType = board[newRow][newColumn];
                boolean bloked = false;

                if(NEIGHBOURtileType == TRAIN){
                    bloked = true;
                }

                if(NEIGHBOURtileType == DESTINATION && (newRow != destR && newColumn != destC)){
                    bloked = true;
                }
                if(!bloked && !visitedArr[newRow][newColumn] && isConnectionValid(CURRENTtileType, NEIGHBOURtileType, DIRECTION_UP)){
                    dfsStack.push(new int[]{ newRow, newColumn });
                }
            }



            //neighbour on the right blok
            newRow = currentRow;
            newColumn = currentColumn + 1;

            if(newColumn < board[0].length){

                int NEIGHBOURtileType = board[newRow][newColumn];
                boolean bloked = false;

                if(NEIGHBOURtileType == TRAIN){
                    bloked = true;
                }

                if(NEIGHBOURtileType == DESTINATION && (newRow != destR || newColumn != destC)){
                    bloked = true;
                }



                if(!bloked && !visitedArr[newRow][newColumn] && isConnectionValid(CURRENTtileType, NEIGHBOURtileType, DIRECTION_RIGHT)){
                    dfsStack.push(new int[] {newRow, newColumn});
                }
            }

            //neighbour below blok
            newRow = currentRow + 1;
            newColumn = currentColumn;

            if(newRow < board.length){
                int NEIGHBOURtileType = board[newRow][newColumn];
                boolean bloked = false;

                if(NEIGHBOURtileType == TRAIN){
                    bloked = true;
                }

                if (NEIGHBOURtileType == DESTINATION && (newRow != destR || newColumn != destC)){
                    bloked = true;
                }

                if(!bloked && !visitedArr[newRow][newColumn] && isConnectionValid(CURRENTtileType, NEIGHBOURtileType, DIRECTION_DOWN)) {
                    dfsStack.push(new int[]{newRow, newColumn});
                }
            }

            //neighbour on the left blok
            newRow = currentRow;
            newColumn = currentColumn - 1;

            if(newColumn >= 0){

                int NEIGHBOURtileType = board[newRow][newColumn];
                boolean bloked = false;

                if(NEIGHBOURtileType == TRAIN){
                    bloked = true;
                }

                if(NEIGHBOURtileType == DESTINATION && (newRow != destR || newColumn != destC)){
                    bloked = true;
                }
                if(!bloked && !visitedArr[newRow][newColumn] && isConnectionValid(CURRENTtileType, NEIGHBOURtileType, DIRECTION_LEFT)){
                    dfsStack.push(new int[]{newRow, newColumn});
                }
            }



        }


        //the loop that check its neighbours
        //calls to isValidConnection
        return new PathResult(false, smallestDistance);
    }



    boolean existsPath(int[][] board, int startR, int startC, int destR, int destC){
        //System.out.println("existsPath called for: ("+startR+","+startC+") -> ("+destR+","+destC+")");
        return DFS(board, startR, startC, destR, destC).reached;
    }
}
