import java.util.Random;

public class ContagemSequencial {
  public static void main(String[] args) {
    byte[] vetor = new byte[1_400_000];
    Random random = new Random();

    for (int i = 0; i < vetor.length; i++)
      vetor[i] = (byte)(random.nextInt(201) - 100);

    byte procurado = vetor[random.nextInt(vetor.length)];

    long inicio = System.nanoTime();

    int contagem = 0;
    for (byte n : vetor)
      if (n == procurado)
        contagem++;

    long fim = System.nanoTime();

    System.out.println("Contagem sequencial: " + contagem);
    System.out.printf("Tempo: %.3f ms%n", (fim - inicio) / 1_000_000.0);
  }
}