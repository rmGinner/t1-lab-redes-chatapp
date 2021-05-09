import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import contracts.RequestControlContract;
import contracts.RequestDataContract;
import contracts.ResponseControlContract;
import contracts.ResponseDataContract;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    private static String nickName;

    private static final int DATA_CHANNEL_PORT = 4390;

    private static final int CONTROL_CHANNEL_PORT = 4391;

    private static final String REGISTER_USER_CONTROL = "REGISTER_USER";

    private static final String KEEP_ALIVE_CONTROL = "KEEP_ALIVE";

    private static final String USER_CMD_PRIVATE_MESSAGE = "/PRIVMSG";

    private static final String USER_CMD_MESSAGE_FOR_ALL = "/MSG";

    private static final String USER_CMD_ENTER_IN_CHAT = "/ENTER";

    private static final String USER_CMD_EXIT_CHAT = "/EXIT";

    private static Timer timer;

    private static DatagramSocket datagramSocket;

    private static MulticastSocket multicastSocket;

    private static final String MULTICAST_ADDRESS = "230.0.0.0";

    private static final Integer MULTICAST_PORT = 5555;

    public static void main(String[] args) throws IOException {
        datagramSocket = new DatagramSocket();
        multicastSocket = new MulticastSocket(MULTICAST_PORT);
        multicastSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));

        createMessageListener();
        createBroadcastMessageListener();

        while (true) {
            System.out.println("Operações: \n\n");
            System.out.printf("Entrar no chat: %s {nome do usuário} \n", USER_CMD_ENTER_IN_CHAT);
            System.out.printf("Sair do chat: %s\n", USER_CMD_EXIT_CHAT);
            System.out.printf("Enviar mensagem para um usuário: %s {usuário} {mensagem} \n", USER_CMD_PRIVATE_MESSAGE);
            System.out.printf("Enviar mensagem para todos os usuários da sala: %s {mensagem} \n\n\n", USER_CMD_MESSAGE_FOR_ALL);

            final var sc = new Scanner(System.in);
            var request = sc.nextLine();

            if (Objects.isNull(request)) {
                System.out.println("Operação inválida!");

                return;
            }

            request = request.toUpperCase().trim();

            final var controlContract = new RequestControlContract();

            if (request.startsWith(USER_CMD_ENTER_IN_CHAT)) {
                var commandArguments = request.split(" ");

                if (commandArguments.length != 2) {
                    System.out.println("Parâmetros inválidos para o comando");
                    continue;
                }

                String newUser = commandArguments[1];

                if (newUser.trim().equalsIgnoreCase(nickName)) {
                    System.out.println("Usuário já cadastrado.");

                    continue;
                }

                nickName = newUser;

                controlContract.setControl(REGISTER_USER_CONTROL);
                controlContract.setControlArgument(nickName);

                sendControlToServer(datagramSocket, controlContract);
            } else if (request.startsWith(USER_CMD_MESSAGE_FOR_ALL)) {
                if (nickName == null) {
                    System.out.println("É necessário entrar no chat primeiro");
                    continue;
                }

                final var commandParams = request.split(" ");

                if (commandParams.length != 2) {
                    System.out.println("Parâmetros inválidos para o comando");
                    continue;
                }

                var message = commandParams[1];

                var dataContract = new RequestDataContract(nickName, message);

                sendDataToServer(dataContract);
            } else if (request.startsWith(USER_CMD_PRIVATE_MESSAGE)) {
                if (nickName == null) {
                    System.out.println("É necessário entrar no chat primeiro");
                    continue;
                }

                var commandParams = request.split(" ");

                if (commandParams.length != 3) {
                    System.out.println("Parâmetros inválidos para o comando.");
                    continue;
                }

                var user = commandParams[1];
                var message = commandParams[2];

                var dataContract = new RequestDataContract(nickName, message, user);

                sendDataToServer(dataContract);
            } else {
                System.out.println("Opção inválida.");
                continue;
            }


        }
    }

    private static <T> T parseJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonMappingException | JsonParseException e) {
            return null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static void sendControlToServer(DatagramSocket datagramSocket, RequestControlContract requestControlContract) throws IOException {
        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(requestControlContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), CONTROL_CHANNEL_PORT));
    }

    private static void sendDataToServer(RequestDataContract requestDataContract) throws IOException {
        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(requestDataContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), DATA_CHANNEL_PORT));
    }

    private static String receiveMessageFromServer() throws IOException {
        byte[] receiveData = new byte[500000];
        final var packet = new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(packet);
        return new String(packet.getData());
    }

    private static String receiveBroadcastMessageFromServer() throws IOException {
        byte[] receiveData = new byte[500000];
        final var packet = new DatagramPacket(receiveData, receiveData.length);
        multicastSocket.receive(packet);
        return new String(packet.getData());
    }

    private static void createMessageListener() {
        Runnable runnable = () -> {
            while (true) {

                try {
                    String receivedJson = receiveMessageFromServer();
                    ResponseControlContract responseControlContract = parseJson(receivedJson, ResponseControlContract.class);

                    if (Objects.nonNull(responseControlContract) && Objects.nonNull(responseControlContract.getMessage())) {
                        if (responseControlContract.isSuccess()) {
                            keepAlive(datagramSocket, nickName);
                            System.out.println(responseControlContract.getMessage());
                        } else {
                            nickName = null;
                            timer.cancel();
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

    private static void createBroadcastMessageListener() {
        Runnable runnable = () -> {
            while (true) {
                try {
                    String receivedJson = receiveBroadcastMessageFromServer();

                    ResponseDataContract responseDataContract = parseJson(receivedJson, ResponseDataContract.class);
                    var builder = "teste";

                    Path destinationFile = Paths.get("C:/Users/10086628/Desktop", "teste.txt");
                    Files.write(destinationFile, builder.getBytes());

                    if (Objects.nonNull(responseDataContract)) {
                        if (Objects.nonNull(responseDataContract.getTo())) {
                            System.out.printf("\n\n %s escreveu para %s: %s \n\n", responseDataContract.getFrom(), responseDataContract.getTo(), responseDataContract.getMessage());
                        } else {
                            System.out.printf("\n\n %s escreveu para TODOS: %s \n\n", responseDataContract.getFrom(), responseDataContract.getMessage());
                        }
                    } else {
                        ResponseControlContract responseControlContract = parseJson(receivedJson, ResponseControlContract.class);

                        if (Objects.nonNull(responseControlContract) && Objects.nonNull(responseControlContract.getMessage())) {
                            System.out.println(responseControlContract.getMessage());
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

    private static void keepAlive(DatagramSocket datagramSocket, String nickName) {
        Runnable runnable = () -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendControlToServer(datagramSocket, new RequestControlContract(KEEP_ALIVE_CONTROL, nickName));
                    } catch (IOException ioException) {
                        System.out.println("Keep alive --- Erro na comunicação com o servidor!!");
                    }
                }
            }, 0, 1000);
        };

        new Thread(runnable).start();
    }
}
