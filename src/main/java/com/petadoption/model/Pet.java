package com.petadoption.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Pet Entity Class (POJO)
 * 
 * Represents a pet available for adoption in the system.
 * Contains all relevant pet information including health, status, and adoption details.
 * 
 * Encapsulation Principles Applied:
 * - All fields are private
 * - Public getters and setters for controlled access
 * - Validation in setters to ensure data integrity
 * - Immutable pet_id after creation
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class Pet {
    
    // Enumerations for pet attributes
    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        Gender(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum VaccinationStatus {
        NOT_VACCINATED("Not Vaccinated"),
        PARTIAL("Partial"),
        COMPLETE("Complete");
        
        private final String displayName;
        VaccinationStatus(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AdoptionStatus {
        AVAILABLE("Available"),
        PENDING("Pending"),
        ADOPTED("Adopted"),
        ARCHIVED("Archived");
        
        private final String displayName;
        AdoptionStatus(String displayName) {
            this.displayName = displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Private fields - encapsulated
    private int petId;                     // Unique identifier (auto-generated)
    private int shelterId;                 // Reference to shelter
    private String petName;                // Pet's name
    private String species;                // Type of animal (Dog, Cat, Rabbit, etc.)
    private String breed;                  // Breed information
    private int ageYears;                  // Age in years
    private int ageMonths;                 // Additional months (0-11)
    private Gender gender;                 // Male, Female, or Unknown
    private BigDecimal weightKg;           // Weight in kilograms
    private String color;                  // Coat/body color description
    private String healthStatus;           // Health assessment
    private VaccinationStatus vaccinationStatus; // Vaccination records
    private boolean neuteredSpayed;        // Surgical status
    private String microchipNumber;        // Identification microchip
    private String description;            // Detailed pet description
    private String specialNeeds;           // Special care requirements
    private AdoptionStatus adoptionStatus; // Current status
    private BigDecimal adoptionFee;        // Adoption cost
    private String imageUrl;               // URL to pet's image
    private LocalDate intakeDate;          // Date received at shelter
    private LocalDate adoptionDate;        // Date when adopted
    private LocalDateTime createdAt;       // Record creation timestamp
    private LocalDateTime updatedAt;       // Record last update timestamp
    
    /**
     * Default constructor
     */
    public Pet() {
        this.adoptionStatus = AdoptionStatus.AVAILABLE;
        this.vaccinationStatus = VaccinationStatus.NOT_VACCINATED;
        this.neuteredSpayed = false;
        this.ageYears = 0;
        this.ageMonths = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential pet information
     * 
     * @param petName pet's name
     * @param species type of animal
     * @param gender pet's gender
     * @param shelterId ID of shelter housing the pet
     */
    public Pet(String petName, String species, Gender gender, int shelterId) {
        this();
        this.petName = petName;
        this.species = species;
        this.gender = gender;
        this.shelterId = shelterId;
    }
    
    // ===== Getters and Setters with Encapsulation =====
    
    public int getPetId() {
        return petId;
    }
    
    public void setPetId(int petId) {
        this.petId = petId;
    }
    
    public int getShelterId() {
        return shelterId;
    }
    
    public void setShelterId(int shelterId) {
        if (shelterId <= 0) {
            throw new IllegalArgumentException("Shelter ID must be positive");
        }
        this.shelterId = shelterId;
    }
    
    public String getPetName() {
        return petName;
    }
    
    public void setPetName(String petName) {
        if (petName == null || petName.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be empty");
        }
        this.petName = petName.trim();
    }
    
    public String getSpecies() {
        return species;
    }
    
    public void setSpecies(String species) {
        if (species == null || species.trim().isEmpty()) {
            throw new IllegalArgumentException("Species cannot be empty");
        }
        this.species = species.trim();
    }
    
    public String getBreed() {
        return breed;
    }
    
    public void setBreed(String breed) {
        this.breed = breed;
    }
    
    public int getAgeYears() {
        return ageYears;
    }
    
    public void setAgeYears(int ageYears) {
        if (ageYears < 0) {
            throw new IllegalArgumentException("Age in years cannot be negative");
        }
        this.ageYears = ageYears;
    }
    
    public int getAgeMonths() {
        return ageMonths;
    }
    
    public void setAgeMonths(int ageMonths) {
        if (ageMonths < 0 || ageMonths > 11) {
            throw new IllegalArgumentException("Age in months must be between 0 and 11");
        }
        this.ageMonths = ageMonths;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null");
        }
        this.gender = gender;
    }
    
    public BigDecimal getWeightKg() {
        return weightKg;
    }
    
    public void setWeightKg(BigDecimal weightKg) {
        if (weightKg != null && weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        this.weightKg = weightKg;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getHealthStatus() {
        return healthStatus;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    public VaccinationStatus getVaccinationStatus() {
        return vaccinationStatus;
    }
    
    public void setVaccinationStatus(VaccinationStatus vaccinationStatus) {
        if (vaccinationStatus == null) {
            throw new IllegalArgumentException("Vaccination status cannot be null");
        }
        this.vaccinationStatus = vaccinationStatus;
    }
    
    public boolean isNeuteredSpayed() {
        return neuteredSpayed;
    }
    
    public void setNeuteredSpayed(boolean neuteredSpayed) {
        this.neuteredSpayed = neuteredSpayed;
    }
    
    public String getMicrochipNumber() {
        return microchipNumber;
    }
    
    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSpecialNeeds() {
        return specialNeeds;
    }
    
    public void setSpecialNeeds(String specialNeeds) {
        this.specialNeeds = specialNeeds;
    }
    
    public AdoptionStatus getAdoptionStatus() {
        return adoptionStatus;
    }
    
    public void setAdoptionStatus(AdoptionStatus adoptionStatus) {
        if (adoptionStatus == null) {
            throw new IllegalArgumentException("Adoption status cannot be null");
        }
        this.adoptionStatus = adoptionStatus;
    }
    
    public BigDecimal getAdoptionFee() {
        return adoptionFee;
    }
    
    public void setAdoptionFee(BigDecimal adoptionFee) {
        if (adoptionFee != null && adoptionFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Adoption fee cannot be negative");
        }
        this.adoptionFee = adoptionFee;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public LocalDate getIntakeDate() {
        return intakeDate;
    }
    
    public void setIntakeDate(LocalDate intakeDate) {
        this.intakeDate = intakeDate;
    }
    
    public LocalDate getAdoptionDate() {
        return adoptionDate;
    }
    
    public void setAdoptionDate(LocalDate adoptionDate) {
        this.adoptionDate = adoptionDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Get pet's age as a human-readable string
     * @return formatted age (e.g., "2 years 3 months")
     */
    public String getFormattedAge() {
        if (ageYears == 0 && ageMonths == 0) {
            return "Less than 1 month";
        }
        StringBuilder age = new StringBuilder();
        if (ageYears > 0) {
            age.append(ageYears).append(" year").append(ageYears > 1 ? "s" : "");
        }
        if (ageMonths > 0) {
            if (ageYears > 0) age.append(" ");
            age.append(ageMonths).append(" month").append(ageMonths > 1 ? "s" : "");
        }
        return age.toString();
    }
    
    /**
     * Generate string representation of Pet
     * @return formatted pet information
     */
    @Override
    public String toString() {
        return "Pet{" +
                "petId=" + petId +
                ", petName='" + petName + '\'' +
                ", species='" + species + '\'' +
                ", breed='" + breed + '\'' +
                ", age=" + getFormattedAge() +
                ", gender=" + gender +
                ", adoptionStatus=" + adoptionStatus +
                '}';
    }
}
