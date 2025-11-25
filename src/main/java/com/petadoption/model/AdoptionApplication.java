package com.petadoption.model;

import java.time.LocalDateTime;

/**
 * Adoption Application Entity Class (POJO)
 * 
 * Represents an adoption application submitted by an adopter for a specific pet.
 * Tracks the application status, adopter information, and approval details.
 * 
 * Encapsulation Principles Applied:
 * - All fields are private
 * - Public getters and setters for controlled access
 * - Validation in setters to ensure data integrity
 * - Immutable application_id after creation
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class AdoptionApplication {
    
    // Enumeration for application status
    public enum ApplicationStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        WITHDRAWN("Withdrawn");
        
        private final String displayName;
        
        ApplicationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Private fields - encapsulated
    private int applicationId;             // Unique identifier (auto-generated)
    private int petId;                     // Reference to pet being applied for
    private int adopterId;                 // Reference to adopter (from adopter_info)
    private LocalDateTime applicationDate; // When application was submitted
    private ApplicationStatus status;      // Current status of application
    private String applicationText;        // Detailed application message
    private String reasonForAdoption;      // Why adopter wants this specific pet
    private int householdMembers;          // Number of people in household
    private boolean hasYard;               // Whether home has outdoor space
    private String yardType;               // Type of yard (fenced, unfenced, etc.)
    private String previousPetExperience;  // Adopter's previous experience
    private String veterinaryReference;    // Contact of current/previous vet
    private String personalReference;      // Non-family personal reference
    private LocalDateTime approvalDate;    // When application was reviewed
    private String approvalNotes;          // Notes from reviewer
    private int reviewedBy;                // User ID of reviewer (admin/shelter staff)
    private LocalDateTime createdAt;       // Record creation timestamp
    private LocalDateTime updatedAt;       // Record last update timestamp
    
    /**
     * Default constructor
     * Initializes application with default values
     */
    public AdoptionApplication() {
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential application information
     * 
     * @param petId ID of the pet being applied for
     * @param adopterId ID of the adopter
     * @param applicationText adoption application details
     */
    public AdoptionApplication(int petId, int adopterId, String applicationText) {
        this();
        this.petId = petId;
        this.adopterId = adopterId;
        this.applicationText = applicationText;
    }
    
    // ===== Getters and Setters with Encapsulation =====
    
    /**
     * Get application ID
     * @return unique application identifier
     */
    public int getApplicationId() {
        return applicationId;
    }
    
    /**
     * Set application ID (typically called after database insertion)
     * @param applicationId auto-generated application ID
     */
    protected void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }
    
    /**
     * Get pet ID
     * @return reference to pet being applied for
     */
    public int getPetId() {
        return petId;
    }
    
    /**
     * Set pet ID
     * @param petId ID of pet this application references
     */
    public void setPetId(int petId) {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        this.petId = petId;
    }
    
    /**
     * Get adopter ID
     * @return reference to adopter
     */
    public int getAdopterId() {
        return adopterId;
    }
    
    /**
     * Set adopter ID
     * @param adopterId ID of adopter making application
     */
    public void setAdopterId(int adopterId) {
        if (adopterId <= 0) {
            throw new IllegalArgumentException("Adopter ID must be positive");
        }
        this.adopterId = adopterId;
    }
    
    /**
     * Get application submission date
     * @return timestamp of when application was submitted
     */
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    /**
     * Get application status
     * @return current status (PENDING, APPROVED, REJECTED, WITHDRAWN)
     */
    public ApplicationStatus getStatus() {
        return status;
    }
    
    /**
     * Set application status
     * @param status the new status
     */
    public void setStatus(ApplicationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Application status cannot be null");
        }
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get full application text
     * @return detailed application information
     */
    public String getApplicationText() {
        return applicationText;
    }
    
    /**
     * Set application text
     * @param applicationText the application details
     */
    public void setApplicationText(String applicationText) {
        if (applicationText != null && applicationText.trim().isEmpty()) {
            throw new IllegalArgumentException("Application text cannot be empty");
        }
        this.applicationText = applicationText;
    }
    
    /**
     * Get reason for adoption
     * @return why adopter wants this specific pet
     */
    public String getReasonForAdoption() {
        return reasonForAdoption;
    }
    
    /**
     * Set reason for adoption
     * @param reasonForAdoption motivation for adopting this pet
     */
    public void setReasonForAdoption(String reasonForAdoption) {
        this.reasonForAdoption = reasonForAdoption;
    }
    
    /**
     * Get household member count
     * @return number of people in adopter's household
     */
    public int getHouseholdMembers() {
        return householdMembers;
    }
    
    /**
     * Set household member count
     * @param householdMembers number of household members
     */
    public void setHouseholdMembers(int householdMembers) {
        if (householdMembers < 1) {
            throw new IllegalArgumentException("Household members must be at least 1");
        }
        this.householdMembers = householdMembers;
    }
    
    /**
     * Check if home has yard
     * @return true if property has yard space
     */
    public boolean isHasYard() {
        return hasYard;
    }
    
    /**
     * Set whether home has yard
     * @param hasYard true if property has outdoor space
     */
    public void setHasYard(boolean hasYard) {
        this.hasYard = hasYard;
    }
    
    /**
     * Get yard type description
     * @return description of yard type (fenced, unfenced, etc.)
     */
    public String getYardType() {
        return yardType;
    }
    
    /**
     * Set yard type
     * @param yardType type of yard available
     */
    public void setYardType(String yardType) {
        this.yardType = yardType;
    }
    
    /**
     * Get previous pet experience
     * @return adopter's history with pet ownership
     */
    public String getPreviousPetExperience() {
        return previousPetExperience;
    }
    
    /**
     * Set previous pet experience
     * @param previousPetExperience description of pet ownership history
     */
    public void setPreviousPetExperience(String previousPetExperience) {
        this.previousPetExperience = previousPetExperience;
    }
    
    /**
     * Get veterinary reference
     * @return veterinarian contact information
     */
    public String getVeterinaryReference() {
        return veterinaryReference;
    }
    
    /**
     * Set veterinary reference
     * @param veterinaryReference vet contact for verification
     */
    public void setVeterinaryReference(String veterinaryReference) {
        this.veterinaryReference = veterinaryReference;
    }
    
    /**
     * Get personal reference
     * @return non-family personal reference contact
     */
    public String getPersonalReference() {
        return personalReference;
    }
    
    /**
     * Set personal reference
     * @param personalReference personal reference contact
     */
    public void setPersonalReference(String personalReference) {
        this.personalReference = personalReference;
    }
    
    /**
     * Get approval date
     * @return when application was reviewed and decided
     */
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    /**
     * Set approval date
     * @param approvalDate timestamp of approval/rejection
     */
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    /**
     * Get approval notes
     * @return reviewer's comments on the application
     */
    public String getApprovalNotes() {
        return approvalNotes;
    }
    
    /**
     * Set approval notes
     * @param approvalNotes reviewer comments and feedback
     */
    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }
    
    /**
     * Get reviewer user ID
     * @return ID of admin/staff who reviewed application
     */
    public int getReviewedBy() {
        return reviewedBy;
    }
    
    /**
     * Set reviewer user ID
     * @param reviewedBy ID of user approving/rejecting application
     */
    public void setReviewedBy(int reviewedBy) {
        if (reviewedBy < 0) {
            throw new IllegalArgumentException("Reviewer ID must be non-negative");
        }
        this.reviewedBy = reviewedBy;
    }
    
    /**
     * Get record creation timestamp
     * @return when this record was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Get record update timestamp
     * @return when this record was last modified
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set record update timestamp
     * @param updatedAt latest modification time
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if application is still being processed
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return status == ApplicationStatus.PENDING;
    }
    
    /**
     * Check if application was approved
     * @return true if status is APPROVED
     */
    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }
    
    /**
     * Generate string representation of AdoptionApplication
     * @return formatted application information
     */
    @Override
    public String toString() {
        return "AdoptionApplication{" +
                "applicationId=" + applicationId +
                ", petId=" + petId +
                ", adopterId=" + adopterId +
                ", status=" + status +
                ", applicationDate=" + applicationDate +
                ", approvalDate=" + approvalDate +
                '}';
    }
}
