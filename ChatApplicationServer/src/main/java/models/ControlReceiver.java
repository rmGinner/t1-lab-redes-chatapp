package models;

import java.net.InetAddress;

/**
 * Receptor de controles, endereço do cliente e porta.
 *
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class ControlReceiver {

    private InetAddress address;

    private int port;

    private String control;

    public ControlReceiver() {

    }

    public ControlReceiver(InetAddress address, int port, String control) {
        this.address = address;
        this.port = port;
        this.control = control;
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

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    @Override
    public String toString() {
        return "UdpDataReceiver{" +
                "address=" + address +
                ", port=" + port +
                ", control='" + control + '\'' +
                '}';
    }
}
