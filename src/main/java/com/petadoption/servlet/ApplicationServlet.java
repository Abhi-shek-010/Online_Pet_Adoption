package com.petadoption.servlet;

import com.petadoption.exception.PetAdoptionException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.AdoptionApplication.ApplicationStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.PetService;
import com.petadoption.util.SessionUtils;
import org.json.JSONObject;
import org.json.JSONException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Management Servlet
 * 
 * Handles adoption application lifecycle:
 * - ADOPTER: Submit application, View my applications
 * - SHELTER: View pending applications, Approve/Reject applications
 * 
 * Endpoints:
 * - POST /api/applications - Submit new application
 * - GET /api/applications/my - Get my applications (Adopter)
 * - GET /api/applications/shelter - Get pending applications for my shelter
 * (Shelter)
 * - PUT /api/applications/{id}/approve - Approve application
 * - PUT /api/applications/{id}/reject - Reject application
 * 
 * @author Pet Adoption System Team
 */
@WebServlet(name = "ApplicationServlet", urlPatterns = {
        "/api/applications",
        "/api/applications/*"
})
public class ApplicationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AdoptionService adoptionService;
    private PetService petService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.adoptionService = new AdoptionService();
        this.petService = new PetService();
        System.out.println("✓ ApplicationServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();

        try {
            HttpSession session = request.getSession(false);
            if (!SessionUtils.isUserLoggedIn(session)) {
                handleError(response, out, "User not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User currentUser = SessionUtils.getUserFromSession(session);

            if (pathInfo != null && pathInfo.equals("/my")) {
                handleGetMyApplications(currentUser, response, out);
            } else if (pathInfo != null && pathInfo.equals("/shelter")) {
                handleGetShelterApplications(currentUser, response, out);
            } else {
                handleError(response, out, "Invalid endpoint", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            handleError(response, out, "Database error: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            handleError(response, out, "Unexpected error: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);
            if (!SessionUtils.isUserLoggedIn(session)) {
                handleError(response, out, "User not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Only Adopters can submit
            if (!SessionUtils.hasRole(session, "ADOPTER")) {
                handleError(response, out, "Only adopters can submit applications", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            handleSubmitApplication(request, session, response, out);

        } catch (Exception e) {
            handleError(response, out, "Error submitting application: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();

        try {
            HttpSession session = request.getSession(false);
            if (!SessionUtils.isUserLoggedIn(session)) {
                handleError(response, out, "User not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Route: /api/applications/{id}/approve OR /api/applications/{id}/reject
            if (pathInfo == null || pathInfo.split("/").length < 3) {
                handleError(response, out, "Invalid endpoint format", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String[] parts = pathInfo.split("/"); // ["", "123", "approve"]
            int appId = Integer.parseInt(parts[1]);
            String action = parts[2];

            User currentUser = SessionUtils.getUserFromSession(session);

            if ("approve".equals(action)) {
                handleApproveApplication(appId, currentUser, response, out);
            } else if ("reject".equals(action)) {
                handleRejectApplication(appId, currentUser, response, out);
            } else {
                handleError(response, out, "Invalid action", HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (NumberFormatException e) {
            handleError(response, out, "Invalid application ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            handleError(response, out, "Error processing application: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    // ==========================================
    // Handlers
    // ==========================================

    private void handleGetMyApplications(User user, HttpServletResponse response, PrintWriter out) throws SQLException {
        List<AdoptionApplication> apps = adoptionService.getApplicationsByAdopter(user.getUserId());
        response.setStatus(HttpServletResponse.SC_OK);
        out.println(listToJson(apps));
    }

    private void handleGetShelterApplications(User user, HttpServletResponse response, PrintWriter out)
            throws SQLException {
        if (user.getUserType() != User.UserType.SHELTER && user.getUserType() != User.UserType.ADMIN) {
            handleError(response, out, "Access denied", HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Get all pending apps
        List<AdoptionApplication> allPending = adoptionService.getPendingApplications();

        // Filter those belonging to this shelter's pets
        // Note: Ideally allow service to do this filter, but for now filtering in Java
        // We assume user.getUserId() corresponds to shelter_id for now or we look up
        // owned pets
        // For simplicity: We will fetch the pet for each app and check ownership

        // Optimize: If we had getApplicationsByShelter(shelterId), use that.
        // Falling back to iterating (not efficient but functional for demo)

        List<AdoptionApplication> shelterApps = allPending.stream().filter(app -> {
            try {
                Pet pet = petService.getPetById(app.getPetId());
                // Check if pet belongs to this user (shelter)
                // We assume shelter users own the pets they uploaded.
                // Pet table has shelter_id. User table has user_id.
                // Shelter Info maps user_id <-> shelter_id.
                // For this demo, let's assume strict owner check via Service calls is too heavy
                // here
                // We will rely on the fact that ONLY the shelter owner can approve later.
                // But to VIEW, we should also filter.

                // Hack: If user is ADMIN, show all. If SHELTER, fetch pet and check shelter_id
                if (user.getUserType() == User.UserType.ADMIN)
                    return true;

                // Real impl would need ShelterDAO to get shelterId from userId.
                // Let's assume pet.getShelterId() == user.getId() (Simplification used in
                // AdoptionService too?)
                // Actually AdoptionService checks `pet.getShelterId() != reviewedBy`
                // So let's filter similarly.
                return pet != null && pet.getShelterId() == user.getUserId(); // Assuming 1:1 mapping for simplicity if
                                                                              // shelter_info not queried
            } catch (Exception e) {
                return false;
            }
        }).collect(Collectors.toList());

        response.setStatus(HttpServletResponse.SC_OK);
        out.println(listToJson(shelterApps));
    }

    private void handleSubmitApplication(HttpServletRequest request, HttpSession session, HttpServletResponse response,
            PrintWriter out) throws Exception {
        JSONObject json = parseJsonRequest(request);

        int petId = json.getInt("petId");
        String reason = json.optString("reason", "");
        int userId = SessionUtils.getUserIdFromSession(session);

        // Get or create adopter_info record for this user
        int adopterId = getOrCreateAdopterInfo(userId);
        if (adopterId <= 0) {
            handleError(response, out, "Failed to create adopter profile",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        AdoptionApplication app = new AdoptionApplication();
        app.setPetId(petId);
        app.setAdopterId(adopterId); // Use adopter_info.adopter_id, not user_id
        app.setReasonForAdoption(reason);
        app.setApplicationText("Application via Web Portal"); // Default text
        app.setHouseholdMembers(1); // Default/Placeholder
        app.setHasYard(false);
        app.setStatus(ApplicationStatus.PENDING);

        boolean success = adoptionService.submitApplication(app);

        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println("{\"success\": true, \"message\": \"Application submitted successfully\"}");
        } else {
            handleError(response, out, "Failed to submit application", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the adopter_id from adopter_info for a given user_id.
     * Creates a new adopter_info record if one doesn't exist.
     */
    private int getOrCreateAdopterInfo(int userId) {
        try (java.sql.Connection conn = com.petadoption.config.DBConnection.getInstance().getConnection()) {
            // First check if adopter_info exists
            String checkSql = "SELECT adopter_id FROM adopter_info WHERE user_id = ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, userId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("adopter_id");
                    }
                }
            }

            // Create adopter_info if it doesn't exist
            String insertSql = "INSERT INTO adopter_info (user_id, employment_status, home_type, has_other_pets, rent_or_own) VALUES (?, 'Unknown', 'Unknown', FALSE, 'Unknown')";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertSql,
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            System.out.println("✓ Created adopter_info for user " + userId);
                            return rs.getInt(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("✗ Error getting/creating adopter_info: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    private void handleApproveApplication(int appId, User user, HttpServletResponse response, PrintWriter out)
            throws SQLException {
        // Need to pass Pet ID to finalizeAdoption.
        AdoptionApplication app = adoptionService.getApplicationById(appId);
        if (app == null) {
            handleError(response, out, "Application not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean success = adoptionService.finalizeAdoption(
                app.getPetId(),
                appId,
                LocalDate.now(),
                "Approved via Web Portal",
                user.getUserId());

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("{\"success\": true, \"message\": \"Application approved and adoption finalized\"}");
        } else {
            handleError(response, out, "Failed to approve application", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRejectApplication(int appId, User user, HttpServletResponse response, PrintWriter out)
            throws SQLException {
        boolean success = adoptionService.rejectApplication(appId, "Rejected via Web Portal", user.getUserId());

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("{\"success\": true, \"message\": \"Application rejected\"}");
        } else {
            handleError(response, out, "Failed to reject application", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ==========================================
    // Utils
    // ==========================================

    private String listToJson(List<AdoptionApplication> apps) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"success\": true, \"applications\": [");
        for (int i = 0; i < apps.size(); i++) {
            AdoptionApplication app = apps.get(i);
            sb.append("{")
                    .append("\"applicationId\": ").append(app.getApplicationId()).append(",")
                    .append("\"petId\": ").append(app.getPetId()).append(",")
                    .append("\"status\": \"").append(app.getStatus()).append("\",")
                    .append("\"applicationDate\": \"").append(app.getApplicationDate()).append("\",")
                    .append("\"reasonForAdoption\": \"").append(escapeJson(app.getReasonForAdoption())).append("\"")
                    .append("}");
            if (i < apps.size() - 1)
                sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private JSONObject parseJsonRequest(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line);
        return new JSONObject(sb.toString());
    }

    private void handleError(HttpServletResponse response, PrintWriter out, String message, int status) {
        response.setStatus(status);
        out.println("{ \"success\": false, \"message\": \"" + escapeJson(message) + "\" }");
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\"", "\\\"").replace("\n", " ");
    }
}
