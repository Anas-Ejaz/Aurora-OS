package Schedulings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProcessData {
    private final SimpleStringProperty id;
    private final SimpleIntegerProperty arrivalTime;
    private final SimpleIntegerProperty burstTime;
    private final SimpleIntegerProperty waitingTime;
    private final SimpleIntegerProperty turnaroundTime;

    public ProcessData(String id, int arrivalTime, int burstTime, int waitingTime, int turnaroundTime) {
        this.id = new SimpleStringProperty(id);
        this.arrivalTime = new SimpleIntegerProperty(arrivalTime);
        this.burstTime = new SimpleIntegerProperty(burstTime);
        this.waitingTime = new SimpleIntegerProperty(waitingTime);
        this.turnaroundTime = new SimpleIntegerProperty(turnaroundTime);
    }

    public String getId() { return id.get(); }
    public SimpleStringProperty idProperty() { return id; }

    public int getArrivalTime() { return arrivalTime.get(); }
    public SimpleIntegerProperty arrivalTimeProperty() { return arrivalTime; }

    public int getBurstTime() { return burstTime.get(); }
    public SimpleIntegerProperty burstTimeProperty() { return burstTime; }

    public int getWaitingTime() { return waitingTime.get(); }
    public SimpleIntegerProperty waitingTimeProperty() { return waitingTime; }

    public int getTurnaroundTime() { return turnaroundTime.get(); }
    public SimpleIntegerProperty turnaroundTimeProperty() { return turnaroundTime; }
}