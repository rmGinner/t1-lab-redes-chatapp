import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import contracts.ClientJsonContract;
import contracts.UserContract;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();

        final var jsonContract = new ClientJsonContract();

        jsonContract.setFrom("rodrigo");
        jsonContract.setMessage("Test message");
        jsonContract.setSubscribe(true);

        String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(jsonContract);
        byte[] sendData = jsonAsString.getBytes();
        System.out.println(sendData.length);

        datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), 4390));

        while (true) {
            byte[] receiveData = new byte[50000];
            final var packet = new DatagramPacket(receiveData, receiveData.length);
            datagramSocket.receive(packet);

            System.out.println(parseJson(new String(packet.getData())));
        }
    }

    private static UserContract parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(json);
        return objectMapper.readValue(json, UserContract.class);
    }
}
