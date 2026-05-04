package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all model entities.
 * Provides common fields like id, createdAt, updatedAt.
 * All model classes should extend this class.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public abstract class BaseEntity {

    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Date formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Default constructor - initializes timestamps to current time.
     */
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with custom timestamps.
     *
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public BaseEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    /**
     * Gets the entity ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the entity ID.
     *
     * @param id the ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the last update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Updates the updatedAt timestamp to the current time.
     * Should be called before saving changes to the entity.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the formatted creation date.
     *
     * @return formatted date string (dd/MM/yyyy)
     */
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_FORMATTER) : "";
    }

    /**
     * Gets the formatted creation date-time.
     *
     * @return formatted date-time string (dd/MM/yyyy HH:mm:ss)
     */
    public String getFormattedCreatedDateTime() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Gets the formatted last update date.
     *
     * @return formatted date string (dd/MM/yyyy)
     */
    public String getFormattedUpdatedAt() {
        return updatedAt != null ? updatedAt.format(DATE_FORMATTER) : "";
    }

    /**
     * Gets the formatted last update date-time.
     *
     * @return formatted date-time string (dd/MM/yyyy HH:mm:ss)
     */
    public String getFormattedUpdatedDateTime() {
        return updatedAt != null ? updatedAt.format(DATE_TIME_FORMATTER) : "";
    }

    /**
     * Checks if the entity is new (has no ID set).
     *
     * @return true if ID is 0 or less, false otherwise
     */
    public boolean isNew() {
        return id <= 0;
    }

    /**
     * Checks if two entities are equal by ID.
     *
     * @param other the other entity
     * @return true if IDs are equal and both non-zero
     */
    public boolean sameAs(BaseEntity other) {
        return other != null && this.id > 0 && this.id == other.id;
    }

    // ============================================
    // ABSTRACT METHODS
    // ============================================

    /**
     * Returns a string representation of the entity.
     * Must be implemented by subclasses.
     *
     * @return string representation
     */
    @Override
    public abstract String toString();
}