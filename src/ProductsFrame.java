import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
public class ProductsFrame extends JPanel {

    private Shitjet shitjet;
    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private JTextArea orderSummaryArea;
    private double totalPrice;
    private Map<String, Integer> productQuantities;
    private JPanel productsPanel;

    public ProductsFrame(Shitjet shitjet) {
        this.shitjet = shitjet;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(230, 245, 255));
        productQuantities = new HashMap<>();

        JPanel topPanel = createTopPanel();
        JScrollPane productsScrollPane = createProductsScrollPane();
        JPanel orderPanel = createOrderPanel();
        JPanel southPanel = createSouthPanel();

        add(topPanel, BorderLayout.NORTH);
        add(productsScrollPane, BorderLayout.WEST);
        add(orderPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchTextField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchProducts(searchTextField.getText()));
        styleButton(searchButton);

        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);

        JLabel categoryTitleLabel = new JLabel("Kategorite");
        categoryTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        categoryTitleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel categoriesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoriesPanel.setPreferredSize(new Dimension(800, 50));
        categoriesPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));

        loadCategories(categoriesPanel);

        topPanel.add(searchPanel);
        topPanel.add(categoryTitleLabel);
        topPanel.add(categoriesPanel);

        return topPanel;
    }

    private JScrollPane createProductsScrollPane() {
        JPanel productsContainerPanel = new JPanel();
        productsContainerPanel.setLayout(new BoxLayout(productsContainerPanel, BoxLayout.Y_AXIS));
        productsContainerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel productsTitleLabel = new JLabel("Produktet");
        productsTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        productsTitleLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));

        loadProducts(null);

        productsContainerPanel.add(productsTitleLabel);
        productsContainerPanel.add(productsPanel);

        JScrollPane productsScrollPane = new JScrollPane(productsContainerPanel);
        productsScrollPane.setPreferredSize(new Dimension(250, 500));
        productsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return productsScrollPane;
    }

    private JPanel createOrderPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        orderSummaryArea = new JTextArea(15, 30);
        orderSummaryArea.setEditable(false);
        orderSummaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        orderSummaryArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JScrollPane orderScrollPane = new JScrollPane(orderSummaryArea);
        orderScrollPane.setPreferredSize(new Dimension(550, 400));

        orderPanel.add(orderScrollPane, BorderLayout.CENTER);

        return orderPanel;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        southPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton checkoutButton = new JButton("Checkout");
        styleButton(checkoutButton);
        checkoutButton.addActionListener(e -> checkout());

        JButton clearOrderButton = new JButton("Clear Order");
        styleButton(clearOrderButton);
        clearOrderButton.addActionListener(e -> clearOrder());

        JButton removeProductButton = new JButton("Remove Product");
        styleButton(removeProductButton);
        removeProductButton.addActionListener(e -> removeFromOrder());

        JButton backButton = new JButton("Back to Paths");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                JFrame pathsFrame = new JFrame("Paths");
                pathsFrame.setContentPane(new Paths());
                pathsFrame.setSize(800, 400);
                pathsFrame.setLocationRelativeTo(null);
                pathsFrame.setVisible(true);
                SwingUtilities.getWindowAncestor(ProductsFrame.this).dispose();
            });
        });

        southPanel.add(checkoutButton);
        southPanel.add(clearOrderButton);
        southPanel.add(removeProductButton);
        southPanel.add(backButton);

        return southPanel;
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 130, 200));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }


    private void loadCategories(JPanel categoriesPanel) {
        String sql = "SELECT DISTINCT type FROM products";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String categoryName = rs.getString("type");
                JButton categoryButton = new JButton(categoryName);
                categoryButton.setBackground(Color.CYAN);
                categoryButton.addActionListener(e -> loadProducts(categoryName));
                categoriesPanel.add(categoryButton);
            }

            JButton allProductsButton = new JButton("All Products");
            allProductsButton.setBackground(Color.CYAN);
            allProductsButton.addActionListener(e -> loadProducts(null));
            categoriesPanel.add(allProductsButton);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchProducts(String searchTerm) {
        productsPanel.removeAll();
        String sql = "SELECT name, price FROM products WHERE name LIKE ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("name");
                    double price = rs.getDouble("price");

                    JButton productButton = new JButton(productName);
                    productButton.setBackground(new Color(255, 182, 193));
                    productButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productButton.setPreferredSize(new Dimension(120, 30));

                    productButton.addActionListener(e -> addToOrder(productName, price));

                    productsPanel.add(productButton);
                    productsPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void loadProducts(String category) {
        productsPanel.removeAll();
        String sql = "SELECT name, price FROM products" + (category != null ? " WHERE type = ?" : "");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (category != null) {
                pstmt.setString(1, category);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("name");
                    double price = rs.getDouble("price");

                    JButton productButton = new JButton(productName);
                    productButton.setBackground(new Color(255, 182, 193));
                    productButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productButton.setPreferredSize(new Dimension(120, 30));

                    productButton.addActionListener(e -> addToOrder(productName, price));

                    productsPanel.add(productButton);
                    productsPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        productsPanel.revalidate();
        productsPanel.repaint();
    }

    private void addToOrder(String name, double price) {
        productQuantities.put(name, productQuantities.getOrDefault(name, 0) + 1);
        updateOrderSummary();
    }

    private void removeFromOrder() {
        String productName = JOptionPane.showInputDialog(this, "Enter the product name to remove:", "Remove Product", JOptionPane.PLAIN_MESSAGE);

        if (productName == null || productName.isEmpty() || !productQuantities.containsKey(productName)) {
            JOptionPane.showMessageDialog(this, productName + " not found in order.", "Product Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter the quantity to remove:", "Remove Quantity", JOptionPane.PLAIN_MESSAGE);
        int quantityToRemove = Integer.parseInt(quantityStr);

        int currentQuantity = productQuantities.get(productName);
        if (quantityToRemove <= 0 || quantityToRemove > currentQuantity) {
            JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a value between 1 and " + currentQuantity, "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (quantityToRemove == currentQuantity) {
            productQuantities.remove(productName);
        } else {
            productQuantities.put(productName, currentQuantity - quantityToRemove);
        }

        updateOrderSummary();
        JOptionPane.showMessageDialog(this, quantityToRemove + " of " + productName + " removed from order.", "Product Removed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateOrderSummary() {
        orderSummaryArea.setText("");
        double total = 0;
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            double price = getProductPriceByName(name);
            orderSummaryArea.append(name + " - Quantity: " + quantity + " - Çmimi: " + price + " each\n");
            total += price * quantity;
        }
        orderSummaryArea.append("\nTotal: " + total);
    }

    private double getProductPriceByName(String name) {
        String sql = "SELECT price FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void reduceProductQuantity(String name, int quantityToDeduct) {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE name = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityToDeduct);
            pstmt.setString(2, name);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void checkout() {
        totalPrice = 0;
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String name = entry.getKey();
            int quantity = entry.getValue();
            double price = getProductPriceByName(name);
            totalPrice += price * quantity;

            int productId = getProductIdByName(name);
            insertSale(productId, quantity, price);

            reduceProductQuantity(name, quantity);
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Total price: " + totalPrice + "\nDo you want to proceed with the checkout?",
                "Checkout",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Checkout successful! Total: " + totalPrice);
            productQuantities.clear();
            updateOrderSummary();
        }
    }

    private void insertSale(int productId, int quantitySold, double salePrice) {
        String insertSaleSql = "INSERT INTO sales (product_id, quantity_sold, sale_price, sale_date) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(insertSaleSql)) {

            pstmt.setInt(1, productId);
            pstmt.setInt(2, quantitySold);
            pstmt.setDouble(3, salePrice);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getProductIdByName(String name) {
        String sql = "SELECT id FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void clearOrder() {
        productQuantities.clear();
        updateOrderSummary();
    }
}
