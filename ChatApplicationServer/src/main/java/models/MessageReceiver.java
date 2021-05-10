package models;

import java.net.InetAddress;

/**
 * //Receptor de mensagens enviadas pelo cliente junto com a sua porta e seu endereço IP.
 *
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
public class MessageReceiver {

    private InetAddress address;

    private int port;

    private String data;

    public MessageReceiver() {

    }

    public MessageReceiver(InetAddress address, int port, String data) {
        this.address = address;
        this.port = port;
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    @Override
    public String toString() {
        return "UdpDataReceiver{" +
                "address=" + address +
                ", port=" + port +
                ", data='" + data + '\'' +
                '}';
    }
}
