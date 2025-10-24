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
        int PORTA_PADRAO = 3000;
        ArrayList<Socket> conexoes = new ArrayList<>();
        ArrayList<ObjectOutputStream> transmissores = new ArrayList<>();
        ArrayList<ObjectInputStream> receptores = new ArrayList<>();

        ComunicadoEncerramento comunicadoEncerramento = new ComunicadoEncerramento();

        long inicioCódigo = System.currentTimeMillis();
        System.out.println("Sistema iniciando...");

        // Realizando conexões com outros computadores na rede
        for (String ip : ips) {
            try {
                System.out.println("Fazendo conexão com máquina: " + ip);
                Socket conexao = new Socket(ip, PORTA_PADRAO);
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
                conexoes.add(conexao);
                transmissores.add(transmissor);
                receptores.add(receptor);
                System.out.println("Conectado ao ip" + ip);
            } catch (Exception e) {
                System.err.println("Falha ao conectar com " + ip + ": " + e.getMessage());
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
        }while( tamanho<=tamanho_Max);
        byte []vetor = new byte[tamanho];
        populatearray(vetor);

        // Envio das mensagens para os servidores
        byte position = (byte) (Math.random() * vetor.length);
        byte value = vetor[position];
        byte qtd = (byte) (vetor.length/ips.length);
        int posIni = 0;
        int posFim = qtd-1;
        byte resto = (byte) (vetor.length % ips.length);
        byte qtdAparicoes = 0;

        Thread[] threads = new Thread[ips.length];

        // Tamanho de vetor divide exatamente com a quantidade de computadores
        if (vetor.length%ips.length == 0){
            for(int i =0; i<ips.length; i++){
                System.out.println("Envinado pacote para: " + ips[i]);
                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                final int indice = i;
                byte qtdServidor = 0;
                threads[i] = new Thread(() -> {
                    try{
                        long exec_servidor_ini = System.currentTimeMillis();
                        Pedido pedido = new Pedido(subArray, value);
                        Resposta resposta = null;
                        Socket conexao = new Socket(ips[indice], 3000);

                        ObjectOutputStream transmissor = new ObjectOutputStream (conexao.getOutputStream());
                        transmissor.writeObject(pedido);
                        transmissor.flush();

                        ObjectInputStream receptor = new ObjectInputStream (conexao.getInputStream());
                        resposta = (Resposta)(receptor.readObject());
                        transmissor.writeObject(comunicadoEncerramento);
                        conexao.close();
                        qtdServidor=resposta.getContagem();
                        long exec_servidor_fim = System.currentTimeMillis();
                        System.out.println("Computador " + indice + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                        System.out.println("Valor " + value + " encontrado " + qtdServidor + " vezes");

                    }catch (Exception e){
                        System.out.println(e);
                    }
                });
                threads[i].start();



                qtdAparicoes += qtdServidor;


                posIni = posFim +1;
                posFim += qtd;
            }
        } 
        // Tamanho de vetor por pcs da numero quebrado
        else{
            for(int i =0; i<ips.length; i++){
                if(i == ips.length -1){
                    posFim += resto;
                }

                long exec_servidor_ini = System.currentTimeMillis();

                System.out.println("Envinado pacote para: " + ips[i]);

                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                Pedido pedido = new Pedido(subArray, value);

                Resposta resposta = null;
                Socket conexao = new Socket(ips[i], 3000);

                ObjectOutputStream transmissor = new ObjectOutputStream (conexao.getOutputStream());
                transmissor.writeObject(pedido);
                transmissor.flush();

                ObjectInputStream receptor = new ObjectInputStream (conexao.getInputStream());
                resposta = (Resposta)(receptor.readObject());

                transmissor.writeObject(comunicadoEncerramento);
                conexao.close();

                byte qtdServidor=resposta.getContagem();

                long exec_servidor_fim = System.currentTimeMillis();
                System.out.println("Computador " + i + " realizou contagem em: " + (exec_servidor_fim - exec_servidor_ini));
                System.out.println("Valor " + value + " encontrado " + qtdServidor + " vezes");
                qtdAparicoes += qtdServidor;

                posIni = posFim +1;
                posFim += qtd;

                
            }
        }

        System.out.println("O valor " + value + " aparece " + qtdAparicoes + " vezes");
        long fimCodigo= System.currentTimeMillis();
        System.out.println("Tempo de execução total: " + (fimCodigo - inicioCódigo));

    }

 

}

