package com.example.chatapp;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * ChatApp1 - Integrated final (Part1 + Part2 + Part3).
 *
 * Notes:
 * - Uses simple file-based JSON helpers (compatible with your earlier approach).
 * - Numeric QuickChat menu (1..9). Robust to Cancel and invalid input.
 * - Message model includes sender (Option A).
 */
public class ChatApp1 {

    // ---------- User model & login (unchanged behaviour) ----------
    static class User {
        private String fullname, gender, username, password, phone, profileImagePath;

        public User(String fullname, String gender, String username,
                    String password, String phone, String profileImagePath) {
            this.fullname = fullname;
            this.gender = gender;
            this.username = username;
            this.password = password;
            this.phone = phone;
            this.profileImagePath = profileImagePath;
        }

        public String getFullname() { return fullname; }
        public String getGender() { return gender; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getPhone() { return phone; }
        public String getProfileImagePath() { return profileImagePath; }
    }

    static class Login {
        protected static HashMap<String, User> users = new HashMap<>();

        public boolean checkUsername(String username) {
            return username != null && username.contains("_") && username.length() <= 5;
        }

        public boolean checkPasswordComplexity(String password) {
            return password != null && password.length() > 8;
        }

        public boolean checkCellphone(String phone) {
            return phone != null && phone.startsWith("+") && phone.length() >= 10 && phone.length() <= 13;
        }

        public String registerUser(String fullname, String gender, String username,
                                   String password, String confirmPassword,
                                   String phone, String imagePath) {

            StringBuilder missingFields = new StringBuilder();
            if (fullname == null || fullname.isEmpty()) missingFields.append("Full Name, ");
            if (username == null || username.isEmpty()) missingFields.append("Username, ");
            if (phone == null || phone.isEmpty()) missingFields.append("Phone Number, ");
            if (password == null || password.isEmpty()) missingFields.append("Password, ");
            if (confirmPassword == null || confirmPassword.isEmpty()) missingFields.append("Confirm Password, ");
            if (gender == null || gender.isEmpty()) missingFields.append("Gender, ");

            if (missingFields.length() > 0) {
                String fields = missingFields.substring(0, missingFields.length() - 2);
                if (fields.contains(",")) return "Please fill in the following fields: " + fields + ".";
                else return "Please fill in the " + fields + ".";
            }

            if (!checkUsername(username)) return "Username must contain an underscore and be no longer than 5 characters.";
            if (!checkPasswordComplexity(password)) return "Password must be at least 8 characters long and include letters.";
            if (!checkCellphone(phone)) return "Phone number is incorrectly formatted. Example: +27831234567.";
            if (!password.equals(confirmPassword)) return "Passwords do not match. Please re-enter your password.";
            if (users.containsKey(username)) return "This username is already taken. Please choose a different one.";

            User user = new User(fullname, gender, username, password, phone, imagePath);
            users.put(username, user);
            return "Registration successful!";
        }

        public boolean loginUser(String username, String password) {
            User user = users.get(username);
            return user != null && user.getPassword().equals(password);
        }

        public String returnLoginStatus(boolean loginSuccess, String username) {
            if (loginSuccess) return "Welcome " + username + ", it is great to see you again.";
            return "Username or password incorrect, please try again.";
        }
    }

    // ---------- Message model (Option A) ----------
    public static class Message {
        private String messageID;
        private String sender;
        private String recipient;
        private String messageText;
        private int messageNumber;
        private String messageHash;

        public Message(String messageID, String sender, String recipient, String messageText, int messageNumber) {
            this.messageID = messageID;
            this.sender = sender;
            this.recipient = recipient;
            this.messageText = messageText == null ? "" : messageText;
            this.messageNumber = messageNumber;
            this.messageHash = createMessageHash();
        }

        public boolean checkMessageID() {
            return messageID != null && messageID.length() <= 10;
        }

        // returns 1 on valid, 0 on invalid (as POE specified)
        public int checkRecipientCell() {
            if (recipient == null) return 0;
            if (!recipient.startsWith("+")) return 0;
            int len = recipient.length();
            if (len < 10 || len > 13) return 0;
            String rest = recipient.substring(1);
            return rest.matches("\\d+") ? 1 : 0;
        }

