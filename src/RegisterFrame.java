import java.awt.*;
import javax.swing.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class RegisterFrame extends JPanel {

    private JLabel label1, label2, label3, label4, label5, label6, label7, label8, label9, label10;
    private JTextField textField1, textField2, textField3, textField4, textField5, textField6, textField7, textField8;
    private JButton btnRegister, btnBack;

    public RegisterFrame() {
        initComponents();
    }

    private void initComponents() {
        label1 = new JLabel("Emri:");
        label2 = new JLabel("Mbiemri:");
        label3 = new JLabel("Username:");
        label4 = new JLabel("Password:");
        label5 = new JLabel("Turni:");
        label6 = new JLabel("Pozicioni:");
        label7 = new JLabel("Pagesa:");
        label8 = new JLabel("Detyra:");
        label9 = new JLabel("REGISTER");
        label9.setFont(new Font("Perpetua Titling MT", Font.BOLD, 24));
        label9.setForeground(new Color(60, 130, 200));

        label10 = new JLabel(new ImageIcon(getClass().getResource("/Images/edit.png")));

        textField1 = new JTextField();
        textField2 = new JTextField();
        textField3 = new JTextField();
        textField4 = new JTextField();
        textField5 = new JTextField();
        textField6 = new JTextField();
        textField7 = new JTextField();
        textField8 = new JTextField();

        btnRegister = new JButton("Register");
        btnBack = new JButton("Back to Paths");

        setLayout(null);
        addComponents();

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerEmployee();
            }
        });

        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPaths();
            }
        });
    }

    private void addComponents() {
        add(label1);
        label1.setBounds(285, 80, 100, 20);
        add(label2);
        label2.setBounds(285, 105, 100, 20);
        add(label3);
        label3.setBounds(285, 130, 100, 20);
        add(label4);
        label4.setBounds(285, 160, 100, 20);
        add(label5);
        label5.setBounds(285, 185, 100, 20);
        add(label6);
        label6.setBounds(285, 215, 100, 20);
        add(label7);
        label7.setBounds(285, 245, 100, 20);
        add(label8);
        label8.setBounds(285, 275, 100, 20);
        add(label9);
        label9.setBounds(95, 80, 200, 30);
        add(label10);
        label10.setBounds(120, 145, 80, 85);

        add(textField1);
        textField1.setBounds(380, 75, 120, 19);
        add(textField2);
        textField2.setBounds(380, 100, 120, 19);
        add(textField3);
        textField3.setBounds(380, 130, 120, 19);
        add(textField4);
        textField4.setBounds(380, 160, 120, 19);
        add(textField5);
        textField5.setBounds(380, 190, 120, 19);
        add(textField6);
        textField6.setBounds(380, 220, 120, 19);
        add(textField7);
        textField7.setBounds(380, 250, 120, 19);
        add(textField8);
        textField8.setBounds(380, 280, 120, 19);

        add(btnRegister);
        btnRegister.setBounds(300, 320, 120, 30);
        add(btnBack);
        btnBack.setBounds(300, 360, 120, 30);

        styleButton(btnRegister);
        styleButton(btnBack);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 130, 200));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void registerEmployee() {
        String emri = textField1.getText();
        String mbiemri = textField2.getText();
        String username = textField3.getText();
        String password = textField4.getText();
        String turni = textField5.getText();
        String pozicioni = textField6.getText();
        String pagesa = textField7.getText();
        String detyra = textField8.getText();

        String url = "jdbc:mysql://localhost:3306/magnusbar";
        String user = "root";
        String pass = "";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            String sql = "INSERT INTO employees (name, surname, username, password, shift, position, salary, duty) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, emri);
            pstmt.setString(2, mbiemri);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, turni);
            pstmt.setString(6, pozicioni);
            pstmt.setString(7, pagesa);
            pstmt.setString(8, detyra);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee registered successfully!");
            openPaths();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error registering employee: " + ex.getMessage());
            openPaths();
        }
    }

    private void openPaths() {
        JPanel pathsPanel = new Paths();
        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        mainFrame.setContentPane(pathsPanel);
        mainFrame.setSize(800, 400);
        mainFrame.revalidate();
        mainFrame.repaint();
    }
}