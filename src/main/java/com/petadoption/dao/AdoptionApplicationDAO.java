package com.petadoption.dao;

import com.petadoption.config.DBConnection;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.AdoptionApplication.ApplicationStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adoption Application Data Access Object (DAO)
 * 
 * Handles all database operations for AdoptionApplication entities using JDBC.
 * Implements PreparedStatement to prevent SQL injection attacks.
 * Supports transaction-aware operations for atomic adoption finalization.
 * 
 * Features:
 * - Full CRUD operations for adoption applications
 * - Transaction support with connection parameter overloads
 * - SQL injection prevention via PreparedStatement
 * - Comprehensive exception handling
 * - Resource cleanup in finally blocks
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class AdoptionApplicationDAO {

    // SQL Query Strings
    private static final String INSERT_APPLICATION = "INSERT INTO adoption_applications " +
            "(pet_id, adopter_id, application_text, reason_for_adoption, " +
            "household_members, has_yard, yard_type, previous_pet_experience, " +
            "veterinary_reference, personal_reference, status, application_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM adoption_applications WHERE application_id = ?";

    private static final String SELECT_ALL = "SELECT * FROM adoption_applications ORDER BY application_date DESC";

    private static final String SELECT_BY_ADOPTER = "SELECT * FROM adoption_applications WHERE adopter_id = ? ORDER BY application_date DESC";

    private static final String SELECT_BY_PET = "SELECT * FROM adoption_applications WHERE pet_id = ? ORDER BY application_date DESC";

    private static final String SELECT_BY_STATUS = "SELECT * FROM adoption_applications WHERE status = ? ORDER BY application_date DESC";

    private static final String SELECT_PENDING = "SELECT * FROM adoption_applications WHERE status = 'PENDING' ORDER BY application_date ASC";

    private static final String UPDATE_APPLICATION_STATUS = "UPDATE adoption_applications SET status = ?, approval_date = ?, "
            +
            "approval_notes = ?, reviewed_by = ?, updated_at = CURRENT_TIMESTAMP WHERE application_id = ?";

    private static final String UPDATE_APPLICATION = "UPDATE adoption_applications SET application_text = ?, reason_for_adoption = ?, "
            +
            "household_members = ?, has_yard = ?, yard_type = ?, previous_pet_experience = ?, " +
            "veterinary_reference = ?, personal_reference = ?, updated_at = CURRENT_TIMESTAMP " +
            "WHERE application_id = ?";

    private static final String DELETE_APPLICATION = "DELETE FROM adoption_applications WHERE application_id = ?";

    private static final String COUNT_BY_PET = "SELECT COUNT(*) FROM adoption_applications WHERE pet_id = ? AND status = 'PENDING'";

    /**
     * Creates a new adoption application in the database
     * 
     * @param application AdoptionApplication object with details to be persisted
     * @return true if insertion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean createApplication(AdoptionApplication application) throws SQLException {
        if (application == null) {
            throw new IllegalArgumentException("Application cannot be null");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_APPLICATION, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setInt(1, application.getPetId());
            preparedStatement.setInt(2, application.getAdopterId());
            preparedStatement.setString(3, application.getApplicationText());
            preparedStatement.setString(4, application.getReasonForAdoption());
            preparedStatement.setInt(5, application.getHouseholdMembers());
            preparedStatement.setBoolean(6, application.isHasYard());
            preparedStatement.setString(7, application.getYardType());
            preparedStatement.setString(8, application.getPreviousPetExperience());
            preparedStatement.setString(9, application.getVeterinaryReference());
            preparedStatement.setString(10, application.getPersonalReference());
            preparedStatement.setString(11, application.getStatus().toString());
            preparedStatement.setTimestamp(12, Timestamp.valueOf(application.getApplicationDate()));

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int applicationId = generatedKeys.getInt(1);
                    application.setApplicationId(applicationId);
                    System.out.println("✓ Adoption application created with ID: " + applicationId);
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error creating adoption application: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(generatedKeys);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return false;
    }

    /**
     * Retrieves an adoption application by its unique ID
     * 
     * @param applicationId unique application identifier
     * @return AdoptionApplication object if found, null otherwise
     * @throws SQLException if database operation fails
     */
    public AdoptionApplication getApplicationById(int applicationId) throws SQLException {
        if (applicationId <= 0) {
            throw new IllegalArgumentException("Application ID must be positive");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_BY_ID);
            preparedStatement.setInt(1, applicationId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToApplication(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving application by ID: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return null;
    }

    /**
     * Retrieves all adoption applications
     * 
     * @return List of all AdoptionApplication objects
     * @throws SQLException if database operation fails
     */
    public List<AdoptionApplication> getAllApplications() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<AdoptionApplication> applications = new ArrayList<>();

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_ALL);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

            System.out.println("✓ Retrieved " + applications.size() + " adoption applications");
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving all applications: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return applications;
    }

    /**
     * Retrieves all applications submitted by a specific adopter
     * 
     * @param adopterId unique adopter identifier
     * @return List of AdoptionApplication objects for the adopter
     * @throws SQLException if database operation fails
     */
    public List<AdoptionApplication> getApplicationsByAdopterId(int adopterId) throws SQLException {
        if (adopterId <= 0) {
            throw new IllegalArgumentException("Adopter ID must be positive");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<AdoptionApplication> applications = new ArrayList<>();

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_BY_ADOPTER);
            preparedStatement.setInt(1, adopterId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

            System.out.println("✓ Retrieved " + applications.size() + " applications for adopter ID: " + adopterId);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving applications by adopter: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return applications;
    }

    /**
     * Retrieves all applications for a specific pet
     * 
     * @param petId unique pet identifier
     * @return List of AdoptionApplication objects for the pet
     * @throws SQLException if database operation fails
     */
    public List<AdoptionApplication> getApplicationsByPetId(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<AdoptionApplication> applications = new ArrayList<>();

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_BY_PET);
            preparedStatement.setInt(1, petId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

            System.out.println("✓ Retrieved " + applications.size() + " applications for pet ID: " + petId);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving applications by pet: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return applications;
    }

    /**
     * Retrieves all pending applications (awaiting review)
     * 
     * @return List of pending AdoptionApplication objects
     * @throws SQLException if database operation fails
     */
    public List<AdoptionApplication> getPendingApplications() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<AdoptionApplication> applications = new ArrayList<>();

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_PENDING);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

            System.out.println("✓ Retrieved " + applications.size() + " pending applications");
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving pending applications: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return applications;
    }

    /**
     * Retrieves applications by status
     * 
     * @param status application status filter
     * @return List of AdoptionApplication objects matching the status
     * @throws SQLException if database operation fails
     */
    public List<AdoptionApplication> getApplicationsByStatus(ApplicationStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<AdoptionApplication> applications = new ArrayList<>();

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(SELECT_BY_STATUS);
            preparedStatement.setString(1, status.toString());
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                applications.add(mapResultSetToApplication(resultSet));
            }

            System.out.println("✓ Retrieved " + applications.size() + " applications with status: " + status);
        } catch (SQLException e) {
            System.err.println("✗ Error retrieving applications by status: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return applications;
    }

    /**
     * Updates application status (standalone - creates own connection)
     * 
     * @param applicationId unique application identifier
     * @param newStatus     new application status
     * @param approvalNotes notes from reviewer
     * @param reviewedBy    user ID of reviewer
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateApplicationStatus(int applicationId, ApplicationStatus newStatus,
            String approvalNotes, int reviewedBy) throws SQLException {
        Connection connection = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            return updateApplicationStatus(connection, applicationId, newStatus, approvalNotes, reviewedBy);
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

    /**
     * Updates application status (transaction-aware - uses provided connection)
     * 
     * @param connection    database connection (for transaction support)
     * @param applicationId unique application identifier
     * @param newStatus     new application status
     * @param approvalNotes notes from reviewer
     * @param reviewedBy    user ID of reviewer
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateApplicationStatus(Connection connection, int applicationId,
            ApplicationStatus newStatus, String approvalNotes,
            int reviewedBy) throws SQLException {
        if (applicationId <= 0 || newStatus == null) {
            throw new IllegalArgumentException("Valid application ID and status required");
        }

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = connection.prepareStatement(UPDATE_APPLICATION_STATUS);

            preparedStatement.setString(1, newStatus.toString());

            if (newStatus == ApplicationStatus.APPROVED || newStatus == ApplicationStatus.REJECTED) {
                preparedStatement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                preparedStatement.setNull(2, Types.TIMESTAMP);
            }

            preparedStatement.setString(3, approvalNotes);
            preparedStatement.setInt(4, reviewedBy);
            preparedStatement.setInt(5, applicationId);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Application status updated to: " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating application status: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
        }

        return false;
    }

    /**
     * Updates application details
     * 
     * @param application AdoptionApplication object with updated information
     * @return true if update successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean updateApplication(AdoptionApplication application) throws SQLException {
        if (application == null || application.getApplicationId() <= 0) {
            throw new IllegalArgumentException("Valid application with ID required");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_APPLICATION);

            preparedStatement.setString(1, application.getApplicationText());
            preparedStatement.setString(2, application.getReasonForAdoption());
            preparedStatement.setInt(3, application.getHouseholdMembers());
            preparedStatement.setBoolean(4, application.isHasYard());
            preparedStatement.setString(5, application.getYardType());
            preparedStatement.setString(6, application.getPreviousPetExperience());
            preparedStatement.setString(7, application.getVeterinaryReference());
            preparedStatement.setString(8, application.getPersonalReference());
            preparedStatement.setInt(9, application.getApplicationId());

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✓ Application updated successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error updating application: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return false;
    }

    /**
     * Deletes an application from the database
     * 
     * @param applicationId unique application identifier
     * @return true if deletion successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteApplication(int applicationId) throws SQLException {
        if (applicationId <= 0) {
            throw new IllegalArgumentException("Application ID must be positive");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(DELETE_APPLICATION);
            preparedStatement.setInt(1, applicationId);

            int rowsDeleted = preparedStatement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✓ Application deleted successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Error deleting application: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return false;
    }

    /**
     * Counts pending applications for a specific pet
     * 
     * @param petId unique pet identifier
     * @return count of pending applications
     * @throws SQLException if database operation fails
     */
    public int countPendingApplicationsByPet(int petId) throws SQLException {
        if (petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(COUNT_BY_PET);
            preparedStatement.setInt(1, petId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error counting pending applications: " + e.getMessage());
            throw e;
        } finally {
            DBConnection.closeResultSet(resultSet);
            DBConnection.closePreparedStatement(preparedStatement);
            DBConnection.closeConnection(connection);
        }

        return 0;
    }

    /**
     * Maps a ResultSet row to an AdoptionApplication object
     * Helper method for query results conversion
     * 
     * @param resultSet database query result
     * @return AdoptionApplication object populated with data
     * @throws SQLException if data conversion fails
     */
    private AdoptionApplication mapResultSetToApplication(ResultSet resultSet) throws SQLException {
        AdoptionApplication application = new AdoptionApplication();

        application.setApplicationId(resultSet.getInt("application_id"));
        application.setPetId(resultSet.getInt("pet_id"));
        application.setAdopterId(resultSet.getInt("adopter_id"));

        Timestamp appDate = resultSet.getTimestamp("application_date");
        if (appDate != null) {
            application.setApplicationDate(appDate.toLocalDateTime());
        }

        String statusStr = resultSet.getString("status");
        if (statusStr != null) {
            application.setStatus(ApplicationStatus.valueOf(statusStr));
        }

        application.setApplicationText(resultSet.getString("application_text"));
        application.setReasonForAdoption(resultSet.getString("reason_for_adoption"));
        application.setHouseholdMembers(resultSet.getInt("household_members"));
        application.setHasYard(resultSet.getBoolean("has_yard"));
        application.setYardType(resultSet.getString("yard_type"));
        application.setPreviousPetExperience(resultSet.getString("previous_pet_experience"));
        application.setVeterinaryReference(resultSet.getString("veterinary_reference"));
        application.setPersonalReference(resultSet.getString("personal_reference"));

        Timestamp approvalDate = resultSet.getTimestamp("approval_date");
        if (approvalDate != null) {
            application.setApprovalDate(approvalDate.toLocalDateTime());
        }

        application.setApprovalNotes(resultSet.getString("approval_notes"));
        application.setReviewedBy(resultSet.getInt("reviewed_by"));

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            application.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return application;
    }
}
