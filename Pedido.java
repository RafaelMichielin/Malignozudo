public class Pedido extends Comunicado {
  private byte[] numeros;

  public Pedido(byte[] numeros) {
    this.numeros = numeros;
  }

  public byte[] getNumeros() {
    return this.numeros;
  }

  public byte[] ordenar() {
    int n = numeros.length;
    int disponiveis = Runtime.getRuntime().availableProcessors();

    // Se o vetor for muito pequeno ou tiver só 1 processador, ordena sequencial direto
    if (disponiveis <= 1 || n < 1000) {
      mergeSort(numeros, 0, n - 1);
      return numeros;
    }

    // Divide o trabalho
    int partes = disponiveis; // Usa todos os núcleos
    int tamanho = n / partes;
    byte[][] blocos = new byte[partes][];
    int inicio = 0;

    for (int i = 0; i < partes; i++) {
      int fim = (i == partes - 1 ? n : inicio + tamanho);
      blocos[i] = new byte[fim - inicio];
      System.arraycopy(numeros, inicio, blocos[i], 0, blocos[i].length);
      inicio = fim;
    }

    // Cria threads para ordenar cada pedaço
    Thread[] threads = new Thread[partes];
    for (int i = 0; i < partes; i++) {
      final int idx = i;
      threads[i] = new Thread(() -> mergeSort(blocos[idx], 0, blocos[idx].length - 1));
      threads[i].start();
    }

    // Espera as threads terminarem
    for (Thread t : threads) {
      try { t.join(); } catch (Exception e) {}
    }

    // Junta os pedaços ordenados (Merge Paralelo dos resultados)
    return mergeBlocos(blocos);
  }

  // --- Lógica Auxiliar de Merge Sort Sequencial (usada pelas threads) ---
  private void mergeSort(byte[] v, int ini, int fim) {
    if (ini >= fim) return;
    int meio = (ini + fim) / 2;
    mergeSort(v, ini, meio);
    mergeSort(v, meio + 1, fim);
    merge(v, ini, meio, fim);
  }

  private void merge(byte[] v, int ini, int meio, int fim) {
    byte[] aux = new byte[fim - ini + 1];
    int i = ini, j = meio + 1, k = 0;
    while (i <= meio && j <= fim) aux[k++] = (v[i] <= v[j]) ? v[i++] : v[j++];
    while (i <= meio) aux[k++] = v[i++];
    while (j <= fim) aux[k++] = v[j++];
    for (k = 0; k < aux.length; k++) v[ini + k] = aux[k];
  }

  // Junta os blocos ordenados das threads
  private byte[] mergeBlocos(byte[][] blocos) {
    while (blocos.length > 1) {
      int qtd = (blocos.length + 1) / 2;
      byte[][] novo = new byte[qtd][];
      for (int i = 0; i < blocos.length; i += 2) {
        if (i + 1 == blocos.length) {
          novo[i / 2] = blocos[i];
        } else {
          novo[i / 2] = mergeVetores(blocos[i], blocos[i+1]);
        }
      }
      blocos = novo;
    }
    return blocos[0];
  }

  private byte[] mergeVetores(byte[] a, byte[] b) {
    byte[] r = new byte[a.length + b.length];
    int i = 0, j = 0, k = 0;
    while (i < a.length && j < b.length) r[k++] = (a[i] <= b[j]) ? a[i++] : b[j++];
    while (i < a.length) r[k++] = a[i++];
    while (j < b.length) r[k++] = b[j++];
    return r;
  }
}