import communication_modules.UserComModule;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Rodrigo Machado <a href="mailto:rodrigo.domingos@pucrs.br">rodrigo.domingos@pucrs.br</a>
 */

public class Main {

    public static void main(String[] args) throws IOException {
        var userComModule = new UserComModule();

        while (true) {
            userComModule.showOperations();

            final var sc = new Scanner(System.in);
            var command = sc.nextLine();

            userComModule.delegateCommand(command);
        }
    }


}
