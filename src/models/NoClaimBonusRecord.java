package models;

import java.time.LocalDate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NoClaimBonusRecord extends BaseEntity {
    private int id;
    private int insurancePolicyId;
    private String policyNumber;
    private int policyYear;
    private int claimFreeYears;
    private double bonusPercentage;
    private LocalDate calculatedDate;

    // JavaFX Properties
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final IntegerProperty policyYearProperty = new SimpleIntegerProperty();
    private final IntegerProperty claimFreeYearsProperty = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> calculatedDateProperty = new SimpleObjectProperty<>();
    private final StringProperty bonusPercentageProperty = new SimpleStringProperty();  // StringProperty for display

    public NoClaimBonusRecord() {
        super();
    }

    public NoClaimBonusRecord(int insurancePolicyId, int policyYear, int claimFreeYears, double bonusPercentage, LocalDate calculatedDate) {
        this();
        this.insurancePolicyId = insurancePolicyId;
        this.policyYear = policyYear;
        this.claimFreeYears = claimFreeYears;
        this.bonusPercentage = bonusPercentage;
        this.calculatedDate = calculatedDate;

        this.policyYearProperty.set(policyYear);
        this.claimFreeYearsProperty.set(claimFreeYears);
        this.calculatedDateProperty.set(calculatedDate);
        this.bonusPercentageProperty.set(String.format("%.0f%%", bonusPercentage));
    }

    public int getInsurancePolicyId() { return insurancePolicyId; }
    public void setInsurancePolicyId(int insurancePolicyId) { this.insurancePolicyId = insurancePolicyId; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; policyNumberProperty.set(policyNumber); }
    public StringProperty policyNumberProperty() { return policyNumberProperty; }

    public int getPolicyYear() { return policyYear; }
    public void setPolicyYear(int policyYear) { this.policyYear = policyYear; policyYearProperty.set(policyYear); }
    public IntegerProperty policyYearProperty() { return policyYearProperty; }

    public int getClaimFreeYears() { return claimFreeYears; }
    public void setClaimFreeYears(int claimFreeYears) { this.claimFreeYears = claimFreeYears; claimFreeYearsProperty.set(claimFreeYears); }
    public IntegerProperty claimFreeYearsProperty() { return claimFreeYearsProperty; }

    public double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(double bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
        bonusPercentageProperty.set(String.format("%.0f%%", bonusPercentage));
    }
    public StringProperty bonusPercentageProperty() { return bonusPercentageProperty; }

    public LocalDate getCalculatedDate() { return calculatedDate; }
    public void setCalculatedDate(LocalDate calculatedDate) { this.calculatedDate = calculatedDate; calculatedDateProperty.set(calculatedDate); }
    public ObjectProperty<LocalDate> calculatedDateProperty() { return calculatedDateProperty; }

    public double getDiscountMultiplier() { return 1 - (bonusPercentage / 100); }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Policy " + policyNumber + " - " + claimFreeYears + " claim-free years - " + bonusPercentage + "% bonus";
    }
}