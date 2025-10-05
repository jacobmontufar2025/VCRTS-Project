import java.awt.*;
import javax.swing.*;

public class Client extends JFrame {
    private String clientName;

    public Client(String clientName) {
        this.clientName = clientName;
        setTitle("Client Interface");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(187, 213, 237));
        add(mainPanel);

        // Welcome message
        JLabel label = new JLabel("Welcome, " + clientName + "!", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Top bar with logout ---
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
        topBar.setOpaque(false);
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton backButton = new JButton("Log out");
        backButton.addActionListener(e -> {
            dispose();
            new LandingPage(); // Go back to login/landing page
        });
        topBar.add(backButton);

        // --- Button to open job submission window ---
        JButton submitJobButton = new JButton("Submit a Job");
        submitJobButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitJobButton.setMaximumSize(new Dimension(200, 40));
        submitJobButton.addActionListener(e -> openJobWindow());

        // Add to main panel
        mainPanel.add(topBar);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(label);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(submitJobButton);

        setVisible(true);
    }

    // opens the visjob page with neccessary client id, duration, and deadline input fields
    private void openJobWindow() {
        try {
            VisJob jobWindow = new VisJob(clientName); 
            jobWindow.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error opening job window: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void submitJob() {
        System.out.println("Client job submitted by: " + clientName);
    }
}