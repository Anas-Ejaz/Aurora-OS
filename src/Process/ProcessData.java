package Process;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProcessData {
    private static final ObservableList<ProcessData> masterProcessList = FXCollections.observableArrayList();

    public static ObservableList<ProcessData> getMasterList() {
        return masterProcessList;
    }

    private final SimpleStringProperty id;
    private final SimpleIntegerProperty arrivalTime;
    private final SimpleIntegerProperty burstTime;
    private final SimpleIntegerProperty priority;       // Added for Priority Scheduling
    private final SimpleIntegerProperty remainingTime;   // Added for Round Robin / SRTF
    private final SimpleIntegerProperty waitingTime;
    private final SimpleIntegerProperty turnaroundTime;

    // Constructor
    public ProcessData(String id, int arrivalTime, int burstTime, int priority, int waitingTime, int turnaroundTime) {
        this.id = new SimpleStringProperty(id);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.priority = new SimpleIntegerProperty(priority);
        this.remainingTime = new SimpleIntegerProperty(burstTime);
        this.waitingTime = new SimpleIntegerProperty(waitingTime);
        this.turnaroundTime = new SimpleIntegerProperty(turnaroundTime);
    }

    // --- GETTERS, SETTERS & PROPERTIES ---
    public String getId() { return id.get(); }
    public void setId(String newId) { this.id.set(newId); }
    public SimpleStringProperty idProperty() { return id; }

    public int getArrivalTime() { return arrivalTime.get(); }
    public void setArrivalTime(int at) { this.arrivalTime.set(at); }
    public SimpleIntegerProperty arrivalTimeProperty() { return arrivalTime; }

    public int getBurstTime() { return burstTime.get(); }
    public void setBurstTime(int bt) { this.burstTime.set(bt); this.remainingTime.set(bt); }
    public SimpleIntegerProperty burstTimeProperty() { return burstTime; }

    public int getPriority() { return priority.get(); }
    public void setPriority(int pr) { this.priority.set(pr); }
    public SimpleIntegerProperty priorityProperty() { return priority; }

    public int getRemainingTime() { return remainingTime.get(); }
    public void setRemainingTime(int rt) { this.remainingTime.set(rt); }
    public SimpleIntegerProperty remainingTimeProperty() { return remainingTime; }

    public int getWaitingTime() { return waitingTime.get(); }
    public void setWaitingTime(int wt) { this.waitingTime.set(wt); }
    public SimpleIntegerProperty waitingTimeProperty() { return waitingTime; }

    public int getTurnaroundTime() { return turnaroundTime.get(); }
    public void setTurnaroundTime(int tt) { this.turnaroundTime.set(tt); }
    public SimpleIntegerProperty turnaroundTimeProperty() { return turnaroundTime; }
}