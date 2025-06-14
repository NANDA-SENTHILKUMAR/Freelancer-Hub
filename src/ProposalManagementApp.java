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

public class ProposalManagementApp extends JFrame {
    private JTextField jobIdField, bidAmountField, proposalIdField;
    private JTextArea coverLetterArea, proposalListArea;
    private JButton submitProposalButton, withdrawProposalButton, viewJobsButton;
    private Connection connection;
    private String username; // Store the username directly

    public ProposalManagementApp(String username) {
        this.username = username; // Set the username
        setTitle("Proposal Management");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // UI setup
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Job ID field (for submitting a new proposal)
        JLabel jobIdLabel = new JLabel("Job ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(jobIdLabel, gbc);

        jobIdField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(jobIdField, gbc);

        // Cover Letter field
        JLabel coverLetterLabel = new JLabel("Cover Letter:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(coverLetterLabel, gbc);

        coverLetterArea = new JTextArea(5, 20);
        gbc.gridx = 1;
        panel.add(new JScrollPane(coverLetterArea), gbc);

        // Bid Amount field
        JLabel bidAmountLabel = new JLabel("Bid Amount:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(bidAmountLabel, gbc);

        bidAmountField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(bidAmountField, gbc);

        // Submit button
        submitProposalButton = new JButton("Submit Proposal");
        submitProposalButton.addActionListener(e -> submitProposal());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(submitProposalButton, gbc);

        // Proposal ID field (for withdrawing a proposal)
        JLabel proposalIdLabel = new JLabel("Proposal ID to Withdraw:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(proposalIdLabel, gbc);

        proposalIdField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(proposalIdField, gbc);

        // Withdraw button
        withdrawProposalButton = new JButton("Withdraw Proposal");
        withdrawProposalButton.addActionListener(e -> withdrawProposal());
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(withdrawProposalButton, gbc);

        // View Jobs button
        viewJobsButton = new JButton("View Available Jobs");
        viewJobsButton.addActionListener(e -> viewJobs());
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(viewJobsButton, gbc);

        // Text area to display submitted proposals
        proposalListArea = new JTextArea(15, 40);
        proposalListArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(proposalListArea);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        connectToDatabase();
        loadSubmittedProposals(); // Load proposals when the app starts

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

    // Load all submitted proposals for the freelancer using username
    private void loadSubmittedProposals() {
        try {
            String query = "SELECT p.proposal_id, j.title, p.bid_amount, p.cover_letter " +
                    "FROM proposals p JOIN jobs j ON p.job_id = j.job_id " +
                    "JOIN users u ON p.freelancer_id = u.user_id WHERE u.username = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            StringBuilder proposals = new StringBuilder();
            proposals.append("Submitted Proposals:\n-------------------------\n");

            while (rs.next()) {
                proposals.append("Proposal ID: ").append(rs.getInt("proposal_id"))
                        .append("\nJob Title: ").append(rs.getString("title"))
                        .append("\nBid Amount: $").append(rs.getDouble("bid_amount"))
                        .append("\nCover Letter: ").append(rs.getString("cover_letter"))
                        .append("\n-------------------------\n");
            }

            proposalListArea.setText(proposals.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load proposals.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Submit a new proposal
    private void submitProposal() {
        String jobIdStr = jobIdField.getText();
        String coverLetter = coverLetterArea.getText();
        String bidAmountStr = bidAmountField.getText();

        if (jobIdStr.isEmpty() || coverLetter.isEmpty() || bidAmountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int jobId = Integer.parseInt(jobIdStr);
            double bidAmount = Double.parseDouble(bidAmountStr);

            // Check if a proposal already exists for this freelancer and job
            String checkQuery = "SELECT COUNT(*) FROM proposals WHERE job_id = ? AND freelancer_id = (SELECT user_id FROM users WHERE username = ?)";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, jobId);
            checkStmt.setString(2, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                JOptionPane.showMessageDialog(this, "You have already submitted a proposal for this job.", "Submission Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert the new proposal if no existing one is found
            String insertQuery = "INSERT INTO proposals (job_id, freelancer_id, cover_letter, bid_amount,status) " +
                    "SELECT ?, user_id, ?, ? ,? FROM users WHERE username = ?";

            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            insertStmt.setInt(1, jobId);
            insertStmt.setString(2, coverLetter);
            insertStmt.setDouble(3, bidAmount);
            insertStmt.setString(4, "pending");
            insertStmt.setString(5, username);
            

            int rowsInserted = insertStmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Proposal submitted successfully!");
                loadSubmittedProposals();  // Refresh the list after submission
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit proposal.", "Submission Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to submit proposal.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Withdraw a specific proposal by proposalId
    private void withdrawProposal() {
        String proposalIdStr = proposalIdField.getText();
        if (proposalIdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Proposal ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int proposalId = Integer.parseInt(proposalIdStr);

            String query = "DELETE FROM proposals WHERE proposal_id = ? " +
                    "AND freelancer_id = (SELECT user_id FROM users WHERE username = ? LIMIT 1)";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, proposalId);
            stmt.setString(2, username);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Proposal withdrawn successfully!");
                loadSubmittedProposals(); // Refresh the list after withdrawal
            } else {
                JOptionPane.showMessageDialog(this, "Failed to withdraw proposal. Please check the Proposal ID.", "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to withdraw proposal.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to view available jobs
    private void viewJobs() {
        JFrame jobsFrame = new JFrame("Available Jobs");
        jobsFrame.setSize(400, 300);
        jobsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jobsFrame.setLocationRelativeTo(this);

        JTextArea jobsArea = new JTextArea();
        jobsArea.setEditable(false);

        // Query to fetch available jobs
        try {
            String query = "SELECT job_id, title FROM jobs WHERE job_id NOT IN (SELECT job_id FROM proposals WHERE freelancer_id = (SELECT user_id FROM users WHERE username = ?))";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            StringBuilder jobsList = new StringBuilder("Available Jobs:\n-----------------\n");
            while (rs.next()) {
                jobsList.append("Job ID: ").append(rs.getInt("job_id"))
                        .append(" - Title: ").append(rs.getString("title"))
                        .append("\n");
            }
            jobsArea.setText(jobsList.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch available jobs.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        jobsFrame.add(new JScrollPane(jobsArea));
        jobsFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new ProposalManagementApp("username"); // Replace "username" with actual username passed during instantiation
    }
}
