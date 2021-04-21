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


    public void setSubscribe(Boolean subscribe) {
        this.subscribe = subscribe;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Boolean getSubscribe() {
        return subscribe;
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
