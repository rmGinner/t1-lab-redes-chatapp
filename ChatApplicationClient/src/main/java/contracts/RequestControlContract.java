package contracts;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class RequestControlContract {

    private String control;

    private String controlArgument;

    public RequestControlContract() {

    }

    public RequestControlContract(String control, String controlArgument) {
        this.control = control;
        this.controlArgument = controlArgument;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getControlArgument() {
        return controlArgument;
    }

    public void setControlArgument(String controlArgument) {
        this.controlArgument = controlArgument;
    }

    @Override
    public String toString() {
        return "CommandContract{" +
                "command='" + control + '\'' +
                ", controlArgument=" + controlArgument +
                '}';
    }
}
