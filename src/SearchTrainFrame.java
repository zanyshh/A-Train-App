import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Vector;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
// JDBC Imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchTrainFrame extends JFrame {

    // --- CRITICAL FIX 1: TrainDetails Class Definition (Define it ONCE here) ---
    // It is public so other files (like BookingFrame) can see it.
    public static class TrainDetails {
        public String id;
        public String name;
        public int basePriceAC;
        public int basePriceSleeper;
        public int basePriceBusiness;

        public TrainDetails(String id, String name, int priceAC, int priceSleeper, int priceBusiness) {
            this.id = id;
            this.name = name;
            this.basePriceAC = priceAC;
            this.basePriceSleeper = priceSleeper;
            this.basePriceBusiness = priceBusiness;
        }

        @Override
        public String toString() {
            return id + " | " + name + " (10:00) | AC:" + basePriceAC + " | SL:" + basePriceSleeper;
        }
    }
    // --- End TrainDetails Class ---

    // --- DBManager Class (Connection Fix) ---
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

        public Vector<String> loadStations() {
            Vector<String> stations = new Vector<>();
            stations.add("Select Station");
            String sql = "SELECT name FROM stations ORDER BY name";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    stations.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error loading stations: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            return stations;
        }

        public Vector<TrainDetails> searchTrains(String fromStation, String toStation) {
            Vector<TrainDetails> trains = new Vector<>();

            // NOTE: This simple query returns ALL trains, as designed for mock data.
            String sql = "SELECT id, name, price_ac, price_sleeper, price_business FROM trains";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    int priceAC = rs.getInt("price_ac");
                    int priceSleeper = rs.getInt("price_sleeper");
                    int priceBusiness = rs.getInt("price_business");
                    trains.add(new TrainDetails(id, name, priceAC, priceSleeper, priceBusiness));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database error during train search: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
            return trains;
        }
    }
    // End of DBManager

    
    public static void main(String[] args) {
        // Performance Critical Components
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("sun.java2d.d3d", "true"); // for d3d usage
        } else {
            System.setProperty("sun.java2d.opengl", "true"); // for OpenGl usage
        }
        SwingUtilities.invokeLater(SearchTrainFrame::new);
         //  System.setProperty("sun.java2d.trace", "timestamp"); // for debugging only system tracing
        System.setProperty("sun.java2d.ddforcevram", "true"); // force VRAM usage
        System.setProperty("sun.java2d.noddraw", "false");   // ensure DDraw not disabled
        System.setProperty("sun.java2d.opengl.fbobject", "true"); // better OpenGL FBO
    }

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(200, 200, 200); // changed to light grey
    private final Color ACTION_COLOR = new Color(255, 165, 0);
    private final int RADIUS = 18; // corner rounding updated to 18px

    private JList<String> resultList;

    // Database Manager Instance
    private final DBManager dbManager = new DBManager();
    // Storage for the last search results
    private Vector<TrainDetails> currentTrainResults = new Vector<>();

    // --- Utility Method ---
    private Vector<String> getTrainDisplayList(Vector<TrainDetails> trains) {
        Vector<String> display = new Vector<>();
        for (TrainDetails train : trains) {
            display.add(train.toString());
        }
        return display;
    }
    // --- End Utility Method ---

    // --- Constructor ---
    public SearchTrainFrame() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

        // UI Defaults
        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);
        UIManager.put("ScrollPane.background", BACKGROUND_BLACK);
        UIManager.put("TextArea.background", FIELD_BACKGROUND);
        UIManager.put("TextArea.foreground", FOREGROUND_LIGHT);
        UIManager.put("TitledBorder.titleColor", PRIMARY_COLOR);
        UIManager.put("ComboBox.background", FIELD_BACKGROUND);
        UIManager.put("ComboBox.foreground", FOREGROUND_LIGHT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionForeground", BACKGROUND_BLACK);
        UIManager.put("List.background", FIELD_BACKGROUND);
        UIManager.put("List.foreground", FOREGROUND_LIGHT);
        UIManager.put("List.selectionBackground", PRIMARY_COLOR.darker());
        UIManager.put("List.selectionForeground", BACKGROUND_BLACK);

        setTitle("Search Trains");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_BLACK);

        JPanel mainContentPanel = new JPanel(new BorderLayout(20, 20));
        mainContentPanel.setBackground(BACKGROUND_BLACK);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🔍 ︎Find Your Train", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 42));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainContentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(BACKGROUND_BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Vector<String> stationList = dbManager.loadStations(); // Load stations from DB

        JComboBox<String> fromBox = new JComboBox<>(stationList);
        JComboBox<String> toBox = new JComboBox<>(stationList);
        styleComboBox(fromBox);
        styleComboBox(toBox);

        JTextField dateField = createStyledTextField(LocalDate.now().toString(), true);

        JButton searchBtn = createStyledButton("Search 🔍︎", PRIMARY_COLOR, Color.BLACK);

        resultList = new JList<>();
        resultList.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        resultList.setBackground(FIELD_BACKGROUND);
        resultList.setForeground(FOREGROUND_LIGHT);
        resultList.setBorder(BorderFactory.createEmptyBorder());
        resultList.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
            new EmptyBorder(5,5,5,5)
        ));
        scrollPane.getViewport().setBackground(FIELD_BACKGROUND);

        // Add FROM label and combo box
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(createStyledLabel("From Station:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(fromBox, gbc);

        // Add TO label and combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        searchPanel.add(createStyledLabel("To Station"), gbc);
        gbc.gridx = 1;
        searchPanel.add(toBox, gbc);

        // Add DATE label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        searchPanel.add(createStyledLabel("Departure:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(dateField, gbc);

        // Add SEARCH button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        searchPanel.add(searchBtn, gbc);

        searchBtn.addActionListener(e -> {
            String from = (String) fromBox.getSelectedItem();
            String to = (String) toBox.getSelectedItem();

            if ("Select Station".equals(from) || "Select Station".equals(to) || from.equals(to)) {
                JOptionPane.showMessageDialog(this, "Please select two different valid 'From' and 'To' stations.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                currentTrainResults = new Vector<>();
                resultList.setListData(new Vector<>());
            } else {
                currentTrainResults = dbManager.searchTrains(from, to);
                if (currentTrainResults.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No trains found for this route.", "No Results", JOptionPane.INFORMATION_MESSAGE);
                }
                resultList.setListData(getTrainDisplayList(currentTrainResults));
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(BACKGROUND_BLACK);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JButton bookBtn = createStyledButton("Book Selected Train", ACTION_COLOR, Color.BLACK);
        JPanel bookButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bookButtonPanel.setBackground(BACKGROUND_BLACK);
        bookButtonPanel.add(bookBtn);

        bookBtn.addActionListener(e -> {
            int selectedIndex = resultList.getSelectedIndex();
            if (selectedIndex != -1 && selectedIndex < currentTrainResults.size()) {
                TrainDetails selectedTrain = currentTrainResults.get(selectedIndex);

                try {
                    new BookingFrame(selectedTrain).setVisible(true);
                    this.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Error launching Booking Frame: " + ex.getMessage(),
                        "Fatal UI Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Please select a train from the list to book.", "No Train Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
        mainContentPanel.add(bookButtonPanel, BorderLayout.SOUTH);

        add(mainContentPanel);
        setVisible(true);
    }

    // --- UI Helper Methods ---
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        label.setForeground(FOREGROUND_LIGHT);
        return label;
    }

    private JTextField createStyledTextField(String initialText, boolean readOnly) {
        JTextField field = new JTextField(initialText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setEditable(!readOnly);
        field.setBorder(new EmptyBorder(8, 10, 8, 10));
        field.setOpaque(false);
        return field;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI emoji", Font.PLAIN, 18));
        box.setBackground(FIELD_BACKGROUND);
        box.setForeground(FOREGROUND_LIGHT);

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

            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(FIELD_BACKGROUND);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, RADIUS, RADIUS);
                super.paintCurrentValue(g, bounds, hasFocus);
                g2.dispose();
            }
        });

        box.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI emoji", Font.BOLD, 18));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

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
