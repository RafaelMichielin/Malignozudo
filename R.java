import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.net.*;

public class R {
    public static final int PORTA_PADRAO = 3000;
    public static void main(String[] args) {

        if(args.length >1)
            {
                System.err.println("uso esperado da porta fodase");
                return;
            }
        int porta = R.PORTA_PADRAO;
        if(args.length == 1)
            porta = Integer.parseInt(args[0]);
        System.out.println("Receptor ativo na porta " + porta);

        ServerSocket socket= null;


        try {
            ServerSocket servidor = new ServerSocket(porta);
            System.out.println("Aguardando conexão do Distribuidor...");

            for (;;) {
                Socket conexao = servidor.accept();
                System.out.println("Conexão estabelecida com " + conexao.getInetAddress().getHostAddress());

                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());

                boolean continuar = true;

                while (continuar) {
                    try {
                        Comunicado comunicado = (Comunicado) receptor.readObject();

                        // Recebimento do pedido
                        if (comunicado instanceof Pedido) {
                            Pedido pedido = (Pedido) comunicado;
                            byte []numeros = pedido.getNumero();
                            byte procurado = pedido.getProcurado();
                            int quantidadeProcessadores = Runtime.getRuntime().availableProcessors();
                          //  System.out.println("Processadores disponíveis: " + quantidadeProcessadores);

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

                            // Espera as threads terminarem
                            for (int i = 0; i < quantidadeProcessadores; i++) {
                                threads[i].join();
                            }

                            // Soma total
                            int total = 0;
                            for (int i = 0; i < quantidadeProcessadores; i++) {
                                total += contagens[i];
                            }

                            // Envia resposta ao Distribuidor
                            transmissor.writeObject(new Resposta((byte) total));
                            transmissor.flush();

                            System.out.println("Contagem concluída e enviada: " + total);
                        }

                        // --- Pedido de encerramento ---
                        else if (comunicado instanceof ComunicadoEncerramento) {
                            System.out.println("Encerrando conexão com Distribuidor...");
                            continuar = false;
                            transmissor.close();
                            receptor.close();
                            conexao.close();
                        }
                    } catch (Exception e) {
                        continuar = false;
                        System.out.println("Erro na comunicação: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro no receptor: " + e.getMessage());
        }
    }
}


