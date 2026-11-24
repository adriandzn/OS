import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
public class CLook extends JFrame {

	

// VARIABLES
	
    // Main menu reference and input limits
    private final JFrame mainMenu;
    private static final int MAX_DISK_SIZE = 200;
    private static final int MAX_REQUESTS = 100;
    
    // User inputs
    private final JTextField diskSizeField = new JTextField();
    private final JTextField headField = new JTextField();
    private final JTextArea requestsField = new JTextArea(3, 20);
    private final JComboBox<String> directionCombo = new JComboBox<>(new String[] {"Upwards", "Downwards"});
    private final JScrollPane requestsScrollPane = new JScrollPane(requestsField);
    
    // Error messages and request counter
    private final JLabel maxError = errorLabel();
    private final JLabel headError = errorLabel();
    private final JLabel requestsError = errorLabel();
    private final JLabel requestCountLabel = new JLabel("0 / 100 requests");
    
    // Buttons
    private final JButton simulateButton = new JButton("Simulate C-LOOK");
    private final JButton resetButton = new JButton("Reset");
    private final JButton sampleInputButton = new JButton("Load Sample Data");
    
    // Outputs
    private final JLabel totalMovementValue = new JLabel("--");
    private final JLabel averageSeekValue = new JLabel("--");
    private final JTextArea sequenceValue = new JTextArea("Enter valid inputs, then click Simulate.", 3, 40);
    private final JTextArea stepMovementValue = new JTextArea("Movement for each step will appear here.", 5, 40);
    
    // Graph
    private final DiskChart chart = new DiskChart();

    

// USER INTERFACE
    
