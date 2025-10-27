public class Resposta extends Comunicado {

  private int contagem;

  public Resposta(int contagem) throws Exception {
    if (contagem < 0) {
      throw new Exception("Contagem inválida");
    }
    this.contagem = contagem;
  }

  public int getContagem() { return this.contagem; }

  public String toString() { return ("" + this.contagem); }
}
