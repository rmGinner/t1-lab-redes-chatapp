package chat_impl;

import contracts.CommandContract;
import contracts.DataContract;
import contracts.ResponseJsonContract;
import models.CommandReceiver;
import models.RegisteredUser;
import models.UdpDataReceiver;
import server.UdpServer;
import utils.Utils;

import java.io.IOException;
import java.net.InetAddress;
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
        createCommandReceiverListener();
    }

//    private void processCommandWhenUserExists(UdpDataReceiver udpDataReceiver, DataContract dataContract, RegisteredUser user) throws IOException {
//        if (!isInSession(udpDataReceiver, user)) {
//            processWhenIsNotInSession(udpDataReceiver, dataContract);
//        } else {
//            processWhenIsInSession(user, dataContract);
//        }
//    }


    private boolean isInSession(UdpDataReceiver udpDataReceiver, RegisteredUser user) {
        return user.getDuration().toSeconds() > 0 &
                udpDataReceiver.getAddress().equals(user.getAddress()) &&
                udpDataReceiver.getPort() == user.getPort();
    }

//    private void processWhenIsNotInSession(UdpDataReceiver udpDataReceiver, DataContract dataContract) throws IOException {
//        InetAddress sentAddress = udpDataReceiver.getAddress();
//        int sentPort = udpDataReceiver.getPort();
//
//        registeredUsers.remove(dataContract.getFrom());
//        var userContract = new UserContract();
//        userContract.setErrorMessage("Sua sessão está expirada. Registre-se novamente!");
//        udpServer.sendData(Utils.toJson(userContract), sentAddress, sentPort);
//    }

    private void sendMessageToDestinations(RegisteredUser user, DataContract dataContract) {
        if (dataContract != null) {
            dataContract.getTo().forEach(dest -> {
                final var sanitizedDest = Utils.sanitizeNickName(dest);
                final var registeredUser = registeredUsers.get(sanitizedDest);

                if (sanitizedDest != null && registeredUser != null) {
                    try {
                        final var responseJsonContract = new ResponseJsonContract(
                                user.getNickName(),
                                dataContract.getMessage()
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

    private RegisteredUser getRegisteredUser(DataContract dataContract) {
        return registeredUsers.get(dataContract.getFrom());
    }

    private void registerUser(DataContract dataContract, InetAddress address, int port) {
        RegisteredUser registeredUser = new RegisteredUser();
        registeredUser.setId(UUID.randomUUID().toString());
        registeredUser.setNickName(dataContract.getFrom());
        registeredUser.setAddress(address);
        registeredUser.setPort(port);

        registeredUsers.put(registeredUser.getNickName(), registeredUser);
    }

    private void createDataReceiverListener() {
        Runnable dataReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    UdpDataReceiver udpDataReceiver = udpServer.receiveData();
                    DataContract dataContract = Utils.parseJson(udpDataReceiver.getData(), DataContract.class);

                    if (Objects.nonNull(dataContract)) {
                        var user = getRegisteredUser(dataContract);

                        if (isInSession(udpDataReceiver, user)) {
                            sendMessageToDestinations(user, dataContract);
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };

        new Thread(dataReceiverTask).start();
    }

    private void createCommandReceiverListener() {
        Runnable commandReceiverTask = () -> {
            try {
                while (this.udpServer.isOpened()) {
                    CommandReceiver udpCommandReceiver = udpServer.receiveCommand();
                    CommandContract commandContract = Utils.parseJson(udpCommandReceiver.getCommand(), CommandContract.class);

                    if (Objects.nonNull(commandContract) &&
                            Objects.nonNull(commandContract.getCommand()) &&
                            !commandContract.getCommand().isBlank()
                    ) {





                        InetAddress sentAddress = udpCommandReceiver.getAddress();
                        int sentPort = udpCommandReceiver.getPort();

                        final var registeredUser = getRegisteredUser(commandContract);

                        udpServer.updateUserSession(registeredUser);
                        final var jsonResponse = Utils.toJson(new UserContract(registeredUser.getId()));

                        if (Objects.nonNull(jsonResponse)) {
                            udpServer.sendData(jsonResponse, sentAddress, sentPort);
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        };

        new Thread(commandReceiverTask).start();
    }
}
