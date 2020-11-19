import java.util.ArrayList;
import java.util.Arrays;
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
        String status = Command.execCommand("vagrant global-status | grep -i \".*" + vmName + " \"").get(0);
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
    public static ArrayList<String> getVMAddresses(String vmName) {
        String addresses = execSSH(vmName, "hostname -I").get(0);
        addresses = addresses.substring(addresses.indexOf(' ') + 1, addresses.length() - 1);
        return new ArrayList<>(Arrays.asList(addresses.split(" ")));
    }

    /**
     * Obtenir les adresses IPv4 associées à la VM
     * @param vmName nom de la VM
     * @return adresse IPv4
     */
    public static ArrayList<String> getVMIPv4Addresses(String vmName) {
        ArrayList<String> addresses = getVMAddresses(vmName);
        ArrayList<String> ipv4Addresses = new ArrayList<>();
        for (String ipAddress : addresses)
            if (Network.checkIPv4(ipAddress))
                ipv4Addresses.add(ipAddress);
        return ipv4Addresses;
    }

    /**
     * Obtenir l'adresse réseau de l'adresse IPv4 associée à la VM
     * @param vmName nom de la VM
     * @return adresse réseau
     */
    public static String getVMIPv4NetworkAddress(String vmName) {
        ArrayList<String> routes = execSSH(vmName, "ip r");
        String vmIPv4Address = getVMIPv4Addresses(vmName).get(0);
        for (String route : routes)
            if (route.contains("proto kernel  scope link  src " + vmIPv4Address))
                return route.substring(0,route.indexOf(' '));
        return null;
    }

    /**
     * Obtenir les adresses IPv6 associées à la VM
     * @param vmName nom de la VM
     * @return adresse IPv6
     */
    public static ArrayList<String> getVMIPv6Addresses(String vmName) {
        ArrayList<String> addresses = getVMAddresses(vmName);
        ArrayList<String> ipv6Addresses = new ArrayList<>();
        for (String ipAddress : addresses)
            if (Network.checkIPv6(ipAddress))
                ipv6Addresses.add(ipAddress);
        return ipv6Addresses;
    }

    /**
     * Obtenir l'adresse réseau de l'adresse IPv6 associée à la VM
     * @param vmName nom de la VM
     * @return adresse réseau
     */
    public static String getVMIPv6NetworkAddress(String vmName) {
        ArrayList<String> routes = execSSH(vmName, "ip -6 r");
        for (String route : routes)
            if (route.contains("proto kernel"))
                return route.substring(0,route.indexOf(' '));
        return null;
    }

    /**
     * Exécuter une série de commandes en SSH sur la VM
     * @param vmName nom de la VM
     */
    public static ArrayList<String> execSSH(String vmName, String... commands) {
        String sshCommand = "vagrant ssh " + getVMId(vmName) + " -c \"";
        for (String command : commands)
            sshCommand = sshCommand.concat(command + ";");
        sshCommand = sshCommand.concat("\"");
        System.out.println(sshCommand);
        return Command.execCommand(sshCommand);
    }
}
