package communication_modules;

import com.google.gson.GsonBuilder;
import contracts.RequestControlContract;
import contracts.RequestDataContract;
import contracts.ResponseControlContract;
import contracts.ResponseDataContract;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class ServerComModule {

    private final DatagramSocket datagramSocket;

    private final MulticastSocket multicastSocket;

    private static final String MULTICAST_ADDRESS = "230.0.0.0";

    private static final Integer MULTICAST_PORT = 5555;

    private static final String REGISTER_USER_CONTROL = "REGISTER_USER";

    private static final String KEEP_ALIVE_CONTROL = "KEEP_ALIVE";

    private static final String EXIT_USER_CONTROL = "EXIT_USER";

    private static final int DATA_CHANNEL_PORT = 4390;

    private static final int CONTROL_CHANNEL_PORT = 4391;

    private Timer timer;

    private String nickName;

    //Cria um socket UDP e UDP multicast
    public ServerComModule() throws IOException {
        this.datagramSocket = new DatagramSocket();
        this.multicastSocket = new MulticastSocket(MULTICAST_PORT);
        this.multicastSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));

        createControlListener();
        createBroadcastMessageListener();
    }

    //Registra um usuário no chat
    public void registerUser(String nickName) throws IOException {
        final var controlContract = new RequestControlContract();

        controlContract.setControl(REGISTER_USER_CONTROL);
        controlContract.setControlArgument(nickName);

        this.nickName = nickName;

        sendControlToServer(controlContract);
    }

    //Verifica se o usuário já registrou um nickname no chat
    public boolean hasUser() {
        return Objects.nonNull(nickName);
    }

    //Envia um controle para o servidor
    private void sendControlToServer(RequestControlContract requestControlContract) throws IOException {
        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(requestControlContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), CONTROL_CHANNEL_PORT));
    }

    //Envia uma mensagem para o servidor
    public void sendMessage(String message, String toUser) throws IOException {
        var dataContract = new RequestDataContract(nickName, message, toUser);

        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(dataContract);
        byte[] sendData = jsonAsString.getBytes();

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), DATA_CHANNEL_PORT));
    }

    //Remove um usuário do chat
    public void exitUser() throws IOException {
        final var requestControlContract = new RequestControlContract(EXIT_USER_CONTROL, this.nickName);
        this.sendControlToServer(requestControlContract);

        this.nickName = null;
    }

    public String getUser() {
        return this.nickName;
    }

    //Cria uma nova thread que somente receberá os controles enviados pelo servidor ao cliente em unicast.
    private void createControlListener() {
        Runnable runnable = () -> {
            while (true) {
                try {
                    String receivedJson = receiveControlFromServer();
                    ResponseControlContract responseControlContract = Utils.parseJson(receivedJson, ResponseControlContract.class);

                    if (Objects.nonNull(responseControlContract) && Objects.nonNull(responseControlContract.getMessage())) {
                        if (responseControlContract.isSuccess()) {
                            keepAlive();
                            System.out.println(responseControlContract.getMessage());
                        } else {
                            nickName = null;

                            if (Objects.nonNull(timer)) {
                                timer.cancel();
                            }
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        };

        new

                Thread(runnable).

                start();

    }

    //Cria uma nova thread que somente receberá as mensagens recebidas em broadcast do servidor e mostrará ao usuário.
    private void createBroadcastMessageListener() {
        Runnable runnable = () -> {
            while (true) {
                try {
                    String receivedJson = receiveBroadcastMessageFromServer();

                    ResponseDataContract responseDataContract = Utils.parseJson(receivedJson, ResponseDataContract.class);

                    if (Objects.nonNull(responseDataContract)) {
                        System.out.println(responseDataContract.getMessage());
                    } else {
                        ResponseControlContract responseControlContract = Utils.parseJson(receivedJson, ResponseControlContract.class);

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

    //Recebe uma resposta de controle do servidor
    private String receiveControlFromServer() throws IOException {
        byte[] receiveData = new byte[500000];
        final var packet = new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(packet);
        return new String(packet.getData());
    }

    //Recebe uma mensagem broadcast do servidor
    private String receiveBroadcastMessageFromServer() throws IOException {
        byte[] receiveData = new byte[500000];
        final var packet = new DatagramPacket(receiveData, receiveData.length);
        multicastSocket.receive(packet);
        return new String(packet.getData());
    }

    //Cria uma nova thread para ficar enviando keep alive ao servidor.
    private void keepAlive() {
        Runnable runnable = () -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        sendControlToServer(new RequestControlContract(KEEP_ALIVE_CONTROL, nickName));
                    } catch (IOException ioException) {
                        System.out.println("Keep alive --- Erro na comunicação com o servidor!!");
                    }
                }
            }, 0, 1000);
        };

        new Thread(runnable).start();
    }
}
