import chat_impl.ChatServerImplementation;
import server.UdpServer;

import java.io.IOException;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {


    public static void main(String[] args) throws IOException {
        final var server = new UdpServer();
        server.start();

        ChatServerImplementation chatServerImplementation = new ChatServerImplementation(server);

        try {
            chatServerImplementation.createChatCommunication();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
