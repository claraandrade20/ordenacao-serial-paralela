package trabalho;

import java.util.Random;

public class GeradorDados {

    public static int[] gerar(String tipo, int n, Random rnd) {
        int[] v = new int[n];

        if ("ordenado".equals(tipo)) {
            for (int i = 0; i < n; i++) {
                v[i] = i;
            }
            return v;
        }

        if ("decrescente".equals(tipo)) {
            for (int i = 0; i < n; i++) {
                v[i] = n - i;
            }
            return v;
        }

        for (int i = 0; i < n; i++) {
            v[i] = rnd.nextInt(n * 10 + 1);
        }
        return v;
    }
}
