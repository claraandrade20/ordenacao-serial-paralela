# Análise de Desempenho de Algoritmos de Busca em Ambientes Concorrentes e Paralelos: Um Estudo Comparativo em Java

## Resumo

Este trabalho compara o tempo de execução de quatro algoritmos de ordenação em Java: Bubble Sort, Quick Sort, Merge Sort e Insertion Sort. Cada um foi implementado em versão serial e em versão paralela. Os testes usaram vetores de 2000, 5000 e 10000 posições, em três situações: aleatório, já ordenado e decrescente. Cada configuração foi repetida 5 vezes, com 2, 4 e 8 threads na versão paralela. Os tempos foram gravados em CSV e resumidos pela média e pelo desvio padrão. Nos tamanhos testados, o Bubble Sort paralelo ficou mais lento que o serial. Quick Sort e Merge Sort ficaram próximos do serial, em geral um pouco mais lentos. O Insertion Sort paralelo reduziu o tempo em vetores aleatórios e decrescentes.

## Introdução

O objetivo do trabalho é observar como o tempo desses algoritmos muda quando o tamanho do vetor, o tipo dos dados e a quantidade de threads mudam. Os quatro métodos pedidos ordenam um vetor de inteiros.

O Bubble Sort percorre o vetor várias vezes e troca elementos vizinhos fora de ordem. Na versão serial, a execução para se uma passada inteira não faz nenhuma troca. A versão paralela segue a ideia do odd-even sort: em cada fase, threads diferentes comparam pares de posições que não se misturam, e uma barreira espera todas terminarem antes da fase seguinte. Essa versão sempre executa uma fase para cada elemento, mesmo quando o vetor já está ordenado.

O Quick Sort escolhe um pivô, separa o trecho em valores menores e maiores e repete o processo nos dois lados. O pivô usado aqui é o elemento do meio do trecho. Na versão paralela, um `ForkJoinPool` divide o trabalho. Trechos com menos de 1000 elementos são ordenados de forma serial, para não criar tarefa demais em pedaços pequenos.

O Merge Sort divide o vetor ao meio, ordena cada metade e junta as duas partes já ordenadas. A versão paralela também usa `ForkJoinPool`, com o mesmo limite de 1000 elementos para voltar ao serial. As duas metades são ordenadas ao mesmo tempo e a junção acontece depois que as duas terminam.

O Insertion Sort vai encaixando cada elemento na parte que já está ordenada. A versão paralela corta o vetor em blocos, uma thread ordena cada bloco com o insertion serial, e no final os blocos são juntados em um único vetor ordenado.

A comparação pedida é entre a execução serial e a paralela, variando o tamanho do vetor e o número de threads. A expectativa mais simples é que mais threads ajudem quando há bastante trabalho e atrapalhem quando o custo de criar e sincronizar threads fica maior que o ganho.

## Metodologia

Os testes foram executados em um notebook com processador Intel Core i5-1235U (10 núcleos, 12 processadores lógicos), Windows 11 e Java 1.8.0_441. O número de processadores lógicos lido pela JVM foi 12. Nenhuma configuração de núcleo foi desligada no sistema: a variação de paralelismo foi feita pela quantidade de threads pedida ao programa (2, 4 e 8).

O programa de teste está na classe `Main`. Para cada algoritmo, tamanho e tipo de dado, ele gera um vetor base e reaproveita uma cópia desse mesmo vetor em todas as amostras. Assim as versões serial e paralela recebem os mesmos valores. Os tipos de entrada são:

- aleatório, com valores inteiros sorteados;
- ordenado, com os valores de 0 até n-1;
- decrescente, com os valores de n até 1.

Os tamanhos foram 2000, 5000 e 10000. Cada combinação foi medida 5 vezes. Antes das medições houve uma execução de aquecimento, descartada, só para a JVM compilar os métodos. O tempo de cada amostra foi obtido com `System.nanoTime()`, contando apenas a ordenação. A conferência do resultado (comparação com `Arrays.sort`) fica fora desse tempo.

Foram 720 medições: 4 algoritmos, 3 tamanhos, 3 tipos de vetor, 5 amostras e 4 modos (serial e 3 quantidades de threads).