    public CLook() {
        this(null);
    }
    public CLook(JFrame mainMenu) {
        super("C-LOOK Disk Scheduling Simulator");
        this.mainMenu = mainMenu;
        setDefaultCloseOperation(mainMenu == null ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 700));
        setSize(1000, 820);
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(250, 248, 239));
        setContentPane(root);
        JPanel page = new ScrollablePage(new BorderLayout(16, 16));
        page.setBorder(new EmptyBorder(18, 20, 18, 20));
        page.setBackground(new Color(250, 248, 239));
        root.add(page, BorderLayout.CENTER);
        JLabel title = new JLabel("C-LOOK Disk Scheduling Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(new Color(38, 38, 34));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        page.add(heading, BorderLayout.NORTH);
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 0, 2, 0));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        page.add(content, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(page,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setBackground(new Color(250, 248, 239));
        root.add(createNavigationPanel(), BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        
        // Input area
        JPanel inputCard = cardPanel();
        inputCard.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 6, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        addInputRow(inputCard, c, 0, "Disk size", diskSizeField,
                "Enter the total number of cylinders. Maximum disk size: 200. Example: 200", maxError);
        addInputRow(inputCard, c, 1, "Current read/write head position", headField,
                "Must be from 0 to disk size - 1. Example: 67", headError);
        requestsField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        requestsField.setLineWrap(true);
        requestsField.setWrapStyleWord(true);
        requestsField.setBorder(new EmptyBorder(8, 9, 8, 9));
        requestsField.setTabSize(4);
        requestsScrollPane.setBorder(new LineBorder(new Color(210, 195, 145), 1, true));
        requestsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        requestsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        requestsScrollPane.setPreferredSize(new Dimension(400, 78));
        requestsScrollPane.setMinimumSize(new Dimension(180, 65));
        addInputRow(inputCard, c, 2, "Disk requests", requestsScrollPane,
                "Separate cylinder numbers using spaces or commas. Maximum amount: 100.", requestsError);
        directionCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        directionCombo.setBackground(new Color(255, 255, 252));
        directionCombo.setForeground(new Color(45, 43, 36));
        directionCombo.setBorder(new LineBorder(new Color(210, 195, 145), 1, true));
        addInputRow(inputCard, c, 3, "Initial direction", directionCombo,
                "Choose whether the head moves toward higher or lower cylinder numbers first.", errorLabel());
        GridBagConstraints countC = (GridBagConstraints)c.clone();
        countC.gridx = 1; countC.gridy = 4; countC.weightx = 1;
        countC.insets = new Insets(0, 9, 8, 9);
        requestCountLabel.setForeground(new Color(93, 83, 55));
        inputCard.add(requestCountLabel, countC);
        GridBagConstraints buttonC = (GridBagConstraints)c.clone();
        buttonC.gridx = 2; buttonC.gridy = 4; buttonC.weightx = 0;
        buttonC.insets = new Insets(0, 9, 8, 9);
        simulateButton.setFocusPainted(false);
        simulateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        simulateButton.setForeground(new Color(38, 38, 34));
        simulateButton.setBackground(new Color(244, 197, 66));
        simulateButton.setBorder(new CompoundBorder(new LineBorder(new Color(218, 166, 28), 1, true), new EmptyBorder(11, 18, 11, 18)));
        simulateButton.setEnabled(false);
        simulateButton.setForeground(new Color(38, 38, 34));
        inputCard.add(simulateButton, buttonC);
        GridBagConstraints resetC = (GridBagConstraints)c.clone();
        resetC.gridx = 2; resetC.gridy = 5; resetC.weightx = 0;
        resetC.insets = new Insets(0, 9, 8, 9);
        resetButton.setFocusPainted(false);
        resetButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        resetButton.setForeground(new Color(65, 62, 52));
        resetButton.setBackground(new Color(242, 235, 211));
        resetButton.setBorder(new CompoundBorder(new LineBorder(new Color(222, 211, 177), 1, true), new EmptyBorder(11, 18, 11, 18)));
        inputCard.add(resetButton, resetC);
        GridBagConstraints sampleC = (GridBagConstraints)c.clone();
        sampleC.gridx = 2; sampleC.gridy = 6; sampleC.weightx = 0;
        sampleC.insets = new Insets(0, 9, 8, 9);
        sampleInputButton.setFocusPainted(false);
        sampleInputButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sampleInputButton.setForeground(new Color(65, 62, 52));
        sampleInputButton.setBackground(new Color(255, 248, 218));
        sampleInputButton.setBorder(new CompoundBorder(new LineBorder(new Color(229, 205, 115), 1, true), new EmptyBorder(10, 14, 10, 14)));
        inputCard.add(sampleInputButton, sampleC);
        content.add(inputCard);
        content.add(Box.createVerticalStrut(14));
        
        // Output area
        JPanel resultsCard = cardPanel();
        resultsCard.setLayout(new BorderLayout(8, 10));
        JLabel resultsTitle = sectionTitle("Simulation results");
        resultsCard.add(resultsTitle, BorderLayout.NORTH);
        JPanel metrics = new JPanel(new GridLayout(1, 2, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metricPanel("Total head movement", totalMovementValue, "ms"));
        metrics.add(metricPanel("Average seek distance", averageSeekValue, "ms / request"));
        JPanel resultsContent = new JPanel();
        resultsContent.setOpaque(false);
        resultsContent.setLayout(new BoxLayout(resultsContent, BoxLayout.Y_AXIS));
        resultsContent.add(metrics);
        resultsContent.add(Box.createVerticalStrut(14));
        JPanel sequencePanel = new JPanel(new BorderLayout(4, 4));
        sequencePanel.setOpaque(false);
        JLabel sequenceTitle = new JLabel("Order of service (seek sequence)");
        sequenceTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        sequenceValue.setForeground(new Color(65, 62, 52));
        sequenceValue.setFont(new Font("Monospaced", Font.PLAIN, 13));
        sequenceValue.setLineWrap(true);
        sequenceValue.setWrapStyleWord(true);
        sequenceValue.setEditable(false);
        sequenceValue.setFocusable(false);
        sequenceValue.setOpaque(false);
        sequenceValue.setBorder(new EmptyBorder(3, 0, 3, 0));
        JScrollPane sequenceScroll = new JScrollPane(sequenceValue,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sequenceScroll.setBorder(null);
        sequenceScroll.setPreferredSize(new Dimension(400, 72));
        sequencePanel.add(sequenceTitle, BorderLayout.NORTH);
        sequencePanel.add(sequenceScroll, BorderLayout.CENTER);
        resultsContent.add(sequencePanel);
        resultsContent.add(Box.createVerticalStrut(12));
        JPanel movementPanel = new JPanel(new BorderLayout(4, 4));
        movementPanel.setOpaque(false);
        JLabel movementTitle = new JLabel("Head movement for each step");
        movementTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        stepMovementValue.setForeground(new Color(65, 62, 52));
        stepMovementValue.setFont(new Font("Monospaced", Font.PLAIN, 13));
        stepMovementValue.setEditable(false);
        stepMovementValue.setFocusable(false);
        stepMovementValue.setLineWrap(true);
        stepMovementValue.setWrapStyleWord(true);
        stepMovementValue.setOpaque(false);
        stepMovementValue.setBorder(new EmptyBorder(3, 0, 3, 0));
        JScrollPane movementScroll = new JScrollPane(stepMovementValue,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        movementScroll.setBorder(null);
        movementScroll.setPreferredSize(new Dimension(400, 105));
        movementPanel.add(movementTitle, BorderLayout.NORTH);
        movementPanel.add(movementScroll, BorderLayout.CENTER);
        resultsContent.add(movementPanel);
        resultsCard.add(resultsContent, BorderLayout.CENTER);
        content.add(resultsCard);
        content.add(Box.createVerticalStrut(14));
        
        // Graph area
        JPanel chartCard = cardPanel();
        chartCard.setLayout(new BorderLayout(8, 8));
        chartCard.add(sectionTitle("Graph of the head's path"), BorderLayout.NORTH);
        chart.setPreferredSize(new Dimension(620, 390));
        chart.setMinimumSize(new Dimension(280, 320));
        chartCard.add(chart, BorderLayout.CENTER);
        content.add(chartCard);
        diskSizeField.setToolTipText("Enter a disk size from 1 to 200");
        diskSizeField.setText("");
        diskSizeField.putClientProperty("JTextField.placeholderText", "ex. 200");
        headField.putClientProperty("JTextField.placeholderText", "ex. 67");
        requestsField.putClientProperty("JTextArea.placeholderText", "ex. 99, 102, 75, 21, 50");
        
        // Functions
        installLiveValidation();
        simulateButton.addActionListener(e -> simulate());
        resetButton.addActionListener(e -> resetForm());
        sampleInputButton.addActionListener(e -> generateSampleInput());
        validateAll();
    }

    
    
// NAVIGATION BUTTONS

    // Back and Exit buttons
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 240, 250));
        panel.setBorder(new EmptyBorder(7, 12, 7, 12));
        JButton backButton = new JButton("Go Back to Main Menu");
        JButton exitButton = new JButton("Exit");
        backButton.addActionListener(e -> {
        	 this.dispose();
        	 new MainMenu().setVisible(true);
        });
        exitButton.addActionListener(e -> System.exit(0));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(exitButton);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    
    
// HELPER METHODS FOR UI

    // Input errors
    private static JLabel errorLabel() {
        JLabel label = new JLabel(" ");
        label.setForeground(new Color(190, 45, 45));
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return label;
    }
    
    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 255, 252));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(232, 222, 190), 1, true),
                new EmptyBorder(15, 16, 15, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }
    
    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(new Color(43, 42, 37));
        return label;
    }
    
    private JPanel metricPanel(String title, JLabel value, String unit) {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(new Color(255, 248, 218));
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(title);
        name.setForeground(new Color(93, 83, 55));
        name.setFont(new Font("SansSerif", Font.PLAIN, 12));
        value.setFont(new Font("SansSerif", Font.BOLD, 25));
        value.setForeground(new Color(145, 105, 8));
        JLabel units = new JLabel(unit);
        units.setForeground(new Color(112, 101, 74));
        units.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(name);
        panel.add(Box.createVerticalStrut(5));
        panel.add(value);
        panel.add(units);
        return panel;
    }
    
    private void addInputRow(JPanel panel, GridBagConstraints base, int row,
                             String labelText, JComponent field, String note, JLabel error) {
        GridBagConstraints a = (GridBagConstraints)base.clone();
        a.gridx = 0; a.gridy = row; a.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(label, a);
        GridBagConstraints b = (GridBagConstraints)base.clone();
        b.gridx = 1; b.gridy = row; b.gridwidth = 2; b.weightx = 0.65;
        if (field instanceof JTextField) {
            field.setFont(new Font("SansSerif", Font.PLAIN, 14));
            field.setBorder(new CompoundBorder(new LineBorder(new Color(210, 195, 145), 1, true),
                    new EmptyBorder(8, 9, 8, 9)));
        }
        JPanel fieldStack = new JPanel();
        fieldStack.setOpaque(false);
        fieldStack.setLayout(new BoxLayout(fieldStack, BoxLayout.Y_AXIS));
        fieldStack.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldStack.add(field);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setForeground(new Color(105, 99, 82));
        noteLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldStack.add(Box.createVerticalStrut(3));
        fieldStack.add(noteLabel);
        fieldStack.add(error);
        panel.add(fieldStack, b);
    }

    
    
