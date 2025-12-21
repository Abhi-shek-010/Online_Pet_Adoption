package com.petadoption.service;

import com.petadoption.dao.PetDAO;
import com.petadoption.model.Pet;
import com.petadoption.model.Pet.AdoptionStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pet Service Layer
 * 
 * Handles business logic for pet-related operations.
 * Provides filtering,search, and validation logic separated from servlets.
 * 
 * MVC Separation:
 * - Servlets handle HTTP requests/responses
 * - Service handles business logic and filtering
 * - DAO handles database operations
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class PetService {

    private final PetDAO petDAO;

    /**
     * Default constructor initializing DAO dependency
     */
    public PetService() {
        this.petDAO = new PetDAO();
    }

    /**
     * Retrieves all available pets
     * Business logic: Only returns pets with AVAILABLE status
     * 
     * @return List of available pets
     * @throws SQLException if database operation fails
     */
    public List<Pet> getAllAvailablePets() throws SQLException {
        return petDAO.getAllAvailablePets();
    }

    /**
     * Searches and filters pets based on multiple criteria
     * 
     * Business logic layer that handles:
     * - Search term filtering
     * - Status filtering
     * - Species filtering
     * 
     * @param searchTerm search keyword (can be null)
     * @param status     adoption status filter (can be null)
     * @param species    species filter (can be null)
     * @return List of pets matching all criteria
     * @throws SQLException if database operation fails
     */
    public List<Pet> searchAndFilterPets(String searchTerm, String status, String species) throws SQLException {
        List<Pet> pets;

        // Start with appropriate base query
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            pets = petDAO.searchPets(searchTerm);
        } else if (status != null && !status.trim().isEmpty()) {
            try {
                AdoptionStatus adoptionStatus = AdoptionStatus.valueOf(status.toUpperCase());
                pets = petDAO.getPetsByStatus(adoptionStatus);
            } catch (IllegalArgumentException e) {
                System.err.println("✗ Invalid status value: " + status);
                pets = petDAO.getAllAvailablePets();
            }
        } else if (species != null && !species.trim().isEmpty()) {
            pets = petDAO.getPetsBySpecies(species);
        } else {
            pets = petDAO.getAllAvailablePets();
        }

        // Apply additional filtering if needed
        if (species != null && !species.trim().isEmpty() && searchTerm != null) {
            final String filterSpecies = species;
            pets = pets.stream()
                    .filter(pet -> pet.getSpecies().equalsIgnoreCase(filterSpecies))
                    .collect(Collectors.toList());
        }

        return pets;
    }

    /**
     * Retrieves pets for a specific shelter
     * 
     * @param shelterId unique shelter identifier
     * @return List of pets belonging to the shelter
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsByShelterId(int shelterId) throws SQLException {
        if (shelterId <= 0) {
            throw new IllegalArgumentException("Shelter ID must be positive");
        }

        return petDAO.getPetsByShelterId(shelterId);
    }

    /**
     * Retrieves a specific pet by ID
     * 
     * @param petId unique pet identifier
     * @return Pet object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Pet getPetById(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }

        return petDAO.getPetById(petId);
    }

    /**
     * Creates a new pet with validation
     * 
     * Business logic:
     * - Validates required fields
     * - Ensures default values
     * - Checks shelter authorization (can be added)
     * 
     * @param pet       Pet object to create
     * @param shelterId ID of shelter creating the pet
     * @return true if creation successful
     * @throws SQLException if database operation fails
     */
    public boolean createPet(Pet pet, int shelterId) throws SQLException {
        if (pet == null) {
            throw new IllegalArgumentException("Pet cannot be null");
        }

        // Validate required fields
        validatePetData(pet);

        // Set shelter ID
        pet.setShelterId(shelterId);

        // Set default status if not set
        if (pet.getAdoptionStatus() == null) {
            pet.setAdoptionStatus(AdoptionStatus.AVAILABLE);
        }

        return petDAO.createPet(pet);
    }

    /**
     * Updates an existing pet with validation
     * 
     * @param pet Pet object with updated data
     * @return true if update successful
     * @throws SQLException if database operation fails
     */
    public boolean updatePet(Pet pet) throws SQLException {
        if (pet == null || pet.getPetId() <= 0) {
            throw new IllegalArgumentException("Valid pet with ID required");
        }

        // Validate updated data
        validatePetData(pet);

        return petDAO.updatePet(pet);
    }

    /**
     * Deletes a pet
     * 
     * Business logic: Could add soft delete or status check
     * 
     * @param petId unique pet identifier
     * @return true if deletion successful
     * @throws SQLException if database operation fails
     */
    public boolean deletePet(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }

        // Business logic: Check if pet can be deleted
        Pet pet = petDAO.getPetById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found");
        }

        if (pet.getAdoptionStatus() == AdoptionStatus.ADOPTED) {
            throw new IllegalStateException("Cannot delete adopted pets");
        }

        return petDAO.deletePet(petId);
    }

    /**
     * Counts available pets for a shelter
     * Useful for capacity management
     * 
     * @param shelterId unique shelter identifier
     * @return count of available pets
     * @throws SQLException if database operation fails
     */
    public int getAvailablePetCount(int shelterId) throws SQLException {
        if (shelterId <= 0) {
            throw new IllegalArgumentException("Shelter ID must be positive");
        }

        return petDAO.countAvailablePetsByShelterId(shelterId);
    }

    /**
     * Retrieves pets by species
     * 
     * @param species type of animal
     * @return List of pets of the specified species
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsBySpecies(String species) throws SQLException {
        if (species == null || species.trim().isEmpty()) {
            throw new IllegalArgumentException("Species cannot be null or empty");
        }

        return petDAO.getPetsBySpecies(species);
    }

    /**
     * Validates pet data
     * Business logic validation before database operations
     * 
     * @param pet Pet object to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePetData(Pet pet) throws IllegalArgumentException {
        if (pet.getPetName() == null || pet.getPetName().trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name is required");
        }

        if (pet.getSpecies() == null || pet.getSpecies().trim().isEmpty()) {
            throw new IllegalArgumentException("Species is required");
        }

        if (pet.getGender() == null) {
            throw new IllegalArgumentException("Gender is required");
        }

        if (pet.getAgeYears() < 0) {
            throw new IllegalArgumentException("Age years cannot be negative");
        }

        if (pet.getAgeMonths() < 0 || pet.getAgeMonths() > 11) {
            throw new IllegalArgumentException("Age months must be between 0 and 11");
        }

        // Additional validation rules can be added here
    }

    /**
     * Checks if a pet is available for adoption
     * 
     * @param petId unique pet identifier
     * @return true if pet is available
     * @throws SQLException if database operation fails
     */
    public boolean isPetAvailable(int petId) throws SQLException {
        Pet pet = petDAO.getPetById(petId);
        return pet != null && pet.getAdoptionStatus() == AdoptionStatus.AVAILABLE;
    }
}
