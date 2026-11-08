import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class ClientMenuState extends WarehouseState {
    private static ClientMenuState instance;
    private static Warehouse warehouse;
    private WarehouseContext context;
    private JFrame frame;
    private JTextArea outputArea;
    private String clientID;

    private ClientMenuState() {
        super();
        warehouse = Warehouse.instance();
        context = WarehouseContext.instance();
    }

    public static ClientMenuState instance() {
        if (instance == null) {
            instance = new ClientMenuState();
        }
        return instance;
    }

    private void show(String text) {
        outputArea.setText(text);
        outputArea.setCaretPosition(0);
    }

    public void showDetails() {
        Client client = warehouse.getClient(clientID);

        if (client == null) {
            show("Client not found.");
            return;
        }

        show(client.toString());
    }

    // Product.toString() includes the price.
    public void showProducts() {
        Iterator<Product> products = warehouse.getProducts();

        if (!products.hasNext()) {
            show("No products in the warehouse.");
            return;
        }

        StringBuilder result = new StringBuilder();

        while (products.hasNext()) {
            result.append(products.next()).append("\n");
        }

        show(result.toString());
    }

    public void showTransactions() {
        Iterator<Invoice> invoices = warehouse.getClientInvoices(clientID);

        if (invoices == null || !invoices.hasNext()) {
            show("No transactions for this client.");
            return;
        }

        StringBuilder result = new StringBuilder();

        while (invoices.hasNext()) {
            result.append(invoices.next()).append("\n");
        }

        show(result.toString());
    }

    public void addToWishlist() {
        String productID = JOptionPane.showInputDialog(
                frame, "Enter product ID (e.g. P1):");

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

            // Adding a product that is already on the wishlist
            // replaces its quantity.
            if (warehouse.addToWishlist(clientID, productID, quantity)) {
                show("Wishlist updated: " + quantity + " of " + productID
                        + ".");
            } else {
                show("Quantity must be a positive number.");
            }
        } catch (NumberFormatException exception) {
            show("Please enter a valid whole number for quantity.");
        }
    }

    public void displayWishlist() {
        Iterator<WishlistItem> items = warehouse.getClientWishlist(clientID);

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

    public void placeOrder() {
        int invoicesBefore = countInvoices();

        if (!warehouse.processOrder(clientID)) {
            show("Could not place an order. Your wishlist is empty.");
            return;
        }

        StringBuilder result = new StringBuilder("Order processed.\n");

        // Items that could not be supplied go on the product waitlists,
        // so an order may produce no new invoice.
        if (countInvoices() > invoicesBefore) {
            Iterator<Invoice> invoices =
                    warehouse.getClientInvoices(clientID);
            Invoice latest = null;

            while (invoices.hasNext()) {
                latest = invoices.next();
            }

            result.append("\n").append(latest).append("\n");
        }

        result.append("\nAny quantities that were out of stock have "
                + "been added to the product waitlists.");
        show(result.toString());
    }

    private int countInvoices() {
        Iterator<Invoice> invoices = warehouse.getClientInvoices(clientID);
        int count = 0;

        while (invoices != null && invoices.hasNext()) {
            invoices.next();
            count++;
        }

        return count;
    }

    // WarehouseContext picks the next state: Login if the user originally
    // logged in as a client, Clerk if a clerk or manager became this client.
    public void logout() {
        context.logout();
    }

    // Called by WarehouseContext. The client is whoever logged in, or
    // whoever a clerk chose with "become client".
    @Override
    public void run() {
        clientID = context.getActiveClientID();
        frame = context.getFrame();

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel(
                "Client Menu - " + clientID, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 8, 8));

        JButton detailsButton = new JButton(
                new AbstractAction("Show My Details") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showDetails();
                    }
                });

        JButton productsButton = new JButton(
                new AbstractAction("Show Products") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showProducts();
                    }
                });

        JButton transactionsButton = new JButton(
                new AbstractAction("Show My Transactions") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        showTransactions();
                    }
                });

        JButton addWishlistButton = new JButton(
                new AbstractAction("Add to Wishlist") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        addToWishlist();
                    }
                });

        JButton displayWishlistButton = new JButton(
                new AbstractAction("Display Wishlist") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        displayWishlist();
                    }
                });

        JButton placeOrderButton = new JButton(
                new AbstractAction("Place Order") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        placeOrder();
                    }
                });

        JButton logoutButton = new JButton(
                new AbstractAction("Logout") {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        logout();
                    }
                });

        buttonPanel.add(detailsButton);
        buttonPanel.add(productsButton);
        buttonPanel.add(transactionsButton);
        buttonPanel.add(addWishlistButton);
        buttonPanel.add(displayWishlistButton);
        buttonPanel.add(placeOrderButton);
        buttonPanel.add(logoutButton);

        outputArea = new JTextArea(12, 28);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

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