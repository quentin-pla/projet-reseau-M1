/**
 * Utilitaire machines virtuelles vagrant
 */
public class VMUtils {
    /**
     * Obtenir le status d'une VM spécifique
     * @param vmName nom de la VM
     */
    public static String getVMStatus(String vmName) {
        String status = Command.execCommand("vagrant global-status | grep -i \".*" + vmName + " \"");
        if (status.length() == 0) {
            System.err.println("ERREUR : Machine " + vmName + " introuvable.");
            System.exit(1);
        }
        return status;
    }

    /**
     * Vérifier si une machine est en cours d'exécution
     * @param vmName nom de la VM
     */
    public static boolean isVMStarted(String vmName) {
        String vmStatus = getVMStatus(vmName);
        int beginIndexState = vmStatus.indexOf("virtualbox") + 11;
        String state = vmStatus.substring(beginIndexState, vmStatus.indexOf(' ', beginIndexState));
        return state.equals("running");
    }

    /**
     * Obtenir l'identifiant d'une VM
     * @param vmName nom de la VM
     * @return identifiant vm
     */
    public static String getVMId(String vmName) {
        String vmStatus = getVMStatus(vmName);
        return vmStatus.substring(0,vmStatus.indexOf(' '));
    }

    /**
     * Exécuter une série de commandes en SSH sur la VM
     * @param vmName nom de la VM
     */
    public static void execSSH(String vmName, String[] commands) {
        StringBuilder sshCommand = new StringBuilder("vagrant ssh " + getVMId(vmName) + " << END\n");
        for (String command : commands)
            sshCommand.append(command).append("\n");
        sshCommand.append("logout\n").append("END");
        System.out.println(sshCommand);
        Command.execCommand(sshCommand.toString());
    }
}
