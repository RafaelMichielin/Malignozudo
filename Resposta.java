public class Resposta extends Comunicado {

    private byte contagem;

    public Resposta(byte contagem) throws Exception {
        if(contagem < 0){
            throw new Exception("Contagem inválida");
        }
        this.contagem = contagem;
    }

    public byte getContagem(){
        return this.contagem;
    }

    public String toString(){
        return (""+this.contagem);
    }
}