// INPUT VALIDATION

    // Checks the inputs whenever the user types or deletes something
    private void installLiveValidation() {
        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateAll(); }
            public void removeUpdate(DocumentEvent e) { validateAll(); }
            public void changedUpdate(DocumentEvent e) { validateAll(); }
        };
        diskSizeField.getDocument().addDocumentListener(listener);
        headField.getDocument().addDocumentListener(listener);
        requestsField.getDocument().addDocumentListener(listener);
    }
    
    // Real time validation of all inputs
    private void validateAll() {
        String diskSizeText = diskSizeField.getText().trim();
        String headText = headField.getText().trim();
        String reqText = requestsField.getText().trim();
        Integer diskSize = parseInteger(diskSizeText);
        
        // Disk Size
        boolean maxValid = false;
        if (diskSizeText.isEmpty()) setError(maxError, "Required.");
        else if (diskSize == null) setError(maxError, "Enter a whole number.");
        else if (diskSize < 1 || diskSize > MAX_DISK_SIZE)
            setError(maxError, "Disk size must be from 1 to 200.");
        else { setError(maxError, " "); maxValid = true; }
        
        // Head Position
        Integer head = parseInteger(headText);
        boolean headValid = false;
        if (headText.isEmpty()) setError(headError, "Required.");
        else if (head == null) setError(headError, "Enter a whole number.");
        else if (head < 0) setError(headError, "Head position cannot be below 0.");
        else if (!maxValid) setError(headError, "Enter a valid disk size first.");
        else if (head >= diskSize) setError(headError, "Head position cannot exceed " + (diskSize - 1) + ".");
        else { setError(headError, " "); headValid = true; }
        
        // Disk Requests
        RequestParse parsed = parseRequests(reqText);
        requestCountLabel.setText(parsed.count + " / 100 requests");
        boolean requestsValid = false;
        if (reqText.isEmpty()) setError(requestsError, "Enter at least one disk request.");
        else if (parsed.error != null) setError(requestsError, parsed.error);
        else if (parsed.values.size() < 1 || parsed.values.size() > MAX_REQUESTS)
            setError(requestsError, "Enter between 1 and 100 disk requests.");
        else if (!maxValid) setError(requestsError, "Enter a valid disk size first.");
        else {
            int invalid = -1;
            for (int value : parsed.values) {
                if (value < 0 || value >= diskSize) { invalid = value; break; }
            }
            if (invalid >= 0) setError(requestsError,
                    "Cylinder " + invalid + " is outside the range 0 to " + (diskSize - 1) + ".");
            else { setError(requestsError, " "); requestsValid = true; }
        }
        
        // Check if all inputs are valid to enable the simulate button
        boolean canSimulate = maxValid && headValid && requestsValid;
        simulateButton.setEnabled(canSimulate);
        simulateButton.setForeground(canSimulate ? Color.BLACK : Color.WHITE);
    }
    
    private void setError(JLabel label, String message) {
        label.setText(message);
    }

    
    
