package trabalho;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// Quick Sort com pivo no meio do trecho, para nao piorar em vetor ja ordenado.
// No paralelo, pedacos grandes sao divididos entre tarefas do ForkJoin.
public class QuickSort {

    private static final int LIMITE = 1000;

    public static void serial(int[] v) {
        quick(v, 0, v.length - 1);
    }

    public static void paralelo(int[] v, int numThreads) {
        if (v.length < 2 || numThreads <= 1) {
            serial(v);
            return;
        }

        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new Tarefa(v, 0, v.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static void quick(int[] v, int esq, int dir) {
        if (esq >= dir) {
            return;
        }
        int p = particiona(v, esq, dir);
        quick(v, esq, p - 1);
        quick(v, p + 1, dir);
    }

    private static int particiona(int[] v, int esq, int dir) {
        int meio = esq + (dir - esq) / 2;
        trocar(v, meio, dir);
        int pivo = v[dir];
        int i = esq;

        for (int j = esq; j < dir; j++) {
            if (v[j] <= pivo) {
                trocar(v, i, j);
                i++;
            }
        }
        trocar(v, i, dir);
        return i;
    }

    private static void trocar(int[] v, int i, int j) {
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] v;
        private final int esq;
        private final int dir;

        Tarefa(int[] v, int esq, int dir) {
            this.v = v;
            this.esq = esq;
            this.dir = dir;
        }

        protected void compute() {
            if (dir - esq < LIMITE) {
                quick(v, esq, dir);
                return;
            }
            int p = particiona(v, esq, dir);
            invokeAll(new Tarefa(v, esq, p - 1), new Tarefa(v, p + 1, dir));
        }
    }
}
