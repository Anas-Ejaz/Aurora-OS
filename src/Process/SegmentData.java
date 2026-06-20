package Process;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SegmentData {
    private final SimpleStringProperty segmentName;
    private final SimpleIntegerProperty baseAddress;
    private final SimpleIntegerProperty limitSize;

    public SegmentData(String name, int base, int limit) {
        this.segmentName = new SimpleStringProperty(name);
        this.baseAddress = new SimpleIntegerProperty(base);
        this.limitSize = new SimpleIntegerProperty(limit);
    }

    public String getSegmentName() { return segmentName.get(); }
    public SimpleStringProperty segmentNameProperty() { return segmentName; }

    public int getBaseAddress() { return baseAddress.get(); }
    public SimpleIntegerProperty baseAddressProperty() { return baseAddress; }

    public int getLimitSize() { return limitSize.get(); }
    public SimpleIntegerProperty limitSizeProperty() { return limitSize; }
}