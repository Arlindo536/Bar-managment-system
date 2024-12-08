import javax.swing.*;

public class  Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                JFrame frame = new JFrame("Login");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(770, 450);
                frame.setLocationRelativeTo(null);

                frame.setContentPane(new LoginFrame());
                frame.setVisible(true);
            }
        });
    }
}
