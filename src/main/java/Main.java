/**
 * Main
 */
public class Main {
    /**
     * Main
     * @param args arguments
     * 0: nom de la machine virtuelle n°1
     * 1: nom de la machine virtuelle n°2
     * 2: nom de l'interface du tunnel
     * 3: adresse de l'interface du tunnel
     * 4: paramètre --listen pour écouter les paquets du tunnel grâce à la commande tshark
     */
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("Utilisation: nom_machine_virtuelle_1 nom_machine_virtuelle_2 nom_interface_tunnel adresse_ip_tunnel [--listen]");
            System.out.println("--listen : écouter les paquets provenant du tunnel");
        } else if (args.length == 4) {
            new Tunnel(args[0], args[1], args[2], args[3]);
        } else if (args.length == 5 && args[4].equals("--listen")) {
            new Tunnel(args[0], args[1], args[2], args[3]).readPacketsFromTunnel();
        } else {
            System.err.println("ERREUR : Nombre d'arguments invalide.");
            System.out.println("Utilisation: nom_machine_virtuelle_1 nom_machine_virtuelle_2 nom_interface_tunnel adresse_ip_tunnel [--listen]");
            System.exit(1);
        }
    }
}

// java Main VM1 VM3 tun0 fc00:1234:ffff::1/64