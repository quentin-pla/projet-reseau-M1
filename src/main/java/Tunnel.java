/**
 * Utilitaire Tunnel
 */
public class Tunnel {
    /**
     * Créer un tunnel IPv6->IPv4
     * @param vm1 nom de la VM n°1
     * @param vm2 nom de la VM n°2
     */
    public static void createTunnel6to4(String vm1, String vm2) {

    }

    /**
     * Main
     * @param args arguments
     */
    public static void main(String[] args) {
        System.out.println(VMUtils.getVMIPv6Address("VM1"));
    }
}
