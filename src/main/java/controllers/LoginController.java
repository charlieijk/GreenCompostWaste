package controllers;

import java.util.HashMap;
import java.util.Map;
import com.greencompost.User;
import com.greencompost.model.DatabaseManager;
import com.greencompost.main.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the login view
 */
public class LoginController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordButton;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private VBox loginPane;
    @FXML private Button resetPasswordButton;
    
    // Mock user repository for demo purposes
    // In a real application, this would connect to a database
    private UserRepository userRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user repository
        userRepository = new UserRepository();
        
        // Set up default view
        loginPane.setVisible(true);
        
        // Show demo credentials hint
        statusLabel.setText("Demo login: username \"demo\", password \"password\"");
        statusLabel.setStyle("-fx-text-fill: -gc-info;");
        
        // Configure password fields and their text field equivalents
        setupPasswordFields();
        
        // Add demo user for testing
        addDemoUser();
    }
    
    /**
     * Set up synchronization between password fields and their text field equivalents
     */
    private void setupPasswordFields() {
        // Set up login password field synchronization
        passwordField.textProperty().addListener((unused1, unused2, newValue) -> {
            passwordTextField.setText(newValue);
        });
        
        passwordTextField.textProperty().addListener((unused1, unused2, newValue) -> {
            passwordField.setText(newValue);
        });
    }
    
    /**
     * Toggle visibility of the login password
     */
    @FXML
    private void togglePasswordVisibility() {
        boolean showPassword = !passwordTextField.isVisible();
        
        // Toggle visibility
        passwordField.setVisible(!showPassword);
        passwordTextField.setVisible(showPassword);
        
        // Update which field has focus and set button text
        if (showPassword) {
            passwordTextField.requestFocus();
            togglePasswordButton.setText("🔒");
        } else {
            passwordField.requestFocus();
            togglePasswordButton.setText("👁");
        }
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        // Get password from whichever field is visible
        String password = passwordField.isVisible() ? 
                          passwordField.getText() : 
                          passwordTextField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password.", true);
            return;
        }
        
        // Try database authentication first (real app functionality)
        com.greencompost.User dbUser = com.greencompost.User.findByUsername(username);
        if (dbUser != null && dbUser.getPassword().equals(password)) {
            showStatus("Login successful!", false);
            
            // Store if "Remember Me" is selected
            if (rememberMeCheckbox.isSelected()) {
                dbUser.setRememberMe(true);
                // Save to database
                com.greencompost.model.DatabaseManager.getInstance().saveUser(dbUser);
            }
            
            // Set current user in Main
            try {
                com.greencompost.main.Main.setCurrentUser(dbUser);
                // Initialize demo data for user
                com.greencompost.main.Main.initializeSampleDataForUser();
                // Also set in MainController for the GUI
                MainController.setCurrentUser(dbUser);
                
                // Open main application window
                openMainApplication();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showStatus("Error setting current user: " + e.getMessage(), true);
                return;
            }
        }
        
        // Fall back to mock repository for demo purposes
        if (userRepository.userExists(username)) {
            User user = userRepository.authenticate(username, password);
            if (user != null) {
                // Authentication successful with mock repository
                showStatus("Login successful!", false);
                
                // Store if "Remember Me" is selected
                if (rememberMeCheckbox.isSelected()) {
                    storeUserCredentials(username);
                }
                
                // Set current user in Main directly
                try {
                    com.greencompost.main.Main.setCurrentUser(user);
                    // Initialize demo data for the authenticated user
                    com.greencompost.main.Main.initializeSampleDataForUser();
                    // Also set in MainController for the GUI
                    MainController.setCurrentUser(user);
                } catch (Exception e) {
                    e.printStackTrace();
                    showStatus("Error setting current user: " + e.getMessage(), true);
                    return;
                }
                
                // Open main application window
                openMainApplication();
            } else {
                // Authentication failed
                showStatus("Invalid username or password.", true);
            }
        } else {
            // User doesn't exist in mock repository either
            showStatus("Username not found. Please register first.", true);
        }
    }
    
    /**
     * Navigate to registration view
     */
    @FXML
    private void switchToRegister() {
        try {
            URL registerViewURL = getClass().getResource("/views/RegisterView.fxml");
            if (registerViewURL == null) {
                registerViewURL = getClass().getResource("../views/RegisterView.fxml");
            }
            
            Parent root = FXMLLoader.load(registerViewURL);
            Scene scene = new Scene(root);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("GreenCompost Connect - Register");
            stage.show();
        } catch (IOException e) {
            showStatus("Error loading registration page: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to reset password view
     */
    @FXML
    private void showResetPassword() {
        try {
            URL resetPasswordViewURL = getClass().getResource("/views/ResetPasswordView.fxml");
            if (resetPasswordViewURL == null) {
                resetPasswordViewURL = getClass().getResource("../views/ResetPasswordView.fxml");
            }
            
            Parent root = FXMLLoader.load(resetPasswordViewURL);
            Scene scene = new Scene(root);
            Stage stage = (Stage) resetPasswordButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("GreenCompost Connect - Reset Password");
            stage.show();
        } catch (IOException e) {
            showStatus("Error loading reset password page: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? 
            "-fx-text-fill: -gc-error;" : 
            "-fx-text-fill: -gc-success;"
        );
    }
    
    /**
     * Store user credentials for "Remember Me" functionality
     */
    private void storeUserCredentials(String username) {
        // In a real application, this would store credentials securely
        // For demo purposes, we'll just print to console
        System.out.println("Storing credentials for user: " + username);
    }
    
    /**
     * Open the main application window
     */
    private void openMainApplication() {
        try {
            // Load main view
            URL mainViewURL = getClass().getResource("/views/MainView.fxml");
            if (mainViewURL == null) {
                mainViewURL = getClass().getResource("../views/MainView.fxml");
            }
            
            Parent root = FXMLLoader.load(mainViewURL);
            
            // Create new scene
            Scene scene = new Scene(root);
            
            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Set new scene
            stage.setScene(scene);
            stage.setTitle("GreenCompost Connect");
            stage.show();
            
        } catch (IOException e) {
            showStatus("Error loading main application: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Add a demo user for testing
     */
    private void addDemoUser() {
        LOGGER.log(Level.INFO, "Adding demo user");
        
        // Check if demo user already exists in database
        com.greencompost.User dbDemoUser = com.greencompost.User.findByUsername("demo");
        if (dbDemoUser != null) {
            LOGGER.log(Level.INFO, "Demo user already exists in database");
            // Add the existing user to our repository
            userRepository.addUser("demo", "password", dbDemoUser);
            return;
        }
        
        // Try through Main class
        try {
            User existingUser = Main.getCurrentUser();
            if (existingUser != null) {
                // Add the existing user to our repository
                userRepository.addUser("demo", "password", existingUser);
                return;
            }
        } catch (Exception e) {
            // If error or null, create a new demo user
            LOGGER.log(Level.INFO, "Creating new demo user: {0}", e.getMessage());
        }
        
        // Create new demo user with proper initialization
        User demoUser = new User();
        demoUser.setUsername("demo");
        demoUser.setPassword("password"); // Ensure password is set
        demoUser.setEmail("demo@example.com");
        demoUser.setLocation("Cork City");
        demoUser.setLatitude(51.9);
        demoUser.setLongitude(-8.47);
        
        // Add to mock repository
        userRepository.addUser("demo", "password", demoUser);
        
        // Also create in database if not exists
        try {
            if (com.greencompost.User.findByUsername("demo") == null) {
                com.greencompost.User dbUser = new com.greencompost.User();
                dbUser.setUsername("demo");
                dbUser.setPassword("password");
                dbUser.setEmail("demo@example.com");
                dbUser.setLocation("Cork City");
                dbUser.setLatitude(51.9);
                dbUser.setLongitude(-8.47);
                
                DatabaseManager.getInstance().saveUser(dbUser);
                LOGGER.log(Level.INFO, "Created demo user in database");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error creating demo user in database", e);
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }
    
    /**
     * Simple class to handle user authentication
     * In a real application, this would connect to a database
     */
    private static class UserRepository {
        private final Map<String, User> users = new HashMap<>();
        private final Map<String, String> credentials = new HashMap<>();
        
        /**
         * Authenticate a user
         */
        public User authenticate(String username, String password) {
            String storedPassword = credentials.get(username);
            if (storedPassword != null && storedPassword.equals(password)) {
                return users.get(username);
            }
            return null;
        }
        
        /**
         * Add a new user
         */
        public void addUser(String username, String password, User user) {
            users.put(username, user);
            credentials.put(username, password);
        }
        
        /**
         * Check if a user exists
         */
        public boolean userExists(String username) {
            return users.containsKey(username);
        }
    }
}