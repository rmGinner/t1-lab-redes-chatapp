import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.gson.GsonBuilder;
import contracts.ClientJsonContract;
import contracts.ResponseJsonContract;
import contracts.UserContract;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    private static String nickName;

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();

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

            final var jsonContract = new ClientJsonContract();

            if (request.equals("enter")) {
                System.out.println("Informe o seu nome de usuário:");
                nickName = sc.nextLine();

                jsonContract.setFrom(nickName);
                jsonContract.subscribe();
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

                jsonContract.setFrom(nickName);
                jsonContract.setMessage(message);
                jsonContract.setTo(destinationsList);
            } else {
                System.out.println("Opção inválida.");
                return;
            }

            String jsonAsString = new GsonBuilder().setPrettyPrinting().setLenient().create().toJson(jsonContract);
            byte[] sendData = jsonAsString.getBytes();
            System.out.println(sendData.length);

            datagramSocket.send(new DatagramPacket(sendData, sendData.length, InetAddress.getLoopbackAddress(), 4390));

            byte[] receiveData = new byte[500000];
            final var packet = new DatagramPacket(receiveData, receiveData.length);
            datagramSocket.receive(packet);

            System.out.println(parseJson(new String(packet.getData())));
        }
    }

    private static Object parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(json);
        try {
            return objectMapper.readValue(json, UserContract.class);
        } catch (UnrecognizedPropertyException e) {
            try {
                return objectMapper.readValue(json, ResponseJsonContract.class);
            } catch (JsonProcessingException e2) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
