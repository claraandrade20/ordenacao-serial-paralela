package trabalho;

import java.util.Arrays;

// Insertion Sort. No paralelo o vetor e dividido em blocos, cada thread
// ordena um bloco e depois os blocos sao juntados.
public class InsertionSort {

    public static void serial(int[] v) {
        for (int i = 1; i < v.length; i++) {
            int chave = v[i];
            int j = i - 1;
            while (j >= 0 && v[j] > chave) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = chave;
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        int n = v.length;
        if (n < 2 || numThreads <= 1) {
            serial(v);
            return;
        }

        int t = Math.min(numThreads, n);
        int base = n / t;
        int resto = n % t;

        final int[][] partes = new int[t][];
        Thread[] grupo = new Thread[t];
        int cursor = 0;

        for (int i = 0; i < t; i++) {
            int tam = base + (i < resto ? 1 : 0);
            partes[i] = Arrays.copyOfRange(v, cursor, cursor + tam);
            cursor += tam;

            final int id = i;
            grupo[i] = new Thread(new Runnable() {
                public void run() {
                    serial(partes[id]);
                }
            });
            grupo[i].start();
        }

        for (int i = 0; i < t; i++) {
            try {
                grupo[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        juntar(partes, v);
    }

    private static void juntar(int[][] partes, int[] destino) {
        int[] idx = new int[partes.length];
        for (int pos = 0; pos < destino.length; pos++) {
            int melhor = -1;
            for (int i = 0; i < partes.length; i++) {
                if (idx[i] >= partes[i].length) {
                    continue;
                }
                if (melhor == -1 || partes[i][idx[i]] < partes[melhor][idx[melhor]]) {
                    melhor = i;
                }
            }
            destino[pos] = partes[melhor][idx[melhor]];
            idx[melhor]++;
        }
    }
}
