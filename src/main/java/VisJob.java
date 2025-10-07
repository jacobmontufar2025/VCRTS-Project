import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * VisJob class represents the GUI for clients to submit jobs.
 * Each job includes Client ID, Duration, and Deadline.
 * Entries are shown in a table and logged to both CSV and TXT files.
 */
public class VisJob extends JFrame {

    // --- USER INFO ---
    private final String clientName; // Logged-in client's username

    // --- FORM FIELDS ---
    private JTextField clientIdField;
    private JTextField deadlineField;
    private JSpinner durationSpinner;
    private JComboBox<String> durationUnitBox;

    // --- TABLE & DATA ---
    private DefaultTableModel tableModel;
    private JTable table;

    // --- FILE OUTPUT ---
    private static final String CSV_FILE = "job_entries.csv";
    private static final String TXT_FILE = "job_data.txt";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VisJob(String clientName) {
        this.clientName = clientName;

        setTitle("Client Job Submission - " + clientName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 560);
        setLocationRelativeTo(null);

        // === ROOT PANEL ===
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(220, 240, 255));

        JLabel header = new JLabel("Job Submission Console  |  Welcome " + clientName, SwingConstants.CENTER);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        root.add(header);
        root.add(Box.createVerticalStrut(10));

        // === FORM PANEL ===
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize fields
        clientIdField = new JTextField();
        ((AbstractDocument) clientIdField.getDocument()).setDocumentFilter(new NumericFilter());

        deadlineField = new JTextField();
        durationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        durationUnitBox = new JComboBox<>(new String[]{"hours", "days"});

        int r = 0;

        gc.gridx = 0; gc.gridy = r; form.add(new JLabel("Client ID:"), gc);
        gc.gridx = 1; gc.gridy = r++; form.add(clientIdField, gc);

        // Duration with spinner + unit selector
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        durationPanel.setOpaque(false);
        durationPanel.add(durationSpinner);
        durationPanel.add(durationUnitBox);

        gc.gridx = 0; gc.gridy = r; form.add(new JLabel("Job Duration:"), gc);
        gc.gridx = 1; gc.gridy = r++; form.add(durationPanel, gc);

        gc.gridx = 0; gc.gridy = r; form.add(new JLabel("Job Deadline (YYYY-MM-DD):"), gc);
        gc.gridx = 1; gc.gridy = r++; form.add(deadlineField, gc);

        // --- BUTTONS ---
        JButton addButton = new JButton("Add to List");
        JButton saveButton = new JButton("Save List to File");
        JButton clearButton = new JButton("Clear Form");
        JButton logoutButton = new JButton("Logout");

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(saveButton);
        buttons.add(clearButton);
        buttons.add(logoutButton);

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 2;
        form.add(buttons, gc);

        root.add(form);
        root.add(Box.createVerticalStrut(12));

        // === TABLE PANEL ===
        tableModel = new DefaultTableModel(new Object[]{
                "Timestamp", "Client ID", "Duration Amount", "Unit", "Deadline"
        }, 0);
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(760, 260));
        root.add(tableScroll);

        // === EVENT HANDLERS ===
        addButton.addActionListener(this::onAdd);
        saveButton.addActionListener(this::onSave);
        clearButton.addActionListener(e -> clearForm());
        logoutButton.addActionListener(e -> {
            dispose();
            try {
                new LandingPage().setVisible(true);
            } catch (Throwable ignored) {}
        });

        setContentPane(root);
        setVisible(true);
    }

    /** Handles adding a new job entry. */
    private void onAdd(ActionEvent e) {
        String clientId = clientIdField.getText().trim();
        int durationAmount = (Integer) durationSpinner.getValue();
        String durationUnit = String.valueOf(durationUnitBox.getSelectedItem());
        String deadline = deadlineField.getText().trim();

        if (clientId.isEmpty() || deadline.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        // ✅ Validate date format and ensure not in the past
        if (!isValidDate(deadline)) {
            JOptionPane.showMessageDialog(this,
                    "Invalid or past date. Please use YYYY-MM-DD and ensure it’s today or later (e.g., 2025-10-07).",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String ts = TS_FMT.format(LocalDateTime.now());
        tableModel.addRow(new Object[]{ts, clientId, durationAmount, durationUnit, deadline});

        // Auto-save to TXT file asynchronously
        new Thread(() -> {
            try (FileWriter writer = new FileWriter(TXT_FILE, true)) {
                writer.write("Username: " + clientName + "\n");
                writer.write("Timestamp: " + ts + "\n");
                writer.write("Client ID: " + clientId + "\n");
                writer.write("Duration: " + durationAmount + " " + durationUnit + "\n");
                writer.write("Deadline: " + deadline + "\n");
                writer.write("-------------------------\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();

        clearForm();
        JOptionPane.showMessageDialog(this, "✅ Entry added and saved to log.");
    }

    /** Saves all table entries to a CSV file. */
    private void onSave(ActionEvent e) {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to save.");
            return;
        }

        File out = new File(CSV_FILE);
        boolean writeHeader = !out.exists();

        try (FileWriter fw = new FileWriter(out, true)) {
            if (writeHeader) {
                fw.write("timestamp,client_id,duration_amount,unit,deadline\n");
            }
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String ts = String.valueOf(tableModel.getValueAt(i, 0));
                String id = String.valueOf(tableModel.getValueAt(i, 1));
                String durAmt = String.valueOf(tableModel.getValueAt(i, 2));
                String durUnit = String.valueOf(tableModel.getValueAt(i, 3));
                String dl = escapeCsv(String.valueOf(tableModel.getValueAt(i, 4)));
                fw.write(ts + "," + id + "," + durAmt + "," + durUnit + "," + dl + "\n");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save CSV: " + ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Saved all entries to " + CSV_FILE);
    }
    
    

    /** Clears all input fields. */
    private void clearForm() {
        clientIdField.setText("");
        durationSpinner.setValue(1);
        durationUnitBox.setSelectedIndex(0);
        deadlineField.setText("");
    }

    /** Escapes commas, quotes, and newlines for CSV safety. */
    private static String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    /** Allows only numeric input for Client ID. */
    private static class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            } else {
                Toolkit.getDefaultToolkit().beep();
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

    /**
     * Checks if the given string is a valid date in yyyy-MM-dd format
     * and ensures it is today or in the future.
     /** Checks if the deadline input is a valid date in yyyy-MM-dd or yyyy-M-d format. */
    private boolean isValidDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            LocalDate enteredDate = LocalDate.parse(dateStr, formatter);
            LocalDate today = LocalDate.now();
            return !enteredDate.isBefore(today); // ✅ only today or future
        } catch (DateTimeParseException ex) {
            return false; // invalid format or impossible date
        }
    }


    /** Test entry point. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisJob("Guest"));
    }
}