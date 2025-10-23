import java.util.Vector;
import java.lang.Math;
public class D {
    private final Vector<String> ips = ['172.16.239.25','172.16.231.165', "172.16.130.112"];

    public static void main (String[] args)
    {
        int tamanho = 1_000_000;
       byte []vetor = new byte[tamanho];
    }

    private void populatearray(byte[]array){
        for (int i=0;i<array.length;i++){
            array[i]= (byte) Math.random();
        }

    }
}

