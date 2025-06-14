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
import java.sql.ResultSet;
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

public class FullScreenLoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JButton loginButton, registerButton, forgotPasswordButton;
    private JLabel backgroundLabel;

    // Database connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/freelance_work_arena";  // Update with your DB name
    private final String DB_USER = "root";  // Update with your DB username
    private final String DB_PASSWORD = "root";  // Update with your DB password

    public FullScreenLoginPage() {
        setTitle("Freelance Hub");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full-screen mode with title bar
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Load and set the background image
        ImageIcon backgroundIcon = new ImageIcon("C:\\Users\\nanda\\OneDrive\\Desktop\\Background_pic.jpeg");
        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(new GridBagLayout());

        // Resize the background image as per window size
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Image img = backgroundIcon.getImage();
                Image scaledImg = img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImg));
            }
        });

        add(backgroundLabel, BorderLayout.CENTER);

        // Create the panel for the login components
        JPanel loginPanel = new JPanel();
        loginPanel.setOpaque(false); // Transparent
        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title label
        JLabel titleLabel = new JLabel("FREELANCE HUB");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 20);
        loginPanel.add(titleLabel, gbc);

        // User Type field
        JLabel userTypeLabel = new JLabel("USER TYPE:");
        userTypeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        userTypeLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(userTypeLabel, gbc);

        userTypeComboBox = new JComboBox<>(new String[]{"freelancer", "client"});
        gbc.gridx = 1;
        loginPanel.add(userTypeComboBox, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("USERNAME:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("PASSWORD:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        // Login button
        loginButton = new JButton("LOGIN");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(loginButton, gbc);

        // Forgot Password button
        forgotPasswordButton = new JButton("FORGOT PASSWORD?");
        gbc.gridy = 5;
        loginPanel.add(forgotPasswordButton, gbc);

        // Register button
        registerButton = new JButton("DON'T HAVE AN ACCOUNT? REGISTER");
        gbc.gridy = 6;
        loginPanel.add(registerButton, gbc);

        // Add login panel to the background image label
        backgroundLabel.add(loginPanel);

        // Adding ActionListeners to the buttons
        addListeners();
    }

    // Method to add action listeners for login and register buttons
    private void addListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String userType = (String) userTypeComboBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (validateLogin(username, password, userType)) {
                        // Redirect based on the user type
                        if (userType.equals("freelancer")) {
                            new FreelancerHomePage(username);  // Redirect to FreelancerHomePage
                        } else if (userType.equals("client")) {
                            new ClientHomePage(username);  // Redirect to ClientHomePage
                        }
                        dispose();  // Close the login page after redirecting
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegistrationPage(); // Open the registration page
                dispose(); // Close the login page
            }
        });

        forgotPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter your username", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    String newPassword = JOptionPane.showInputDialog("Enter new password:"); // Prompt the user for a new password
                    if (newPassword != null && !newPassword.isEmpty()) {
                        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                            String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                            updateStmt.setString(1, newPassword);
                            updateStmt.setString(2, username);
        
                            int rowsUpdated = updateStmt.executeUpdate();
                            if (rowsUpdated > 0) {
                                JOptionPane.showMessageDialog(null, "Password updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Database error occurred while updating password.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    // Method to validate login credentials using JDBC
    private boolean validateLogin(String username, String password, String userType) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND user_type = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, userType);
            ResultSet rs = stmt.executeQuery();

            return rs.next();  // Return true if login is successful
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;  // Login failed
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FullScreenLoginPage());
    }
}
