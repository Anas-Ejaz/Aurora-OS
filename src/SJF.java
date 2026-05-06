import java.util.*;

public class SJF {

    public static List<GanttBlock> run(List<PCB> processes) {
        List<GanttBlock> gantt = new ArrayList<>();

        List<PCB> remaining = new ArrayList<>(processes);
        int currentTime = 0;
        int completed   = 0;
        int n           = remaining.size();

        while (completed < n) {
            // Find arrived process with shortest burst
            PCB chosen = null;
            for (PCB p : remaining) {
                if (p.arrival <= currentTime) {
                    if (chosen == null || p.burst < chosen.burst
                            || (p.burst == chosen.burst && p.index < chosen.index)) {
                        chosen = p;
                    }
                }
            }

            if (chosen == null) {
                // No process ready — advance time to next arrival
                currentTime++;
                continue;
            }

            chosen.start  = currentTime;
            chosen.finish = currentTime + chosen.burst;
            chosen.computeMetrics();

            gantt.add(new GanttBlock(chosen.pid, chosen.name,
                                     chosen.start, chosen.finish, chosen.index));

            currentTime = chosen.finish;
            remaining.remove(chosen);
            completed++;
        }

        return gantt;
    }
}