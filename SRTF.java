

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.DefaultCellEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

public class SRTF extends JFrame {
    private final JFrame mainMenu;

    // ================================================================
    // 1. VARIABLES USED BY THE PROGRAM
    // ================================================================

    // Colors used in the interface

    private static final Color DARK_GREEN = new Color(30, 75, 66);
    private static final Color LIGHT_GREEN = new Color(226, 239, 233);

    // Input section: process count and input table

    private JSpinner processCountSpinner;
    private JTable inputTable;
    private DefaultTableModel inputModel;

    // Output section: results table

    private JTable resultTable;
    private DefaultTableModel resultModel;

    // Labels that display totals, averages, and performance
    private JLabel totalLabel;
    private JLabel averageLabel;
    private JLabel performanceLabel;

    // Other interface variables: zoom, queue, and Gantt Chart

    private double zoomLevel = 1.0;
    private final Map<Component, Font> originalFonts =
            new IdentityHashMap<Component, Font>();
    private JTextArea queueArea;
    private GanttPanel ganttPanel;

    // ================================================================
    // 2. PROCESS AND GANTT CHART DATA
    // ================================================================

    // Stores the input and calculated values for one process

    // Stores the details and computed times of each process
    private static class Process {
        String id;             // Process name, such as P1 or P2
        int arrivalTime;       // Time when the process enters the ready queue
        int burstTime;         // Total CPU time needed by the process
        int remainingTime;     // CPU time the process still needs
        int completionTime;    // Time when the process finishes
        int turnaroundTime;    // Completion Time - Arrival Time
        int waitingTime;       // Turnaround Time - Burst Time
        int responseTime = -1; // First time the process gets the CPU
        int inputOrder;        // Original row position in the input table

