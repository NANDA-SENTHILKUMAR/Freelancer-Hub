import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FreelancerJobOperationsApp extends JFrame {
    private JTextArea jobListArea;
    private JButton viewAppliedJobsButton, applyJobButton;
    private JComboBox<String> categoryComboBox;
    private Connection connection;

    public FreelancerJobOperationsApp() {
        setTitle("Freelancer Job Operations");
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize UI components
        jobListArea = new JTextArea();
        jobListArea.setEditable(false);
        viewAppliedJobsButton = new JButton("View Applied Jobs");
        applyJobButton = new JButton("Apply for Job");
        categoryComboBox = new JComboBox<>();  // Dropdown for categories

        // Layout setup
        JPanel panel = new JPanel(new BorderLayout());
        JPanel categoryPanel = new JPanel();

        // Add category combo box
        categoryPanel.add(new JLabel("Filter by Category:"));
        categoryPanel.add(categoryComboBox);

        panel.add(categoryPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(jobListArea), BorderLayout.CENTER);
        JPanel actionPanel = new JPanel();
        actionPanel.add(viewAppliedJobsButton);
        actionPanel.add(applyJobButton);
        panel.add(actionPanel, BorderLayout.SOUTH);

        add(panel);

        connectToDatabase();
        loadAllCategories();  // Load categories into the dropdown
        loadAllJobs();  // Load jobs when the app starts
        addListeners();
        setVisible(true);
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/freelance_work_arena", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load all categories from the categories table into the category dropdown
    private void loadAllCategories() {
        try {
            String query = "SELECT category_name FROM categories";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            categoryComboBox.addItem("All Categories");  // Default option
            while (rs.next()) {
                categoryComboBox.addItem(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load all jobs (optionally sorted by category)
    private void loadAllJobs() {
        try {
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            String query;

            if (selectedCategory == null || selectedCategory.equals("All Categories")) {
                query = "SELECT j.job_id, j.title, j.description, j.budget, c.category_name FROM jobs j JOIN categories c ON j.category_id = c.category_id";
            } else {
                query = "SELECT j.job_id, j.title, j.description, j.budget, c.category_name FROM jobs j JOIN categories c ON j.category_id = c.category_id WHERE c.category_name = ?";
            }

            PreparedStatement stmt = connection.prepareStatement(query);

            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                stmt.setString(1, selectedCategory);
            }

            ResultSet rs = stmt.executeQuery();
            StringBuilder jobList = new StringBuilder();

            while (rs.next()) {
                jobList.append("Job ID: ").append(rs.getInt("job_id"))
                       .append("\nTitle: ").append(rs.getString("title"))
                       .append("\nDescription: ").append(rs.getString("description"))
                       .append("\nBudget: $").append(rs.getBigDecimal("budget"))
                       .append("\nCategory: ").append(rs.getString("category_name"))
                       .append("\n-------------------------\n");
            }
            jobListArea.setText(jobList.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Apply for a specific job by jobId and redirect to ProposalSubmissionApp
    private void applyForJob(int jobId) {
        try {
            // First, check if the job exists
            String checkJobQuery = "SELECT job_id FROM jobs WHERE job_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkJobQuery);
            checkStmt.setInt(1, jobId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Job ID not found. Please select a valid job.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Redirect to ProposalSubmissionApp for proposal submission
            new ProposalSubmissionApp(jobId);
            dispose(); // Close the current FreelancerJobOperationsApp window
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to apply for job. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        // Apply job listener
        applyJobButton.addActionListener(e -> {
            String jobIdStr = JOptionPane.showInputDialog(this, "Enter Job ID to apply for:");
            if (jobIdStr != null && !jobIdStr.trim().isEmpty()) {
                int jobId = Integer.parseInt(jobIdStr.trim());
                applyForJob(jobId);
            }
        });

        // Category combo box listener (sort jobs by selected category)
        categoryComboBox.addActionListener(e -> loadAllJobs());
    }

    public static void main(String[] args) {
        new FreelancerJobOperationsApp();
    }
}
