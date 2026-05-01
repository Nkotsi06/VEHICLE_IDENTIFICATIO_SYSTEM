package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class RankChangeRequest {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty officerId = new SimpleIntegerProperty();
    private final StringProperty officerName = new SimpleStringProperty();
    private final StringProperty currentRank = new SimpleStringProperty();
    private final StringProperty requestedRank = new SimpleStringProperty();
    private final StringProperty reason = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty reviewedBy = new SimpleIntegerProperty();
    private final StringProperty reviewerName = new SimpleStringProperty();
    private final StringProperty reviewNotes = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

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

    public String getCurrentRank() { return currentRank.get(); }
    public void setCurrentRank(String value) { currentRank.set(value); }
    public StringProperty currentRankProperty() { return currentRank; }

    public String getRequestedRank() { return requestedRank.get(); }
    public void setRequestedRank(String value) { requestedRank.set(value); }
    public StringProperty requestedRankProperty() { return requestedRank; }

    public String getReason() { return reason.get(); }
    public void setReason(String value) { reason.set(value); }
    public StringProperty reasonProperty() { return reason; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public int getReviewedBy() { return reviewedBy.get(); }
    public void setReviewedBy(int value) { reviewedBy.set(value); }
    public IntegerProperty reviewedByProperty() { return reviewedBy; }

    public String getReviewerName() { return reviewerName.get(); }
    public void setReviewerName(String value) { reviewerName.set(value); }
    public StringProperty reviewerNameProperty() { return reviewerName; }

    public String getReviewNotes() { return reviewNotes.get(); }
    public void setReviewNotes(String value) { reviewNotes.set(value); }
    public StringProperty reviewNotesProperty() { return reviewNotes; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
}