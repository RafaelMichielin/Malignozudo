import java.io.*;
import java.net.*;

public class R {
  public static final int PORTA_PADRAO = 12345;

  public static void main(String[] args) {
    int porta = PORTA_PADRAO;
    if (args.length > 0) {
      try {
        porta = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.err.println("Porta inválida.");
        return;
      }
    }

    System.out.println("[R] Receptor iniciado na porta " + porta);

    try (ServerSocket servidor = new ServerSocket(porta)) {
      while (true) {
        System.out.println("[R] Aguardando conexão...");
        try (Socket conexao = servidor.accept();
             ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
             ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream())) {

          System.out.println("[R] Conectado: " + conexao.getInetAddress());
          boolean continuar = true;

          while (continuar) {
            try {
              Comunicado comunicado = (Comunicado) receptor.readObject();

              if (comunicado instanceof Pedido) {
                Pedido pedido = (Pedido) comunicado;
                System.out.println("[R] Pedido recebido. Iniciando ordenação AVANÇADA...");

                // === LÓGICA NOVA INJETADA AQUI ===
                int qtdProcessadores = Runtime.getRuntime().availableProcessors();

                // 1. Divide o pedido em partes
                Pedido[] partesDoPedido = pedido.divide(qtdProcessadores);
                ThreadSorteadora[] threads = new ThreadSorteadora[qtdProcessadores];

                // 2. Dispara threads para ordenar cada parte
                for (int i = 0; i < qtdProcessadores; i++) {
                  threads[i] = new ThreadSorteadora(partesDoPedido[i]);
                  threads[i].start();
                }

                // 3. Espera todas terminarem
                for (int i = 0; i < qtdProcessadores; i++) {
                  threads[i].join();
                }

                // 4. Coleta os resultados
                byte[][] blocosOrdenados = new byte[qtdProcessadores][];
                for (int i = 0; i < qtdProcessadores; i++) {
                  blocosOrdenados[i] = threads[i].getResultado();
                }

                // 5. Faz o Merge Paralelo dos resultados
                byte[] resultadoFinal = ThreadSorteadora.intercalarMultiploComThreads(blocosOrdenados, qtdProcessadores);
                // =================================

                System.out.println("[R] Ordenação concluída. Enviando resposta.");
                transmissor.writeObject(new Resposta(resultadoFinal));
                transmissor.flush();
              }
              else if (comunicado instanceof ComunicadoEncerramento) {
                System.out.println("[R] Encerrando conexão.");
                continuar = false;
              }
            } catch (EOFException e) {
              continuar = false;
            }
          }
        } catch (Exception e) {
          System.err.println("[R] Erro na conexão: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.err.println("[R] Erro ao abrir porta: " + e.getMessage());
    }
  }
}