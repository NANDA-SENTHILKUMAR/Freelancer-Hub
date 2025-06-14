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

public class FreelancerHomePage extends JFrame {
    private JLabel backgroundLabel;
    private JButton jobDetailsButton, contractsButton, proposalManagementButton;
    private JButton paymentHistoryButton, chatWithClientsButton;
    private String username; // Store the freelancer's username

    public FreelancerHomePage(String username) {
        this.username = username; // Store the username
        ToolTipManager.sharedInstance().setInitialDelay(10);
        ToolTipManager.sharedInstance().setDismissDelay(5000);
        UIManager.put("ToolTip.font", new Font("Arial", Font.PLAIN, 18));
        UIManager.put("ToolTip.background", Color.LIGHT_GRAY);
        UIManager.put("ToolTip.foreground", Color.BLACK);
        setTitle("Freelancer Hub - Freelancer Home");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        ImageIcon backgroundIcon = new ImageIcon("C:\\Users\\nanda\\OneDrive\\Desktop\\Background_pic.jpeg");
        backgroundLabel = new JLabel();
        backgroundLabel.setLayout(new GridBagLayout());

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Image img = backgroundIcon.getImage();
                Image scaledImg = img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImg));
            }
        });

        add(backgroundLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("FREELANCER HUB");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        buttonPanel.add(titleLabel, gbc);

        // Create and add the buttons with toolti
        jobDetailsButton = new JButton("JOB DETAILS");
        jobDetailsButton.setFont(new Font("Arial", Font.PLAIN, 18));
        jobDetailsButton.setToolTipText("View Jobs, Search Job, Apply Job, Applied Jobs");
        gbc.gridy = 2;
        buttonPanel.add(jobDetailsButton, gbc);

        contractsButton = new JButton("CONTRACTS");
        contractsButton.setFont(new Font("Arial", Font.PLAIN, 18));
        contractsButton.setToolTipText("Ongoing Contracts, Submit Work, Submit Payment Request");
        gbc.gridy = 3;
        buttonPanel.add(contractsButton, gbc);

        proposalManagementButton = new JButton("PROPOSAL MANAGEMENT");
        proposalManagementButton.setFont(new Font("Arial", Font.PLAIN, 18));
        proposalManagementButton.setToolTipText("View Submitted Proposal, Withdraw Proposal");
        gbc.gridy = 4;
        buttonPanel.add(proposalManagementButton, gbc);

        paymentHistoryButton = new JButton("PAYMENT HISTORY");
        paymentHistoryButton.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 5;
        buttonPanel.add(paymentHistoryButton, gbc);

        chatWithClientsButton = new JButton("CHAT WITH CLIENTS");
        chatWithClientsButton.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 6;
        buttonPanel.add(chatWithClientsButton, gbc);

        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setToolTipText("Logout and return to login page.");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 8;
        buttonPanel.add(logoutButton, gbc);

        // Set preferred size for buttons
        jobDetailsButton.setPreferredSize(new Dimension(300, 50));
        contractsButton.setPreferredSize(new Dimension(300, 50));
        proposalManagementButton.setPreferredSize(new Dimension(300, 50));
        paymentHistoryButton.setPreferredSize(new Dimension(300, 50));
        chatWithClientsButton.setPreferredSize(new Dimension(300, 50));
        logoutButton.setPreferredSize(new Dimension(300, 50));

        // Add action listeners for the buttons
        jobDetailsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FreelancerJobOperationsApp();
            }
        });

        contractsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FreelancerContractManagementPage(username); // Pass the username
                
            }
        });

        proposalManagementButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ProposalManagementApp(username);
                
            }
        });

        paymentHistoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Pass only the username to the PaymentHistoryPage
                new PaymentHistoryPage(username);
            }
        });
        

        chatWithClientsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                new MessagingApp(username); 
                
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new FullScreenLoginPage();
                dispose(); 
            }
        });

        backgroundLabel.add(buttonPanel);
    }

    public static void main(String[] args) {
        String user = "test_user"; 
        new FreelancerHomePage(user);
    }
}
