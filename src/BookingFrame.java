import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;
import java.awt.event.ActionListener;

public class BookingFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(255, 215, 0);
    private final Color BACKGROUND_BLACK = Color.BLACK;
    private final Color FIELD_BACKGROUND = new Color(25, 25, 25);
    private final Color FOREGROUND_LIGHT = new Color(240, 240, 240);
    private final Color ACTION_COLOR = new Color(255, 165, 0);
    private static final String RUPEE_SYMBOL = "\u20B9"; 
    
    private final SearchTrainFrame.TrainDetails selectedTrain;
    private JTextField trainNameField;
    private JComboBox<String> classBox;
    private JTextField seatCountField;

    public BookingFrame(SearchTrainFrame.TrainDetails trainDetails) {
        this.selectedTrain = trainDetails;
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}

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
        
        setExtendedState(JFrame.MAXIMIZED_BOTH); 

        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_BLACK);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_BLACK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(BACKGROUND_BLACK);
        JLabel titleLabel = new JLabel("TICKET BOOKING", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(7, 2, 20, 30));
        panel.setBackground(BACKGROUND_BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        trainNameField = createStyledTextField(selectedTrain.name, true);
        JTextField passengerName = createStyledTextField(null, false);
        JTextField ageField = createStyledTextField(null, false);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        styleComboBox(genderBox);
        classBox = new JComboBox<>(new String[]{"AC", "Sleeper", "Business"});
        styleComboBox(classBox);
        seatCountField = createStyledTextField("1", false);
        JButton confirmBtn = createStyledButton("Confirm Booking", PRIMARY_COLOR, Color.BLACK);

        panel.add(createStyledLabel("Train Name:"));
        panel.add(trainNameField);
        panel.add(createStyledLabel("Passenger Name:"));
        panel.add(passengerName);
        panel.add(createStyledLabel("Age:"));
        panel.add(ageField);
        panel.add(createStyledLabel("Gender:"));
        panel.add(genderBox);
        panel.add(createStyledLabel("Class:"));
        panel.add(classBox);
        panel.add(createStyledLabel("No. of Seats:"));
        panel.add(seatCountField);
        panel.add(new JLabel(""));
        panel.add(confirmBtn);

        confirmBtn.addActionListener(e -> {
            try {
                String name = passengerName.getText().trim();
                int seats = Integer.parseInt(seatCountField.getText().trim());
                String selectedClass = (String) classBox.getSelectedItem();

                if (seats <= 0 || name.isEmpty() || ageField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill out all passenger details correctly.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int totalAmount = calculatePrice(selectedTrain, selectedClass, seats);
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Booking Confirmed!\n\nTrain: %s (%s)\nSeats: %d\nTotal Amount: %s%d\n\nTicket booked successfully.", 
                        selectedTrain.name, selectedClass, seats, RUPEE_SYMBOL, totalAmount), 
                    "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);

                this.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please ensure Age and No. of Seats are valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(panel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }
    
    private int calculatePrice(SearchTrainFrame.TrainDetails train, String selectedClass, int seatCount) {
        int basePrice;
        if (selectedClass.equals("AC")) {
            basePrice = train.basePriceAC;
        } else if (selectedClass.equals("Sleeper")) {
            basePrice = train.basePriceSleeper;
        } else if (selectedClass.equals("Business")) {
            basePrice = train.basePriceBusiness;
        } else {
            basePrice = 0;
        }
        return basePrice * seatCount;
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