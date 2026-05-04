package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * NoClaimBonusRecord model representing no-claim bonus history for insurance policies.
 * Tracks claim-free years and applicable discounts.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class NoClaimBonusRecord extends BaseEntity {

    // Core fields
    private int id;
    private int insurancePolicyId;
    private String policyNumber;
    private int policyYear;
    private int claimFreeYears;
    private double bonusPercentage;
    private LocalDate calculatedDate;

    // Bonus percentage thresholds
    public static final double BONUS_1_YEAR = 10.0;
    public static final double BONUS_2_YEARS = 20.0;
    public static final double BONUS_3_YEARS = 30.0;
    public static final double BONUS_4_YEARS = 40.0;
    public static final double BONUS_5_PLUS_YEARS = 50.0;

    // JavaFX Properties
    private final IntegerProperty insurancePolicyIdProperty = new SimpleIntegerProperty();
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final IntegerProperty policyYearProperty = new SimpleIntegerProperty();
    private final IntegerProperty claimFreeYearsProperty = new SimpleIntegerProperty();
    private final DoubleProperty bonusPercentageProperty = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> calculatedDateProperty = new SimpleObjectProperty<>();
    private final StringProperty bonusPercentageDisplayProperty = new SimpleStringProperty();
    private final StringProperty discountMultiplierProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public NoClaimBonusRecord() {
        super();
        this.calculatedDate = LocalDate.now();

        calculatedDateProperty.set(calculatedDate);
    }

    /**
     * Constructor for creating a new NCB record.
     *
     * @param insurancePolicyId the insurance policy ID
     * @param policyYear        the policy year
     * @param claimFreeYears    number of claim-free years
     * @param bonusPercentage   the bonus percentage
     * @param calculatedDate    the calculation date
     */
    public NoClaimBonusRecord(int insurancePolicyId, int policyYear, int claimFreeYears,
                              double bonusPercentage, LocalDate calculatedDate) {
        this();
        this.insurancePolicyId = insurancePolicyId;
        this.policyYear = policyYear;
        this.claimFreeYears = claimFreeYears;
        this.bonusPercentage = bonusPercentage;
        this.calculatedDate = calculatedDate;

        insurancePolicyIdProperty.set(insurancePolicyId);
        policyYearProperty.set(policyYear);
        claimFreeYearsProperty.set(claimFreeYears);
        bonusPercentageProperty.set(bonusPercentage);
        calculatedDateProperty.set(calculatedDate);
        updateDisplayProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDisplayProperties() {
        bonusPercentageDisplayProperty.set(String.format("%.0f%%", bonusPercentage));
        discountMultiplierProperty.set(String.format("%.2f", getDiscountMultiplier()));
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getInsurancePolicyId() {
        return insurancePolicyId;
    }

    public void setInsurancePolicyId(int insurancePolicyId) {
        this.insurancePolicyId = insurancePolicyId;
        insurancePolicyIdProperty.set(insurancePolicyId);
    }

    public IntegerProperty insurancePolicyIdProperty() {
        return insurancePolicyIdProperty;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        policyNumberProperty.set(policyNumber);
    }

    public StringProperty policyNumberProperty() {
        return policyNumberProperty;
    }

    public int getPolicyYear() {
        return policyYear;
    }

    public void setPolicyYear(int policyYear) {
        this.policyYear = policyYear;
        policyYearProperty.set(policyYear);
    }

    public IntegerProperty policyYearProperty() {
        return policyYearProperty;
    }

    public int getClaimFreeYears() {
        return claimFreeYears;
    }

    public void setClaimFreeYears(int claimFreeYears) {
        this.claimFreeYears = claimFreeYears;
        claimFreeYearsProperty.set(claimFreeYears);
        updateDisplayProperties();
    }

    public IntegerProperty claimFreeYearsProperty() {
        return claimFreeYearsProperty;
    }

    public double getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(double bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
        bonusPercentageProperty.set(bonusPercentage);
        updateDisplayProperties();
    }

    public DoubleProperty bonusPercentageProperty() {
        return bonusPercentageProperty;
    }

    public LocalDate getCalculatedDate() {
        return calculatedDate;
    }

    public void setCalculatedDate(LocalDate calculatedDate) {
        this.calculatedDate = calculatedDate;
        calculatedDateProperty.set(calculatedDate);
    }

    public ObjectProperty<LocalDate> calculatedDateProperty() {
        return calculatedDateProperty;
    }

    public StringProperty bonusPercentageDisplayProperty() {
        return bonusPercentageDisplayProperty;
    }

    public StringProperty discountMultiplierProperty() {
        return discountMultiplierProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the discount multiplier (1 - bonusPercentage/100).
     *
     * @return discount multiplier
     */
    public double getDiscountMultiplier() {
        return 1 - (bonusPercentage / 100);
    }

    public String getBonusPercentageDisplay() {
        return bonusPercentageDisplayProperty.get();
    }

    public String getDiscountMultiplierDisplay() {
        return discountMultiplierProperty.get();
    }

    public String getFormattedCalculatedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return calculatedDate != null ? calculatedDate.format(formatter) : "";
    }

    public String getBonusLevel() {
        if (claimFreeYears >= 5) return "Maximum (5+ years)";
        if (claimFreeYears >= 4) return "Excellent (4 years)";
        if (claimFreeYears >= 3) return "Good (3 years)";
        if (claimFreeYears >= 2) return "Standard (2 years)";
        if (claimFreeYears >= 1) return "Basic (1 year)";
        return "None";
    }

    /**
     * Calculates the bonus percentage based on claim-free years.
     *
     * @param years claim-free years
     * @return bonus percentage
     */
    public static double calculateBonusPercentage(int years) {
        if (years >= 5) return BONUS_5_PLUS_YEARS;
        if (years >= 4) return BONUS_4_YEARS;
        if (years >= 3) return BONUS_3_YEARS;
        if (years >= 2) return BONUS_2_YEARS;
        if (years >= 1) return BONUS_1_YEAR;
        return 0;
    }

    /**
     * Calculates the premium after applying NCB discount.
     *
     * @param basePremium the base premium
     * @return discounted premium
     */
    public double calculateDiscountedPremium(double basePremium) {
        return basePremium * getDiscountMultiplier();
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
        return "Policy " + policyNumber + " - " + claimFreeYears + " claim-free years - " + getBonusPercentageDisplay() + " bonus";
    }

    /**
     * Creates a copy of this NCB record.
     *
     * @return a new NoClaimBonusRecord instance
     */
    public NoClaimBonusRecord copy() {
        NoClaimBonusRecord copy = new NoClaimBonusRecord();
        copy.setId(this.id);
        copy.setInsurancePolicyId(this.insurancePolicyId);
        copy.setPolicyNumber(this.policyNumber);
        copy.setPolicyYear(this.policyYear);
        copy.setClaimFreeYears(this.claimFreeYears);
        copy.setBonusPercentage(this.bonusPercentage);
        copy.setCalculatedDate(this.calculatedDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}