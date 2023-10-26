package chat;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Permite enviar e receber mensagens por meio de um socket cliente.
 
 *
 * <p>O servidor cria uma instância desta classe para cada cliente conectado,
 * assim ele pode mensagens para e receber mensagens de cada cliente.
 * Cada cliente que conecta no servidor também cria uma instância dessa classe,
 * assim ele pode enviar para e receber mensagens do servidor.</p>
 *  
 */
public class ClientSocket implements Closeable {
    /**
     * Login do cliente.
     */
    private String login;

    /**
     * Socket representando a conexão de um cliente com o servidor.
     */
    private final Socket socket;

    /**
     *  Permite ler mensagens recebidas ou enviadas pelo cliente.
    
     */
    private final BufferedReader in;

    /**
     *  Permite enviar mensagens do cliente para o servidor ou do servidor para o cliente.
     *  S
     */
    private final PrintWriter out;

    /**
     * Instancia um ClientSocket.
     *
    
     */
    public ClientSocket(final Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    
    public boolean sendMsg(String msg) {
        out.println(msg);
        
        //retorna true se não houve nenhum erro ao enviar mensagem ou false caso tenha havido
        return !out.checkError();
    }

    
    public String getMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    /**
     * Fecha a conexão do socket e os objetos usados para enviar e receber mensagens.
     */
    @Override
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch(IOException e){
            System.err.println("Erro ao fechar socket: " + e.getMessage());
        }
    }

    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    public boolean isOpen(){
        return !socket.isClosed();
    }
}

