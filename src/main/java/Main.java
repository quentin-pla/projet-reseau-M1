public class Main {
    /**
     * Main
     * @param args arguments
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
