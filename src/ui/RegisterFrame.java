package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import util.DBConnection;
import java.security.MessageDigest;


public class RegisterFrame extends JFrame {
    public RegisterFrame() {
        setTitle("Digital Locker - Register");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setUndecorated(true);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Close button
        JButton closeBtn = new JButton("X");
        closeBtn.setBounds(380, 10, 30, 25);
        closeBtn.setFocusPainted(false);
        closeBtn.setBackground(Color.RED);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createEmptyBorder());
        closeBtn.addActionListener(e -> System.exit(0));
        panel.add(closeBtn);

        JLabel titleLabel = new JLabel("REGISTER", SwingConstants.CENTER);
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

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(150, 170, 90, 30);
        registerBtn.setBackground(new Color(70, 130, 180));
        registerBtn.setForeground(Color.WHITE);
        panel.add(registerBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(260, 170, 90, 30);
        backBtn.setBackground(new Color(128, 128, 128));
        backBtn.setForeground(Color.WHITE);
        panel.add(backBtn);

        registerBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            String hashPass = hashPassword(pass);

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password.");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Failed to connect to database.");
                    return;
                }

                PreparedStatement ps = conn.prepareStatement("INSERT INTO users(username, password) VALUES(?, ?)");
                ps.setString(1, user);
                ps.setString(2, hashPass);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registered successfully!");
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame()); // Safer thread call
            } catch (SQLException ex) {
                if (ex.getMessage().contains("Duplicate entry") || ex.getMessage().toLowerCase().contains("unique")) {
                    JOptionPane.showMessageDialog(this, "Username already exists.");
                } else {
                    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                }
            }
        });

        backBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame());
        });

        add(panel);
        setVisible(true);
    }

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
