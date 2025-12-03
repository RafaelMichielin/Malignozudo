import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class D{
  private static final int[] PORTAS = {12344, 12345, 12346};
  private static final String IP = "127.0.0.1";
  private static final int MAX_TAMANHO = 1_400_000;

  public static void main(String[] args) throws Exception {
    System.out.println("=== SISTEMA DISTRIBUÍDO (DISTRIBUIDOR) ===");

    int numServidores = PORTAS.length;
    Socket[] conexoes = new Socket[numServidores];
    ObjectOutputStream[] transmissores = new ObjectOutputStream[numServidores];
    ObjectInputStream[] receptores = new ObjectInputStream[numServidores];

    System.out.println("[D] Conectando aos servidores...");
    try {
      for (int i = 0; i < numServidores; i++) {
        conexoes[i] = new Socket(IP, PORTAS[i]);
        transmissores[i] = new ObjectOutputStream(conexoes[i].getOutputStream());
        receptores[i] = new ObjectInputStream(conexoes[i].getInputStream());
        System.out.println("[D] Conectado à porta " + PORTAS[i]);
      }
    } catch (IOException e) {
      System.err.println("[ERRO] Falha ao conectar: " + e.getMessage());
      return;
    }

    boolean continuar = true;
    while (continuar) {
      int tamanho = 0;
      try {
        System.out.print("\nDigite o tamanho do vetor (máx " + MAX_TAMANHO + "): ");
        tamanho = Teclado.getUmInt();
        if (tamanho <= 0 || tamanho > MAX_TAMANHO) continue;
      } catch (Exception e) { continue; }

      byte[] vetor = new byte[tamanho];
      new Random().nextBytes(vetor);
      System.out.println("[D] Vetor gerado.");

      // Divisão para os servidores
      int parteTam = tamanho / numServidores;
      byte[][] partes = new byte[numServidores][];
      int inicio = 0;
      for (int i = 0; i < numServidores; i++) {
        int fim = (i == numServidores - 1) ? tamanho : inicio + parteTam;
        partes[i] = Arrays.copyOfRange(vetor, inicio, fim);
        inicio = fim;
      }

      // Envio e Recebimento
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
            System.err.println("[ERRO] Servidor " + idx + ": " + e.getMessage());
          }
        });
        threads[i].start();
      }

      for (Thread t : threads) {
        try { t.join(); } catch (InterruptedException e) {}
      }

      System.out.println("[D] Fazendo merge final PARALELO...");

      // Usando o merge paralelo novo
      int coresDisponiveis = Runtime.getRuntime().availableProcessors();
      byte[] vetorFinal = ThreadSorteadora.intercalarMultiploComThreads(resultadosOrdenados, coresDisponiveis);

      long fimTempo = System.nanoTime();
      System.out.printf("[SUCESSO] Ordenação total em %.3f ms%n", (fimTempo - inicioTempo) / 1_000_000.0);

      // Arquivo TXT
      try {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nomeArquivo = "resultado_" + dataHora + ".txt";
        File arquivo = new File(nomeArquivo);
        FileWriter fw = new FileWriter(arquivo);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(Arrays.toString(vetorFinal));
        bw.close();
        fw.close();
        System.out.println("[ARQUIVO] Salvo em: " + arquivo.getAbsolutePath());
      } catch (Exception e) {
        System.out.println("Erro ao salvar arquivo.");
      }

      System.out.print("Ordenar outro vetor? (s/n): ");
      try {
        if (!Teclado.getUmString().equalsIgnoreCase("s")) continuar = false;
      } catch (Exception e) { continuar = false; }
    }

    // Encerramento
    for (int i = 0; i < numServidores; i++) {
      try {
        transmissores[i].writeObject(new ComunicadoEncerramento());
        conexoes[i].close();
      } catch (Exception e) {}
    }
  }
}