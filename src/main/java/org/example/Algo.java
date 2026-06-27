package org.example;

import java.util.Random;
import mpi.MPI;

public class Algo {
    private final GameEvaluator evaluator;
    private final Train[] trains;
    private final int trainPairCount;
    private final Random rnd;
    private int populationSize=20;
    private int generations=200;

    public Algo(GameEvaluator evaluator, Train[] trains, int trainPairCount){
        this.evaluator=evaluator;
        this.trains=trains;
        this.trainPairCount=trainPairCount;
        this.rnd = new Random(Configurations.RANDOM_SEED);
    }

    public void run(int[][] startBoard, int[][] initialBoard, BoardPanel boardPanel){
        long startTime=System.nanoTime();

        int rows = startBoard.length;
        int cols=startBoard[0].length;

        int[][][] population = new int[populationSize][rows][cols];
        int[] fitness =new int[populationSize];

        population[0] = copyBoard(startBoard);

        for(int i=1; i<populationSize;i++){
            population[i] = copyBoard(startBoard);
            randomMutation(population[i]);

        }

        int[][] bestBoard = copyBoard(startBoard);
        int bestScore =Integer.MIN_VALUE;
        int stagniraniGenerations = 0;

        for (int g=0;g<generations;g++){
            int oldBestScore = bestScore;

            evaluatePopulationDistributed(population, fitness);
            for(int i=0;i<populationSize;i++){
                if(fitness[i]>bestScore){
                    bestScore=fitness[i];
                    bestBoard=copyBoard(population[i]);
                }
            }

            if(bestScore==oldBestScore){
                stagniraniGenerations++;
            } else {
                stagniraniGenerations=0;
            }

            int best1=0;
            int best2=0;

            for(int i=0;i<populationSize;i++){
                if(fitness[i]>fitness[best1]){
                    best2=best1;
                    best1=i;
                }
                else if (i!=best1 && fitness[i]>fitness[best2]){
                    best2=i;
                }
            }


            if(g%10==0){
                int[][] currentSnap =copyBoard(bestBoard);
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.applyBoard(currentSnap));
                System.out.println("generation: "+g+" best= "+bestScore);
            }

            int[][][] newPopulation = new int[populationSize][rows][cols];
            newPopulation[0]=copyBoard(bestBoard);
            if(stagniraniGenerations>30){
                for(int i=1;i<populationSize/2;i++){
                    int parent1=turnir(fitness);
                    int parent2=turnir(fitness);
                    int[][] child=crossingRows(population[parent1], population[parent2]);
                    randomMutation(child);
                    newPopulation[i] = child;
                }

                for(int i=populationSize/2; i<populationSize; i++){
                    newPopulation[i] =copyBoard(startBoard);
                    randomMutation(newPopulation[i]);
                }

                stagniraniGenerations=0;
            } else {
                for(int i=1;i<populationSize;i++){
                    int parent1=turnir(fitness);
                    int parent2= turnir(fitness);
                    int[][] child = crossingRows(population[parent1], population[parent2]);
                randomMutation(child);
                newPopulation[i]=child;
                }
            }
            population = newPopulation;




        }
        int[][] finalSnap=copyBoard(bestBoard);
        javax.swing.SwingUtilities.invokeLater(() -> boardPanel.applyBoard(finalSnap));
        System.out.println("final best score= "+bestScore);

        long endTime=System.nanoTime();
        double seconds =(endTime - startTime) / 1_000_000_000.0;
        System.out.println("runtime: " + seconds);

        /*
        int[] stop = {0};

        for(int worker=1;worker<MPI.COMM_WORLD.Size(); worker++){
            MPI.COMM_WORLD.Send(stop, 0, 1, MPI.INT, worker, 5);

        }

         */
    }

    private int[][] copyBoard(int[][] board){
        int[][] copy=new int[board.length][board[0].length];
        for(int r=0;r< board.length;r++){
            System.arraycopy(board[r], 0, copy[r], 0, board[0].length);
        }
        return copy;
    }

    private void randomMutation(int[][] board){
        int rows=board.length;
        int cols=board[0].length;


        //double rate = 0.05;
        int totalCells=rows*cols;
        int numOfMutations =Math.max(1, totalCells/20);
        for (int i=0;i<numOfMutations;i++){
            int r=rnd.nextInt(rows);
            int c= rnd.nextInt(cols);
            if (board[r][c] >= 0 ){
                int oldType = board[r][c];
                int newType;
                do{
                    newType= rnd.nextInt(11);
                }while(newType==oldType);
                board[r][c]=newType;
            }
        }



    }

    /* private void heavierMutation(int[][] board){
        int rows=board.length;
        int cols = board[0].length;
        int totalCells=rows*cols;
        int numOfMutations=totalCells/5;
        for(int i=0;i<numOfMutations;i++){
            int r =rnd.nextInt(rows);
            int c=rnd.nextInt(cols);

            if(board[r][ c] >=0){
                board[r][c]=rnd.nextInt(11);
            }
        }
    }

     */

    private int[][] crossingRows(int[][] A, int[][] B){
        int rows = A.length;
        int cols = A[0].length;
        int[][] child=new int[rows][cols];
        int split= rnd.nextInt(rows);

        for(int r=0;r<rows;r++){
            for(int c=0;c<cols;c++){
                if (r<=split){
                    child[r][c] = A[r][c];
                } else{
                    child[r][c]=B[r][c];
                }
            }
        }
        return child;
    }

    private int turnir(int[] fitness){
        int bestIndex = rnd.nextInt(populationSize);
        int bestFitness = fitness[bestIndex];

        for(int i=1;i<3;i++){
            int candidate=rnd.nextInt(populationSize);
            if(fitness[candidate]>bestFitness){
                bestFitness=fitness[candidate];
                bestIndex=candidate;
            }
        }
        return bestIndex;
    }

    private void evaluatePopulationDistributed(int[][][] population, int[] fitness){
        int workerCount = MPI.COMM_WORLD.Size()-1;

        if(workerCount<=0){
            for(int i=0;i<population.length;i++){
                fitness[i] = evaluator.evaluateTiles(population[0], population[i], trains, trainPairCount);
            }
            return;
        }

        int rows=population[0].length;
        int cols=population[0][0].length;

        int nextCandidate=0;
        while(nextCandidate<population.length){
            int jobsHere=Math.min(workerCount, population.length - nextCandidate);

            for(int i=0;i<jobsHere;i++){
                int worker=i+1;
                int candidateIndex=nextCandidate+i;

                int[] indexData={candidateIndex};
                int[] flatBoard=flattenBoard(population[candidateIndex], rows, cols);

                MPI.COMM_WORLD.Send(indexData, 0, 1, MPI.INT, worker, 6);
                MPI.COMM_WORLD.Send(flatBoard, 0, flatBoard.length, MPI.INT, worker, 3);
            }

            for(int i=0; i<jobsHere; i++){
                // int worker=i+1; OVA NE TI TRB ZS CEKAS OD KOJ BILO SRC NE RED PO RED
                int[] result = new int[2];

                MPI.COMM_WORLD.Recv(result, 0, 2, MPI.INT, MPI.ANY_SOURCE, 4);
                int candidateIndex=result[0];
                int candidateFitness=result[1];
                fitness[candidateIndex] = candidateFitness;
            }
            nextCandidate+=jobsHere;
        }
    }

    private int[] flattenBoard(int[][] board, int rows, int cols){
        int[] flatBoard=new int[rows*cols];
        for(int r=0;r<rows;r++){
            for(int c=0;c<cols;c++){
                flatBoard[r*cols+c]=board[r][c];
            }
        }

        return flatBoard;
    }
}
