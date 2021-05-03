package contracts;

import java.util.Arrays;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class CommandContract {

    private String command;

    private String[] commandArguments;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getCommandArguments() {
        return commandArguments;
    }

    public void setCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    @Override
    public String toString() {
        return "CommandContract{" +
                "command='" + command + '\'' +
                ", commandArguments=" + Arrays.toString(commandArguments) +
                '}';
    }
}
