# Análise de Desempenho de Algoritmos de Busca em Ambientes Concorrentes e Paralelos: Um Estudo Comparativo em Java

## Resumo

Este trabalho compara o tempo de execução de quatro algoritmos de ordenação em Java: Selection Sort, Quick Sort, Merge Sort e Insertion Sort. Cada um foi implementado em versão serial e em versão paralela. Os testes usaram vetores de 2000, 5000 e 10000 posições, em três situações: aleatório, já ordenado e decrescente. Cada configuração foi repetida 5 vezes, com 2, 4 e 8 threads na versão paralela. Os tempos foram gravados em CSV e resumidos pela média e pelo desvio padrão. Nos tamanhos testados, o Selection Sort paralelo ficou mais lento que o serial. Quick Sort e Merge Sort ficaram próximos do serial, em geral um pouco mais lentos. O Insertion Sort paralelo reduziu o tempo em vetores aleatórios e decrescentes a partir de 5000 elementos.

## Introdução

O objetivo do trabalho é observar como o tempo desses algoritmos muda quando o tamanho do vetor, o tipo dos dados e a quantidade de threads mudam. Os quatro métodos escolhidos ordenam um vetor de inteiros. Eles foram selecionados entre Bubble Sort, Quick Sort, Merge Sort, Selection Sort, Counting Sort e Insertion Sort.

O Selection Sort percorre o restante do vetor, acha o menor valor e coloca esse valor na posição atual. A versão paralela divide esse restante entre as threads. Cada uma procura o menor do seu pedaço. Uma barreira espera todas terminarem, a thread principal faz a troca, e só então começa a posição seguinte.

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

### Selection Sort

No vetor aleatório, o serial cresce com o tamanho. A versão paralela ficou mais lenta, e o tempo piorou quando o número de threads aumentou.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 1,98 | 60,34 | 147,07 | 261,32 |
| 5000 | 8,24 | 99,61 | 276,00 | 617,47 |
| 10000 | 40,42 | 278,37 | 573,46 | 2468,20 |

Tempos médios em milissegundos. Gráfico: `resultados/graficos/selection_aleatorio.svg`.

O motivo é a sincronização. Cada posição espera todas as threads acharem o menor do seu pedaço. São quase tantas esperas quanto o tamanho do vetor. O serial só percorre o vetor e troca, sem barreira.

O tipo do vetor muda pouco o Selection Sort serial, porque ele sempre olha o restante do vetor. Com 10000 elementos, o serial levou 40,42 ms no aleatório, 62,57 ms no ordenado e 38,07 ms no decrescente. Com 8 threads esses tempos foram para cerca de 2468 ms, 1427 ms e 1536 ms. A versão paralela não compensou em nenhum dos três tipos.

No caso de 10000 elementos aleatórios com 8 threads, o desvio padrão ficou alto (1425 ms). Uma amostra chegou a 4406 ms e puxou a média. As outras continuaram acima de 1 segundo, então a conclusão permanece: mais threads deixaram essa versão mais lenta.

### Quick Sort

O Quick Sort serial ficou em torno de 1 ms com 10000 elementos aleatórios. A versão paralela não melhorou esse tempo.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 0,23 | 1,24 | 0,92 | 0,90 |
| 5000 | 0,60 | 1,10 | 1,82 | 2,18 |
| 10000 | 1,18 | 1,60 | 1,66 | 2,11 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/quick_aleatorio.svg`.

Com 10000 elementos o serial continua na frente. Parte do vetor só é dividida entre tarefas quando o trecho passa de 1000 elementos, então sobra pouco trabalho paralelo. Além disso, cada medição cria um `ForkJoinPool` novo. Em vetor ordenado e em vetor decrescente o quadro é parecido: o serial fica em torno de 0,15 ms e 0,30 ms com 10000 elementos, e as versões com threads ficam acima disso. O pivô do meio evita o pior caso clássico do Quick Sort nesses dois vetores, então os dois lados ficam equilibrados e o algoritmo já é rápido no serial.

### Merge Sort

O Merge Sort também é rápido nesses tamanhos. No vetor aleatório, a versão serial ficou à frente nas três quantidades de threads.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 0,26 | 0,60 | 1,28 | 0,73 |
| 5000 | 0,60 | 2,34 | 1,86 | 1,41 |
| 10000 | 1,43 | 2,01 | 1,99 | 2,48 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/merge_aleatorio.svg`.

