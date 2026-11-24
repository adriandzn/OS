import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainMenu extends JFrame {
    private static final Color PURPLE = new Color(91, 55, 130);
    private static final Color DARK_PURPLE = new Color(59, 35, 91);
    private static final Color LIGHT_PURPLE = new Color(246, 241, 251);

    public MainMenu() {
        setTitle("The Kernels - Scheduling Algorithms");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 650);
        setMinimumSize(new Dimension(650, 560));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(LIGHT_PURPLE);
        root.setBorder(new EmptyBorder(24, 30, 24, 30));
        setContentPane(root);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("THE KERNELS");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(DARK_PURPLE);
        JLabel subtitle = new JLabel("CPU & Disk Scheduling Simulator");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(105, 87, 122));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        root.add(heading, BorderLayout.NORTH);

        JPanel algorithms = new JPanel(new GridLayout(1, 2, 18, 0));
        algorithms.setOpaque(false);
        algorithms.add(createCategoryPanel("CPU SCHEDULING",
                new String[]{"SRTF", "Round Robin"},
                new Runnable[]{() -> openSRTF(), () -> openRoundRobin()}));
        algorithms.add(createCategoryPanel("DISK SCHEDULING",
                new String[]{"SCAN", "C-LOOK"},
                new Runnable[]{() -> openScan(), () -> openCLook()}));

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);
        center.add(algorithms, BorderLayout.CENTER);
        JPanel aboutPanel = new JPanel(new BorderLayout(12, 0));
        aboutPanel.setBackground(Color.WHITE);
        aboutPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(218, 204, 233)),
                        "ABOUT THE KERNELS"),
                new EmptyBorder(8, 12, 8, 12)));
        JLabel members = new JLabel("<html><b>3-ITB Group #1:</b> The Kernels<br>"
                + "<b>Members:</b> ALBERTO, Jandrel Jimwee C. &nbsp;|&nbsp; "
                + "ARMAS, Francisco III I. &nbsp;|&nbsp; DIZON, Adrian D. &nbsp;|&nbsp; "
                + "LINGAD, Immanuel Marcus T.</html>");
        members.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        members.setForeground(DARK_PURPLE);
        aboutPanel.add(members, BorderLayout.CENTER);
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));
        aboutPanel.add(exitButton, BorderLayout.EAST);
        center.add(aboutPanel, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);
    }

    private JPanel createCategoryPanel(String heading, String[] names, Runnable[] actions) {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 204, 233), 1, true),
                new EmptyBorder(18, 16, 18, 16)));
        JLabel label = new JLabel(heading, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(DARK_PURPLE);
        panel.add(label);
        for (int i = 0; i < names.length; i++) {
            final Runnable action = actions[i];
            JButton button = new JButton(names[i]);
            styleButton(button);
            button.addActionListener(e -> action.run());
            panel.add(button);
        }
        return panel;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(PURPLE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        button.setOpaque(true);
    }

    private void openSRTF() { launch(new SRTF()); }
    private void openRoundRobin() { launch(new RoundRobin()); }
    private void openScan() { launch(new Scan()); }
    private void openCLook() { launch(new CLook()); }

    private void launch(JFrame frame) {
        setVisible(false);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
