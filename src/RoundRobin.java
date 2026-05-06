import java.util.*;


public class RoundRobin {

    public static List<GanttBlock> run(List<PCB> processes, int quantum) {
        List<GanttBlock> gantt = new ArrayList<>();

        // Sort by arrival time
        List<PCB> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrival));

        int n = sorted.size();
        int[] remaining = new int[n];
        boolean[] started = new boolean[n];

        for (int i = 0; i < n; i++) {
            remaining[i] = sorted.get(i).burst;
        }

        Queue<Integer> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completed   = 0;
        int ptr         = 0;   // points to next process to arrive

        // Add all processes that arrive at time 0
        while (ptr < n && sorted.get(ptr).arrival <= currentTime) {
            readyQueue.add(ptr++);
        }

        // If nothing at time 0, jump to first arrival
        if (readyQueue.isEmpty() && ptr < n) {
            currentTime = sorted.get(ptr).arrival;
            readyQueue.add(ptr++);
        }

        while (completed < n) {
            if (readyQueue.isEmpty()) {
                // CPU idle — jump to next arriving process
                currentTime = sorted.get(ptr).arrival;
                while (ptr < n && sorted.get(ptr).arrival <= currentTime) {
                    readyQueue.add(ptr++);
                }
            }

            int idx = readyQueue.poll();
            PCB p = sorted.get(idx);

            if (!started[idx]) {
                p.start    = currentTime;
                started[idx] = true;
            }

            int runFor = Math.min(quantum, remaining[idx]);
            gantt.add(new GanttBlock(p.pid, p.name, currentTime, currentTime + runFor, p.index));

            currentTime  += runFor;
            remaining[idx] -= runFor;

            // Enqueue newly arrived processes before re-queueing current
            while (ptr < n && sorted.get(ptr).arrival <= currentTime) {
                readyQueue.add(ptr++);
            }

            if (remaining[idx] == 0) {
                p.finish = currentTime;
                p.computeMetrics();
                completed++;
            } else {
                readyQueue.add(idx);   // not done — go back to queue
            }
        }

        return gantt;
    }
}