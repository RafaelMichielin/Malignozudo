import java.util.Random;
import java.util.Arrays;

public class OrdenacaoSequencial {

  public static void main(String[] args) {
    byte[] vector = new byte[1_400_000];
    Random random = new Random();

    // Preenchendo o vetor
    for (int i = 0; i < vector.length; i++) {
      vector[i] = (byte) random.nextInt(256);
    }

    long startTime = System.nanoTime();

    // Ordenação com MergeSort sequencial
    mergeSort(vector, 0, vector.length - 1);

    long endTime = System.nanoTime();

    System.out.println("MergeSort sequencial concluído!");
    System.out.printf("Tempo: %.3f ms%n", (endTime - startTime) / 1_000_000.0);

    // Pergunta para imprimir vetor
    System.out.print("Deseja imprimir o vetor ordenado? (S/N): ");
    try {
      char c = (char) System.in.read();
      if (c == 'S' || c == 's') {
        System.out.println(Arrays.toString(vector));
      }
    } catch (Exception e) {
      System.out.println("[ERRO] na leitura do teclado.");
    }
  }

  // -------------------------------------------------------------
  // Merge Sort Sequencial
  // -------------------------------------------------------------
  private static void mergeSort(byte[] array, int left, int right) {
    if (left >= right) return;

    int mid = (left + right) / 2;

    mergeSort(array, left, mid);
    mergeSort(array, mid + 1, right);
    merge(array, left, mid, right);
  }

  private static void merge(byte[] array, int left, int mid, int right) {
    int size1 = mid - left + 1;
    int size2 = right - mid;

    byte[] leftArr = new byte[size1];
    byte[] rightArr = new byte[size2];

    System.arraycopy(array, left, leftArr, 0, size1);
    System.arraycopy(array, mid + 1, rightArr, 0, size2);

    int i = 0, j = 0, k = left;

    while (i < size1 && j < size2) {
      if (leftArr[i] <= rightArr[j]) {
        array[k++] = leftArr[i++];
      } else {
        array[k++] = rightArr[j++];
      }
    }

    while (i < size1) array[k++] = leftArr[i++];
    while (j < size2) array[k++] = rightArr[j++];
  }
}
