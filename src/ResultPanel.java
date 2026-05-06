import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ResultPanel extends JPanel {

    private static final Color BG_DARK   = new Color(30, 30, 35);
    private static final Color BG_MID    = new Color(42, 42, 50);
    private static final Color BG_CARD   = new Color(52, 52, 62);
    private static final Color BG_ALT    = new Color(48, 48, 58);
    private static final Color BLUE      = new Color(55, 130, 230);
    private static final Color BLUE_LT   = new Color(90, 160, 255);
    private static final Color BLUE_DK   = new Color(35,  95, 185);
    private static final Color WHITE     = new Color(235, 238, 245);
    private static final Color WHITE_DIM = new Color(165, 172, 195);
    private static final Color BORDER_C  = new Color(65,  68,  85);
    private static final Color GREEN     = new Color(55,  190, 120);
    private static final Color ORANGE    = new Color(230, 165,  45);

    private DefaultTableModel tableModel;
    private JLabel avgWaiting, avgTurnaround, avgResponse;
    private JLabel algoLabel, quantumLabel;

    public ResultPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(new EmptyBorder(10, 0, 0, 0));

        add(buildTitleRow(),  BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildAvgRow(),    BorderLayout.SOUTH);
    }

    // --- Title row with algo name and quantum label ---
    private JPanel buildTitleRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel title = new JLabel("Simulation Results");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        quantumLabel = new JLabel("");
        quantumLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        quantumLabel.setForeground(ORANGE);

        algoLabel = new JLabel("");
        algoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        algoLabel.setForeground(BLUE_LT);

        right.add(quantumLabel);
        right.add(algoLabel);

        row.add(title, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // --- Results table ---
    private JScrollPane buildTable() {
        String[] columns = {"PID", "Name", "Arrival", "Burst", "Start",
                            "Finish", "Turnaround", "Waiting", "Response"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setBackground(BG_CARD);
        table.setForeground(WHITE);
        table.setGridColor(BORDER_C);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionBackground(BLUE_DK);
        table.setSelectionForeground(WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_MID);
        header.setForeground(BLUE_LT);
        header.setFont(new Font("SansSerif", Font.BOLD, 11));
        header.setBorder(new MatteBorder(0, 0, 2, 0, BLUE_DK));

        // Per-column foreground colors
        Color[] colColors = {
            BLUE_LT, WHITE, WHITE_DIM, WHITE_DIM,
            WHITE, WHITE, GREEN, ORANGE, BLUE_LT
        };

        for (int i = 0; i < columns.length; i++) {
            final Color fg = colColors[i];
            table.getColumnModel().getColumn(i).setCellRenderer(
                new DefaultTableCellRenderer() {
                    public Component getTableCellRendererComponent(
                            JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                        setBackground(sel ? BLUE_DK : (row % 2 == 0 ? BG_CARD : BG_ALT));
                        setForeground(sel ? WHITE : fg);
                        setHorizontalAlignment(SwingConstants.CENTER);
                        setBorder(new EmptyBorder(0, 4, 0, 4));
                        return this;
                    }
                }
            );
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_CARD);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(new LineBorder(BORDER_C, 1));
        return scroll;
    }

    // --- Averages bar at the bottom ---
    private JPanel buildAvgRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        row.setBackground(BG_CARD);
        row.setBorder(new CompoundBorder(
            new LineBorder(BORDER_C, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));

        avgWaiting    = avgLabel("Avg Waiting Time:   —");
        avgTurnaround = avgLabel("Avg Turnaround:   —");
        avgResponse   = avgLabel("Avg Response:   —");

        row.add(avgWaiting);
        row.add(separator());
        row.add(avgTurnaround);
        row.add(separator());
        row.add(avgResponse);
        return row;
    }

    private JLabel avgLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        l.setForeground(WHITE_DIM);
        return l;
    }

    private JLabel separator() {
        JLabel l = new JLabel("│");
        l.setForeground(BORDER_C);
        return l;
    }

    // --- Public API ---

    /** Populate table with process results and compute averages. */
    public void setData(List<PCB> processes, String algoName, int quantum) {
        tableModel.setRowCount(0);

        double totalWT = 0, totalTAT = 0, totalRT = 0;

        for (PCB p : processes) {
            tableModel.addRow(new Object[]{
                p.pid, p.name, p.arrival, p.burst,
                p.start, p.finish, p.turnaround, p.waiting, p.response
            });
            totalWT  += p.waiting;
            totalTAT += p.turnaround;
            totalRT  += p.response;
        }

        int n = processes.size();
        avgWaiting   .setText("Avg Waiting Time:   " + String.format("%.2f", totalWT  / n));
        avgTurnaround.setText("Avg Turnaround:   "   + String.format("%.2f", totalTAT / n));
        avgResponse  .setText("Avg Response:   "     + String.format("%.2f", totalRT  / n));

        algoLabel  .setText("[ " + algoName + " ]");
        quantumLabel.setText(quantum > 0 ? "Quantum = " + quantum + " ms    " : "");
    }

    /** Reset panel to blank state. */
    public void clear() {
        tableModel.setRowCount(0);
        avgWaiting   .setText("Avg Waiting Time:   —");
        avgTurnaround.setText("Avg Turnaround:   —");
        avgResponse  .setText("Avg Response:   —");
        algoLabel  .setText("");
        quantumLabel.setText("");
    }
}