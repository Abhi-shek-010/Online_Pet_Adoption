-- =====================================================
-- Seed Script for PawMatch Shelters and Pets
-- =====================================================

-- Use INSERT IGNORE to skip if exists (assuming email/username unique)
INSERT IGNORE INTO users (username, email, password_hash, full_name, user_type, is_active) VALUES 
('shelter_paws', 'shelter1@paws.com', 'hashed_pw_1', 'Paws & Claws Shelter', 'SHELTER', 1),
('shelter_haven', 'shelter2@haven.com', 'hashed_pw_2', 'Safe Haven Rescue', 'SHELTER', 1),
('shelter_tails', 'shelter3@tails.com', 'hashed_pw_3', 'Happy Tails Sanctuary', 'SHELTER', 1),
('shelter_furry', 'shelter4@furry.com', 'hashed_pw_5', 'Furry Friends Foundation', 'SHELTER', 1),
('shelter_second', 'shelter5@second.com', 'hashed_pw_6', 'Second Chance Animal Rescue', 'SHELTER', 1),
('shelter_guardian', 'shelter6@guardian.com', 'hashed_pw_7', 'Guardian Angels Pet Shelter', 'SHELTER', 1);

-- Get User IDs of shelters
SET @user1_id = (SELECT user_id FROM users WHERE email = 'shelter1@paws.com');
SET @user2_id = (SELECT user_id FROM users WHERE email = 'shelter2@haven.com');
SET @user3_id = (SELECT user_id FROM users WHERE email = 'shelter3@tails.com');
SET @user4_id = (SELECT user_id FROM users WHERE email = 'shelter4@furry.com');
SET @user5_id = (SELECT user_id FROM users WHERE email = 'shelter5@second.com');
SET @user6_id = (SELECT user_id FROM users WHERE email = 'shelter6@guardian.com');

-- Create shelter_info entries (required for pets table FK)
INSERT IGNORE INTO shelter_info (user_id, shelter_name, license_number, contact_person, capacity, description, is_verified) VALUES
(@user1_id, 'Paws & Claws Shelter', 'LIC-PAW-001', 'John Smith', 50, 'Giving every paw a second chance', TRUE),
(@user2_id, 'Safe Haven Rescue', 'LIC-HAV-002', 'Maria Garcia', 40, 'Where healing begins, and love never ends', TRUE),
(@user3_id, 'Happy Tails Sanctuary', 'LIC-TAL-003', 'Mike Chen', 60, 'Wagging tails, happy homes', TRUE),
(@user4_id, 'Furry Friends Foundation', 'LIC-FUR-004', 'Sarah Johnson', 75, 'Every animal deserves unconditional love', TRUE),
(@user5_id, 'Second Chance Animal Rescue', 'LIC-SEC-005', 'David Park', 45, 'New beginnings for forgotten souls', TRUE),
(@user6_id, 'Guardian Angels Pet Shelter', 'LIC-GUA-006', 'Emily Rodriguez', 55, 'Protecting paws, one rescue at a time', TRUE);

-- Get Shelter IDs (from shelter_info table)
SET @shelter1_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user1_id);
SET @shelter2_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user2_id);
SET @shelter3_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user3_id);
SET @shelter4_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user4_id);
SET @shelter5_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user5_id);
SET @shelter6_id = (SELECT shelter_id FROM shelter_info WHERE user_id = @user6_id);

-- Insert Pets for Shelter 1 (Paws & Claws - Dogs/Cats)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter1_id, 'Max', 'Dog', 'Golden Retriever', 2, 0, 'MALE', 'Very friendly and loves fetch. Great with kids.', 'AVAILABLE', 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=600&q=80'),
(@shelter1_id, 'Luna', 'Cat', 'Siamese', 1, 6, 'FEMALE', 'Vocal and affectionate. Likes high places.', 'AVAILABLE', 'https://images.unsplash.com/photo-1513245543132-31f507417b26?auto=format&fit=crop&w=600&q=80'),
(@shelter1_id, 'Rocky', 'Dog', 'Bulldog', 4, 0, 'MALE', 'Lazy but lovable. Snoring champion.', 'AVAILABLE', 'https://images.unsplash.com/photo-1517849845537-4d257902454a?auto=format&fit=crop&w=600&q=80'),
(@shelter1_id, 'Sunny', 'Bird', 'Cockatiel', 1, 0, 'MALE', 'Cheerful singer who loves to whistle tunes.', 'AVAILABLE', 'https://images.unsplash.com/photo-1552728089-57bdde30beb3?auto=format&fit=crop&w=600&q=80');

