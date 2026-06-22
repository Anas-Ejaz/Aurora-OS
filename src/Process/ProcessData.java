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
    private final SimpleIntegerProperty priority;       
    private final SimpleIntegerProperty remainingTime;   
    private final SimpleIntegerProperty waitingTime;
    private final SimpleIntegerProperty turnaroundTime;

    // --- PCB EXTRA FIELDS ADDED HERE ---
    private final SimpleStringProperty state;
    private final SimpleStringProperty pc;
    private final SimpleStringProperty memory;

    // Constructor
    public ProcessData(String id, int arrivalTime, int burstTime, int priority, int waitingTime, int turnaroundTime) {
        this.id = new SimpleStringProperty(id);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.priority = new SimpleIntegerProperty(priority);
        this.remainingTime = new SimpleIntegerProperty(burstTime);
        this.waitingTime = new SimpleIntegerProperty(waitingTime);
        this.turnaroundTime = new SimpleIntegerProperty(turnaroundTime);
        
        // Default PCB values sets dynamically based on ID
        this.state = new SimpleStringProperty("READY");
        this.pc = new SimpleStringProperty("0x0040" + String.format("%02X", (int)(Math.random() * 255)));
        this.memory = new SimpleStringProperty(((int)(Math.random() * 4) + 1) * 128 + "MB");
    }

    // --- PCB GETTERS, SETTERS & PROPERTIES ---
    public String getState() { return state.get(); }
    public void setState(String newState) { this.state.set(newState); }
    public SimpleStringProperty stateProperty() { return state; }

    public String getPc() { return pc.get(); }
    public void setPc(String newPc) { this.pc.set(newPc); }
    public SimpleStringProperty pcProperty() { return pc; }

    public String getMemory() { return memory.get(); }
    public SimpleStringProperty memoryProperty() { return memory; }

    // --- ORIGINAL GETTERS & SETTERS ---
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