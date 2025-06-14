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

public class ClientProposalManagementPage extends JFrame {
    private JTable proposalsTable;
    private DefaultTableModel tableModel;
    private JButton acceptProposalButton, rejectProposalButton;
    private Connection connection;
    private String username; // Store the username

    public ClientProposalManagementPage(String username) {
        this.username = username; // Store the username
        setTitle("Client Proposal Management");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table for proposals
        String[] columnNames = {"Proposal ID", "Job Title", "Freelancer", "Bid Amount", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        proposalsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(proposalsTable);

        // Buttons
        acceptProposalButton = new JButton("Accept Proposal");
        rejectProposalButton = new JButton("Reject Proposal");

        acceptProposalButton.addActionListener(new AcceptProposalAction());
        rejectProposalButton.addActionListener(new RejectProposalAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(acceptProposalButton);
        buttonPanel.add(rejectProposalButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        connectToDatabase();
        loadProposals();
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

    private void loadProposals() {
        try {
            String query = "SELECT p.proposal_id, j.title, u.username, p.bid_amount, p.status " +
                           "FROM proposals p " +
                           "JOIN jobs j ON p.job_id = j.job_id " +
                           "JOIN users u ON p.freelancer_id = u.user_id " +
                           "WHERE j.client_id = (SELECT user_id FROM users WHERE username = ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username); // Set the username parameter
            ResultSet rs = stmt.executeQuery();

            // Clear the existing rows
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("proposal_id"),
                    rs.getString("title"),
                    rs.getString("username"),
                    rs.getDouble("bid_amount"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load proposals.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class AcceptProposalAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = proposalsTable.getSelectedRow();
            if (selectedRow != -1) {
                int proposalId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    // Step 1: Update the proposal status to "Accepted"
                    String updateQuery = "UPDATE proposals SET status = 'accepted' WHERE proposal_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(updateQuery);
                    stmt.setInt(1, proposalId);
                    stmt.executeUpdate();

                    // Step 2: Retrieve job_id and freelancer_id for the accepted proposal
                    String jobIdQuery = "SELECT job_id, freelancer_id FROM proposals WHERE proposal_id = ?";
                    PreparedStatement jobStmt = connection.prepareStatement(jobIdQuery);
                    jobStmt.setInt(1, proposalId);
                    ResultSet rs = jobStmt.executeQuery();

                    if (rs.next()) {
                        int jobId = rs.getInt("job_id");
                        int freelancerId = rs.getInt("freelancer_id");

                        // Step 3: Insert into the contracts table
                        String insertContractQuery = "INSERT INTO contracts (job_id, freelancer_id, client_id, status) " +
                                                     "VALUES (?, ?, (SELECT user_id FROM users WHERE username = ?),?)";
                        PreparedStatement insertStmt = connection.prepareStatement(insertContractQuery);
                        insertStmt.setInt(1, jobId);
                        insertStmt.setInt(2, freelancerId);
                        insertStmt.setString(3, username);
                        insertStmt.setString(4, "active"); // Use client's username to get client_id
                        insertStmt.executeUpdate();

                        // Step 4: Reject other proposals for the same job
                        String rejectQuery = "UPDATE proposals SET status = 'rejected' WHERE job_id = ? AND proposal_id != ?";
                        PreparedStatement rejectStmt = connection.prepareStatement(rejectQuery);
                        rejectStmt.setInt(1, jobId);
                        rejectStmt.setInt(2, proposalId);
                        rejectStmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Proposal accepted and contract created successfully!");
                    loadProposals(); // Reload the proposals
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Failed to accept proposal.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Please select a proposal to accept.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RejectProposalAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = proposalsTable.getSelectedRow();
            if (selectedRow != -1) {
                int proposalId = (int) tableModel.getValueAt(selectedRow, 0);
                try {
                    // Update the proposal status to "Rejected"
                    String updateQuery = "UPDATE proposals SET status = 'Rejected' WHERE proposal_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(updateQuery);
                    stmt.setInt(1, proposalId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Proposal rejected successfully!");
                    loadProposals(); // Reload the proposals
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Failed to reject proposal.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(ClientProposalManagementPage.this, "Please select a proposal to reject.", "Selection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new ClientProposalManagementPage("client1"); // Example username
    }
}
