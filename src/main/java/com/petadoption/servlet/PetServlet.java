package com.petadoption.servlet;

import com.petadoption.exception.PetAdoptionException;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.PetService;
import com.petadoption.util.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Pet Management Servlet
 * 
 * Handles pet-related operations with role-based access control:
 * - SHELTER users can POST new pets and view their own pets
 * - ADOPTER users can GET available pets
 * - ADMIN users can manage all pets
 * 
 * Endpoints:
 * - GET /pets - Get available pets (Adopters)
 * - GET /pets/{petId} - Get pet details
 * - POST /pets - Create new pet (Shelters)
 * - PUT /pets/{petId} - Update pet (Shelters/Admins)
 * 
 * Features:
 * - Session-based authentication
 * - Role-based access control
 * - Comprehensive input validation
 * - Exception handling with meaningful messages
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
@WebServlet(name = "PetServlet", urlPatterns = { "/pets",
        "/pets/*" }, description = "Handles pet management operations with role-based access")
public class PetServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private PetService petService;

    /**
     * Initialize the servlet with service layer
     */
    @Override
    public void init() throws ServletException {
        super.init();
        this.petService = new PetService();
        System.out.println("✓ PetServlet initialized with service layer");
    }

    /**
     * Handle HTTP GET requests (retrieve pets)
     * 
     * Query Parameters:
     * - action: "available" (default), "shelter", "search"
     * - search: search term for pets
     * - status: adoption status filter
     * 
     * @param request  HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException      if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String pathInfo = request.getPathInfo();
            String action = request.getParameter("action");

            if (pathInfo != null && pathInfo.length() > 1) {
                // Get specific pet by ID
                try {
                    int petId = Integer.parseInt(pathInfo.substring(1));
                    handleGetPetById(petId, response, out);
                } catch (NumberFormatException e) {
                    handleError(response, out, "Invalid pet ID",
                            HttpServletResponse.SC_BAD_REQUEST);
                }
            } else if ("search".equals(action)) {
                // Search pets
                String searchTerm = request.getParameter("q");
                handleSearchPets(searchTerm, response, out);
            } else if ("shelter".equals(action)) {
                // Get pets for specific shelter (requires authentication)
                HttpSession session = request.getSession(false);
                if (!SessionUtils.isUserLoggedIn(session)) {
                    handleError(response, out, "User not authenticated",
                            HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                if (!SessionUtils.hasAnyRole(session, "SHELTER", "ADMIN")) {
                    handleError(response, out, "Access denied. Shelter staff required",
                            HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // TODO: Get pets for shelter (requires shelter_id in session)
                handleError(response, out, "Feature not yet implemented",
                        HttpServletResponse.SC_NOT_IMPLEMENTED);
            } else {
                // Get all available pets (default - NO AUTHENTICATION REQUIRED for browsing)
                handleGetAvailablePets(response, out);
            }

        } catch (SQLException e) {
            System.err.println("✗ Database Error: " + e.getMessage());
            handleError(response, out, "Database error occurred",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("✗ Unexpected Error: " + e.getMessage());
            handleError(response, out, "Unexpected error occurred",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    /**
     * Handle HTTP POST requests (create new pet)
     * 
     * Required parameters:
     * - petName: Pet's name
     * - species: Type of animal
     * - breed: Pet's breed
     * - gender: MALE, FEMALE, or UNKNOWN
     * - ageYears: Age in years
     * - ageMonths: Age in months (0-11)
     * - vaccinationStatus: NOT_VACCINATED, PARTIAL, or COMPLETE
     * - adoptionFee: Adoption cost (optional)
     * 
     * @param request  HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException      if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Check authentication
            HttpSession session = request.getSession(false);
            if (!SessionUtils.isUserLoggedIn(session)) {
                handleError(response, out, "User not authenticated",
                        HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Check authorization - only SHELTER and ADMIN can create pets
            if (!SessionUtils.hasAnyRole(session, "SHELTER", "ADMIN")) {
                handleError(response, out, "Access denied. Shelter staff required",
                        HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Create new pet
            handleCreatePet(request, session, response, out);

        } catch (PetAdoptionException e) {
            System.err.println("✗ Business Logic Error: " + e.getMessage());
            handleError(response, out, e.getMessage(), e.getHttpStatusCode());
        } catch (SQLException e) {
            System.err.println("✗ Database Error: " + e.getMessage());
            handleError(response, out, "Database error occurred",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("✗ Unexpected Error: " + e.getMessage());
            handleError(response, out, "Unexpected error occurred",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            out.close();
        }
    }

    /**
     * Retrieve all available pets
     * 
     * @param response HTTP response
     * @param out      PrintWriter for output
     * @throws SQLException if database error occurs
     */
    private void handleGetAvailablePets(HttpServletResponse response, PrintWriter out)
            throws SQLException {

        // Delegate to service layer
        List<Pet> pets = petService.getAllAvailablePets();

        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"message\": \"Retrieved " + pets.size() + " available pets\",");
        out.println("  \"count\": " + pets.size() + ",");
        out.println("  \"pets\": [");

        for (int i = 0; i < pets.size(); i++) {
            Pet pet = pets.get(i);
            out.print("    " + petToJson(pet));
            if (i < pets.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }

        out.println("  ]");
        out.println("}");
    }

    /**
     * Retrieve specific pet by ID
     * 
     * @param petId    pet identifier
     * @param response HTTP response
     * @param out      PrintWriter for output
     * @throws SQLException if database error occurs
     */
    private void handleGetPetById(int petId, HttpServletResponse response, PrintWriter out)
            throws SQLException, PetAdoptionException {

        // Delegate to service layer
        Pet pet = petService.getPetById(petId);

        if (pet == null) {
            handleError(response, out, "Pet not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"pet\": " + petToJson(pet));
        out.println("}");
    }

    /**
     * Search pets by name, species, or breed
     * 
     * @param searchTerm search keyword
     * @param response   HTTP response
     * @param out        PrintWriter for output
     * @throws SQLException if database error occurs
     */
    private void handleSearchPets(String searchTerm, HttpServletResponse response,
            PrintWriter out) throws SQLException, PetAdoptionException {

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            handleError(response, out, "Search term is required",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Delegate to service layer
        List<Pet> pets = petService.searchAndFilterPets(searchTerm.trim(), null, null);

        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"message\": \"Found " + pets.size() + " matching pets\",");
        out.println("  \"count\": " + pets.size() + ",");
        out.println("  \"pets\": [");

        for (int i = 0; i < pets.size(); i++) {
            Pet pet = pets.get(i);
            out.print("    " + petToJson(pet));
            if (i < pets.size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }

        out.println("  ]");
        out.println("}");
    }

    /**
     * Create a new pet in the database
     * 
     * @param request  HTTP request
     * @param session  user session
     * @param response HTTP response
     * @param out      PrintWriter for output
     * @throws SQLException         if database error occurs
     * @throws PetAdoptionException if validation fails
     */
    private void handleCreatePet(HttpServletRequest request, HttpSession session,
            HttpServletResponse response, PrintWriter out)
            throws SQLException, PetAdoptionException {

        try {
            // Extract and validate parameters
            String petName = request.getParameter("petName");
            String species = request.getParameter("species");
            String breed = request.getParameter("breed");
            String genderStr = request.getParameter("gender");
            String ageYearsStr = request.getParameter("ageYears");
            String ageMonthsStr = request.getParameter("ageMonths");
            String vaccinationStatusStr = request.getParameter("vaccinationStatus");
            String adoptionFeeStr = request.getParameter("adoptionFee");
            String description = request.getParameter("description");
            String specialNeeds = request.getParameter("specialNeeds");

            // Validate required fields
            if (petName == null || petName.trim().isEmpty()) {
                throw new PetAdoptionException("Pet name is required",
                        HttpServletResponse.SC_BAD_REQUEST);
            }
            if (species == null || species.trim().isEmpty()) {
                throw new PetAdoptionException("Species is required",
                        HttpServletResponse.SC_BAD_REQUEST);
            }
            if (genderStr == null || genderStr.trim().isEmpty()) {
                throw new PetAdoptionException("Gender is required",
                        HttpServletResponse.SC_BAD_REQUEST);
            }

            // Parse and validate gender
            Pet.Gender gender;
            try {
                gender = Pet.Gender.valueOf(genderStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new PetAdoptionException("Invalid gender value",
                        HttpServletResponse.SC_BAD_REQUEST);
            }

            // Parse optional integer fields
            int ageYears = 0;
            if (ageYearsStr != null && !ageYearsStr.isEmpty()) {
                try {
                    ageYears = Integer.parseInt(ageYearsStr);
                    if (ageYears < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new PetAdoptionException("Invalid age years value",
                            HttpServletResponse.SC_BAD_REQUEST);
                }
            }

            int ageMonths = 0;
            if (ageMonthsStr != null && !ageMonthsStr.isEmpty()) {
                try {
                    ageMonths = Integer.parseInt(ageMonthsStr);
                    if (ageMonths < 0 || ageMonths > 11)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new PetAdoptionException("Age months must be between 0 and 11",
                            HttpServletResponse.SC_BAD_REQUEST);
                }
            }

            // Parse vaccination status
            Pet.VaccinationStatus vaccinationStatus = Pet.VaccinationStatus.NOT_VACCINATED;
            if (vaccinationStatusStr != null && !vaccinationStatusStr.isEmpty()) {
                try {
                    vaccinationStatus = Pet.VaccinationStatus.valueOf(vaccinationStatusStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new PetAdoptionException("Invalid vaccination status",
                            HttpServletResponse.SC_BAD_REQUEST);
                }
            }

            // Parse adoption fee
            BigDecimal adoptionFee = null;
            if (adoptionFeeStr != null && !adoptionFeeStr.isEmpty()) {
                try {
                    adoptionFee = new BigDecimal(adoptionFeeStr);
                    if (adoptionFee.compareTo(BigDecimal.ZERO) < 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new PetAdoptionException("Invalid adoption fee",
                            HttpServletResponse.SC_BAD_REQUEST);
                }
            }

            // Create Pet object
            // Get shelterId from session
            User currentUser = SessionUtils.getCurrentUser(request);
            int shelterId = 1; // Default - should be retrieved from shelter_info table based on user_id

            Pet pet = new Pet(petName.trim(), species.trim(), gender, shelterId);
            pet.setBreed(breed);
            pet.setAgeYears(ageYears);
            pet.setAgeMonths(ageMonths);
            pet.setVaccinationStatus(vaccinationStatus);
            pet.setAdoptionFee(adoptionFee);
            pet.setDescription(description);
            pet.setSpecialNeeds(specialNeeds);
            pet.setIntakeDate(LocalDate.now());

            // Delegate to service layer (validates and saves)
            boolean created = petService.createPet(pet, shelterId);

            if (!created) {
                throw new PetAdoptionException("Failed to create pet",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            System.out.println("✓ New pet created: " + petName + " (ID: " + pet.getPetId() + ")");

            // Return success response
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println("{");
            out.println("  \"success\": true,");
            out.println("  \"message\": \"Pet created successfully\",");
            out.println("  \"petId\": " + pet.getPetId() + ",");
            out.println("  \"petName\": \"" + pet.getPetName() + "\"");
            out.println("}");

        } catch (PetAdoptionException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("✗ Error creating pet: " + e.getMessage());
            throw new PetAdoptionException("Failed to create pet: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Convert Pet object to JSON string
     * 
     * @param pet Pet object to convert
     * @return JSON representation of pet
     */
    private String petToJson(Pet pet) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"petId\": ").append(pet.getPetId()).append(", ");
        json.append("\"petName\": \"").append(escapeJson(pet.getPetName())).append("\", ");
        json.append("\"species\": \"").append(escapeJson(pet.getSpecies())).append("\", ");
        json.append("\"breed\": \"").append(escapeJson(pet.getBreed())).append("\", ");
        json.append("\"age\": \"").append(escapeJson(pet.getFormattedAge())).append("\", ");
        json.append("\"gender\": \"").append(pet.getGender()).append("\", ");
        json.append("\"adoptionStatus\": \"").append(pet.getAdoptionStatus()).append("\", ");
        if (pet.getAdoptionFee() != null) {
            json.append("\"adoptionFee\": ").append(pet.getAdoptionFee()).append(", ");
        }
        if (pet.getShelterName() != null) {
            json.append("\"shelterName\": \"").append(escapeJson(pet.getShelterName())).append("\", ");
        }
        json.append("\"description\": \"").append(escapeJson(pet.getDescription() != null ? pet.getDescription() : ""))
                .append("\"");
        json.append("}");
        return json.toString();
    }

    /**
     * Handle error responses
     * 
     * @param response   HTTP response
     * @param out        PrintWriter for output
     * @param message    error message
     * @param statusCode HTTP status code
     */
    private void handleError(HttpServletResponse response, PrintWriter out,
            String message, int statusCode) {
        response.setStatus(statusCode);
        out.println("{" +
                "\"success\": false, " +
                "\"message\": \"" + escapeJson(message) + "\", " +
                "\"statusCode\": " + statusCode +
                "}");
    }

    /**
     * Escape special characters in JSON strings
     * 
     * @param text text to escape
     * @return escaped text safe for JSON
     */
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
