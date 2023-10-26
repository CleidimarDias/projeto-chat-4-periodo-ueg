package chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class ChatServer {
    
    public static final int PORT = 4000;

    /**
     * Objeto que permite ao servidor ficar escutando na porta especificada acima.
     */
    private ServerSocket serverSocket;

    /**
     * Lista de todos os clientes conectados ao servidor.
     */
    private final List<ClientSocket> clientSocketList;

    public ChatServer() {
        clientSocketList = new LinkedList<>();
    }

    /**
     * Executa a aplicação servidora que fica em loop infinito aguardando conexões
     * dos clientes.
    
     */
    public static void main(String[] args) {
        final ChatServer server = new ChatServer();
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }

    /**
     * Inicia a aplicação, criando um socket para o servidor
    
     * 
     
     */
    private void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println(
                "Servidor de chat  iniciado no endereço " + serverSocket.getInetAddress().getHostAddress() +
                " e porta " + PORT);
        clientConnectionLoop();
    }

    /**
     * Inicia o loop infinito de espera por conexões dos clientes. Cada vez que um
     * 
     * {@link #clientMessageLoop} que ficará
     * esperando mensagens do cliente.
     * 
     * 
     */
    private void clientConnectionLoop() throws IOException {
        try {
            while (true) {
                System.out.println("Aguardando conexão de novo cliente");
                
                final ClientSocket clientSocket;
                try {
                    clientSocket = new ClientSocket(serverSocket.accept());
                    System.out.println("Cliente " + clientSocket.getRemoteSocketAddress() + " conectado");
                }catch(SocketException e){
                    System.err.println("Erro ao aceitar conexão do cliente. O servidor possivelmente está sobrecarregado:");
                    System.err.println(e.getMessage());
                    continue;
                }

                /*
                Cria uma nova Thread para permitir que o servidor não fique bloqueado enquanto
                atende às requisições de um único cliente.
                */
                try {
                    new Thread(() -> clientMessageLoop(clientSocket)).start();
                    clientSocketList.add(clientSocket);
                }catch(OutOfMemoryError ex){
                    System.err.println(
                            "Não foi possível criar thread para novo cliente. O servidor possivelmente está sobrecarregdo. Conexão será fechada: ");
                    System.err.println(ex.getMessage());
                    clientSocket.close();
                }
            }
        } finally{
            /*Se sair do laço de repetição por algum erro, exibe uma mensagem
            indicando que o servidor finalizou e fecha o socket do servidor.*/
            stop();
        }
    }

    /**
     * Método executado sempre que um cliente conectar ao servidor.
     * O método fica em loop aguardando mensagens do cliente,
     * até que este desconecte.
     * A primeira mensagem que o servidor receber após um cliente conectar é o login enviado pelo cliente.
     * 
     
     */
    private void clientMessageLoop(final ClientSocket clientSocket){
        try {
            String msg;
            while((msg = clientSocket.getMessage()) != null){
                final SocketAddress clientIP = clientSocket.getRemoteSocketAddress();
                if("sair".equalsIgnoreCase(msg)){
                    return;
                }

                if(clientSocket.getLogin() == null){
                    clientSocket.setLogin(msg);
                    System.out.println("Cliente "+ clientIP + " logado como " + clientSocket.getLogin() +".");
                    msg = "Cliente " + clientSocket.getLogin() + " logado.";
                }
                else {
                    System.out.println("Mensagem recebida de "+ clientSocket.getLogin() +": " + msg);
                    msg = clientSocket.getLogin() + " diz: " + msg;
                };

                sendMsgToAll(clientSocket, msg);
            }
        } finally {
            clientSocket.close();
        }
    }  
    
    
    private void sendMsgToAll(final ClientSocket sender, final String msg) {
        final Iterator<ClientSocket> iterator = clientSocketList.iterator();
        int count = 0;
        
        /*Percorre a lista usando o iterator enquanto existir um próxima elemento (hasNext)
        para processar, ou seja, enquanto não percorrer a lista inteira.*/
        while (iterator.hasNext()) {
            //Obtém o elemento atual da lista para ser processado.
            final ClientSocket client = iterator.next();
            /*Verifica se o elemento atual da lista (cliente) não é o cliente que enviou a mensagem.
            Se não for, encaminha a mensagem pra tal cliente.*/
            if (!client.equals(sender)) {
                if(client.sendMsg(msg))
                    count++;
                else iterator.remove();
            }
        }
        System.out.println("Mensagem encaminhada para " + count + " clientes");
    }

    /**
     * Fecha o socket do servidor quando a aplicação estiver sendo finalizada.
     */
    private void stop()  {
        try {
            System.out.println("Finalizando servidor");
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket do servidor: " + e.getMessage());
        }
    }
}