-- Insert Pets for Shelter 2 (Safe Haven - Small animals/Mixed)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter2_id, 'Bella', 'Dog', 'Beagle', 3, 0, 'FEMALE', 'Curious trail hound. Needs a fenced yard.', 'AVAILABLE', 'https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?auto=format&fit=crop&w=600&q=80'),
(@shelter2_id, 'Thumper', 'Rabbit', 'Holland Lop', 0, 9, 'MALE', 'Loves carrots and head scratches.', 'AVAILABLE', 'https://images.unsplash.com/photo-1585110396067-c3d6e345b319?auto=format&fit=crop&w=600&q=80'),
(@shelter2_id, 'Daisy', 'Cat', 'Tabby', 5, 0, 'FEMALE', 'Quiet lap cat. Good for seniors.', 'AVAILABLE', 'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=600&q=80'),
(@shelter2_id, 'Kiwi', 'Bird', 'Budgie', 0, 8, 'FEMALE', 'Playful parakeet with bright green feathers.', 'AVAILABLE', 'https://images.unsplash.com/photo-1544923408-75c5cef46f14?auto=format&fit=crop&w=600&q=80');

-- Insert Pets for Shelter 3 (Happy Tails - Active dogs)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter3_id, 'Cooper', 'Dog', 'Australian Shepherd', 1, 0, 'MALE', 'High energy, needs a job. Agility prospect.', 'AVAILABLE', 'https://images.unsplash.com/photo-1529429617124-95b109e86bb8?auto=format&fit=crop&w=600&q=80'),
(@shelter3_id, 'Milo', 'Dog', 'Jack Russell', 2, 4, 'MALE', 'Small but mighty. Excellent mouser.', 'AVAILABLE', 'https://images.unsplash.com/photo-1558788353-f76d92427f16?auto=format&fit=crop&w=600&q=80'),
(@shelter3_id, 'Simba', 'Cat', 'Maine Coon', 2, 0, 'MALE', 'Gentle giant. Loves water.', 'AVAILABLE', 'https://images.unsplash.com/photo-1533738363-b7f9aef128ce?auto=format&fit=crop&w=600&q=80'),
(@shelter3_id, 'Rio', 'Bird', 'Blue Macaw', 4, 0, 'MALE', 'Stunning blue parrot. Loves to talk and dance.', 'AVAILABLE', 'https://images.unsplash.com/photo-1544923408-75c5cef46f14?auto=format&fit=crop&w=600&q=80');

-- Insert Pets for Shelter 4 (Furry Friends Foundation)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter4_id, 'Charlie', 'Dog', 'Labrador', 3, 0, 'MALE', 'Loves swimming and playing fetch. Family favorite.', 'AVAILABLE', 'https://images.unsplash.com/photo-1591769225440-811ad7d6eab3?auto=format&fit=crop&w=600&q=80'),
(@shelter4_id, 'Mittens', 'Cat', 'Calico', 2, 6, 'FEMALE', 'Sweet and playful. Gets along with other cats.', 'AVAILABLE', 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=600&q=80'),
(@shelter4_id, 'Duke', 'Dog', 'Great Dane', 4, 0, 'MALE', 'Gentle giant who thinks he is a lap dog.', 'AVAILABLE', 'https://images.unsplash.com/photo-1558929996-da64ba858215?auto=format&fit=crop&w=600&q=80'),
(@shelter4_id, 'Polly', 'Bird', 'African Grey', 5, 0, 'FEMALE', 'Highly intelligent. Can mimic over 100 words.', 'AVAILABLE', 'https://images.unsplash.com/photo-1555169062-013468b47731?auto=format&fit=crop&w=600&q=80');

-- Insert Pets for Shelter 5 (Second Chance Animal Rescue)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter5_id, 'Shadow', 'Cat', 'Black Shorthair', 1, 0, 'MALE', 'Mysterious and affectionate. Loves hiding spots.', 'AVAILABLE', 'https://images.unsplash.com/photo-1606214174585-fe31582dc6ee?auto=format&fit=crop&w=600&q=80'),
(@shelter5_id, 'Rosie', 'Dog', 'Poodle Mix', 5, 0, 'FEMALE', 'Senior sweetheart looking for a quiet home.', 'AVAILABLE', 'https://images.unsplash.com/photo-1583511655826-05700d52f4d9?auto=format&fit=crop&w=600&q=80'),
(@shelter5_id, 'Scout', 'Dog', 'Border Collie', 2, 0, 'MALE', 'Incredibly smart. Needs mental stimulation.', 'AVAILABLE', 'https://images.unsplash.com/photo-1503256207526-0d5d80fa2f47?auto=format&fit=crop&w=600&q=80'),
(@shelter5_id, 'Tweety', 'Bird', 'Canary', 1, 6, 'MALE', 'Beautiful yellow singer. Fills the room with song.', 'AVAILABLE', 'https://images.unsplash.com/photo-1591198936750-16d8e15eec4f?auto=format&fit=crop&w=600&q=80');

