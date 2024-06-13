import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class MainClient extends JFrame {
    private JPanel messagePanel;
    private JScrollPane scrollPane;
    private JTextField textField;
    private JButton sendButton;
    private XmlRpcClient client;
    private String username;
    private JLabel avatarLabel;
    private ImageIcon selectedAvatar;
    private JLabel usernameLabel;

    private static final String ASSETS_PATH = "/Users/halima/Desktop/School/RMI_RPC/Chat/mini_chat_RPC/assets/";

    public MainClient(String serverUrl) {
        super("XML-RPC Chat Client");

        setSize(720, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        username = JOptionPane.showInputDialog(this, "Entrez votre nom d'utilisateur :");
        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nom d'utilisateur requis pour rejoindre le chat.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        selectedAvatar = chooseAvatar();
        avatarLabel = new JLabel(
                new ImageIcon(selectedAvatar.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
        usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));

        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(serverUrl));
            client = new XmlRpcClient();
            client.setConfig(config);
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this, "Erreur de configuration : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
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
                Vector<String> params = new Vector<>();
                params.add(username);
                params.add(message);
                client.execute("chat.sendMessage", params);

                textField.setText("");
                addMessageToPanel(username, message, true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur d'envoi : " + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void refreshMessages() {
        try {
            Vector<String> params = new Vector<>();
            Object[] result = (Object[]) client.execute("chat.getMessages", params);
            messagePanel.removeAll();

            for (Object msg : result) {
                String[] parts = msg.toString().split(":", 2);
                if (parts.length == 2) {
                    String sender = parts[0].trim();
                    String message = parts[1].trim();
                    boolean isSentByCurrentUser = sender.equals(username);
                    addMessageToPanel(sender, message, isSentByCurrentUser);
                }
            }

            messagePanel.revalidate();
            messagePanel.repaint();
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de réception des messages : " + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addMessageToPanel(String sender, String message, boolean isSentByCurrentUser) {
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BoxLayout(messageBubble, BoxLayout.Y_AXIS));

        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        senderLabel.setForeground(isSentByCurrentUser ? new Color(0, 128, 0) : new Color(0, 0, 139));

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

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(messageBubble, isSentByCurrentUser ? BorderLayout.EAST : BorderLayout.WEST);
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        messagePanel.add(wrapper);
    }

    private ImageIcon chooseAvatar() {
        String[] avatars = { "male.png", "male1.png", "female.png", "female1.png" };
        JDialog avatarDialog = new JDialog(this, "Choisissez votre avatar", true);
        avatarDialog.setLayout(new FlowLayout());

        JButton[] avatarButtons = new JButton[avatars.length];
        ImageIcon[] avatarIcons = new ImageIcon[avatars.length];

        for (int i = 0; i < avatars.length; i++) {
            avatarIcons[i] = loadAvatarIcon(avatars[i], avatars[i].replace(".png", ""));
            avatarButtons[i] = new JButton(
                    new ImageIcon(avatarIcons[i].getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
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

    private ImageIcon loadAvatarIcon(String filename, String description) {
        File file = new File(ASSETS_PATH + filename);
        if (!file.exists()) {
            throw new RuntimeException("Ressource non trouvée : " + file.getAbsolutePath());
        }
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        icon.setDescription(description);
        return icon;
    }

    public static void main(String[] args) {
        new MainClient("http://localhost:8080/RPC2");
    }
}