// RESET AND GENERATE SAMPLE INPUT

    // Clears input fields and results
    private void resetForm() {
        diskSizeField.setText("");
        headField.setText("");
        requestsField.setText("");
        totalMovementValue.setText("--");
        averageSeekValue.setText("--");
        sequenceValue.setText("Enter valid inputs, then click Simulate.");
        stepMovementValue.setText("Movement for each step will appear here.");
        chart.clearSimulation();
        validateAll();
    }
    
    // Generates sample input
    private void generateSampleInput() {
        diskSizeField.setText("200");
        headField.setText("67");
        requestsField.setText("45, 21, 167, 58, 158, 43, 64, 26, 97, 86, 125, 185, 18, 137, 106, 19, 56, 75, 19, 149");
        directionCombo.setSelectedItem("Upwards");
        totalMovementValue.setText("--");
        averageSeekValue.setText("--");
        sequenceValue.setText("Enter valid inputs, then click Simulate.");
        stepMovementValue.setText("Movement for each step will appear here.");
        chart.clearSimulation();
        validateAll();
    }

    
    
// READING AND CHECKING INPUT VALUES
    
    // Converts text to integer
    private Integer parseInteger(String value) {
        if (value.isEmpty() || !value.matches("\\d+")) return null;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ex) { return null; }
    }
    
    // Holds the parsed numbers, error message, and request count
    private static class RequestParse {
        List<Integer> values = new ArrayList<>();
        String error;
        int count;
    }
    
    // Stores requests in a list
    private RequestParse parseRequests(String text) {
        RequestParse result = new RequestParse();
        if (text.trim().isEmpty()) return result;
        
        // Removes spaces and commas
        String[] parts = text.trim().split("[\\s,]+", -1);
        for (String part : parts) {
            String token = part.trim();
            if (token.isEmpty()) continue;
            
            // Checks if valid integer
            if (!token.matches("\\d+")) {
                result.error = "Invalid format. Use whole cylinder numbers separated by spaces or commas.";
                result.count = result.values.size();
                return result;
            }
            try {
                result.values.add(Integer.parseInt(token));
            } catch (NumberFormatException ex) {
                result.error = "One of the cylinder numbers is too large.";
                result.count = result.values.size();
                return result;
            }
        }
        
        // Checks maximum amount of requests
        result.count = result.values.size();
        if (result.count > MAX_REQUESTS)
            result.error = "Too many requests. The maximum amount is 100.";
        return result;
    }

    
    
