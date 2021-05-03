package chat_impl;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public enum CommandStrategy {

    SUBSCRIBE {
        @Override
        public void processBy(String[] commandArgs) {

        }
    },

    UNSUBSCRIBE {
        @Override
        public void processBy(String[] commandArgs) {

        }
    },
    SEND_MESSAGE {
        @Override
        public void processBy(String[] commandArgs) {

        }
    };

    abstract public void processBy(String[] commandArgs);

    public static CommandStrategy getCommandByName(String commandName) {
        return Objects.nonNull(commandName) ?
                Arrays.stream(values()).filter(cmd -> cmd.name().equalsIgnoreCase(commandName.trim())).findFirst().orElse(null) :
                null;
    }

}
