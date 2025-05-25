package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import util.DBConnection;
// Add this import at the top of the file
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Key;
import java.util.Base64;

public class DashboardFrame extends JFrame {
    private String username;
    private JPanel mainPanel;
    private JPanel filesPanel;
    private final String STORAGE_PATH;
    private static final Color BACKGROUND_COLOR = new Color(36, 41, 46);
    private static final Color BUTTON_COLOR = new Color(88, 166, 255);
    private static final Color TEXT_COLOR = Color.WHITE;
    // Removed duplicate ENCRYPTION_KEY field since it's already defined below
    // Removed duplicate ALGORITHM declaration since it's defined later in the file

    public DashboardFrame(String username) {
        this.username = username;
        this.STORAGE_PATH = System.getProperty("user.dir") + "/storage/" + username + "/";
        
        setTitle("Digital Locker - " + username);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create storage directory
        new File(STORAGE_PATH).mkdirs();

        // Initialize components
        initializeComponents();
        setVisible(true);
    }

    private void initializeComponents() {
        // Top Panel
        add(createTopPanel(), BorderLayout.NORTH);

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.add(createButtonsPanel(), BorderLayout.CENTER);

        // Files Panel
        filesPanel = createFilesPanel();
        filesPanel.setVisible(false);
        mainPanel.add(filesPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void saveFileToDatabase(String filename, String filepath, boolean isEncrypted) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO files (user_id, filename, file_path, is_encrypted) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, filename);
            pstmt.setString(3, filepath);
            pstmt.setBoolean(4, isEncrypted);
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // 128-bit key size
        return keyGen.generateKey();
    }

    // Removed duplicate ENCRYPTION_KEY declaration since it's already defined as a Key type below


