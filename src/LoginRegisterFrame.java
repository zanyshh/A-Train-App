
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
             try {
                 Class.forName("com.mysql.cj.jdbc.Driver"); 
             } catch (ClassNotFoundException e) {
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
                        return storedPassword.equals(password); 
                    }
                }
            } catch (SQLException e) {}
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
        System.setProperty("sun.java2d.ddforcevram", "true"); 
        System.setProperty("sun.java2d.noddraw", "false");    
        System.setProperty("sun.java2d.opengl.fbobject", "true"); 
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
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);

        setTitle("User Authentication"); 

        // --- Added code to make title bar black ---
        getRootPane().putClientProperty("JRootPane.titleBarBackground", Color.BLACK);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600); 
        setLocationRelativeTo(null); 
        getContentPane().setBackground(BACKGROUND_BLACK); 

        cl = new CardLayout(); 
        cards = new JPanel(cl);
        cards.setOpaque(false);

        JPanel loginPanel = createLoginPanel();
        JPanel registerPanel = createRegisterPanel();

        cards.add(loginPanel, "LOGIN");
        cards.add(registerPanel, "REGISTER");

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(BACKGROUND_BLACK);
        outerPanel.add(cards);
        add(outerPanel);

        cl.show(cards, "LOGIN"); 
        setVisible(true); 
    }

    private JPanel createLoginPanel() {
        JPanel panel = createStyledContentPanel("RAILWAY LOGIN"); // Title changed to be all caps for emphasis 

        loginUsernameField = createRoundedTextField(null, false);
        loginPasswordField = createRoundedPasswordField();
        JButton loginButton = createStyledButton("LOGIN →", PRIMARY_COLOR, Color.BLACK);

        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

        inputContainerPanel.add(createInputBlock(createStyledLabel("Username"), loginUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25)); 
        inputContainerPanel.add(createInputBlock(createStyledLabel("Password"), loginPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(40));

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(loginButton);
        inputContainerPanel.add(buttonWrapper);
        inputContainerPanel.add(Box.createVerticalStrut(20));

        JLabel switchToRegister = createSwitchLabel("New user? Click here to Sign Up", "REGISTER");
        JPanel switchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchWrapper.setOpaque(false);
        switchWrapper.add(switchToRegister);
        inputContainerPanel.add(switchWrapper);

        loginButton.addActionListener(e -> handleLogin());

        panel.add(inputContainerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = createStyledContentPanel("NEW USER? SIGN UP!"); 

        registerUsernameField = createRoundedTextField(null, false);
        registerPasswordField = createRoundedPasswordField();
        registerConfirmPasswordField = createRoundedPasswordField();
        JButton registerButton = createStyledButton("REGISTER️ →", ACTION_COLOR, Color.BLACK);

        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 30, 0));

        inputContainerPanel.add(createInputBlock(createStyledLabel("Username:"), registerUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("Password:"), registerPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("Confirm Password:"), registerConfirmPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(40));

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(registerButton);
        inputContainerPanel.add(buttonWrapper);
        inputContainerPanel.add(Box.createVerticalStrut(20));

        JLabel switchToLogin = createSwitchLabel("Already have an account? Click here to Log In", "LOGIN");
        JPanel switchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchWrapper.setOpaque(false);
        switchWrapper.add(switchToLogin);
        inputContainerPanel.add(switchWrapper);

        registerButton.addActionListener(e -> handleRegister());

        panel.add(inputContainerPanel, BorderLayout.CENTER);
        return panel;
    }

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

        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        // ⭐ MODIFICATION: Increased Font Size to 56 and ensured Font.BOLD
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 56)); 
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    private JLabel createSwitchLabel(String text, String targetCard) {
        JLabel label = new JLabel(text);
        label.setForeground(ACTION_COLOR.brighter());
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cl.show(cards, targetCard); 
                loginUsernameField.setText("");
                loginPasswordField.setText("");
                registerUsernameField.setText("");
                registerPasswordField.setText("");
                registerConfirmPasswordField.setText("");
            }
        });
        return label;
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Username and Password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authManager.authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + username + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            try {
                // Assuming SearchTrainFrame is a separate compiled class file
                new SearchTrainFrame().setVisible(true); 
                this.dispose();
            } catch (Exception ex) {
                ex.printStackTrace(); 
                JOptionPane.showMessageDialog(this, 
                    "Error opening Search Frame. Check the console and ensure SearchTrainFrame.java is compiled and accessible.", 
                    "Fatal UI Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Login Failed: Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
            registerUsernameField.setText("");
            registerPasswordField.setText("");
            registerConfirmPasswordField.setText("");
            cl.show(cards, "LOGIN"); 
        }
    }

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

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        label.setForeground(FOREGROUND_LIGHT);
        return label;
    }

    private JTextField createRoundedTextField(String initialText, boolean readOnly) {
        JTextField field = new JTextField(initialText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        field.setEditable(!readOnly);
        return field;
    }

    private JPasswordField createRoundedPasswordField() {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 40));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        field.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return field;
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(15, 40, 15, 40));

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