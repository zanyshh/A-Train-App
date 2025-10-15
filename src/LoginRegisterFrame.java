import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;
import java.util.HashMap;
import java.util.Map;

public class LoginRegisterFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240);
    private final Color ACTION_COLOR = new Color(255, 165, 0);

    private JPanel cards;
    private CardLayout cl;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;
    
    // Mock user storage for demo purposes
    private final Map<String, String> MOCK_USERS = new HashMap<>();

    private class MockAuthManager {
        public MockAuthManager() {
            // Hardcoded initial user for testing login
            MOCK_USERS.put("testuser", "password123");
            MOCK_USERS.put("demo", "demo");
        }
        
        public boolean authenticateUser(String username, String password) {
            return MOCK_USERS.containsKey(username) && MOCK_USERS.get(username).equals(password);
        }

        public boolean registerUser(String username, String password) {
            if (MOCK_USERS.containsKey(username)) {
                JOptionPane.showMessageDialog(LoginRegisterFrame.this, 
                    "Username already exists. Choose a different one.", 
                    "Registration Failed", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            MOCK_USERS.put(username, password);
            return true;
        }
    }
    
    private final MockAuthManager authManager = new MockAuthManager();

    public LoginRegisterFrame() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);

        setTitle("User Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
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
        JPanel panel = createStyledContentPanel("RAILWAY LOGIN");
        
        loginUsernameField = createStyledTextField(null, false);
        loginPasswordField = createStyledPasswordField();
        JButton loginButton = createStyledButton("LOGIN", PRIMARY_COLOR, Color.BLACK);
        
        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 50, 0));

        inputContainerPanel.add(createInputBlock(createStyledLabel("USERNAME:"), loginUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("PASSWORD:"), loginPasswordField));
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
        JPanel panel = createStyledContentPanel("NEW USER SIGN UP");
        
        registerUsernameField = createStyledTextField(null, false);
        registerPasswordField = createStyledPasswordField();
        registerConfirmPasswordField = createStyledPasswordField();
        JButton registerButton = createStyledButton("REGISTER", ACTION_COLOR, Color.BLACK);
        
        JPanel inputContainerPanel = new JPanel();
        inputContainerPanel.setOpaque(false);
        inputContainerPanel.setLayout(new BoxLayout(inputContainerPanel, BoxLayout.Y_AXIS));
        inputContainerPanel.setBorder(new EmptyBorder(50, 0, 30, 0));

        inputContainerPanel.add(createInputBlock(createStyledLabel("USERNAME:"), registerUsernameField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("PASSWORD:"), registerPasswordField));
        inputContainerPanel.add(Box.createVerticalStrut(25));
        inputContainerPanel.add(createInputBlock(createStyledLabel("CONFIRM PASSWORD:"), registerConfirmPasswordField));
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }
    
    private JLabel createSwitchLabel(String text, String targetCard) {
        JLabel label = new JLabel(text);
        label.setForeground(ACTION_COLOR.brighter());
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authManager.authenticateUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + username + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            new SearchTrainFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Login Failed: Invalid username or password.\n\nTry: 'demo' / 'demo'", "Error", JOptionPane.ERROR_MESSAGE);
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(FOREGROUND_LIGHT);
        return label;
    }

    private JTextField createStyledTextField(String initialText, boolean readOnly) {
        JTextField field = new JTextField(initialText);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setEditable(!readOnly);

        Border baseBorder = BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 2, 0, new Color(40, 40, 40)),
            new EmptyBorder(8, 5, 8, 5)
        );
        field.setBorder(baseBorder);

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

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        
        Border baseBorder = BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 2, 0, new Color(40, 40, 40)),
            new EmptyBorder(8, 5, 8, 5)
        );
        field.setBorder(baseBorder);

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

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginRegisterFrame::new);
    }
}