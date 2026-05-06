import java.util.*;

public class SRTF {

    public static List<GanttBlock> run(List<PCB> processes) {
        List<GanttBlock> gantt = new ArrayList<>();

        int n = processes.size();
        int[] remaining = new int[n];
        boolean[] started = new boolean[n];

        for (int i = 0; i < n; i++) {
            remaining[i] = processes.get(i).burst;
        }

        int totalTime   = processes.stream().mapToInt(p -> p.arrival + p.burst).max().orElse(0);
        int currentTime = 0;
        int completed   = 0;

        // For building contiguous Gantt blocks
        int lastIdx       = -1;
        int segmentStart  = 0;

        while (completed < n && currentTime <= totalTime) {
            // Pick arrived process with shortest remaining time
            int chosenIdx = -1;
            int minRemain = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                PCB p = processes.get(i);
                if (remaining[i] > 0 && p.arrival <= currentTime && remaining[i] < minRemain) {
                    minRemain  = remaining[i];
                    chosenIdx  = i;
                }
            }

            if (chosenIdx == -1) {
                // CPU idle
                currentTime++;
                continue;
            }

            PCB chosen = processes.get(chosenIdx);

            if (!started[chosenIdx]) {
                chosen.start      = currentTime;
                started[chosenIdx] = true;
            }

            // If process changed, close previous Gantt block
            if (chosenIdx != lastIdx) {
                if (lastIdx != -1) {
                    PCB prev = processes.get(lastIdx);
                    gantt.add(new GanttBlock(prev.pid, prev.name,
                                             segmentStart, currentTime, prev.index));
                }
                segmentStart = currentTime;
                lastIdx      = chosenIdx;
            }

            remaining[chosenIdx]--;
            currentTime++;

            if (remaining[chosenIdx] == 0) {
                chosen.finish = currentTime;
                chosen.computeMetrics();
                completed++;
            }
        }

        // Close the final block
        if (lastIdx != -1) {
            PCB last = processes.get(lastIdx);
            gantt.add(new GanttBlock(last.pid, last.name,
                                     segmentStart, currentTime, last.index));
        }

        return gantt;
    }
}