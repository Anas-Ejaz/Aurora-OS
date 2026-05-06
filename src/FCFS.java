import java.util.*;

public class FCFS {

    public static List<GanttBlock> run(List<PCB> processes) {
        List<GanttBlock> gantt = new ArrayList<>();

        // Sort by arrival time, then by index as tie-breaker
        List<PCB> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt((PCB p) -> p.arrival)
                              .thenComparingInt(p -> p.index));

        int currentTime = 0;

        for (PCB p : sorted) {
            // CPU is idle — jump to process arrival
            if (currentTime < p.arrival) {
                currentTime = p.arrival;
            }

            p.start  = currentTime;
            p.finish = currentTime + p.burst;
            p.computeMetrics();

            gantt.add(new GanttBlock(p.pid, p.name, p.start, p.finish, p.index));

            currentTime = p.finish;
        }

        return gantt;
    }
}