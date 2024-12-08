import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Punonjesit extends JPanel {
    private JButton checkInButton;
    private JButton checkOutButton;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextArea infoArea;
    private JButton backToPathsButton;

    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Map<Integer, Timer> activeTimers = new HashMap<>();
    private Map<Integer, JLabel> realTimeCounters = new HashMap<>();

    public Punonjesit() {
        initComponents();
        addListeners();
        loadEmployees();
        checkMonthEnd();
    }

    private void initComponents() {
        checkInButton = new JButton("Check In");
        checkOutButton = new JButton("Check Out");
        infoArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(infoArea);

        backToPathsButton = new JButton("Back to Paths");

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(checkInButton);
        inputPanel.add(checkOutButton);

        checkOutButton.setEnabled(false);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Emri", "Pozicioni", "Turni", "Oret per muajin"}, 0);
        employeeTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        JScrollPane tableScrollPane = new JScrollPane(employeeTable);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(tableScrollPane, BorderLayout.CENTER);
        topPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(backToPathsButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Styling the components
        setBackground(new Color(230, 245, 255));
        styleButton(checkInButton);
        styleButton(checkOutButton);
        styleButton(backToPathsButton);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 130, 200));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void addListeners() {
        checkInButton.addActionListener(e -> checkIn());
        checkOutButton.addActionListener(e -> checkOut());
        backToPathsButton.addActionListener(e -> {
            JFrame pathsFrame = new JFrame("Paths");
            pathsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            pathsFrame.setContentPane(new Paths());
            pathsFrame.setSize(800, 400);
            pathsFrame.setLocationRelativeTo(null);
            pathsFrame.setVisible(true);
        });

        employeeTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
    }

    private void updateButtonStates() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            checkInButton.setEnabled(false);
            checkOutButton.setEnabled(false);
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean isCheckedIn = activeTimers.containsKey(employeeId);

        checkInButton.setEnabled(!isCheckedIn);
        checkOutButton.setEnabled(isCheckedIn);
    }

    private void checkIn() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String query = "INSERT INTO employee_attendance (employee_id, check_in_time, date) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, employeeId);
            statement.setString(2, now.format(formatter));
            statement.setDate(3, Date.valueOf(LocalDate.now()));
            statement.executeUpdate();

            long workedSeconds = getTotalWorkedSecondsForMonth(employeeId);

            startRealTimeCounter(employeeId, selectedRow, workedSeconds);
            infoArea.append("Employee ID " + employeeId + " checked in at " + now.format(formatter) + "\n");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking in employee.");
        }
    }

    private void checkOut() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }

        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        String query = "UPDATE employee_attendance SET check_out_time = ? WHERE employee_id = ? AND check_out_time IS NULL";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, now.format(formatter));
            statement.setInt(2, employeeId);
            statement.executeUpdate();

            stopRealTimeCounter(employeeId);
            infoArea.append("Employee ID " + employeeId + " checked out at " + now.format(formatter) + "\n");

            checkMonthEnd();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error checking out employee.");
        }
    }
    private long getTotalWorkedSecondsForMonth(int employeeId) {
        String query = "SELECT SUM(TIMESTAMPDIFF(SECOND, check_in_time, check_out_time)) AS worked_seconds " +
                "FROM employee_attendance WHERE employee_id = ? AND MONTH(date) = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, employeeId);
            statement.setInt(2, LocalDate.now().getMonthValue());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("worked_seconds");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void startRealTimeCounter(int employeeId, int row, long initialWorkedSeconds) {
        Timer timer = new Timer(1000, e -> {
            JLabel counterLabel = realTimeCounters.computeIfAbsent(employeeId, id -> new JLabel(formatSecondsToHHMMSS(initialWorkedSeconds)));
            tableModel.setValueAt(counterLabel.getText(), row, 4);

            String[] parts = counterLabel.getText().split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            seconds++;
            if (seconds == 60) {
                seconds = 0;
                minutes++;
            }
            if (minutes == 60) {
                minutes = 0;
                hours++;
            }

            counterLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        });

        timer.start();
        activeTimers.put(employeeId, timer);
    }


    private void stopRealTimeCounter(int employeeId) {
        Timer timer = activeTimers.remove(employeeId);
        if (timer != null) {
            timer.stop();
        }
        realTimeCounters.remove(employeeId);
    }
    private void loadEmployees() {
        String query = "SELECT id, name, position, shift FROM employees";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int employeeId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String position = resultSet.getString("position");
                String shift = resultSet.getString("shift");

                tableModel.addRow(new Object[]{employeeId, name, position, shift, "00:00:00"});
            }

            checkMonthEnd();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void checkMonthEnd() {
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();

        String query = "SELECT employee_id, SUM(TIMESTAMPDIFF(SECOND, check_in_time, check_out_time)) AS worked_seconds " +
                "FROM employee_attendance WHERE MONTH(date) = ? GROUP BY employee_id";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, currentMonth);
            ResultSet resultSet = statement.executeQuery();

            infoArea.append("Total Time Worked This Month:\n");
            while (resultSet.next()) {
                int employeeId = resultSet.getInt("employee_id");
                long workedSeconds = resultSet.getLong("worked_seconds");

                String workedTimeFormatted = formatSecondsToHHMMSS(workedSeconds);
                infoArea.append("Employee ID " + employeeId + ": " + workedTimeFormatted + "\n");

                updateWorkedTimeInTable(employeeId, workedTimeFormatted);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String formatSecondsToHHMMSS(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateWorkedTimeInTable(int employeeId, String workedTimeFormatted) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((int) tableModel.getValueAt(i, 0) == employeeId) {
                tableModel.setValueAt(workedTimeFormatted, i, 4);
                break;
            }
        }
    }
}