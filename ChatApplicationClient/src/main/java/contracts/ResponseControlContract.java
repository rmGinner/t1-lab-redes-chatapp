package contracts;

/**
 * Contrato de dados para receber os controles enviados pelo servidor
 *
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
public class ResponseControlContract {

    private boolean success;

    private String message;

    public ResponseControlContract() {

    }

    public ResponseControlContract(
            String message,
            boolean success
    ) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ResponseJsonContract{" +
                "message='" + message + '\'' +
                '}';
    }
}
