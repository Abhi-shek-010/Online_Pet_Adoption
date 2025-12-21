package com.petadoption.servlet;

import com.petadoption.model.Adoption;
import com.petadoption.service.AdoptionService;
import com.petadoption.util.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Adoption Records Servlet
 * 
 * Handles requests for finalized adoption records.
 * 
 * Endpoints:
 * - GET /api/adoptions/happy-families
 * - GET /api/adoptions/my-pets
 * 
 * @author Pet Adoption System Team
 */
@WebServlet(name = "AdoptionRecordsServlet", urlPatterns = { "/api/adoptions/happy-families",
        "/api/adoptions/my-pets" })
public class AdoptionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AdoptionService adoptionService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.adoptionService = new AdoptionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String path = request.getServletPath();

        try {
            if (path.contains("happy-families")) {
                handleHappyFamilies(response, out);
            } else if (path.contains("my-pets")) {
                handleMyPets(request, response, out);
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

    private void handleHappyFamilies(HttpServletResponse response, PrintWriter out) throws SQLException {
        List<Adoption> adoptions = adoptionService.getHappyFamilies();

        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"count\": " + adoptions.size() + ",");
        out.println("  \"adoptions\": [");

        for (int i = 0; i < adoptions.size(); i++) {
            out.print("    " + adoptionToJson(adoptions.get(i), true)); // Include adopter name
            if (i < adoptions.size() - 1)
                out.println(",");
            else
                out.println();
        }

        out.println("  ]");
        out.println("}");
    }

    private void handleMyPets(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws SQLException {
        HttpSession session = request.getSession(false);
        if (!SessionUtils.isUserLoggedIn(session)) {
            handleError(response, out, "User not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = SessionUtils.getUserIdFromSession(session);
        List<Adoption> adoptions = adoptionService.getAdoptedPetsByUser(userId);

        response.setStatus(HttpServletResponse.SC_OK);
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"count\": " + adoptions.size() + ",");
        out.println("  \"pets\": [");

        for (int i = 0; i < adoptions.size(); i++) {
            out.print("    " + adoptionToJson(adoptions.get(i), false)); // Don't need adopter name (it's me)
            if (i < adoptions.size() - 1)
                out.println(",");
            else
                out.println();
        }

        out.println("  ]");
        out.println("}");
    }

    private String adoptionToJson(Adoption ad, boolean includeAdopter) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"adoptionId\": ").append(ad.getAdoptionId()).append(", ");
        json.append("\"petId\": ").append(ad.getPetId()).append(", ");
        json.append("\"petName\": \"").append(escapeJson(ad.getPetName())).append("\", ");
        json.append("\"species\": \"").append(escapeJson(ad.getSpecies())).append("\", ");
        json.append("\"breed\": \"").append(escapeJson(ad.getBreed())).append("\", ");
        json.append("\"adoptionDate\": \"").append(ad.getAdoptionDate().toLocalDate()).append("\"");

        if (includeAdopter) {
            json.append(", \"adopterName\": \"").append(escapeJson(ad.getAdopterName())).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    private void handleError(HttpServletResponse response, PrintWriter out, String message, int status) {
        response.setStatus(status);
        out.println("{ \"success\": false, \"message\": \"" + escapeJson(message) + "\" }");
    }

    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\"", "\\\"");
    }
}
