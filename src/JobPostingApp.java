import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
import javax.swing.JTextField;

public class JobPostingApp extends JFrame {
    private JTextField titleField, budgetField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryComboBox;
    private JButton postJobButton, viewPostedJobsButton, editJobButton, backButton;
    private String username;

    // Database connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/freelance_work_arena";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "root";

    public JobPostingApp(String username) {
        this.username = username;
        setTitle("Post a Job - Freelance Hub");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create the panel for the job posting components
        JPanel jobPanel = new JPanel();
        jobPanel.setOpaque(false); // Transparent
        jobPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Job Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        jobPanel.add(new JLabel("Job Title:"), gbc);
        titleField = new JTextField(10);
        gbc.gridx = 1;
        jobPanel.add(titleField, gbc);

        // Job Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        jobPanel.add(new JLabel("Job Description:"), gbc);
        descriptionArea = new JTextArea(5, 15);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jobPanel.add(new JScrollPane(descriptionArea), gbc);

        // Budget
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        jobPanel.add(new JLabel("Budget (USD):"), gbc);
        budgetField = new JTextField(10);
        gbc.gridx = 1;
        jobPanel.add(budgetField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 3;
        jobPanel.add(new JLabel("Category:"), gbc);
        categoryComboBox = new JComboBox<>(new String[]{
            "Web Development", 
            "Mobile App Development", 
            "Graphic Design", 
            "Content Writing", 
            "Digital Marketing", 
            "Data Analysis", 
            "Video Editing", 
            "Translation Services"
        });        gbc.gridx = 1;
        jobPanel.add(categoryComboBox, gbc);

        // Post Job Button
        postJobButton = new JButton("Post Job");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jobPanel.add(postJobButton, gbc);

        // Buttons for View and Edit Jobs
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        // View Posted Jobs Button
        viewPostedJobsButton = new JButton("View My Posted Jobs");
        buttonPanel.add(viewPostedJobsButton);

        // Edit Posted Job Button
        editJobButton = new JButton("Edit Job");
        buttonPanel.add(editJobButton);

        // Add button panel to the main panel
        gbc.gridy = 5;
        jobPanel.add(buttonPanel, gbc);

        // Back Button
        backButton = new JButton("Back to Home");
        gbc.gridy = 6;
        jobPanel.add(backButton, gbc);

        add(jobPanel, BorderLayout.CENTER);

        addListeners();
    }

    private void addListeners() {
        postJobButton.addActionListener(e -> postJob());
        viewPostedJobsButton.addActionListener(e -> viewPostedJobs());
        editJobButton.addActionListener(e -> editJob());
        backButton.addActionListener(e -> dispose()); // Logic to go back to Home Page (implement as needed)
    }

    private void postJob() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String budget = budgetField.getText();
        String category = (String) categoryComboBox.getSelectedItem();
    
        // Input validation
        if (title.isEmpty() || description.isEmpty() || budget.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Step 1: Retrieve the client_id from the logged-in username
            String loggedInUsername = username; // This should be passed to this page from login
            String getClientIdSql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement clientStmt = connection.prepareStatement(getClientIdSql);
            clientStmt.setString(1, loggedInUsername);
            ResultSet clientResult = clientStmt.executeQuery();
    
            if (clientResult.next()) {
                int clientId = clientResult.getInt("user_id");
    
                // Step 2: Retrieve the category_id based on the selected category name
                String getCategoryIdSql = "SELECT category_id FROM categories WHERE category_name = ?";
                PreparedStatement categoryStmt = connection.prepareStatement(getCategoryIdSql);
                categoryStmt.setString(1, category);
                ResultSet categoryResult = categoryStmt.executeQuery();
    
                if (categoryResult.next()) {
                    int categoryId = categoryResult.getInt("category_id");
    
                    // Step 3: Insert the job with client_id and category_id
                    String insertJobSql = "INSERT INTO jobs (client_id, category_id, title, description, budget, posted_date) VALUES (?, ?, ?, ?, ?, CURDATE())";
                    PreparedStatement jobStmt = connection.prepareStatement(insertJobSql);
                    jobStmt.setInt(1, clientId);
                    jobStmt.setInt(2, categoryId);
                    jobStmt.setString(3, title);
                    jobStmt.setString(4, description);
                    jobStmt.setString(5, budget);
                    jobStmt.executeUpdate();
    
                    JOptionPane.showMessageDialog(this, "Job posted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields(); // Clear fields after posting
                } else {
                    JOptionPane.showMessageDialog(this, "Selected category not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Client not found. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error posting job: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewPostedJobs() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT job_id, title, description, budget FROM jobs"; // Modify as per your schema
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            StringBuilder jobList = new StringBuilder("Posted Jobs:\n");
            while (resultSet.next()) {
                jobList.append("Job ID: ").append(resultSet.getInt("job_id"))
                        .append(", Title: ").append(resultSet.getString("title"))
                        .append(", Budget: ").append(resultSet.getString("budget")).append("\n");
            }

            JOptionPane.showMessageDialog(this, jobList.toString(), "My Posted Jobs", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving posted jobs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editJob() {
        String jobIdStr = JOptionPane.showInputDialog(this, "Enter the Job ID to edit:");

        if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Job ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int jobId = Integer.parseInt(jobIdStr);
            String newTitle = JOptionPane.showInputDialog(this, "Enter new Title:");
            String newDescription = JOptionPane.showInputDialog(this, "Enter new Description:");
            String newBudgetStr = JOptionPane.showInputDialog(this, "Enter new Budget:");

            if (newTitle != null && newDescription != null && newBudgetStr != null) {
                double newBudget = Double.parseDouble(newBudgetStr);
                updateJob(jobId, newTitle, newDescription, newBudget);
            } else {
                JOptionPane.showMessageDialog(this, "All fields are required to update the job.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Budget.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateJob(int jobId, String newTitle, String newDescription, double newBudget) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE jobs SET title = ?, description = ?, budget = ? WHERE job_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, newTitle);
            preparedStatement.setString(2, newDescription);
            preparedStatement.setBigDecimal(3, new java.math.BigDecimal(newBudget));
            preparedStatement.setInt(4, jobId);
            preparedStatement.executeUpdate();

            JOptionPane.showMessageDialog(this, "Job updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update job. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        titleField.setText("");
        descriptionArea.setText("");
        budgetField.setText("");
        categoryComboBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        String user="username";
        new JobPostingApp(user);
    }
}
