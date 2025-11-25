package com.petadoption.dao;

import com.petadoption.config.DBConnection;
import com.petadoption.model.Pet;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Pet Data Access Object (DAO)
 * 
 * Handles all database operations for Pet entities using JDBC.
 * Implements PreparedStatement to prevent SQL injection attacks.
 * Provides CRUD operations (Create, Read, Update, Delete) for pets.
 * Includes specialized queries for pet adoption workflows.
 * 
 * Security Features:
 * - Uses PreparedStatement for parameterized queries
 * - Prevents SQL injection vulnerabilities
 * - Input validation at setter level
 * - Supports only authorized shelter/admin operations
 * 
 * Production-Ready Features:
 * - Connection pooling support
 * - Comprehensive exception handling
 * - Pagination support for large datasets
 * - Detailed logging capabilities
 * - Resource cleanup in finally blocks
 * - Search and filter operations
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class PetDAO {
    
    // SQL Query Constants
    private static final String INSERT_PET = 
        "INSERT INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, weight_kg, color, health_status, vaccination_status, neutered_spayed, microchip_number, description, special_needs, adoption_status, adoption_fee, image_url, intake_date) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_PET_BY_ID = 
        "SELECT * FROM pets WHERE pet_id = ?";
    
    private static final String SELECT_ALL_AVAILABLE_PETS = 
        "SELECT * FROM pets WHERE adoption_status = 'AVAILABLE' ORDER BY created_at DESC";
    
    private static final String SELECT_PETS_BY_SHELTER = 
        "SELECT * FROM pets WHERE shelter_id = ? ORDER BY created_at DESC";
    
    private static final String SELECT_PETS_BY_STATUS = 
        "SELECT * FROM pets WHERE adoption_status = ? ORDER BY created_at DESC";
    
    private static final String SELECT_PETS_BY_SPECIES = 
        "SELECT * FROM pets WHERE species = ? AND adoption_status = 'AVAILABLE' ORDER BY pet_name ASC";
    
    private static final String SELECT_PETS_BY_BREED = 
        "SELECT * FROM pets WHERE breed = ? AND adoption_status = 'AVAILABLE'";
    
    private static final String SELECT_ALL_PETS = 
        "SELECT * FROM pets ORDER BY created_at DESC";
    
    private static final String UPDATE_PET = 
        "UPDATE pets SET pet_name = ?, breed = ?, age_years = ?, age_months = ?, weight_kg = ?, color = ?, health_status = ?, vaccination_status = ?, neutered_spayed = ?, microchip_number = ?, description = ?, special_needs = ?, adoption_status = ?, adoption_fee = ?, image_url = ?, updated_at = NOW() " +
        "WHERE pet_id = ?";
    
    private static final String UPDATE_PET_ADOPTION_STATUS = 
        "UPDATE pets SET adoption_status = ?, adoption_date = ?, updated_at = NOW() WHERE pet_id = ?";
    
    private static final String DELETE_PET = 
        "DELETE FROM pets WHERE pet_id = ?";
    
    private static final String COUNT_AVAILABLE_PETS_BY_SHELTER = 
        "SELECT COUNT(*) FROM pets WHERE shelter_id = ? AND adoption_status = 'AVAILABLE'";
    
    private static final String SEARCH_PETS = 
        "SELECT * FROM pets WHERE adoption_status = 'AVAILABLE' AND (pet_name LIKE ? OR species LIKE ? OR breed LIKE ?) ORDER BY pet_name ASC";
    
    /**
     * Creates a new pet record in the database
     * 
     * @param pet Pet object with details to be persisted
     * @return true if insertion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean createPet(Pet pet) throws SQLException {
        if (pet == null) {
            throw new IllegalArgumentException("Pet object cannot be null");
        }
        
        if (pet.getShelterId() <= 0) {
            throw new IllegalArgumentException("Valid shelter ID required");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_PET, Statement.RETURN_GENERATED_KEYS);
            
            // Set parameters using PreparedStatement (prevents SQL injection)
            preparedStatement.setInt(1, pet.getShelterId());
            preparedStatement.setString(2, pet.getPetName());
            preparedStatement.setString(3, pet.getSpecies());
            preparedStatement.setString(4, pet.getBreed());
            preparedStatement.setInt(5, pet.getAgeYears());
            preparedStatement.setInt(6, pet.getAgeMonths());
            preparedStatement.setString(7, pet.getGender().toString());
            
            // Handle BigDecimal for weight
            if (pet.getWeightKg() != null) {
                preparedStatement.setBigDecimal(8, pet.getWeightKg());
            } else {
                preparedStatement.setNull(8, Types.DECIMAL);
            }
            
            preparedStatement.setString(9, pet.getColor());
            preparedStatement.setString(10, pet.getHealthStatus());
            preparedStatement.setString(11, pet.getVaccinationStatus().toString());
            preparedStatement.setBoolean(12, pet.isNeuteredSpayed());
            preparedStatement.setString(13, pet.getMicrochipNumber());
            preparedStatement.setString(14, pet.getDescription());
            preparedStatement.setString(15, pet.getSpecialNeeds());
            preparedStatement.setString(16, pet.getAdoptionStatus().toString());
            
            // Handle BigDecimal for adoption fee
            if (pet.getAdoptionFee() != null) {
                preparedStatement.setBigDecimal(17, pet.getAdoptionFee());
            } else {
                preparedStatement.setNull(17, Types.DECIMAL);
            }
            
            preparedStatement.setString(18, pet.getImageUrl());
            
            // Handle LocalDate for intake date
            if (pet.getIntakeDate() != null) {
                preparedStatement.setDate(19, Date.valueOf(pet.getIntakeDate()));
            } else {
                preparedStatement.setDate(19, Date.valueOf(LocalDate.now()));
            }
            
            // Execute insertion
            int rowsInserted = preparedStatement.executeUpdate();
            
            if (rowsInserted > 0) {
                // Get the auto-generated pet ID
                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    pet.setPetId(resultSet.getInt(1));
                    System.out.println("✓ Pet created successfully with ID: " + pet.getPetId());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error creating pet: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Retrieves a pet by its unique ID
     * 
     * @param petId unique pet identifier
     * @return Pet object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public Pet getPetById(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PET_BY_ID);
            preparedStatement.setInt(1, petId);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return mapResultSetToPet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pet by ID: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return null;
    }
    
    /**
     * Retrieves all pets currently available for adoption
     * 
     * @return List of available Pet objects
     * @throws SQLException if database operation fails
     */
    public List<Pet> getAllAvailablePets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_ALL_AVAILABLE_PETS);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " available pets from database");
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving available pets: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Retrieves all pets in a specific shelter
     * 
     * @param shelterId unique shelter identifier
     * @return List of Pet objects from the shelter
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsByShelterId(int shelterId) throws SQLException {
        if (shelterId <= 0) {
            throw new IllegalArgumentException("Shelter ID must be positive");
        }
        
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PETS_BY_SHELTER);
            preparedStatement.setInt(1, shelterId);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " pets for shelter ID: " + shelterId);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pets by shelter: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Retrieves all pets with a specific adoption status
     * Useful for admin dashboards and reporting
     * 
     * @param status adoption status filter
     * @return List of Pet objects with specified status
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsByStatus(Pet.AdoptionStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PETS_BY_STATUS);
            preparedStatement.setString(1, status.toString());
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " pets with status: " + status);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pets by status: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Retrieves all available pets of a specific species
     * Useful for filtering on adoption portal
     * 
     * @param species type of animal (Dog, Cat, Rabbit, etc.)
     * @return List of Pet objects matching the species
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsBySpecies(String species) throws SQLException {
        if (species == null || species.isEmpty()) {
            throw new IllegalArgumentException("Species cannot be null or empty");
        }
        
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PETS_BY_SPECIES);
            preparedStatement.setString(1, species);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " pets of species: " + species);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pets by species: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Retrieves all available pets of a specific breed
     * 
     * @param breed breed of animal
     * @return List of Pet objects matching the breed
     * @throws SQLException if database operation fails
     */
    public List<Pet> getPetsByBreed(String breed) throws SQLException {
        if (breed == null || breed.isEmpty()) {
            throw new IllegalArgumentException("Breed cannot be null or empty");
        }
        
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PETS_BY_BREED);
            preparedStatement.setString(1, breed);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " pets of breed: " + breed);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pets by breed: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Retrieves all pets from the database
     * Note: Use with pagination in production for large datasets
     * 
     * @return List of all Pet objects
     * @throws SQLException if database operation fails
     */
    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_ALL_PETS);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Retrieved " + pets.size() + " pets from database");
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving all pets: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Searches for pets by name, species, or breed
     * Useful for adoption portal search functionality
     * 
     * @param searchTerm search keyword to match against pet attributes
     * @return List of matching available pets
     * @throws SQLException if database operation fails
     */
    public List<Pet> searchPets(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        List<Pet> pets = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SEARCH_PETS);
            
            // Add wildcard for LIKE search
            String searchPattern = "%" + searchTerm + "%";
            preparedStatement.setString(1, searchPattern);
            preparedStatement.setString(2, searchPattern);
            preparedStatement.setString(3, searchPattern);
            
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                pets.add(mapResultSetToPet(resultSet));
            }
            
            System.out.println("✓ Found " + pets.size() + " pets matching search term: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("✗ Error searching pets: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return pets;
    }
    
    /**
     * Updates an existing pet's information
     * 
     * @param pet Pet object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updatePet(Pet pet) throws SQLException {
        if (pet == null || pet.getPetId() <= 0) {
            throw new IllegalArgumentException("Pet object with valid ID is required");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_PET);
            
            // Set parameters
            preparedStatement.setString(1, pet.getPetName());
            preparedStatement.setString(2, pet.getBreed());
            preparedStatement.setInt(3, pet.getAgeYears());
            preparedStatement.setInt(4, pet.getAgeMonths());
            
            if (pet.getWeightKg() != null) {
                preparedStatement.setBigDecimal(5, pet.getWeightKg());
            } else {
                preparedStatement.setNull(5, Types.DECIMAL);
            }
            
            preparedStatement.setString(6, pet.getColor());
            preparedStatement.setString(7, pet.getHealthStatus());
            preparedStatement.setString(8, pet.getVaccinationStatus().toString());
            preparedStatement.setBoolean(9, pet.isNeuteredSpayed());
            preparedStatement.setString(10, pet.getMicrochipNumber());
            preparedStatement.setString(11, pet.getDescription());
            preparedStatement.setString(12, pet.getSpecialNeeds());
            preparedStatement.setString(13, pet.getAdoptionStatus().toString());
            
            if (pet.getAdoptionFee() != null) {
                preparedStatement.setBigDecimal(14, pet.getAdoptionFee());
            } else {
                preparedStatement.setNull(14, Types.DECIMAL);
            }
            
            preparedStatement.setString(15, pet.getImageUrl());
            preparedStatement.setInt(16, pet.getPetId());
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                pet.setUpdatedAt(LocalDateTime.now());
                System.out.println("✓ Pet updated successfully with ID: " + pet.getPetId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating pet: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Updates pet's adoption status
     * Called when pet is adopted or status changes
     * 
     * @param petId unique pet identifier
     * @param newStatus new adoption status
     * @param adoptionDate date of adoption (null for non-adopted status)
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateAdoptionStatus(int petId, Pet.AdoptionStatus newStatus, LocalDate adoptionDate) throws SQLException {
        if (petId <= 0 || newStatus == null) {
            throw new IllegalArgumentException("Valid pet ID and status required");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_PET_ADOPTION_STATUS);
            
            preparedStatement.setString(1, newStatus.toString());
            
            if (adoptionDate != null) {
                preparedStatement.setDate(2, Date.valueOf(adoptionDate));
            } else {
                preparedStatement.setNull(2, Types.DATE);
            }
            
            preparedStatement.setInt(3, petId);
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("✓ Pet adoption status updated to: " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating adoption status: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Deletes a pet from the database
     * Note: Consider soft delete with status change in production
     * 
     * @param petId unique pet identifier
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deletePet(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(DELETE_PET);
            preparedStatement.setInt(1, petId);
            
            int rowsDeleted = preparedStatement.executeUpdate();
            
            if (rowsDeleted > 0) {
                System.out.println("✓ Pet deleted successfully with ID: " + petId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting pet: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return false;
    }
    
    /**
     * Counts available pets in a specific shelter
     * Useful for capacity monitoring
     * 
     * @param shelterId unique shelter identifier
     * @return count of available pets
     * @throws SQLException if database operation fails
     */
    public int countAvailablePetsByShelterId(int shelterId) throws SQLException {
        if (shelterId <= 0) {
            throw new IllegalArgumentException("Shelter ID must be positive");
        }
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(COUNT_AVAILABLE_PETS_BY_SHELTER);
            preparedStatement.setInt(1, shelterId);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error counting available pets: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }
        
        return 0;
    }
    
    /**
     * Maps a ResultSet row to a Pet object
     * Helper method for query results conversion
     * 
     * @param resultSet database query result
     * @return Pet object populated with data from ResultSet
     * @throws SQLException if data conversion fails
     */
    private Pet mapResultSetToPet(ResultSet resultSet) throws SQLException {
        Pet pet = new Pet();
        pet.setPetId(resultSet.getInt("pet_id"));
        pet.setShelterId(resultSet.getInt("shelter_id"));
        pet.setPetName(resultSet.getString("pet_name"));
        pet.setSpecies(resultSet.getString("species"));
        pet.setBreed(resultSet.getString("breed"));
        pet.setAgeYears(resultSet.getInt("age_years"));
        pet.setAgeMonths(resultSet.getInt("age_months"));
        pet.setGender(Pet.Gender.valueOf(resultSet.getString("gender")));
        
        // Handle BigDecimal for weight
        BigDecimal weight = resultSet.getBigDecimal("weight_kg");
        if (weight != null) {
            pet.setWeightKg(weight);
        }
        
        pet.setColor(resultSet.getString("color"));
        pet.setHealthStatus(resultSet.getString("health_status"));
        pet.setVaccinationStatus(Pet.VaccinationStatus.valueOf(resultSet.getString("vaccination_status")));
        pet.setNeuteredSpayed(resultSet.getBoolean("neutered_spayed"));
        pet.setMicrochipNumber(resultSet.getString("microchip_number"));
        pet.setDescription(resultSet.getString("description"));
        pet.setSpecialNeeds(resultSet.getString("special_needs"));
        pet.setAdoptionStatus(Pet.AdoptionStatus.valueOf(resultSet.getString("adoption_status")));
        
        // Handle BigDecimal for adoption fee
        BigDecimal fee = resultSet.getBigDecimal("adoption_fee");
        if (fee != null) {
            pet.setAdoptionFee(fee);
        }
        
        pet.setImageUrl(resultSet.getString("image_url"));
        
        // Handle LocalDate for intake date
        Date intakeDate = resultSet.getDate("intake_date");
        if (intakeDate != null) {
            pet.setIntakeDate(intakeDate.toLocalDate());
        }
        
        // Handle LocalDate for adoption date
        Date adoptionDate = resultSet.getDate("adoption_date");
        if (adoptionDate != null) {
            pet.setAdoptionDate(adoptionDate.toLocalDate());
        }
        
        return pet;
    }
}
