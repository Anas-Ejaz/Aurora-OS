package SystemMonitor;

public class ProcessLogData {
    private final String appName;
    private final String pidString;
    private final String memoryWorkingSet;
    private final String statusText;

    public ProcessLogData(String appName, String pidString, String memoryWorkingSet, String statusText) {
        this.appName = appName;
        this.pidString = pidString;
        this.memoryWorkingSet = memoryWorkingSet;
        this.statusText = statusText;
    }

    // These getters MUST match PropertyValueFactory definitions exactly!
    public String getAppName() { return appName; }
    public String getPidString() { return pidString; }
    public String getMemoryWorkingSet() { return memoryWorkingSet; }
    public String getStatusText() { return statusText; }
}