import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.io.*;
import java.net.*;

//172.16.239.25 -> vinicius
//172.16.231.165 -> rafael
// 172.16.130.112 -> samuel
public class R {
    public static void main (String[] args)
    {
        int PORTA_PADRAO = 3000;
        System.out.println("Receptor ativo"+ PORTA_PADRAO);
        try{
            ServerSocket servidor = new ServerSocket(PORTA_PADRAO);

            for (;;) {
                Socket conexao = servidor.accept();
                ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());

                System.out.println("Conexão estabelecida");
                boolean continuar = true;
                while(continuar){
                    try{
                        Comunicado comunicado = (Comunicado) receptor.readObject();

                        if (comunicado instanceof Pedido){
                            Pedido pedido = (Pedido) comunicado;
                            byte contagem = pedido.contar();

                            Resposta resposta = new Resposta(contagem);
                            transmissor.writeObject(resposta);
                            transmissor.flush();
                        }
                        else if (comunicado instanceof ComunicadoEncerramento){
                            continuar = false;
                            transmissor.close();
                            receptor.close();
                            conexao.close();
                        }
                    }catch (Exception e){
                        continuar = false;
                        System.out.println("Conexão encerrada");
                    }
                }
            }

        }
        catch (Exception e){
            System.err.println("Erro no receptor"+ e.getMessage());
        }
//        while true
//        if(nao recebe nada){ignorea}
//        if(recebe)
//        e


        Socket conexao=null;
        try
        {

            int porta= PORTA_PADRAO;

            conexao = new Socket (host, porta);
        }
        catch (Exception erro)
        {
            System.err.println ("Indique o servidor e a porta corretos!\n");
            return;
        }

        ObjectOutputStream transmissor=null;
        try
        {
            transmissor =
                    new ObjectOutputStream(
                            conexao.getOutputStream());
        }
        catch (Exception erro)
        {
            System.err.println ("Indique o servidor e a porta corretos!\n");
            return;
        }

        ObjectInputStream receptor=null;
        try
        {
            receptor =
                    new ObjectInputStream(
                            conexao.getInputStream());
        }
        catch (Exception erro)
        {
            System.err.println ("Indique o servidor e a porta corretos!\n");
            return;
        }
        ComunicadoEncerramento comunicadoEncerramento = null;
    }


}
