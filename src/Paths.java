import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Paths extends JPanel {

    private JButton button1, button2, button3, button4, button5, button6, button7;
    private JLabel label1;

    public Paths() {
        initComponents();
    }

    private void initComponents() {
        button1 = new JButton("Kasa");
        button2 = new JButton("Produktet");
        button3 = new JButton("Rregjistrimi");
        button4 = new JButton("<- Login");
        button5 = new JButton("Rezervimet");
        button6 = new JButton("Shitjet");
        button7 = new JButton("Punonjesit");

        label1 = new JLabel(new ImageIcon(getClass().getResource("/Images/arrows.png")));

        setLayout(new GridBagLayout());
        setBackground(new Color(230, 240, 255));

        Dimension buttonSize = new Dimension(180, 50);
        styleButton(button1, buttonSize);
        styleButton(button2, buttonSize);
        styleButton(button3, buttonSize);
        styleButton(button4, buttonSize);
        styleButton(button5, buttonSize);
        styleButton(button6, buttonSize);
        styleButton(button7, buttonSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(button1, gbc);

        gbc.gridx = 1;
        add(button2, gbc);

        gbc.gridx = 2;
        add(button6, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(button5, gbc);

        gbc.gridx = 1;
        add(button3, gbc);

        gbc.gridx = 2;
        add(button7, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(label1, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(button4, gbc);

        button1.addActionListener(e -> openFrame(new ProductsFrame(new Shitjet()), "Kasa:", 900, 600));
        button2.addActionListener(e -> openFrame(new Menaxhimi(), "Menaxhimi", 1000, 600));
        button3.addActionListener(e -> openFrame(new RegisterFrame(), "Register", 600, 450));
        button4.addActionListener(e -> openFrame(new LoginFrame(), "Login", 770, 420));
        button5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Rezervimet rezervimetFrame = new Rezervimet();
                    rezervimetFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    rezervimetFrame.setSize(1000, 600);
                    rezervimetFrame.setLocationRelativeTo(null);
                    rezervimetFrame.setResizable(false);
                    rezervimetFrame.setVisible(true);

                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(Paths.this);
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Paths.this, "Error opening Rezervimet frame: " + ex.getMessage());
                }
            }
        });

        button6.addActionListener(e -> {
            Shitjet shitjet = new Shitjet();
            shitjet.showFrame();
            SwingUtilities.getWindowAncestor(Paths.this).dispose();
        });
        button7.addActionListener(e -> {
            try {
                JFrame punonjesitFrame = new JFrame("Punonjesit");
                punonjesitFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                punonjesitFrame.setContentPane(new Punonjesit());
                punonjesitFrame.setSize(1000, 400);
                punonjesitFrame.setLocationRelativeTo(null);
                punonjesitFrame.setResizable(true);
                punonjesitFrame.setVisible(true);
                SwingUtilities.getWindowAncestor(Paths.this).dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Paths.this, "Error opening Punonjesit panel: " + ex.getMessage());
            }
        });
    }

    private void styleButton(JButton button, Dimension size) {
        button.setPreferredSize(size);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
    }

    private void openFrame(JPanel panel, String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(panel);
        frame.setResizable(true);
        frame.setVisible(true);
        SwingUtilities.getWindowAncestor(this).dispose();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Paths");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);
        frame.add(new Paths());
        frame.setVisible(true);
    }
}
