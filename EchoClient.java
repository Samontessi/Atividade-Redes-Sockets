import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class EchoClient {

    public static void main(String[] args) throws IOException {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 4444;

        try (Socket socket = new Socket(host, port);
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.err.println("Conectado a " + host + " na porta " + port);
            System.err.println("Comandos: ECHO <mensagem> | QUIT");

            String linha;
            while ((linha = stdin.readLine()) != null) {
                out.println(linha);              // envia o comando

                String resposta = in.readLine(); // espera a resposta
                if (resposta == null) {
                    System.err.println("Servidor fechou a conexão");
                    break;
                }
                System.out.println(resposta);

                if (resposta.equals("BYE")) {
                    break;
                }
            }
        }

        System.err.println("Conexão com " + host + " encerrada");
    }
}
