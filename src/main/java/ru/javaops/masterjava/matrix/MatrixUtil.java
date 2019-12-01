package ru.javaops.masterjava.matrix;

import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        class VectorMultiplier2 {
            final int j;
            final int[][] matrixA;
            final int[] thatColumn;

            public VectorMultiplier2(int j, int[][] matrixA, int[] thatColumn) {
                this.j = j;
                this.matrixA = matrixA;
                this.thatColumn = thatColumn;
            }

            void calculate() {
                for (int i = 0; i < matrixSize; i++) {
                    int[] thisRow = matrixA[i];
                    int sum = 0;
                    for (int k = 0; k < matrixSize; k++) {
                        sum += thisRow[k] * thatColumn[k];
                    }
                    matrixC[i][j] = sum;
                }
            }
        }

        CompletionService<VectorMultiplier2> completionService = new ExecutorCompletionService<>(executor);

        for (int j = 0; j < matrixSize; j++) {
            int[] thatColumn = new int[matrixSize];
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            VectorMultiplier2 multiplier = new VectorMultiplier2(j, matrixA, thatColumn);

            completionService.submit(() -> {
                multiplier.calculate();
                return multiplier;
            });
        }

        for (int i = 0; i < matrixSize; i++) {
            completionService.take();
        }

        return matrixC;
    }


    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int[] thatColumn = new int[matrixSize];

        for (int j = 0; j < matrixSize; j++) {
            for (int k = 0; k < matrixSize; k++) {
                thatColumn[k] = matrixB[k][j];
            }

            for (int i = 0; i < matrixSize; i++) {
                int[] thisRow = matrixA[i];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thisRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }

        return matrixC;
    }

    private static int[][] transpose(int[][] matrixB) {
        int bRows = matrixB.length;
        int bColumns = matrixB[0].length;

        int[][] matrixBT = new int[bColumns][bRows];

        for (int i = 0; i < bRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                matrixBT[j][i] = matrixB[i][j];
            }
        }
        return matrixBT;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
