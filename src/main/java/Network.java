import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utilitaire réseau
 */
public class Network {
    /**
     * Vérifier si une adresse IP est au format IPv4
     * @param ip adresse IP
     * @return booléen
     */
    public static boolean checkIPv4(String ip) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address instanceof Inet4Address;
    }

    /**
     * Vérifier si une adresse IP est au format IPv6
     * @param ip adresse IP
     * @return booléen
     */
    public static boolean checkIPv6(String ip) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address instanceof Inet6Address;
    }
}
