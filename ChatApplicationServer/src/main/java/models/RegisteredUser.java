package models;

import java.net.InetAddress;
import java.time.Duration;

/**
 * Modelo de representação de um usuário cadastrado
 *
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class RegisteredUser {

    private String nickName;

    private Duration duration;

    private InetAddress address;

    private int port;

    public RegisteredUser() {
        this.duration = Duration.ofSeconds(20);
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

    @Override
    public String toString() {
        return "RegisteredUser{" +
                ", nickName='" + nickName + '\'' +
                ", duration=" + duration +
                ", address=" + address +
                ", port=" + port +
                '}';
    }
}
