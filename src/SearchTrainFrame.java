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

public class SearchTrainFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240);
    private final Color ACTION_COLOR = new Color(255, 165, 0);
    
    private JList<String> resultList;
    
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
    
    private static final Vector<TrainDetails> MOCK_TRAINS = new Vector<>();
    static {
        MOCK_TRAINS.add(new TrainDetails("12051", "Janshatabdi Express", 1200, 500, 1800)); 
        MOCK_TRAINS.add(new TrainDetails("16346", "Netravati Express", 900, 350, 1500));
        MOCK_TRAINS.add(new TrainDetails("22114", "Duronto Express", 1500, 650, 2500));
    }
    
    private Vector<String> loadStations() {
        Vector<String> stations = new Vector<>();
        stations.add("Select Station");
        stations.add("Mumbai CSMT");
        stations.add("New Delhi");
        stations.add("Kollam Jn");
        return stations;
    }
    
    private Vector<String> getTrainDisplayList(Vector<TrainDetails> trains) {
        Vector<String> display = new Vector<>();
        for (TrainDetails train : trains) {
            display.add(train.toString());
        }
        return display;
    }

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
        
        setExtendedState(JFrame.MAXIMIZED_BOTH); 

        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_BLACK);

        JPanel mainContentPanel = new JPanel(new BorderLayout(15, 15));
        mainContentPanel.setBackground(BACKGROUND_BLACK);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("FIND YOUR TRAIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);
        mainContentPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        searchPanel.setBackground(BACKGROUND_BLACK);
        
        Vector<String> stationList = loadStations();

        JComboBox<String> fromBox = new JComboBox<>(stationList);
        styleComboBox(fromBox);
        JComboBox<String> toBox = new JComboBox<>(stationList);
        styleComboBox(toBox);
        
        JTextField dateField = createStyledTextField(LocalDate.now().toString(), false);
        
        JButton searchBtn = createStyledButton("Search", PRIMARY_COLOR, Color.BLACK);

        resultList = new JList<>();
        resultList.setFont(new Font("Monospaced", Font.PLAIN, 18));
        
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, PRIMARY_COLOR),
            "Available Trains",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 20),
            PRIMARY_COLOR
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

            if ("Select Station".equals(from) || "Select Station".equals(to)) {
                JOptionPane.showMessageDialog(this, "Please select valid 'From' and 'To' stations.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                resultList.setListData(new Vector<>());
            } else {
                resultList.setListData(getTrainDisplayList(MOCK_TRAINS));
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

        bookBtn.addActionListener(e -> {
            int selectedIndex = resultList.getSelectedIndex();
            if (selectedIndex != -1) {
                TrainDetails selectedTrain = MOCK_TRAINS.get(selectedIndex);
                this.dispose();
                new BookingFrame(selectedTrain).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a train from the list to book.", "No Train Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
        mainContentPanel.add(bookButtonPanel, BorderLayout.SOUTH);

        add(mainContentPanel);
        setVisible(true);
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
                        new MatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
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
    
    private void styleComboBox(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
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