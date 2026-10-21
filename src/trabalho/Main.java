package trabalho;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Main {

    static final int[] TAMANHOS = {2000, 5000, 10000};
    static final int AMOSTRAS = 5;
    static final int[] THREADS = {2, 4, 8};
    static final String[] TIPOS = {"aleatorio", "ordenado", "decrescente"};
    static final String[] ALGORITMOS = {
        "Selection Sort", "Quick Sort", "Merge Sort", "Insertion Sort"
    };

    static class Medida {
        String algoritmo;
        String versao;
        int threads;
        int tamanho;
        String tipo;
        int amostra;
        double tempoMs;
    }

    static class Resumo {
        String algoritmo;
        String versao;
        int threads;
        int tamanho;
        String tipo;
        double media;
        double desvio;
        double minimo;
        double maximo;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Processadores logicos: " + Runtime.getRuntime().availableProcessors());
        verificarCorrecao();

        if (args.length > 0 && "teste".equals(args[0])) {
            System.out.println("Somente o teste de correcao foi executado.");
            return;
        }

        aquecer();

        File pasta = new File("resultados");
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new RuntimeException("Nao foi possivel criar a pasta resultados");
        }

        List<Medida> medidas = new ArrayList<Medida>();
        File csvFile = new File(pasta, "tempos.csv");
        Writer csv = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile), StandardCharsets.UTF_8));
        csv.write('\uFEFF');
        csv.write("algoritmo;versao;threads;tamanho;tipo_dados;amostra;tempo_ms\n");

        int total = ALGORITMOS.length * TAMANHOS.length * TIPOS.length * AMOSTRAS * (1 + THREADS.length);
        int feito = 0;

        try {
            for (int a = 0; a < ALGORITMOS.length; a++) {
                String algo = ALGORITMOS[a];
                for (int t = 0; t < TAMANHOS.length; t++) {
                    int n = TAMANHOS[t];
                    for (int p = 0; p < TIPOS.length; p++) {
                        String tipo = TIPOS[p];
                        System.out.println("Rodando " + algo + " n=" + n + " tipo=" + tipo + "...");

                        Random rnd = new Random(42L + n + tipo.hashCode());
                        int[] base = GeradorDados.gerar(tipo, n, rnd);
                        int[] esperado = base.clone();
                        Arrays.sort(esperado);

                        for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
                            int[] copia = base.clone();
                            double ms = medirSerial(algo, copia);
                            if (!Arrays.equals(copia, esperado)) {
                                throw new RuntimeException("Resultado incorreto: " + algo + " serial");
                            }
                            Medida m = nova(algo, "serial", 1, n, tipo, amostra, ms);
                            medidas.add(m);
                            escrever(csv, m);
                            feito++;
                        }

                        for (int h = 0; h < THREADS.length; h++) {
                            int th = THREADS[h];
                            for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
                                int[] copia = base.clone();
                                double ms = medirParalelo(algo, copia, th);
                                if (!Arrays.equals(copia, esperado)) {
                                    throw new RuntimeException("Resultado incorreto: " + algo + " paralelo " + th);
                                }
                                Medida m = nova(algo, "paralelo", th, n, tipo, amostra, ms);
                                medidas.add(m);
                                escrever(csv, m);
                                feito++;
                            }
                        }

                        csv.flush();
                        System.out.println("  pronto (" + feito + "/" + total + ")");
                    }
                }
            }
        } finally {
            csv.close();
        }

        List<Resumo> resumos = agregar(medidas);
        salvarResumo(new File(pasta, "resumo.csv"), resumos);
        Grafico.gerar(new File(pasta, "graficos"), resumos);
        salvarAmbiente(new File(pasta, "ambiente.txt"));
        System.out.println("Arquivos gerados na pasta resultados.");
    }

    static void verificarCorrecao() {
        int[] manual = {9, 1, 9, -4, 3, 3, 0, 8, 2, -1, 5};
        int[] esperadoManual = manual.clone();
        Arrays.sort(esperadoManual);

        int[] threads = {2, 3, 8};
        for (int i = 0; i < ALGORITMOS.length; i++) {
            String algo = ALGORITMOS[i];
            int[] s = manual.clone();
            aplicarSerial(algo, s);
            if (!Arrays.equals(s, esperadoManual)) {
                throw new RuntimeException("Falhou no serial: " + algo);
            }
            for (int j = 0; j < threads.length; j++) {
                int[] p = manual.clone();
                aplicarParalelo(algo, p, threads[j]);
                if (!Arrays.equals(p, esperadoManual)) {
                    throw new RuntimeException("Falhou no paralelo: " + algo + " threads=" + threads[j]);
                }
            }
        }

        Random rnd = new Random(7);
        for (int i = 0; i < TIPOS.length; i++) {
            int[] original = GeradorDados.gerar(TIPOS[i], 300, rnd);
            int[] esperado = original.clone();
            Arrays.sort(esperado);
            for (int j = 0; j < ALGORITMOS.length; j++) {
                int[] s = original.clone();
                aplicarSerial(ALGORITMOS[j], s);
                if (!Arrays.equals(s, esperado)) {
                    throw new RuntimeException("Falhou: " + ALGORITMOS[j] + " " + TIPOS[i]);
                }
                int[] p = original.clone();
                aplicarParalelo(ALGORITMOS[j], p, 4);
                if (!Arrays.equals(p, esperado)) {
                    throw new RuntimeException("Falhou paralelo: " + ALGORITMOS[j] + " " + TIPOS[i]);
                }
            }
        }

        int[] um = {4};
        SelectionSort.paralelo(um, 4);
        System.out.println("Teste de correcao ok.");
    }

    static void aquecer() {
        Random rnd = new Random(1);
        int[] base = GeradorDados.gerar("aleatorio", 4000, rnd);
        for (int i = 0; i < ALGORITMOS.length; i++) {
            int[] s = base.clone();
            aplicarSerial(ALGORITMOS[i], s);
            int[] p = base.clone();
            aplicarParalelo(ALGORITMOS[i], p, 4);
        }
        System.out.println("Aquecimento concluido (essa execucao nao entra no CSV).");
    }

    static double medirSerial(String algo, int[] v) {
        long ini = System.nanoTime();
        aplicarSerial(algo, v);
        return (System.nanoTime() - ini) / 1000000.0;
    }

    static double medirParalelo(String algo, int[] v, int threads) {
        long ini = System.nanoTime();
        aplicarParalelo(algo, v, threads);
        return (System.nanoTime() - ini) / 1000000.0;
    }

    static void aplicarSerial(String algo, int[] v) {
        if ("Selection Sort".equals(algo)) {
            SelectionSort.serial(v);
        } else if ("Quick Sort".equals(algo)) {
            QuickSort.serial(v);
        } else if ("Merge Sort".equals(algo)) {
            MergeSort.serial(v);
        } else {
            InsertionSort.serial(v);
        }
    }

    static void aplicarParalelo(String algo, int[] v, int threads) {
        if ("Selection Sort".equals(algo)) {
            SelectionSort.paralelo(v, threads);
        } else if ("Quick Sort".equals(algo)) {
            QuickSort.paralelo(v, threads);
        } else if ("Merge Sort".equals(algo)) {
            MergeSort.paralelo(v, threads);
        } else {
            InsertionSort.paralelo(v, threads);
        }
    }

    static Medida nova(String algo, String versao, int threads, int n, String tipo, int amostra, double ms) {
        Medida m = new Medida();
        m.algoritmo = algo;
        m.versao = versao;
        m.threads = threads;
        m.tamanho = n;
        m.tipo = tipo;
        m.amostra = amostra;
        m.tempoMs = ms;
        return m;
    }

    static void escrever(Writer csv, Medida m) throws Exception {
        csv.write(m.algoritmo + ";" + m.versao + ";" + m.threads + ";" + m.tamanho + ";"
                + m.tipo + ";" + m.amostra + ";" + fmt(m.tempoMs) + "\n");
    }

    static List<Resumo> agregar(List<Medida> medidas) {
        Map<String, List<Medida>> grupos = new LinkedHashMap<String, List<Medida>>();
        for (int i = 0; i < medidas.size(); i++) {
            Medida m = medidas.get(i);
            String chave = m.algoritmo + "|" + m.versao + "|" + m.threads + "|" + m.tamanho + "|" + m.tipo;
            List<Medida> lista = grupos.get(chave);
            if (lista == null) {
                lista = new ArrayList<Medida>();
                grupos.put(chave, lista);
            }
            lista.add(m);
        }

        List<Resumo> saida = new ArrayList<Resumo>();
        for (List<Medida> g : grupos.values()) {
            Medida p = g.get(0);
            double soma = 0;
            double min = Double.POSITIVE_INFINITY;
            double max = 0;
            for (int i = 0; i < g.size(); i++) {
                double valor = g.get(i).tempoMs;
                soma += valor;
                if (valor < min) {
                    min = valor;
                }
                if (valor > max) {
                    max = valor;
                }
            }
            double media = soma / g.size();
            double acc = 0;
            for (int i = 0; i < g.size(); i++) {
                double d = g.get(i).tempoMs - media;
                acc += d * d;
            }
            double desvio = 0;
            if (g.size() > 1) {
                desvio = Math.sqrt(acc / (g.size() - 1));
            }

            Resumo r = new Resumo();
            r.algoritmo = p.algoritmo;
            r.versao = p.versao;
            r.threads = p.threads;
            r.tamanho = p.tamanho;
            r.tipo = p.tipo;
            r.media = media;
            r.desvio = desvio;
            r.minimo = min;
            r.maximo = max;
            saida.add(r);
        }
        return saida;
    }

    static void salvarResumo(File arquivo, List<Resumo> resumos) throws Exception {
        Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(arquivo), StandardCharsets.UTF_8));
        w.write('\uFEFF');
        w.write("algoritmo;versao;threads;tamanho;tipo_dados;media_ms;desvio_padrao_ms;minimo_ms;maximo_ms\n");
        for (int i = 0; i < resumos.size(); i++) {
            Resumo r = resumos.get(i);
            w.write(r.algoritmo + ";" + r.versao + ";" + r.threads + ";" + r.tamanho + ";" + r.tipo + ";"
                    + fmt(r.media) + ";" + fmt(r.desvio) + ";" + fmt(r.minimo) + ";" + fmt(r.maximo) + "\n");
        }
        w.close();
    }

    static void salvarAmbiente(File arquivo) throws Exception {
        Writer w = new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8);
        w.write("java.version=" + System.getProperty("java.version") + "\n");
        w.write("java.vendor=" + System.getProperty("java.vendor") + "\n");
        w.write("os.name=" + System.getProperty("os.name") + "\n");
        w.write("os.arch=" + System.getProperty("os.arch") + "\n");
        w.write("processadores_logicos=" + Runtime.getRuntime().availableProcessors() + "\n");
        w.write("tamanhos=");
        for (int i = 0; i < TAMANHOS.length; i++) {
            if (i > 0) {
                w.write(",");
            }
            w.write(String.valueOf(TAMANHOS[i]));
        }
        w.write("\namostras=" + AMOSTRAS + "\n");
        w.write("threads=2,4,8\n");
        w.close();
    }

    static String fmt(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.3f", valor);
    }
}
