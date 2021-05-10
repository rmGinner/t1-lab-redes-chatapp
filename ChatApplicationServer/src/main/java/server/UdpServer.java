package server;

import models.ControlReceiver;
import models.UdpDataReceiver;

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

    public void start() throws IOException {
        this.dataChannel = new DatagramSocket(DATA_CHANNEL_PORT);
        this.controlChannel = new DatagramSocket(CONTROL_CHANNEL_PORT);
    }

    public boolean isOpened() {
        return !this.dataChannel.isClosed();
    }

    public UdpDataReceiver receiveData() throws IOException {
        byte[] receiveData = new byte[50000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, InetAddress.getLoopbackAddress(  ), DATA_CHANNEL_PORT);
        dataChannel.receive(receivePacket);

        return new UdpDataReceiver(
                receivePacket.getAddress(),
                receivePacket.getPort(),
                new String(receivePacket.getData())
        );
    }

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

    public void sendUnicastData(String data, InetAddress address, int port) throws IOException {
        byte[] sendData = data.getBytes();
        dataChannel.send(new DatagramPacket(sendData, sendData.length, address, port));
    }

    public void responseControlRequestUnicast(String responseText, InetAddress address, int port) throws IOException {
        byte[] sendData = responseText.getBytes();
        controlChannel.send(new DatagramPacket(sendData, sendData.length, address, port));
    }

    public void responseControlRequestBroadcast(String responseText) throws IOException {
        byte[] sendData = responseText.getBytes();
        controlChannel.send(new DatagramPacket(sendData, sendData.length, InetAddress.getByName(MULTICAST_ADDRESS), MULTICAST_PORT));
    }
}
