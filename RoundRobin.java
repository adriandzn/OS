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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

//Main Window 
public class RoundRobin extends JFrame {
    private final JFrame mainMenu;

    private static final Color PRIMARY_RED = new Color(160, 35, 45);
    private static final Color LIGHT_RED = new Color(248, 226, 228);
    private static final int BASE_PIXELS_PER_TIME_UNIT = 35;
    private static final int MAX_TIME_VALUE = 10000;

    //Track whether the current input is from the sample-data button
    private JSpinner processCountSpinner;
    private boolean sampleDataLoaded = false;
    private JSpinner quantumSpinner;
    private JTable inputTable;
    private DefaultTableModel inputModel;
    private JTable resultTable;
    private DefaultTableModel resultModel;
    private JLabel totalLabel;
    private JLabel averageLabel;
    private JLabel utilizationLabel;
    private JLabel throughputLabel;
    private JTextArea queueArea;
    private GanttPanel ganttPanel;

    private double zoomLevel = 1.0;
    private final Map<Component, Font> originalFonts =
            new IdentityHashMap<Component, Font>();

    //Stores the input values and calculated times for one process. 
    private static class Process {
        String id;
        int arrivalTime;
        int burstTime;
        int remainingTime;
        int completionTime;
        int turnaroundTime;
        int waitingTime;
        int responseTime;
        int firstStartTime;
        int inputOrder;

