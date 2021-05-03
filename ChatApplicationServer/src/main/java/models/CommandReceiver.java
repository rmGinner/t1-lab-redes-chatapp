package models;

import java.net.InetAddress;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class CommandReceiver {

    private InetAddress address;

    private int port;

    private String command;

    public CommandReceiver() {

    }

    public CommandReceiver(InetAddress address, int port, String command) {
        this.address = address;
        this.port = port;
        this.command = command;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "UdpDataReceiver{" +
                "address=" + address +
                ", port=" + port +
                ", command='" + command + '\'' +
                '}';
    }
}
