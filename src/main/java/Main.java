public class Main {
    /**
     * Main
     * @param args arguments
     * 0: nom de la machine virtuelle n°1
     * 1: nom de la machine virtuelle n°2
     * 2: nom de l'interface du tunnel
     * 3: adresse de l'interface du tunnel
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            new Tunnel(args[0], args[1], args[2], args[3]);
        } else {
            System.err.println("ERREUR : Nombre d'arguments invalide.");
            System.out.println("Utilisation: {nom machine virtuelle n°1} {nom machine virtuelle n°2} {nom interface tunnel} {adresse ip associée au tunnel}");
            System.exit(1);
        }
    }
}

// java Main VM1 VM3 tun0 fc00:1234:ffff::1/64