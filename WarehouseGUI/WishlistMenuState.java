import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class WishlistMenuState extends WarehouseState {
    private static WishlistMenuState instance;

    private final Warehouse warehouse;
    private final WarehouseContext context;

    private JFrame frame;
    private JTextArea outputArea;
    private String clientID;

    private WishlistMenuState() {
        super();
        warehouse = Warehouse.instance();
        context = WarehouseContext.instance();
    }

    public static WishlistMenuState instance() {
        if (instance == null) {
            instance = new WishlistMenuState();
        }
        return instance;
    }

    private void show(String text) {
        outputArea.setText(text);
        outputArea.setCaretPosition(0);
    }

    private void addToWishlist() {
        String productID = JOptionPane.showInputDialog(
                frame, "Enter product ID (for example, P1):");

        if (productID == null || productID.trim().isEmpty()) {
            return;
        }

        productID = productID.trim();

        if (warehouse.getProduct(productID) == null) {
            show("Product ID not found.");
            return;
        }

        String quantityInput = JOptionPane.showInputDialog(
                frame, "Enter quantity:");

        if (quantityInput == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityInput.trim());

            if (warehouse.addToWishlist(
                    clientID, productID, quantity)) {
                show("Wishlist updated: " + quantity
                        + " of " + productID + ".");
            } else {
                show("Quantity must be a positive whole number.");
            }
        } catch (NumberFormatException exception) {
            show("Please enter a valid whole number.");
        }
    }

    private void displayWishlist() {
        Iterator<WishlistItem> items =
                warehouse.getClientWishlist(clientID);

        if (items == null || !items.hasNext()) {
            show("Your wishlist is empty.");
            return;
        }

        StringBuilder result = new StringBuilder();

        while (items.hasNext()) {
            result.append(items.next()).append("\n");
        }

        show(result.toString());
    }

    private void placeOrder() {
        int invoicesBefore = countInvoices();

        if (!warehouse.processOrder(clientID)) {
            show("Could not place an order. Your wishlist is empty.");
            return;
        }

        StringBuilder result =
                new StringBuilder("Order processed.\n");

        if (countInvoices() > invoicesBefore) {
            Iterator<Invoice> invoices =
                    warehouse.getClientInvoices(clientID);

            Invoice latest = null;

            while (invoices != null && invoices.hasNext()) {
                latest = invoices.next();
            }

            if (latest != null) {
                result.append("\n").append(latest).append("\n");
            }
        }

        result.append(
                "\nAny quantities that were unavailable were "
                        + "added to the product waitlists.");

        show(result.toString());
    }

    private int countInvoices() {
        Iterator<Invoice> invoices =
                warehouse.getClientInvoices(clientID);

        int count = 0;

        while (invoices != null && invoices.hasNext()) {
            invoices.next();
            count++;
        }

        return count;
    }

    private void returnToClientMenu() {
        context.returnToClientMenu();
    }

    private void logout() {
        context.logout();
    }

    @Override
    public void run() {
        clientID = context.getActiveClientID();
        frame = context.getFrame();

        JPanel mainPanel = new JPanel(
                new BorderLayout(10, 15));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 30, 20, 30));

        JLabel title = new JLabel(
                "Wishlist Operations - " + clientID,
                SwingConstants.CENTER);

        title.setFont(
                new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(
                new GridLayout(5, 1, 8, 8));

        JButton addButton = new JButton(
                new AbstractAction("Add to Wishlist") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        addToWishlist();
                    }
                });

        JButton displayButton = new JButton(
                new AbstractAction("Display Wishlist") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        displayWishlist();
                    }
                });

        JButton orderButton = new JButton(
                new AbstractAction("Place Order") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        placeOrder();
                    }
                });

        JButton backButton = new JButton(
                new AbstractAction("Return to Client Menu") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        returnToClientMenu();
                    }
                });

        JButton logoutButton = new JButton(
                new AbstractAction("Logout") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        logout();
                    }
                });

        buttonPanel.add(addButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(orderButton);
        buttonPanel.add(backButton);
        buttonPanel.add(logoutButton);

        outputArea = new JTextArea(12, 28);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane =
                new JScrollPane(outputArea);

        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Output"));

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.toFront();
    }
}