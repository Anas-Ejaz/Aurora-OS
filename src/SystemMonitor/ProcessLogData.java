package SystemMonitor;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ProcessLogData {
    private final SimpleIntegerProperty timestamp;
    private final SimpleDoubleProperty cpuUsage;
    private final SimpleDoubleProperty ramUsage;
    private final SimpleStringProperty status;

    public ProcessLogData(int timestamp, double cpuUsage, double ramUsage, String status) {
        this.timestamp = new SimpleIntegerProperty(timestamp);
        this.cpuUsage = new SimpleDoubleProperty(cpuUsage);
        this.ramUsage = new SimpleDoubleProperty(ramUsage);
        this.status = new SimpleStringProperty(status);
    }

    public int getTimestamp() { return timestamp.get(); }
    public SimpleIntegerProperty timestampProperty() { return timestamp; }

    public double getCpuUsage() { return cpuUsage.get(); }
    public SimpleDoubleProperty cpuUsageProperty() { return cpuUsage; }

    public double getRamUsage() { return ramUsage.get(); }
    public SimpleDoubleProperty ramUsageProperty() { return ramUsage; }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
}