public class PCB {

    // --- Input fields ---
    public String pid;
    public String name;
    public int    arrival;
    public int    burst;
    public int    priority;
    public int    index;      // row order (used for tie-breaking)

    // --- Computed fields (filled after simulation) ---
    public int start;
    public int finish;
    public int turnaround;   // finish - arrival
    public int waiting;      // turnaround - burst
    public int response;     // start - arrival

    public PCB(String pid, String name, int arrival, int burst, int priority, int index) {
        this.pid      = pid;
        this.name     = name;
        this.arrival  = arrival;
        this.burst    = burst;
        this.priority = priority;
        this.index    = index;
    }


    /** Call this after setting start and finish to compute derived metrics. */
    public void computeMetrics() {
        this.turnaround = finish - arrival;
        this.waiting    = turnaround - burst;
        this.response   = start - arrival;
    }
}