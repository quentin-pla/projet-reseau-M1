import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Utilitaire machines virtuelles vagrant
 */
public class VMUtils {
    /**
     * Liste des VMs instanciées
     */
    private static final ArrayList<VM> vms = new ArrayList<>();

    /**
     * Réservoir de threads
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(8);

    /**
     * Service de complétion
     */
    private static CompletionService<Void> service = new ExecutorCompletionService<>(executor);

    /**
     * Liste des taches en cours
     */
    private static final CopyOnWriteArrayList<Future<Void>> tasks = new CopyOnWriteArrayList<>();

    /**
     * Obtenir le status de chaque VM initialisée
     */
    public static void getVMsStatus() {
        System.out.println("Récupération du status des machines en cours d'exécution...\r");
        ArrayList<String> status = Command.execCommand("vagrant global-status");
        if (status != null) {
            initTunnelVms(status);
            initNetworkVms(status);
            System.out.println("# Machines actives sur le réseau: " + vms.size() + ".");
        } else {
            System.err.println("ERREUR : Status des machines irrécupérable.\r");
            System.exit(1);
        }
    }

    /**
     * Initialiser les machines extrémités du tunnel
     * @param status status vagrant
     */
    private static void initTunnelVms(ArrayList<String> status) {
        for (VM vm : vms) {
            for (String output : status) {
                if (output.contains(vm.getName() + " ")) {
                    vm.setId(output.substring(0, output.indexOf(' ')));
                    int beginIndexState = output.indexOf("virtualbox") + 11;
                    String state = output.substring(beginIndexState, output.indexOf(' ', beginIndexState));
                    vm.setTurnedOn(state.equals("running"));
                    break;
                }
            }
            if (!vm.checkStatus()) {
                System.err.println("ERREUR : Machine " + vm.getName() + " introuvable.\r");
                System.exit(1);
            }
            if (!vm.isTurnedOn()) {
                System.err.println("ERREUR : Machine " + vm.getName() + " éteinte. Veuillez la démarrer.\r");
                System.exit(1);
            }
        }
    }

    /**
     * Initialiser les machines du réseau
     * @param status status vagrant
     */
    private static void initNetworkVms(ArrayList<String> status) {
        ArrayList<String> vmIds = new ArrayList<>();
        for (VM vm : vms) vmIds.add(vm.getId());
        for (String output : status) {
            if (output.contains("running")) {
                String vmId = output.substring(0, output.indexOf(' '));
                String vmName = output.substring(output.lastIndexOf('/') + 1, output.indexOf(' ',
                        output.lastIndexOf('/') + 1));
                if (!vmIds.contains(vmId)) {
                    vmIds.add(vmId);
                    VM vm = new VM(vmName);
                    vm.setId(vmId);
                    vm.setTurnedOn(true);
                }
            }
        }
    }

    /**
     * Obtenir les adresses IP associées aux adresses de chaque VM initialisée
     */
    public static void getVMsAddresses() {
        Map<String, Map<String,ArrayList<String>>> sshCommands = new HashMap<>();
        for (VM vm : vms) {
            execParallelTask(() -> {
                if (vm.getId() == null) {
                    System.err.println("ERREUR : ID de " + vm.getName() + " non défini.\r");
                    System.exit(1);
                }
                System.out.println("Récupération des adresses de " + vm.getName() + "...\r");
                //Exécution et récupération des sorties commandes
                String[] commandsToRun = new String[]{"hostname -I", "ip r","ip -6 r"};
                sshCommands.put(vm.getName(), execSSH(vm, commandsToRun));
                //Liste des adresses IP récupérées par hostname
                String addresses = sshCommands.get(vm.getName()).get(commandsToRun[0]).get(0);
                //Suppression de la première adresse étant réservée à vagrant
                addresses = addresses.substring(addresses.indexOf(' ') + 1, addresses.length() - 1);
                //Association des adresses avec leur adresse réseau à la VM
                for (String ipAddress : addresses.split(" ")) {
                    if (Network.checkIPv4(ipAddress)) {
                        for (String route : sshCommands.get(vm.getName()).get(commandsToRun[1]))
                            if (route.contains("proto kernel  scope link  src " + ipAddress)) {
                                vm.getIpv4Addresses().add(ipAddress);
                                vm.getIpv4NetworkAddresses().add(route.substring(0, route.indexOf(' ')));
                            }
                    }
                    else if (Network.checkIPv6(ipAddress)) {
                        for (String route : sshCommands.get(vm.getName()).get(commandsToRun[2]))
                            if (route.contains("proto kernel") && route.contains(ipAddress.substring(0,ipAddress.indexOf("::")))) {
                                vm.getIpv6Addresses().add(ipAddress);
                                vm.getIpv6NetworkAddresses().add(route.substring(0, route.indexOf(' ')));
                            }
                    }
                }
                System.out.println("# Initialisation des adresses de " + vm.getName() + " terminée.\r");
            });
        }
        waitTasksToFinish();
    }

    /**
     * Exécuter une série de commandes en SSH sur la VM
     * @param vm machine virtuelle
     */
    public static Map<String,ArrayList<String>> execSSH(VM vm, String... commands) {
        if (vm.getId() == null) {
            System.err.println("ERREUR : ID de " + vm.getName() + " irrécupérable.\r");
            System.exit(1);
        }
        String sshCommand = "vagrant ssh " + vm.getId() + " -c \"";
        for (String command : commands)
            sshCommand = sshCommand.concat(command + ";echo NEXT;");
        sshCommand = sshCommand.concat("\"");
        ArrayList<String> sshOutput = Command.execCommand(sshCommand);
        Map<String,ArrayList<String>> commandsOutput = new LinkedHashMap<>();
        for (String command : commands)
            commandsOutput.put(command,new ArrayList<>());
        int commandNumber = 0;
        for (String output : sshOutput) {
            if (output.equals("NEXT")) ++commandNumber;
            else commandsOutput.get(commands[commandNumber]).add(output);
        }
        return commandsOutput;
    }

    /**
     * Exécuter une tâche de manière parallèle
     * @param runnable exécutable
     */
    public static void execParallelTask(Runnable runnable) {
        if (executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(4);
            service = new ExecutorCompletionService<>(executor);
        }
        tasks.add(service.submit(runnable,null));
    }

    /**
     * Attendre que les tâches en cours se terminent
     */
    public static void waitTasksToFinish() {
        while (!tasks.isEmpty())
            tasks.removeIf(Future::isDone);
        executor.shutdown();
    }

    // GETTERS //

    public static ArrayList<VM> getVms() {
        return vms;
    }
}
