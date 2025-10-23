public class Resposta extends Comunicado {

private Byte contagem;

    public Resposta(byte contagem) {
        this.contagem = contagem;
    }
public byte getContagem(){
    return this.contagem;
}
public toString(){
    return (""+this.contagem);
}
}
