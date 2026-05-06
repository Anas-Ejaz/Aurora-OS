public class GanttBlock {

    public String pid;
    public String name;
    public int    start;
    public int    end;
    public int    colorIndex;   // maps to P_COLORS array in GanttPanel

    public GanttBlock(String pid, String name, int start, int end, int colorIndex) {
        this.pid        = pid;
        this.name       = name;
        this.start      = start;
        this.end        = end;
        this.colorIndex = colorIndex;
    }
}