package com.example.TTN_E_Commerce.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.TTN_E_Commerce.Entity.Category;
import com.example.TTN_E_Commerce.Entity.Product;
import com.example.TTN_E_Commerce.Entity.ProductVariation;
import com.example.TTN_E_Commerce.Entity.Role;
import com.example.TTN_E_Commerce.Entity.Seller;
import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Repository.CategoryRepository;
import com.example.TTN_E_Commerce.Repository.RoleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Order(2)
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository,
            CategoryRepository categoryRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Categories if none exist, or load them
        Map<String, Category> categoryMap = new HashMap<>();
        if (categoryRepository.count() > 0) {
            System.out.println("[DatabaseSeeder] Categories already exist. Loading categories map...");
            for (Category c : categoryRepository.findAll()) {
                categoryMap.put(c.getName(), c);
            }
        } else {
            System.out.println("[DatabaseSeeder] Seeding categories...");
            String[] categoryNames = {
                "Bath & Personal",
                "Cleaning",
                "Bed",
                "Storage",
                "Kitchen & Dining",
                "Electronics",
                "Décor & Furniture",
                "For Class",
                "Extras"
            };

            for (String name : categoryNames) {
                Category category = new Category();
                category.setName(name);
                entityManager.persist(category);
                categoryMap.put(name, category);
            }
        }

        // 2. Seed Default Seller or load existing
        Long sellerUserCount = entityManager.createQuery("select count(u) from User u where u.email = 'seller@hostelkart.com'", Long.class)
                .getSingleResult();
        Seller seller;
        if (sellerUserCount > 0) {
            System.out.println("[DatabaseSeeder] Default seller already exists. Loading existing seller...");
            seller = entityManager.createQuery("select s from Seller s where s.user.email = 'seller@hostelkart.com'", Seller.class)
                    .getSingleResult();
        } else {
            System.out.println("[DatabaseSeeder] Seeding default seller...");
            User sellerUser = new User();
            sellerUser.setEmail("seller@hostelkart.com");
            sellerUser.setFirstName("Alpha");
            sellerUser.setLastName("Sellers");
            sellerUser.setPassword(passwordEncoder.encode("seller123"));
            sellerUser.setActive(true);
            sellerUser.setDeleted(false);

            Role sellerRole = roleRepository.findByAuthority(RoleType.SELLER)
                    .orElseThrow(() -> new RuntimeException("Role SELLER not found"));
            sellerUser.setRoles(Set.of(sellerRole));
            entityManager.persist(sellerUser);

            seller = new Seller();
            seller.setUser(sellerUser);
            seller.setGst("07AAAAA1111A1Z1");
            seller.setCompanyName("Alpha Retailers Pvt Ltd");
            seller.setCompanyContact("9876543211");
            entityManager.persist(seller);
        };
        // 3. Seed Demo Products

        // Bed
        seedProduct(
                "Sleepwell Memory Foam Mattress (6x3 ft)",
                "Sleepwell",
                "Bed",
                "Single size 6x3 orthopedic medium firm mattress. Designed for absolute student sleeping comfort.",
                seller,
                categoryMap,
                3899.0,
                25,
                "{\"Size\": \"Single 6x3\", \"Firmness\": \"Orthopedic Medium\"}",
                "https://m.media-amazon.com/images/I/71nZ2fB9vLL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Bombay Dyeing Single Satin Sheets Set",
                "Bombay Dyeing",
                "Bed",
                "Luxurious and smooth slate blue single satin sheets set. Elevates your hostel room aesthetic.",
                seller,
                categoryMap,
                799.0,
                45,
                "{\"Color\": \"Slate Blue\", \"Size\": \"Single\"}",
                "https://m.media-amazon.com/images/I/71k42xW-LIL._AC_SL1500_.jpg"
        );

        // Bath & Personal
        seedProduct(
                "Water-Resistant Canvas Dopp Kit Toiletries Bag",
                "Solimo",
                "Bath & Personal",
                "Spacious toiletries travel bag with water-resistant lining, multi-compartment dividers, and a sturdy handle.",
                seller,
                categoryMap,
                549.0,
                40,
                "{\"Color\": \"Charcoal Grey\", \"Material\": \"Waxed Canvas\"}",
                "https://m.media-amazon.com/images/I/81hB8cK6JWL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Premium Microfiber Towel Set (2-Piece)",
                "Solimo",
                "Bath & Personal",
                "Super absorbent, ultra quick-drying microfiber towels that are light on the skin and highly compact.",
                seller,
                categoryMap,
                549.0,
                40,
                "{\"Color\": \"Charcoal Grey\", \"Material\": \"Microfiber\"}",
                "https://m.media-amazon.com/images/I/81hB8cK6JWL._AC_SL1500_.jpg"
        );

        // Cleaning
        seedProduct(
                "Heavy-Duty Mesh Laundry Bag with Drawstring",
                "Tide",
                "Cleaning",
                "Durable and breathable mesh laundry bag that holds up to 3 loads of college clothes.",
                seller,
                categoryMap,
                249.0,
                60,
                "{\"Color\": \"Pure White\", \"Size\": \"Jumbo\"}",
                "https://m.media-amazon.com/images/I/81Pz-0u6aWL._AC_SL1500_.jpg"
        );

        // Decor & Furniture
        seedProduct(
                "Dimmable LED Desk Lamp with USB Charger Port",
                "Lumens",
                "Décor & Furniture",
                "Flexible dimmable desk lamp with 3 color temperatures, LCD calendar display, clock, and built-in USB ports.",
                seller,
                categoryMap,
                999.0,
                45,
                "{\"Color\": \"Matte Black\", \"Brightness\": \"900lm\"}",
                "https://m.media-amazon.com/images/I/61jCtzH-EUL._AC_SL1500_.jpg"
        );

        // Electronics
        seedProduct(
                "Sony WH-1000XM4 Active Noise Canceling Headphones",
                "Sony",
                "Electronics",
                "Industry-leading active noise canceling headphones. Block out roommate noise and focus on study playlists.",
                seller,
                categoryMap,
                19999.0,
                15,
                "{\"Color\": \"Midnight Blue\"}",
                "https://m.media-amazon.com/images/I/71o8Q5hCeVL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Belkin 4-Outlet Surge Protector (2-meter cord)",
                "Belkin",
                "Electronics",
                "Safeguard your electronics with a robust surge protector. Essential for multiple devices in hostel rooms.",
                seller,
                categoryMap,
                199.0,
                120,
                "{\"Size\": \"2m Cord\", \"Outlets\": \"4 Outlets\"}",
                "https://m.media-amazon.com/images/I/71F9yOsqfYL._AC_SL1500_.jpg"
        );

        // For Class
        seedProduct(
                "Moleskine Classic Hardcover Notebooks (3-Pack)",
                "Moleskine",
                "For Class",
                "Ruled, acid-free pages in classic black hardcover binding. Your ultimate college lecture companion.",
                seller,
                categoryMap,
                499.0,
                30,
                "{\"Size\": \"Pack of 3\", \"PaperType\": \"Ruled\"}",
                "https://m.media-amazon.com/images/I/71nZ2fB9vLL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Wildcraft Alpinist Backpack (Stealth Black)",
                "Wildcraft",
                "For Class",
                "Rugged 45L high durability backpack for carrying study gear, heavy textbooks, and college laptops.",
                seller,
                categoryMap,
                2499.0,
                50,
                "{\"Color\": \"Carbon Black\", \"Capacity\": \"45L\"}",
                "https://m.media-amazon.com/images/I/81hB8cK6JWL._AC_SL1500_.jpg"
        );

        // Kitchen & Dining
        seedProduct(
                "Milton Thermosteel Insulated Steel Water Bottle",
                "Milton",
                "Kitchen & Dining",
                "Double-walled vacuum insulated hot & cold water bottle. Keeps water icy cold or piping hot for up to 24 hours.",
                seller,
                categoryMap,
                799.0,
                90,
                "{\"Color\": \"Brushed Steel\", \"Capacity\": \"1.0L\"}",
                "https://m.media-amazon.com/images/I/61B+M9xX2pL._SL1500_.jpg"
        );

        // Missing products to complete the 20 products catalog
        seedProduct(
                "Axe Phoenix Body Wash for Men (Pack of 3)",
                "Axe",
                "Bath & Personal",
                "Axe Phoenix Body wash leaves you feeling clean, fresh, and smelling like refreshing mint and rosemary. Specially formulated for a quick, clarifying campus shower.",
                seller,
                categoryMap,
                499.0,
                50,
                "{\"PackSize\": \"Pack of 3\"}",
                "https://m.media-amazon.com/images/I/81Pz-0u6aWL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Tide 3-in-1 Laundry Detergent Pods (42 Count)",
                "Tide",
                "Cleaning",
                "Tide PODS is a 3-in-1 laundry solution: detergent, stain remover, and color protector. Super convenient for hostel washing machines.",
                seller,
                categoryMap,
                899.0,
                40,
                "{\"Count\": \"42 Count\"}",
                "https://m.media-amazon.com/images/I/81Pz-0u6aWL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Dyson V8 Origin Cordless Stick Vacuum Cleaner",
                "Dyson",
                "Cleaning",
                "Powerful, lightweight cordless vacuum engineered for deep cleaning hostel carpets, hard floors, and tight room corners.",
                seller,
                categoryMap,
                28999.0,
                10,
                "{\"Color\": \"Purple\", \"Type\": \"Cordless\"}",
                "https://m.media-amazon.com/images/I/61jCtzH-EUL._AC_SL1500_.jpg"
        );

        seedProduct(
                "3M Command Damage-Free Utility Hooks (12-Pack)",
                "3M",
                "Storage",
                "Adhesive hooks that hold strongly and remove cleanly without damaging hostel walls. Perfect for keys, headphones, jackets, and towels.",
                seller,
                categoryMap,
                199.0,
                150,
                "{\"PackSize\": \"12 Hooks\", \"Type\": \"Adhesive\"}",
                "https://m.media-amazon.com/images/I/71F9yOsqfYL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Solimo 5-Shelf Hanging Wardrobe Closet Organizer",
                "Solimo",
                "Storage",
                "Fabric shelves that hang directly inside narrow wardrobes. Maximizes closet space to store clothes neatly.",
                seller,
                categoryMap,
                499.0,
                35,
                "{\"Color\": \"Grey\", \"Shelves\": \"5-Shelf\"}",
                "https://m.media-amazon.com/images/I/81hB8cK6JWL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Keurig K-Mini Single Serve Pod Coffee Maker",
                "Keurig",
                "Kitchen & Dining",
                "Ultra-compact single-serve coffeemaker that fits comfortably on any desk. Brews standard coffee pods in under 2 minutes.",
                seller,
                categoryMap,
                5999.0,
                15,
                "{\"Color\": \"Black\", \"Type\": \"Single Serve\"}",
                "https://m.media-amazon.com/images/I/61B+M9xX2pL._SL1500_.jpg"
        );

        seedProduct(
                "Anker PowerCore 10000mAh Portable Phone Charger",
                "Anker",
                "Electronics",
                "Ultra-compact, light high-speed phone power bank. Fits inside backpacks and pocket sleeves.",
                seller,
                categoryMap,
                1899.0,
                80,
                "{\"Capacity\": \"10000mAh\", \"Color\": \"Black\"}",
                "https://m.media-amazon.com/images/I/71F9yOsqfYL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Solimo Cozy Tufted Floor Cushion Ottoman Seat",
                "Solimo",
                "Décor & Furniture",
                "Plush, tufted round floor ottoman cushion. Provides extra comfortable seating on the floor for roommates.",
                seller,
                categoryMap,
                1199.0,
                30,
                "{\"Color\": \"Teal\", \"Shape\": \"Round\"}",
                "https://m.media-amazon.com/images/I/81hB8cK6JWL._AC_SL1500_.jpg"
        );

        seedProduct(
                "Decathlon Heavy-Duty Steel Bike U-Lock",
                "Decathlon",
                "Extras",
                "High-security anti-pick hardened steel U-lock to lock bicycles, scooters, or cabinets safely on campus grounds.",
                seller,
                categoryMap,
                899.0,
                50,
                "{\"Material\": \"Hardened Steel\", \"Type\": \"U-Lock\"}",
                "https://m.media-amazon.com/images/I/71F9yOsqfYL._AC_SL1500_.jpg"
        );

        System.out.println("[DatabaseSeeder] Seeding completed successfully.");
    }

    private void seedProduct(
            String name,
            String brand,
            String categoryName,
            String description,
            Seller seller,
            Map<String, Category> categoryMap,
            Double price,
            Integer qty,
            String metadataJson,
            String imgUrl
    ) {
        Long existingCount = entityManager.createQuery("select count(p) from Product p where p.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        if (existingCount > 0) {
            return;
        }

        Product product = new Product();
        // Do not manually set product ID to let Hibernate generate the UUID dynamically on persist.
        product.setName(name);
        product.setBrand(brand);
        product.setDescription(description);
        product.setCategory(categoryMap.get(categoryName));
        product.setSeller(seller);
        product.setIsActive(true);
        product.setIsCancellable(true);
        product.setIsReturnable(true);
        product.setIsDeleted(false);
        entityManager.persist(product);

        ProductVariation variation = new ProductVariation();
        // Do not manually set variation ID to let Hibernate generate the UUID dynamically on persist.
        variation.setProduct(product);
        variation.setPrice(price);
        variation.setQuantityAvailable(qty);
        variation.setMetadata(metadataJson);
        variation.setPrimaryImageName(imgUrl);
        variation.setIsActive(true);
        entityManager.persist(variation);
    }
}
