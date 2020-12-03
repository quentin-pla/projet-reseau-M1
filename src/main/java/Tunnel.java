import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
     * Utilitaire VMs
     */
    private static final VMUtils vmUtils = VMUtils.getInstance();

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
        checkArgs();
        initTunnel();
    }

    /**
     * Vérifier les paramètres
     */
    private void checkArgs() {
        String ipAddress = intAddress.substring(0,intAddress.indexOf('/'));
        if (!Network.checkIPv4(ipAddress) && !Network.checkIPv6(ipAddress)) {
            System.err.println("ERREUR : L'adresse IP du tunnel est invalide.");
            System.exit(1);
        }
        if (!intName.matches("[A-Za-z0-9]+")) {
            System.err.println("ERREUR : Le nom de l'interface du tunnel est invalide.");
            System.exit(1);
        }
    }

    /**
     * Initialisation du tunnel IPv6->IPv4
     */
    private void initTunnel() {
        //Récupération de l'état des machines
        vmUtils.getVMsStatus();
        //Récupération des adresses ip des machines
        vmUtils.getVMsAddresses();

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

        System.out.println("Création du tunnel en cours...");
        //Création de la première extremité du tunnel sur la VM n°1
        vmUtils.execParallelTask(() -> vmUtils.execSSH(vm1,
            "sudo ip tun del " + intName,
            "sudo ip tun add " + intName + " mode sit remote " + vm2.getIpv4Addresses().get(0) + " local " + vm1.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm2.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //Création de la seconde extremité du tunnel sur la VM n°2
        vmUtils.execParallelTask(() -> vmUtils.execSSH(vm2,
            "sudo ip tun del " + intName,
            "sudo ip tun add " + intName + " mode sit remote " + vm1.getIpv4Addresses().get(0) + " local " + vm2.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm1.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //On attend que les extremités du tunnel soient créées
        vmUtils.waitTasksToFinish();
        System.out.println("# Tunnel " + intName + " créé avec succès.\r");

        System.out.println("Ajout des routes aux hôtes reliés au tunnel...");
        //Récupération des hôtes liés à la première extrémité
        ArrayList<VM> extremity1LinkedVMs = getNetworkVMsLinkedExtremity(vm1);
        //Remplacement de la route pour passer par le tunnel
        for (VM vm : extremity1LinkedVMs)
            vmUtils.execParallelTask(() -> vmUtils.execSSH(vm,
                    "sudo ip route replace " + vm2.getIpv6NetworkAddresses().get(0) + " via " + vm1.getIpv6Addresses().get(0)));
        //Récupération des hôtes liés à la deuxième extrémité
        ArrayList<VM> extremity2LinkedVMs = getNetworkVMsLinkedExtremity(vm2);
        //Remplacement de la route pour passer par le tunnel
        for (VM vm : extremity2LinkedVMs)
            vmUtils.execParallelTask(() -> vmUtils.execSSH(vm,
                    "sudo ip route replace " + vm1.getIpv6NetworkAddresses().get(0) + " via " + vm2.getIpv6Addresses().get(0)));
        //On attend que les hôtes soient reliés
        vmUtils.waitTasksToFinish();
        System.out.println("Liaison des hôtes terminée.");
    }

    /**
     * Obtenir les VMs liées à une extrémité du tunnel
     * @param extremity machine virtuelle étant une extrémité du tunnel
     * @return liste des hôtes reliés
     */
    public ArrayList<VM> getNetworkVMsLinkedExtremity(VM extremity) {
        ArrayList<VM> linkedVMs = new ArrayList<>();
        for (VM vm : vmUtils.getVms())
            if (vm != extremity)
                if (vm.getIpv6NetworkAddresses().contains(extremity.getIpv6NetworkAddresses().get(0)))
                    linkedVMs.add(vm);
        return linkedVMs;
    }

    /**
     * Lire les paquets du tunnel depuis les deux extrémités
     */
    public void readPacketsFromTunnel() {
        vmUtils.execParallelTask(() -> readPacketsFromExtremity(vm1));
        vmUtils.execParallelTask(() -> readPacketsFromExtremity(vm2));
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
