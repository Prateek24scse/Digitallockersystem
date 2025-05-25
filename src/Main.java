import ui.LoginFrame;

public class Main {
    public static void main(String[] args) {
        // Launch the login frame on the Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}
