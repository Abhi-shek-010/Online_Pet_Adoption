package com.petadoption.dao;

import com.petadoption.config.DBConnection;
import com.petadoption.model.Adoption;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adoption Data Access Object (DAO)
 * 
 * Handles database operations for the 'adoptions' table.
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class AdoptionDAO {

    // PREPARED STATEMENTS
    private static final String INSERT_ADOPTION = "INSERT INTO adoptions (adopter_id, pet_id, adoption_date, contract_signed) VALUES (?, ?, ?, ?)";

    // JOIN queries to get useful display info
    private static final String SELECT_BY_ADOPTER = "SELECT a.*, p.pet_name, p.species, p.breed FROM adoptions a " +
            "JOIN pets p ON a.pet_id = p.pet_id " +
            "WHERE a.adopter_id = ? ORDER BY a.adoption_date DESC";

    private static final String SELECT_ALL_HAPPY_FAMILIES = "SELECT a.*, p.pet_name, p.species, p.breed, u.full_name as adopter_name FROM adoptions a "
            +
            "JOIN pets p ON a.pet_id = p.pet_id " +
            "JOIN users u ON a.adopter_id = u.user_id " +
            "ORDER BY a.adoption_date DESC";

    /**
     * Creates a new adoption record
     * 
     * @param connection Transaction connection
     * @param adoption   Adoption object
     * @return true if successful
     * @throws SQLException
     */
    public boolean createAdoption(Connection connection, Adoption adoption) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(INSERT_ADOPTION, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, adoption.getAdopterId());
            ps.setInt(2, adoption.getPetId());
            ps.setTimestamp(3, Timestamp.valueOf(adoption.getAdoptionDate()));
            ps.setBoolean(4, adoption.isContractSigned());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    adoption.setAdoptionId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } finally {
            DBConnection.closePreparedStatement(ps);
        }
    }

    /**
     * Get all adopted pets for a specific user
     * 
     * @param userId Adopter's User ID
     * @return List of Adoptions
     */
    public List<Adoption> getAdoptionsByAdopter(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Adoption> adoptions = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(SELECT_BY_ADOPTER);
            ps.setInt(1, userId);

            rs = ps.executeQuery();
            while (rs.next()) {
                adoptions.add(mapResultSetToAdoption(rs, true));
            }
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closePreparedStatement(ps);
            DBConnection.closeConnection(conn);
        }
        return adoptions;
    }

    /**
     * Get all happy families (all adoptions)
     * 
     * @return List of Adoptions with enrichments
     */
    public List<Adoption> getAllHappyFamilies() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Adoption> adoptions = new ArrayList<>();

        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(SELECT_ALL_HAPPY_FAMILIES);

            rs = ps.executeQuery();
            while (rs.next()) {
                adoptions.add(mapResultSetToAdoption(rs, false));
            }
        } finally {
            DBConnection.closeResultSet(rs);
            DBConnection.closePreparedStatement(ps);
            DBConnection.closeConnection(conn);
        }
        return adoptions;
    }

    private Adoption mapResultSetToAdoption(ResultSet rs, boolean isAdopterQuery) throws SQLException {
        Adoption ad = new Adoption();
        ad.setAdoptionId(rs.getInt("adoption_id"));
        ad.setAdopterId(rs.getInt("adopter_id"));
        ad.setPetId(rs.getInt("pet_id"));

        Timestamp ts = rs.getTimestamp("adoption_date");
        if (ts != null)
            ad.setAdoptionDate(ts.toLocalDateTime());

        ad.setContractSigned(rs.getBoolean("contract_signed"));

        // Map DTO fields
        try {
            ad.setPetName(rs.getString("pet_name"));
            ad.setSpecies(rs.getString("species"));
            ad.setBreed(rs.getString("breed"));

            if (!isAdopterQuery) {
                ad.setAdopterName(rs.getString("adopter_name"));
            }
        } catch (SQLException e) {
            // Ignore if columns missing
        }

        return ad;
    }
}
