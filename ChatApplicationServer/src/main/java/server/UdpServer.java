package server;

import models.ControlReceiver;
import models.MessageReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class UdpServer {

    private DatagramSocket dataChannel;

    private DatagramSocket controlChannel;

    private static final Integer DATA_CHANNEL_PORT = 4390;

    private static final Integer CONTROL_CHANNEL_PORT = 4391;

    private static final String MULTICAST_ADDRESS = "230.0.0.0";

    private static final Integer MULTICAST_PORT = 5555;

    private Timer timer = new Timer();

    public UdpServer() {
    }

    //Inicializa o servidor com as portas para dados e controles
    public void start() throws IOException {
        this.dataChannel = new DatagramSocket(DATA_CHANNEL_PORT);
        this.controlChannel = new DatagramSocket(CONTROL_CHANNEL_PORT);
    }

    //Verifica se o servidor está inicializado.
    public boolean isOpened() {
        return !this.dataChannel.isClosed();
    }

    //Recebe uma mensagem enviada pelo cliente.
    public MessageReceiver receiveData() throws IOException {
        byte[] receiveData = new byte[50000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, InetAddress.getLoopbackAddress(  ), DATA_CHANNEL_PORT);
        dataChannel.receive(receivePacket);

        return new MessageReceiver(
                receivePacket.getAddress(),
                receivePacket.getPort(),
                new String(receivePacket.getData())
        );
    }

    //Recebe um controle enviado pelo cliente.
    public ControlReceiver receiveControl() throws IOException {
        byte[] receiveData = new byte[50000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, InetAddress.getLoopbackAddress(), CONTROL_CHANNEL_PORT);
        controlChannel.receive(receivePacket);

        return new ControlReceiver(
                receivePacket.getAddress(),
                receivePacket.getPort(),
                new String(receivePacket.getData())
        );
    }

    //Responde à uma solicitação de controle enviado pelo cliente
    public void responseControlRequestUnicast(String responseText, InetAddress address, int port) throws IOException {
        byte[] sendData = responseText.getBytes();
        controlChannel.send(new DatagramPacket(sendData, sendData.length, address, port));
    }

    //Responde à uma solicitação de controle em broadcast enviado pelo cliente
    public void responseMessageControlRequestBroadcast(String responseText) throws IOException {
        byte[] sendData = responseText.getBytes();
        controlChannel.send(new DatagramPacket(sendData, sendData.length, InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT));
    }
}
