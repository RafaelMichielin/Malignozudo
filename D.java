import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class D {
  // Portas
  private static final int[] PORTAS = {12344, 12345, 12346};
  private static final String IP = "127.0.0.1";
  private static final int MAX_TAMANHO = 1_400_000;

  public static void main(String[] args) throws Exception {
    System.out.println("=== SISTEMA DISTRIBUÍDO DE ORDENAÇÃO (CLIENTE D) ===");

    int numServidores = PORTAS.length;
    Socket[] conexoes = new Socket[numServidores];
    ObjectOutputStream[] transmissores = new ObjectOutputStream[numServidores];
    ObjectInputStream[] receptores = new ObjectInputStream[numServidores];

    //  Conexão Inicial Persistente
    System.out.println("[D] Conectando aos servidores...");
    try {
      for (int i = 0; i < numServidores; i++) {
        conexoes[i] = new Socket(IP, PORTAS[i]);
        transmissores[i] = new ObjectOutputStream(conexoes[i].getOutputStream());
        receptores[i] = new ObjectInputStream(conexoes[i].getInputStream());
        System.out.println("[D] Conectado à porta " + PORTAS[i]);
      }
    } catch (IOException e) {
      System.err.println("[ERRO] Falha ao conectar. Verifique se os Receptores (R) estão rodando.");
      return;
    }

    boolean continuar = true;
    while (continuar) {
      //  Leitura do tamanho do vetor
      int tamanho = 0;
      try {
        System.out.print("\nDigite o tamanho do vetor (máx " + MAX_TAMANHO + "): ");
        tamanho = Teclado.getUmInt();
        if (tamanho <= 0 || tamanho > MAX_TAMANHO) {
          System.out.println("Tamanho inválido.");
          continue;
        }
      } catch (Exception e) { continue; }

      // 3. Geração do vetor aleatório (byte)
      byte[] vetor = new byte[tamanho];
      Random rand = new Random();
      rand.nextBytes(vetor);

      System.out.println("[D] Vetor gerado. Tamanho: " + tamanho);

      // 4. Divisão do vetor
      int parteTam = tamanho / numServidores;
      byte[][] partes = new byte[numServidores][];
      int inicio = 0;

      for (int i = 0; i < numServidores; i++) {
        int fim = (i == numServidores - 1) ? tamanho : inicio + parteTam;
        partes[i] = Arrays.copyOfRange(vetor, inicio, fim);
        inicio = fim;
      }

      // 5. Envio e Recebimento (Paralelo)
      byte[][] resultadosOrdenados = new byte[numServidores][];
      Thread[] threads = new Thread[numServidores];
      long inicioTempo = System.nanoTime();

      for (int i = 0; i < numServidores; i++) {
        final int idx = i;
        threads[i] = new Thread(() -> {
          try {
            transmissores[idx].writeObject(new Pedido(partes[idx]));
            transmissores[idx].flush();

            Resposta resp = (Resposta) receptores[idx].readObject();
            resultadosOrdenados[idx] = resp.getVetor();
          } catch (Exception e) {
            System.err.println("[ERRO] Servidor " + PORTAS[idx] + ": " + e.getMessage());
          }
        });
        threads[i].start();
      }

      // Aguarda threads
      for (Thread t : threads) {
        try { t.join(); } catch (InterruptedException e) {}
      }

      System.out.println("[D] Todos os servidores responderam. Fazendo merge final...");

      //  Merge Final Local
      byte[] vetorFinal = mergeMultiplo(resultadosOrdenados);

      long fimTempo = System.nanoTime();
      double ms = (fimTempo - inicioTempo) / 1_000_000.0;

      System.out.printf("[SUCESSO] Ordenação concluída em %.3f ms%n", ms);

      try {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = "resultado_" + dataHora + ".txt";

        FileWriter fw = new FileWriter(nomeArquivo);
        BufferedWriter bw = new BufferedWriter(fw);

        // Escreve o vetor como string
        bw.write(Arrays.toString(vetorFinal));

        bw.close();
        fw.close();

        System.out.println("[ARQUIVO] Resultado salvo em: " + nomeArquivo);

      } catch (IOException e) {
        System.err.println("[ERRO] Falha ao criar arquivo de texto: " + e.getMessage());
      }

      //  Loop de repetição
      System.out.print("Ordenar outro vetor? (s/n): ");
      try {
        String s = Teclado.getUmString();
        if (!s.equalsIgnoreCase("s")) continuar = false;
      } catch (Exception e) { continuar = false; }
    }

    System.out.println("[D] Enviando sinal de fim...");
    for (int i = 0; i < numServidores; i++) {
      try {
        transmissores[i].writeObject(new ComunicadoEncerramento());
        conexoes[i].close();
      } catch (Exception e) {}
    }
    System.out.println("Fim.");
  }

  //  LÓGICA DE MERGE
  private static byte[] mergeMultiplo(byte[][] arrays) {
    while (arrays.length > 1) {
      int novaQtd = (arrays.length + 1) / 2;
      byte[][] novo = new byte[novaQtd][];
      for (int i = 0; i < arrays.length; i += 2) {
        if (i + 1 == arrays.length) novo[i/2] = arrays[i];
        else novo[i/2] = mergeDois(arrays[i], arrays[i+1]);
      }
      arrays = novo;
    }
    return arrays[0];
  }

  private static byte[] mergeDois(byte[] a, byte[] b) {
    if (a == null) return b;
    if (b == null) return a;
    byte[] r = new byte[a.length + b.length];
    int i=0, j=0, k=0;
    while(i < a.length && j < b.length) r[k++] = (a[i] <= b[j]) ? a[i++] : b[j++];
    while(i < a.length) r[k++] = a[i++];
    while(j < b.length) r[k++] = b[j++];
    return r;
  }
}