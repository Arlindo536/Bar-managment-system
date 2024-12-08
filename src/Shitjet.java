import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.awt.event.ActionListener;

public class Shitjet {
    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private JTable table;
    private DefaultTableModel tableModel;
    private JFrame frame;

    public Shitjet() {
        frame = new JFrame("Shitjet");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(230, 245, 255)); // Light background color

        String[] columnNames = {"Product Name", "Quantity", "Sale Price", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 250));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        buttonsPanel.setBackground(new Color(230, 245, 255)); // Consistent background color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton totalSalesTodayButton = createStyledButton("Total Sales Today", e -> getTodaySales());
        JButton totalSalesMonthButton = createStyledButton("Total Sales This Month", e -> getMonthlySales());
        JButton salesBreakdownButton = createStyledButton("Sales Breakdown for the Month", e -> getMonthlySalesBreakdown());
        JButton backToPathsButton = createStyledButton("Back to Paths", e -> backToPaths());

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonsPanel.add(totalSalesTodayButton, gbc);

        gbc.gridx = 1;
        buttonsPanel.add(totalSalesMonthButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonsPanel.add(salesBreakdownButton, gbc);

        gbc.gridx = 1;
        buttonsPanel.add(backToPathsButton, gbc);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void showFrame() {
        frame.setVisible(true);
    }

    private JButton createStyledButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 130, 200));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(actionListener);
        return button;
    }
    private void backToPaths() {
        JFrame pathsFrame = new JFrame("Paths");
        pathsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pathsFrame.setContentPane(new Paths());
        pathsFrame.setSize(800, 400);
        pathsFrame.setLocationRelativeTo(null);
        pathsFrame.setVisible(true);
        frame.dispose();
    }
    public void getTodaySales() {
        LocalDate today = LocalDate.now();
        String sql = "SELECT p.name, SUM(s.quantity_sold) AS total_quantity, s.sale_price, SUM(s.quantity_sold * s.sale_price) AS total " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "WHERE s.sale_date = ? GROUP BY p.name, s.sale_price";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(today));
            ResultSet rs = pstmt.executeQuery();

            tableModel.setRowCount(0);

            double totalSalesForToday = 0;
            while (rs.next()) {
                String productName = rs.getString("name");
                int totalQuantity = rs.getInt("total_quantity");
                double salePrice = rs.getDouble("sale_price");
                double total = rs.getDouble("total");
                totalSalesForToday += total;

                tableModel.addRow(new Object[]{productName, totalQuantity, salePrice, total});
            }

            JOptionPane.showMessageDialog(frame, "Total Sales for Today: " + totalSalesForToday, "Today's Sales", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: Could not retrieve today's sales data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getMonthlySales() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        String sql = "SELECT p.name, SUM(s.quantity_sold) AS total_quantity, SUM(s.quantity_sold * s.sale_price) AS total_sales " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "WHERE s.sale_date BETWEEN ? AND ? GROUP BY p.name ORDER BY total_sales DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startOfMonth));
            pstmt.setDate(2, Date.valueOf(endOfMonth));
            ResultSet rs = pstmt.executeQuery();

            tableModel.setRowCount(0);

            double totalSalesForMonth = 0;
            while (rs.next()) {
                String productName = rs.getString("name");
                int totalQuantity = rs.getInt("total_quantity");
                double totalSales = rs.getDouble("total_sales");
                totalSalesForMonth += totalSales;

                tableModel.addRow(new Object[]{productName, totalQuantity, totalSales});
            }

            JOptionPane.showMessageDialog(frame, "Total Sales for the Month: " + totalSalesForMonth, "Monthly Sales", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: Could not retrieve this month's sales data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getMonthlySalesBreakdown() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        String dailySalesSQL = "SELECT DATE(s.sale_date) AS salesDate, SUM(s.quantity_sold * s.sale_price) AS dailyTotal " +
                "FROM sales s " +
                "WHERE s.sale_date BETWEEN ? AND ? " +
                "GROUP BY DATE(s.sale_date) " +
                "ORDER BY salesDate";

        String monthlySalesSQL = "SELECT SUM(s.quantity_sold * s.sale_price) AS monthlyTotal " +
                "FROM sales s " +
                "WHERE s.sale_date BETWEEN ? AND ?";

        String topProductSQL = "SELECT p.name, SUM(s.quantity_sold) AS totalQuantity " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "WHERE s.sale_date BETWEEN ? AND ? " +
                "GROUP BY p.name ORDER BY totalQuantity DESC LIMIT 1";

        String topThreeProductsSQL = "SELECT p.name, SUM(s.quantity_sold * s.sale_price) AS totalSales " +
                "FROM sales s JOIN products p ON s.product_id = p.id " +
                "WHERE s.sale_date BETWEEN ? AND ? " +
                "GROUP BY p.name ORDER BY totalSales DESC LIMIT 3";

        String topDaySQL = "SELECT DATE(s.sale_date) AS salesDate, SUM(s.quantity_sold * s.sale_price) AS dailyTotal " +
                "FROM sales s " +
                "WHERE s.sale_date BETWEEN ? AND ? " +
                "GROUP BY DATE(s.sale_date) ORDER BY dailyTotal DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            try (PreparedStatement dailyStmt = conn.prepareStatement(dailySalesSQL);
                 PreparedStatement monthlyStmt = conn.prepareStatement(monthlySalesSQL);
                 PreparedStatement topProductStmt = conn.prepareStatement(topProductSQL);
                 PreparedStatement topThreeProductsStmt = conn.prepareStatement(topThreeProductsSQL);
                 PreparedStatement topDayStmt = conn.prepareStatement(topDaySQL)) {

                dailyStmt.setDate(1, Date.valueOf(startOfMonth));
                dailyStmt.setDate(2, Date.valueOf(endOfMonth));
                monthlyStmt.setDate(1, Date.valueOf(startOfMonth));
                monthlyStmt.setDate(2, Date.valueOf(endOfMonth));
                topProductStmt.setDate(1, Date.valueOf(startOfMonth));
                topProductStmt.setDate(2, Date.valueOf(endOfMonth));
                topThreeProductsStmt.setDate(1, Date.valueOf(startOfMonth));
                topThreeProductsStmt.setDate(2, Date.valueOf(endOfMonth));
                topDayStmt.setDate(1, Date.valueOf(startOfMonth));
                topDayStmt.setDate(2, Date.valueOf(endOfMonth));

                ResultSet monthlyRs = monthlyStmt.executeQuery();
                double totalSalesForMonth = 0;
                if (monthlyRs.next()) {
                    totalSalesForMonth = monthlyRs.getDouble("monthlyTotal");
                }

                ResultSet topProductRs = topProductStmt.executeQuery();
                String topProduct = "";
                if (topProductRs.next()) {
                    topProduct = topProductRs.getString("name");
                }

                ResultSet topThreeProductsRs = topThreeProductsStmt.executeQuery();
                StringBuilder topThreeProducts = new StringBuilder();
                while (topThreeProductsRs.next()) {
                    String productName = topThreeProductsRs.getString("name");
                    topThreeProducts.append(productName).append(" ");
                }

                ResultSet topDayRs = topDayStmt.executeQuery();
                String topDay = "";
                double topDaySales = 0;
                if (topDayRs.next()) {
                    topDay = topDayRs.getString("salesDate");
                    topDaySales = topDayRs.getDouble("dailyTotal");
                }

                String message = "Monthly Total Sales: " + totalSalesForMonth +
                        "\nTop-Selling Product: " + topProduct +
                        "\nTop 3 Products by Sales: " + topThreeProducts.toString() +
                        "\nDay with Most Sales: " + topDay + " (Total: " + topDaySales + ")";
                JOptionPane.showMessageDialog(frame, message, "Sales Breakdown for the Month", JOptionPane.INFORMATION_MESSAGE);

                ResultSet dailySalesRs = dailyStmt.executeQuery();
                tableModel.setRowCount(0);
                while (dailySalesRs.next()) {
                    Date saleDate = dailySalesRs.getDate("salesDate");
                    double dailyTotal = dailySalesRs.getDouble("dailyTotal");
                    tableModel.addRow(new Object[]{saleDate.toString(), dailyTotal});
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: Could not retrieve monthly sales breakdown.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
