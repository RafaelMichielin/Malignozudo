public class Resposta extends Comunicado {

  private byte[] vetorOrdenado;

  public Resposta(byte[] vetor) throws Exception {
    if (vetor == null)
      throw new Exception("Vetor nulo");
    this.vetorOrdenado = vetor;
  }

  public byte[] getVetor() {
    return this.vetorOrdenado;
  }
}
