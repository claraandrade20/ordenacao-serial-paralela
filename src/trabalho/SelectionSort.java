package trabalho;

import java.util.concurrent.CyclicBarrier;

// Selection Sort. No paralelo, cada thread procura o menor valor do seu pedaco.
// Uma barreira espera todas, e so entao o menor encontrado troca de lugar.
public class SelectionSort {

    public static void serial(int[] v) {
        int n = v.length;
        for (int i = 0; i < n - 1; i++) {
            int menor = i;
            for (int j = i + 1; j < n; j++) {
                if (v[j] < v[menor]) {
                    menor = j;
                }
            }
            if (menor != i) {
                trocar(v, i, menor);
            }
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        int n = v.length;
        if (n < 2 || numThreads <= 1) {
            serial(v);
            return;
        }

        int t = Math.min(numThreads, n);
        if (t <= 1) {
            serial(v);
            return;
        }

        final CyclicBarrier barreira = new CyclicBarrier(t);
        final int[] locais = new int[t];
        Thread[] grupo = new Thread[t];

        for (int id = 0; id < t; id++) {
            final int meuId = id;
            final int totalThreads = t;
            grupo[id] = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int fase = 0; fase < n - 1; fase++) {
                            int total = n - fase;
                            int bloco = (total + totalThreads - 1) / totalThreads;
                            int de = fase + meuId * bloco;
                            int ate = Math.min(n, de + bloco);

                            int idx = -1;
                            for (int j = de; j < ate; j++) {
                                if (idx < 0 || v[j] < v[idx]) {
                                    idx = j;
                                }
                            }
                            locais[meuId] = idx;
                            barreira.await();

                            if (meuId == 0) {
                                int melhor = fase;
                                for (int k = 0; k < totalThreads; k++) {
                                    int cand = locais[k];
                                    if (cand >= 0 && v[cand] < v[melhor]) {
                                        melhor = cand;
                                    }
                                }
                                if (melhor != fase) {
                                    trocar(v, fase, melhor);
                                }
                            }
                            barreira.await();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            grupo[id].start();
        }

        for (int i = 0; i < t; i++) {
            try {
                grupo[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }

    private static void trocar(int[] v, int i, int j) {
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
    }
}
