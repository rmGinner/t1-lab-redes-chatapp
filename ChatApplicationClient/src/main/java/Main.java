import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.*;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    private static String nickName;

    private static int DATA_CHANNEL_PORT = 4390;

    private static int CONTROL_CHANNEL_PORT = 4391;

    private static String REGISTER_USER_CONTROL = "REGISTER_USER";

    private static String KEEP_ALIVE_CONTROL = "KEEP_ALIVE";

    private static Timer timer = new Timer();

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        createMessageListener(datagramSocket);
        createControlListener(datagramSocket);

        while (true) {
            System.out.println("Operações: ");
            System.out.println("Entrar no chat: ENTER");
            System.out.println("Sair do chat: EXIT");
            System.out.println("Enviar mensagem: SEND_MESSAGE");

            final var sc = new Scanner(System.in);
            var request = sc.nextLine();

            if (Objects.isNull(request)) {
                System.out.println("Operação inválida!");

                return;
            }

            request = request.toLowerCase().trim();

            final var controlContract = new RequestControlContract();

            if (request.equals("enter")) {
                System.out.println("Informe o seu nome de usuário:");
                String newUser = sc.nextLine();

                if (newUser.trim().equalsIgnoreCase(nickName)) {
                    System.out.println("Usuário já cadastrado.");

                    continue;
                }

                nickName = newUser;

                controlContract.setControl(REGISTER_USER_CONTROL);
                controlContract.setControlArgument(nickName);

                sendControlToServer(datagramSocket, controlContract);
            } else if (request.equals("send_message")) {
                if (nickName == null) {
                    System.out.println("É necessário entrar no chat primeiro");
                    return;
                }

                System.out.println("Digite a mensagem: ");
                var message = sc.nextLine();


                System.out.println("Informe os destinatários, separados por ',' : ");
                var destinations = sc.nextLine();
                List<String> destinationsList = destinations.contains(",") ? Arrays.asList(destinations.split(",")) : List.of(destinations);

                var dataContract = new RequestDataContract(nickName, message, destinationsList);

                sendDataToServer(datagramSocket, dataContract);
            } else {
                System.out.println("Opção inválida.");
                return;
            }


        }
    }

    private static <T> T parseJson(String json, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void sendControlToServer(DatagramSocket datagramSocket, RequestControlContract requestControlContract) throws IOException {
        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(requestControlContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), CONTROL_CHANNEL_PORT));
    }

    private static void sendDataToServer(DatagramSocket datagramSocket, RequestDataContract requestDataContract) throws IOException {
        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(requestDataContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.setBroadcast(true);
        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getByName("191.255.255.255"), DATA_CHANNEL_PORT));
    }

    private static <T> T receiveMessageFromServer(DatagramSocket datagramSocket, Class<T> clazz) throws IOException {
        byte[] receiveData = new byte[500000];
        final var packet = new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(packet);

        return parseJson(new String(packet.getData()), clazz);
    }

    private static void createMessageListener(DatagramSocket datagramSocket) {
        Runnable runnable = () -> {
            while (true) {

                ResponseDataContract responseDataContract = null;
                try {
                    responseDataContract = receiveMessageFromServer(datagramSocket, ResponseDataContract.class);
                    if (Objects.nonNull(responseDataContract) && Objects.nonNull(responseDataContract.getTo())) {
                        if (responseDataContract.getTo().equalsIgnoreCase(nickName)) {
                            System.out.println("Você recebeu uma mensagem!!");
                            System.out.println("De:" + responseDataContract.getFrom());
                            System.out.println("Mensagem: " + responseDataContract.getMessage());
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }

    private static void createControlListener(DatagramSocket datagramSocket) {
        Runnable runnable = () -> {
            while (true) {

                ResponseControlContract responseControlContract = null;
                try {
                    responseControlContract = receiveMessageFromServer(datagramSocket, ResponseControlContract.class);

                    if (Objects.nonNull(responseControlContract) && Objects.nonNull(responseControlContract.getMessage())) {
                        System.out.println(responseControlContract.getMessage());
                        System.out.println(responseControlContract.isSuccess());

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


    private static void keepAlive(DatagramSocket datagramSocket, String nickName) {
        Runnable runnable = () -> {
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
