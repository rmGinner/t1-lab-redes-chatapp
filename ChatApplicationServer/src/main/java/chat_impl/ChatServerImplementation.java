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
import java.util.*;


public class ChatServerImplementation {
    private final UdpServer udpServer;
    private final Map<String, RegisteredUser> registeredUsers = new HashMap<>();

    public ChatServerImplementation(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    private Timer timer = new Timer();

    public void createChatCommunication() throws IOException {
        createDataReceiverListener();
        createControlReceiverListener();
    }

    private boolean isInSession(UdpDataReceiver udpDataReceiver, RegisteredUser user) {
        return Objects.nonNull(user) && user.getDuration().toSeconds() > 0 &
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
                                requestDataContract.getMessage(),
                                dest
                        );

                        udpServer.sendBroadCastData(
                                Utils.toJson(responseJsonContract)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private RegisteredUser getRegisteredUser(String nickName) {
        return registeredUsers.get(nickName);
    }

    private void registerUser(String nickName, InetAddress address, int port) throws IOException {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setId(UUID.randomUUID().toString());
        registeredUser.setNickName(nickName);
        registeredUser.setAddress(address);
        registeredUser.setPort(port);

        registeredUsers.put(registeredUser.getNickName(), registeredUser);
        udpServer.createUserSession(registeredUser);

        var jsonResponse = Utils.toJson(new ResponseControlContract("Usuário criado", true));

        udpServer.responseControlRequest(jsonResponse, address, port);
    }

    private void createDataReceiverListener() {
        Runnable dataReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    UdpDataReceiver udpDataReceiver = udpServer.receiveData();
                    RequestDataContract requestDataContract = Utils.parseJson(udpDataReceiver.getData(), RequestDataContract.class);

                    if (Objects.nonNull(requestDataContract)) {
                        var user = getRegisteredUser(requestDataContract.getFrom());

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
                    System.out.println(udpControlReceiver.getControl());
                    RequestControlContract requestControlContract = Utils.parseJson(udpControlReceiver.getControl(), RequestControlContract.class);

                    if (Objects.nonNull(requestControlContract) &&
                            Objects.nonNull(requestControlContract.getControl()) &&
                            !requestControlContract.getControl().isBlank()
                    ) {
                        try {
                            executeControlBy(requestControlContract, udpControlReceiver);
                        } catch (IllegalArgumentException illException) {
                            final var jsonResponse = Utils.toJson(new ResponseControlContract(illException.getMessage(), false));
                            udpServer.responseControlRequest(jsonResponse, udpControlReceiver.getAddress(), udpControlReceiver.getPort());
                        } catch (IOException e) {
                            final var jsonResponse = Utils.toJson(new ResponseControlContract("Ocorreu um erro inesperado. Tente novamente", false));
                            udpServer.responseControlRequest(jsonResponse, udpControlReceiver.getAddress(), udpControlReceiver.getPort());
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };

        new Thread(commandReceiverTask).start();
    }

    private void executeControlBy(RequestControlContract requestControlContract, ControlReceiver controlReceiver) throws IllegalArgumentException, IOException {
        requestControlContract.setControl(requestControlContract.getControl().trim());

        switch (requestControlContract.getControl().toUpperCase()) {
            case "REGISTER_USER" -> {
                if (!registeredUsers.containsKey(requestControlContract.getControlArgument())) {
                    registerUser(requestControlContract.getControlArgument(), controlReceiver.getAddress(), controlReceiver.getPort());
                } else if (registeredUsers.get(requestControlContract.getControlArgument()).getDuration().toSeconds() > 0) {
                    udpServer.responseControlRequest(Utils.toJson(new ResponseControlContract("Usuário já está cadastrado", true)), controlReceiver.getAddress(), controlReceiver.getPort());
                } else {
                    registeredUsers.remove(requestControlContract.getControlArgument());
                    udpServer.responseControlRequest(Utils.toJson(new ResponseControlContract("Seu tempo de sessão esgotou. Realize novamente o login.", false)), controlReceiver.getAddress(), controlReceiver.getPort());
                }
            }
            case "KEEP_ALIVE" -> keepAlive(requestControlContract.getControlArgument());
            default -> throw new IllegalArgumentException("Controle inválido");
        }
    }

    private void keepAlive(String nickName) {
        final var registeredUser = this.registeredUsers.get(nickName);
        if (Objects.nonNull(registeredUser)) {
            this.registeredUsers.get(nickName).setDuration(Duration.ofSeconds(20));
        }
    }

}
