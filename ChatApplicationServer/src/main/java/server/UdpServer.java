package server;

import models.CommandReceiver;
import models.RegisteredUser;
import models.UdpDataReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class UdpServer {

    private DatagramSocket dataChannel;

    private DatagramSocket commandChannel;

    private static final Integer DATA_CHANNEL_PORT = 4390;

    private static final Integer COMMAND_CHANNEL_PORT = 4391;

    private Timer timer = new Timer();

    public UdpServer() {
    }

    public void start() throws IOException {
        this.dataChannel = new DatagramSocket(DATA_CHANNEL_PORT);
        this.commandChannel = new DatagramSocket(COMMAND_CHANNEL_PORT);
    }

    public boolean isOpened() {
        return !this.dataChannel.isClosed();
    }

    public void stop() {
        this.dataChannel.close();
    }

    public UdpDataReceiver receiveData() throws IOException {
        byte[] receiveData = new byte[50000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, InetAddress.getLoopbackAddress(), DATA_CHANNEL_PORT);
        dataChannel.receive(receivePacket);

        return new UdpDataReceiver(
                receivePacket.getAddress(),
                receivePacket.getPort(),
                new String(receivePacket.getData())
        );
    }

    public CommandReceiver receiveCommand() throws IOException {
        byte[] receiveData = new byte[50000];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length, InetAddress.getLoopbackAddress(), COMMAND_CHANNEL_PORT);
        commandChannel.receive(receivePacket);

        return new CommandReceiver(
                receivePacket.getAddress(),
                receivePacket.getPort(),
                new String(receivePacket.getData())
        );
    }

    public void updateUserSession(RegisteredUser user) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (user.getDuration().toSeconds() > 0) {
                    user.setDuration(Duration.ofSeconds(user.getDuration().toSeconds() - 1));
                } else {
                    this.cancel();
                }
            }
        }, 0, 1000);
    }

    public void sendData(String data, InetAddress address, int port) throws IOException {
        byte[] sendData = data.getBytes();
        dataChannel.send(new DatagramPacket(sendData, sendData.length, address, port));
    }
}
