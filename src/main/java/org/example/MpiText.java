package org.example;

import mpi.MPI;
import mpi.Status;
import javax.swing.SwingUtilities;

public class MpiText {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (rank == 0){
            SwingUtilities.invokeLater(Main::createAndShowGUI);
            return;

        } else {
            int[] thingsToRecieve = new int[3];

            MPI.COMM_WORLD.Recv(thingsToRecieve, 0, 3, MPI.INT, 0, 0);
            int rows = thingsToRecieve[0];
            int cols = thingsToRecieve[1];
            int trainPairCount = thingsToRecieve[2];
            int[] flatBoard = new int[rows*cols];
            MPI.COMM_WORLD.Recv(flatBoard, 0, rows*cols, MPI.INT, 0, 1);

            System.out.println("rank" + rank + "recv rows: " + rows + ", cols: "+cols+", trains: "+ trainPairCount);
            System.out.println(
                    "rank " + rank +
                            " received board: first=" + flatBoard[0] +
                            ", last=" + flatBoard[rows * cols - 1]
            );

            int[] flatTrains = new int[trainPairCount*4];
            MPI.COMM_WORLD.Recv(flatTrains, 0, flatTrains.length, MPI.INT, 0, 2);
            System.out.println("rank" + rank+"train1: "+flatTrains[0]+","+flatTrains[1]+"->"+flatTrains[2]+","+flatTrains[3]);


            Train[] trains = new Train[trainPairCount];
            for (int i=0; i<trainPairCount; i++){
                int startRow = flatTrains[i*4];
                int startCol = flatTrains[i*4 +1];
                int destRow = flatTrains[i*4+2];
                int destCol = flatTrains[i*4 +3];

                trains[i] = new Train(startRow, startCol, destRow, destCol);
            }

            int[][] initialBoard=new int[rows][cols];

            for(int r=0; r<rows; r++){
                for(int c=0; c<cols; c++){
                    initialBoard[r][c] = flatBoard[r*cols+c];
                }
            }

            GameEvaluator evaluator = new GameEvaluator();
            while(true){
                Status status = MPI.COMM_WORLD.Probe(0, MPI.ANY_TAG);

                if(status.tag == 5){
                    int[] stop = new int[1];
                    MPI.COMM_WORLD.Recv(stop, 0, 1, MPI.INT, 0, 5);
                    break;
                }

                int[] batchInfo = new int[2];
                MPI.COMM_WORLD.Recv(batchInfo, 0, 2, MPI.INT, 0, 6);
                int startIndex=batchInfo[0];
                int count = batchInfo[1];

                int cells=rows*cols;
                int[] flatBatch=new int[count*cells];

                MPI.COMM_WORLD.Recv(
                        flatBatch,
                        0,
                        flatBatch.length,
                        MPI.INT,
                        0,
                        3
                );


                int[] batchResults = new int[count+1];
                batchResults[0] = startIndex;

                for (int b=0;b<count;b++){
                    int[][] candidateBoard = new int[rows][cols];

                    for (int r=0;r<rows;r++){
                        for (int c=0;c<cols;c++){
                            int flatIndex=b*cells + r*cols+c;
                            candidateBoard[r][c] = flatBatch[flatIndex];
                        }
                    }

                    int candidateFitness = evaluator.evaluateTiles(initialBoard, candidateBoard, trains, trainPairCount);

                    batchResults[b+1] = candidateFitness;
                }

                MPI.COMM_WORLD.Send(
                        batchResults,
                        0,
                        batchResults.length,
                        MPI.INT,
                        0,
                        4
                );


            }
        }

        MPI.Finalize();
    }

}