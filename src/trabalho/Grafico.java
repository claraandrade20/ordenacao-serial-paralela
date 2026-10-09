package trabalho;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// Grafico simples em SVG, so para colar no relatorio. Nao e interativo.
public class Grafico {

    static void gerar(File pasta, List<Main.Resumo> dados) throws Exception {
        pasta.mkdirs();
        String[] algos = {"Bubble Sort", "Quick Sort", "Merge Sort", "Insertion Sort"};
        String[] tipos = {"aleatorio", "ordenado", "decrescente"};

        List<String> arquivos = new ArrayList<String>();
        for (int i = 0; i < algos.length; i++) {
            for (int j = 0; j < tipos.length; j++) {
                List<Main.Resumo> parte = new ArrayList<Main.Resumo>();
                for (int k = 0; k < dados.size(); k++) {
                    Main.Resumo r = dados.get(k);
                    if (r.algoritmo.equals(algos[i]) && r.tipo.equals(tipos[j])) {
                        parte.add(r);
                    }
                }
                String nome = arquivo(algos[i]) + "_" + tipos[j] + ".svg";
                desenhar(new File(pasta, nome), algos[i], tipos[j], parte);
                arquivos.add(nome);
            }
        }
        indice(new File(pasta, "index.html"), arquivos);
    }

    private static void desenhar(File arquivo, String algo, String tipo, List<Main.Resumo> parte) throws Exception {
        List<Integer> tamanhos = new ArrayList<Integer>();
        for (int i = 0; i < parte.size(); i++) {
            Integer t = Integer.valueOf(parte.get(i).tamanho);
            if (!tamanhos.contains(t)) {
                tamanhos.add(t);
            }
        }
        Collections.sort(tamanhos);

        String[] nomes = {"Serial", "2 threads", "4 threads", "8 threads"};
        String[] versoes = {"serial", "paralelo", "paralelo", "paralelo"};
        int[] threads = {1, 2, 4, 8};
        String[] cores = {"#1f4e79", "#c45911", "#548235", "#7030a0"};

        double max = 0;
        for (int i = 0; i < parte.size(); i++) {
            if (parte.get(i).media > max) {
                max = parte.get(i).media;
            }
        }
        if (max <= 0) {
            max = 1;
        }
        max = max * 1.15;

        int esq = 70;
        int topo = 50;
        int largura = 430;
        int altura = 280;
        int base = topo + altura;

        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"760\" height=\"420\">\n");
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n");
        svg.append("<text x=\"380\" y=\"28\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"16\">");
        svg.append(esc(algo + " - " + rotuloTipo(tipo)));
        svg.append("</text>\n");
        svg.append("<line x1=\"" + esq + "\" y1=\"" + base + "\" x2=\"" + (esq + largura) + "\" y2=\"" + base + "\" stroke=\"black\"/>\n");
        svg.append("<line x1=\"" + esq + "\" y1=\"" + topo + "\" x2=\"" + esq + "\" y2=\"" + base + "\" stroke=\"black\"/>\n");
        svg.append("<text x=\"16\" y=\"190\" font-family=\"Arial\" font-size=\"12\" transform=\"rotate(-90 16 190)\">Tempo medio (ms)</text>\n");
        svg.append("<text x=\"285\" y=\"400\" font-family=\"Arial\" font-size=\"12\">Tamanho do vetor</text>\n");

        for (int g = 0; g <= 4; g++) {
            double valor = max * g / 4.0;
            int y = base - (int) Math.round(altura * (valor / max));
            svg.append("<line x1=\"" + esq + "\" y1=\"" + y + "\" x2=\"" + (esq + largura) + "\" y2=\"" + y + "\" stroke=\"#dddddd\"/>\n");
            svg.append("<text x=\"" + (esq - 8) + "\" y=\"" + (y + 4) + "\" text-anchor=\"end\" font-family=\"Arial\" font-size=\"11\">");
            svg.append(num(valor));
            svg.append("</text>\n");
        }

        int qtd = tamanhos.size();
        for (int i = 0; i < qtd; i++) {
            int x = esq;
            if (qtd > 1) {
                x = esq + (int) Math.round(largura * (i / (double) (qtd - 1)));
            }
            svg.append("<text x=\"" + x + "\" y=\"" + (base + 20) + "\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"11\">");
            svg.append(tamanhos.get(i));
            svg.append("</text>\n");
        }

        for (int s = 0; s < nomes.length; s++) {
            StringBuilder pontos = new StringBuilder();
            for (int i = 0; i < qtd; i++) {
                int tamanho = tamanhos.get(i).intValue();
                double media = buscar(parte, versoes[s], threads[s], tamanho);
                int x = esq;
                if (qtd > 1) {
                    x = esq + (int) Math.round(largura * (i / (double) (qtd - 1)));
                }
                int y = base - (int) Math.round(altura * (media / max));
                if (pontos.length() > 0) {
                    pontos.append(' ');
                }
                pontos.append(x).append(',').append(y);
                svg.append("<circle cx=\"" + x + "\" cy=\"" + y + "\" r=\"3.5\" fill=\"" + cores[s] + "\"/>\n");
            }
            svg.append("<polyline fill=\"none\" stroke=\"" + cores[s] + "\" stroke-width=\"2\" points=\"" + pontos + "\"/>\n");

            int ly = 70 + s * 22;
            svg.append("<line x1=\"560\" y1=\"" + ly + "\" x2=\"590\" y2=\"" + ly + "\" stroke=\"" + cores[s] + "\" stroke-width=\"3\"/>\n");
            svg.append("<text x=\"598\" y=\"" + (ly + 4) + "\" font-family=\"Arial\" font-size=\"12\">" + nomes[s] + "</text>\n");
        }

        svg.append("</svg>\n");

        Writer w = new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8);
        w.write(svg.toString());
        w.close();
    }

    private static void indice(File arquivo, List<String> nomes) throws Exception {
        Writer w = new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8);
        w.write("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Graficos</title></head><body>\n");
        w.write("<h1>Graficos das medias</h1>\n");
        for (int i = 0; i < nomes.size(); i++) {
            w.write("<h2>" + nomes.get(i) + "</h2>\n");
            w.write("<img src=\"" + nomes.get(i) + "\" alt=\"" + nomes.get(i) + "\"/>\n");
        }
        w.write("</body></html>\n");
        w.close();
    }

    private static double buscar(List<Main.Resumo> parte, String versao, int threads, int tamanho) {
        for (int i = 0; i < parte.size(); i++) {
            Main.Resumo r = parte.get(i);
            if (r.versao.equals(versao) && r.threads == threads && r.tamanho == tamanho) {
                return r.media;
            }
        }
        return 0;
    }

    private static String arquivo(String algo) {
        if ("Bubble Sort".equals(algo)) {
            return "bubble";
        }
        if ("Quick Sort".equals(algo)) {
            return "quick";
        }
        if ("Merge Sort".equals(algo)) {
            return "merge";
        }
        return "insertion";
    }

    private static String rotuloTipo(String tipo) {
        if ("ordenado".equals(tipo)) {
            return "vetor ordenado";
        }
        if ("decrescente".equals(tipo)) {
            return "vetor decrescente";
        }
        return "vetor aleatorio";
    }

    private static String num(double v) {
        if (v >= 100) {
            return String.format(Locale.US, "%.0f", v);
        }
        if (v >= 10) {
            return String.format(Locale.US, "%.1f", v);
        }
        return String.format(Locale.US, "%.2f", v);
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }
}