-- Insert Pets for Shelter 6 (Guardian Angels Pet Shelter)
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter6_id, 'Princess', 'Cat', 'Ragdoll', 3, 0, 'FEMALE', 'Fluffy and regal. Expects royal treatment.', 'AVAILABLE', 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=600&q=80'),
(@shelter6_id, 'Zeus', 'Dog', 'Husky', 2, 6, 'MALE', 'Vocal and energetic. Loves cold weather.', 'AVAILABLE', 'https://images.unsplash.com/photo-1605568427561-40dd23c2acea?auto=format&fit=crop&w=600&q=80'),
(@shelter6_id, 'Cleo', 'Cat', 'Bengal', 1, 6, 'FEMALE', 'Wild at heart. Loves climbing and exploring.', 'AVAILABLE', 'https://images.unsplash.com/photo-1592194996308-7b43878e84a6?auto=format&fit=crop&w=600&q=80'),
(@shelter6_id, 'Phoenix', 'Bird', 'Lovebird', 1, 0, 'FEMALE', 'Colorful and social. Bonds deeply with owners.', 'AVAILABLE', 'https://images.unsplash.com/photo-1452570053594-1b985d6ea890?auto=format&fit=crop&w=600&q=80');

-- Add some pre-existing adoptions for "Happy Families" demo
INSERT IGNORE INTO users (username, email, password_hash, full_name, user_type, is_active) VALUES 
('happy_family', 'happy@family.com', 'hashed_pw_4', 'The Smiths', 'ADOPTER', 1);

SET @adopter_user_id = (SELECT user_id FROM users WHERE email = 'happy@family.com');

-- Create adopter_info entry (required for adoption_applications FK)
INSERT IGNORE INTO adopter_info (user_id, employment_status, home_type, has_other_pets, rent_or_own) VALUES
(@adopter_user_id, 'Full-time', 'House', FALSE, 'Own');

SET @adopter_id = (SELECT adopter_id FROM adopter_info WHERE user_id = @adopter_user_id);

-- Add an adopted pet for Happy Families demo
INSERT IGNORE INTO pets (shelter_id, pet_name, species, breed, age_years, age_months, gender, description, adoption_status, image_url) VALUES
(@shelter1_id, 'Lucky', 'Dog', 'Mixed', 5, 0, 'MALE', 'A very lucky boy who found his forever home.', 'ADOPTED', 'https://images.unsplash.com/photo-1561037404-61cd46aa615b?auto=format&fit=crop&w=600&q=80');

SET @lucky_id = (SELECT pet_id FROM pets WHERE pet_name = 'Lucky' LIMIT 1);

-- Create adoptions record
INSERT IGNORE INTO adoptions (adopter_id, pet_id, adoption_date, contract_signed) VALUES
(@adopter_user_id, @lucky_id, DATE_SUB(NOW(), INTERVAL 14 DAY), 1);

-- =====================================================
-- Script Complete
-- =====================================================
SELECT 'Seeding complete!' as Status, 
       (SELECT COUNT(*) FROM users WHERE user_type = 'SHELTER') as Shelters,
       (SELECT COUNT(*) FROM pets) as Pets;
