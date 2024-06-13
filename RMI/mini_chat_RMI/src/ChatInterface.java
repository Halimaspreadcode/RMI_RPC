import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatInterface extends Remote {
    void sendMessage(String user, String message) throws RemoteException;
    String receiveMessages() throws RemoteException;
    void login(String username, String avatar) throws RemoteException;
}