        Process(String id, int arrivalTime, int burstTime, int inputOrder) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            this.inputOrder = inputOrder;
            this.firstStartTime = -1;
            this.responseTime = -1;
        }
    }

    //Represents one continous block in the Gantt Chart
    private static class Segment {
        String processID;
        int startTime;
        int endTime;

        Segment(String processID, int startTime, int endTime) {
            this.processID = processID;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public RoundRobin() {
        this(null);
    }
    
    //Creates the window, input, and output panels. 
    public RoundRobin(JFrame mainMenu) {
        this.mainMenu = mainMenu;
        setTitle("CPU Scheduling - Round Robin");
        setDefaultCloseOperation(mainMenu == null ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
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

    //Creates the back and Exit Buttons for each page
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

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(exitButton);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    //Creates the title area
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_RED);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("ROUND ROBIN (RR) CPU SCHEDULING");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel("CPU Scheduling Algorithm Simulation");
        subtitle.setForeground(LIGHT_RED);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(12, 15, 15, 15));
        main.add(createInputPanel(), BorderLayout.NORTH);
        main.add(createOutputTabs(), BorderLayout.CENTER);
        return main;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(215, 190, 193)),
                "PROCESS INPUT (1-20 PROCESSES)"));

        JPanel controls = new JPanel();
        controls.setBackground(Color.WHITE);
        controls.add(new JLabel("Number of Processes:"));

        processCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 20, 1));
        processCountSpinner.setPreferredSize(new Dimension(65, 27));
        controls.add(processCountSpinner);

        controls.add(new JLabel("Time Quantum:"));
        quantumSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        quantumSpinner.setPreferredSize(new Dimension(65, 27));
        controls.add(quantumSpinner);

        JButton generateButton = new JButton("Generate Input Rows");
        generateButton.addActionListener(e -> generateInputRows());
        controls.add(generateButton);

        JButton sampleButton = new JButton("Load Sample Data");
        sampleButton.addActionListener(e -> loadSampleData());
        controls.add(sampleButton);

        JButton calculateButton = new JButton("Calculate Round Robin");
        calculateButton.setBackground(PRIMARY_RED);
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setOpaque(true);
        calculateButton.setFocusPainted(false);
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

    //Builds the result table, Gantt Chart, and ready-queue log tabs. 
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

        averageLabel = new JLabel("AVERAGE Turnaround Time (TAT): -     "
                + "AVERAGE Waiting Time (WT): -     "
                + "AVERAGE Response Time (RT): -");

        totalLabel.setFont(new Font("Arial", Font.BOLD, 13));
        averageLabel.setFont(new Font("Arial", Font.BOLD, 13));

        utilizationLabel = new JLabel("CPU UTILIZATION: -");
        throughputLabel = new JLabel("THROUGHPUT: -");
        utilizationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        throughputLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        summaryPanel.add(totalLabel);
        summaryPanel.add(averageLabel);
        summaryPanel.add(utilizationLabel);
        summaryPanel.add(throughputLabel);

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
        queueArea.setBackground(Color.WHITE);
        queueArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tabs.addTab("Process Results", resultsPanel);
        tabs.addTab("Gantt Chart", ganttScroll);
        tabs.addTab("Process Queue / Pool", new JScrollPane(queueArea));
        return tabs;
    }

    private void generateInputRows() {
        if (inputTable != null && inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        // Manual input requires at least 10 processes.
        sampleDataLoaded = false;
        processCountSpinner.setModel(
                new SpinnerNumberModel(10, 1, 20, 1));

        inputModel.setRowCount(0);
        int count = (Integer) processCountSpinner.getValue();
        for (int i = 1; i <= count; i++) {
            inputModel.addRow(new Object[]{"P" + i, 0, 1});
        }
    }

    private void loadSampleData() {
        // Sample data is allowed to contain only 5 processes.
        sampleDataLoaded = true;
        processCountSpinner.setModel(
                new SpinnerNumberModel(5, 5, 20, 1));
        quantumSpinner.setValue(2);

        inputModel.setRowCount(0);
        inputModel.addRow(new Object[]{"P1", 0, 5});
        inputModel.addRow(new Object[]{"P2", 1, 3});
        inputModel.addRow(new Object[]{"P3", 2, 1});
        inputModel.addRow(new Object[]{"P4", 3, 2});
        inputModel.addRow(new Object[]{"P5", 4, 3});

        clearOutputs();
    }

    //Restores the normal 10 process input
    private void resetApplication() {
        sampleDataLoaded = false;
        processCountSpinner.setModel(
                new SpinnerNumberModel(10, 1, 20, 1));
        quantumSpinner.setValue(2);
        generateInputRows();
        clearOutputs();
    }
    
    //Clears all calculated results
    private void clearOutputs() {
        resultModel.setRowCount(0);
        totalLabel.setText("TOTAL Completion Time (CT): -     "
                + "TOTAL Turnaround Time (TAT): -     "
                + "TOTAL Waiting Time (WT): -");
        averageLabel.setText("AVERAGE Turnaround Time (TAT): -     "
                + "AVERAGE Waiting Time (WT): -     "
                + "AVERAGE Response Time (RT): -");
        utilizationLabel.setText("CPU UTILIZATION: -");
        throughputLabel.setText("THROUGHPUT: -");
        queueArea.setText("");
        ganttPanel.setSegments(new ArrayList<Segment>(), 0);
    }

    //Checks the inputs and runs the scheduling algorithm. 
    private void calculateScheduling() {
        if (inputTable.isEditing()) {
            inputTable.getCellEditor().stopCellEditing();
        }

        int count = inputModel.getRowCount();
        //Applies the minimum depending on whether it is for sample data or Manual Input
        int minimumProcessCount = sampleDataLoaded ? 5 : 1;
        if (count < minimumProcessCount || count > 20) {
            JOptionPane.showMessageDialog(this,
                    "Please enter between " + minimumProcessCount
                    + " and 20 processes.",
                    "Invalid Process Count", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantum = (Integer) quantumSpinner.getValue();
        List<Process> processes = new ArrayList<Process>();
        Set<String> usedIDs = new HashSet<String>();

        try {
            for (int i = 0; i < count; i++) {
                Object idValue = inputModel.getValueAt(i, 0);
                String id = idValue == null ? "" : idValue.toString().trim();
                if (id.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Process ID cannot be empty. Check row " + (i + 1) + ".");
                }
                if (!usedIDs.add(id.toLowerCase())) {
                    throw new IllegalArgumentException("Duplicate Process ID: " + id);
                }

                Object arrivalValue = inputModel.getValueAt(i, 1);
                Object burstValue = inputModel.getValueAt(i, 2);
                if (arrivalValue == null || arrivalValue.toString().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Arrival Time cannot be empty. Check " + id + ".");
                }
                if (burstValue == null || burstValue.toString().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Burst Time cannot be empty. Check " + id + ".");
                }
                int arrival = Integer.parseInt(arrivalValue.toString().trim());
                int burst = Integer.parseInt(burstValue.toString().trim());

                if (arrival < 0) {
                    throw new IllegalArgumentException(
                            "Arrival Time cannot be negative. Check " + id + ".");
                }
                if (burst <= 0) {
                    throw new IllegalArgumentException(
                            "Burst Time must be greater than zero. Check " + id + ".");
                }
                if (arrival > MAX_TIME_VALUE || burst > MAX_TIME_VALUE) {
                    throw new IllegalArgumentException(
                            "Arrival Time and Burst Time must not exceed "
                            + MAX_TIME_VALUE + " ms. Check " + id + ".");
                }
                processes.add(new Process(id, arrival, burst, i));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Arrival Time and Burst Time must be valid whole numbers. "
                    + "Do not use letters, decimals, or values outside the allowed range.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Segment> gantt = new ArrayList<Segment>();
        List<String> queueLog = new ArrayList<String>();

        int totalBusyTime = 0;
        for (Process process : processes) {
            totalBusyTime += process.burstTime;
        }

        int finalTime = executeRoundRobin(processes, quantum, gantt, queueLog);
        displayResults(processes, totalBusyTime, finalTime);
        ganttPanel.setSegments(gantt, finalTime);

        StringBuilder log = new StringBuilder();
        for (String entry : queueLog) {
            log.append(entry).append("\n");
            log.append("--------------------------------------------------\n");
        }
        queueArea.setText(log.toString());
        queueArea.setCaretPosition(0);
    }

    // Array-based Round Robin implementation, following the pasted program.
    // Each process is represented by its index in the input arrays.
    private int executeRoundRobin(List<Process> processes, int quantum,
                                  List<Segment> gantt, List<String> queueLog) {
        int n = processes.size();

        int[] at = new int[n];
        int[] bt = new int[n];
        int[] rt = new int[n];
        int[] ct = new int[n];
        int[] tat = new int[n];
        int[] wt = new int[n];
        int[] response = new int[n];
        int[] firstStart = new int[n];
        boolean[] added = new boolean[n];

        // A fixed integer array is used as the ready queue, just like the
        // pasted implementation. front removes; rear appends.
        int[] queue = new int[2000000];
        int front = 0;
        int rear = 0;
        int time = 0;
        int completed = 0;

        for (int i = 0; i < n; i++) {
            at[i] = processes.get(i).arrivalTime;
            bt[i] = processes.get(i).burstTime;
            rt[i] = bt[i];
            firstStart[i] = -1;
            response[i] = 0;
        }

        // Add processes that have arrived at the initial time.
        rear = queueUpdation(time, n, at, added, queue, rear);

        while (completed < n) {
            // If no process is ready, move time forward to the next arrival.
            if (front == rear) {
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!added[i] && at[i] < nextArrival) {
                        nextArrival = at[i];
                    }
                }

                if (nextArrival != Integer.MAX_VALUE && nextArrival > time) {
                    queueLog.add("TIME " + time + "\n  CPU IDLE until time "
                            + nextArrival + ". No process is ready.");
                    addSegment(gantt, "IDLE", time, nextArrival);
                    time = nextArrival;
                }
                rear = queueUpdation(time, n, at, added, queue, rear);
                continue;
            }

            int current = queue[front++];

            StringBuilder snapshot = new StringBuilder();
            snapshot.append("TIME ").append(time).append("\n");
            snapshot.append("  RUNNING: ").append(processes.get(current).id)
                    .append(" [Remaining: ").append(rt[current]).append("]\n");
            snapshot.append("  READY QUEUE: ");
            if (front == rear) {
                snapshot.append("EMPTY");
            } else {
                for (int i = front; i < rear; i++) {
                    snapshot.append(processes.get(queue[i]).id);
                    if (i < rear - 1) snapshot.append(" -> ");
                }
            }
            snapshot.append("\n  TIME QUANTUM: ").append(quantum);
            queueLog.add(snapshot.toString());

            if (firstStart[current] == -1) {
                firstStart[current] = time;
                response[current] = firstStart[current] - at[current];
            }

            int startTime = time;
            int executionTime = Math.min(quantum, rt[current]);
            rt[current] -= executionTime;
            time += executionTime;
            addSegment(gantt, processes.get(current).id, startTime, time);

            // Add every process that arrived during this time slice before
            // placing the interrupted process at the back of the queue.
            rear = queueUpdation(time, n, at, added, queue, rear);

            if (rt[current] > 0) {
                rear = queueMaintainence(queue, rear, current);
                queueLog.add("  " + processes.get(current).id
                        + " used its time quantum and was placed at the end "
                        + "of the ready queue. Remaining time: "
                        + rt[current] + ".");
            } else {
                ct[current] = time;
                completed++;
                queueLog.add("  " + processes.get(current).id
                        + " COMPLETED at time " + time + ".");
            }
        }

        // Calculate CT, TAT and WT using the same array-based formulas.
        for (int i = 0; i < n; i++) {
            tat[i] = ct[i] - at[i];
            wt[i] = tat[i] - bt[i];

            Process process = processes.get(i);
            process.completionTime = ct[i];
            process.turnaroundTime = tat[i];
            process.waitingTime = wt[i];
            process.responseTime = response[i];
        }

        return time;
    }

    // Adds newly arrived processes to the integer-array ready queue once.
    private int queueUpdation(int currentTime, int n, int[] at,
                              boolean[] added, int[] queue, int rear) {
        for (int i = 0; i < n; i++) {
            if (at[i] <= currentTime && !added[i]) {
                queue[rear++] = i;
                added[i] = true;
            }
        }
        return rear;
    }

    // Places a process that did not finish within its quantum at the rear.
    private int queueMaintainence(int[] queue, int rear, int processIndex) {
        queue[rear++] = processIndex;
        return rear;
    }

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

    private void displayResults(List<Process> processes,
                                int totalBusyTime, int finalTime) {
        resultModel.setRowCount(0);

        int totalCT = 0;
        int totalTAT = 0;
        int totalWT = 0;
        int totalRT = 0;

        for (Process p : processes) {
            resultModel.addRow(new Object[]{
                    p.id, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime,
                    p.waitingTime, p.responseTime
            });
            totalCT += p.completionTime;
            totalTAT += p.turnaroundTime;
            totalWT += p.waitingTime;
            totalRT += p.responseTime;
        }

        resultModel.addRow(new Object[]{
                "TOTAL", "", "", totalCT + " ms",
                totalTAT + " ms", totalWT + " ms", totalRT + " ms"
        });

        int n = processes.size();
        totalLabel.setText(String.format(
                "TOTAL Completion Time (CT): %d ms     "
                + "TOTAL Turnaround Time (TAT): %d ms     "
                + "TOTAL Waiting Time (WT): %d ms",
                totalCT, totalTAT, totalWT));

        averageLabel.setText(String.format(
                "AVERAGE Turnaround Time (TAT): %.2f ms     "
                + "AVERAGE Waiting Time (WT): %.2f ms     "
                + "AVERAGE Response Time (RT): %.2f ms",
                (double) totalTAT / n, (double) totalWT / n,
                (double) totalRT / n));

        double cpuUtilization = finalTime > 0
                ? (double) totalBusyTime / finalTime * 100.0 : 0.0;
        double throughput = finalTime > 0
                ? (double) n / finalTime : 0.0;

        utilizationLabel.setText(String.format(
                "CPU UTILIZATION: %.2f%%  (Busy Time: %d ms / Elapsed Time: %d ms)",
                cpuUtilization, totalBusyTime, finalTime));
        throughputLabel.setText(String.format(
                "THROUGHPUT: %.4f processes/ms  (%d processes / %d ms)",
                throughput, n, finalTime));
    }

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

    //Zoom controls
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

    private static class GanttPanel extends JPanel {
        private List<Segment> segments = new ArrayList<Segment>();
        private double zoomScale = 1.0;

        private static final Color[] PROCESS_COLORS = {
                new Color(248, 210, 213),
                new Color(245, 190, 196),
                new Color(250, 224, 225),
                new Color(235, 185, 190),
                new Color(252, 232, 233),
                new Color(226, 170, 177)
        };

        GanttPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));
        }

        void setSegments(List<Segment> newSegments, int endTime) {
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
            for (Segment segment : segments) {
                width += Math.max(75,
                        (segment.endTime - segment.startTime) * 35) + 2;
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

            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString("CPU EXECUTION ORDER", 30, 25);

            if (segments.isEmpty()) {
                g2.setFont(new Font("Arial", Font.PLAIN, 13));
                g2.setColor(Color.GRAY);
                g2.drawString("Run the Round Robin calculation to display the Gantt Chart.",
                        30, 55);
                g2.dispose();
                return;
            }

            int x = 30;
            int y = 55;
            int blockHeight = 55;

            for (int i = 0; i < segments.size(); i++) {
                Segment segment = segments.get(i);
                int duration = segment.endTime - segment.startTime;
                int width = Math.max(75, duration * 35);

                Color fill = segment.processID.equals("IDLE")
                        ? new Color(225, 225, 225)
                        : PROCESS_COLORS[i % PROCESS_COLORS.length];

                g2.setColor(fill);
                g2.fillRoundRect(x, y, width, blockHeight, 8, 8);

                g2.setColor(new Color(145, 75, 82));
                g2.drawRoundRect(x, y, width, blockHeight, 8, 8);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                String label = segment.processID;
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (width - labelWidth) / 2, y + 32);

                g2.setFont(new Font("Arial", Font.PLAIN, 12));
                g2.drawString(String.valueOf(segment.startTime),
                        x - 3, y + blockHeight + 23);

                x += width;
            }

            Segment last = segments.get(segments.size() - 1);
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.drawString(String.valueOf(last.endTime),
                    x - 3, y + blockHeight + 23);

            g2.dispose();
        }
    }
    //Starts the Swing Application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RoundRobin app = new RoundRobin();
            app.setVisible(true);
        });
    }
}
