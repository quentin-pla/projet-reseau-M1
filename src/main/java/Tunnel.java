/**
 * Utilitaire Tunnel
 */
public class Tunnel {
    /**
     * Créer un tunnel IPv6->IPv4
     * @param vm1 nom de la VM n°1
     * @param vm2 nom de la VM n°2
     * @param intName nom de l'interface
     * @param intAddress adresse IP de l'interface
     */
    public static void createTunnel6to4(String vm1, String vm2, String intName, String intAddress) {
        //On vérifie si les machines fonctionnent
        if (VMUtils.isVMStarted(vm1) && VMUtils.isVMStarted(vm2)) {
            //Récupération de l'adresse IPv4 de chaque VM
            String vm1Ipv4 = VMUtils.getVMIPv4Addresses(vm1).get(0);
            String vm2Ipv4 = VMUtils.getVMIPv4Addresses(vm2).get(0);
            //Récupération de l'adresse réseau IPv6 de chaque VM
            String vm1NetworkIpv6 = VMUtils.getVMIPv6NetworkAddress(vm1);
            String vm2NetworkIpv6 = VMUtils.getVMIPv6NetworkAddress(vm2);

            System.out.println("create tunnel");
            //Création du tunnel
            VMUtils.execSSH(vm1,
                "sudo ip tun add " + intName + " mode sit remote " + vm2Ipv4 + " local " + vm1Ipv4,
                "sudo ip link set dev " + intName + " up",
                "sudo ip -6 route add " + vm2NetworkIpv6 + " dev " + intName + " metric 1",
                "sudo ip a add " + intAddress + " dev " + intName);
        }
    }

    /**
     * Main
     * @param args arguments
     */
    public static void main(String[] args) {
        createTunnel6to4("VM1","VM3","tun1","fc00:1234:ffff::1/64");
    }
}
