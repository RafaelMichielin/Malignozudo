import java.util.Arrays;
import java.util.Vector;
import java.lang.Math;
public class D {
    public static void main (String[] args) throws Exception {
        String[] ips = {"172.16.239.25","172.16.231.165", "172.16.130.112"};
        int tamanho = 1_000_000;
        byte []vetor = new byte[tamanho];
        populatearray(vetor);
        byte position = Math.random(vetor.length);
        byte value = vetor[position];

        byte qtd = vetor.length/ips.lenght;
        int posIni = 0;
        int posFim = qtd-1;
        byte resto = vetor.length % ips.length;
        byte qtdAparicoes = 0

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

    private static void populatearray(byte[] array){
        for (int i=0;i<array.length;i++){
            array[i]= (byte) Math.random(201)-100;
        }
    }

}

