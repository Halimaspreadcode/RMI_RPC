import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatServer extends UnicastRemoteObject implements ChatInterface {
    private final List<String> messages;
    private final Map<String, String> users;

    protected ChatServer() throws RemoteException {
        super();
        messages = new ArrayList<>();
        users = new LinkedHashMap<>();
    }

    @Override
    public synchronized void sendMessage(String user, String message) throws RemoteException {
        messages.add(user + ": " + message);
    }

    @Override
    public synchronized String receiveMessages() throws RemoteException {
        return String.join("\n", messages);
    }

    @Override
    public synchronized void login(String username, String avatar) throws RemoteException {
        users.put(username, avatar);
        messages.add(username + " a rejoint le chat.");
    }
}
