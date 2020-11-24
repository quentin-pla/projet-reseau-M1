import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utilitaire Tunnel
 */
public class Tunnel {
    /**
     * Machine virtuelle n°1
     */
    private final VM vm1;

    /**
     * Machine virtuelle n°2
     */
    private final VM vm2;

    /**
     * Nom de l'interface du tunnel
     */
    private final String intName;

    /**
     * Adresse de l'interface du tunnel
     */
    private final String intAddress;

    /**
     * Constructeur
     * @param vm1Name nom de la machine virtuelle n°1
     * @param vm2Name nom de la machine virtuelle n°2
     * @param intName nom de l'interface
     * @param intAddress adresse de l'interface
     */
    public Tunnel(String vm1Name, String vm2Name, String intName, String intAddress) {
        this.vm1 = new VM(vm1Name);
        this.vm2 = new VM(vm2Name);
        this.intName = intName;
        this.intAddress = intAddress;
        initTunnel();
    }

    /**
     * Initialisation du tunnel IPv6->IPv4
     */
    private void initTunnel() {
        //Récupération de l'état des machines
        VMUtils.getVMsStatus();
        //Récupération des adresses ip des machines
        VMUtils.getVMsAddresses();
        //Vérification des adresses pour la VM n°1
        if (vm1.getIpv4Addresses().isEmpty() || vm1.getIpv6Addresses().isEmpty()) {
            System.err.println("ERREUR : " + vm1.getName() + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.\r");
            System.exit(1);
        }
        //Vérification des adresses pour la VM n°2
        if (vm2.getIpv4Addresses().isEmpty() || vm2.getIpv6Addresses().isEmpty()) {
            System.err.println("ERREUR : " + vm2.getName() + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.\r");
            System.exit(1);
        }
        //Création de la première extremité du tunnel sur la VM n°1
        VMUtils.execParallelTask(() -> VMUtils.execSSH(vm1,
            "sudo ip tun del " + intName,
            "sudo ip tun add " + intName + " mode sit remote " + vm2.getIpv4Addresses().get(0) + " local " + vm1.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm2.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //Création de la seconde extremité du tunnel sur la VM n°2
        VMUtils.execParallelTask(() -> VMUtils.execSSH(vm2,
            "sudo ip tun del " + intName,
            "sudo ip tun add " + intName + " mode sit remote " + vm1.getIpv4Addresses().get(0) + " local " + vm2.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm1.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //On attend que les extremités du tunnel soient créées
        VMUtils.waitTasksToFinish();
        //Message de succès
        System.out.println("Tunnel " + intName + " créé avec succès.\r");
    }

    /**
     * Lire les paquets du tunnel depuis les deux extrémités
     */
    public void readPacketsFromTunnel() {
        VMUtils.execParallelTask(() -> readPacketsFromExtremity(vm1));
        VMUtils.execParallelTask(() -> readPacketsFromExtremity(vm2));
    }

    /**
     * Lire les paquets du tunnel depuis une extremité
     * @param vm extremité du tunnel
     */
    public void readPacketsFromExtremity(VM vm) {
        String sshCommand = "vagrant ssh " + vm.getId() + " -c \"sudo tshark -i " + intName + "\"";
        ProcessBuilder processBuilder = new ProcessBuilder().command("bash", "-c", sshCommand);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        try {
            Process process = processBuilder.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean ready = false;
            while((line = in.readLine()) != null) {
                if (ready) System.out.println(intName + "@" + vm.getName() + "> " + line + "\r");
                if (line.equals("Capturing on '" + intName + "'")) {
                    ready = true;
                    System.out.println("Capture des paquets sur " + intName + " via " + vm.getName() + "...\r");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