Em vetor ordenado e decrescente o serial também ficou à frente. Com 10000 elementos ordenados, o serial levou 0,70 ms e 8 threads levaram 2,08 ms. A diferença entre os tipos de vetor é pequena, o que combina com o Merge Sort, que divide o vetor do mesmo jeito independente da ordem inicial.

### Insertion Sort

Este foi o caso em que a versão paralela reduziu o tempo, desde que o vetor não estivesse ordenado e tivesse tamanho suficiente.

| Tamanho | Serial | 2 threads | 4 threads | 8 threads |
| --- | ---: | ---: | ---: | ---: |
| 2000 | 0,37 | 1,49 | 1,08 | 1,60 |
| 5000 | 2,73 | 1,12 | 0,93 | 1,76 |
| 10000 | 7,41 | 3,64 | 2,05 | 2,15 |

Tempos médios em milissegundos, vetor aleatório. Gráfico: `resultados/graficos/insertion_aleatorio.svg`.

Com 2000 elementos aleatórios, criar as threads custou mais do que ordenar o vetor inteiro no serial. Com 10000 elementos, o tempo cai de 7,41 ms no serial para cerca de 2 ms com 4 ou 8 threads. No vetor decrescente a queda é parecida: 15,54 ms no serial e 2,55 ms com 8 threads. Cada thread ordena um bloco menor. Como o insertion é quadrático, um bloco pequeno termina bem antes do vetor inteiro.

No vetor já ordenado acontece o contrário. O serial com 10000 elementos levou 0,013 ms, porque quase não há deslocamento de elementos. Com 8 threads o tempo foi cerca de 1,45 ms. Criar as threads e juntar os blocos custa mais do que a ordenação em si.

### Leitura geral

O tamanho do vetor aumenta o tempo em todos os algoritmos, de forma mais visível no Selection Sort e no Insertion Sort seriais. A natureza do vetor muda bastante o Insertion Sort: ordenado é muito rápido, decrescente é o mais lento. No Selection Sort os três tipos ficam próximos, porque a busca pelo menor sempre percorre o resto do vetor. Quick Sort e Merge Sort quase não mudam entre os três tipos, dentro desses tamanhos.

Mais threads não significou menos tempo de forma geral. No Selection Sort, mais threads aumentaram o tempo. No Quick Sort e no Merge Sort, o serial foi suficiente para vetores de até 10000 elementos. No Insertion Sort, mais threads ajudaram quando havia trabalho quadrático de verdade (aleatório e decrescente, a partir de 5000 elementos) e atrapalharam no vetor já ordenado.

Os gráficos estáticos estão na pasta `resultados/graficos`. Dá para abrir o `index.html` no navegador ou inserir os arquivos SVG no relatório do template. Os números completos, com desvio, mínimo e máximo, estão em `resultados/resumo.csv`.

## Conclusão

A comparação mostrou que paralelizar a ordenação nem sempre reduz o tempo. Com vetores de até 10000 inteiros, Selection Sort, Quick Sort e Merge Sort ficaram melhores na versão serial. O Selection Sort paralelo sofreu com a barreira em toda posição. Quick Sort e Merge Sort já são rápidos nesse tamanho, e o custo de criar o pool apareceu na medição. O Insertion Sort paralelo valeu a pena em vetores aleatórios e decrescentes maiores, porque cada thread trabalha em um pedaço bem menor.

O CSV gerado permite refazer os gráficos ou repetir o teste com outros tamanhos, mudando os valores no início da classe `Main`.

## Referências

CORMEN, Thomas H. et al. **Algoritmos: teoria e prática**. 3. ed. Rio de Janeiro: Elsevier, 2012.

ORACLE. **Fork/Join**. The Java Tutorials. Disponível em: https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html. Acesso em: 9 out. 2026.

ORACLE. **Class ForkJoinPool**. Java Platform SE 8. Disponível em: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html. Acesso em: 9 out. 2026.

SEDGEWICK, Robert; WAYNE, Kevin. **Algorithms**. 4. ed. Boston: Addison-Wesley, 2011.

## Anexos

Os códigos estão na pasta `src/trabalho`:

- `SelectionSort.java` — serial e busca paralela do menor valor
- `QuickSort.java` — serial e ForkJoin
- `MergeSort.java` — serial e ForkJoin
- `InsertionSort.java` — serial e blocos em threads
- `GeradorDados.java` — monta os três tipos de vetor
- `Main.java` — executa os testes, calcula média e desvio e grava os CSV
- `Grafico.java` — gera os SVG

Link do GitHub: https://github.com/claraandrade20/ordenacao-serial-paralela
