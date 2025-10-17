import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;
// JDBC
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class LoginRegisterFrame extends JFrame {
    
    // --- DBManager Class ---
    public static class DBManager {
        
        private static final String DB_URL = "jdbc:mysql://localhost:3306/railway_db";
        private static final String USER = "root";
        private static final String PASS = "root"; // <-- Ensure this is your actual MySQL root password
        
        public Connection getConnection() throws SQLException {
             // Added Class.forName for robust driver loading (especially with modern JDKs)
             try {
                 Class.forName("com.mysql.cj.jdbc.Driver"); 
             } catch (ClassNotFoundException e) {
                 // In case the driver JAR is somehow not on the classpath
                 throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
             }
             return java.sql.DriverManager.getConnection(DB_URL, USER, PASS);
        }

        public boolean authenticateUser(String username, String password) {
            String sql = "SELECT password FROM users WHERE username = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        // This method checks the username and password against the 'users' table.
                        return storedPassword.equals(password); 
                    }
                }
            } catch (SQLException e) {
                 // Removed JOptionPane here to avoid repeated errors. Handled in the UI method.
                 // e.printStackTrace(); // Optional: print error to console
            }
            return false;
        }

        public boolean registerUser(String username, String password) {
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
            
            try (Connection conn = getConnection()) {
                
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, username);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(null, "Username already exists. Choose a different one.", "Registration Failed", JOptionPane.WARNING_MESSAGE);
                            return false; 
                        }
                    }
                }
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, password); 
                    // This method registers a new user by inserting their details into the 'users' table.
                    insertStmt.executeUpdate(); 
                    return true;
                }
            } catch (SQLException e) {
                 JOptionPane.showMessageDialog(null, "Database error during registration: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                 return false;
            }
        }
    }
    
    // --- Main Method ---
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("sun.java2d.d3d", "true"); 
        } else {
            System.setProperty("sun.java2d.opengl", "true"); 
        }
        SwingUtilities.invokeLater(LoginRegisterFrame::new);
    }

    
    
    // Color constants
    private final Color PRIMARY_COLOR = new Color(255, 215, 0); 
    private final Color BACKGROUND_BLACK = Color.BLACK; 
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25); 
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240); 
    private final Color ACTION_COLOR = new Color(255, 165, 0); 
    
    
    
    // Card layout for switching between login/register
    private JPanel cards;
    private CardLayout cl;
    
    
    
    
    // Login input fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    
    
    
    
    // Register input fields
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;
    
    
    
    
    // Instance of the database manager
    private final DBManager authManager = new DBManager(); 

    
    
    
    
    
    // Constructor
    public LoginRegisterFrame() {
        // Set Nimbus look & feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

        // Customize colors
        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);

        setTitle("User Authentication"); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600); 
        setLocationRelativeTo(null); 
        getContentPane().setBackground(BACKGROUND_BLACK); 

        cl = new CardLayout(); 
        cards = new JPanel(cl);
        cards.setOpaque(false);

        // Create login and register panels
        JPanel loginPanel = createLoginPanel();
        JPanel registerPanel = createRegisterPanel();

        // Add panels to cards
        cards.add(loginPanel, "LOGIN");
        cards.add(registerPanel, "REGISTER");

        // Outer panel to center cards
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(BACKGROUND_BLACK);
        outerPanel.add(cards);
        add(outerPanel);

        cl.show(cards, "LOGIN"); 
        setVisible(true); 
    }

    // Create login panel
    private JPanel createLoginPanel() {
        JPanel panel = createStyledContentPanel("RAILWAY LOGIN"); 

        // Input fields
        loginUsernameField = createStyledTextField(null, false);
        loginPasswordField = createStyledPasswordField();
        JButton loginButton = createStyledButton("LOGIN", PRIMARY_COLOR, Color.BLACK);

        // Container for inputs
        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

        // Add input blocks
        inputContainerPanel.add(createInputBlock(createStyledLabel("USERNAME:"), loginUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25)); 
        inputContainerPanel.add(createInputBlock(createStyledLabel("PASSWORD:"), loginPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(40));

        // Add login button
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(loginButton);
        inputContainerPanel.add(buttonWrapper);
        inputContainerPanel.add(Box.createVerticalStrut(20));

        // Switch to register label
        JLabel switchToRegister = createSwitchLabel("New user? Click here to Sign Up", "REGISTER");
        JPanel switchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchWrapper.setOpaque(false);
        switchWrapper.add(switchToRegister);
        inputContainerPanel.add(switchWrapper);

        // Login button action
        loginButton.addActionListener(e -> handleLogin());

        panel.add(inputContainerPanel, BorderLayout.CENTER);
        return panel;
    }

    // Create register panel
    private JPanel createRegisterPanel() {
        JPanel panel = createStyledContentPanel("NEW USER SIGN UP"); 

        // Input fields
        registerUsernameField = createStyledTextField(null, false);
        registerPasswordField = createStyledPasswordField();
        registerConfirmPasswordField = createStyledPasswordField();
        JButton registerButton = createStyledButton("REGISTER", ACTION_COLOR, Color.BLACK);

        // Container for inputs
        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 30, 0));

        // Add input blocks
        inputContainerPanel.add(createInputBlock(createStyledLabel("USERNAME:"), registerUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("PASSWORD:"), registerPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("CONFIRM PASSWORD:"), registerConfirmPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(40));

        // Add register button
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(registerButton);
        inputContainerPanel.add(buttonWrapper);
        inputContainerPanel.add(Box.createVerticalStrut(20));

        // Switch to login label
        JLabel switchToLogin = createSwitchLabel("Already have an account? Click here to Log In", "LOGIN");
        JPanel switchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchWrapper.setOpaque(false);
        switchWrapper.add(switchToLogin);
        inputContainerPanel.add(switchWrapper);

        // Register button action
        registerButton.addActionListener(e -> handleRegister());

        panel.add(inputContainerPanel, BorderLayout.CENTER);
        return panel;
    }

    // Create styled panel with title and rounded corners
    private JPanel createStyledContentPanel(String titleText) {
        JPanel panel = new JPanel(new BorderLayout(30, 30)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BACKGROUND_BLACK.darker().darker()); 
                int arc = 40; 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.setColor(PRIMARY_COLOR.darker()); 
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(50, 60, 50, 60));
        panel.setPreferredSize(new Dimension(600, 650));

        // Title label
        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    // Create clickable label to switch panels
    private JLabel createSwitchLabel(String text, String targetCard) {
        JLabel label = new JLabel(text);
        label.setForeground(ACTION_COLOR.brighter());
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cl.show(cards, targetCard); 
                // Clear all fields when switching
                loginUsernameField.setText("");
                loginPasswordField.setText("");
                registerUsernameField.setText("");
                registerPasswordField.setText("");
                registerConfirmPasswordField.setText("");
            }
        });
        return label;
    }

    // Handle login logic
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authManager.authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + username + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // --- FIX 1: Open SearchTrainFrame and add exception handling ---
            try {
                // Ensure the SearchTrainFrame class is in your project and compiled
                new SearchTrainFrame().setVisible(true); 
                this.dispose(); // Close the current frame
            } catch (Exception ex) {
                // If the SearchTrainFrame fails (e.g., a DB connection issue in its constructor)
                ex.printStackTrace(); // Print the detailed error to your console
                JOptionPane.showMessageDialog(this, 
                    "Error opening Search Frame. Check the console and ensure SearchTrainFrame's DBManager has the correct password.", 
                    "Fatal UI Error", JOptionPane.ERROR_MESSAGE);
            }
            // --- END FIX 1 ---
        } else {
            // Note: The mock demo credentials are now only valid if they are first registered in the DB.
            JOptionPane.showMessageDialog(this, "Login Failed: Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Handle registration logic
    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(registerConfirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authManager.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration Successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
            // Clear fields
            registerUsernameField.setText("");
            registerPasswordField.setText("");
            registerConfirmPasswordField.setText("");
            cl.show(cards, "LOGIN"); 
            // The application expects the user to manually click LOGIN after this success.
        }
    }

    // Create a labeled input block
    private JPanel createInputBlock(JLabel label, JComponent field) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(5)); 

        field.setMaximumSize(new Dimension(400, field.getPreferredSize().height + 15));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(field);

        return wrapper;
    }

    // Create styled label
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(FOREGROUND_LIGHT);
        return label;
    }

    // Create styled text field
    private JTextField createStyledTextField(String initialText, boolean readOnly) {
        JTextField field = new JTextField(initialText);
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setEditable(!readOnly);

        Border baseBorder = BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(40, 40, 40)),
                new EmptyBorder(8, 5, 8, 5)
        );
        field.setBorder(baseBorder);

        // Highlight border on focus
        if (!readOnly) {
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                private final Border focusBaseBorder = field.getBorder();

                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            new MatteBorder(0, 0, 2, 0, ACTION_COLOR),
                            new EmptyBorder(8, 5, 8, 5)
                    ));
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    field.setBorder(focusBaseBorder);
                }
            });
        }
        return field;
    }

    // Create styled password field
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);

        Border baseBorder = BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(40, 40, 40)),
                new EmptyBorder(8, 5, 8, 5)
        );
        field.setBorder(baseBorder);

        // Highlight border on focus
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            private final Border focusBaseBorder = field.getBorder();

            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 0, 2, 0, ACTION_COLOR),
                        new EmptyBorder(8, 5, 8, 5)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(baseBorder);
            }
        });
        return field;
    }

    // Create styled button with hover effect
    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(15, 40, 15, 40));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
        return button;
    }
}