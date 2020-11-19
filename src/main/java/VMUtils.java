import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire machines virtuelles vagrant
 */
public class VMUtils {
    /**
     * Liste des identifiants associés aux noms des VMs
     */
    private static Map<String,String> vmIDs = new HashMap<>();

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
        if (!vmIDs.containsKey(vmName)) {
            String vmStatus = getVMStatus(vmName);
            String id = vmStatus.substring(0,vmStatus.indexOf(' '));
            vmIDs.put(vmName,id);
        }
        return vmIDs.get(vmName);
    }

    /**
     * Récupérer les adresses IP associées à une VM
     * @param vmName nom de la VM
     * @return adresses
     */
    public static String[] getVMAddresses(String vmName) {
        String addresses = execSSH(vmName, "hostname -I");
        addresses = addresses.substring(addresses.indexOf(' ') + 1, addresses.length() - 1);
        return addresses.split(" ");
    }

    /**
     * Obtenir l'adresse IPv4 associée à la VM
     * @param vmName nom de la VM
     * @return adresse IPv4
     */
    public static String getVMIPv4Address(String vmName) {
        String[] addresses = getVMAddresses(vmName);
        for (String ipAddress : addresses) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (address instanceof Inet4Address)
                return ipAddress;
        }
        return null;
    }

    /**
     * Obtenir l'adresse IPv6 associée à la VM
     * @param vmName nom de la VM
     * @return adresse IPv6
     */
    public static String getVMIPv6Address(String vmName) {
        String[] addresses = getVMAddresses(vmName);
        for (String ipAddress : addresses) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (address instanceof Inet6Address)
                return ipAddress;
        }
        return null;
    }

    /**
     * Exécuter une série de commandes en SSH sur la VM
     * @param vmName nom de la VM
     */
    public static String execSSH(String vmName, String... commands) {
        String sshCommand = "vagrant ssh " + getVMId(vmName) + " -c \"";
        for (String command : commands)
            sshCommand = sshCommand.concat(command + ";");
        sshCommand = sshCommand.concat("\"");
        return Command.execCommand(sshCommand);
    }
}
