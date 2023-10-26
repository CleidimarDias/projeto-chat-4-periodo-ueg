package chat;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class ChatClient implements Runnable {
    /**
     * Endereço IP ou nome DNS para conectar no servidor.
     * O número da porta é obtido diretamente da constante {@link BlockingChatServerApp#PORT}
     * na classe do servidor.
     */
    public static final String SERVER_ADDRESS = "127.0.0.1";

    /**
     * Objeto para capturar dados do teclado e assim
     * permitir que o usuário digite mensagens a enviar.
     */
    private final Scanner scanner;
    
    /**
     * Objeto que armazena alguns dados do cliente (como o login)
     * 
     */
    private ClientSocket clientSocket;
    
    /**
     * Executa a aplicação cliente.
     * Pode-se executar quantas instâncias desta classe desejar.
     * Isto permite ter vários clientes conectados e interagindo
     * por meio do servidor.     
     * 
     */
    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient();
            client.start();
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
    
    /**
     * Instancia um cliente, realizando o mínimo de operações necessárias.
     */
    public ChatClient(){
        scanner = new Scanner(System.in);
    }

    /**
     * Inicia o cliente, conectando ao servidor e
     * entrando no loop de envio e recebimento de mensagens.
     *
     */
    private void start() throws IOException {
        final Socket socket = new Socket(SERVER_ADDRESS, ChatServer.PORT);
        clientSocket = new ClientSocket(socket);
        System.out.println(
            "Cliente conectado ao servidor no endereço " + SERVER_ADDRESS +
            " e porta " + ChatServer.PORT);

        login();

        new Thread(this).start();
        messageLoop();
    }

    /**
     * Executa o login no sistema, enviando o login digitado para o servidor.
     * A primeira mensagem que o servidor receber após um cliente conectar é então o login daquele cliente.
     */
    private void login() {
        System.out.print("Digite seu login: ");
        final String login = scanner.nextLine();
        clientSocket.setLogin(login);
        clientSocket.sendMsg(login);
    }

    /**
     * Inicia o loop de envio e recebimento de mensagens.
     * O loop é interrompido quando o usuário digitar "sair".
     */
    private void messageLoop() {
        String msg;
        do {
            System.out.print("Digite uma msg (ou 'sair' para encerrar): ");
            msg = scanner.nextLine();
            clientSocket.sendMsg(msg);
        } while(!"sair".equalsIgnoreCase(msg));
        clientSocket.close();
    }

    /**
     * Aguarda mensagens do servidor enquanto o socket não for fechado
     * e o cliente não receber uma mensagem null.
     * Se uma mensagem null for recebida, é porque ocorreu erro na conexão com o servidor.
     * Neste caso, podemos encerrar a espera por novas mensagens.
     * 
     *
     */
    @Override
    public void run() {
        String msg;
        while((msg = clientSocket.getMessage())!=null) {
            System.out.println(msg);
        }
    }
}
