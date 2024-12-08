import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
public class Menaxhimi extends JPanel {

    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private JTextField nameField, priceField, quantityField, categoryField;
    private JTextArea invoiceArea;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public Menaxhimi() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 255));

        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Shto ose Përditëso Produkte"));
        inputPanel.setBackground(new Color(245, 250, 255));

        JPanel invoicePanel = new JPanel(new BorderLayout());
        invoicePanel.setBorder(BorderFactory.createTitledBorder("Faturë"));
        invoicePanel.setBackground(new Color(245, 250, 255));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Lista e Produkteve"));
        listPanel.setBackground(new Color(245, 250, 255));

        JLabel nameLabel = new JLabel("Emri:");
        JLabel priceLabel = new JLabel("Çmimi:");
        JLabel quantityLabel = new JLabel("Sasia:");
        JLabel categoryLabel = new JLabel("Kategoria:");

        nameField = new JTextField();
        priceField = new JTextField();
        quantityField = new JTextField();
        categoryField = new JTextField();

        styleLabel(nameLabel);
        styleLabel(priceLabel);
        styleLabel(quantityLabel);
        styleLabel(categoryLabel);

        JButton backButton = createStyledButton("Back to Paths");
        JButton addButton = createStyledButton("Shto Produkt");
        JButton updateButton = createStyledButton("Përditëso");
        JButton deleteButton = createStyledButton("Fshi");
        JButton retrieveButton = createStyledButton("Kërko");

        backButton.addActionListener(e -> returnToPaths(backButton));

        invoiceArea = new JTextArea();
        invoiceArea.setEditable(false);
        invoiceArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        invoiceArea.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        invoicePanel.add(new JScrollPane(invoiceArea), BorderLayout.CENTER);

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(priceLabel);
        inputPanel.add(priceField);
        inputPanel.add(quantityLabel);
        inputPanel.add(quantityField);
        inputPanel.add(categoryLabel);
        inputPanel.add(categoryField);
        inputPanel.add(addButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);
        inputPanel.add(retrieveButton);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Emri", "Çmimi", "Sasia", "Kategoria"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        productTable.getTableHeader().setBackground(new Color(70, 130, 180));
        productTable.getTableHeader().setForeground(Color.WHITE);

        listPanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, invoicePanel);
        splitPane.setDividerLocation(500);
        splitPane.setDividerSize(5);
        splitPane.setBackground(new Color(230, 240, 255));

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, listPanel);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setDividerSize(5);

        add(mainSplitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(230, 240, 255));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> handleAddProduct());
        updateButton.addActionListener(e -> handleUpdateProduct());
        deleteButton.addActionListener(e -> handleDeleteProduct());
        retrieveButton.addActionListener(e -> handleRetrieveProduct());

        try {
            loadProducts();
            checkLowStockProducts();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage());
        }
    }

    private void styleLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(70, 130, 180));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
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

    private void returnToPaths(JButton backButton) {
        JFrame pathsFrame = new JFrame("Paths");
        pathsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pathsFrame.setContentPane(new Paths());
        pathsFrame.setSize(800, 400);
        pathsFrame.setLocationRelativeTo(null);
        pathsFrame.setVisible(true);

        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(backButton);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    private void handleAddProduct() {
        String name = nameField.getText();
        String type = categoryField.getText();

        try {
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            addProduct(name, price, quantity, type);
            invoiceArea.append("Produkti u shtua: " + name + "\n");
            loadProducts();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Çmimi dhe sasia duhet të jenë numra!", "Gabim", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleUpdateProduct() {
        String name = nameField.getText();

        try {
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            updateProduct(name, price, quantity);
            invoiceArea.append("Produkti u përditësua: " + name + "\n");
            loadProducts();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Çmimi dhe sasia duhet të jenë numra!", "Gabim", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleDeleteProduct() {
        String name = nameField.getText();

        try {
            deleteProduct(name);
            invoiceArea.append("Produkti u fshi: " + name + "\n");
            loadProducts();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handleRetrieveProduct() {
        String name = nameField.getText();

        try {
            retrieveProduct(name);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    private void addProduct(String name, double price, int quantity, String type) throws SQLException {
        String sql = "INSERT INTO products (name, price, quantity, type, sales, description, salesDate) VALUES (?, ?, ?, ?, 0, '', CURDATE())";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, type);
            pstmt.executeUpdate();
            checkLowStockProducts();
        }
    }

    private void updateProduct(String name, double price, int quantity) throws SQLException {
        String sqlSelect = "SELECT sales, description, salesDate FROM products WHERE name = ?";
        String sqlUpdate = "UPDATE products SET price = ?, quantity = ?, type = ?, sales = ?, description = ?, salesDate = ? WHERE name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate)) {

            pstmtSelect.setString(1, name);
            ResultSet rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                int sales = rs.getInt("sales");
                String description = rs.getString("description");
                Date salesDate = rs.getDate("salesDate");

                pstmtUpdate.setDouble(1, price);
                pstmtUpdate.setInt(2, quantity);
                pstmtUpdate.setString(3, categoryField.getText());
                pstmtUpdate.setInt(4, sales);
                pstmtUpdate.setString(5, description);
                pstmtUpdate.setDate(6, salesDate);
                pstmtUpdate.setString(7, name);

                pstmtUpdate.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found: " + name, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct(String name) throws SQLException {
        String sql = "DELETE FROM products WHERE name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    private void retrieveProduct(String name) throws SQLException {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String productName = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String type = rs.getString("type");
                invoiceArea.append("Emri: " + productName + "\nÇmimi: " + price + "\nSasia: " + quantity + "\nKategoria: " + type + "\n");
            } else {
                invoiceArea.append("Produkti nuk u gjet.\n");
            }
        }
    }

    private void loadProducts() throws SQLException {
        String sql = "SELECT * FROM products";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String type = rs.getString("type");
                int sales = rs.getInt("sales");
                String description = rs.getString("description");
                Date salesDate = rs.getDate("salesDate");

                tableModel.addRow(new Object[]{id, name, price, quantity, type, sales, description, salesDate});
            }
        }
    }


    private void checkLowStockProducts() {
        StringBuilder lowStockProducts = new StringBuilder();
        boolean hasLowStock = false;

        try {
            String sql = "SELECT name, quantity FROM products WHERE quantity < 10";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String name = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    lowStockProducts.append("Product: ").append(name).append(", Quantity: ").append(quantity).append("\n");
                    hasLowStock = true;
                }
            }

            if (hasLowStock) {
                JOptionPane.showMessageDialog(this,
                        "The following products are low in stock:\n\n" + lowStockProducts,
                        "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}