        private String createMessageHash() {
            String idPart = messageID != null && messageID.length() >= 2 ? messageID.substring(0, 2) : "00";
            String text = messageText.trim();
            String[] words = text.split("\\s+");
            String first = words.length >= 1 ? words[0].replaceAll("[^a-zA-Z0-9]", "") : "";
            String last = words.length >= 2 ? words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "") : first;
            String combined = (first + last).toUpperCase();
            return (idPart + ":" + messageNumber + ":" + combined).toUpperCase();
        }

        // Actions: 1=Send, 2=Store, 3=Disregard
        public String sentMessage(int choice) {
            switch (choice) {
                case 1:
                    // add to parallel arrays and persist
                    ChatApp1.contents.add(this);
                    ChatApp1.messageHashes.add(this.messageHash);
                    ChatApp1.messageIDs.add(this.messageID);
                    ChatApp1.persistAllSentMessages();
                    return "Message successfully sent.";
                case 2:
                    // store draft
                    Map<String, String> draft = new LinkedHashMap<>();
                    draft.put("MessageID", messageID);
                    draft.put("Sender", sender);
                    draft.put("MessageHash", messageHash);
                    draft.put("Recipient", recipient);
                    draft.put("Message", messageText);
                    List<Map<String,String>> drafts = ChatApp1.readJsonListFromFile(ChatApp1.STORED_FILE);
                    drafts.add(draft);
                    ChatApp1.writeJsonListToFile(ChatApp1.STORED_FILE, drafts);
                    ChatApp1.loadStoredMessagesIntoArray();
                    return "Message successfully stored.";
                case 3:
                    ChatApp1.disregardedMessages.add(this);
                    return "Message disregarded.";
                default:
                    return "Invalid action.";
            }
        }

        // getters
        public String getMessageID() { return messageID; }
        public String getSender() { return sender; }
        public String getRecipient() { return recipient; }
        public String getMessageText() { return messageText; }
        public String getMessageHash() { return messageHash; }
        public int getMessageNumber() { return messageNumber; }
    }

    // ---------- Part 3 data structures ----------
    public static final List<Message> contents = new ArrayList<>();            // sent messages
    public static final List<Message> disregardedMessages = new ArrayList<>(); // disregarded
    public static final List<Map<String,String>> storedMessages = new ArrayList<>(); // loaded from stored_messages.json
    public static final List<String> messageHashes = new ArrayList<>();
    public static final List<String> messageIDs = new ArrayList<>();

    public static final String MESSAGES_FILE = "messages.json";
    public static final String STORED_FILE = "stored_messages.json";

    // ---------- Part 3 operations ----------

    public static void persistAllSentMessages() {
        List<Map<String,String>> list = new ArrayList<>();
        for (Message m : contents) {
            Map<String,String> obj = new LinkedHashMap<>();
            obj.put("MessageID", m.getMessageID());
            obj.put("Sender", m.getSender());
            obj.put("MessageHash", m.getMessageHash());
            obj.put("Recipient", m.getRecipient());
            obj.put("Message", m.getMessageText());
            list.add(obj);
        }
        writeJsonListToFile(MESSAGES_FILE, list);
    }

    public static void loadStoredMessagesIntoArray() {
        storedMessages.clear();
        List<Map<String,String>> read = readJsonListFromFile(STORED_FILE);
        storedMessages.addAll(read);
    }


    public static String displayLongestSentMessage() {
        if (contents.isEmpty()) return "No sent messages.";
        Message best = contents.get(0);
        for (Message m : contents) {
            if (m.getMessageText().length() > best.getMessageText().length()) best = m;
        }
        return "Longest message (ID: " + best.getMessageID() + "):\n" + best.getMessageText();
    }

    public static String searchByMessageID(String id) {
        for (Message m : contents) {
            if (m.getMessageID().equals(id)) {
                return "Recipient: " + m.getRecipient() + "\nMessage: " + m.getMessageText();
            }
        }
        return "Message ID not found in sent messages.";
    }

    public static String searchByRecipient(String recipient) {
        StringBuilder sb = new StringBuilder();
        for (Message m : contents) {
            if (recipient != null && recipient.equals(m.getRecipient())) {
                sb.append("MessageID: ").append(m.getMessageID()).append("\n")
                  .append("Sender: ").append(m.getSender()).append("\n")
                  .append("Message: ").append(m.getMessageText()).append("\n\n");
            }
        }
        if (sb.length() == 0) return "No sent messages to recipient: " + recipient;
        return sb.toString();
    }

