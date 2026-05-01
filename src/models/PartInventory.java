package models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PartInventory extends BaseEntity {
    private int id;
    private int workshopId;
    private String workshopName;
    private String partName;
    private String partNumber;
    private int quantity;
    private int reorderLevel;
    private double unitPrice;
    private String stockStatus;

    // JavaFX Properties for TableView binding
    private final StringProperty partNameProperty = new SimpleStringProperty();
    private final StringProperty partNumberProperty = new SimpleStringProperty();
    private final IntegerProperty quantityProperty = new SimpleIntegerProperty();
    private final IntegerProperty reorderLevelProperty = new SimpleIntegerProperty();
    private final DoubleProperty unitPriceProperty = new SimpleDoubleProperty();
    private final StringProperty stockStatusProperty = new SimpleStringProperty();

    public PartInventory() {
        super();
        this.quantity = 0;
        this.reorderLevel = 10;
    }

    public PartInventory(int workshopId, String partName, String partNumber, int quantity, int reorderLevel, double unitPrice) {
        this();
        this.workshopId = workshopId;
        this.partName = partName;
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.unitPrice = unitPrice;

        // Update properties
        partNameProperty.set(partName);
        partNumberProperty.set(partNumber);
        quantityProperty.set(quantity);
        reorderLevelProperty.set(reorderLevel);
        unitPriceProperty.set(unitPrice);
        updateStockStatus();
    }

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
        updateStockStatus();
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
    }

    public StringProperty stockStatusProperty() {
        return stockStatusProperty;
    }

    public void updateStockStatus() {
        if (quantity <= 0) {
            this.stockStatus = "OUT_OF_STOCK";
        } else if (quantity <= reorderLevel) {
            this.stockStatus = "LOW_STOCK";
        } else {
            this.stockStatus = "IN_STOCK";
        }
        stockStatusProperty.set(this.stockStatus);
    }

    public double getInventoryValue() {
        return quantity * unitPrice;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel && quantity > 0;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }

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
        return partName + " - Qty: " + quantity + " - " + utils.CurrencyUtil.format(unitPrice);
    }
}