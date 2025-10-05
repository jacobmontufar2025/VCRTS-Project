import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileWriter;

/**
 * The VisJob class represents the job submission window for clients.
 * Each logged-in client can submit job details such as their ID, 
 * job duration, and job deadline. Submitted data is logged and saved to a file.
 * this is visjob
 */
public class VisJob extends JFrame {

    // --- UI COMPONENTS ---
    private JTextField clientIdField;
    private JTextField durationField;
    private JTextField deadlineField;
    private JButton submitButton;

    // --- LOGIC HANDLERS ---
    private jobHandler controller; // Handles job creation and submission
    private static int jobCount = 0; // Tracks number of total submitted jobs

    // --- USER DATA ---
    private String clientName; // Stores logged-in user's name (passed from login page)

    /**
     * Constructor that accepts the logged-in user's name.
     * @param clientName the name of the client currently logged in
     */
    public VisJob(String clientName) {
        this.clientName = clientName;
        initUI(); // Build and initialize the interface
    }

    /**
     * Default constructor for testing purposes.
     * If no name is provided, defaults to "Guest".
     */
    public VisJob() {
        this("Guest");
    }

    /**
     * Initializes and configures all UI components.
     */
    private void initUI() {
        controller = new jobHandler(); // Instantiate the job handler

        // --- WINDOW SETUP ---
        setTitle("Job Submission - " + clientName); // Window title includes username
        setExtendedState(JFrame.MAXIMIZED_BOTH);    // Fullscreen mode
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- INPUT FIELDS ---
        clientIdField = new JTextField(); // Starts empty (client enters their unique ID)
        ((AbstractDocument) clientIdField.getDocument()).setDocumentFilter(new NumericFilter()); // ✅ Only allow numbers
        durationField = new JTextField(); // Duration input
        deadlineField = new JTextField(); // Deadline input

        // --- LABELS ---
        JLabel clientLabel = new JLabel("Client ID:");
        JLabel durationLabel = new JLabel("Duration:");
        JLabel deadlineLabel = new JLabel("Deadline:");

        // --- INPUT PANEL ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 20, 50));
        inputPanel.add(clientLabel);
        inputPanel.add(clientIdField);
        inputPanel.add(durationLabel);
        inputPanel.add(durationField);
        inputPanel.add(deadlineLabel);
        inputPanel.add(deadlineField);

        // --- SUBMIT BUTTON ---
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 24));
        submitButton.setPreferredSize(new Dimension(0, 60));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        buttonPanel.add(submitButton, BorderLayout.CENTER);

        // --- EXIT BUTTON ---
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 18));
        exitButton.addActionListener(ev -> dispose());
        exitButton.setBounds(20, 20, 100, 40);
        this.getLayeredPane().add(exitButton, JLayeredPane.PALETTE_LAYER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                exitButton.setBounds(20, 20, 100, 40);
            }
        });

        // --- LAYOUT CONFIGURATION ---
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- DYNAMIC FONT SCALING ---
        addDynamicFont(clientIdField, 16, 36);
        addDynamicFont(durationField, 16, 36);
        addDynamicFont(deadlineField, 16, 36);

        addDynamicLabelFont(clientLabel, 18, 36);
        addDynamicLabelFont(durationLabel, 18, 36);
        addDynamicLabelFont(deadlineLabel, 18, 36);

        // --- EVENT HANDLERS ---
        submitButton.addActionListener(e -> handleSubmit());
    }

    /**
     * Handles submission logic.
     */
    private void handleSubmit() {
        String clientId = clientIdField.getText().trim();
        String duration = durationField.getText().trim();
        String deadline = deadlineField.getText().trim();

        if (clientId.isEmpty() || duration.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill out all fields before submitting.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Job job = controller.createJob(clientId, duration, deadline);
        controller.submitJob(job);

        new Thread(() -> {
            try (FileWriter writer = new FileWriter("job_data.txt", true)) {
                writer.write("Username: " + clientName + "\n");
                writer.write("Timestamp: " + job.getTimestamp() + "\n");
                writer.write("Client ID: " + clientId + "\n");
                writer.write("Job Duration: " + duration + "\n");
                writer.write("Job Deadline: " + deadline + "\n");
                writer.write("-------------------------\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        jobCount++;

        JOptionPane.showMessageDialog(this,
                "✅ Data submitted and saved:\n" +
                        "Username: " + clientName + "\n" +
                        "Client ID: " + clientId + "\n" +
                        "Duration: " + duration + "\n" +
                        "Deadline: " + deadline + "\n" +
                        "Submitted At: " + job.getTimestamp() + "\n\n" +
                        "📌 Total jobs in queue: " + jobCount);

        this.dispose();

        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                VisJob newJobForm = new VisJob(clientName);
                newJobForm.setVisible(true);
            });
        }).start();
    }

    private void addDynamicFont(JTextField field, int minFontSize, int maxFontSize) {
        field.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int fieldHeight = field.getHeight();
                int fontSize = Math.min(Math.max(fieldHeight - 10, minFontSize), maxFontSize);
                field.setFont(new Font("Arial", Font.PLAIN, fontSize));
            }
        });
    }

    private void addDynamicLabelFont(JLabel label, int minFontSize, int maxFontSize) {
        label.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int labelHeight = label.getHeight();
                int fontSize = Math.min(Math.max(labelHeight - 4, minFontSize), maxFontSize);
                label.setFont(new Font("Arial", Font.BOLD, fontSize));
            }
        });
    }

    /**
     * Small helper that only allows numeric input in a JTextField.
     */
    private static class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep(); // ⚠️ feedback if user types non-numeric
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text != null && text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            } else if (text != null && !text.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VisJob form = new VisJob("Guest User");
            form.setVisible(true);
        });
    }
}