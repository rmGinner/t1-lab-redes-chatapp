package contracts;

import java.util.List;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class ClientJsonContract {

    private String from;

    private List<String> to;

    private String message;

    private Boolean subscribe;

    private Boolean unSubscribe;

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void subscribe() {
        this.subscribe = true;
    }

    public void unSubscribe() {
        this.unSubscribe = true;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "ClientJsonContract{" +
                "to=" + to +
                ", message='" + message + '\'' +
                ", isRegistration=" + subscribe +
                '}';
    }
}
