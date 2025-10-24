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
        // Inicializando código
        String[] ips = {"172.16.239.25","172.16.231.165", "172.16.130.112"};

        int quantidadeIps = ips.length;

        int PORTA_PADRAO = 3000;

        Socket[] conexoesList = new Socket[quantidadeIps];
        ObjectInputStream[] receptoresList = new ObjectInputStream[quantidadeIps];
        ObjectOutputStream[] transmissoresList = new ObjectOutputStream[quantidadeIps];
        Thread[] threadsList = new Thread[quantidadeIps];
        byte[] resultados = new byte[quantidadeIps];

        ComunicadoEncerramento comunicadoEncerramento = new ComunicadoEncerramento();

        long inicioCódigo = System.currentTimeMillis();
        System.out.println("Sistema iniciando...");

        // Realizando conexões com outros computadores na rede
        for (int i = 0; i < ips.length; i++) {
            try {
                System.out.println("Fazendo conexão com máquina: " + ips[i]);
                Socket conexao = new Socket(ips[i], PORTA_PADRAO);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                conexoesList[i] = conexao;
                transmissoresList[i] = transmissor;
                receptoresList[i] = receptor;
                System.out.println("Conectado ao ip" + ips[i]);
            } catch (Exception e) {
                System.err.println("Falha ao conectar com " + ips[i] + ": " + e.getMessage());
            }
        }

        // Criando vetor de tamanho maximo
        int tamanho_Max = 1_477_891_713;
        int tamanho;
        do{
            System.out.print("Escolha um valor de no máximo " + tamanho_Max + " para inicializar o array: ");
            tamanho = Teclado.getUmInt();
            if(tamanho > tamanho_Max){
                System.out.println("Tamanho maior que o limite, escolha um valor menor");
            }
        }while( tamanho > tamanho_Max);
        byte []vetor = new byte[tamanho];
        populatearray(vetor);

        // Envio das mensagens para os servidores 
        int position = (int)(Math.random() * vetor.length);
        byte value = vetor[position];
        System.out.println("Valor a ser buscado: " + value);

        int qtd = vetor.length/ips.length;
        int posIni = 0;
        int posFim = qtd;

        int resto = vetor.length % ips.length;
        int qtdAparicoes = 0;

        if (vetor.length%ips.length == 0){
        // Tamanho de vetor divide exatamente com a quantidade de computadores
            for(int j =0; j<ips.length; j++){

                System.out.println("Envinado pacote para: " + ips[j]);

                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                final int indice = j;
                threadsList[j] = new Thread(() -> {
                    try{
                        long exec_servidor_ini = System.currentTimeMillis();
                        Pedido pedido = new Pedido(subArray, value);
                        Resposta resposta = null;
                        transmissoresList[indice].writeObject(pedido);
                        transmissoresList[indice].flush();

                        resposta = (Resposta)(receptoresList[indice].readObject());

                        resultados[indice]=resposta.getContagem();

                        long exec_servidor_fim = System.currentTimeMillis();
                        System.out.println("Computador " + indice + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                        System.out.println("Valor " + value + " encontrado " + resultados[indice] + " vezes");

                    }catch (Exception e){
                        System.out.println(e);
                    }
                });
                threadsList[j].start();

                posIni = posFim +1;
                posFim += qtd;
            }
        } 
        else{
        // Tamanho de vetor por pcs da numero quebrado
            for(int j =0; j<ips.length; j++){
                if(j == ips.length -1){
                    posFim += resto;
                }

                System.out.println("Envinado pacote para: " + ips[j]);

                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                final int indice = j;
                threadsList[j] = new Thread(() -> {
                    try{
                        long exec_servidor_ini = System.currentTimeMillis();
                        Pedido pedido = new Pedido(subArray, value);
                        Resposta resposta = null;
                        transmissoresList[indice].writeObject(pedido);
                        transmissoresList[indice].flush();

                        resposta = (Resposta)(receptoresList[indice].readObject());

                        resultados[indice]=resposta.getContagem();

                        long exec_servidor_fim = System.currentTimeMillis();
                        System.out.println("Computador " + indice + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                        System.out.println("Valor " + value + " encontrado " + resultados[indice] + " vezes");

                    }catch (Exception e){
                        System.out.println(e);
                    }
                });
                threadsList[j].start();

                posIni = posFim +1;
                posFim += qtd;
                
            }
        }

        for (Thread t: threadsList){
            t.join();
        }

        for (int k =0; k<resultados.length; k++){
            qtdAparicoes += resultados[k];
            transmissoresList[k].writeObject(comunicadoEncerramento);
            transmissoresList[k].flush();
            transmissoresList[k].close();
            receptoresList[k].close();
            conexoesList[k].close();
            System.out.println("Conexão com " + ips[k] + " encerrada.");
        }

        System.out.println("O valor " + value + " aparece " + qtdAparicoes + " vezes");
        long fimCodigo= System.currentTimeMillis();
        System.out.println("Tempo de execução total: " + (fimCodigo - inicioCódigo));

    }

 

}

