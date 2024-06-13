import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private List<String> messages = new ArrayList<>();

    // Méthode pour envoyer un message
    public void sendMessage(String username, String message) {
        messages.add(username + ": " + message);
        System.out.println("Message reçu de " + username + ": " + message);
    }

    // Méthode pour obtenir les messages
    public String[] getMessages() {
        return messages.toArray(new String[0]);
    }

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer(8080);
            XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            
            // Enregistrement du gestionnaire "chat"
            phm.addHandler("chat", ChatServer.class);
            xmlRpcServer.setHandlerMapping(phm);

            XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
            serverConfig.setEnabledForExtensions(true);
            serverConfig.setContentLengthOptional(false);

            server.start();
            System.out.println("Chat server started successfully.");
        } catch (Exception e) {
            System.err.println("Chat server: " + e.getMessage());
        }
    }
}
