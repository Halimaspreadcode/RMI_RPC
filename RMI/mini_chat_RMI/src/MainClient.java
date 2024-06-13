import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MainClient extends JFrame {
    private JPanel messagePanel;
    private JScrollPane scrollPane;
    private JTextField textField;
    private JButton sendButton;
    private ChatInterface chat;
    private String username;
    private JLabel avatarLabel;
    private ImageIcon selectedAvatar;
    private JLabel usernameLabel;

    private static final String ASSETS_PATH = "/Users/halima/Desktop/School/RMI_RPC/RMI/mini_chat_RMI/assets/";

    public MainClient() {
        super("RMI Chat Client");

        setSize( 720, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        username = JOptionPane.showInputDialog(this, "Entrez votre nom d'utilisateur :");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom d'utilisateur requis pour rejoindre le chat.", "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        selectedAvatar = chooseAvatar();
        avatarLabel = new JLabel(new ImageIcon(selectedAvatar.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
        usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        initializeRMI();
        try {
            chat.login(username, selectedAvatar.getDescription());
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        createGUI();
        refreshMessages();

        new Thread(() -> {
            while (true) {
                try {
                    refreshMessages();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initializeRMI() {
        try {
            chat = (ChatInterface) Naming.lookup("rmi://localhost/ChatService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur RMI : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // Ouvrir à tous les utilisateurs
    // private void initializeRMI() {
    //     try {
    //         // Remplacer par l'adresse IP 
    //         chat = (ChatInterface) Naming.lookup("rmi://192.168.x.x/ChatService");
    //     } catch (Exception e) {
    //         JOptionPane.showMessageDialog(this, "Erreur RMI : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    //         e.printStackTrace();
    //         System.exit(1);
    //     }
    // }
    

    private void createGUI() {
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        textField = new JTextField(40);
        sendButton = new JButton("Envoyer");
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        avatarPanel.setBackground(new Color(200, 220, 240));
        avatarPanel.add(avatarLabel);
        avatarPanel.add(usernameLabel);

        add(avatarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            try {
                chat.sendMessage(username, message);
                textField.setText("");
                addMessageToPanel(username, message, true);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, "Erreur d'envoi : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Méthode pour rafraîchir les messages affichés
    private void refreshMessages() {
        try {
            String allMessages = chat.receiveMessages();
            List<String> messages = parseMessages(allMessages);

            // Effacer le contenu actuel du panneau
            messagePanel.removeAll();

            // Ajouter chaque message au panneau
            for (String message : messages) {
                String[] parts = message.split(":", 2);
                if (parts.length == 2) {
                    String sender = parts[0].trim();
                    String msg = parts[1].trim();
                    boolean isSentByCurrentUser = sender.equals(username);
                    addMessageToPanel(sender, msg, isSentByCurrentUser);
                }
            }

            // Repeindre et faire défiler vers le bas
            messagePanel.revalidate();
            messagePanel.repaint();
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Erreur de réception des messages : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Méthode pour ajouter les messages au panneau
    private void addMessageToPanel(String sender, String message, boolean isSentByCurrentUser) {
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BoxLayout(messageBubble, BoxLayout.Y_AXIS));

        // Nom de l'expéditeur
        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        senderLabel.setForeground(isSentByCurrentUser ? new Color(0, 128, 0) : new Color(0, 0, 139));

        // Message dans une bulle
        JTextArea messageText = new JTextArea(message);
        messageText.setEditable(false);
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setFont(new Font("Arial", Font.PLAIN, 14));
        messageText.setBackground(isSentByCurrentUser ? new Color(144, 238, 144) : new Color(173, 216, 230));
        messageText.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        messageBubble.add(senderLabel);
        messageBubble.add(messageText);
        messageBubble.setAlignmentX(isSentByCurrentUser ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);

        // Espacement entre les messages
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(messageBubble, isSentByCurrentUser ? BorderLayout.EAST : BorderLayout.WEST);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messagePanel.add(wrapper);
    }

    // Méthode pour parser les messages reçus en liste
    private List<String> parseMessages(String allMessages) {
        List<String> messages = new ArrayList<>();
        String[] lines = allMessages.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                messages.add(line);
            }
        }
        return messages;
    }

    // Méthode pour afficher le dialogue de sélection d'avatar
    private ImageIcon chooseAvatar() {
        String[] avatars = {"male.png", "male1.png", "female.png", "female1.png"};
        JDialog avatarDialog = new JDialog(this, "Choisissez votre avatar", true);
        avatarDialog.setLayout(new FlowLayout());

        JButton[] avatarButtons = new JButton[avatars.length];
        ImageIcon[] avatarIcons = new ImageIcon[avatars.length];

        for (int i = 0; i < avatars.length; i++) {
            avatarIcons[i] = loadAvatarIcon(avatars[i], avatars[i].replace(".png", ""));
            avatarButtons[i] = new JButton(new ImageIcon(avatarIcons[i].getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
            final int index = i;
            avatarButtons[i].addActionListener(e -> {
                avatarDialog.dispose();
                selectedAvatar = avatarIcons[index];
            });
            avatarDialog.add(avatarButtons[i]);
        }

        avatarDialog.pack();
        avatarDialog.setVisible(true);

        return selectedAvatar;
    }

    // Méthode pour charger un avatar depuis les ressources
    private ImageIcon loadAvatarIcon(String filename, String description) {
        File file = new File(ASSETS_PATH + filename);
        if (!file.exists()) {
            throw new RuntimeException("Ressource non trouvée : " + file.getAbsolutePath());
        }
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        icon.setDescription(description);
        return icon;
    }

    // Méthode principale pour lancer le client
    public static void main(String[] args) {
        new MainClient();
    }
}
