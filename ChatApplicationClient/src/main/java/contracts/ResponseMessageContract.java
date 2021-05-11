package contracts;

/**
 * Contrato de dados para receber mensagens do servidor.
 *
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
public class ResponseMessageContract {

    private String to;

    private String from;

    private String message;

    public ResponseMessageContract() {

    }

    public ResponseMessageContract(
            String from,
            String message,
            String to
    ) {
        this.from = from;
        this.message = message;
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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "ResponseDataContract{" +
                "to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
