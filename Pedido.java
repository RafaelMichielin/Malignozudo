public class Pedido extends Comunicado {
  private byte[] numeros;

  public Pedido(byte[] numeros) {
    this.numeros = numeros;
  }

  public byte[] getNumeros() {
    return this.numeros;
  }

  public Pedido[] divide(int divisoes) {
    if (divisoes <= 0) divisoes = 1;

    Pedido[] pedidos = new Pedido[divisoes];
    byte[] vetorOriginal = this.numeros;

    int tamanhoOriginal = vetorOriginal.length;
    int tamanhoBase = tamanhoOriginal / divisoes;
    int resto = tamanhoOriginal % divisoes;
    int posicao = 0;

    for (int i = 0; i < divisoes; i++) {
      int tamanhoSubvetor = tamanhoBase;
      if (resto > 0) {
        tamanhoSubvetor++;
        resto--;
      }

      byte[] subVetor = new byte[tamanhoSubvetor];
      System.arraycopy(vetorOriginal, posicao, subVetor, 0, tamanhoSubvetor);

      posicao += tamanhoSubvetor;
      pedidos[i] = new Pedido(subVetor);
    }
    return pedidos;
  }
}