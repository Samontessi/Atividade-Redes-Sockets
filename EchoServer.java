import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class EchoServer {

    public static void main(String[] args) throws IOException {
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 4444;
        int numThreads = (args.length > 1) ? Integer.parseInt(args[1]) : 10;

        // pool fixo: no máximo numThreads clientes atendidos ao mesmo tempo;
        // conexões excedentes esperam na fila interna do executor
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        // ao encerrar o servidor, finaliza o pool de forma ordenada
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Encerrando o pool de threads...");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.err.println("Servidor iniciado na porta " + port
                    + " com pool de " + numThreads + " threads");

            while (true) {
                // espera bloqueante até chegar uma conexão
                Socket clientSocket = serverSocket.accept();
                System.err.println("Conexão aceita de " + clientSocket.getRemoteSocketAddress());

                // entrega o atendimento ao pool; uma thread livre executa o handler
                // e a thread principal volta imediatamente para o accept()
                pool.submit(new ClientHandler(clientSocket));
            }
        } finally {
            pool.shutdown();
        }
    }
}

/** Atende um único cliente, seguindo o protocolo. */
class ClientHandler implements Runnable {

    private final Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String cliente = socket.getRemoteSocketAddress().toString();
        String thread = Thread.currentThread().getName();

        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            String linha;
            while ((linha = in.readLine()) != null) {
                System.err.println("[" + thread + "] " + cliente + " -> " + linha);

                String resposta = processar(linha);
                out.println(resposta);

                if (resposta.equals("BYE")) {
                    break; // estado ENCERRADO
                }
            }
        } catch (IOException e) {
            System.err.println("[" + thread + "] Erro com " + cliente + ": " + e.getMessage());
        }

        System.err.println("[" + thread + "] Conexão encerrada com " + cliente);
    }

    // Interpreta um comando e devolve a resposta do protocolo 
    private String processar(String linha) {
        String texto = linha.trim();
        if (texto.isEmpty()) {
            return "ERR comando vazio";
        }

        // separa comando e parâmetro
        String[] partes = texto.split("\\s+", 2);
        String comando = partes[0].toUpperCase();
        String parametro = (partes.length > 1) ? partes[1] : "";

        switch (comando) {
            case "ECHO":
                if (parametro.isEmpty()) {
                    return "ERR ECHO precisa de uma mensagem";
                }
                return "OK " + parametro;
            case "QUIT":
                return "BYE";
            default:
                return "ERR comando desconhecido: " + comando;
        }
    }
}