Para cada grupo de 5 amostras foram calculados a média, o desvio padrão amostral, o mínimo e o máximo. Tudo isso está em `resultados/resumo.csv`. O detalhe de cada amostra está em `resultados/tempos.csv`, com separador `;` para abrir no Excel. Os gráficos da média ficam em `resultados/graficos/`. O arquivo `index.html` dessa pasta mostra todos eles.

O tempo da versão paralela inclui a criação das threads ou do `ForkJoinPool`. Isso faz parte do custo de usar a versão paralela do jeito que ela foi escrita.

## Resultados e Discussão

Os gráficos usam a média das 5 amostras. A linha azul é a versão serial. As outras são 2, 4 e 8 threads.

### Bubble Sort

No vetor aleatório, o serial cresce com o tamanho, como se espera de um algoritmo quadrático. A versão paralela ficou mais lenta, e o tempo piorou quando o número de threads aumentou.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 4,58 | 48,38 | 62,71 | 172,49 |
| 5000 | 17,01 | 72,87 | 184,76 | 434,97 |
| 10000 | 69,77 | 183,56 | 353,09 | 922,80 |

Tempos médios em milissegundos. Gráfico: `resultados/graficos/bubble_aleatorio.svg`.

O motivo é a sincronização. Cada fase espera todas as threads na barreira, e são tantas fases quanto o tamanho do vetor. Com 8 threads há mais espera e mais custo para organizar o trabalho. O serial, nesse caso, só compara e troca, sem barreira.

No vetor já ordenado a diferença fica maior. Com 10000 elementos o serial levou cerca de 0,006 ms, porque para na primeira passada. O paralelo com 8 threads levou cerca de 751 ms, porque continua até o fim. No vetor decrescente de 10000 elementos o serial levou 79,93 ms e o paralelo com 8 threads levou 819,34 ms. Ou seja, nesses testes o Bubble Sort não se beneficiou das threads.

O desvio padrão do Bubble Sort serial em 2000 elementos aleatórios ficou alto (3,07 ms em torno de uma média de 4,58 ms). Nas outras linhas da tabela a média ainda mostra a mesma tendência: mais threads, mais tempo.

### Quick Sort

O Quick Sort serial ficou abaixo de 1 ms mesmo com 10000 elementos aleatórios. A versão paralela não melhorou esse tempo.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 0,15 | 0,63 | 1,44 | 1,20 |
| 5000 | 0,48 | 2,15 | 1,41 | 4,46 |
| 10000 | 0,88 | 1,32 | 0,97 | 2,83 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/quick_aleatorio.svg`.

Com 10000 elementos o serial continua na frente. Parte do vetor só é dividida entre tarefas quando o trecho passa de 1000 elementos, então sobra pouco trabalho paralelo. Além disso, cada medição cria um `ForkJoinPool` novo. Em vetor ordenado e em vetor decrescente o quadro é parecido: o serial fica em torno de 0,16 ms e 0,35 ms com 10000 elementos, e as versões com threads ficam acima disso. O pivô do meio evita o pior caso clássico do Quick Sort nesses dois vetores, então os dois lados ficam equilibrados e o algoritmo já é rápido no serial.

Alguns desvios ficaram grandes perto da média, por exemplo 8 threads e 5000 elementos aleatórios (média 4,46 ms, desvio 2,01 ms). A ordem geral não muda: o serial foi o mais rápido na maior parte dos casos.

### Merge Sort

O Merge Sort também é rápido nesses tamanhos. No vetor aleatório, 2 threads chegaram perto do serial só com 10000 elementos (1,23 ms contra 1,31 ms). Com 4 e 8 threads o tempo médio subiu.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 0,29 | 0,69 | 0,90 | 0,89 |
| 5000 | 0,67 | 0,85 | 0,88 | 1,71 |
| 10000 | 1,31 | 1,23 | 2,55 | 1,96 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/merge_aleatorio.svg`.

Em vetor ordenado e decrescente o serial também ficou à frente. Com 10000 elementos ordenados, o serial levou 0,31 ms e 8 threads levaram 1,37 ms. A diferença entre os tipos de vetor é pequena, o que combina com o Merge Sort, que divide o vetor do mesmo jeito independente da ordem inicial.

