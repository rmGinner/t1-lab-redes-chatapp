package communication_modules;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class UserComModule {

    private static final String USER_CMD_PRIVATE_MESSAGE = "/PRIVMSG";

    private static final String USER_CMD_MESSAGE_FOR_ALL = "/MSG";

    private static final String USER_CMD_ENTER_IN_CHAT = "/ENTER";

    private static final String USER_CMD_EXIT_CHAT = "/EXIT";

    private final ServerComModule serverComModule;

    public UserComModule() throws IOException {
        this.serverComModule = new ServerComModule();
    }

    public void delegateCommand(
            String command
    ) throws IOException {
        if (Objects.isNull(command)) {
            System.out.println("Operação inválida!");

            return;
        }

        command = command.toUpperCase().trim();

        if (command.startsWith(USER_CMD_ENTER_IN_CHAT)) {
            var commandArguments = command.split(" ");

            String newUser = commandArguments[1];

            if (serverComModule.hasUser()) {
                System.out.println("Usuário já cadastrado.");

                return;
            }

            this.serverComModule.registerUser(newUser);
        } else if (command.startsWith(USER_CMD_MESSAGE_FOR_ALL)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            final var commandParams = command.split(" ");

            if (commandParams.length != 2) {
                System.out.println("Parâmetros inválidos para o comando");
                return;
            }

            var message = commandParams[1];

            serverComModule.sendMessage(message, null);
        } else if (command.startsWith(USER_CMD_PRIVATE_MESSAGE)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            var commandParams = command.split(" ");

            if (commandParams.length != 3) {
                System.out.println("Parâmetros inválidos para o comando.");
                return;
            }

            var toUser = commandParams[1];
            var message = commandParams[2];

            serverComModule.sendMessage(message, toUser);
        } else {
            System.out.println("Opção inválida.");
        }
    }

    public void showOperations() {
        System.out.println("Operações: \n\n");
        System.out.printf("Entrar no chat: %s {nome do usuário} \n", USER_CMD_ENTER_IN_CHAT);
        System.out.printf("Sair do chat: %s\n", USER_CMD_EXIT_CHAT);
        System.out.printf("Enviar mensagem para um usuário: %s {usuário} {mensagem} \n", USER_CMD_PRIVATE_MESSAGE);
        System.out.printf("Enviar mensagem para todos os usuários da sala: %s {mensagem} \n\n\n", USER_CMD_MESSAGE_FOR_ALL);
    }
}
