import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;

public class ClientMenuState extends WarehouseState {
    private static ClientMenuState instance;

    private final Warehouse warehouse;
    private final WarehouseContext context;

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

    private void showDetails() {
        Client client = warehouse.getClient(clientID);

        if (client == null) {
            show("Client not found.");
            return;
        }

        show(client.toString());
    }

    private void showProducts() {
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

    private void showTransactions() {
        Iterator<Invoice> invoices =
                warehouse.getClientInvoices(clientID);

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

    private void openWishlistMenu() {
        context.openWishlistMenu();
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
                "Client Menu - " + clientID,
                SwingConstants.CENTER);

        title.setFont(
                new Font("SansSerif", Font.BOLD, 24));

        JPanel buttonPanel = new JPanel(
                new GridLayout(5, 1, 8, 8));

        JButton detailsButton = new JButton(
                new AbstractAction("Show My Details") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        showDetails();
                    }
                });

        JButton productsButton = new JButton(
                new AbstractAction("Show Products") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        showProducts();
                    }
                });

        JButton transactionsButton = new JButton(
                new AbstractAction("Show My Transactions") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        showTransactions();
                    }
                });

        JButton wishlistButton = new JButton(
                new AbstractAction("Wishlist Operations") {
                    @Override
                    public void actionPerformed(
                            ActionEvent event) {
                        openWishlistMenu();
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

        buttonPanel.add(detailsButton);
        buttonPanel.add(productsButton);
        buttonPanel.add(transactionsButton);
        buttonPanel.add(wishlistButton);
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