// C-LOOK ALGORITHM

    // Creates the order of requests and calculates the head movement
    private void simulate() {
        Integer diskSize = parseInteger(diskSizeField.getText().trim());
        Integer initialHead = parseInteger(headField.getText().trim());
        RequestParse parsed = parseRequests(requestsField.getText().trim());
        if (diskSize == null || initialHead == null
                || parsed.error != null || parsed.values.isEmpty()) {
            return;
        }
        
        // Separate requests based on the initial head position
        int head = initialHead;
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int request : parsed.values) {
            if (request < head) {
                left.add(request);
            } else if (request > head) {
                right.add(request);
            } else {
                right.add(request);
            }
        }
        
        // Sort the requests then create the order of requests
        Collections.sort(left);
        Collections.sort(right);
        List<Integer> order = new ArrayList<>();
        order.add(head);
        if ("Upwards".equals(directionCombo.getSelectedItem())) {
            order.addAll(right);
            order.addAll(left);
        } else {
            Collections.sort(left, Collections.reverseOrder());
            Collections.sort(right, Collections.reverseOrder());
            order.addAll(left);
            order.addAll(right);
        }
        
        // Calculate total head movement
        long movement = 0;
        for (int i = 1; i < order.size(); i++) {
            movement += Math.abs(order.get(i) - order.get(i - 1));
        }
        // Calculate average seek distance
        double average = (double) movement / parsed.values.size();
        totalMovementValue.setText(String.format("%.2f", (double) movement));
        averageSeekValue.setText(String.format("%.2f", average));
        // Display order of serivce
        sequenceValue.setText(join(order, "  →  "));
        
        // Head movement for each step
        StringBuilder stepDetails = new StringBuilder();
        for (int i = 1; i < order.size(); i++) {
            int from = order.get(i - 1);
            int to = order.get(i);
            int distance = Math.abs(to - from);
            stepDetails.append(String.format("Step %d: %d → %d = %d ms%n", i, from, to, distance));
        }
        stepDetails.append(String.format("Total movement: %d ms", movement));
        stepMovementValue.setText(stepDetails.toString());
        
        // Send data to graph
        chart.setSimulation(order, diskSize - 1);
    }

    
    
