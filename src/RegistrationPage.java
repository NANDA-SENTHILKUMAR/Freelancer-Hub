import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class RegistrationPage extends JFrame {
    private JTextField usernameField, emailField, phoneNoField; // Added phoneNoField
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JButton registerButton, backButton;

    // Database connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/freelance_work_arena";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "root";

    public RegistrationPage() {
        setTitle("Register - Freelance Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Load and set the background image
        ImageIcon backgroundIcon = new ImageIcon("C:\\Users\\nanda\\OneDrive\\Desktop\\Background_pic.jpeg");
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setLayout(new GridBagLayout());

        // Resize the background image as per window size
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Image img = backgroundIcon.getImage();
                Image scaledImg = img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImg));
            }
        });

        add(backgroundLabel, BorderLayout.WEST);

        // Create the panel for the registration components
        JPanel registerPanel = new JPanel();
        registerPanel.setOpaque(false); // Transparent
        registerPanel.setLayout(new GridBagLayout());
        backgroundLabel.add(registerPanel);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Ensure buttons and text fields fill horizontally

        // Title label
        JLabel titleLabel = new JLabel("REGISTER");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 20);
        registerPanel.add(titleLabel, gbc);

        // User Type field
        JLabel userTypeLabel = new JLabel("USER TYPE:");
        userTypeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userTypeLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        registerPanel.add(userTypeLabel, gbc);

        userTypeComboBox = new JComboBox<>(new String[]{"freelancer", "client"});
        gbc.gridx = 1;
        registerPanel.add(userTypeComboBox, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("USERNAME:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        registerPanel.add(usernameField, gbc);

        // Email field
        JLabel emailLabel = new JLabel("EMAIL:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(emailLabel, gbc);

        emailField = new JTextField(15);
        gbc.gridx = 1;
        registerPanel.add(emailField, gbc);

        // Phone Number field
        JLabel phoneNoLabel = new JLabel("PHONE NO:");
        phoneNoLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        phoneNoLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerPanel.add(phoneNoLabel, gbc);

        phoneNoField = new JTextField(15); // New field for phone number
        gbc.gridx = 1;
        registerPanel.add(phoneNoField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("PASSWORD:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        registerPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        registerPanel.add(passwordField, gbc);

        // Register button
        registerButton = new JButton("REGISTER");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        registerPanel.add(registerButton, gbc);

        // Back button
        backButton = new JButton("BACK TO LOGIN");
        gbc.gridy = 7;
        registerPanel.add(backButton, gbc);

        // Add registration panel to the background image label
        backgroundLabel.add(registerPanel);

        // Adding ActionListeners to the buttons
        addListeners();
    }

    private void addListeners() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String phoneNo = phoneNoField.getText(); // Get phone number
                String password = new String(passwordField.getPassword());
                String userType = (String) userTypeComboBox.getSelectedItem();

                if (username.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    registerUser(username, email, phoneNo, password, userType);
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FullScreenLoginPage(); // Go back to login page
                dispose();
            }
        });
    }

    private void registerUser(String username, String email, String phoneNo, String password, String userType) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO users (username, email, phone_no, password, user_type) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, phoneNo); // Save phone number
            stmt.setString(4, password);
            stmt.setString(5, userType);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                new FullScreenLoginPage(); // Go back to login page
                dispose();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationPage());
    }
}
