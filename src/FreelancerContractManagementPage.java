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

public class FreelancerContractManagementPage extends JFrame {
    private JTable contractsTable;
    private DefaultTableModel tableModel;
    private JButton updateStatusButton, requestPaymentButton, submitReviewButton;
    private Connection connection;
    private String freelancerUsername;

    public FreelancerContractManagementPage(String freelancerUsername) {
        this.freelancerUsername = freelancerUsername;
        setTitle("Freelancer Contract Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table for contracts
        String[] columnNames = {"Contract ID", "Job Title", "Client", "Status", "Work Submitted"};
        tableModel = new DefaultTableModel(columnNames, 0);
        contractsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(contractsTable);

        // Buttons
        updateStatusButton = new JButton("Update Work Status");
        requestPaymentButton = new JButton("Request Payment");
        submitReviewButton = new JButton("Submit Review");

        updateStatusButton.addActionListener(new UpdateStatusAction());
        requestPaymentButton.addActionListener(new RequestPaymentAction());
        submitReviewButton.addActionListener(new SubmitReviewAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(requestPaymentButton);
        buttonPanel.add(submitReviewButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        connectToDatabase();
        loadContracts(freelancerUsername);
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

    private void loadContracts(String freelancerUsername) {
        try {
            String query = "SELECT c.contract_id, j.title, u.username, c.status, c.work_submitted " +
                           "FROM contracts c " +
                           "JOIN jobs j ON c.job_id = j.job_id " +
                           "JOIN users u ON c.client_id = u.user_id " +
                           "WHERE c.freelancer_id = (SELECT user_id FROM users WHERE username = ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, freelancerUsername);
            ResultSet rs = stmt.executeQuery();

            // Clear existing rows
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("contract_id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getBoolean("work_submitted")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load contracts.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class UpdateStatusAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    String updateQuery = "UPDATE contracts SET status = 'Work in Progress' WHERE contract_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(updateQuery);
                    stmt.setInt(1, contractId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Work status updated successfully!");
                    loadContracts(freelancerUsername);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Failed to update work status.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Please select a contract to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RequestPaymentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    String requestQuery = "UPDATE contracts SET payment_requested = 1 WHERE contract_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(requestQuery);
                    stmt.setInt(1, contractId);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Payment requested successfully!");
                    loadContracts(freelancerUsername);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Failed to request payment.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Please select a contract to request payment.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class SubmitReviewAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = contractsTable.getSelectedRow();
            if (selectedRow != -1) {
                int contractId = (int) tableModel.getValueAt(selectedRow, 0);
                String reviewText = JOptionPane.showInputDialog(FreelancerContractManagementPage.this, "Enter your review:");
                if (reviewText != null && !reviewText.trim().isEmpty()) {
                    try {
                        String insertReviewQuery = "INSERT INTO reviews (contract_id, reviewer_id, review_text, rating) " +
                                                   "VALUES (?, (SELECT user_id FROM users WHERE username = ?), ?, ?)";
                        PreparedStatement stmt = connection.prepareStatement(insertReviewQuery);
                        stmt.setInt(1, contractId);
                        stmt.setString(2, freelancerUsername);
                        stmt.setString(3, reviewText);
                        stmt.setInt(4, 5);  // Assuming 5-star rating
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Review submitted successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Failed to submit review.", "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(FreelancerContractManagementPage.this, "Please select a contract to submit a review.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new FreelancerContractManagementPage("freelancer1");
    }
}
