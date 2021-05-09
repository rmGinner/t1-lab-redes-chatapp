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

    private Timer timer = new Timer();

    public ChatServerImplementation(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    public void createChatCommunication() throws IOException {
        createDataReceiverListener();
        createControlReceiverListener();
    }

    private boolean isInSession(UdpDataReceiver udpDataReceiver, RegisteredUser user) {
        return Objects.nonNull(user) && user.getDuration().toSeconds() > 0 &
                udpDataReceiver.getAddress().equals(user.getAddress()) &&
                udpDataReceiver.getPort() == user.getPort();
    }

    private void sendMessageToDestinations(RegisteredUser user, RequestDataContract requestDataContract) throws IOException {
        if (requestDataContract != null) {
            final var responseJsonContract = new ResponseDataContract(
                    user.getNickName(),
                    requestDataContract.getMessage(),
                    requestDataContract.getTo()
            );

            udpServer.responseControlRequestBroadcast(Utils.toJson(responseJsonContract));
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
        createUserSession(registeredUser);

        var jsonResponse = Utils.toJson(new ResponseControlContract("Usuário criado", true));

        udpServer.responseControlRequestUnicast(jsonResponse, address, port);

        try {
            udpServer.responseControlRequestBroadcast(
                    Utils.toJson(
                            new ResponseControlContract("O usuário " + nickName + " entrou na sala.", false)
                    )
            );
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
                    RequestControlContract requestControlContract = Utils.parseJson(udpControlReceiver.getControl(), RequestControlContract.class);

                    if (Objects.nonNull(requestControlContract) &&
                            Objects.nonNull(requestControlContract.getControl()) &&
                            !requestControlContract.getControl().isBlank()
                    ) {
                        try {
                            executeControlBy(requestControlContract, udpControlReceiver);
                        } catch (IllegalArgumentException illException) {
                            final var jsonResponse = Utils.toJson(new ResponseControlContract(illException.getMessage(), false));
                            udpServer.responseControlRequestUnicast(jsonResponse, udpControlReceiver.getAddress(), udpControlReceiver.getPort());
                        } catch (IOException e) {
                            final var jsonResponse = Utils.toJson(new ResponseControlContract("Ocorreu um erro inesperado. Tente novamente", false));
                            udpServer.responseControlRequestUnicast(jsonResponse, udpControlReceiver.getAddress(), udpControlReceiver.getPort());
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
                    udpServer.responseControlRequestUnicast(Utils.toJson(new ResponseControlContract("Usuário já está cadastrado", true)), controlReceiver.getAddress(), controlReceiver.getPort());
                } else {
                    registeredUsers.remove(requestControlContract.getControlArgument());
                    udpServer.responseControlRequestUnicast(Utils.toJson(new ResponseControlContract("Seu tempo de sessão esgotou. Realize novamente o login.", false)), controlReceiver.getAddress(), controlReceiver.getPort());
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

    private void createUserSession(RegisteredUser user) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (user.getDuration().toSeconds() > 0) {
                    user.setDuration(Duration.ofSeconds(user.getDuration().toSeconds() - 1));
                } else {
                    try {
                        udpServer.responseControlRequestBroadcast(
                                Utils.toJson(
                                        new ResponseControlContract("O usuário " + user.getNickName() + " foi desconectado da sala.", false)
                                )
                        );
                    } catch (IOException ioException) {
                        System.out.println("Erro genérico ao enviar dados para o cliente.");
                    }

                    registeredUsers.remove(user.getNickName());

                    this.cancel();
                }
            }
        }, 0, 1000);
    }

}
