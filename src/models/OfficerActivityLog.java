package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class OfficerActivityLog {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty officerId = new SimpleIntegerProperty();
    private final StringProperty officerName = new SimpleStringProperty();
    private final StringProperty actionType = new SimpleStringProperty();
    private final StringProperty actionDescription = new SimpleStringProperty();
    private final StringProperty targetType = new SimpleStringProperty();
    private final IntegerProperty targetId = new SimpleIntegerProperty();
    private final StringProperty targetName = new SimpleStringProperty();
    private final StringProperty ipAddress = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getOfficerId() { return officerId.get(); }
    public void setOfficerId(int value) { officerId.set(value); }
    public IntegerProperty officerIdProperty() { return officerId; }

    public String getOfficerName() { return officerName.get(); }
    public void setOfficerName(String value) { officerName.set(value); }
    public StringProperty officerNameProperty() { return officerName; }

    public String getActionType() { return actionType.get(); }
    public void setActionType(String value) { actionType.set(value); }
    public StringProperty actionTypeProperty() { return actionType; }

    public String getActionDescription() { return actionDescription.get(); }
    public void setActionDescription(String value) { actionDescription.set(value); }
    public StringProperty actionDescriptionProperty() { return actionDescription; }

    public String getTargetType() { return targetType.get(); }
    public void setTargetType(String value) { targetType.set(value); }
    public StringProperty targetTypeProperty() { return targetType; }

    public int getTargetId() { return targetId.get(); }
    public void setTargetId(int value) { targetId.set(value); }
    public IntegerProperty targetIdProperty() { return targetId; }

    public String getTargetName() { return targetName.get(); }
    public void setTargetName(String value) { targetName.set(value); }
    public StringProperty targetNameProperty() { return targetName; }

    public String getIpAddress() { return ipAddress.get(); }
    public void setIpAddress(String value) { ipAddress.set(value); }
    public StringProperty ipAddressProperty() { return ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
}