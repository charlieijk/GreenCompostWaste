package controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.greencompost.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

/**
 * Controller for the register view
 */
public class RegisterController implements Initializable {

    @FXML private VBox registerPane;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regPasswordTextField;
    @FXML private Button regTogglePasswordButton;
    @FXML private PasswordField regConfirmPasswordField;
    @FXML private TextField regConfirmPasswordTextField;
    @FXML private Button regToggleConfirmPasswordButton;
    @FXML private TextField regEmailField;
    @FXML private ComboBox<String> regCityComboBox;
    @FXML private TextField regNeighborhoodField;
    @FXML private TextField regLatitudeField;
    @FXML private TextField regLongitudeField;
    @FXML private Button createAccountButton;
    @FXML private Button backToLoginButton;
    @FXML private Label statusLabel;
    
    // City information
    private final List<String> cities = Arrays.asList(
        "Cork", "Dublin", "Galway", "Limerick", "Belfast", "San Francisco",
        "London", "Paris", "Berlin", "New York"
    );
    
    // Map of city coordinates (latitude, longitude)
    private final Map<String, double[]> cityCoordinates = new HashMap<>();

    // Mock user repository for demo purposes
    // In a real application, this would connect to a database
    private UserRepository userRepository;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user repository
        userRepository = new UserRepository();
        
        // Configure password fields and their text field equivalents
        setupPasswordFields();
        
        // Initialize city coordinates
        initializeCityCoordinates();
        
        // Populate city dropdown
        ObservableList<String> cityOptions = FXCollections.observableArrayList(cities);
        regCityComboBox.setItems(cityOptions);
        
