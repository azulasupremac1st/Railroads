package org.example;

import java.util.Random;

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

        for (int g=0;g<generations;g++){
            for(int i=0;i<populationSize;i++){
                fitness[i]=evaluator.evaluateTiles(initialBoard, population[i], trains,trainPairCount);
                if(fitness[i]>bestScore){
                    bestScore=fitness[i];
                    bestBoard=copyBoard(population[i]);
                }
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
                int[][] snapshot=copyBoard(bestBoard);
                javax.swing.SwingUtilities.invokeLater(() -> boardPanel.applyBoard(snapshot));
                System.out.println("generation: "+g+" best= "+bestScore);
            }

            int[][][] newPopulation = new int[populationSize][rows][cols];
            for(int i=0;i<populationSize;i++){
                int parent1=turnir(fitness);
                int parent2=turnir(fitness);

                int[][] child=crossingRows(population[parent1], population[parent2]);

                randomMutation(child);
                newPopulation[i] = child;
            }
            population=newPopulation;
        }
        int[][] finalSnapshot=copyBoard(bestBoard);
        javax.swing.SwingUtilities.invokeLater(() -> boardPanel.applyBoard(finalSnapshot));
        System.out.println("final best score= "+bestScore);
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
}
