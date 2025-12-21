package com.petadoption.model;

import java.time.LocalDateTime;

/**
 * Adoption Entity Class (POJO)
 * 
 * Represents a finalized adoption record in the system.
 * Serves as the permanent record of pet ownership.
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class Adoption {

    private int adoptionId;
    private int adopterId;
    private int petId;
    private LocalDateTime adoptionDate;
    private boolean contractSigned;

    // DTO fields (not in table, but useful for display)
    private String petName;
    private String species;
    private String breed;
    private String adopterName; // Full name of the adopter
    private String shelterName; // Name of the shelter that validated the adoption

    public Adoption() {
        this.adoptionDate = LocalDateTime.now();
        this.contractSigned = false;
    }

    public Adoption(int adopterId, int petId, boolean contractSigned) {
        this();
        this.adopterId = adopterId;
        this.petId = petId;
        this.contractSigned = contractSigned;
    }

    public int getAdoptionId() {
        return adoptionId;
    }

    public void setAdoptionId(int adoptionId) {
        this.adoptionId = adoptionId;
    }

    public int getAdopterId() {
        return adopterId;
    }

    public void setAdopterId(int adopterId) {
        this.adopterId = adopterId;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public LocalDateTime getAdoptionDate() {
        return adoptionDate;
    }

    public void setAdoptionDate(LocalDateTime adoptionDate) {
        this.adoptionDate = adoptionDate;
    }

    public boolean isContractSigned() {
        return contractSigned;
    }

    public void setContractSigned(boolean contractSigned) {
        this.contractSigned = contractSigned;
    }

    // Getters and Setters for DTO fields

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getAdopterName() {
        return adopterName;
    }

    public void setAdopterName(String adopterName) {
        this.adopterName = adopterName;
    }

    public String getShelterName() {
        return shelterName;
    }

    public void setShelterName(String shelterName) {
        this.shelterName = shelterName;
    }

    @Override
    public String toString() {
        return "Adoption{" +
                "adoptionId=" + adoptionId +
                ", adopterId=" + adopterId +
                ", petId=" + petId +
                ", adoptionDate=" + adoptionDate +
                '}';
    }
}
