import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public class ClientHomePage extends JFrame {

    private JLabel backgroundLabel;
    private JButton jobManagementButton, proposalManagementButton, contractManagementButton;
    private JButton paymentButton, communicationButton, logoutButton;
    private String username;

    public ClientHomePage(String username) {
        // Use ToolTipManager for tooltip settings
        this.username = username; // Store the username
        ToolTipManager.sharedInstance().setInitialDelay(10); // Delay before showing tooltip
        ToolTipManager.sharedInstance().setDismissDelay(5000); // Time in milliseconds before tooltip disappears

        // Set global tooltip appearance using UIManager
        UIManager.put("ToolTip.font", new Font("Arial", Font.PLAIN, 18));
        UIManager.put("ToolTip.background", Color.LIGHT_GRAY);
        UIManager.put("ToolTip.foreground", Color.BLACK);
        setTitle("Freelancer Hub - Client Home");
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

        // Add a listener to resize the background image as per window size
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Image img = backgroundIcon.getImage();
                Image scaledImg = img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImg));
            }
        });

        add(backgroundLabel, BorderLayout.CENTER);

        // Create the panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Make the panel transparent
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel titleLabel = new JLabel("CLIENT HUB");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        buttonPanel.add(titleLabel, gbc);

        // Create and add the buttons with tooltips
        jobManagementButton = new JButton("JOB MANAGEMENT");
        jobManagementButton.setFont(new Font("Arial", Font.PLAIN, 18));
        jobManagementButton.setToolTipText("Post Job, My Job Listings, Edit Job Postings");
        gbc.gridy = 1;
        buttonPanel.add(jobManagementButton, gbc);

        proposalManagementButton = new JButton("PROPOSAL MANAGEMENT");
        proposalManagementButton.setFont(new Font("Arial", Font.PLAIN, 18));
        proposalManagementButton.setToolTipText("View Proposals, Accept/Reject Proposals");
        gbc.gridy = 2;
        buttonPanel.add(proposalManagementButton, gbc);

        contractManagementButton = new JButton("CONTRACT MANAGEMENT");
        contractManagementButton.setFont(new Font("Arial", Font.PLAIN, 18));
        contractManagementButton.setToolTipText("Manage Contracts, End Contracts");
        gbc.gridy = 3;
        buttonPanel.add(contractManagementButton, gbc);

        paymentButton = new JButton("PAYMENT AND INVOICING");
        paymentButton.setFont(new Font("Arial", Font.PLAIN, 18));
        paymentButton.setToolTipText("Payment approvals, payment history");
        gbc.gridy = 4;
        buttonPanel.add(paymentButton, gbc);

        communicationButton = new JButton("COMMUNICATION");
        communicationButton.setFont(new Font("Arial", Font.PLAIN, 18));
        communicationButton.setToolTipText("Message Freelancer, Reviews");
        gbc.gridy = 5;
        buttonPanel.add(communicationButton, gbc);

        // Logout Button
        logoutButton = new JButton("LOGOUT");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 18));
        logoutButton.setToolTipText("Logout and return to login page.");
        gbc.gridy = 6; // Adjust this to position it correctly
        buttonPanel.add(logoutButton, gbc);

        // Set preferred size for buttons to ensure tooltips are visible
        jobManagementButton.setPreferredSize(new Dimension(300, 50));
        proposalManagementButton.setPreferredSize(new Dimension(300, 50));
        contractManagementButton.setPreferredSize(new Dimension(300, 50));
        paymentButton.setPreferredSize(new Dimension(300, 50));
        communicationButton.setPreferredSize(new Dimension(300, 50));
        logoutButton.setPreferredSize(new Dimension(300, 50));

        // Add action listeners for the buttons
        jobManagementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new JobPostingApp(username); // Opens the Job Posting App
            }
        });

        proposalManagementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                new ClientProposalManagementPage(username); // Opens the Job Posting App
                
            }
        });

        contractManagementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Open Contract Management
                new ClientContractManagementPage(username); // Pass only the username

            }
        });

        paymentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new PaymentHistoryPage(username);
            }
        });

        communicationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Open Communication
                new MessagingApp(username); // Pass only the usernam
            }
        });

        // Add action listener for the logout button
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FullScreenLoginPage(); // Return to login page
                dispose(); // Close the current window
            }
        });

        // Add button panel to the background image label
        backgroundLabel.add(buttonPanel);
    }

    public static void main(String[] args) {
        String user = "test_user"; // Replace with actual username from login
        new ClientHomePage(user);
    }
}
