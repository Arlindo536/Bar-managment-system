import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginFrame extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private static final String URL = "jdbc:mysql://localhost:3306/magnusbar";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public LoginFrame() {
        initComponents();
        addLoginButtonListener();
    }

    private void initComponents() {
        // Title and Labels
        JLabel labelUsername = new JLabel("Username:");
        JLabel labelPassword = new JLabel("Password:");
        JLabel labelTitle = new JLabel("MagnusBar");

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        // Set up fonts and layout
        labelTitle.setFont(new Font("Kristen ITC", Font.BOLD, 30));
        labelUsername.setFont(new Font("Inter", Font.PLAIN, 18));
        labelPassword.setFont(new Font("Inter", Font.PLAIN, 18));

        labelTitle.setForeground(new Color(60, 130, 200));
        labelUsername.setForeground(Color.BLACK);
        labelPassword.setForeground(Color.BLACK);

        // Set Layout and Bounds
        setLayout(null);
        labelTitle.setBounds(115, 50, 210, 40); // Title placement
        labelUsername.setBounds(20, 150, 115, 30);
        labelPassword.setBounds(20, 190, 115, 30);
        usernameField.setBounds(150, 150, 215, 30);
        passwordField.setBounds(150, 190, 215, 30);
        loginButton.setBounds(150, 230, 100, 40);

        // Add components to the panel
        add(labelTitle);
        add(labelUsername);
        add(usernameField);
        add(labelPassword);
        add(passwordField);
        add(loginButton);

        // Add background image (if exists)
        ImageIcon icon = new ImageIcon(getClass().getResource("/Images/whiskey.jpg"));
        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            Image image = icon.getImage();
            Image scaledImage = image.getScaledInstance(370, 430, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setBounds(370, 0, 470, 430);
            add(imageLabel);
        } else {
            System.out.println("Image not found or invalid.");
        }

        // Style the login button
        styleButton(loginButton);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 130, 200)); // Blue color
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private boolean validateUser(String username, String password) {
        // Validate user based on username and password only
        String query = "SELECT * FROM employees WHERE username = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            // If a record exists, user is valid
            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error. Please check your connection and credentials.");
            return false;
        }
    }

    private void addLoginButtonListener() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Validate the user based on username and password
                if (validateUser(username, password)) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login successful!");

                    // Open Paths frame for all users
                    JFrame pathsFrame = new JFrame("Paths");
                    pathsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    pathsFrame.setSize(800, 400);
                    pathsFrame.setLocationRelativeTo(null);
                    pathsFrame.add(new Paths()); // Assuming Paths is a valid JPanel or JFrame
                    pathsFrame.setVisible(true);

                    // Close the login frame
                    SwingUtilities.getWindowAncestor(LoginFrame.this).dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password.");
                }
            }
        });
    }
}
