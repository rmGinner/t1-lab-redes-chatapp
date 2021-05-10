package communication_modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class UserComModule {

    private static final String USER_CMD_PRIVATE_MESSAGE = "/PRIVMSG";

    private static final String USER_CMD_MESSAGE_FOR_ALL = "/MSG";

    private static final String USER_CMD_ENTER_IN_CHAT = "/ENTER";

    private static final String USER_CMD_EXIT_CHAT = "/EXIT";

    private static final String GET_ATTENTION = "/GETATT";

    private static final String GET_ATTENTION_MANY = "/GETATTMANY";

    private static final String REACT_TO_USER = "/REACT";

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

        if (!command.contains(" ")) {
            System.out.println("Operação inválida!");

            return;
        }

        var commandArguments = command.split(" ");

        if (commandArguments[0].trim().equalsIgnoreCase(USER_CMD_ENTER_IN_CHAT)) {
            if (commandArguments.length != 2) {
                System.out.println("Parâmetros inválidos para o comando");
                return;
            }

            String newUser = commandArguments[1];

            if (serverComModule.hasUser()) {
                System.out.println("Usuário já cadastrado.");

                return;
            }

            this.serverComModule.registerUser(newUser);
        } else if (commandArguments[0].trim().equalsIgnoreCase(USER_CMD_MESSAGE_FOR_ALL)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            if (commandArguments.length != 2) {
                System.out.println("Parâmetros inválidos para o comando");
                return;
            }

            var message = commandArguments[1];

            serverComModule.sendMessage(String.format("\n\n %s escreveu para TODOS: %s \n\n", serverComModule.getUser(), message), null);
        } else if (commandArguments[0].trim().equalsIgnoreCase(USER_CMD_PRIVATE_MESSAGE)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            if (commandArguments.length != 3) {
                System.out.println("Parâmetros inválidos para o comando.");
                return;
            }

            var toUser = commandArguments[1];
            var message = commandArguments[2];

            serverComModule.sendMessage(String.format("\n\n %s escreveu para %s: %s \n\n", serverComModule.getUser(), toUser, message), toUser);
        } else if (commandArguments[0].trim().equalsIgnoreCase(USER_CMD_EXIT_CHAT)) {
            serverComModule.exitUser();
        } else if (commandArguments[0].trim().equalsIgnoreCase(GET_ATTENTION)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            if (commandArguments.length != 2) {
                System.out.println("Parâmetros inválidos para o comando.");
                return;
            }

            var toUser = commandArguments[1];

            serverComModule.sendMessage(String.format("%s chamou a atenção de %s", serverComModule.getUser(), toUser), toUser);
        } else if (commandArguments[0].trim().equalsIgnoreCase(GET_ATTENTION_MANY)) {
            if (!serverComModule.hasUser()) {
                System.out.println("É necessário entrar no chat primeiro");
                return;
            }

            if (commandArguments.length < 3) {
                System.out.println("Parâmetros inválidos para o comando.");
                return;
            }

            var toUsers = Arrays.copyOfRange(commandArguments, 1, commandArguments.length);

            var sb = new StringBuilder(String.format("%s chamou a atenção de ", serverComModule.getUser()));

            for (int i = 0; i < toUsers.length; i++) {
                if (i == 0) {
                    sb.append(" %s");
                } else if (toUsers[i].equalsIgnoreCase(toUsers[toUsers.length - 1])) {
                    sb.append(" e %s");
                } else {
                    sb.append(", %s");
                }
            }

            String formattedMessage = String.format(sb.toString(), toUsers);
            serverComModule.sendMessage(formattedMessage, null);
        } else if (commandArguments[0].trim().equalsIgnoreCase(REACT_TO_USER)) {
            if (commandArguments.length != 2) {
                System.out.println("Parâmetros inválidos para o comando");
                return;
            }

            String originalMessage = "%s reagiu a última mensagem de %s: %s";

            originalMessage = originalMessage.formatted(serverComModule.getUser(), commandArguments[1], ":D");

            serverComModule.sendMessage(originalMessage, commandArguments[0]);
        } else {
            System.out.println("Opção inválida.");
        }
    }

    public void showOperations() {
        System.out.println("Operações: \n\n");
        System.out.printf("Entrar no chat: %s {nome do usuário} \n", USER_CMD_ENTER_IN_CHAT);
        System.out.printf("Sair do chat: %s\n", USER_CMD_EXIT_CHAT);
        System.out.printf("Enviar mensagem para um usuário: %s {usuário} {mensagem} \n", USER_CMD_PRIVATE_MESSAGE);
        System.out.printf("Enviar mensagem para todos os usuários da sala: %s {mensagem} \n", USER_CMD_MESSAGE_FOR_ALL);
        System.out.printf("Chamar a atenção de alguém: %s {usuário} \n", GET_ATTENTION);
        System.out.printf("Chamar a atenção de vários usuários: %s {usuário1} {usuário2} {usuário3}... \n", GET_ATTENTION_MANY);
        System.out.printf("Reagir à última mensagem de um usuário: %s {usuário} \n\n\n", REACT_TO_USER);

    }
}
