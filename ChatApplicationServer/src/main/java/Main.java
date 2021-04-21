import chat_impl.ChatServerImplementation;
import models.RegisteredUser;
import server.UdpServer;

import java.io.IOException;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        final var server = new UdpServer();
        server.start(4390);

        ChatServerImplementation chatServerImplementation = new ChatServerImplementation(server);

        try {
            chatServerImplementation.createChatCommunication();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
