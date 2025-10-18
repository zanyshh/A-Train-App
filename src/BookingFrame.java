import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// JDBC Imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

// CRITICAL FIX 1: Import the TrainDetails class from SearchTrainFrame.java
// If SearchTrainFrame is in the default package, no import statement is needed.
// If it is in a package, you would need: import com.yourpackage.SearchTrainFrame.TrainDetails;


public class BookingFrame extends JFrame {

    public static class DBManager {

        private static final String DB_URL = "jdbc:mysql://localhost:3306/railway_db";
        private static final String USER = "root";
        private static final String PASS = "root";

        public Connection getConnection() throws SQLException {
            // FIX: Added Class.forName for robust driver loading
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + e.getMessage());
            }
            return java.sql.DriverManager.getConnection(DB_URL, USER, PASS);
        }

        public boolean saveBooking(String trainId, String passengerName, int age, String travelClass, int seats, int totalAmount) {
            String sql = "INSERT INTO bookings (train_id, passenger_name, age, travel_class, seats, total_amount, booking_reference) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String bookingRef = generateBookingReference();

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, trainId);
                pstmt.setString(2, passengerName);
                pstmt.setInt(3, age);
                pstmt.setString(4, travelClass);
                pstmt.setInt(5, seats);
                pstmt.setInt(6, totalAmount);
                pstmt.setString(7, bookingRef);

                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error during booking: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        private String generateBookingReference() {
            // Simple 8-character reference code
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            Random random = new Random();
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }
    }
    // End of DBManager

    public static void main(String[] args) {
        
        // Performance Critical Components
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) System.setProperty("sun.java2d.d3d", "true");
        else System.setProperty("sun.java2d.opengl", "true");
        
      //  System.setProperty("sun.java2d.trace", "timestamp"); // for debugging only system tracing
        System.setProperty("sun.java2d.ddforcevram", "true"); // force VRAM usage
        System.setProperty("sun.java2d.noddraw", "false");   // ensure DDraw not disabled
        System.setProperty("sun.java2d.opengl.fbobject", "true"); // better OpenGL FBO


        // Mock train data for standalone testing
        // You MUST use SearchTrainFrame.TrainDetails here!
        SearchTrainFrame.TrainDetails mockTrain = new SearchTrainFrame.TrainDetails("12051", "Janshatabdi Express", 1200, 500, 1800);
        SwingUtilities.invokeLater(() -> new BookingFrame(mockTrain));
    }

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240);
    private final Color ACTION_COLOR = new Color(255, 165, 0);
    private final int RADIUS = 8;
    private static final String RUPEE_SYMBOL = "\u20B9";

    // IMPORTANT: The type here must match the type in SearchTrainFrame!
    private final SearchTrainFrame.TrainDetails selectedTrain;
    private JTextField trainNameField, seatCountField;
    private final DBManager dbManager = new DBManager();

    // IMPORTANT: The constructor must accept SearchTrainFrame.TrainDetails!
    public BookingFrame(SearchTrainFrame.TrainDetails trainDetails) {
        this.selectedTrain = trainDetails;

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);
        UIManager.put("ComboBox.background", FIELD_BACKGROUND);
        UIManager.put("ComboBox.foreground", PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionForeground", BACKGROUND_BLACK);

        setTitle("Book Your Ticket");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_BLACK);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_BLACK);
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("🎟️ Ticket Booking", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        trainNameField = createStyledTextField(selectedTrain.name, true);
        JTextField passengerName = createStyledTextField("", false);
        JTextField ageField = createStyledTextField("", false);
        JComboBox<String> classBox = createStyledComboBox(new String[]{"AC", "Sleeper", "Business"});
        seatCountField = createStyledTextField("1", false);
        JButton confirmBtn = createStyledButton("Confirm Booking ➜", PRIMARY_COLOR, Color.BLACK);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Train Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(trainNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Passenger Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passengerName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Age:"), gbc);
        gbc.gridx = 1;
        formPanel.add(ageField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Class:"), gbc);
        gbc.gridx = 1;
        formPanel.add(classBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createStyledLabel("No. of Seats:"), gbc);
        gbc.gridx = 1;
        formPanel.add(seatCountField, gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(confirmBtn, gbc);

        confirmBtn.addActionListener(e -> {
            try {
                String name = passengerName.getText().trim();
                String ageStr = ageField.getText().trim();
                int seats = Integer.parseInt(seatCountField.getText().trim());
                String selectedClass = (String) classBox.getSelectedItem();

                if (name.isEmpty() || ageStr.isEmpty() || seats <= 0) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields correctly.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int age = Integer.parseInt(ageStr);

                int totalAmount = calculatePrice(selectedTrain, selectedClass, seats);

                if (dbManager.saveBooking(selectedTrain.id, name, age, selectedClass, seats, totalAmount)) {
                    JOptionPane.showMessageDialog(this,
                            String.format("Booking Confirmed! ✅\n\nTrain: %s\nClass: %s\nSeats: %d\nTotal: %s%d",
                                    selectedTrain.name, selectedClass, seats, RUPEE_SYMBOL, totalAmount),
                            "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
                }

                // Clear fields for new booking
                passengerName.setText("");
                ageField.setText("");
                seatCountField.setText("1");
                classBox.setSelectedIndex(0);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age and Seats must be numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private int calculatePrice(SearchTrainFrame.TrainDetails train, String cls, int seats) {
        int basePrice = 0;
        switch (cls) {
            case "AC":
                basePrice = train.basePriceAC;
                break;
            case "Sleeper":
                basePrice = train.basePriceSleeper;
                break;
            case "Business":
                basePrice = train.basePriceBusiness;
                break;
        }
        return basePrice * seats;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        label.setForeground(FOREGROUND_LIGHT);
        return label;
    }

    private JTextField createStyledTextField(String text, boolean readOnly) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setEditable(!readOnly);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BACKGROUND, RADIUS, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        box.setBackground(FIELD_BACKGROUND);
        box.setForeground(PRIMARY_COLOR);
        box.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton("\u25BC");
                arrow.setBorder(BorderFactory.createEmptyBorder());
                arrow.setContentAreaFilled(false);
                arrow.setFocusPainted(false);
                arrow.setForeground(PRIMARY_COLOR);
                return arrow;
            }
        });
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BACKGROUND, RADIUS, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return box;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(bg, RADIUS, true));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
}