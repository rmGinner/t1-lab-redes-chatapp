package contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseDataContract {

    private String to;

    private String from;

    private String message;

    public ResponseDataContract() {

    }

    public ResponseDataContract(
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
