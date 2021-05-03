package contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseJsonContract {

    private String from;

    private String message;

    public ResponseJsonContract(
            String from,
            String message
    ) {
        this.from = from;
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResponseJsonContract{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
