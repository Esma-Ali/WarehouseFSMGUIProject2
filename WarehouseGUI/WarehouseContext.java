import javax.swing.*;

public class WarehouseContext {
    public static final int LOGIN_STATE = 0;
    public static final int CLIENT_STATE = 1;
    public static final int CLERK_STATE = 2;
    public static final int MANAGER_STATE = 3;

    private static final int NO_ROLE = -1;
    private static final int INVALID_TRANSITION = -1;

    private static final int LOGIN_CLIENT = 0;
    private static final int LOGIN_CLERK = 1;
    private static final int LOGIN_MANAGER = 2;
    private static final int BECOME_CLIENT = 3;
    private static final int BECOME_CLERK = 4;
    private static final int LOGOUT_TO_LOGIN = 5;
    private static final int LOGOUT_TO_CLERK = 6;
    private static final int LOGOUT_TO_MANAGER = 7;

    private static WarehouseContext context;

    private final Warehouse warehouse;
    private final JFrame frame;
    private WarehouseState[] states;

    private int currentState = LOGIN_STATE;
    private int originalLoginRole = NO_ROLE;
    private String activeClientID;
    private boolean running = true;

    // Rows are events. Columns are Login, Client, Clerk, and Manager.
    private final int[][] transitionMatrix = {
            {CLIENT_STATE, INVALID_TRANSITION,
                    INVALID_TRANSITION, INVALID_TRANSITION},
            {CLERK_STATE, INVALID_TRANSITION,
                    INVALID_TRANSITION, INVALID_TRANSITION},
            {MANAGER_STATE, INVALID_TRANSITION,
                    INVALID_TRANSITION, INVALID_TRANSITION},
            {INVALID_TRANSITION, INVALID_TRANSITION,
                    CLIENT_STATE, INVALID_TRANSITION},
            {INVALID_TRANSITION, INVALID_TRANSITION,
                    INVALID_TRANSITION, CLERK_STATE},
            {INVALID_TRANSITION, LOGIN_STATE,
                    LOGIN_STATE, LOGIN_STATE},
            {INVALID_TRANSITION, CLERK_STATE,
                    INVALID_TRANSITION, INVALID_TRANSITION},
            {INVALID_TRANSITION, INVALID_TRANSITION,
                    MANAGER_STATE, INVALID_TRANSITION}
    };

    private WarehouseContext() {
        warehouse = Warehouse.instance();

        frame = new JFrame("Warehouse FSM GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 450);
        frame.setLocationRelativeTo(null);
    }

    public static WarehouseContext instance() {
        if (context == null) {
            context = new WarehouseContext();
        }
        return context;
    }

    private void initializeStates() {
        if (states != null) {
            return;
        }

        states = new WarehouseState[] {
                LoginState.instance(),
                ClientMenuState.instance(),
                ClerkMenuState.instance(),
                ManagerMenuState.instance()
        };
    }

    public void process() {
        initializeStates();
        states[currentState].run();
    }

    public JFrame getFrame() {
        return frame;
    }

    public boolean login(int role, String clientID) {
        if (currentState != LOGIN_STATE) {
            return false;
        }

        int event;

        if (role == CLIENT_STATE) {
            if (clientID == null
                    || warehouse.getClient(clientID.trim()) == null) {
                return false;
            }

            activeClientID = clientID.trim();
            event = LOGIN_CLIENT;
        } else if (role == CLERK_STATE) {
            activeClientID = null;
            event = LOGIN_CLERK;
        } else if (role == MANAGER_STATE) {
            activeClientID = null;
            event = LOGIN_MANAGER;
        } else {
            return false;
        }

        originalLoginRole = role;
        changeState(event);
        return true;
    }

    public boolean becomeClient(String clientID) {
        if (currentState != CLERK_STATE
                || clientID == null
                || warehouse.getClient(clientID.trim()) == null) {
            return false;
        }

        activeClientID = clientID.trim();
        changeState(BECOME_CLIENT);
        return true;
    }

    public boolean becomeClerk() {
        if (currentState != MANAGER_STATE) {
            return false;
        }

        changeState(BECOME_CLERK);
        return true;
    }

    public void logout() {
        int event;

        if (currentState == CLIENT_STATE) {
            activeClientID = null;

            if (originalLoginRole == CLIENT_STATE) {
                event = LOGOUT_TO_LOGIN;
            } else {
                event = LOGOUT_TO_CLERK;
            }
        } else if (currentState == CLERK_STATE) {
            if (originalLoginRole == MANAGER_STATE) {
                event = LOGOUT_TO_MANAGER;
            } else {
                event = LOGOUT_TO_LOGIN;
            }
        } else if (currentState == MANAGER_STATE) {
            event = LOGOUT_TO_LOGIN;
        } else {
            return;
        }

        changeState(event);

        if (currentState == LOGIN_STATE) {
            clearSession();
        }
    }

    public String getActiveClientID() {
        return activeClientID;
    }

    public int getOriginalLoginRole() {
        return originalLoginRole;
    }

    public int getCurrentState() {
        return currentState;
    }

    private void changeState(int event) {
        if (event < 0 || event >= transitionMatrix.length) {
            throw new IllegalArgumentException(
                    "Invalid transition event.");
        }

        int nextState = transitionMatrix[event][currentState];

        if (nextState == INVALID_TRANSITION) {
            throw new IllegalStateException(
                    "Transition is not allowed from the current state.");
        }

        currentState = nextState;
        states[currentState].run();
    }

    private void clearSession() {
        originalLoginRole = NO_ROLE;
        activeClientID = null;
    }

    public void stop() {
        running = false;
        clearSession();
        currentState = LOGIN_STATE;
        frame.dispose();
    }

    public boolean isRunning() {
        return running;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> WarehouseContext.instance().process());
    }
}