        // Add listener to update coordinates when city is selected
        regCityComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCoordinatesForCity(newVal);
            }
        });
    }
    
    /**
     * Initialize city coordinates map
     */
    private void initializeCityCoordinates() {
        // Add coordinates for each city (latitude, longitude)
        cityCoordinates.put("Cork", new double[]{51.89, -8.47});
        cityCoordinates.put("Dublin", new double[]{53.35, -6.26});
        cityCoordinates.put("Galway", new double[]{53.27, -9.05});
        cityCoordinates.put("Limerick", new double[]{52.66, -8.62});
        cityCoordinates.put("Belfast", new double[]{54.60, -5.93});
        cityCoordinates.put("San Francisco", new double[]{37.77, -122.42});
        cityCoordinates.put("London", new double[]{51.51, -0.13});
        cityCoordinates.put("Paris", new double[]{48.86, 2.35});
        cityCoordinates.put("Berlin", new double[]{52.52, 13.40});
        cityCoordinates.put("New York", new double[]{40.71, -74.01});
    }
    
    /**
     * Update coordinate fields based on selected city
     */
    private void updateCoordinatesForCity(String city) {
        if (cityCoordinates.containsKey(city)) {
            double[] coords = cityCoordinates.get(city);
            regLatitudeField.setText(String.valueOf(coords[0]));
            regLongitudeField.setText(String.valueOf(coords[1]));
        }
    }
    
    /**
     * Set up synchronization between password fields and their text field equivalents
     */
    private void setupPasswordFields() {
        // Set up registration password field synchronization
        regPasswordField.textProperty().addListener((unused1, unused2, newValue) -> {
            regPasswordTextField.setText(newValue);
        });
        
        regPasswordTextField.textProperty().addListener((unused1, unused2, newValue) -> {
            regPasswordField.setText(newValue);
        });
        
        // Set up registration confirm password field synchronization
        regConfirmPasswordField.textProperty().addListener((unused1, unused2, newValue) -> {
            regConfirmPasswordTextField.setText(newValue);
        });
        
        regConfirmPasswordTextField.textProperty().addListener((unused1, unused2, newValue) -> {
            regConfirmPasswordField.setText(newValue);
        });
    }
    
    /**
     * Toggle visibility of the registration password
     */
    @FXML
    private void toggleRegPasswordVisibility() {
        boolean showPassword = !regPasswordTextField.isVisible();
        
        // Toggle visibility
        regPasswordField.setVisible(!showPassword);
        regPasswordTextField.setVisible(showPassword);
        
        // Update which field has focus and set button text
        if (showPassword) {
            regPasswordTextField.requestFocus();
            regTogglePasswordButton.setText("🔒");
        } else {
            regPasswordField.requestFocus();
            regTogglePasswordButton.setText("👁");
        }
    }
    
    /**
     * Toggle visibility of the registration confirm password
     */
    @FXML
    private void toggleRegConfirmPasswordVisibility() {
        boolean showPassword = !regConfirmPasswordTextField.isVisible();
        
        // Toggle visibility
        regConfirmPasswordField.setVisible(!showPassword);
        regConfirmPasswordTextField.setVisible(showPassword);
        
        // Update which field has focus and set button text
        if (showPassword) {
            regConfirmPasswordTextField.requestFocus();
            regToggleConfirmPasswordButton.setText("🔒");
        } else {
            regConfirmPasswordField.requestFocus();
            regToggleConfirmPasswordButton.setText("👁");
        }
    }
    
    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister() {
        // Get registration input
        String username = regUsernameField.getText().trim();
        // Get password from whichever field is visible
        String password = regPasswordField.isVisible() ? 
                         regPasswordField.getText() : 
                         regPasswordTextField.getText();
        // Get confirm password from whichever field is visible
        String confirmPassword = regConfirmPasswordField.isVisible() ? 
                               regConfirmPasswordField.getText() : 
                               regConfirmPasswordTextField.getText();
        String email = regEmailField.getText().trim();
        String selectedCity = regCityComboBox.getValue();
        String neighborhood = regNeighborhoodField.getText().trim();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || selectedCity == null) {
            showStatus("Please fill in all required fields and select a city.", true);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match.", true);
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            showStatus("Please enter a valid email address.", true);
            return;
        }
        
        try {
            // Parse latitude and longitude
            double latitude = Double.parseDouble(regLatitudeField.getText().trim());
            double longitude = Double.parseDouble(regLongitudeField.getText().trim());
            
            // Validate coordinates
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                showStatus("Invalid coordinates. Latitude must be between -90 and 90, longitude between -180 and 180.", true);
                return;
            }
            
            // Check if username already exists
            if (userRepository.userExists(username)) {
                showStatus("Username already exists. Please choose another.", true);
                return;
            }
            
            // Create location string with city and optional neighborhood
            String location = neighborhood.isEmpty() ? 
                            selectedCity : 
                            neighborhood + ", " + selectedCity;
            
            // Create new user with city-based constructor
            User newUser = new User(username, password, email, location, selectedCity);
            
            // For backward compatibility, also set the coordinates
            newUser.setLatitude(latitude);
            newUser.setLongitude(longitude);
            
            userRepository.addUser(username, password, newUser);
            
            // Registration successful
            showStatus("Account created successfully! You can now log in.", false);
            
            // Switch back to login form
            navigateToLogin();
            
        } catch (NumberFormatException e) {
            showStatus("Please enter valid numbers for latitude and longitude.", true);
        } catch (Exception e) {
            showStatus("Error creating account: " + e.getMessage(), true);
        }
    }
    
    /**
     * Switch back to login form
     */
    @FXML
    private void navigateToLogin() {
        try {
            URL loginViewURL = getClass().getResource("/views/LoginView.fxml");
            if (loginViewURL == null) {
                loginViewURL = getClass().getResource("../views/LoginView.fxml");
            }
            
            Parent root = FXMLLoader.load(loginViewURL);
            Scene scene = new Scene(root);
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("GreenCompost Connect - Login");
            stage.show();
        } catch (IOException e) {
            showStatus("Error navigating to login: " + e.getMessage(), true);
            e.printStackTrace();
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
     * Simple class to handle user authentication
     * In a real application, this would connect to a database
     */
    private static class UserRepository {
        private final Map<String, User> users = new HashMap<>();
        private final Map<String, String> credentials = new HashMap<>();
        
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