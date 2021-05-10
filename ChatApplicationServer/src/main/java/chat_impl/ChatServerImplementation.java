package chat_impl;

import contracts.RequestControlContract;
import contracts.RequestDataContract;
import contracts.ResponseControlContract;
import contracts.ResponseDataContract;
import models.ControlReceiver;
import models.MessageReceiver;
import models.RegisteredUser;
import server.UdpServer;
import utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;

/**
 * Implementação concreta do chat.
 */
public class ChatServerImplementation {
    private final UdpServer udpServer;
    private final Map<String, RegisteredUser> registeredUsers = new HashMap<>();

    public ChatServerImplementation(UdpServer udpServer) {
        this.udpServer = udpServer;
    }

    //Cria os listeners para os dados e controles enviados do cliente.
    public void createChatCommunication() throws IOException {
        createMessageReceiverListener();
        createControlReceiverListener();
    }

    //Verifica se um determinado usuário está cadastrado no chat
    private boolean isInSession(MessageReceiver messageReceiver, RegisteredUser user) {
        return Objects.nonNull(user) && user.getDuration().toSeconds() > 0 &
                messageReceiver.getAddress().equals(user.getAddress()) &&
                messageReceiver.getPort() == user.getPort();
    }

    //Envia uma mensagem em broadcast para os destinos
    private void sendMessageToDestinations(RegisteredUser user, RequestDataContract requestDataContract) throws IOException {
        if (requestDataContract != null) {
            final var responseJsonContract = new ResponseDataContract(
                    user.getNickName(),
                    requestDataContract.getMessage(),
                    requestDataContract.getTo()
            );

            udpServer.responseMessageControlRequestBroadcast(Utils.toJson(responseJsonContract));
        }

    }

    //Busca um usuário cadastrado
    private RegisteredUser getRegisteredUser(String nickName) {
        return registeredUsers.get(nickName);
    }

    //Cadastra um usuário no chat
    private void registerUser(String nickName, InetAddress address, int port) throws IOException {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setNickName(nickName);
        registeredUser.setAddress(address);
        registeredUser.setPort(port);

        registeredUsers.put(registeredUser.getNickName(), registeredUser);
        createUserSession(registeredUser);

        var jsonResponse = Utils.toJson(new ResponseControlContract("Usuário criado", true));

        udpServer.responseControlRequestUnicast(jsonResponse, address, port);

        try {
            udpServer.responseMessageControlRequestBroadcast(
                    Utils.toJson(
                            new ResponseControlContract("O usuário " + nickName + " entrou na sala.", false)
                    )
            );
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //Cria uma nova thread para ficar recebendo as mensagens enviadas pelo cliente e para repassar elas aos destinatários em broadcast.
    private void createMessageReceiverListener() {
        Runnable dataReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    MessageReceiver messageReceiver = udpServer.receiveData();
                    RequestDataContract requestDataContract = Utils.parseJson(messageReceiver.getData(), RequestDataContract.class);

                    if (Objects.nonNull(requestDataContract)) {
                        var user = getRegisteredUser(requestDataContract.getFrom());

                        if (isInSession(messageReceiver, user)) {
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

    //Cria uma nova thread para ficar recebendo os controles enviados pelo cliente.
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

    //Executa um controle de acordo com a solicitação do cliente.
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
            case "EXIT_USER" -> exitUser(requestControlContract.getControlArgument());
            default -> throw new IllegalArgumentException("Controle inválido");
        }
    }

    private void exitUser(String controlArgument) throws IOException {
        this.registeredUsers.remove(controlArgument);
        udpServer.responseMessageControlRequestBroadcast(
                Utils.toJson(
                        new ResponseControlContract("O usuário " + controlArgument + " foi desconectado da sala.", false)
                )
        );
    }

    private void keepAlive(String nickName) {
        final var registeredUser = this.registeredUsers.get(nickName);
        if (Objects.nonNull(registeredUser)) {
            this.registeredUsers.get(nickName).setDuration(Duration.ofSeconds(20));
        }
    }

    private void createUserSession(RegisteredUser user) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (user.getDuration().toSeconds() > 0) {
                    user.setDuration(Duration.ofSeconds(user.getDuration().toSeconds() - 1));
                } else {
                    try {
                        udpServer.responseMessageControlRequestBroadcast(
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
