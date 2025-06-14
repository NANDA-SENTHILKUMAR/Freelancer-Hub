import java.awt.BorderLayout;
import java.sql.Connection; // Import for DefaultTableModel
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PaymentHistoryPage extends JFrame {
    private JTable paymentHistoryTable;
    private DefaultTableModel tableModel;
    private String username;
    private Connection connection;

    public PaymentHistoryPage(String username) {
        this.username = username;
        setTitle("Payment History");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Table for payment history
        String[] columnNames = {"Payment ID", "Contract ID", "Client ID", "Freelancer ID", "Amount", "Payment Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        paymentHistoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(paymentHistoryTable);
        add(scrollPane, BorderLayout.CENTER);

        connectToDatabase();
        loadPaymentHistory();

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

    private void loadPaymentHistory() {
        try {
            String query = "SELECT p.payment_id, p.contract_id, c.client_id, c.freelancer_id, p.amount, p.payment_date, p.status " +
                           "FROM payments p " +
                           "JOIN contracts c ON p.contract_id = c.contract_id " +
                           "JOIN users u1 ON c.client_id = u1.user_id " +  // Join for client
                           "JOIN users u2 ON c.freelancer_id = u2.user_id " +  // Join for freelancer
                           "WHERE u1.username = ? OR u2.username = ?";  // Check if the username matches either client or freelancer
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);  // Set username for client
            stmt.setString(2, username);  // Set username for freelancer
            ResultSet rs = stmt.executeQuery();
            // Clear existing rows
            tableModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("payment_id"),
                    rs.getInt("contract_id"),
                    rs.getInt("client_id"),
                    rs.getInt("freelancer_id"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("payment_date"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load payment history.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        new PaymentHistoryPage("client1");  // Example usage
    }
}