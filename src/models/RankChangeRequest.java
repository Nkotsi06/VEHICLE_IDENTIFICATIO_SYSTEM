package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RankChangeRequest model representing police officer rank change requests.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class RankChangeRequest extends BaseEntity {

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_WITHDRAWN = "WITHDRAWN";

    // Rank constants
    public static final String RANK_CONSTABLE = "CONSTABLE";
    public static final String RANK_CORPORAL = "CORPORAL";
    public static final String RANK_SERGEANT = "SERGEANT";
    public static final String RANK_INSPECTOR = "INSPECTOR";
    public static final String RANK_CHIEF = "CHIEF";

    // Core properties
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
    private final ObjectProperty<LocalDateTime> reviewedAt = new SimpleObjectProperty<>();

    // Computed properties
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public RankChangeRequest() {
        this.status.set(STATUS_PENDING);
        this.createdAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());

        updateStatusDisplay();

        this.status.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    /**
     * Constructor for creating a new rank change request.
     *
     * @param officerId     the officer ID
     * @param officerName   the officer name
     * @param currentRank   the current rank
     * @param requestedRank the requested rank
     * @param reason        the reason for request
     */
    public RankChangeRequest(int officerId, String officerName, String currentRank,
                             String requestedRank, String reason) {
        this();
        this.officerId.set(officerId);
        this.officerName.set(officerName);
        this.currentRank.set(currentRank);
        this.requestedRank.set(requestedRank);
        this.reason.set(reason);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        String currentStatus = status.get();
        switch (currentStatus) {
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                statusColorProperty.set("#FFC107");
                break;
            case STATUS_APPROVED:
                statusDisplayProperty.set("Approved");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_REJECTED:
                statusDisplayProperty.set("Rejected");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_WITHDRAWN:
                statusDisplayProperty.set("Withdrawn");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(currentStatus);
                statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt.get(); }
    public void setReviewedAt(LocalDateTime value) { reviewedAt.set(value); }
    public ObjectProperty<LocalDateTime> reviewedAtProperty() { return reviewedAt; }

    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public String getStatusColor() { return statusColorProperty.get(); }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.get() != null ? createdAt.get().format(formatter) : "";
    }

    public String getFormattedReviewedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return reviewedAt.get() != null ? reviewedAt.get().format(formatter) : "";
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status.get());
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equals(status.get());
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equals(status.get());
    }

    public void approve(String reviewerName, String notes) {
        this.status.set(STATUS_APPROVED);
        this.reviewerName.set(reviewerName);
        this.reviewNotes.set(notes);
        this.reviewedAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
    }

    public void reject(String reviewerName, String notes) {
        this.status.set(STATUS_REJECTED);
        this.reviewerName.set(reviewerName);
        this.reviewNotes.set(notes);
        this.reviewedAt.set(LocalDateTime.now());
        this.updatedAt.set(LocalDateTime.now());
    }

    public void withdraw() {
        this.status.set(STATUS_WITHDRAWN);
        this.updatedAt.set(LocalDateTime.now());
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return officerName.get() + " - " + getCurrentRank() + " → " + getRequestedRank() + " (" + getStatusDisplay() + ")";
    }

    /**
     * Creates a copy of this rank change request.
     *
     * @return a new RankChangeRequest instance
     */
    public RankChangeRequest copy() {
        RankChangeRequest copy = new RankChangeRequest();
        copy.setId(this.getId());
        copy.setOfficerId(this.getOfficerId());
        copy.setOfficerName(this.getOfficerName());
        copy.setCurrentRank(this.getCurrentRank());
        copy.setRequestedRank(this.getRequestedRank());
        copy.setReason(this.getReason());
        copy.setStatus(this.getStatus());
        copy.setReviewedBy(this.getReviewedBy());
        copy.setReviewerName(this.getReviewerName());
        copy.setReviewNotes(this.getReviewNotes());
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        copy.setReviewedAt(this.getReviewedAt());
        return copy;
    }
}