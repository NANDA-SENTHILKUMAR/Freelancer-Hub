import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ClientContractManagementPage extends JFrame {
    private JTable contractsTable;
    private DefaultTableModel tableModel;
    private JButton payButton, submitReviewButton,readReviewButton;
    private Connection connection;
    private String clientUsername;

    public ClientContractManagementPage(String clientUsername) {
        this.clientUsername = clientUsername;
        setTitle("Client Contract Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table for contracts
        String[] columnNames = {"Contract ID", "Job Title", "Freelancer", "Status", "Payment Requested"};
        tableModel = new DefaultTableModel(columnNames, 0);
        contractsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(contractsTable);

        // Buttons
        payButton = new JButton("Make Payment");
        submitReviewButton = new JButton("Submit Review");
        readReviewButton = new JButton("Read Reviews");

        payButton.addActionListener(new PayAction());
        submitReviewButton.addActionListener(new SubmitReviewAction());
        readReviewButton.addActionListener(new ReadReviewAction());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(payButton);
        buttonPanel.add(submitReviewButton);
        buttonPanel.add(readReviewButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        connectToDatabase();
        loadContracts(clientUsername);
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

    private void loadContracts(String clientUsername) {
        try {
            String query = "SELECT c.contract_id, j.title, u.username, c.status, c.payment_requested " +
                           "FROM contracts c " +
                           "JOIN jobs j ON c.job_id = j.job_id " +
                           "JOIN users u ON c.freelancer_id = u.user_id " +
                           "WHERE c.client_id = (SELECT user_id FROM users WHERE username = ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, clientUsername);
            ResultSet rs = stmt.executeQuery();

            // Clear existing rows
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("contract_id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getBoolean("payment_requested")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load contracts.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class PayAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                boolean paymentRequested = (boolean) tableModel.getValueAt(selectedRow, 4);
                String freelancerUsername = (String) tableModel.getValueAt(selectedRow, 2); // Get the freelancer's username
                
                if (!paymentRequested) {
                    JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Payment has not been requested yet.");
                    return;
                }
    
                // Prompt for payment amount
                String amountStr = JOptionPane.showInputDialog(ClientContractManagementPage.this, "Enter the amount to be paid:");
                if (amountStr != null) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Please enter a valid amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
    
                        // Confirm the freelancer's username
                        String freelancerConfirmation = JOptionPane.showInputDialog(ClientContractManagementPage.this,
                                "Confirm the freelancer's username:", freelancerUsername);
                        if (freelancerConfirmation != null && freelancerConfirmation.equals(freelancerUsername)) {
                            int confirm = JOptionPane.showConfirmDialog(ClientContractManagementPage.this,
                                    "You are about to pay " + amount + " to freelancer " + freelancerUsername + " for contract ID " + contractId + ".\n" +
                                    "Is this correct?",
                                    "Confirm Payment",
                                    JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                // Retrieve the client ID and freelancer ID for the payment entry
                                int clientId = getUserIdByUsername(clientUsername); // Assuming you have this method to fetch client ID
                                int freelancerId = getUserIdByUsername(freelancerUsername); // Fetch freelancer ID
    
                                // Insert payment details into the payments table
                                // Insert payment details into the payments table


                                String insertQuery = "INSERT INTO payments (contract_id, amount, status) VALUES (?, ?, ?)";
                                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                                    insertStmt.setInt(1, contractId); // contract_id is already in the payments table
                                    insertStmt.setDouble(2, amount);
                                    insertStmt.setString(3,"completed");  // amount for the payment
                                    insertStmt.executeUpdate();
                                }
    
                                // Update payment status in the contracts table or as needed
                                String updateQuery = "UPDATE contracts SET status = 'Paid' WHERE contract_id = ?";
                                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                    updateStmt.setInt(1, contractId);
                                    updateStmt.executeUpdate();
                                }
    
                                JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Payment made successfully!");
                                loadContracts(clientUsername); // Refresh the contract list
                            }
                        } else {
                            JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Freelancer username does not match.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Failed to process payment.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Please select a contract to make a payment.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    
        // Example method to get user ID by username
        private int getUserIdByUsername(String username) {
            int userId = -1; // Default value for not found
            String query = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return userId;
        }
    }
    
    private class SubmitReviewAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                String reviewText = JOptionPane.showInputDialog(ClientContractManagementPage.this, "Enter your review:");

                if (reviewText != null && !reviewText.trim().isEmpty()) {
                    try {
                        String insertReviewQuery = "INSERT INTO reviews (contract_id, reviewer_id, review_text, rating) " +
                                                   "VALUES (?, (SELECT user_id FROM users WHERE username = ?), ?, ?)";
                        PreparedStatement stmt = connection.prepareStatement(insertReviewQuery);
                        stmt.setInt(1, contractId);
                        stmt.setString(2, clientUsername);
                        stmt.setString(3, reviewText);
                        stmt.setInt(4, 5);  // Assuming 5-star rating
                        stmt.executeUpdate();

                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Review submitted successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Failed to submit review.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Please select a contract to submit a review.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private class ReadReviewAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                StringBuilder reviews = new StringBuilder();
    
                try {
                    String query = "SELECT review_text FROM reviews WHERE contract_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(query);
                    stmt.setInt(1, contractId);
                    ResultSet rs = stmt.executeQuery();
    
                    while (rs.next()) {
                        reviews.append(rs.getString("review_text")).append("\n");
                    }
    
                    if (reviews.length() == 0) {
                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, "No reviews available for this contract.");
                    } else {
                        JOptionPane.showMessageDialog(ClientContractManagementPage.this, reviews.toString(), "Reviews", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Failed to load reviews.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(ClientContractManagementPage.this, "Please select a contract to read reviews.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    public static void main(String[] args) {
        new ClientContractManagementPage("client1");
    }
}
