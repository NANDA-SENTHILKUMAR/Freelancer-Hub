import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MessagingApp extends JFrame {
    private JTextField receiverUsernameField;
    private JTextArea messageArea, receivedMessagesArea;
    private JButton sendMessageButton;
    private Connection connection;
    private String username; // Store the logged-in user's username
    private int userId; // Store the logged-in user's ID after retrieving it from the database
    private JComboBox<String> userTypeComboBox;

    public MessagingApp(String username) {
        this.username = username; // Set the username
        setTitle("Messaging");
        setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel userTypeLabel = new JLabel("Message Type:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userTypeLabel, gbc);

        // ComboBox for selecting user type (Freelancer or Client)
        userTypeComboBox = new JComboBox<>(new String[]{"Freelancer", "Client"});
        gbc.gridx = 1;
        panel.add(userTypeComboBox, gbc);

        JLabel receiverUsernameLabel = new JLabel("Receiver Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(receiverUsernameLabel, gbc);

        receiverUsernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(receiverUsernameField, gbc);

        JLabel messageLabel = new JLabel("Message:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(messageLabel, gbc);

        messageArea = new JTextArea(5, 20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(new JScrollPane(messageArea), gbc);

        sendMessageButton = new JButton("Send Message");
        sendMessageButton.addActionListener(e -> sendMessage());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(sendMessageButton, gbc);

        JLabel receivedMessagesLabel = new JLabel("Received Messages:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(receivedMessagesLabel, gbc);

        receivedMessagesArea = new JTextArea(10, 40);
        receivedMessagesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(receivedMessagesArea);
        gbc.gridy = 5;
        panel.add(scrollPane, gbc);

        add(panel);
        connectToDatabase();
        retrieveUserId(); // Retrieve the userId based on the username
        loadReceivedMessages(); // Load received messages on startup
        setVisible(true);
    }

    // Connect to the database
    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/freelance_work_arena", "root", "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Retrieve the userId using the username
    private void retrieveUserId() {
        try {
            String query = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                userId = resultSet.getInt("user_id");
            } else {
                JOptionPane.showMessageDialog(this, "User ID not found for the username: " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to retrieve user ID.");
        }
    }

    private void sendMessage() {
        String receiverUsername = receiverUsernameField.getText();
        String messageText = messageArea.getText();
        String userType = (String) userTypeComboBox.getSelectedItem();

        if (receiverUsername.isEmpty() || messageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.");
            return;
        }

        try {
            String query = "SELECT user_id FROM users WHERE username = ? AND user_type = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, receiverUsername);
            preparedStatement.setString(2, userType.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int receiverId = resultSet.getInt("user_id");

                String sql = "INSERT INTO messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
                PreparedStatement messageStatement = connection.prepareStatement(sql);
                messageStatement.setInt(1, userId); // Use the logged-in user's ID
                messageStatement.setInt(2, receiverId);
                messageStatement.setString(3, messageText);
                messageStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Message sent successfully!");
                loadReceivedMessages(); // Refresh received messages after sending
            } else {
                JOptionPane.showMessageDialog(this, "Receiver not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send message.");
        }
    }

    private void loadReceivedMessages() {
        try {
            String query = "SELECT m.message_text, u.username FROM messages m " +
                           "JOIN users u ON m.sender_id = u.user_id " +
                           "WHERE m.receiver_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId); // Use the logged-in user's ID
            ResultSet resultSet = preparedStatement.executeQuery();

            StringBuilder receivedMessages = new StringBuilder();
            receivedMessages.append("Messages:\n");

            while (resultSet.next()) {
                receivedMessages.append("From: ").append(resultSet.getString("username"))
                                .append("\nMessage: ").append(resultSet.getString("message_text"))
                                .append("\n-------------------------\n");
            }

            receivedMessagesArea.setText(receivedMessages.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load received messages.");
        }
    }

    public static void main(String[] args) {
        new MessagingApp("test_user"); // Example username
    }
}