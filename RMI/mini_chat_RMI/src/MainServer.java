import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            ChatServer server = new ChatServer();
            Naming.rebind("rmi://localhost/ChatService", server);
            System.out.println("Serveur RMI en cours d'exécution...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
