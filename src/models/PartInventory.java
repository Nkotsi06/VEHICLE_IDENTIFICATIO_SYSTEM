package models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * PartInventory model representing parts inventory for workshops.
 * Tracks parts quantity, pricing, and stock status.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PartInventory extends BaseEntity {

    // Core fields
    private int id;
    private int workshopId;
    private String workshopName;
    private String partName;
    private String partNumber;
    private int quantity;
    private int reorderLevel;
    private double unitPrice;
    private String stockStatus;
    private String supplier;
    private String location;

    // Stock status constants
    public static final String STATUS_IN_STOCK = "IN_STOCK";
    public static final String STATUS_LOW_STOCK = "LOW_STOCK";
    public static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String STATUS_DISCONTINUED = "DISCONTINUED";

    // Default values
    private static final int DEFAULT_REORDER_LEVEL = 10;
    private static final int DEFAULT_QUANTITY = 0;

    // JavaFX Properties for TableView binding
    private final StringProperty partNameProperty = new SimpleStringProperty();
    private final StringProperty partNumberProperty = new SimpleStringProperty();
    private final IntegerProperty quantityProperty = new SimpleIntegerProperty();
    private final IntegerProperty reorderLevelProperty = new SimpleIntegerProperty();
    private final DoubleProperty unitPriceProperty = new SimpleDoubleProperty();
    private final StringProperty stockStatusProperty = new SimpleStringProperty();
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty supplierProperty = new SimpleStringProperty();
    private final StringProperty locationProperty = new SimpleStringProperty();
    private final StringProperty formattedPriceProperty = new SimpleStringProperty();
    private final StringProperty inventoryValueProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with default values.
     */
    public PartInventory() {
        super();
        this.quantity = DEFAULT_QUANTITY;
        this.reorderLevel = DEFAULT_REORDER_LEVEL;
        this.stockStatus = STATUS_OUT_OF_STOCK;

        quantityProperty.set(DEFAULT_QUANTITY);
        reorderLevelProperty.set(DEFAULT_REORDER_LEVEL);
        stockStatusProperty.set(STATUS_OUT_OF_STOCK);
        updateStatusDisplay();
        updateDerivedProperties();

        quantityProperty.addListener((obs, oldVal, newVal) -> {
            updateStockStatus();
            updateDerivedProperties();
        });
        unitPriceProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
    }

    /**
     * Constructor for creating a new part inventory item.
     *
     * @param workshopId   the workshop ID
     * @param partName     the part name
     * @param partNumber   the part number
     * @param quantity     the quantity in stock
     * @param reorderLevel the reorder level
     * @param unitPrice    the unit price
     */
    public PartInventory(int workshopId, String partName, String partNumber,
                         int quantity, int reorderLevel, double unitPrice) {
        this();
        this.workshopId = workshopId;
        this.partName = partName;
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitPrice = unitPrice;

        partNameProperty.set(partName);
        partNumberProperty.set(partNumber);
        quantityProperty.set(quantity);
        reorderLevelProperty.set(reorderLevel);
        unitPriceProperty.set(unitPrice);
        updateStockStatus();
        updateDerivedProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStockStatus() {
        if (quantity <= 0) {
            this.stockStatus = STATUS_OUT_OF_STOCK;
        } else if (quantity <= reorderLevel) {
            this.stockStatus = STATUS_LOW_STOCK;
        } else {
            this.stockStatus = STATUS_IN_STOCK;
        }
        stockStatusProperty.set(this.stockStatus);
        updateStatusDisplay();
    }

    private void updateStatusDisplay() {
        switch (stockStatus) {
            case STATUS_IN_STOCK:
                statusDisplayProperty.set("In Stock");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_LOW_STOCK:
                statusDisplayProperty.set("Low Stock");
                statusColorProperty.set("#FF9800");
                break;
            case STATUS_OUT_OF_STOCK:
                statusDisplayProperty.set("Out of Stock");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_DISCONTINUED:
                statusDisplayProperty.set("Discontinued");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(stockStatus);
                statusColorProperty.set("#9E9E9E");
        }
    }

    private void updateDerivedProperties() {
        formattedPriceProperty.set(String.format("M%,.2f", unitPrice));
        inventoryValueProperty.set(String.format("M%,.2f", getInventoryValue()));
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
        workshopNameProperty.set(workshopName);
    }

    public StringProperty workshopNameProperty() {
        return workshopNameProperty;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
        partNameProperty.set(partName);
    }

    public StringProperty partNameProperty() {
        return partNameProperty;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
        partNumberProperty.set(partNumber);
    }

    public StringProperty partNumberProperty() {
        return partNumberProperty;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        quantityProperty.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantityProperty;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
        reorderLevelProperty.set(reorderLevel);
        updateStockStatus();
    }

    public IntegerProperty reorderLevelProperty() {
        return reorderLevelProperty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        unitPriceProperty.set(unitPrice);
    }

    public DoubleProperty unitPriceProperty() {
        return unitPriceProperty;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
        stockStatusProperty.set(stockStatus);
        updateStatusDisplay();
    }

    public StringProperty stockStatusProperty() {
        return stockStatusProperty;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
        supplierProperty.set(supplier);
    }

    public StringProperty supplierProperty() {
        return supplierProperty;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        locationProperty.set(location);
    }

    public StringProperty locationProperty() {
        return locationProperty;
    }

    public String getFormattedPrice() {
        return formattedPriceProperty.get();
    }

    public StringProperty formattedPriceProperty() {
        return formattedPriceProperty;
    }

    public String getInventoryValue() {
        return inventoryValueProperty.get();
    }

    public StringProperty inventoryValueProperty() {
        return inventoryValueProperty;
    }

    public String getStatusDisplay() {
        return statusDisplayProperty.get();
    }

    public StringProperty statusDisplayProperty() {
        return statusDisplayProperty;
    }

    public String getStatusColor() {
        return statusColorProperty.get();
    }

    public StringProperty statusColorProperty() {
        return statusColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public double calculateInventoryValue() {
        return quantity * unitPrice;
    }

    public boolean isLowStock() {
        return STATUS_LOW_STOCK.equals(stockStatus);
    }

    public boolean isOutOfStock() {
        return STATUS_OUT_OF_STOCK.equals(stockStatus);
    }

    public boolean isInStock() {
        return STATUS_IN_STOCK.equals(stockStatus);
    }

    public boolean needsReorder() {
        return quantity <= reorderLevel;
    }

    public void addStock(int amount) {
        if (amount > 0) {
            this.quantity += amount;
            quantityProperty.set(this.quantity);
        }
    }

    public void removeStock(int amount) {
        if (amount > 0 && amount <= quantity) {
            this.quantity -= amount;
            quantityProperty.set(this.quantity);
        }
    }

    public void updatePrice(double newPrice) {
        if (newPrice > 0) {
            this.unitPrice = newPrice;
            unitPriceProperty.set(newPrice);
        }
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return partName + " - Qty: " + quantity + " - " + getFormattedPrice();
    }

    /**
     * Creates a copy of this part inventory.
     *
     * @return a new PartInventory instance
     */
    public PartInventory copy() {
        PartInventory copy = new PartInventory();
        copy.setId(this.id);
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setPartName(this.partName);
        copy.setPartNumber(this.partNumber);
        copy.setQuantity(this.quantity);
        copy.setReorderLevel(this.reorderLevel);
        copy.setUnitPrice(this.unitPrice);
        copy.setStockStatus(this.stockStatus);
        copy.setSupplier(this.supplier);
        copy.setLocation(this.location);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}