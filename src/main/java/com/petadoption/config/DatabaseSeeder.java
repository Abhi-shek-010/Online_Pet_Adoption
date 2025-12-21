package com.petadoption.config;

import com.petadoption.dao.PetDAO;
import com.petadoption.dao.UserDAO;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.model.User.UserType;
import com.petadoption.service.UserService;
import com.petadoption.util.PasswordUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Database Seeder
 * Populates the database with initial data if empty.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

        @Override
        public void run(String... args) throws Exception {
                System.out.println("Starting Database Seeding...");

                UserService userService = new UserService();
                PetDAO petDAO = new PetDAO();

                try {
                        // Check if shelters and pets exist
                        List<User> shelterUsers = userService.getUsersByType(UserType.SHELTER);
                        List<Pet> pets = petDAO.getAllPets();

                        if (shelterUsers.isEmpty()) {
                                System.out.println("No shelter users found. Seeding full data set...");
                                seedData(userService, petDAO);
                        } else {
                                System.out.println("Shelter users found. Checking/Seeding shelter_info and pets...");
                                // Force update passwords for demo users to ensure login works
                                forceUpdatePasswords(userService);

                                // Ensure shelter_info exists for these users and then seed pets if needed
                                seedPetsForExistingShelters(userService, petDAO, shelterUsers, pets.isEmpty());
                        }

                } catch (Exception e) {
                        System.err.println("Error seeding database: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void forceUpdatePasswords(UserService userService) {
                try {
                        updateUserPassword(userService, "shelter1@paws.com", "password123");
                        updateUserPassword(userService, "shelter2@haven.com", "password123");
                        updateUserPassword(userService, "shelter3@tails.com", "password123");
                        updateUserPassword(userService, "shelter4@furry.com", "password123");
                        updateUserPassword(userService, "shelter5@second.com", "password123");
                        updateUserPassword(userService, "shelter6@guardian.com", "password123");
                        updateUserPassword(userService, "happy@family.com", "password123");
                        System.out.println("Demo user passwords updated/verified.");
                } catch (Exception e) {
                        System.err.println("Error updating passwords: " + e.getMessage());
                }
        }

        private void updateUserPassword(UserService userService, String email, String newPassword) throws Exception {
                User user = userService.getUserByEmail(email);
                if (user != null) {
                        // We use the DAO directly or service change password method if verify old
                        // password is an issue
                        // UserService.changePassword requires old password. We might not know it.
                        // So we'll use a direct DAO update approach or just rely on re-hashing

                        // However, UserService doesn't expose a "force set password" method easily
                        // without old password
                        // Let's hack it using UserDAO directly or assuming we can just set it
                        com.petadoption.dao.UserDAO userDAO = new UserDAO();
                        String hashedPassword = PasswordUtils.hashPassword(newPassword);
                        userDAO.updateUserPassword(user.getUserId(), hashedPassword);
                }
        }

        private void seedData(UserService userService, PetDAO petDAO) throws Exception {
                // 1. Create Shelter Users and Info
                int shelterId1 = createShelter(userService, "shelter_paws", "shelter1@paws.com", "Paws & Claws Shelter",
                                "LIC-001");
                int shelterId2 = createShelter(userService, "shelter_haven", "shelter2@haven.com", "Safe Haven Rescue",
                                "LIC-002");
                int shelterId3 = createShelter(userService, "shelter_tails", "shelter3@tails.com",
                                "Happy Tails Sanctuary", "LIC-003");

                // 2. Create Adopter
                User adopter = new User();
                adopter.setUsername("happy_family");
                adopter.setEmail("happy@family.com");
                adopter.setFullName("The Smiths");
                adopter.setUserType(UserType.ADOPTER);
                try {
                        userService.registerUser(adopter, "password123");
                } catch (Exception e) {
                        System.out.println("Adopter already exists: " + e.getMessage());
                        updateUserPassword(userService, "happy@family.com", "password123");
                }

                // 3. Create Pets
                seedPets(petDAO, shelterId1, shelterId2, shelterId3);

                System.out.println("Seed completion.");
        }

        private void seedPetsForExistingShelters(UserService userService, PetDAO petDAO, List<User> shelterUsers,
                        boolean forceSeedPets) throws Exception {
                // We need at least 3 shelters for our seed data distribution
                // If fewer, we reuse them
                if (shelterUsers.isEmpty())
                        return;

                int[] shelterIds = new int[3];

                // Get valid shelter_ids (creating records in shelter_info if missing)
                for (int i = 0; i < 3; i++) {
                        User user = shelterUsers.get(i % shelterUsers.size());
                        // Generate a dummy license if one doesn't exist to avoid unique constraint
                        // issues if we execute this multiple times
                        // But verifyShelterInfoExists handles checking first.
                        String license = "LIC-" + user.getUsername().toUpperCase() + "-" + (i + 1);
                        shelterIds[i] = verifyShelterInfoExists(user, license);
                }

                if (forceSeedPets) {
                        System.out.println("Seeding pets...");
                        seedPets(petDAO, shelterIds[0], shelterIds[1], shelterIds[2]);
                } else {
                        System.out.println("Pets already exist. Skipping pet seeding.");
                }
        }

        private void seedPets(PetDAO petDAO, int id1, int id2, int id3) throws Exception {
                // Shelter 1 Pets
                createPet(petDAO, id1, "Max", "Dog", "Golden Retriever", 2, Pet.Gender.MALE,
                                "Very friendly and loves fetch. Great with kids.",
                                "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id1, "Luna", "Cat", "Siamese", 1, Pet.Gender.FEMALE,
                                "Vocal and affectionate. Likes high places.",
                                "https://images.unsplash.com/photo-1513245543132-31f507417b26?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id1, "Rocky", "Dog", "Bulldog", 4, Pet.Gender.MALE,
                                "Lazy but lovable. Snoring champion.",
                                "https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=600&q=80");

                // Shelter 2 Pets
                createPet(petDAO, id2, "Bella", "Dog", "Beagle", 3, Pet.Gender.FEMALE,
                                "Curious trail hound. Needs a fenced yard.",
                                "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id2, "Thumper", "Rabbit", "Holland Lop", 0, Pet.Gender.MALE,
                                "Loves carrots and head scratches.",
                                "https://images.unsplash.com/photo-1585110396067-c3d6e345b319?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id2, "Daisy", "Cat", "Tabby", 5, Pet.Gender.FEMALE,
                                "Quiet lap cat. Good for seniors.",
                                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80");

                // Shelter 3 Pets
                createPet(petDAO, id3, "Cooper", "Dog", "Australian Shepherd", 1, Pet.Gender.MALE,
                                "High energy, needs a job. Agility prospect.",
                                "https://images.unsplash.com/photo-1529429617124-95b109e86bb8?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id3, "Milo", "Dog", "Jack Russell", 2, Pet.Gender.MALE,
                                "Small but mighty. Excellent mouser.",
                                "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=600&q=80");
                createPet(petDAO, id3, "Simba", "Cat", "Maine Coon", 2, Pet.Gender.MALE,
                                "Gentle giant. Loves water.",
                                "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=600&q=80");
        }

        // Creates User + ShelterInfo and returns shelter_id
        private int createShelter(UserService userService, String username, String email, String fullName,
                        String license) throws Exception {
                User shelter = new User();
                shelter.setUsername(username);
                shelter.setEmail(email);
                shelter.setFullName(fullName);
                shelter.setUserType(UserType.SHELTER);

                try {
                        userService.registerUser(shelter, "password123");
                } catch (Exception e) {
                        System.out.println("User " + username + " already exists, retrieving...");
                        updateUserPassword(userService, email, "password123");
                }

                User createdUser = userService.getUserByEmail(email);
                return verifyShelterInfoExists(createdUser, license);
        }

        // Checks/Creates shelter_info entry and returns shelter_id
        private int verifyShelterInfoExists(User user, String license) throws Exception {
                try (java.sql.Connection conn = com.petadoption.config.DBConnection.getInstance().getConnection()) {
                        // Check if exists
                        String checkSql = "SELECT shelter_id FROM shelter_info WHERE user_id = ?";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(checkSql)) {
                                ps.setInt(1, user.getUserId());
                                try (java.sql.ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                                return rs.getInt(1);
                                        }
                                }
                        }

                        // Create if not exists
                        String insertSql = "INSERT INTO shelter_info (user_id, shelter_name, license_number, is_verified) VALUES (?, ?, ?, TRUE)";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(insertSql,
                                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                                ps.setInt(1, user.getUserId());
                                ps.setString(2, user.getFullName());
                                ps.setString(3, license);
                                ps.executeUpdate();

                                try (java.sql.ResultSet rs = ps.getGeneratedKeys()) {
                                        if (rs.next()) {
                                                return rs.getInt(1);
                                        }
                                }
                        }
                }
                throw new RuntimeException("Failed to get/create shelter_info for user " + user.getUserId());
        }

        private void createPet(PetDAO petDAO, int shelterId, String name, String species, String breed, int age,
                        Pet.Gender gender, String desc, String img) throws Exception {
                Pet pet = new Pet();
                pet.setShelterId(shelterId);
                pet.setPetName(name);
                pet.setSpecies(species);
                pet.setBreed(breed);
                pet.setAgeYears(age);
                pet.setAgeMonths(0);
                pet.setGender(gender);
                pet.setDescription(desc);
                pet.setImageUrl(img);
                pet.setAdoptionStatus(Pet.AdoptionStatus.AVAILABLE);

                // Defaults
                pet.setWeightKg(new BigDecimal("10.0"));
                pet.setColor("Mixed");
                pet.setHealthStatus("Healthy");
                pet.setVaccinationStatus(Pet.VaccinationStatus.COMPLETE);
                pet.setNeuteredSpayed(true);
                pet.setMicrochipNumber("N/A");
                pet.setSpecialNeeds("None");
                pet.setAdoptionFee(new BigDecimal("50.00"));
                pet.setIntakeDate(LocalDate.now());

                petDAO.createPet(pet);
        }
}