// JOIN METHOD for displaying order of serivce
    
    private String join(List<Integer> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) builder.append(separator);
            builder.append(values.get(i));
        }
        return builder.toString();
    }


    
// ALLOW SCROLLABLE

    // Allows scroll if content is taller than the window
    private static class ScrollablePage extends JPanel implements Scrollable {
        ScrollablePage(LayoutManager layout) {
            super(layout);
        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }


    
// GRAPH

    // Draws the graph using the seek sequence
    private class DiskChart extends JPanel {
        private List<Integer> sequence = new ArrayList<>();
        private int maxCylinder = 199;
        DiskChart() {
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(8, 5, 5, 5));
        }
        void setSimulation(List<Integer> order, int max) {
            sequence = new ArrayList<>(order);
            maxCylinder = max;
            repaint();
        }
        void clearSimulation() {
            sequence.clear();
            maxCylinder = 199;
            repaint();
        }
        
        // Draws the graph lines, points, and cylinder labels
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int left = 55, right = 22, top = 48, bottom = 25;
            int plotW = Math.max(1, w - left - right);
            int plotH = Math.max(1, h - top - bottom);
            g2.setColor(new Color(239, 230, 196));         
            for (int i = 0; i <= 5; i++) {
                int y = top + i * plotH / 5;
                g2.drawLine(left, y, left + plotW, y);
            }
            g2.setColor(new Color(105, 99, 82));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("Cylinder number", left + (plotW - g2.getFontMetrics().stringWidth("Cylinder number")) / 2, 17);
            for (int i = 0; i <= 5; i++) {
                int value = (int)Math.round((double)maxCylinder * i / 5);
                int x = left + i * plotW / 5;
                String tick = String.valueOf(value);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(tick, x - fm.stringWidth(tick) / 2, top - 10);
            }
            int n = sequence.size();
            int[] xs = new int[n];
            int[] ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = left + (int)Math.round((double)sequence.get(i) / Math.max(1, maxCylinder) * plotW);
                ys[i] = top + (n == 1 ? plotH / 2 : (int)((long)i * plotH / (n - 1)));
            }
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(new Color(205, 151, 20));
            for (int i = 1; i < n; i++) g2.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
            for (int i = 0; i < n; i++) {
                g2.setColor(new Color(205, 151, 20));
                g2.fill(new Ellipse2D.Double(xs[i] - 4, ys[i] - 4, 8, 8));
                g2.setColor(new Color(48, 46, 39));
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String label = String.valueOf(sequence.get(i));
                FontMetrics fm = g2.getFontMetrics();
                int labelX = xs[i] - fm.stringWidth(label) / 2;
                int labelY = ys[i] - 9;
                if (labelY < top + 10) labelY = ys[i] + 17;
                g2.drawString(label, labelX, labelY);
            }
            g2.dispose();
        }
        
    }


    
// MAIN METHOD

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            UIManager.put("TextField.selectionBackground", new Color(255, 226, 125));
            UIManager.put("TextField.selectionForeground", new Color(38, 38, 34));
            UIManager.put("TextArea.selectionBackground", new Color(255, 226, 125));
            UIManager.put("TextArea.selectionForeground", new Color(38, 38, 34));
            new CLook().setVisible(true);
        });
    }
}
