package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Use correct driver
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/digitallocker", "root", "123456");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS digitallocker;
USE digitallocker;

-- Users table for storing user information
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Stores hashed password
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Files table for storing file information
CREATE TABLE IF NOT EXISTS files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- User sessions table for managing active sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Activity log table for tracking user actions
CREATE TABLE IF NOT EXISTS activity_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action_type ENUM('LOGIN', 'LOGOUT', 'UPLOAD', 'DOWNLOAD', 'ENCRYPT', 'DECRYPT', 'DELETE') NOT NULL,
    file_id INT,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_files_user_id ON files(user_id);
CREATE INDEX idx_files_upload_date ON files(upload_date);
CREATE INDEX idx_activity_log_user_id ON activity_log(user_id);
CREATE INDEX idx_activity_log_timestamp ON activity_log(action_timestamp);

-- Create a view for file statistics
CREATE VIEW file_statistics AS
SELECT 
    u.username,
    COUNT(f.id) as total_files,
    SUM(CASE WHEN f.is_encrypted = 1 THEN 1 ELSE 0 END) as encrypted_files,
    SUM(f.file_size) as total_storage_used
FROM users u
LEFT JOIN files f ON u.id = f.user_id
GROUP BY u.id, u.username;

-- Add sample admin user (password: admin123)
INSERT INTO users (username, password, email) VALUES 
('admin', 'e5a7c3deb5669b7c94e679f2b5aab468', 'admin@digitallocker.com');