        Process(String id, int arrivalTime, int burstTime, int inputOrder) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            this.inputOrder = inputOrder;
        }
    }

    // Stores one block in the Gantt Chart and its start/end time
    private static class Segment {
        String processID; // Process shown in this Gantt Chart block
        int startTime;    // Start of this block
        int endTime;      // End of this block

        Segment(String processID, int startTime, int endTime) {
            this.processID = processID;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // ================================================================
    // 3. WINDOW SETUP
    // ================================================================

    public SRTF() {
        this(null);
    }

    public SRTF(JFrame mainMenu) {
        this.mainMenu = mainMenu;
        setTitle("CPU Scheduling - Shortest Remaining Time First");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(createNavigationPanel(), BorderLayout.NORTH);
        topPanel.add(createHeader(), BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        generateInputRows();
        captureOriginalFonts(getContentPane());
        setupZoomShortcuts();
    }

    // ================================================================
    // 4. UI SECTION - NAVIGATION, HEADER AND MAIN LAYOUT
    // ================================================================

    // Creates the Go Back to Main Menu and Exit buttons
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 240, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));

        JButton backButton = new JButton("Go Back to Main Menu");
        JButton exitButton = new JButton("Exit");

        backButton.addActionListener(e -> {
            this.dispose();
            new MainMenu().setVisible(true);
        });

        exitButton.addActionListener(e -> System.exit(0));

        JPanel actions = new JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(exitButton);

        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("SHORTEST REMAINING TIME FIRST (SRTF)");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel("CPU Scheduling Algorithm Simulation");
        subtitle.setForeground(new Color(225, 239, 233));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    // Creates the main area and places the input and output sections

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(12, 15, 15, 15));

        main.add(createInputPanel(), BorderLayout.NORTH);
        main.add(createOutputTabs(), BorderLayout.CENTER);
        return main;
    }

    // Builds the process input table and its buttons

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 198)),
                "PROCESS INPUT (1-20 PROCESSES)"));

        JPanel controls = new JPanel();
        controls.setBackground(Color.WHITE);

        controls.add(new JLabel("Number of Processes:"));

        processCountSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 20, 1));
        processCountSpinner.setPreferredSize(new Dimension(65, 27));
        controls.add(processCountSpinner);

        JButton generateButton = new JButton("Generate Input Rows");
        generateButton.addActionListener(e -> generateInputRows());
        controls.add(generateButton);

        JButton sampleButton = new JButton("Load Sample Data");
        sampleButton.addActionListener(e -> loadSampleData());
        controls.add(sampleButton);

        JButton calculateButton = new JButton("Calculate SRTF");
        calculateButton.setBackground(DARK_GREEN);
        calculateButton.setForeground(Color.WHITE);
        calculateButton.addActionListener(e -> calculateScheduling());
        controls.add(calculateButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetApplication());
        controls.add(resetButton);

        panel.add(controls, BorderLayout.NORTH);

        inputModel = new DefaultTableModel(
                new Object[]{"Process ID", "Arrival Time (AT)", "Burst Time (BT)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(25);
        inputTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JTextField numberEditorField = new JTextField();
        DefaultCellEditor numberEditor = new DefaultCellEditor(numberEditorField) {
            @Override
            public Component getTableCellEditorComponent(
                    JTable table, Object value, boolean isSelected,
                    int row, int column) {
                Component editor = super.getTableCellEditorComponent(
                        table, value, isSelected, row, column);
                SwingUtilities.invokeLater(numberEditorField::selectAll);
                return editor;
            }
        };
        numberEditor.setClickCountToStart(1);
        inputTable.getColumnModel().getColumn(1).setCellEditor(numberEditor);
        inputTable.getColumnModel().getColumn(2).setCellEditor(numberEditor);

        JScrollPane inputScroll = new JScrollPane(inputTable);
        inputScroll.setPreferredSize(new Dimension(700, 285));
        panel.add(inputScroll, BorderLayout.CENTER);

        return panel;
    }

    // Builds the tabs for results, Gantt Chart, and process queue

    // Creates the results table, Gantt Chart area, and process queue area
    private JTabbedPane createOutputTabs() {
        JTabbedPane tabs = new JTabbedPane();

        resultModel = new DefaultTableModel(
                new Object[]{"Process ID", "Arrival Time (AT)", "Burst Time (BT)",
                        "Completion Time (CT)", "Turnaround Time (TAT)",
                        "Waiting Time (WT)", "Response Time (RT)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(resultModel);
        resultTable.setRowHeight(25);
        resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        totalLabel = new JLabel("TOTAL Completion Time (CT): -     "
                + "TOTAL Turnaround Time (TAT): -     "
                + "TOTAL Waiting Time (WT): -");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 13));

        averageLabel = new JLabel("AVERAGE Completion Time (CT): -     "
                + "AVERAGE Turnaround Time (TAT): -     "
                + "AVERAGE Waiting Time (WT): -");
        averageLabel.setFont(new Font("Arial", Font.BOLD, 13));

        performanceLabel = new JLabel("AVERAGE Response Time (RT): -     CPU Utilization: -     Throughput: -");
        performanceLabel.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        summaryPanel.add(totalLabel);
        summaryPanel.add(averageLabel);
        summaryPanel.add(performanceLabel);

        JPanel resultsPanel = new JPanel(new BorderLayout(5, 5));
        resultsPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultsPanel.add(summaryPanel, BorderLayout.SOUTH);

        ganttPanel = new GanttPanel();
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.getViewport().setBackground(Color.WHITE);

        queueArea = new JTextArea();
        queueArea.setEditable(false);
        queueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        queueArea.setLineWrap(false);
        JScrollPane queueScroll = new JScrollPane(queueArea);

        tabs.addTab("Process Results", resultsPanel);
        tabs.addTab("Gantt Chart", ganttScroll);
        tabs.addTab("Process Queue / Pool", queueScroll);

        return tabs;
    }

    // ================================================================
    // 5. INPUT BUTTON FUNCTIONS
    // ================================================================

    // Creates the blank rows based on the selected process count
    private void generateInputRows() {
        if (inputTable != null && inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        inputModel.setRowCount(0);
        int count = (Integer) processCountSpinner.getValue();

        for (int i = 1; i <= count; i++) {
            inputModel.addRow(new Object[]{"P" + i, 0, 1});
        }
    }

    // Loads the provided sample process data into the input table
    private void loadSampleData() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        Object[][] sample = {
            {"P1", 13, 10},
            {"P2", 6, 7},
            {"P5", 1, 8},
            {"P7", 23, 18},
            {"P3", 3, 12},
            {"P6", 38, 5},
            {"P4", 34, 17}
        };

        processCountSpinner.setValue(sample.length);
        inputModel.setRowCount(0);

        for (Object[] process : sample) {
            inputModel.addRow(process);
        }

        resultModel.setRowCount(0);

        totalLabel.setText("TOTAL Completion Time (CT): -     "
                + "TOTAL Turnaround Time (TAT): -     "
                + "TOTAL Waiting Time (WT): -");

        averageLabel.setText("AVERAGE Completion Time (CT): -     "
                + "AVERAGE Turnaround Time (TAT): -     "
                + "AVERAGE Waiting Time (WT): -");

        performanceLabel.setText("AVERAGE Response Time (RT): -     CPU Utilization: -     Throughput: -");
        queueArea.setText("");
        ganttPanel.setSegments(new ArrayList<Segment>());
    }

    // Clears the previous results and returns the input table to its defaults
    private void resetApplication() {
        generateInputRows();
        resultModel.setRowCount(0);
        totalLabel.setText("TOTAL Completion Time (CT): -     "
                + "TOTAL Turnaround Time (TAT): -     "
                + "TOTAL Waiting Time (WT): -");
        averageLabel.setText("AVERAGE Completion Time (CT): -     "
                + "AVERAGE Turnaround Time (TAT): -     "
                + "AVERAGE Waiting Time (WT): -");
        performanceLabel.setText("AVERAGE Response Time (RT): -     CPU Utilization: -     Throughput: -");
        queueArea.setText("");
        ganttPanel.setSegments(new ArrayList<Segment>());
    }

    // ================================================================
    // 6. CHECK INPUT AND START THE SRTF CALCULATION
    // ================================================================

    private void calculateScheduling() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        int count = inputModel.getRowCount();

        if (count < 1 || count > 20) {
            JOptionPane.showMessageDialog(this,
                    "Please enter between 1 and 20 processes.",
                    "Invalid Process Count", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Read the values from the table and check if they are valid
        List<Process> processes = new ArrayList<Process>();
        Set<String> usedIDs = new HashSet<String>();

        try {
            for (int i = 0; i < count; i++) {
                String id = String.valueOf(inputModel.getValueAt(i, 0)).trim();

                if (id.isEmpty()) {
                    id = "P" + (i + 1);
                }

                if (!usedIDs.add(id.toLowerCase())) {
                    throw new IllegalArgumentException("Duplicate Process ID: " + id);
                }

                int arrival = Integer.parseInt(
                        String.valueOf(inputModel.getValueAt(i, 1)).trim());
                int burst = Integer.parseInt(
                        String.valueOf(inputModel.getValueAt(i, 2)).trim());

                if (arrival < 0) {
                    throw new IllegalArgumentException(
                            "Arrival Time cannot be negative. Check " + id + ".");
                }

                if (burst <= 0) {
                    throw new IllegalArgumentException(
                            "Burst Time must be greater than zero. Check " + id + ".");
                }

                processes.add(new Process(id, arrival, burst, i));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Arrival Time and Burst Time must be whole numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // These lists will hold the Gantt Chart blocks and queue history
        List<Segment> gantt = new ArrayList<Segment>();
        List<String> queueLog = new ArrayList<String>();

        executeSRTF(processes, gantt, queueLog);
        ganttPanel.setSegments(gantt);
        displayResults(processes);

        StringBuilder log = new StringBuilder();
        for (String entry : queueLog) {
            log.append(entry).append("\n");
            log.append("--------------------------------------------------\n");
        }
        queueArea.setText(log.toString());
        queueArea.setCaretPosition(0);
    }

    // ================================================================
    // 7. SRTF ALGORITHM
    // ================================================================

    // At every time unit, choose the arrived process with the shortest
    // remaining burst time. This is preemptive, so the choice is checked again.
    private void executeSRTF(List<Process> processes,
                             List<Segment> gantt,
                             List<String> queueLog) {
        // Step 1: Sort processes by arrival time, like the reference code
        List<Process> sorted = new ArrayList<Process>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0;
        int completed = 0;
        int total = sorted.size();

        // Continue until every process has finished
        while (completed < total) {
            Process running = null;
            List<Process> readyQueue = new ArrayList<Process>();

            // Step 2: Find the arrived process with the shortest remaining time
            for (Process p : sorted) {
                if (p.arrivalTime <= time && p.remainingTime > 0 &&
                        (running == null ||
                         p.remainingTime < running.remainingTime)) {
                    running = p;
                }

                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    readyQueue.add(p);
                }
            }

            // Step 3: If no process is ready, move time forward while CPU is idle
            if (running == null) {
                queueLog.add("TIME " + time + "\n  CPU IDLE - No process has arrived.");
                addSegment(gantt, "IDLE", time, time + 1);
                time++;
                continue;
            }

            // Step 4: Sort the queue only for displaying it on the screen
            // The running process was already selected using the reference logic
            readyQueue.sort(Comparator
                    .comparingInt((Process p) -> p.remainingTime)
                    .thenComparingInt(p -> p.arrivalTime)
                    .thenComparingInt(p -> p.inputOrder));

            // Step 5: Record response time the first time this process runs
            if (running.responseTime == -1) {
                running.responseTime = time - running.arrivalTime;
            }

            StringBuilder snapshot = new StringBuilder("TIME " + time + "\n");
            for (int i = 0; i < readyQueue.size(); i++) {
                Process p = readyQueue.get(i);
                snapshot.append("  ").append(p.id)
                        .append(" [Remaining: ").append(p.remainingTime)
                        .append(", ").append(p == running ? "RUNNING" : "READY")
                        .append("]");
                if (i < readyQueue.size() - 1) snapshot.append("  ->");
                snapshot.append("\n");
            }
            queueLog.add(snapshot.toString());

            // Step 6: Run the selected process for one time unit
            addSegment(gantt, running.id, time, time + 1);
            running.remainingTime--;
            time++;

            // Step 7: If no time is left, calculate its final times
            if (running.remainingTime == 0) {
                running.completionTime = time;
                running.turnaroundTime = time - running.arrivalTime;
                running.waitingTime = running.turnaroundTime - running.burstTime;
                completed++;
            }
        }
    }

    // ================================================================
    // 8. GANTT CHART DATA
    // ================================================================

    // Combines consecutive time units of the same process into one block
    private void addSegment(List<Segment> gantt, String processID,
                            int start, int end) {
        if (!gantt.isEmpty()) {
            Segment last = gantt.get(gantt.size() - 1);

            if (last.processID.equals(processID) && last.endTime == start) {
                last.endTime = end;
                return;
            }
        }

        gantt.add(new Segment(processID, start, end));
    }

    // ================================================================
    // 9. DISPLAY RESULTS AND CALCULATE TOTALS / AVERAGES
    // ================================================================

    private void displayResults(List<Process> processes) {
        resultModel.setRowCount(0);

        // CT  = time when a process finishes
        // TAT = CT - AT
        // WT  = TAT - BT
        // RT  = first time the process runs - AT

        int totalCT = 0;
        int totalTAT = 0;
        int totalWT = 0;
        int totalRT = 0;
        int totalBurst = 0;

        for (Process p : processes) {
            resultModel.addRow(new Object[]{
                p.id,
                p.arrivalTime,
                p.burstTime,
                p.completionTime,
                p.turnaroundTime,
                p.waitingTime,
                p.responseTime
            });

            totalCT += p.completionTime;
            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
            totalBurst += p.burstTime;
        }

        resultModel.addRow(new Object[]{
            "TOTAL", "", "", totalCT + " ms", totalTAT + " ms", totalWT + " ms", totalRT + " ms"
        });

        int n = processes.size();
        totalLabel.setText(String.format(
                "TOTAL Completion Time (CT): %d ms     "
                + "TOTAL Turnaround Time (TAT): %d ms     "
                + "TOTAL Waiting Time (WT): %d ms",
                totalCT, totalTAT, totalWT));

        averageLabel.setText(String.format(
                "AVERAGE Completion Time (CT): %.2f ms     "
                + "AVERAGE Turnaround Time (TAT): %.2f ms     "
                + "AVERAGE Waiting Time (WT): %.2f ms",
                (double) totalCT / n,
                (double) totalTAT / n,
                (double) totalWT / n));

        int elapsedTime = 0;
        if (!ganttPanel.segments.isEmpty()) {
            elapsedTime = ganttPanel.segments.get(ganttPanel.segments.size() - 1).endTime;
        }
        double cpuUtilization = elapsedTime == 0 ? 0.0 : (totalBurst * 100.0) / elapsedTime;
        double throughput = elapsedTime == 0 ? 0.0 : (double) n / elapsedTime;
        performanceLabel.setText(String.format(
                "AVERAGE Response Time (RT): %.2f ms     CPU Utilization: %.2f%%     Throughput: %.4f processes/ms",
                (double) totalRT / n, cpuUtilization, throughput));
    }

    // ================================================================
    // 10. ZOOM FUNCTIONS AND KEYBOARD SHORTCUTS
    // ================================================================

    // Saves the original font sizes so the zoom can be reset later
    private void captureOriginalFonts(Container container) {
        for (Component component : container.getComponents()) {
            if (component.getFont() != null) {
                originalFonts.put(component, component.getFont());
            }
            if (component instanceof Container) {
                captureOriginalFonts((Container) component);
            }
        }
    }

    private void zoomIn() {
        if (zoomLevel < 2.0) {
            zoomLevel = Math.min(2.0, zoomLevel + 0.1);
            applyZoom();
        }
    }

    private void zoomOut() {
        if (zoomLevel > 0.7) {
            zoomLevel = Math.max(0.7, zoomLevel - 0.1);
            applyZoom();
        }
    }

    private void resetZoom() {
        zoomLevel = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        for (Map.Entry<Component, Font> entry : originalFonts.entrySet()) {
            Component component = entry.getKey();
            Font original = entry.getValue();
            component.setFont(original.deriveFont(
                    Math.max(8.0f, (float) (original.getSize2D() * zoomLevel))));
        }

        if (inputTable != null) {
            inputTable.setRowHeight(Math.max(18, (int) Math.round(25 * zoomLevel)));
        }
        if (resultTable != null) {
            resultTable.setRowHeight(Math.max(18, (int) Math.round(25 * zoomLevel)));
        }
        if (ganttPanel != null) {
            ganttPanel.setZoomScale(zoomLevel);
        }
        revalidate();
        repaint();
    }

    // Sets the keyboard shortcuts for zooming in, out, and resetting
    private void setupZoomShortcuts() {
        javax.swing.JRootPane root = getRootPane();
        int condition = javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

        root.getInputMap(condition).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK),
                "zoomIn");
        root.getInputMap(condition).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "zoomIn");
        root.getInputMap(condition).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_DOWN_MASK),
                "zoomIn");
        root.getInputMap(condition).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK),
                "zoomOut");
        root.getInputMap(condition).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK),
                "resetZoom");

        root.getActionMap().put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zoomIn();
            }
        });
        root.getActionMap().put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                zoomOut();
            }
        });
        root.getActionMap().put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetZoom();
            }
        });
    }

    // ================================================================
    // 11. GANTT CHART UI - DRAWING THE CHART
    // ================================================================

    private static class GanttPanel extends JPanel {
        private List<Segment> segments = new ArrayList<Segment>();
        private double zoomScale = 1.0;

        GanttPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        }

        void setSegments(List<Segment> newSegments) {
            segments = new ArrayList<Segment>(newSegments);
            revalidate();
            repaint();
        }

        void setZoomScale(double scale) {
            zoomScale = scale;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            int width = 80;
            for (Segment s : segments) {
                width += Math.max(75, (s.endTime - s.startTime) * 35);
            }
            return new Dimension(
                    (int) Math.round(Math.max(800, width) * zoomScale),
                    (int) Math.round(190 * zoomScale));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.scale(zoomScale, zoomScale);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (segments.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Run the SRTF calculation to display the Gantt Chart.",
                        25, 45);
                g2.dispose();
                return;
            }

            int x = 30;
            int y = 55;
            int height = 55;

            Color[] colors = {
                new Color(211, 231, 220),
                new Color(210, 226, 245),
                new Color(248, 226, 193),
                new Color(235, 216, 239),
                new Color(247, 216, 216),
                new Color(218, 231, 239)
            };

            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("CPU EXECUTION ORDER", 30, 25);

            for (int i = 0; i < segments.size(); i++) {
                Segment s = segments.get(i);
                int width = Math.max(75, (s.endTime - s.startTime) * 35);

                Color fill = s.processID.equals("IDLE")
                        ? new Color(225, 225, 225)
                        : colors[i % colors.length];

                g2.setColor(fill);
                g2.fillRoundRect(x, y, width, height, 8, 8);

                g2.setColor(new Color(90, 110, 100));
                g2.drawRoundRect(x, y, width, height, 8, 8);

                g2.setColor(Color.BLACK);
                String label = s.processID;
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (width - labelWidth) / 2, y + 32);

                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.drawString(String.valueOf(s.startTime), x - 3, y + height + 23);

                x += width;
                g2.setFont(new Font("Arial", Font.BOLD, 13));
            }

            Segment last = segments.get(segments.size() - 1);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString(String.valueOf(last.endTime), x - 3, y + height + 23);

            g2.dispose();
        }
    }

    // ================================================================
    // 12. MAIN METHOD - START THE APPLICATION
    // ================================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SRTF app = new SRTF();
            app.setVisible(true);
        });
    }
}