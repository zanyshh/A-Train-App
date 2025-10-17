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

    //Use D3D (directX 3D on windows and opengl on unix based system
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.setProperty("sun.java2d.d3d", "true");
        } else {
            System.setProperty("sun.java2d.opengl", "true");
        }
        SwingUtilities.invokeLater(SearchTrainFrame::new);
    }

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240);
    private final Color ACTION_COLOR = new Color(255, 165, 0);
    private final int RADIUS = 8;

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

        UIManager.put("nimbusBase", new Color(10, 10, 10));
        UIManager.put("text", FOREGROUND_LIGHT);
        UIManager.put("control", BACKGROUND_BLACK);
        UIManager.put("info", PRIMARY_COLOR);
        UIManager.put("ScrollPane.background", BACKGROUND_BLACK);
        UIManager.put("TextArea.background", FIELD_BACKGROUND);
        UIManager.put("TextArea.foreground", FOREGROUND_LIGHT);
        UIManager.put("TitledBorder.titleColor", PRIMARY_COLOR);
        UIManager.put("ComboBox.background", FIELD_BACKGROUND);
        UIManager.put("ComboBox.foreground", PRIMARY_COLOR);
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

        JPanel mainContentPanel = new JPanel(new BorderLayout(15, 15));
        mainContentPanel.setBackground(BACKGROUND_BLACK);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("FIND YOUR TRAIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainContentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        searchPanel.setBackground(BACKGROUND_BLACK);

        Vector<String> stationList = dbManager.loadStations(); // Load stations from DB

        JComboBox<String> fromBox = new JComboBox<>(stationList);
        styleComboBox(fromBox);
        JComboBox<String> toBox = new JComboBox<>(stationList);
        styleComboBox(toBox);

        // Note: The date is currently fixed to today's date and not used in the search query.
        JTextField dateField = createStyledTextField(LocalDate.now().toString(), true); // Made readOnly true

        JButton searchBtn = createStyledButton("Search 🔍", PRIMARY_COLOR, Color.BLACK);

        resultList = new JList<>();
        resultList.setFont(new Font("Arial", Font.PLAIN, 18));
        resultList.setBackground(FIELD_BACKGROUND);
        resultList.setForeground(FOREGROUND_LIGHT);
        resultList.setBorder(BorderFactory.createEmptyBorder());
        resultList.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
            new EmptyBorder(5,5,5,5)
        ));

        searchPanel.add(createStyledLabel("FROM STATION:"));
        searchPanel.add(fromBox);
        searchPanel.add(createStyledLabel("TO STATION:"));
        searchPanel.add(toBox);
        searchPanel.add(createStyledLabel("TRAVEL DATE:"));
        searchPanel.add(dateField);
        searchPanel.add(new JLabel(""));
        searchPanel.add(searchBtn);

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

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_BLACK);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JButton bookBtn = createStyledButton("Book Selected Train", ACTION_COLOR, Color.BLACK);
        JPanel bookButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bookButtonPanel.setBackground(BACKGROUND_BLACK);
        bookButtonPanel.add(bookBtn);

        // --- Book Button Action Listener ---
        bookBtn.addActionListener(e -> {
            int selectedIndex = resultList.getSelectedIndex();
            if (selectedIndex != -1 && selectedIndex < currentTrainResults.size()) {
                TrainDetails selectedTrain = currentTrainResults.get(selectedIndex);

                // CRITICAL FIX: Instantiate and show the BookingFrame,
                // passing the selected train's data.
                try {
                    new BookingFrame(selectedTrain).setVisible(true);
                    this.dispose(); // Close the current frame
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
        // --- END Book Button Action Listener ---

        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
        mainContentPanel.add(bookButtonPanel, BorderLayout.SOUTH);

        add(mainContentPanel);
        setVisible(true);
    }

    // --- UI Helper Methods ---
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
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
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setBackground(FIELD_BACKGROUND);
        field.setForeground(readOnly ? PRIMARY_COLOR.darker() : PRIMARY_COLOR);
        field.setCaretColor(PRIMARY_COLOR);
        field.setEditable(!readOnly);
        field.setBorder(new EmptyBorder(8, 10, 8, 10));
        field.setOpaque(false);
        return field;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Arial", Font.PLAIN, 18));
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
        button.setFont(new Font("Arial", Font.BOLD, 18));
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