import java.net.Socket;
import java.util.*;
import java.io.*;
import java.lang.Math;

public class D {
    private static void populatearray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (Math.random() * 201 - 100);
        }
    }

    public static void main (String[] args) throws Exception {
        int [] portas = {3000,3001,3002};
        int quantidadeIps = portas.length;
        int PORTA_PADRAO = 3000;

        Socket[] conexoesList = new Socket[quantidadeIps];
        ObjectInputStream[] receptoresList = new ObjectInputStream[quantidadeIps];
        ObjectOutputStream[] transmissoresList = new ObjectOutputStream[quantidadeIps];
        Thread[] threadsList = new Thread[quantidadeIps];
        int[] resultados = new int[quantidadeIps]; // trocado de byte[] para int[]

        ComunicadoEncerramento comunicadoEncerramento = new ComunicadoEncerramento();

        long inicioCódigo = System.currentTimeMillis();
        System.out.println("Sistema iniciando...");

        // Conexão com receptores
        for (int i = 0; i < portas.length; i++) {
            try {
                System.out.println("Fazendo conexão com máquina: " + portas[i]);
                Socket conexao = new Socket("localhost", portas[i]);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                conexoesList[i] = conexao;
                transmissoresList[i] = transmissor;
                receptoresList[i] = receptor;
                System.out.println("Conectado ao ip " + portas[i]);
            } catch (Exception e) {
                System.err.println("Falha ao conectar com " + portas[i] + ": " + e.getMessage());
            }
        }

        // Criação do vetor
        int tamanho_Max = 1_477_891_713;
        int tamanho;
        do {
            System.out.print("Escolha um valor de no máximo " + tamanho_Max + " para inicializar o array: ");
            tamanho = Teclado.getUmInt();
            if (tamanho > tamanho_Max) {
                System.out.println("Tamanho maior que o limite, escolha um valor menor");
            }
        } while (tamanho > tamanho_Max);

        byte[] vetor = new byte[tamanho];
        populatearray(vetor);

        // Definição do valor a ser buscado
        int position = (int)(Math.random() * vetor.length);
        byte value = vetor[position];
        System.out.println("Valor a ser buscado: " + value);

        int qtd = vetor.length / portas.length;
        int posIni = 0;
        int posFim = qtd;

        int resto = vetor.length % portas.length;
        int qtdAparicoes = 0;

        if (vetor.length % portas.length == 0) {
            // Divisão exata do vetor
            for (int j = 0; j < portas.length; j++) {
                System.out.println("Enviando pacote para: " + portas[j]);

                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                final int indice = j;

                threadsList[j] = new Thread(() -> {
                    try {
                        long exec_servidor_ini = System.currentTimeMillis();
                        Pedido pedido = new Pedido(subArray, value);
                        transmissoresList[indice].writeObject(pedido);
                        transmissoresList[indice].flush();

                        Resposta resposta = (Resposta)(receptoresList[indice].readObject());
                        resultados[indice] = resposta.getContagem(); // sem cast para byte

                        long exec_servidor_fim = System.currentTimeMillis();
                        System.out.println("Computador " + indice + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                        System.out.println("Valor " + value + " encontrado " + resultados[indice] + " vezes");
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
            for (int j = 0; j < portas.length; j++) {
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

                        Resposta resposta = (Resposta)(receptoresList[indice].readObject());
                        resultados[indice] = resposta.getContagem(); // sem cast para byte

                        long exec_servidor_fim = System.currentTimeMillis();
                        System.out.println("Computador " + indice + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                        System.out.println("Valor " + value + " encontrado " + resultados[indice] + " vezes");
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
            t.join();
        }

        // Encerrar conexões e somar resultados
        for (int k = 0; k < resultados.length; k++) {
            qtdAparicoes += resultados[k];
            transmissoresList[k].writeObject(comunicadoEncerramento);
            transmissoresList[k].flush();
            transmissoresList[k].close();
            receptoresList[k].close();
            conexoesList[k].close();
            System.out.println("Conexão com " + portas[k] + " encerrada.");
        }

        System.out.println("O valor " + value + " aparece " + qtdAparicoes + " vezes");
        long fimCodigo = System.currentTimeMillis();
        System.out.println("Tempo de execução total: " + (fimCodigo - inicioCódigo));
    }
}
