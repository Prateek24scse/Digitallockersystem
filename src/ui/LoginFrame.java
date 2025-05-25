package ui;
import ui.RegisterFrame;
import ui.RegisterFrame;
import ui.DashboardFrame;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import util.DBConnection;
import java.security.MessageDigest;

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Digital Locker - Login");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(25, 25, 25));
        panel.setLayout(null);

        // Close button
        JButton closeBtn = new JButton("X");
        closeBtn.setBounds(380, 10, 30, 25);
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(Color.RED);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.addActionListener(e -> System.exit(0));
        panel.add(closeBtn);

        JLabel titleLabel = new JLabel("LOGIN", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBounds(0, 20, 420, 30);
        panel.add(titleLabel);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setBounds(60, 80, 100, 25);
        panel.add(userLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(150, 80, 200, 25);
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(60, 120, 100, 25);
        panel.add(passLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(150, 120, 200, 25);
        panel.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(150, 170, 90, 30);
        loginBtn.setBackground(new Color(60, 179, 113));
        loginBtn.setForeground(Color.WHITE);
        panel.add(loginBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(260, 170, 90, 30);
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        panel.add(registerBtn);

        // Action for Login
        loginBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            String hashPass = hashPassword(pass);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1, user);
                ps.setString(2, hashPass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new DashboardFrame(user);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error.");
            }
        });

        // Action for Register
        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });

        add(panel);
        setVisible(true);
    }

    // SHA-256 Password Hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
