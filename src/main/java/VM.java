import java.util.ArrayList;

/**
 * Machine virtuelle Vagrant
 */
public class VM {
    /**
     * Liste des VMs instanciées
     */
    private static final ArrayList<VM> VMs = new ArrayList<>();

    /**
     * Liste des adresses IPv4 associées à la machine
     */
    private final ArrayList<String> ipv4Addresses = new ArrayList<>();

    /**
     * Liste des adresses réseaux IPv4 associées à la machine
     */
    private final ArrayList<String> ipv4NetworkAddresses = new ArrayList<>();

    /**
     * Liste des adresses IPv6 associées à la machine
     */
    private final ArrayList<String> ipv6Addresses = new ArrayList<>();

    /**
     * Liste des adresses réseaux IPv6 associées à la machine
     */
    private final ArrayList<String> ipv6NetworkAddresses = new ArrayList<>();

    /**
     * Nom de la machine
     */
    private String name;

    /**
     * ID vagrant
     */
    private String id;

    /**
     * Savoir si elle fonctionne
     */
    private Boolean isTurnedOn;

    /**
     * Constructeur
     * @param name nom de la VM
     */
    public VM(String name) {
        this.name = name;
        VMs.add(this);
    }

    /**
     * Vérifier l'état d'une machine
     * @return booléen
     */
    public boolean checkStatus() {
        return id != null && isTurnedOn != null;
    }

    // GETTERS & SETTERS //

    public static ArrayList<VM> getVMs() {
        return VMs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTurnedOn() {
        return isTurnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        isTurnedOn = turnedOn;
    }

    public ArrayList<String> getIpv4Addresses() { return ipv4Addresses; }

    public ArrayList<String> getIpv4NetworkAddresses() { return ipv4NetworkAddresses; }

    public ArrayList<String> getIpv6Addresses() { return ipv6Addresses; }

    public ArrayList<String> getIpv6NetworkAddresses() { return ipv6NetworkAddresses; }
}
