import java.net.Socket;
import java.util.*;
import java.io.*;

import java.lang.Math;
public class D {
    public static void main (String[] args) throws Exception {
        String[] ips = {"172.16.239.25","172.16.231.165", "172.16.130.112"};
        int PORTA_PADRAO = 3000;


        ArrayList<Socket> conexoes = new ArrayList<>();
        ArrayList<ObjectOutputStream> transmissores = new ArrayList<>();
        ArrayList<ObjectInputStream> receptores = new ArrayList<>();

        for (String ip : ips) {
            try {
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
        int tamanho = 1_000_000;
        byte []vetor = new byte[tamanho];
        populatearray(vetor);
        byte position = (byte) (Math.random() * vetor.length);
        byte value = vetor[position];
        byte qtd = (byte) (vetor.length/ips.length);
        int posIni = 0;
        int posFim = qtd-1;
        byte resto = (byte) (vetor.length % ips.length);
        byte qtdAparicoes = 0;
        // byte posIni = 0;
        // byte posFim = qtd;

        if (vetor.length%ips.length == 0){
            for(int i =0; i<ips.length; i++){
                byte[] subArray = Arrays.copyOfRange(vetor, posIni, posFim);
                Pedido pedido = new Pedido(subArray, value);
                Resposta resposta = null;
//
//              servidor.envia(pedidoo)
//              servidor.receba(servidorResposta)
//              resposta = servidorResposta
//              qtdAparicoes += resposta.contagem


                posIni = posFim +1;
                posFim += qtd;
            }

        } else{
            for(int i =0; i<ips.length; i++){
                if(i == ips.length -1){
                    posFim += resto;
                }


                posIni = posFim +1;
                posFim += qtd;
            }
        }

    }

    private static void populatearray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) (Math.random() * 201 - 100);
        }
    }

}

