package contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContract {

    private String registrationId;

    private String errorMessage;

    public UserContract() {
    }

    public UserContract(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "UserRegistrationContract{" +
                "registrationId='" + registrationId + '\'' +
                '}';
    }
}
