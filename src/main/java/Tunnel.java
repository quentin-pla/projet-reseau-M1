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
            System.err.println("ERREUR : " + vm1.getName() + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.");
            System.exit(1);
        }
        //Vérification des adresses pour la VM n°2
        if (vm2.getIpv4Addresses().isEmpty() || vm2.getIpv6Addresses().isEmpty()) {
            System.err.println("ERREUR : " + vm2.getName() + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.");
            System.exit(1);
        }
        //Création du tunnel sur la VM n°1
        VMUtils.execParallelTask(() -> VMUtils.execSSH(vm1,
            "sudo ip tun add " + intName + " mode sit remote " + vm2.getIpv4Addresses().get(0) + " local " + vm1.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm2.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //Création du tunnel sur la VM n°2
        VMUtils.execParallelTask(() -> VMUtils.execSSH(vm2,
            "sudo ip tun add " + intName + " mode sit remote " + vm1.getIpv4Addresses().get(0) + " local " + vm2.getIpv4Addresses().get(0),
            "sudo ip link set dev " + intName + " up",
            "sudo ip -6 route add " + vm1.getIpv6NetworkAddresses().get(0) + " dev " + intName + " metric 1",
            "sudo ip a add " + intAddress + " dev " + intName));
        //On attend que les tunnels soient créés
        VMUtils.waitTasksToFinish();
        //Message de succès
        System.out.println("Tunnel " + intName + " créé avec succès.");
    }
}
