import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GanttPanel extends JPanel {

    // Colors for each process (assigned by process index)
    private static final Color[] P_COLORS = {
        new Color(55, 130, 230),
        new Color(220,  80, 100),
        new Color(55,  190, 120),
        new Color(230, 165,  45),
        new Color(160,  80, 230),
        new Color(55,  200, 215),
        new Color(230, 110,  55),
        new Color(100, 210,  80),
        new Color(210,  70, 170),
        new Color(70,  195, 180)
    };

    private static final int BLOCK_H   = 50;   // height of each gantt block
    private static final int TIME_H    = 18;   // height below block for time labels
    private static final int PAD       = 16;   // padding around chart
    private static final int PX_PER_MS = 42;   // pixels per time unit

    private List<GanttBlock> blocks = new ArrayList<>();

    public GanttPanel() {
        setBackground(new Color(52, 52, 62));
        setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));
    }

    /** Load new simulation results and repaint. */
    public void setBlocks(List<GanttBlock> blocks) {
        this.blocks = blocks;

        // Resize panel width to fit all blocks
        int totalTime = blocks.isEmpty() ? 0 : blocks.get(blocks.size() - 1).end;
        int width     = PAD * 2 + totalTime * PX_PER_MS + 60;
        setPreferredSize(new Dimension(Math.max(600, width), BLOCK_H + TIME_H + PAD * 2));

        revalidate();
        repaint();
    }

    /** Reset chart to empty. */
    public void clear() {
        blocks.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (blocks.isEmpty()) {
            g2.setColor(new Color(165, 172, 195));
            g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
            g2.drawString("Run a simulation to see the Gantt chart here.", PAD, PAD + 24);
            g2.dispose();
            return;
        }

        int blockY = PAD;
        int timeY  = PAD + BLOCK_H + 14;   // y-position of time labels

        for (GanttBlock block : blocks) {
            int x = PAD + block.start * PX_PER_MS;
            int w = (block.end - block.start) * PX_PER_MS;

            Color base  = P_COLORS[block.colorIndex % P_COLORS.length];
            Color light = base.brighter();

            // Draw filled block with gradient
            GradientPaint gradient = new GradientPaint(x, blockY, light, x, blockY + BLOCK_H, base.darker());
            g2.setPaint(gradient);
            g2.fillRoundRect(x, blockY, w, BLOCK_H, 6, 6);

            // Draw block border
            g2.setColor(light);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, blockY, w, BLOCK_H, 6, 6);

            // Draw PID label inside block (centered)
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int labelX = x + (w - fm.stringWidth(block.pid)) / 2;
            int labelY = blockY + BLOCK_H / 2 + fm.getAscent() / 2 - 2;
            if (labelX >= x) {
                g2.drawString(block.pid, labelX, labelY);
            }

            // Draw start time below block
            g2.setColor(new Color(165, 172, 195));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.drawString(String.valueOf(block.start), x, timeY);
        }

        // Draw end time of last block
        if (!blocks.isEmpty()) {
            GanttBlock last = blocks.get(blocks.size() - 1);
            int endX = PAD + last.end * PX_PER_MS;
            g2.setColor(new Color(165, 172, 195));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2.drawString(String.valueOf(last.end), endX, timeY);
        }

        g2.dispose();
    }
}