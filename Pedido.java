import java.io.*;
public class Pedido extends Comunicado {
    private byte[] numero;
    private byte procurado;

    public  Pedido(byte n[], byte pr) throws Exception {
        try {
            this.numero = n;
            this.procurado = pr;
        } catch (Exception erro) {
            throw new Exception("Erro de construtor");
        }
    }

    public byte contar() {
        byte qtd = 0;
        for (int i = 0; i < this.numero.length; i++) {
            if (this.numero[i] == this.procurado) {
                qtd++;
            }
        }
        return qtd;
    }
}