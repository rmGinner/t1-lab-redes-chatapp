package server;

import models.RegisteredUser;

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

    private DatagramPacket receivePacket;

    private DatagramSocket datagramSocket;

    private Timer timer = new Timer();

    public UdpServer() {
    }

    public void start(int port) throws IOException {
        this.datagramSocket = new DatagramSocket(port);
    }

    public boolean isOpened() {
        return !this.datagramSocket.isClosed();
    }

    public void stop() {
        this.datagramSocket.close();
    }

    public String receiveData() throws IOException {
        byte[] receiveData = new byte[50000];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        datagramSocket.receive(receivePacket);

        return new String(receivePacket.getData());
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

    public InetAddress getAddressFromLastReceivedPacket() {
        return receivePacket.getAddress();
    }

    public int getPortFromLastReceivedPacket() {
        return receivePacket.getPort();
    }

    public void sendData(String data, InetAddress address, int port) throws IOException {
        byte[] sendData = data.getBytes();
        datagramSocket.send(new DatagramPacket(sendData, sendData.length, address, port));
    }
}
