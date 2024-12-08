import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;


public class Rezervimet extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;
    private JPanel tablePanel;
    private JPanel eventPanel;
    private List<JLabel> tableLabels;
    private List<JLabel> eventLabels;

    public Rezervimet() {
        setTitle("Sistemi i Rezervimit");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 255));

        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(200, 0));
        tablePanel.setBackground(new Color(245, 250, 255));

        JLabel tablesLabel = new JLabel("Tables", SwingConstants.CENTER);
        tablesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tablesLabel.setForeground(new Color(70, 130, 180));
        tablesLabel.setPreferredSize(new Dimension(200, 30));
        tablePanel.add(tablesLabel, BorderLayout.NORTH);

        JPanel tableContentPanel = new JPanel();
        tableContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        tableContentPanel.setBackground(new Color(245, 250, 255));
        tablePanel.add(tableContentPanel, BorderLayout.CENTER);

        eventPanel = new JPanel();
        eventPanel.setLayout(new BorderLayout());
        eventPanel.setPreferredSize(new Dimension(200, 0));
        eventPanel.setBackground(new Color(245, 250, 255));

        JLabel eventsLabel = new JLabel("Events", SwingConstants.CENTER);
        eventsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        eventsLabel.setForeground(new Color(70, 130, 180));
        eventsLabel.setPreferredSize(new Dimension(200, 30));
        eventPanel.add(eventsLabel, BorderLayout.NORTH);

        JPanel eventContentPanel = new JPanel();
        eventContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        eventContentPanel.setBackground(new Color(245, 250, 255));
        eventPanel.add(eventContentPanel, BorderLayout.CENTER);

        JButton reserveEventButton = createStyledButton("Reserve Event");
        JButton removeEventButton = createStyledButton("Remove Event");
        JButton bookTableButton = createStyledButton("Book Table");
        JButton addTableButton = createStyledButton("Add Table");
        JButton removeTableButton = createStyledButton("Remove Table");

        JButton backButton = createStyledButton("Back to Paths");
        backButton.addActionListener(e -> {
            JFrame pathsFrame = new JFrame("Paths");
            pathsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            pathsFrame.setContentPane(new Paths());
            pathsFrame.setSize(800, 400);
            pathsFrame.setLocationRelativeTo(null);
            pathsFrame.setVisible(true);
            this.dispose();
        });

        reserveEventButton.addActionListener(e -> reserveEvent());
        removeEventButton.addActionListener(e -> removeEvent());
        bookTableButton.addActionListener(e -> bookTable());
        addTableButton.addActionListener(e -> addTable());
        removeTableButton.addActionListener(e -> removeTable());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10));
        buttonPanel.setBackground(new Color(230, 240, 255));
        buttonPanel.add(reserveEventButton);
        buttonPanel.add(removeEventButton);
        buttonPanel.add(bookTableButton);
        buttonPanel.add(addTableButton);
        buttonPanel.add(removeTableButton);
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);
        add(eventPanel, BorderLayout.EAST);

        establishConnection();
        loadTables();
        loadEvents();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180)); // Steel blue
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }

    private void establishConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection established successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadTables() {
        JPanel tableContentPanel = new JPanel();
        tableContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel tablesLabel = new JLabel("Tables", SwingConstants.CENTER);
        tablesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tablesLabel.setForeground(new Color(70, 130, 180));
        tablesLabel.setPreferredSize(new Dimension(200, 30));

        String sql = "SELECT t.TableNumber, t.Capacity, t.Location, t.BookingDate, c.ClientName, c.ContactNr, t.IsOccupied " +
                "FROM Tables t LEFT JOIN Client c ON t.ClientID = c.ClientID";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;

                int tableNumber = rs.getInt("TableNumber");
                int capacity = rs.getInt("Capacity");
                String location = rs.getString("Location");
                Timestamp bookingDate = rs.getTimestamp("BookingDate");
                String clientName = rs.getString("ClientName");
                String contactNr = rs.getString("ContactNr");
                boolean isOccupied = rs.getBoolean("IsOccupied");

                String labelText = "<html>Table " + tableNumber + "<br>Capacity: " + capacity + "<br>Location: " + location;
                if (isOccupied) {
                    labelText += "<br>Reserved by: " + clientName + "<br>Contact: " + contactNr +
                            "<br>Reservation Time: " + bookingDate + "</html>";
                } else {
                    labelText += "<br>Available</html>";
                }

                JLabel tableLabel = new JLabel(labelText, SwingConstants.CENTER);
                tableLabel.setOpaque(true);
                tableLabel.setBackground(isOccupied ? Color.RED : Color.GREEN);
                tableLabel.setForeground(Color.WHITE);
                tableLabel.setPreferredSize(new Dimension(230, 120));
                tableLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                tableContentPanel.add(tableLabel);
            }

            if (!found) {

                JLabel noTablesLabel = new JLabel("No tables found.", SwingConstants.CENTER);
                noTablesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noTablesLabel.setForeground(Color.GRAY);
                tableContentPanel.add(noTablesLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablePanel.removeAll();
        tablePanel.add(tablesLabel, BorderLayout.NORTH); // Add the consistent header label
        tablePanel.add(tableContentPanel, BorderLayout.CENTER);
        tablePanel.revalidate();
        tablePanel.repaint();
    }


    private void loadEvents() {
        JPanel eventContentPanel = new JPanel();
        eventContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel eventsLabel = new JLabel("Events", SwingConstants.CENTER);
        eventsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        eventsLabel.setForeground(new Color(70, 130, 180));
        eventsLabel.setPreferredSize(new Dimension(200, 30));

        String sql = "SELECT e.EventName, e.EventDate, c.ContactEmail, c.ContactNr " +
                "FROM Evente e " +
                "JOIN Client c ON e.ClientID = c.ClientID";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean found = false;

            while (rs.next()) {
                found = true;

                String eventName = rs.getString("EventName");
                Timestamp eventDate = rs.getTimestamp("EventDate");
                String contactEmail = rs.getString("ContactEmail");
                String contactNr = rs.getString("ContactNr");

                String labelText = "<html>" +
                        eventName + "<br>" +
                        eventDate + "<br>" +
                        "<strong>Email:</strong> " + contactEmail + "<br>" +
                        "<strong>Phone:</strong> " + contactNr +
                        "</html>";

                JLabel eventLabel = new JLabel(labelText, SwingConstants.CENTER);
                eventLabel.setOpaque(true);
                eventLabel.setBackground(new Color(200, 200, 200));
                eventLabel.setForeground(Color.DARK_GRAY);
                eventLabel.setPreferredSize(new Dimension(200, 100));
                eventLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                eventContentPanel.add(eventLabel);
            }

            if (!found) {
                System.out.println("No events found in the database.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        eventPanel.removeAll();

        eventPanel.add(eventsLabel, BorderLayout.NORTH);
        eventPanel.add(eventContentPanel, BorderLayout.CENTER);

        eventPanel.revalidate();
        eventPanel.repaint();
    }

    private void reserveEvent() {
        String eventName = JOptionPane.showInputDialog(this, "Enter Event Name:");
        String eventDateStr = JOptionPane.showInputDialog(this, "Enter Event Date (yyyy-mm-dd hh:mm:ss):");
        String clientName = JOptionPane.showInputDialog(this, "Enter Client Name:");
        String contactNr = JOptionPane.showInputDialog(this, "Enter Client Contact Number:");
        String contactEmail = JOptionPane.showInputDialog(this, "Enter Client Contact Email:");

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date eventDate = dateFormat.parse(eventDateStr);
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(eventDate.getTime());

            String clientSql = "INSERT INTO Client (ClientName, ContactNr, ContactEmail) VALUES (?, ?, ?)";
            try (PreparedStatement clientPstmt = connection.prepareStatement(clientSql, Statement.RETURN_GENERATED_KEYS)) {
                clientPstmt.setString(1, clientName);
                clientPstmt.setString(2, contactNr);
                clientPstmt.setString(3, contactEmail);
                clientPstmt.executeUpdate();

                ResultSet clientRs = clientPstmt.getGeneratedKeys();
                int clientId = -1;
                if (clientRs.next()) {
                    clientId = clientRs.getInt(1);
                }

                String eventSql = "INSERT INTO Evente (EventName, EventDate, ClientID) VALUES (?, ?, ?)";
                try (PreparedStatement eventPstmt = connection.prepareStatement(eventSql)) {
                    eventPstmt.setString(1, eventName);
                    eventPstmt.setTimestamp(2, sqlDate);
                    eventPstmt.setInt(3, clientId);
                    eventPstmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Event reserved successfully!");
                loadEvents();
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use yyyy-mm-dd hh:mm:ss.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void removeEvent() {
        String eventName = JOptionPane.showInputDialog(this, "Enter Event Name to Remove:");
        if (eventName == null || eventName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Event name cannot be empty.");
            return;
        }

        String sql = "DELETE FROM Evente WHERE EventName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Event removed successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Event not found.");
            }
            loadEvents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void bookTable() {
        String tableNumberStr = JOptionPane.showInputDialog(this, "Enter Table Number to Book:");
        String clientName = JOptionPane.showInputDialog(this, "Enter Client Name:");
        String numberOfPersonsStr = JOptionPane.showInputDialog(this, "Enter Number of Persons:");
        String bookingDateStr = JOptionPane.showInputDialog(this, "Enter Reservation Date (yyyy-mm-dd hh:mm:ss):");

        try {
            int tableNumber = Integer.parseInt(tableNumberStr);
            int numberOfPersons = Integer.parseInt(numberOfPersonsStr);

            String tableCheckSql = "SELECT Capacity, IsOccupied FROM Tables WHERE TableNumber = ?";
            try (PreparedStatement checkPstmt = connection.prepareStatement(tableCheckSql)) {
                checkPstmt.setInt(1, tableNumber);
                ResultSet rs = checkPstmt.executeQuery();

                if (rs.next()) {
                    int tableCapacity = rs.getInt("Capacity");
                    boolean isOccupied = rs.getBoolean("IsOccupied");

                    if (isOccupied) {
                        JOptionPane.showMessageDialog(this, "Table is already reserved!");
                        return;
                    }

                    if (numberOfPersons > tableCapacity) {
                        JOptionPane.showMessageDialog(this, "Number of persons exceeds the table's capacity!");
                        return;
                    }

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date bookingDate = dateFormat.parse(bookingDateStr);
                    java.sql.Timestamp sqlDate = new java.sql.Timestamp(bookingDate.getTime());

                    String updateTableSql = "UPDATE Tables SET IsOccupied = TRUE, BookingDate = ? WHERE TableNumber = ?";
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateTableSql)) {
                        updatePstmt.setTimestamp(1, sqlDate);
                        updatePstmt.setInt(2, tableNumber);
                        updatePstmt.executeUpdate();
                    }

                    String clientSql = "INSERT INTO Client (ClientName) VALUES (?) ON DUPLICATE KEY UPDATE ClientName = ClientName";
                    try (PreparedStatement pstmt = connection.prepareStatement(clientSql)) {
                        pstmt.setString(1, clientName);
                        pstmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "Table booked successfully!");
                    loadTables();
                } else {
                    JOptionPane.showMessageDialog(this, "Table not found.");
                }
            }

        } catch (NumberFormatException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check your inputs.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    private void addTable() {
        String tableNumberStr = JOptionPane.showInputDialog(this, "Enter Table Number:");
        String capacityStr = JOptionPane.showInputDialog(this, "Enter Capacity:");
        String location = JOptionPane.showInputDialog(this, "Enter Location:");

        try {
            int tableNumber = Integer.parseInt(tableNumberStr);
            int capacity = Integer.parseInt(capacityStr);

            String sql = "INSERT INTO Tables (TableNumber, Capacity, Location, IsOccupied) VALUES (?, ?, ?, FALSE)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, tableNumber);
                pstmt.setInt(2, capacity);
                pstmt.setString(3, location);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Table added successfully.");
                loadTables();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Please enter valid numbers.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void removeTable() {
        String tableNumberStr = JOptionPane.showInputDialog(this, "Enter Table Number to Remove:");
        try {
            int tableNumber = Integer.parseInt(tableNumberStr);

            String sql = "DELETE FROM Tables WHERE TableNumber = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, tableNumber);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Table removed successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Table not found.");
                }
                loadTables();
            }
        } catch (NumberFormatException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

}
