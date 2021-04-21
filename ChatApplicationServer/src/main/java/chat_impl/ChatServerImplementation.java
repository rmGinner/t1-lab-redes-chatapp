package chat_impl;

import contracts.ClientJsonContract;
import contracts.UserContract;
import models.RegisteredUser;
import server.UdpServer;
import utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class ChatServerImplementation {
    private UdpServer udpServer;
    private Map<String, RegisteredUser> registeredUsers = new HashMap<>();

    public ChatServerImplementation(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    public void createChatCommunication() throws IOException {
        while (this.udpServer.isOpened()) {
            String receivedData = udpServer.receiveData();

            ClientJsonContract clientJsonContract = Utils.parseJson(receivedData, ClientJsonContract.class);

            if (Objects.nonNull(clientJsonContract)) {
                var user = registeredUsers.get(clientJsonContract.getFrom());
                if (Objects.nonNull(user)) {
                    if (user.getDuration().toSeconds() <= 0) {
                        System.out.println("Session expired");
                        InetAddress sentAddress = udpServer.getAddressFromLastReceivedPacket();
                        int sentPort = udpServer.getPortFromLastReceivedPacket();

                        registeredUsers.remove(clientJsonContract.getFrom());
                        var userContract = new UserContract();
                        userContract.setErrorMessage("Sua sessão está expirada. Registre-se novamente!");
                        udpServer.sendData(Utils.toJson(userContract), sentAddress, sentPort);
                    } else {
                        System.out.println("Previous duration: " + user.getDuration().toSeconds());
                        user.setDuration(Duration.ofSeconds(20));
                    }
                } else {
                    if (clientJsonContract.getSubscribe()) {

                        InetAddress sentAddress = udpServer.getAddressFromLastReceivedPacket();
                        int sentPort = udpServer.getPortFromLastReceivedPacket();

                        final var registeredUser = subscribeUser(clientJsonContract);

                        udpServer.updateUserSession(registeredUser);
                        final var jsonResponse = Utils.toJson(new UserContract(registeredUser.getId()));

                        if (Objects.nonNull(jsonResponse)) {
                            udpServer.sendData(jsonResponse, sentAddress, sentPort);
                        }
                    }
                }
            }
        }
    }

    private RegisteredUser subscribeUser(ClientJsonContract clientJsonContract) {
        if (!registeredUsers.containsKey(clientJsonContract.getFrom())) {
            RegisteredUser registeredUser = new RegisteredUser();
            registeredUser.setId(UUID.randomUUID().toString());
            registeredUser.setAddress(udpServer.getAddressFromLastReceivedPacket());
            registeredUser.setPort(udpServer.getPortFromLastReceivedPacket());
            registeredUser.setNickName(clientJsonContract.getFrom());

            registeredUsers.put(registeredUser.getNickName(), registeredUser);
        }

        return registeredUsers.get(clientJsonContract.getFrom());
    }
}
