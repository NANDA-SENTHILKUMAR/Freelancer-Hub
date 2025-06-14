import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ProposalSubmissionApp extends JFrame {
    private JTextField bidAmountField, usernameField;
    private JTextArea coverLetterArea;
    private JButton submitProposalButton;
    private JTextArea appliedProposalsArea; // Text area to display applied proposals
    private int jobId;
    private Connection connection;

    public ProposalSubmissionApp(int jobId) {
        this.jobId = jobId; // Store the job ID for submitting a proposal
        setTitle("Submit Proposal");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI setup
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Job ID (non-editable)
        JLabel jobIdLabel = new JLabel("Job ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(jobIdLabel, gbc);

        JTextField jobIdField = new JTextField(20);
        jobIdField.setText(String.valueOf(jobId));
        jobIdField.setEditable(false);
        gbc.gridx = 1;
        panel.add(jobIdField, gbc);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Cover Letter
        JLabel coverLetterLabel = new JLabel("Cover Letter:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(coverLetterLabel, gbc);

        coverLetterArea = new JTextArea(5, 20);
        gbc.gridx = 1;
        panel.add(new JScrollPane(coverLetterArea), gbc);

        // Bid Amount
        JLabel bidAmountLabel = new JLabel("Bid Amount:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(bidAmountLabel, gbc);

        bidAmountField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(bidAmountField, gbc);

        // Submit Button
        submitProposalButton = new JButton("Submit Proposal");
        submitProposalButton.addActionListener(e -> submitProposal());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(submitProposalButton, gbc);

        // Text area to display applied proposals
        appliedProposalsArea = new JTextArea(10, 50);
        appliedProposalsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(appliedProposalsArea);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        connectToDatabase();
        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/freelance_work_arena", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitProposal() {
        String username = usernameField.getText();
        String coverLetter = coverLetterArea.getText();
        String bidAmount = bidAmountField.getText();

        if (username.isEmpty() || coverLetter.isEmpty() || bidAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int freelancerId = getFreelancerId(username);
            String query = "INSERT INTO proposals (job_id, freelancer_id, cover_letter, bid_amount,status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, jobId);
            stmt.setInt(2, freelancerId);
            stmt.setString(3, coverLetter);
            stmt.setDouble(4, Double.parseDouble(bidAmount));
            stmt.setString(5, "pending");

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Proposal submitted successfully!");
                showAppliedProposals(freelancerId); // Show applied proposals after submission
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit proposal.", "Submission Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit proposal. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to get the freelancer's ID based on their username
    private int getFreelancerId(String username) throws SQLException {
        String query = "SELECT user_id FROM users WHERE username = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("user_id");
        } else {
            throw new SQLException("Freelancer not found");
        }
    }

    // Method to show applied proposals for the freelancer
    private void showAppliedProposals(int freelancerId) {
        try {
            String query = "SELECT p.proposal_id, j.title, p.bid_amount, p.cover_letter " +
                           "FROM proposals p JOIN jobs j ON p.job_id = j.job_id " +
                           "WHERE p.freelancer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, freelancerId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder appliedProposals = new StringBuilder();
            appliedProposals.append("Your Applied Proposals:\n-------------------------\n");

            while (rs.next()) {
                appliedProposals.append("Proposal ID: ").append(rs.getInt("proposal_id"))
                                .append("\nJob Title: ").append(rs.getString("title"))
                                .append("\nBid Amount: $").append(rs.getDouble("bid_amount"))
                                .append("\nCover Letter: ").append(rs.getString("cover_letter"))
                                .append("\n-------------------------\n");
            }

            appliedProposalsArea.setText(appliedProposals.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load applied proposals.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new ProposalSubmissionApp(1); // Example job ID
    }
}