    // Remove the dynamic key generation approach and use only the fixed key
    // Remove all key generation related code and static block
    // Define the encryption key bytes
    private static final byte[] ENCRYPTION_KEY_BYTES = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    };
    
    // Create the encryption key directly
    private static final Key ENCRYPTION_KEY = new SecretKeySpec(ENCRYPTION_KEY_BYTES, "AES");
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    private void encryptFile() {
        try {
            JFileChooser fileChooser = new JFileChooser(new File(STORAGE_PATH));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File inputFile = fileChooser.getSelectedFile();
                File encryptedFile = new File(inputFile.getPath() + ".encrypted");

                // Read the file
                byte[] fileContent = new byte[(int) inputFile.length()];
                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    fis.read(fileContent);
                }

                // Create cipher
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY);

                // Encrypt the file content
                byte[] encryptedContent = cipher.doFinal(fileContent);

                // Write encrypted content
                try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                    fos.write(encryptedContent);
                }

                // Save to database
                saveFileToDatabase(encryptedFile.getName(), encryptedFile.getPath(), true);
                
                // Delete original file
                inputFile.delete();
                
                JOptionPane.showMessageDialog(this, "File encrypted successfully!");
                loadFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error encrypting file: " + e.getMessage());
        }
    }

    private void decryptFile() {
        try {
            JFileChooser fileChooser = new JFileChooser(new File(STORAGE_PATH));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".encrypted");
                }
                public String getDescription() {
                    return "Encrypted Files";
                }
            });

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File encryptedFile = fileChooser.getSelectedFile();
                File decryptedFile = new File(encryptedFile.getPath().replace(".encrypted", ""));

                // Read encrypted content
                byte[] encryptedContent = new byte[(int) encryptedFile.length()];
                try (FileInputStream fis = new FileInputStream(encryptedFile)) {
                    fis.read(encryptedContent);
                }

                // Create cipher
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, ENCRYPTION_KEY);

                // Decrypt the content
                byte[] decryptedContent = cipher.doFinal(encryptedContent);

                // Write decrypted content
                try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                    fos.write(decryptedContent);
                }

                // Update database
                saveFileToDatabase(decryptedFile.getName(), decryptedFile.getPath(), false);
                
                // Delete encrypted file
                encryptedFile.delete();

                JOptionPane.showMessageDialog(this, "File decrypted successfully!");
                loadFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error decrypting file: " + e.getMessage());
        }
    }

    private void deleteFile() {
        try {
            JFileChooser fileChooser = new JFileChooser(new File(STORAGE_PATH));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                // Delete from database
                Connection conn = DBConnection.getConnection();
                String sql = "DELETE FROM files WHERE filename = ? AND user_id = (SELECT id FROM users WHERE username = ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selectedFile.getName());
                pstmt.setString(2, username);
                pstmt.executeUpdate();

                // Delete file from storage
                if (selectedFile.delete()) {
                    JOptionPane.showMessageDialog(this, "File deleted successfully!");
                    loadFiles();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete the file!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting file: " + e.getMessage());
        }
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel("Welcome to Digital Locker, " + username);
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setForeground(TEXT_COLOR);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcome, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create buttons with consistent styling
        JButton[] buttons = {
            createStyledButton("View Files", "📁"),
            createStyledButton("Upload File", "⬆️"),
            createStyledButton("Encrypt File", "🔒"),
            createStyledButton("Decrypt File", "🔓"),
            createStyledButton("Delete File", "🗑️"),
            createStyledButton("Logout", "🚪")
        };

        // Add buttons to panel in a grid
        int row = 0;
        int col = 0;
        for (JButton button : buttons) {
            gbc.gridx = col;
            gbc.gridy = row;
            panel.add(button, gbc);
            col++;
            if (col == 2) { // 2 buttons per row
                col = 0;
                row++;
            }
        }

        return panel;
    }

    private JButton createStyledButton(String text, String icon) {
        JButton button = new JButton(icon + " " + text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BUTTON_COLOR.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        // Add action listeners
        switch (text) {
            case "View Files":
                button.addActionListener(e -> toggleFilesPanel());
                break;
            case "Upload File":
                button.addActionListener(e -> uploadFile());
                break;
            case "Encrypt File":
                button.addActionListener(e -> encryptFile());
                break;
            case "Decrypt File":
                button.addActionListener(e -> decryptFile());
                break;
            case "Delete File":
                button.addActionListener(e -> deleteFile());
                break;
            case "Logout":
                button.addActionListener(e -> logout());
                break;
        }

        return button;
    }

    private JPanel createFilesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(45, 50, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private void toggleFilesPanel() {
        filesPanel.setVisible(!filesPanel.isVisible());
        if (filesPanel.isVisible()) {
            loadFiles();
        }
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                File destination = new File(STORAGE_PATH + selectedFile.getName());
                copyFile(selectedFile, destination);
                saveFileToDatabase(selectedFile.getName(), destination.getPath(), false); // Added false for is_encrypted parameter
                JOptionPane.showMessageDialog(this, "File uploaded successfully!");
                loadFiles();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error uploading file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }

    private void loadFiles() {
        filesPanel.removeAll();
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT filename, file_path FROM files WHERE user_id = (SELECT id FROM users WHERE username = ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String filename = rs.getString("filename");
                String filepath = rs.getString("file_path");
                addFilePanel(filename, filepath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        filesPanel.revalidate();
        filesPanel.repaint();
    }

    private void addFilePanel(String filename, String filepath) {
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel fileLabel = new JLabel(filename);
        filePanel.add(fileLabel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton downloadBtn = new JButton("Download");
        downloadBtn.addActionListener(e -> downloadFile(filepath, filename));
        
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteFile(filename));

        buttonsPanel.add(downloadBtn);
        buttonsPanel.add(deleteBtn);
        filePanel.add(buttonsPanel, BorderLayout.EAST);

        filesPanel.add(filePanel);
    }

    private void downloadFile(String filepath, String filename) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File source = new File(filepath);
                File destination = fileChooser.getSelectedFile();
                copyFile(source, destination);
                JOptionPane.showMessageDialog(this, "File downloaded successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error downloading file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteFile(String filename) {
        try {
            // Delete from database
            Connection conn = DBConnection.getConnection();
            String sql = "DELETE FROM files WHERE filename = ? AND user_id = (SELECT id FROM users WHERE username = ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, filename);
            pstmt.setString(2, username);
            pstmt.executeUpdate();

            // Delete from storage
            new File(STORAGE_PATH + filename).delete();

            // Refresh files list
            loadFiles();
            JOptionPane.showMessageDialog(this, "File deleted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}