    public static boolean deleteByMessageHash(String hash) {
        Iterator<Message> it = contents.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            Message m = it.next();
            if (m.getMessageHash().equals(hash)) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) {
            messageHashes.clear();
            messageIDs.clear();
            for (Message m : contents) {
                messageHashes.add(m.getMessageHash());
                messageIDs.add(m.getMessageID());
            }
            persistAllSentMessages();
        }
        return removed;
    }

    public static String displayReportAllSentMessages() {
        if (contents.isEmpty()) return "No sent messages to report.";
        StringBuilder sb = new StringBuilder();
        for (Message m : contents) {
            sb.append("MessageHash: ").append(m.getMessageHash())
              .append(" | MessageID: ").append(m.getMessageID())
              .append(" | Sender: ").append(safe(m.getSender()))
              .append(" | Recipient: ").append(safe(m.getRecipient()))
              .append(" | Message: ").append(m.getMessageText())
              .append("\n");
        }
        return sb.toString();
    }

    private static String safe(String s) { return s == null ? "" : s; }

    // ---------- Lightweight JSON helpers ----------
    public static List<Map<String,String>> readJsonListFromFile(String filename) {
        try {
            Path p = Paths.get(filename);
            if (!Files.exists(p)) return new ArrayList<>();
            String content = Files.readString(p);
            if (content.trim().isEmpty()) return new ArrayList<>();
            List<Map<String,String>> out = new ArrayList<>();
            String trimmed = content.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return out;
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            if (inner.isEmpty()) return out;
            String[] items = inner.split("\\},\\s*\\{");
            for (String item : items) {
                String it = item;
                if (!it.startsWith("{")) it = "{" + it;
                if (!it.endsWith("}")) it = it + "}";
                Map<String,String> map = new LinkedHashMap<>();
                String body = it.substring(1, it.length() - 1).trim();
                String[] pairs = body.split(",\\s*\"");
                for (String pair : pairs) {
                    String clean = pair.trim();
                    if (!clean.startsWith("\"")) clean = "\"" + clean;
                    int colon = clean.indexOf(':');
                    if (colon < 0) continue;
                    String k = clean.substring(0, colon).replaceAll("[\"\\{\\}]", "").trim();
                    String v = clean.substring(colon + 1).trim();
                    v = v.replaceAll("^[\\s\"]+|[\\s\"]+$", "");
                    v = v.replaceAll("[\\},]$", "").trim();
                    v = v.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\\\", "\\");
                    map.put(k, v);
                }
                out.add(map);
            }
            return out;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static void writeJsonListToFile(String filename, List<Map<String,String>> list) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            writer.write("[\n");
            for (int i = 0; i < list.size(); i++) {
                Map<String,String> map = list.get(i);
                writer.write("  {\n");
                int j = 0;
                for (Map.Entry<String,String> e : map.entrySet()) {
                    writer.write("    \"" + escapeJson(e.getKey()) + "\": \"" + escapeJson(e.getValue()) + "\"");
                    j++;
                    if (j < map.size()) writer.write(",");
                    writer.write("\n");
                }
                writer.write("  }");
                if (i < list.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("]\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // ---------- GUI: Registration and Login (unchanged, simplified) ----------
    static class RegisterForm {
        private JFrame frame;
        private JTextField fullnameField, usernameField, phoneField;
        private JPasswordField passwordField, confirmpasswordField;
        private JRadioButton maleRadioButton, femaleRadioButton;
        private ButtonGroup genderGroup;
        private JLabel profilePictureImage;
        private JButton browseButton, returnToLoginButton;
        private String selectedImagePath;

        public RegisterForm() {
            frame = new JFrame("Register Form");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 750);
            frame.setLocationRelativeTo(null);

            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBackground(Color.BLACK);
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Full Name
            gbc.gridx = 0; gbc.gridy = 0;
            contentPanel.add(new JLabel("Full Name") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            fullnameField = new JTextField(15);
            contentPanel.add(fullnameField, gbc);

            // Username
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Username") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            contentPanel.add(usernameField, gbc);

            // Phone
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Phone") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            phoneField = new JTextField(15);
            contentPanel.add(phoneField, gbc);

            // Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            contentPanel.add(passwordField, gbc);

            // Show password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showPasswordCheck = new JCheckBox("Show");
            showPasswordCheck.setForeground(Color.BLUE);
            showPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showPasswordCheck, gbc);
            showPasswordCheck.addActionListener(e ->
                    passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Confirm Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Confirm Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            confirmpasswordField = new JPasswordField(15);
            contentPanel.add(confirmpasswordField, gbc);

            // Show confirm password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showConfirmPasswordCheck = new JCheckBox("Show");
            showConfirmPasswordCheck.setForeground(Color.BLUE);
            showConfirmPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showConfirmPasswordCheck, gbc);
            showConfirmPasswordCheck.addActionListener(e ->
                    confirmpasswordField.setEchoChar(showConfirmPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Gender
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Gender") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            JPanel genderPanel = new JPanel();
            genderPanel.setBackground(Color.BLACK);
            maleRadioButton = new JRadioButton("Male"); maleRadioButton.setForeground(Color.BLUE); maleRadioButton.setBackground(Color.BLACK);
            femaleRadioButton = new JRadioButton("Female"); femaleRadioButton.setForeground(Color.BLUE); femaleRadioButton.setBackground(Color.BLACK);
            genderGroup = new ButtonGroup();
            genderGroup.add(maleRadioButton); genderGroup.add(femaleRadioButton);
            genderPanel.add(maleRadioButton); genderPanel.add(femaleRadioButton);
            contentPanel.add(genderPanel, gbc);

            // Profile Picture
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Profile Picture") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            profilePictureImage = new JLabel();
            profilePictureImage.setPreferredSize(new Dimension(120, 120));
            Border border = BorderFactory.createLineBorder(Color.BLUE, 2);
            profilePictureImage.setBorder(border);
            contentPanel.add(profilePictureImage, gbc);

            gbc.gridy++;
            browseButton = new JButton("Browse");
            contentPanel.add(browseButton, gbc);
            browseButton.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    selectedImagePath = file.getAbsolutePath();
                    ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage()
                            .getScaledInstance(profilePictureImage.getWidth(), profilePictureImage.getHeight(), Image.SCALE_SMOOTH));
                    profilePictureImage.setIcon(icon);
                }
            });

            // Register button
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            JButton buttonRegister = new JButton("Register");
            contentPanel.add(buttonRegister, gbc);

            // Return to Login button
            returnToLoginButton = new JButton("Return");
            returnToLoginButton.addActionListener(e -> {
                frame.setVisible(false);
                new LoginForm();
            });
            contentPanel.add(returnToLoginButton, gbc);

            Login loginLogic = new Login();
            buttonRegister.addActionListener(e -> {
                String fullname = fullnameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmpasswordField.getPassword());
                String phone = phoneField.getText().trim();
                String gender = maleRadioButton.isSelected() ? "Male" : femaleRadioButton.isSelected() ? "Female" : "";

                String registrationMessage = loginLogic.registerUser(
                        fullname, gender, username, password, confirmPassword, phone, selectedImagePath
                );

                JOptionPane.showMessageDialog(frame, registrationMessage);
                if (registrationMessage.equals("Registration successful!")) {
                    frame.setVisible(false); // Close the registration form
                    new LoginForm(); // Open login page
                }
            });

            frame.add(contentPanel);
            frame.setVisible(true);
        }
    }

    static class LoginForm {
        private JFrame frame;
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton, registerButton;

        public LoginForm() {
            frame = new JFrame("Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(450, 400);
            frame.setLocationRelativeTo(null);

            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBackground(Color.BLACK);
            contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 5));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            // Username
            gbc.gridx = 0; gbc.gridy = 0;
            contentPanel.add(new JLabel("Username") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            usernameField = new JTextField(15);
            contentPanel.add(usernameField, gbc);

            // Password
            gbc.gridx = 0; gbc.gridy++;
            contentPanel.add(new JLabel("Password") {{ setForeground(Color.BLUE); }}, gbc);
            gbc.gridx = 1;
            passwordField = new JPasswordField(15);
            contentPanel.add(passwordField, gbc);

            // Show password checkbox
            gbc.gridx = 1; gbc.gridy++;
            JCheckBox showPasswordCheck = new JCheckBox("Show");
            showPasswordCheck.setForeground(Color.BLUE);
            showPasswordCheck.setBackground(Color.BLACK);
            contentPanel.add(showPasswordCheck, gbc);
            showPasswordCheck.addActionListener(e ->
                    passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char)0 : '•')
            );

            // Login button
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
            loginButton = new JButton("Login");
            contentPanel.add(loginButton, gbc);

            // Register button (link to registration)
            gbc.gridy++;
            registerButton = new JButton("Register");
            registerButton.setForeground(Color.BLUE);
            registerButton.setBackground(Color.WHITE);
            contentPanel.add(registerButton, gbc);

            Login loginLogic = new Login();
            loginButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                boolean loginSuccess = loginLogic.loginUser(username, password);
                String message = loginLogic.returnLoginStatus(loginSuccess, username);
                JOptionPane.showMessageDialog(frame, message);
                if (loginSuccess) {
                    frame.setVisible(false);
                    runQuickChatLoop(username); // launch quickchat for logged-in user
                }
            });

            registerButton.addActionListener(e -> {
                frame.setVisible(false);
                new RegisterForm(); // Open register form
            });

            frame.add(contentPanel);
            frame.setVisible(true);
        }
    }

    // ---------- REPLACED numeric QuickChat loop ----------
    private static void runQuickChatLoop(String loggedInUsername) {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.");

        String numStr = JOptionPane.showInputDialog("How many messages will you enter?");
        int maxMessages;
        try {
            if (numStr == null) return;
            maxMessages = Integer.parseInt(numStr);
            if (maxMessages < 1) {
                JOptionPane.showMessageDialog(null, "Number must be at least 1.");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Invalid number entered.");
            return;
        }

        int entered = 0;

        while (true) {
            String menu =
                    "QuickChat Menu\n\n" +
                    "1. Send Messages\n" +
                    "2. Display Sender & Recipient\n" +
                    "3. Display Longest Message\n" +
                    "4. Search Message by ID\n" +
                    "5. Search Message by Recipient\n" +
                    "6. Delete Message by Hash\n" +
                    "7. Load Stored Messages\n" +
                    "8. Display Full Report\n" +
                    "9. Quit\n\n" +
                    "Enter your choice (1-9):";

            String choiceStr = JOptionPane.showInputDialog(menu);
            if (choiceStr == null) return; // user cancelled

            int choice;
            try {
                choice = Integer.parseInt(choiceStr.trim());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Please enter a number between 1 and 9.");
                continue;
            }

            switch (choice) {
                case 1:
                    if (entered >= maxMessages) {
                        JOptionPane.showMessageDialog(null, "You reached the limit (" + maxMessages + ").");
                        break;
                    }
                    // call flow that sends/stores/disregards a single message; returns 1 if a message entry was consumed
                    int consumed = sendMessageFlow(loggedInUsername);
                    if (consumed > 0) entered += consumed;
                    break;

                case 2:
                    JOptionPane.showMessageDialog(null, displaySenderAndRecipient());
                    break;

                case 3:
                    JOptionPane.showMessageDialog(null, displayLongestSentMessage());
                    break;

                case 4:
                    String id = JOptionPane.showInputDialog("Enter Message ID to search:");
                    if (id == null || id.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No ID entered.");
                    } else {
                        JOptionPane.showMessageDialog(null, searchByMessageID(id.trim()));
                    }
                    break;

                case 5:
                    String rcpt = JOptionPane.showInputDialog("Enter Recipient to search (full number):");
                    if (rcpt == null || rcpt.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No recipient entered.");
                    } else {
                        JOptionPane.showMessageDialog(null, searchByRecipient(rcpt.trim()));
                    }
                    break;

                case 6:
                    String hash = JOptionPane.showInputDialog("Enter Message Hash to delete (exact):");
                    if (hash == null || hash.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No hash entered.");
                    } else {
                        boolean ok = deleteByMessageHash(hash.trim());
                        if (ok) JOptionPane.showMessageDialog(null, "Message deleted successfully.");
                        else JOptionPane.showMessageDialog(null, "Message hash not found.");
                    }
                    break;

                case 7:
                    loadStoredMessagesIntoArray();
                    if (storedMessages.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No stored messages found in " + STORED_FILE);
                    } else {
                        StringBuilder sb = new StringBuilder("Stored messages loaded:\n\n");
                        for (Map<String,String> m : storedMessages) {
                            sb.append("MessageID: ").append(m.getOrDefault("MessageID","")).append("\n")
                              .append("Sender: ").append(m.getOrDefault("Sender","")).append("\n")
                              .append("Recipient: ").append(m.getOrDefault("Recipient","")).append("\n")
                              .append("Message: ").append(m.getOrDefault("Message","")).append("\n\n");
                        }
                        JOptionPane.showMessageDialog(null, sb.toString());
                    }
                    break;

                case 8:
                    JOptionPane.showMessageDialog(null, displayReportAllSentMessages());
                    break;

                case 9:
                    JOptionPane.showMessageDialog(null, "Total sent messages: " + contents.size());
                    return;

                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Enter 1-9.");
            }
        }
    }

    // --------- Helper flows used by numeric menu ---------
    private static int sendMessageFlow(String loggedInUsername) {
        // returns 1 if a message entry was processed (sent/stored/disregarded), 0 otherwise
        String messageID = generateRandomDigitString(10);
        String recipient = JOptionPane.showInputDialog("Enter Recipient (include international code, e.g. +27831234567):");
        if (recipient == null) {
            JOptionPane.showMessageDialog(null, "Cancelled.");
            return 0;
        }
        recipient = recipient.trim();
        String messageText = JOptionPane.showInputDialog("Enter message (250 chars max):");
        if (messageText == null) {
            JOptionPane.showMessageDialog(null, "Cancelled.");
            return 0;
        }
        if (messageText.length() > 250) {
            JOptionPane.showMessageDialog(null, "Message exceeds 250 characters. Entry cancelled.");
            return 0;
        }

        String sender = (loggedInUsername == null || loggedInUsername.isEmpty()) ? "Developer" : loggedInUsername;
        int messageNumber = contents.size();
        Message m = new Message(messageID, sender, recipient, messageText, messageNumber);

        if (m.checkRecipientCell() == 0) {
            JOptionPane.showMessageDialog(null, "Cell phone number is incorrectly formatted. Please include international code.");
            return 0;
        }

        String actionStr =
                "Choose action:\n1. Send Message\n2. Store Message\n3. Disregard Message\nEnter 1-3:";
        String choice = JOptionPane.showInputDialog(actionStr);
        if (choice == null) {
            JOptionPane.showMessageDialog(null, "Cancelled.");
            return 0;
        }
        int act;
        try {
            act = Integer.parseInt(choice.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid action. Use 1-3.");
            return 0;
        }
        if (act < 1 || act > 3) {
            JOptionPane.showMessageDialog(null, "Invalid action. Use 1-3.");
            return 0;
        }

        String res = m.sentMessage(act);
        String details = "Message Details:\nMessageID: " + m.getMessageID()
                + "\nSender: " + m.getSender()
                + "\nMessageHash: " + m.getMessageHash()
                + "\nRecipient: " + m.getRecipient()
                + "\nMessage: " + m.getMessageText();

        JOptionPane.showMessageDialog(null, res + "\n\n" + details);
        return 1;
    }

    public static String displaySenderAndRecipient() {
    if (contents.isEmpty()) return "No sent messages available.";
    StringBuilder sb = new StringBuilder();
    for (Message m : contents) {
        sb.append("Sender: ").append(m.getSender())
          .append(" | Recipient: ").append(m.getRecipient())
          .append("\n");
    }
    return sb.toString();
}

    // ---------- Utility & startup ----------
    private static String generateRandomDigitString(int n) {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    // ---------- main: loads persisted messages into arrays and starts UI ----------
    public static void main(String[] args) {
        // quick test user
        Login.users.put("test_user", new User("Test User", "Male", "test_user", "Password123", "+27831234567", ""));

        // Load persisted sent messages (if any)
        List<Map<String,String>> persisted = readJsonListFromFile(MESSAGES_FILE);
        if (!persisted.isEmpty()) {
            for (Map<String,String> m : persisted) {
                String id = m.getOrDefault("MessageID", generateRandomDigitString(6));
                String sender = m.getOrDefault("Sender", "Developer");
                String recipient = m.getOrDefault("Recipient", "");
                String message = m.getOrDefault("Message", "");
                int num = contents.size();
                Message mm = new Message(id, sender, recipient, message, num);
                contents.add(mm);
                messageHashes.add(mm.getMessageHash());
                messageIDs.add(mm.getMessageID());
            }
        }

        // Ensure stored messages array is loaded (if present)
        loadStoredMessagesIntoArray();

        // Start login UI
        SwingUtilities.invokeLater(() -> new LoginForm());
    }
}




    
