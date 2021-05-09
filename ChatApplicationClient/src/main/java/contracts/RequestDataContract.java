package contracts;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class RequestDataContract {

    private String from;

    private String to;

    private String message;

    public RequestDataContract() {

    }

    public RequestDataContract(String message) {
        this.message = message;
    }

    public RequestDataContract(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public RequestDataContract(String from, String message, String to) {
        this.from = from;
        this.message = message;
        this.to = to;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
                '}';
    }
}
