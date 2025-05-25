
# Digital Locker System

## Description
A secure digital locker system that allows users to store and manage their files with encryption capabilities. The system provides a user-friendly GUI interface and secure file storage with AES encryption.

## Features
- User Authentication
  - Secure registration and login system
  - Password hashing for security
  - Individual user storage spaces

- File Management
  - File upload and storage
  - AES encryption/decryption of files
  - Secure file deletion
  - Organized file listing

- Security
  - AES/ECB/PKCS5Padding encryption
  - 128-bit encryption key
  - Encrypted file tracking

- User Interface
  - Modern GUI with custom styling
  - Intuitive dashboard layout
  - File operation buttons
  - Status notifications

## Technical Requirements
- Java JDK 8 or higher
- MySQL 5.7 or higher
- MySQL Connector/J (JDBC driver)
- Swing GUI support

## Project Structure
- src/
  - ui/ : User interface components
  - util/ : Utility classes and database connection
- lib/ : External libraries
- storage/ : User file storage directory

## Database Setup
1. Install MySQL if not already installed
2. Create a new database named 'digitallocker':
   ```sql
   CREATE DATABASE digitallocker;
   ```

## How to Run:
1. Create MySQL database using `database.sql`.
2. Update DB username/password in `DBConnection.java` if needed.
3. Compile all Java files in `src/`.
4. Run `Main.java`.

## Requirements:
- Java JDK
- MySQL
- Swing GUI support
