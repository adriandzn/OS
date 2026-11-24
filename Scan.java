import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Scan extends JFrame {
    private final JFrame mainMenu;
    private PlaceholderTextArea reqQueueField;
    private JTextField headPosField, diskSizeField;
    private JComboBox<String> directionBox;
    private JTextArea sequenceArea, movementDetailsArea;
    private JLabel totalMovementLabel, avgSeekLabel;
    private GraphPanel graphPanel;
    private JTabbedPane tabbedPane;

    // To have a Blue-Green theme constraints for the UI DESIGN
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // Code to implement a Teal/Mint Palette
    private final Color PRIMARY_COLOR = new Color(0, 150, 136);      // Teal
    private final Color PANEL_BG = new Color(242, 250, 249);         // Very light mint for panels
    private final Color GRAPH_LINE_COLOR = new Color(38, 166, 154);  // Medium Teal
    private final Color GRAPH_NODE_COLOR = new Color(0, 105, 92);    // Dark Teal for contrast

    public Scan() {
        this(null);
    }

    public Scan(JFrame mainMenu) {
        this.mainMenu = mainMenu;
        // Set Nimbus Look and Feel for a modern UI
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            setUIFont(new javax.swing.plaf.FontUIResource(MAIN_FONT));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("SCAN Disk Scheduling Simulator - Blue-Green Theme");
        setSize(950, 750);
        setDefaultCloseOperation(mainMenu == null ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Code for the Input Panel with a Blue-Green Theme
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                " Required Inputs ", TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, PRIMARY_COLOR));
        inputPanel.setBackground(PANEL_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Row 1: Request queue with automatic line wrapping and placeholder
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        inputPanel.add(new JLabel("Request Queue (comma/space separated):"), gbc);

        reqQueueField = new PlaceholderTextArea("Enter disk requests (minimum 20, maximum 100)...", 3, 35);
        reqQueueField.setLineWrap(true);
        reqQueueField.setWrapStyleWord(true);
        reqQueueField.setFont(MAIN_FONT);
        JScrollPane requestQueueScroll = new JScrollPane(reqQueueField);
        requestQueueScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        requestQueueScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        requestQueueScroll.setPreferredSize(new Dimension(420, 65));
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(requestQueueScroll, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 2
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Initial Head Position:"), gbc);
        headPosField = new JTextField(10);
        gbc.gridx = 1;
        inputPanel.add(headPosField, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Disk Size (cylinders):"), gbc);
        diskSizeField = new JTextField(10);
        diskSizeField.setText("200");
        gbc.gridx = 3;
        inputPanel.add(diskSizeField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Initial Direction:"), gbc);
        directionBox = new JComboBox<>(new String[]{"Up (Towards Max)", "Down (Towards 0)"});
        gbc.gridx = 1;
        inputPanel.add(directionBox, gbc);

        // Code for the Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton runBtn = new JButton("Run SCAN");
        runBtn.setBackground(PRIMARY_COLOR);
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(BOLD_FONT);
        runBtn.setFocusPainted(false);
        runBtn.setOpaque(true);
        runBtn.setContentAreaFilled(true);

        JButton sampleBtn = new JButton("Load Sample Data");
        JButton clearBtn = new JButton("Reset / Clear");

        buttonPanel.add(runBtn);
        buttonPanel.add(sampleBtn);
        buttonPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        inputPanel.add(buttonPanel, gbc);

        // Code for the Center Panel: Results & Tabs
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);

        // Code to have a sequence panel
        JPanel sequencePanel = new JPanel(new BorderLayout(5, 5));
        sequencePanel.setBackground(Color.WHITE);
        sequencePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                " SCAN Results ", TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, PRIMARY_COLOR));

        sequenceArea = new JTextArea(3, 20);
        sequenceArea.setEditable(false);
        sequenceArea.setLineWrap(true);
        sequenceArea.setWrapStyleWord(true);
        sequenceArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        sequenceArea.setBackground(PANEL_BG);
        sequencePanel.add(new JScrollPane(sequenceArea), BorderLayout.CENTER);

        // Code for the Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
        totalMovementLabel = new JLabel("Total Head Movement: --");
        totalMovementLabel.setFont(BOLD_FONT);
        totalMovementLabel.setForeground(GRAPH_NODE_COLOR);
        avgSeekLabel = new JLabel("Average Seek Distance: --");
        avgSeekLabel.setFont(BOLD_FONT);
        avgSeekLabel.setForeground(GRAPH_NODE_COLOR);
        statsPanel.add(totalMovementLabel);
        statsPanel.add(avgSeekLabel);
        sequencePanel.add(statsPanel, BorderLayout.SOUTH);

        centerPanel.add(sequencePanel, BorderLayout.NORTH);

        // Code for the Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(MAIN_FONT);

        movementDetailsArea = new JTextArea();
        movementDetailsArea.setEditable(false);
        movementDetailsArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        movementDetailsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        movementDetailsArea.setBackground(PANEL_BG);
        tabbedPane.addTab("Movement Details", new JScrollPane(movementDetailsArea));

        graphPanel = new GraphPanel();
        graphPanel.setBackground(Color.WHITE);
        tabbedPane.addTab("Head Path Graph", graphPanel);

        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        JPanel body = new JPanel(new BorderLayout(15, 15));
        body.setBackground(Color.WHITE);
        body.add(inputPanel, BorderLayout.NORTH);
        body.add(centerPanel, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(Color.WHITE);
        root.add(createNavigationPanel(), BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        // Event listeners for buttons
        runBtn.addActionListener(e -> runAlgorithm());
        clearBtn.addActionListener(e -> clearFields());
        sampleBtn.addActionListener(e -> {
            reqQueueField.setText("82, 170, 43, 140, 24, 16, 190, 105, 55, 67, 10, 115, 130, 99, 5, 45, 180, 75, 88, 150");
            headPosField.setText("50");
            diskSizeField.setText("200");
            directionBox.setSelectedIndex(0);
        });
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 240, 250));
        panel.setBorder(new EmptyBorder(7, 12, 7, 12));
        JButton backButton = new JButton("Go Back to Main Menu");
        JButton exitButton = new JButton("Exit");
        backButton.addActionListener(e -> {
            this.dispose();
            if (mainMenu != null) {
                mainMenu.setVisible(true);
            }
        });
        exitButton.addActionListener(e -> System.exit(0));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(exitButton);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) UIManager.put(key, f);
        }
    }

    private void runAlgorithm() {
        try {
            int diskSize = Integer.parseInt(diskSizeField.getText().trim());
            int headPos = Integer.parseInt(headPosField.getText().trim());
            String direction = (directionBox.getSelectedIndex() == 0) ? "right" : "left";

            if (diskSize <= 0) {
                JOptionPane.showMessageDialog(this, "Disk size must be greater than 0.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (headPos < 0 || headPos >= diskSize) {
                JOptionPane.showMessageDialog(this,
                    "Initial head position must be between 0 and " + (diskSize - 1) + ".",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String queueText = reqQueueField.getText().trim();
            if (queueText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter between 20 and 100 disk requests.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] reqStrings = queueText.split("[,\\s]+");
            List<Integer> requests = new ArrayList<>();
            for (String value : reqStrings) {
                if (!value.isEmpty()) {
                    int cylinder = Integer.parseInt(value);
                    if (cylinder < 0 || cylinder >= diskSize) {
                        JOptionPane.showMessageDialog(this,
                            "Request value " + cylinder + " is outside the valid disk range (0 to " + (diskSize - 1) + ").",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    requests.add(cylinder);
                }
            }

            if (requests.size() < 20 || requests.size() > 100) {
                JOptionPane.showMessageDialog(this,
                    "Please enter between 20 and 100 requests. You entered " + requests.size() + ".",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            calculateScan(requests, headPos, diskSize, direction);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric integers.",
                "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // GeeksforGeeks SCAN implementation
    private void calculateScan(List<Integer> requests, int initialHead, int diskSize, String direction) {
        int seek_count = 0;
        int distance, cur_track;
        int head = initialHead;

        Vector<Integer> left = new Vector<>();
        Vector<Integer> right = new Vector<>();
        Vector<Integer> seek_sequence = new Vector<>();

        seek_sequence.add(initialHead);

        // Appending end values which have to be visited before reversing the direction
        if (direction.equals("left")) {
            left.add(0);
        } else if (direction.equals("right")) {
            right.add(diskSize - 1);
        }

        for (int i = 0; i < requests.size(); i++) {
            int req = requests.get(i);
            if (req < head) {
                left.add(req);
            }
            if (req > head) {
                right.add(req);
            }
        }

        // Sorting left and right vectors
        Collections.sort(left);
        Collections.sort(right);

        // Run the while loop two times, one by one scanning right and left of the head
        int run = 2;
        while (run-- > 0) {
            if (direction.equals("left")) {
                for (int i = left.size() - 1; i >= 0; i--) {
                    cur_track = left.get(i);
                    seek_sequence.add(cur_track);
                    distance = Math.abs(cur_track - head);
                    seek_count += distance;
                    head = cur_track;
                }
                direction = "right";
            } else if (direction.equals("right")) {
                for (int i = 0; i < right.size(); i++) {
                    cur_track = right.get(i);
                    seek_sequence.add(cur_track);
                    distance = Math.abs(cur_track - head);
                    seek_count += distance;
                    head = cur_track;
                }
                direction = "left";
            }
        }

        updateUI(seek_sequence, seek_count, requests.size(), diskSize);
    }

    private void updateUI(List<Integer> sequence, int totalMovement, int originalReqCount, int diskSize) {
        StringBuilder seqStr = new StringBuilder("Service Order: ");
        StringBuilder details = new StringBuilder(String.format("%-10s | %-15s | %-15s | %-15s\n", "Step", "Start", "End", "Movement"));
        details.append("-".repeat(65)).append("\n");

        for (int i = 0; i < sequence.size(); i++) {
            seqStr.append(sequence.get(i));
            if (i < sequence.size() - 1) seqStr.append("  →  ");

            if (i > 0) {
                int start = sequence.get(i - 1);
                int end = sequence.get(i);
                int move = Math.abs(start - end);
                details.append(String.format("Step %-5d | %-15d | %-15d | %-15d\n", i, start, end, move));
            }
        }

        sequenceArea.setText(seqStr.toString());
        movementDetailsArea.setText(details.toString());

        totalMovementLabel.setText("Total Head Movement: " + totalMovement + " ms");
        double avg = originalReqCount == 0 ? 0 : (double) totalMovement / originalReqCount;
        avgSeekLabel.setText(String.format("Average Seek Distance: %.2f ms/request", avg));

        graphPanel.setGraphData(sequence, diskSize);
        tabbedPane.setSelectedIndex(1);
    }

    private void clearFields() {
        reqQueueField.setText("");
        headPosField.setText("");
        sequenceArea.setText("");
        movementDetailsArea.setText("");
        totalMovementLabel.setText("Total Head Movement: --");
        avgSeekLabel.setText("Average Seek Distance: --");
        graphPanel.setGraphData(null, 200);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Scan().setVisible(true);
        });
    }

    // Custom JTextArea that paints placeholder text when empty
    static class PlaceholderTextArea extends JTextArea {
        private final String placeholder;

        public PlaceholderTextArea(String placeholder, int rows, int columns) {
            super(rows, columns);
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, insets.left + 4, insets.top + fm.getAscent() + 2);
                g2.dispose();
            }
        }
    }

    // Code to achieve an enhanced graph panel
    class GraphPanel extends JPanel {
        private List<Integer> sequence;
        private int diskSize = 200;
        private final int PADDING_LEFT = 50;
        private final int PADDING_RIGHT = 30;
        private final int PADDING_TOP = 20;
        private final int PADDING_BOTTOM = 40;

        public void setGraphData(List<Integer> sequence, int diskSize) {
            this.sequence = sequence;
            this.diskSize = diskSize;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int graphWidth = width - PADDING_LEFT - PADDING_RIGHT;
            int graphHeight = height - PADDING_TOP - PADDING_BOTTOM;

            // Draw Grid Lines (Subtle Gray)
            g2.setColor(new Color(235, 240, 240));
            int gridLines = 10;
            for (int i = 0; i <= gridLines; i++) {
                int x = PADDING_LEFT + (i * graphWidth / gridLines);
                g2.drawLine(x, PADDING_TOP, x, height - PADDING_BOTTOM);
            }
            if (sequence != null && !sequence.isEmpty()) {
                for (int i = 0; i < sequence.size(); i++) {
                    int y = PADDING_TOP + (i * graphHeight / (sequence.size() - 1));
                    g2.drawLine(PADDING_LEFT, y, width - PADDING_RIGHT, y);
                }
            }

            // Code to Draw Axes
            g2.setColor(GRAPH_NODE_COLOR);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(PADDING_LEFT, PADDING_TOP, PADDING_LEFT, height - PADDING_BOTTOM);
            g2.drawLine(PADDING_LEFT, height - PADDING_BOTTOM, width - PADDING_RIGHT, height - PADDING_BOTTOM);

            // Code for Axis Labels
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(GRAPH_NODE_COLOR);
            g2.drawString("0", PADDING_LEFT - 5, height - PADDING_BOTTOM + 20);
            g2.drawString(String.valueOf(diskSize - 1), width - PADDING_RIGHT - 15, height - PADDING_BOTTOM + 20);

            g2.drawString("Cylinders", width / 2 - 25, height - PADDING_BOTTOM + 35);
            g2.drawString("Step", PADDING_LEFT - 35, PADDING_TOP - 5);

            if (sequence == null || sequence.isEmpty()) return;

            int steps = sequence.size();
            List<Point> points = new ArrayList<>();
            for (int i = 0; i < steps; i++) {
                int x = PADDING_LEFT + (int) ((double) sequence.get(i) / (diskSize - 1) * graphWidth);
                int y = PADDING_TOP + (int) ((double) i / (steps - 1) * graphHeight);
                points.add(new Point(x, y));
            }

            // Draw Lines and Nodes
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);

                // This is for the Line connecting to the previous point
                if (i > 0) {
                    Point prev = points.get(i - 1);
                    g2.setColor(GRAPH_LINE_COLOR);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawLine(prev.x, prev.y, p.x, p.y);
                }

                // This is for drawing the Data Node
                g2.setColor(GRAPH_NODE_COLOR);
                g2.fillOval(p.x - 6, p.y - 6, 12, 12);

                // This is for drawing Cylinder Label Text above the node
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));

                int labelX = p.x + 8;
                int labelY = p.y - 8;
                if (i > 0 && points.get(i - 1).x > p.x) {
                    labelX = p.x - 25;
                }
                g2.drawString(String.valueOf(sequence.get(i)), labelX, labelY);
            }
        }
    }
}