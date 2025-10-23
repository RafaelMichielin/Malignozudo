import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//172.16.239.25 -> vinicius
//172.16.231.165 -> rafael
// 172.16.130.112 -> samuel
public class R {

    public static void main (String[] args)
    {
        int PORTA_PADRAO = 3000;
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



}}