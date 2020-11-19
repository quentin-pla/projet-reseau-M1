import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Commande Bash
 */
public class Command {
    /**
     * Éxecuter une commande bash
     * @param command commande
     */
    public static String execCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line).append("\n");
            if (process.waitFor() == 0)
                return output.toString();
            else System.err.println(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
        return null;
    }
}
