# Análise de Desempenho de Algoritmos de Busca em Ambientes Concorrentes e Paralelos: Um Estudo Comparativo em Java

Trabalho em Java com Bubble Sort, Quick Sort, Merge Sort e Insertion Sort.
Cada um tem versão serial e versão paralela. O programa mede o tempo, grava CSV e gera gráficos simples.

O relatório está em [RELATORIO.md](RELATORIO.md).

## Como rodar

Na pasta do projeto:

```bat
executar.bat
```

Ou na mão:

```bat
javac -encoding UTF-8 --release 8 -d bin src\trabalho\*.java
java -cp bin trabalho.Main
```

Só para checar se os algoritmos ordenam certo, sem medir desempenho:

```bat
java -cp bin trabalho.Main teste
```

O teste completo demora alguns minutos por causa do Bubble Sort e do Insertion Sort.

## Saída

- `resultados/tempos.csv` — cada amostra
- `resultados/resumo.csv` — média, desvio padrão, mínimo e máximo
- `resultados/graficos/` — SVG de cada algoritmo e tipo de vetor
- `resultados/graficos/index.html` — abre os gráficos no navegador

O CSV usa `;` para abrir no Excel
