package trabalho;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

// Merge Sort classico. No paralelo as duas metades sao ordenadas ao mesmo tempo.
public class MergeSort {

    private static final int LIMITE = 1000;

    public static void serial(int[] v) {
        if (v.length < 2) {
            return;
        }
        int[] aux = new int[v.length];
        mergeSort(v, aux, 0, v.length - 1);
    }

    public static void paralelo(int[] v, int numThreads) {
        if (v.length < 2 || numThreads <= 1) {
            serial(v);
            return;
        }

        int[] aux = new int[v.length];
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        try {
            pool.invoke(new Tarefa(v, aux, 0, v.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static void mergeSort(int[] v, int[] aux, int esq, int dir) {
        if (esq >= dir) {
            return;
        }
        int meio = esq + (dir - esq) / 2;
        mergeSort(v, aux, esq, meio);
        mergeSort(v, aux, meio + 1, dir);
        merge(v, aux, esq, meio, dir);
    }

    private static void merge(int[] v, int[] aux, int esq, int meio, int dir) {
        for (int i = esq; i <= dir; i++) {
            aux[i] = v[i];
        }

        int i = esq;
        int j = meio + 1;
        for (int k = esq; k <= dir; k++) {
            if (i > meio) {
                v[k] = aux[j++];
            } else if (j > dir) {
                v[k] = aux[i++];
            } else if (aux[j] < aux[i]) {
                v[k] = aux[j++];
            } else {
                v[k] = aux[i++];
            }
        }
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] v;
        private final int[] aux;
        private final int esq;
        private final int dir;

        Tarefa(int[] v, int[] aux, int esq, int dir) {
            this.v = v;
            this.aux = aux;
            this.esq = esq;
            this.dir = dir;
        }

        protected void compute() {
            if (dir - esq < LIMITE) {
                mergeSort(v, aux, esq, dir);
                return;
            }
            int meio = esq + (dir - esq) / 2;
            invokeAll(new Tarefa(v, aux, esq, meio), new Tarefa(v, aux, meio + 1, dir));
            merge(v, aux, esq, meio, dir);
        }
    }
}
