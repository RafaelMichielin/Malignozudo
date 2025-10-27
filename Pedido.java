public class Pedido extends Comunicado {
  private int[] numero;
  private byte procurado;

  public Pedido(int n[], byte pr) throws Exception {

    if (n == null) {
      throw new Exception("Array de números vazio");
    }

    if (pr < -100 || pr > 100) {
      throw new Exception("Valor procurado inválido");
    }
    this.numero = n;
    this.procurado = pr;
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

  public byte getProcurado() { return procurado; }

  public int[] getNumero() { return numero; }
}