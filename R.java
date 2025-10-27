import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class R {
  public static final int PORTA_PADRAO = 3000;
  public static void main(String[] args) {

    if (args.length > 1) {
      System.err.println(
          "Somente argumento da porta esperado (java R [porta])");
      return;
    }
    int porta = R.PORTA_PADRAO;
    if (args.length == 1)
      porta = Integer.parseInt(args[0]);
    System.out.println("Receptor ativo na porta " + porta);

    try (ServerSocket servidor = new ServerSocket(porta)) {
      System.out.println("Servidor aguardando conexões...");

      while (true) {
        try {
          Socket conexao = servidor.accept();
          System.out.println("Conexão estabelecida com " +
                             conexao.getInetAddress().getHostAddress());

          ObjectOutputStream transmissor =
              new ObjectOutputStream(conexao.getOutputStream());
          ObjectInputStream receptor =
              new ObjectInputStream(conexao.getInputStream());

          boolean continuar = true;
          while (continuar) {
            try {
              Comunicado comunicado = (Comunicado)receptor.readObject();

              if (comunicado instanceof Pedido) {
                Pedido pedido = (Pedido)comunicado;
                byte[] numeros = pedido.getNumero();
                byte procurado = pedido.getProcurado();
                int quantidadeProcessadores =
                    Runtime.getRuntime().availableProcessors();
                System.out.println("Processadores disponíveis no sistema: " +
                                   quantidadeProcessadores);

                int tamanho = numeros.length;
                int parte = tamanho / quantidadeProcessadores;
                int resto = tamanho % quantidadeProcessadores;

                Thread[] threads = new Thread[quantidadeProcessadores];
                int[] contagens = new int[quantidadeProcessadores];

                int inicio = 0;
                int fim = parte - 1;

                for (int i = 0; i < quantidadeProcessadores; i++) {
                  if (i == quantidadeProcessadores - 1)
                    fim += resto;

                  final int ini = inicio;
                  final int f = fim;
                  final int indice = i;

                  System.out.println("Thread " + i + " processando de " + ini +
                                     " até " + f);

                  threads[i] = new Thread(() -> {
                    int c = 0;
                    for (int j = ini; j <= f; j++) {
                      if (numeros[j] == procurado)
                        c++;
                    }
                    contagens[indice] = c;
                  });

                  threads[i].start();
                  inicio = fim + 1;
                  fim += parte;
                }

                for (int i = 0; i < quantidadeProcessadores; i++) {
                  threads[i].join();
                }

                int total = 0;
                for (int i = 0; i < quantidadeProcessadores; i++) {
                  System.out.println("Contagem da thread " + i + ": " +
                                     contagens[i]);
                  total += contagens[i];
                }

                System.out.println("Total de ocorrências do valor " +
                                   procurado + ": " + total);

                transmissor.writeObject(new Resposta((byte)total));
                transmissor.flush();

              }

              else if (comunicado instanceof ComunicadoEncerramento) {
                System.out.println("Encerrando conexão com Distribuidor...");
                continuar = false;
              }
            } catch (Exception e) {
              System.out.println("Erro na comunicação: " + e.getMessage());
              e.printStackTrace();
              continuar = false;
            }
          }

          transmissor.close();
          receptor.close();
          conexao.close();
          System.out.println("Conexão fechada. Aguardando nova conexão...");

        } catch (Exception e) {
          System.err.println("Erro ao processar conexão: " + e.getMessage());
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao criar ServerSocket: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
