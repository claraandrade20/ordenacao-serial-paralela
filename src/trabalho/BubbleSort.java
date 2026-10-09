package trabalho;

import java.util.concurrent.CyclicBarrier;

// Bubble Sort serial com parada antecipada.
// A versao paralela e o odd-even sort: compara pares sem sobrepor posicoes.
public class BubbleSort {

    public static void serial(int[] v) {
        int n = v.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (v[j] > v[j + 1]) {
                    trocar(v, j, j + 1);
                    trocou = true;
                }
            }
            if (!trocou) {
                break;
            }
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        int n = v.length;
        if (n < 2 || numThreads <= 1) {
            serial(v);
            return;
        }

        int t = Math.min(numThreads, n / 2);
        if (t <= 1) {
            serial(v);
            return;
        }

        final CyclicBarrier barreira = new CyclicBarrier(t);
        Thread[] grupo = new Thread[t];

        for (int id = 0; id < t; id++) {
            final int meuId = id;
            final int totalThreads = t;
            grupo[id] = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int fase = 0; fase < n; fase++) {
                            int inicio = fase % 2;
                            int totalPares = (n - inicio) / 2;
                            int bloco = (totalPares + totalThreads - 1) / totalThreads;
                            int de = meuId * bloco;
                            int ate = Math.min(totalPares, de + bloco);

                            for (int k = de; k < ate; k++) {
                                int i = inicio + 2 * k;
                                int j = i + 1;
                                if (v[i] > v[j]) {
                                    trocar(v, i, j);
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
