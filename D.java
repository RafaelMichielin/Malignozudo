import java.io.*;
import java.lang.Math;
import java.net.Socket;
import java.util.*;

public class D {
  private static void populatearray(byte[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = (byte)(Math.random() * 201 - 100);
    }
  }

  public static void main(String[] args) throws Exception {
    int[] portas = {3000, 3001, 3002};
    int quantidadeIps = portas.length;

    Socket[] conexoesList = new Socket[quantidadeIps];
    ObjectInputStream[] receptoresList = new ObjectInputStream[quantidadeIps];
    ObjectOutputStream[] transmissoresList =
        new ObjectOutputStream[quantidadeIps];

    int[] resultados = new int[quantidadeIps];
    ComunicadoEncerramento comunicadoEncerramento =
        new ComunicadoEncerramento();

    System.out.println("Sistema iniciando...");

    // Realizando conexões com os servidores (uma vez)
    for (int i = 0; i < portas.length; i++) {
      try {
        System.out.println("Fazendo conexão com porta: " + portas[i]);
        Socket conexao = new Socket("localhost", portas[i]);
        ObjectOutputStream transmissor =
            new ObjectOutputStream(conexao.getOutputStream());
        ObjectInputStream receptor =
            new ObjectInputStream(conexao.getInputStream());
        conexoesList[i] = conexao;
        transmissoresList[i] = transmissor;
        receptoresList[i] = receptor;
        System.out.println("Conectado à porta " + portas[i]);
      } catch (Exception e) {
        System.err.println("Falha ao conectar com " + portas[i] + ": " +
                           e.getMessage());
      }
    }

    // Loop principal - processar múltiplas buscas
    while (true) {
      Thread[] threadsList = new Thread[quantidadeIps];

      long inicioCódigo = System.currentTimeMillis();

      int tamanho_Max = 1_477_891_713;
      int tamanho;
      do {
        System.out.print("Escolha um valor de no máximo " + tamanho_Max +
                         " para inicializar o array: ");
        tamanho = Teclado.getUmInt();
        if (tamanho > tamanho_Max) {
          System.out.println(
              "Tamanho maior que o limite, escolha um valor menor");
        }
      } while (tamanho > tamanho_Max);

      byte[] vetor = new byte[tamanho];
      populatearray(vetor);

      // Definição do valor a ser buscado
      int position = (int)(Math.random() * vetor.length);
      byte value = vetor[position];
      System.out.println("Valor a ser buscado: " + value);

      int qtdPortasValidas = 0;
      for (int i = 0; i < conexoesList.length; i++) {
        if (conexoesList[i] != null) {
          qtdPortasValidas++;
        }
      }

      System.out.println("Número de conexões válidas para envio: " +
                         qtdPortasValidas);

      int qtd = vetor.length / qtdPortasValidas;
      int posIni = 0;
      int posFim = qtd;

      int resto = vetor.length % portas.length;
      int qtdAparicoes = 0;

      int[] temposExec = new int[qtdPortasValidas];

      if (vetor.length % portas.length == 0) {
        // Divisão exata do vetor
        for (int j = 0; j < qtdPortasValidas; j++) {
          if (conexoesList[j] == null) {
            continue;
          }
          System.out.println("Enviando pacote para: " + portas[j]);

          byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
          final int indice = j;

          threadsList[j] = new Thread(() -> {
            try {
              long exec_servidor_ini = System.currentTimeMillis();
              Pedido pedido = new Pedido(subArray, value);
              transmissoresList[indice].writeObject(pedido);
              transmissoresList[indice].flush();

              Resposta resposta =
                  (Resposta)(receptoresList[indice].readObject());
              resultados[indice] = resposta.getContagem(); // sem cast para byte

              long exec_servidor_fim = System.currentTimeMillis();
              temposExec[indice] = (int)(exec_servidor_fim - exec_servidor_ini);
            } catch (Exception e) {
              System.out.println(e);
            }
          });
          threadsList[j].start();

          posIni = posFim + 1;
          posFim += qtd;
        }
      } else {
        // Divisão com resto
        for (int j = 0; j < qtdPortasValidas; j++) {
          if (conexoesList[j] == null) {
            continue;
          }
          if (j == portas.length - 1) {
            posFim += resto;
          }

          System.out.println("Enviando pacote para: " + portas[j]);
          byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
          final int indice = j;

          threadsList[j] = new Thread(() -> {
            try {
              long exec_servidor_ini = System.currentTimeMillis();
              Pedido pedido = new Pedido(subArray, value);
              transmissoresList[indice].writeObject(pedido);
              transmissoresList[indice].flush();

              Resposta resposta =
                  (Resposta)(receptoresList[indice].readObject());
              resultados[indice] = resposta.getContagem(); // sem cast para byte

              long exec_servidor_fim = System.currentTimeMillis();
              temposExec[indice] = (int)(exec_servidor_fim - exec_servidor_ini);
            } catch (Exception e) {
              System.out.println(e);
            }
          });
          threadsList[j].start();

          posIni = posFim + 1;
          posFim += qtd;
        }
      }

      // Esperar threads terminarem
      for (Thread t : threadsList) {
        if (t != null) {
          t.join();
        }
      }

      for (int k = 0; k < qtdPortasValidas; k++) {
        System.out.println("Resultado do computador " + portas[k] + ": " +
                           resultados[k]);
        System.out.println("Tempo de execução do computador " + portas[k] +
                           ": " + temposExec[k] + " ms");
      }

      // Somar resultados
      for (int k = 0; k < resultados.length; k++) {
        qtdAparicoes += resultados[k];
      }

      System.out.println("O valor " + value + " aparece " + qtdAparicoes +
                         " vezes");
      long fimCodigo = System.currentTimeMillis();
      System.out.println("Tempo de execução total: " +
                         (fimCodigo - inicioCódigo));

      String resposta;

      do {
        System.out.print("\nDeseja realizar outra busca? (s/n): ");
        resposta = Teclado.getUmString();
        if (!resposta.equalsIgnoreCase("n") &&
            !resposta.equalsIgnoreCase("s")) {
          System.out.println(
              "Resposta inválida. Por favor, digite 's' para sim ou 'n' para não.");
        }
      } while (!resposta.equalsIgnoreCase("s") &&
               !resposta.equalsIgnoreCase("n"));

      if (resposta.equalsIgnoreCase("n")) {
        System.out.println("Encerrando o sistema...");
        break;
      }
    }

    // Encerrar conexões
    for (int k = 0; k < resultados.length; k++) {
      if(conexoesList[k]==null){
        continue;
      }
      transmissoresList[k].writeObject(comunicadoEncerramento);
      transmissoresList[k].flush();
      transmissoresList[k].close();
      receptoresList[k].close();
      conexoesList[k].close();
      System.out.println("Conexão com " + portas[k] + " encerrada.");
    }
  }
}
