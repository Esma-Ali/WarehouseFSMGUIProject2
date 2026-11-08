

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class ManagerMenuState extends WarehouseState {

    private static ManagerMenuState instance;
    private Warehouse warehouse;
    private JFrame frame;

    private ManagerMenuState() {
        super();
        warehouse = Warehouse.instance();
    }

    public static ManagerMenuState instance() {
        if (instance == null) {
            instance = new ManagerMenuState();
        }
        return instance;
    }

    private void addProduct() {
        String name = JOptionPane.showInputDialog(
                frame, "Enter product name:");

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        String quantityInput = JOptionPane.showInputDialog(
                frame, "Enter quantity:");

        if (quantityInput == null) {
            return;
        }

        String priceInput = JOptionPane.showInputDialog(
                frame, "Enter unit price:");

        if (priceInput == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityInput);
            double price = Double.parseDouble(priceInput);

            if (quantity < 0 || price < 0) {
                JOptionPane.showMessageDialog(
                        frame, "Quantity and price cannot be negative.");
                return;
            }

            Product result = warehouse.addProduct(
                    name.trim(), quantity, price);

            if (result != null) {
                JOptionPane.showMessageDialog(
                        frame, "Product added: " + result);
            } else {
                JOptionPane.showMessageDialog(
                        frame, "Product could not be added.");
            }

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    frame, "Please enter valid numbers.");
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    frame, exception.getMessage());
        }
    }

    private void showWaitlist() {
        String productID = JOptionPane.showInputDialog(
                frame, "Enter product ID:");

        if (productID == null || productID.trim().isEmpty()) {
            return;
        }

        Iterator<WaitlistItem> items =
                warehouse.getProductWaitlist(productID.trim());

        if (items == null) {
            JOptionPane.showMessageDialog(
                    frame, "Product not found.");
            return;
        }

        StringBuilder result = new StringBuilder();

        while (items.hasNext()) {
            result.append(items.next()).append("\n");
        }

        if (result.length() == 0) {
            result.append("No clients are waiting for this product.");
        }

        JOptionPane.showMessageDialog(
                frame, result.toString(),
                "Product Waitlist", JOptionPane.INFORMATION_MESSAGE);
    }

    private void receiveShipment() {
        String productID = JOptionPane.showInputDialog(
                frame, "Enter product ID:");

        if (productID == null || productID.trim().isEmpty()) {
            return;
        }

        String quantityInput = JOptionPane.showInputDialog(
                frame, "Enter received quantity:");

        if (quantityInput == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityInput);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(
                        frame, "Quantity must be greater than zero.");
                return;
            }

            Iterator<Invoice> invoices =
                    warehouse.receiveShipment(productID.trim(), quantity);

            if (invoices == null) {
                JOptionPane.showMessageDialog(
                        frame, "Shipment could not be processed. Check the product ID.");
                return;
            }

            StringBuilder result = new StringBuilder();

            while (invoices.hasNext()) {
                result.append(invoices.next()).append("\n");
            }

            if (result.length() == 0) {
                result.append("No waitlist orders were filled. Remaining stock was added.");
            }

            JOptionPane.showMessageDialog(
                    frame, result.toString(),
                    "Shipment Results", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                    frame, "Please enter a valid quantity.");
        }
    }

    private void becomeClerk() {
        WarehouseContext.instance().becomeClerk();
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
                "Manager Menu", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton addProductButton = new JButton(
                new AbstractAction("Add Product") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        addProduct();
                    }
                });

        JButton waitlistButton = new JButton(
                new AbstractAction("Show Waitlist") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showWaitlist();
                    }
                });

        JButton shipmentButton = new JButton(
                new AbstractAction("Receive Shipment") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        receiveShipment();
                    }
                });

        JButton clerkButton = new JButton(
                new AbstractAction("Become Clerk") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        becomeClerk();
                    }
                });

        JButton logoutButton = new JButton(
                new AbstractAction("Logout") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        logout();
                    }
                });

        buttonPanel.add(addProductButton);
        buttonPanel.add(waitlistButton);
        buttonPanel.add(shipmentButton);
        buttonPanel.add(clerkButton);
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
