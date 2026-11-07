import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginState extends WarehouseState {
    private static LoginState instance;
    private JFrame frame;

    private LoginState() {
        super();
    }

    public static LoginState instance() {
        if (instance == null) {
            instance = new LoginState();
        }
        return instance;
    }

    private void loginClient() {
        String clientID = JOptionPane.showInputDialog(
                frame,
                "Enter client ID:",
                "Client Login",
                JOptionPane.QUESTION_MESSAGE);

        if (clientID == null) {
            return;
        }

        if (!WarehouseContext.instance().login(
                WarehouseContext.CLIENT_STATE, clientID.trim())) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Invalid client ID.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loginClerk() {
        WarehouseContext.instance().login(
                WarehouseContext.CLERK_STATE, null);
    }

    private void loginManager() {
        WarehouseContext.instance().login(
                WarehouseContext.MANAGER_STATE, null);
    }

    private void exitProgram() {
        int choice = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to exit?",
                "Exit Warehouse",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            WarehouseContext.instance().stop();
        }
    }

    @Override
    public void run() {
        frame = WarehouseContext.instance().getFrame();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel(
                "Warehouse Login",
                SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));

        JButton clientButton = new JButton(
                new AbstractAction("Login as Client") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        loginClient();
                    }
                });

        JButton clerkButton = new JButton(
                new AbstractAction("Login as Clerk") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        loginClerk();
                    }
                });

        JButton managerButton = new JButton(
                new AbstractAction("Login as Manager") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        loginManager();
                    }
                });

        JButton exitButton = new JButton(
                new AbstractAction("Exit") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        exitProgram();
                    }
                });

        buttonPanel.add(clientButton);
        buttonPanel.add(clerkButton);
        buttonPanel.add(managerButton);
        buttonPanel.add(exitButton);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.toFront();
    }
}
