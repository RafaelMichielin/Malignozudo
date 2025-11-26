import java.io.*;
import java.net.*;

public class R {
  public static final int PORTA_PADRAO = 12345;

  public static void main(String[] args) {
    int porta = PORTA_PADRAO;
    if (args.length > 0) {
      try {
        porta = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
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
                System.out.println("[R] Pedido recebido. Iniciando ordenação paralela...");

                byte[] resultado = pedido.ordenar();

                System.out.println("[R] Ordenação concluída. Enviando resposta.");
                transmissor.writeObject(new Resposta(resultado));
                transmissor.flush();
              }
              else if (comunicado instanceof ComunicadoEncerramento) {
                System.out.println("[R] Encerrando conexão.");
                continuar = false;
              }
            } catch (EOFException e) {
              continuar = false; // Cliente desconectou
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