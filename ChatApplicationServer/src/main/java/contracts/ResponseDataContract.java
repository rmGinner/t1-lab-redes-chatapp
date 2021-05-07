package contracts;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class ResponseDataContract {

    public ResponseDataContract(
            String from,
            String message
    ) {
        this.from = from;
        this.message = message;
    }

    private String from;

    private String message;

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
        return "DataContract{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
