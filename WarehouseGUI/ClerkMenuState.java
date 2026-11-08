import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class ClerkMenuState extends WarehouseState {

    private static ClerkMenuState instance;
    private Warehouse warehouse;
    private JFrame frame;

    private ClerkMenuState() {
        super();
        warehouse = Warehouse.instance();
    }

    public static ClerkMenuState instance() {
        if (instance == null) {
            instance = new ClerkMenuState();
        }
        return instance;
    }

    // Shows multi-line output in a scrollable text area.
    private void showResults(String title, String text) {
        JTextArea area = new JTextArea(text, 12, 50);
        area.setEditable(false);
        area.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(
                frame, new JScrollPane(area),
                title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void addClient() {
        int choice;

        do {
            String name = JOptionPane.showInputDialog(
                    frame, "Enter client name:");

            if (name == null || name.trim().isEmpty()) {
                return;
            }

            String address = JOptionPane.showInputDialog(
                    frame, "Enter client address:");

            if (address == null || address.trim().isEmpty()) {
                return;
            }

            Client result = warehouse.addClient(name.trim(), address.trim());

            if (result == null) {
                JOptionPane.showMessageDialog(
                        frame, "Could not add client.");
            } else {
                JOptionPane.showMessageDialog(
                        frame, "Added " + result);
            }

            choice = JOptionPane.showConfirmDialog(
                    frame, "Add another client?",
                    "Add Client", JOptionPane.YES_NO_OPTION);
        } while (choice == JOptionPane.YES_OPTION);
    }

    private void showProducts() {
        Iterator<Product> products = warehouse.getProducts();
        StringBuilder result = new StringBuilder();

        while (products.hasNext()) {
            result.append(products.next()).append("\n");
        }

        if (result.length() == 0) {
            result.append("No products in the warehouse.");
        }

        showResults("All Products", result.toString());
    }

    private void showClients() {
        Iterator<Client> clients = warehouse.getClients();
        StringBuilder result = new StringBuilder();

        while (clients.hasNext()) {
            result.append(clients.next()).append("\n");
        }

        if (result.length() == 0) {
            result.append("No clients in the warehouse.");
        }

        showResults("All Clients", result.toString());
    }

    // A positive balance means the client owes money.
    private void showClientsWithBalance() {
        Iterator<Client> clients = warehouse.getClients();
        StringBuilder result = new StringBuilder();

        while (clients.hasNext()) {
            Client client = clients.next();

            if (client.getBalance() > 0) {
                result.append(client).append("\n");
            }
        }

        if (result.length() == 0) {
            result.append("No clients have an outstanding balance.");
        }

        showResults("Clients With Outstanding Balance", result.toString());
    }

    private void recordPayment() {
        String clientID = JOptionPane.showInputDialog(
                frame, "Enter client ID:");

        if (clientID == null || clientID.trim().isEmpty()) {
            return;
        }

        clientID = clientID.trim();

        if (warehouse.getClient(clientID) == null) {
            JOptionPane.showMessageDialog(
                    frame, "Client ID not found.");
            return;
        }

        String amountInput = JOptionPane.showInputDialog(
                frame, "Enter payment amount:");

        if (amountInput == null) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountInput.trim());

            if (!Double.isFinite(amount)) {
                throw new NumberFormatException();
            }

            Double balance = warehouse.recordPayment(clientID, amount);

            if (balance == null) {
                JOptionPane.showMessageDialog(
                        frame, "Payment must be a positive amount.");
            } else {
                JOptionPane.showMessageDialog(
                        frame, "Payment recorded. New balance: $"
                                + String.format("%.2f", balance));
            }
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    frame, "Please enter a valid amount.");
        }
    }

    private void becomeClient() {
        String clientID = JOptionPane.showInputDialog(
                frame, "Enter client ID (e.g. C1):");

        if (clientID == null || clientID.trim().isEmpty()) {
            return;
        }

        if (!WarehouseContext.instance().becomeClient(clientID.trim())) {
            JOptionPane.showMessageDialog(
                    frame, "Invalid client ID.",
                    "Become Client", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        WarehouseContext.instance().logout();
    }

    @Override
    public void run() {
        frame = WarehouseContext.instance().getFrame();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 20));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel(
                "Clerk Menu", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));

        // Each button has its own action
        JButton addClientButton = new JButton(
                new AbstractAction("Add Client") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        addClient();
                    }
                });

        JButton showProductsButton = new JButton(
                new AbstractAction("Show Products") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showProducts();
                    }
                });

        JButton showClientsButton = new JButton(
                new AbstractAction("Show Clients") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showClients();
                    }
                });

        JButton balanceButton = new JButton(
                new AbstractAction("Show Clients With Outstanding Balance") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showClientsWithBalance();
                    }
                });

        JButton paymentButton = new JButton(
                new AbstractAction("Record Payment") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        recordPayment();
                    }
                });

        JButton clientButton = new JButton(
                new AbstractAction("Become Client") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        becomeClient();
                    }
                });

        JButton logoutButton = new JButton(
                new AbstractAction("Logout") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        logout();
                    }
                });

        buttonPanel.add(addClientButton);
        buttonPanel.add(showProductsButton);
        buttonPanel.add(showClientsButton);
        buttonPanel.add(balanceButton);
        buttonPanel.add(paymentButton);
        buttonPanel.add(clientButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.toFront();
    }
}
