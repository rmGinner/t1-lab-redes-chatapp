package chat_impl;

import contracts.RequestControlContract;
import contracts.RequestDataContract;
import contracts.ResponseControlContract;
import contracts.ResponseDataContract;
import models.ControlReceiver;
import models.RegisteredUser;
import models.UdpDataReceiver;
import server.UdpServer;
import utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class ChatServerImplementation {
    private final UdpServer udpServer;
    private final Map<String, RegisteredUser> registeredUsers = new HashMap<>();

    public ChatServerImplementation(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    public void createChatCommunication() throws IOException {
        createDataReceiverListener();
        createControlReceiverListener();
    }

    private boolean isInSession(UdpDataReceiver udpDataReceiver, RegisteredUser user) {
        return user.getDuration().toSeconds() > 0 &
                udpDataReceiver.getAddress().equals(user.getAddress()) &&
                udpDataReceiver.getPort() == user.getPort();
    }

    private void sendMessageToDestinations(RegisteredUser user, RequestDataContract requestDataContract) {
        if (requestDataContract != null) {
            requestDataContract.getTo().forEach(dest -> {
                final var sanitizedDest = Utils.sanitizeNickName(dest);
                final var registeredUser = sanitizedDest != null ? registeredUsers.get(sanitizedDest) : null;

                if (registeredUser != null) {
                    try {
                        final var responseJsonContract = new ResponseDataContract(
                                user.getNickName(),
                                requestDataContract.getMessage()
                        );

                        udpServer.sendData(
                                Utils.toJson(responseJsonContract),
                                registeredUser.getAddress(),
                                registeredUser.getPort()
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private RegisteredUser getRegisteredUser(RequestDataContract requestDataContract) {
        return registeredUsers.get(requestDataContract.getFrom());
    }

    private void registerUser(String nickName, InetAddress address, int port) {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setId(UUID.randomUUID().toString());
        registeredUser.setNickName(nickName);
        registeredUser.setAddress(address);
        registeredUser.setPort(port);

        registeredUsers.put(registeredUser.getNickName(), registeredUser);
        udpServer.createUserSession(registeredUser);
    }

    private void createDataReceiverListener() {
        Runnable dataReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    UdpDataReceiver udpDataReceiver = udpServer.receiveData();
                    RequestDataContract requestDataContract = Utils.parseJson(udpDataReceiver.getData(), RequestDataContract.class);

                    if (Objects.nonNull(requestDataContract)) {
                        var user = getRegisteredUser(requestDataContract);

                        if (isInSession(udpDataReceiver, user)) {
                            sendMessageToDestinations(user, requestDataContract);
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };

        new Thread(dataReceiverTask).start();
    }

    private void createControlReceiverListener() {
        Runnable commandReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    ControlReceiver udpControlReceiver = udpServer.receiveControl();
                    RequestControlContract requestControlContract = Utils.parseJson(udpControlReceiver.getControl(), RequestControlContract.class);

                    if (Objects.nonNull(requestControlContract) &&
                            Objects.nonNull(requestControlContract.getControl()) &&
                            !requestControlContract.getControl().isBlank()
                    ) {

                        final var sentAddress = udpControlReceiver.getAddress();
                        final var sentPort = udpControlReceiver.getPort();

                        String jsonResponse = "{}";

                        try {
                            executeControlBy(requestControlContract, udpControlReceiver);

                            jsonResponse = Utils.toJson(new ResponseControlContract("Usuário criado", true));
                        } catch (IllegalArgumentException illException) {
                            jsonResponse = Utils.toJson(new ResponseControlContract(illException.getMessage(), false));
                        }

                        udpServer.sendData(jsonResponse, sentAddress, sentPort);
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };

        new Thread(commandReceiverTask).start();
    }

    private void executeControlBy(RequestControlContract requestControlContract, ControlReceiver controlReceiver) throws IllegalArgumentException {
        requestControlContract.setControl(requestControlContract.getControl().trim());

        switch (requestControlContract.getControl().toUpperCase()) {
            case "REGISTER_USER" -> registerUser(requestControlContract.getControlArgument(), controlReceiver.getAddress(), controlReceiver.getPort());
            case "KEEP_ALIVE" -> keepAlive(requestControlContract.getControlArgument());
            default -> throw new IllegalArgumentException("Controle inválido");
        }
    }

    private void keepAlive(String nickName) {
        if (Objects.nonNull(this.registeredUsers.get(nickName))) {
            this.registeredUsers.get(nickName).setDuration(Duration.ofSeconds(20));
        }
    }

}
