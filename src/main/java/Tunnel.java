/**
 * Utilitaire Tunnel
 */
public class Tunnel {
    /**
     * Créer un tunnel IPv6->IPv4
     * @param vm1Name nom de la VM n°1
     * @param vm2Name nom de la VM n°2
     * @param intName nom de l'interface
     * @param intAddress adresse IP de l'interface
     */
    public static void createTunnel6to4(String vm1Name, String vm2Name, String intName, String intAddress) {
        //Instantiation des VMs
        VM vm1 = new VM(vm1Name);
        VM vm2 = new VM(vm2Name);
        //Récupération de l'état des machines
        VMUtils.getVMsStatus();
        //Récupération des adresses ip des machines
        VMUtils.getVMsAddresses();
        //Vérification des adresses pour la VM n°1
        if (vm1.getIpv4Addresses().isEmpty() || vm1.getIpv6Addresses().isEmpty()) {
            System.err.println("ERREUR : " + vm1Name + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.");
            System.exit(1);
        }
        //Vérification des adresses pour la VM n°2
        if (vm2.getIpv4Addresses().isEmpty() || vm2.getIpv6Addresses().isEmpty()) {
            System.err.println("ERREUR : " + vm2Name + " ne dispose pas de deux interfaces comprenant une adresse IPv4 et une adresse IPv6.");
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
    }

    /**
     * Main
     * @param args arguments
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            createTunnel6to4("VM1", "VM3", "tun0", "fc00:1234:ffff::1/64");
        } else {
            System.err.println("ERREUR : Nombre d'arguments invalide.");
            System.out.println("Utilisation: {nom machine virtuelle n°1} {nom machine virtuelle n°2} {nom interface tunnel} {adresse ip associée au tunnel}");
            System.exit(1);
        }
    }
}
