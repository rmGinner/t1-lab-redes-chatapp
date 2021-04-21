package models;

import java.net.InetAddress;
import java.time.Duration;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class RegisteredUser {

    private String id;

    private InetAddress address;

    private int port;

    private String nickName;

    private Duration duration;

    public RegisteredUser() {
        this.duration = Duration.ofSeconds(20);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "RegisteredUser{" +
                "id='" + id + '\'' +
                ", address=" + address +
                ", port=" + port +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}
