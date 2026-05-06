import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends JFrame {

    private static final Color BG_DARK   = new Color(30, 30, 35);
    private static final Color BG_MID    = new Color(42, 42, 50);
    private static final Color BG_CARD   = new Color(52, 52, 62);
    private static final Color BLUE      = new Color(55, 130, 230);
    private static final Color BLUE_LT   = new Color(90, 160, 255);
    private static final Color BLUE_DK   = new Color(35,  95, 185);
    private static final Color WHITE     = new Color(235, 238, 245);
    private static final Color WHITE_DIM = new Color(165, 172, 195);
    private static final Color BORDER_C  = new Color(65,  68,  85);

    private DefaultTableModel tableModel;
    private JTable processTable;
    private JComboBox<String> algorithmBox;
    private JSpinner quantumSpinner;
    private JPanel quantumRow;
    private GanttPanel ganttPanel;
    private ResultPanel resultPanel;
    private JLabel statusLabel;

    private int pidCounter = 1;

    public MainScreen() {
        setTitle("CPU Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 840);
        setMinimumSize(new Dimension(1050, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadSampleData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                g.setColor(BG_MID);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BLUE_DK);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("◈ ");
        icon.setFont(new Font("Monospaced", Font.BOLD, 26));
        icon.setForeground(BLUE_LT);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel title = new JLabel("Aurora OS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("FCFS  ·  Round Robin  ·  SJF  ·  SRTF");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(WHITE_DIM);

        text.add(title);
        text.add(subtitle);
        left.add(icon);
        left.add(text);

        JLabel badge = new JLabel("OS PROJECT");
        badge.setFont(new Font("Monospaced", Font.BOLD, 11));
        badge.setForeground(BLUE_LT);
        badge.setOpaque(true);
        badge.setBackground(new Color(55, 130, 230, 30));
        badge.setBorder(new CompoundBorder(
            new LineBorder(BLUE, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));

        header.add(left,  BorderLayout.WEST);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BODY
    // ══════════════════════════════════════════════════════════════════════
    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildLeftPanel(),
            buildRightPanel()
        );
        split.setDividerLocation(420);
        split.setDividerSize(4);
        split.setBackground(BORDER_C);
        split.setBorder(null);
        return split;
    }

    // ── LEFT PANEL ────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 10));

        panel.add(buildAlgorithmCard(), BorderLayout.NORTH);
        panel.add(buildProcessTable(),  BorderLayout.CENTER);

        // FIX: Run button in its own panel at SOUTH, always full width
        panel.add(buildButtons(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAlgorithmCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel cardTitle = new JLabel("Algorithm Configuration");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        cardTitle.setForeground(BLUE_LT);
        cardTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(10));

        JPanel algoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        algoRow.setOpaque(false);
        algoRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel algoLabel = new JLabel("Algorithm: ");
        algoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algoLabel.setForeground(WHITE_DIM);

        String[] options = {
            "FCFS – First Come First Serve",
            "Round Robin",
            "SJF – Shortest Job First (Non-Preemptive)",
            "SRTF – Shortest Remaining Time (Preemptive)"
        };
        algorithmBox = new JComboBox<>(options);
        algorithmBox.setBackground(BG_MID);
        algorithmBox.setForeground(WHITE);
        algorithmBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algorithmBox.addActionListener(e -> onAlgorithmChange());

        algoRow.add(algoLabel);
        algoRow.add(algorithmBox);
        card.add(algoRow);
        card.add(Box.createVerticalStrut(10));

        quantumRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        quantumRow.setOpaque(false);
        quantumRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel qLabel = new JLabel("Time Quantum: ");
        qLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        qLabel.setForeground(WHITE_DIM);

        quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        quantumSpinner.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JFormattedTextField qField = ((JSpinner.DefaultEditor) quantumSpinner.getEditor()).getTextField();
        qField.setBackground(BG_MID);
        qField.setForeground(WHITE);
        qField.setCaretColor(WHITE);
        qField.setColumns(4);

        JLabel qUnit = new JLabel(" ms");
        qUnit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        qUnit.setForeground(WHITE_DIM);

        quantumRow.add(qLabel);
        quantumRow.add(quantumSpinner);
        quantumRow.add(qUnit);
        quantumRow.setVisible(false);
        card.add(quantumRow);

        return card;
    }

    private JPanel buildProcessTable() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JLabel title = new JLabel("Process Table");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(WHITE);

        String[] columns = {"PID", "Process Name", "Arrival Time", "Burst Time", "Priority"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return col != 0;
            }
        };

        processTable = new JTable(tableModel);
        processTable.setBackground(BG_CARD);
        processTable.setForeground(WHITE);
        processTable.setGridColor(BORDER_C);
        processTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        processTable.setRowHeight(26);
        processTable.setSelectionBackground(BLUE_DK);
        processTable.setSelectionForeground(WHITE);
        processTable.setShowHorizontalLines(true);
        processTable.setShowVerticalLines(false);

        JTableHeader header = processTable.getTableHeader();
        header.setBackground(BG_MID);
        header.setForeground(BLUE_LT);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(new MatteBorder(0, 0, 2, 0, BLUE_DK));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(BG_CARD);
        centerRenderer.setForeground(WHITE);
        for (int i = 0; i < columns.length; i++) {
            processTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        processTable.getColumnModel().getColumn(0).setMaxWidth(55);

        JScrollPane scroll = new JScrollPane(processTable);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(new LineBorder(BORDER_C, 1));

        wrapper.add(title,  BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // FIX: Separated Add/Remove/Clear row from Run button
    // Run button gets its own full-width row so it never gets pushed off-screen
    private JPanel buildButtons() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setOpaque(false);

        // Top row: Add / Remove / Clear
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JButton addBtn = makeButton("＋ Add",    BG_CARD,  WHITE);
        JButton remBtn = makeButton("－ Remove", BG_CARD,  WHITE_DIM);
        JButton clrBtn = makeButton("⊘ Clear",   BG_CARD,  WHITE_DIM);

        addBtn.addActionListener(e -> addProcess());
        remBtn.addActionListener(e -> removeProcess());
        clrBtn.addActionListener(e -> clearAll());

        topRow.add(addBtn);
        topRow.add(remBtn);
        topRow.add(clrBtn);

        // Bottom row: Run button — full width, always visible
        JButton runBtn = makeButton("▶   Run Simulation", BLUE_DK, WHITE);
        runBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        runBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        runBtn.addActionListener(e -> runSimulation());

        // Wrap runBtn in a panel so it stretches full width
        JPanel runRow = new JPanel(new BorderLayout());
        runRow.setOpaque(false);
        runRow.add(runBtn, BorderLayout.CENTER);

        outer.add(topRow, BorderLayout.NORTH);
        outer.add(runRow, BorderLayout.SOUTH);
        return outer;
    }

    // ── RIGHT PANEL ───────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(14, 10, 14, 14));

        JLabel ganttTitle = new JLabel("Gantt Chart");
        ganttTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        ganttTitle.setForeground(WHITE);

        ganttPanel = new GanttPanel();

        JScrollPane ganttScroll = new JScrollPane(ganttPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScroll.setBorder(new LineBorder(BORDER_C, 1));
        ganttScroll.getViewport().setBackground(new Color(52, 52, 62));
        ganttScroll.setPreferredSize(new Dimension(0, 120));

        JPanel ganttSection = new JPanel(new BorderLayout(0, 6));
        ganttSection.setOpaque(false);
        ganttSection.add(ganttTitle,  BorderLayout.NORTH);
        ganttSection.add(ganttScroll, BorderLayout.CENTER);
        ganttSection.setPreferredSize(new Dimension(0, 140));

        resultPanel = new ResultPanel();

        panel.add(ganttSection, BorderLayout.NORTH);
        panel.add(resultPanel,  BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_MID);
        footer.setBorder(new CompoundBorder(
            new MatteBorder(2, 0, 0, 0, BLUE_DK),
            new EmptyBorder(6, 16, 6, 16)
        ));

        statusLabel = new JLabel("Ready — add processes and click Run Simulation");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(WHITE_DIM);

        JLabel version = new JLabel("CPU Scheduling Simulator  v2.0");
        version.setFont(new Font("Monospaced", Font.PLAIN, 11));
        version.setForeground(BORDER_C);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(version,     BorderLayout.EAST);
        return footer;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════════════════
    private void addProcess() {
        tableModel.addRow(new Object[]{
            "P" + pidCounter,
            "Process " + pidCounter,
            0, 5, 1
        });
        int last = processTable.getRowCount() - 1;
        processTable.setRowSelectionInterval(last, last);
        processTable.scrollRectToVisible(processTable.getCellRect(last, 0, true));
        pidCounter++;
    }

    private void removeProcess() {
        int row = processTable.getSelectedRow();
        if (row >= 0) {
            tableModel.removeRow(row);
        } else {
            setStatus("Please select a row to remove.");
        }
    }

    private void clearAll() {
        tableModel.setRowCount(0);
        pidCounter = 1;
        ganttPanel.clear();
        resultPanel.clear();
        setStatus("Cleared.");
    }

    private void onAlgorithmChange() {
        boolean isRR = algorithmBox.getSelectedIndex() == 1;
        quantumRow.setVisible(isRR);
        revalidate();
    }

    private void runSimulation() {
        if (tableModel.getRowCount() == 0) {
            setStatus("No processes found. Add at least one process.");
            return;
        }

        // Stop any active cell editing so the last typed value is committed
        if (processTable.isEditing()) {
            processTable.getCellEditor().stopCellEditing();
        }

        try {
            // FIX: collectProcesses() now uses PCB, not Process
            List<PCB> processes = collectProcesses();
            int algo    = algorithmBox.getSelectedIndex();
            int quantum = (int) quantumSpinner.getValue();

            List<GanttBlock> gantt;
            switch (algo) {
                case 0: gantt = FCFS.run(processes);               break;
                case 1: gantt = RoundRobin.run(processes, quantum); break;
                case 2: gantt = SJF.run(processes);                break;
                case 3: gantt = SRTF.run(processes);               break;
                default: gantt = FCFS.run(processes);
            }

            ganttPanel.setBlocks(gantt);
            resultPanel.setData(
                processes,
                algorithmBox.getSelectedItem().toString(),
                algo == 1 ? quantum : -1
            );

            setStatus("Done — " + processes.size() + " processes | "
                      + algorithmBox.getSelectedItem());

        } catch (NumberFormatException e) {
            setStatus("Error: Check that Arrival and Burst times are valid numbers.");
        } catch (IllegalArgumentException e) {
            setStatus("Error: " + e.getMessage());
        }
    }

    // FIX: Changed Process → PCB throughout
    private List<PCB> collectProcesses() {
        List<PCB> list = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String pidStr  = tableModel.getValueAt(i, 0).toString().trim();
            int arrival    = Integer.parseInt(tableModel.getValueAt(i, 2).toString().trim());
            int burst      = Integer.parseInt(tableModel.getValueAt(i, 3).toString().trim());

            if (burst <= 0)   throw new IllegalArgumentException("Burst time must be > 0 for " + pidStr);
            if (arrival < 0)  throw new IllegalArgumentException("Arrival time cannot be negative for " + pidStr);

            // Extract numeric part from PID string (e.g. "P3" → 3)
            int pidNum;
            try {
                pidNum = Integer.parseInt(pidStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                pidNum = i + 1;
            }

            list.add(new PCB(pidStr, pidStr, pidNum, arrival, burst, pidNum));
        }
        return list;
    }

    private void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()  ? bg.darker()  :
                             getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadSampleData() {
        Object[][] samples = {
            {"P1", "Process 1", 0, 8, 3},
            {"P2", "Process 2", 1, 4, 1},
            {"P3", "Process 3", 2, 9, 4},
            {"P4", "Process 4", 3, 5, 2},
            {"P5", "Process 5", 4, 2, 5}
        };
        for (Object[] row : samples) {
            tableModel.addRow(row);
        }
        pidCounter = 6;
    }
}