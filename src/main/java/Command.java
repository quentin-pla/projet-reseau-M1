import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Commande Bash
 */
public class Command {
    /**
     * Éxecuter une commande bash
     * @param command commande
     */
    public static ArrayList<String> execCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        try {
            Process process = processBuilder.start();
            ArrayList<String> output = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                output.add(line);
            if (process.waitFor() == 0) return output;
            else System.err.println(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
        return null;
    }
}
