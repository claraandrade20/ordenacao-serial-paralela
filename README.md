# Analise de desempenho - ordenacao serial e paralela

Trabalho simples em Java com Bubble Sort, Quick Sort, Merge Sort e Insertion Sort.
Cada um tem versao serial e versao paralela. O programa mede o tempo, grava CSV e gera graficos simples.

## Como rodar

Na pasta do projeto:

```bat
executar.bat
```

Ou na mao:

```bat
javac -encoding UTF-8 --release 8 -d bin src\trabalho\*.java
java -cp bin trabalho.Main
```

So para checar se os algoritmos ordenam certo, sem medir desempenho:

```bat
java -cp bin trabalho.Main teste
```

O teste completo demora alguns minutos por causa do Bubble Sort e do Insertion Sort.

## Saida

- `resultados/tempos.csv` — cada amostra
- `resultados/resumo.csv` — media, desvio padrao, minimo e maximo
- `resultados/graficos/` — SVG de cada algoritmo e tipo de vetor
- `resultados/graficos/index.html` — abre os graficos no navegador

O CSV usa `;` para abrir no Excel.
