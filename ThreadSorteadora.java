public class ThreadSorteadora extends Thread {
    private Pedido pedido;
    private byte[] resultado;

    public ThreadSorteadora(Pedido pedido) {
        this.pedido = pedido;
    }

    @Override
    public void run() {
        // Pega os dados do pedido e ordena localmente
        byte[] numeros = pedido.getNumeros();
        resultado = ordenar(numeros);
    }

    public byte[] getResultado() {
        return resultado;
    }

    // Lógica de Merge Sort
    public static byte[] ordenar(byte[] numeros){
        if (numeros == null || numeros.length <= 1) return numeros;
        byte[] auxiliar = new byte[numeros.length];
        ordenarMergeSort(numeros, auxiliar, 0, numeros.length - 1);
        return numeros;
    }

    private static void ordenarMergeSort(byte[] a, byte[] auxiliar, int esquerdo, int direito){
        if (esquerdo >= direito) return;
        int meio = esquerdo + (direito - esquerdo) / 2;
        ordenarMergeSort(a, auxiliar, esquerdo, meio);
        ordenarMergeSort(a, auxiliar, meio + 1, direito);
        intercalar(a, auxiliar, esquerdo, meio, direito);
    }

    private static void intercalar(byte[] a, byte[] auxiliar, int esquerdo, int meio, int direito){
        int i = esquerdo;
        int j = meio + 1;
        int k = esquerdo;
        while (i <= meio && j <= direito){
            if (a[i] <= a[j]) auxiliar[k++] = a[i++];
            else auxiliar[k++] = a[j++];
        }
        while (i <= meio) auxiliar[k++] = a[i++];
        while (j <= direito) auxiliar[k++] = a[j++];
        for (int idx = esquerdo; idx <= direito; idx++) a[idx] = auxiliar[idx];
    }

    // Lógica Junção Paralela (Merge K-Way com Threads)
    // Métodos auxiliares para juntar dois vetores
    public static byte[] intercalarDois(byte[] a, byte[] b){
        if (a == null || a.length == 0) return b;
        if (b == null || b.length == 0) return a;
        byte[] res = new byte[a.length + b.length];
        int i = 0, j = 0, k = 0;
        while (i < a.length && j < b.length) res[k++] = (a[i] <= b[j]) ? a[i++] : b[j++];
        while (i < a.length) res[k++] = a[i++];
        while (j < b.length) res[k++] = b[j++];
        return res;
    }

    // Thread auxiliar interna para fazer merge de pares em paralelo
    private static class TarefaJuncao extends Thread {
        private final byte[] a;
        private final byte[] b;
        private byte[] res;
        public TarefaJuncao(byte[] a, byte[] b){ this.a=a; this.b=b; }
        @Override public void run(){ res = intercalarDois(a,b); }
        public byte[] get(){ return res; }
    }

    public static byte[] intercalarMultiploComThreads(byte[][] arrays, int maximoDeThreads){
        if (arrays == null || arrays.length == 0) return new byte[0];
        if (arrays.length == 1) return arrays[0];
        byte[][] atual = arrays;

        while (atual.length > 1){
            int pares = atual.length / 2;
            int resto = atual.length % 2;
            int threadsUsar = Math.min(pares, Math.max(1, maximoDeThreads));

            TarefaJuncao[] tarefas = new TarefaJuncao[pares];
            int produzidos = 0;
            byte[][] proximaRodada = new byte[pares + resto][];
            int idx = 0;

            while (idx < pares){
                int lote = Math.min(threadsUsar, pares - idx);
                for (int i = 0; i < lote; i++){
                    tarefas[idx + i] = new TarefaJuncao(atual[(idx + i)*2], atual[(idx + i)*2 + 1]);
                    tarefas[idx + i].start();
                }
                for (int i = 0; i < lote; i++){
                    try { tarefas[idx + i].join(); } catch (InterruptedException ignored) {}
                    proximaRodada[produzidos++] = tarefas[idx + i].get();
                }
                idx += lote;
            }
            if (resto == 1) proximaRodada[produzidos] = atual[atual.length - 1];
            atual = proximaRodada;
        }
        return atual[0];
    }
}