Um ponto do gráfico de vetor decrescente precisa de cuidado. Com 5000 elementos e 8 threads, a média foi 6,48 ms e o desvio padrão 11,85 ms. Das cinco amostras, quatro ficaram entre 0,77 ms e 1,91 ms. A quinta levou 27,66 ms e puxou a média. Por isso aquele ponto do gráfico `merge_decrescente.svg` fica alto demais em relação aos outros. O desvio padrão está no CSV justamente para mostrar esse tipo de amostra isolada.

### Insertion Sort

Este foi o caso em que a versão paralela reduziu o tempo, desde que o vetor não estivesse ordenado.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 2,15 | 1,20 | 1,09 | 1,69 |
| 5000 | 8,51 | 2,75 | 1,47 | 1,64 |
| 10000 | 35,31 | 11,61 | 5,11 | 3,78 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/insertion_aleatorio.svg`.

Com 10000 elementos aleatórios, o tempo cai de 35,31 ms no serial para 3,78 ms com 8 threads. No vetor decrescente a queda é parecida: 74,88 ms no serial e 5,76 ms com 8 threads. Cada thread ordena um bloco menor. Como o insertion é quadrático, um bloco pequeno termina bem antes do vetor inteiro.

No vetor já ordenado acontece o contrário. O serial com 10000 elementos levou 0,023 ms, porque quase não há deslocamento de elementos. Com 8 threads o tempo foi cerca de 0,96 ms. Criar as threads e juntar os blocos custa mais do que a ordenação em si.

Com 2000 elementos aleatórios, 8 threads (1,69 ms) ficaram piores que 4 threads (1,09 ms). O vetor já é pequeno e dividir em oito blocos não compensa.

### Leitura geral

O tamanho do vetor aumenta o tempo em todos os algoritmos, de forma mais visível no Bubble Sort e no Insertion Sort seriais. A natureza do vetor muda bastante o Bubble Sort e o Insertion Sort seriais: ordenado é muito rápido, decrescente é o mais lento. Quick Sort e Merge Sort quase não mudam entre os três tipos, dentro desses tamanhos.

Mais threads não significou menos tempo de forma geral. No Bubble Sort, mais threads aumentaram o tempo. No Quick Sort e no Merge Sort, o serial foi suficiente para vetores de até 10000 elementos. No Insertion Sort, mais threads ajudaram quando havia trabalho quadrático de verdade (aleatório e decrescente) e atrapalharam no vetor já ordenado.

Os gráficos estáticos estão na pasta `resultados/graficos`. Dá para abrir o `index.html` no navegador ou inserir os arquivos SVG no relatório do template. Os números completos, com desvio, mínimo e máximo, estão em `resultados/resumo.csv`.

## Conclusão

A comparação mostrou que paralelizar a ordenação nem sempre reduz o tempo. Com vetores de até 10000 inteiros, Bubble Sort, Quick Sort e Merge Sort ficaram melhores ou equivalentes na versão serial. O Bubble Sort paralelo sofreu com a barreira em toda fase. Quick Sort e Merge Sort já são rápidos nesse tamanho, e o custo de criar o pool apareceu na medição. O Insertion Sort paralelo valeu a pena em vetores aleatórios e decrescentes, porque cada thread trabalha em um pedaço bem menor.

O CSV gerado permite refazer os gráficos ou repetir o teste com outros tamanhos, mudando os valores no início da classe `Main`.

## Referências

CORMEN, Thomas H. et al. **Algoritmos: teoria e prática**. 3. ed. Rio de Janeiro: Elsevier, 2012.

ORACLE. **Fork/Join**. The Java Tutorials. Disponível em: https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html. Acesso em: 9 out. 2026.

ORACLE. **Class ForkJoinPool**. Java Platform SE 8. Disponível em: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html. Acesso em: 9 out. 2026.

SEDGEWICK, Robert; WAYNE, Kevin. **Algorithms**. 4. ed. Boston: Addison-Wesley, 2011.

## Anexos

Os códigos estão na pasta `src/trabalho`:

- `BubbleSort.java` — serial e odd-even paralelo
- `QuickSort.java` — serial e ForkJoin
- `MergeSort.java` — serial e ForkJoin
- `InsertionSort.java` — serial e blocos em threads
- `GeradorDados.java` — monta os três tipos de vetor
- `Main.java` — executa os testes, calcula média e desvio e grava os CSV
- `Grafico.java` — gera os SVG

Link do GitHub: https://github.com/claraandrade20/ordenacao-serial-paralela
