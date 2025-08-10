package controllers;

import com.greencompost.User;
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
import com.greencompost.model.DatabaseManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the reset password view
 */
public class ResetPasswordController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordController.class.getName());

    @FXML private VBox resetPasswordPane;
    @FXML private TextField resetUsernameField;
    @FXML private PasswordField resetNewPasswordField;
    @FXML private TextField resetNewPasswordTextField;
    @FXML private Button resetTogglePasswordButton;
    @FXML private PasswordField resetConfirmPasswordField;
    @FXML private TextField resetConfirmPasswordTextField;
    @FXML private Button resetToggleConfirmPasswordButton;
    @FXML private Button resetPasswordSubmitButton;
    @FXML private Button backToLoginFromResetButton;
    @FXML private Label statusLabel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure password fields and their text field equivalents
        setupPasswordFields();
    }
    
    /**
     * Set up synchronization between password fields and their text field equivalents
     */
    @SuppressWarnings("unused")
    private void setupPasswordFields() {
        // Set up reset password field synchronization
        resetNewPasswordField.textProperty().addListener((_1, _2, newValue) -> {
            resetNewPasswordTextField.setText(newValue);
        });
        
        resetNewPasswordTextField.textProperty().addListener((_1, _2, newValue) -> {
            resetNewPasswordField.setText(newValue);
        });
        
        // Set up reset confirm password field synchronization
        resetConfirmPasswordField.textProperty().addListener((_1, _2, newValue) -> {
            resetConfirmPasswordTextField.setText(newValue);
        });
        
        resetConfirmPasswordTextField.textProperty().addListener((_1, _2, newValue) -> {
            resetConfirmPasswordField.setText(newValue);
        });
    }
    
    /**
     * Toggle visibility of the reset password field
     */
    @FXML
    private void toggleResetPasswordVisibility() {
        boolean showPassword = !resetNewPasswordTextField.isVisible();
        
        // Toggle visibility
        resetNewPasswordField.setVisible(!showPassword);
        resetNewPasswordTextField.setVisible(showPassword);
        
        // Update which field has focus and set button text
        if (showPassword) {
            resetNewPasswordTextField.requestFocus();
            resetTogglePasswordButton.setText("🔒");
        } else {
            resetNewPasswordField.requestFocus();
            resetTogglePasswordButton.setText("👁");
        }
    }
    
    /**
     * Toggle visibility of the reset confirm password field
     */
    @FXML
    private void toggleResetConfirmPasswordVisibility() {
        boolean showPassword = !resetConfirmPasswordTextField.isVisible();
        
        // Toggle visibility
        resetConfirmPasswordField.setVisible(!showPassword);
        resetConfirmPasswordTextField.setVisible(showPassword);
        
        // Update which field has focus and set button text
        if (showPassword) {
            resetConfirmPasswordTextField.requestFocus();
            resetToggleConfirmPasswordButton.setText("🔒");
        } else {
            resetConfirmPasswordField.requestFocus();
            resetToggleConfirmPasswordButton.setText("👁");
        }
    }
    
    /**
     * Handle reset password button click
     */
    @FXML
    private void handleResetPassword() {
        try {
            String username = resetUsernameField.getText().trim();
            LOGGER.log(Level.INFO, "Reset password attempt for username: {0}", username);
            
            // Get password from whichever field is visible
            String newPassword = resetNewPasswordField.isVisible() ? 
                                resetNewPasswordField.getText() : 
                                resetNewPasswordTextField.getText();
            // Get confirm password from whichever field is visible
            String confirmPassword = resetConfirmPasswordField.isVisible() ? 
                                   resetConfirmPasswordField.getText() : 
                                   resetConfirmPasswordTextField.getText();
            
            // Validate input
            if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showStatus("Please fill in all fields", true);
                return;
            }
            
            // Verify passwords match
            if (!newPassword.equals(confirmPassword)) {
                showStatus("Passwords do not match", true);
                return;
            }
            
            // Verify new password meets minimum requirements
            if (newPassword.length() < 6) {
                showStatus("Password must be at least 6 characters long", true);
                return;
            }
            
            // Log the attempt
            LOGGER.log(Level.INFO, "Attempting to reset password for username: {0}", username); 
            
            // Try direct SQL query to bypass any potential issues in the higher-level API
            java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
            
            // Check if username exists first
            String checkSql = "SELECT username FROM users WHERE username = ?";
            try (java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                java.sql.ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    LOGGER.log(Level.INFO, "Found user with username: {0}", username);
                    
                    // Update password directly with SQL
                    String updateSql = "UPDATE users SET password = ? WHERE username = ?";
                    try (java.sql.PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, newPassword);
                        updateStmt.setString(2, username);
                        int rowsAffected = updateStmt.executeUpdate();
                        
                        if (rowsAffected > 0) {
                            LOGGER.log(Level.INFO, "Password reset successful for user: {0}", username);
                            showStatus("Password reset successful! You can now log in with your new password.", false);
                            
                            // Clear fields
                            resetUsernameField.setText("");
                            resetNewPasswordField.setText("");
                            resetNewPasswordTextField.setText("");
                            resetConfirmPasswordField.setText("");
                            resetConfirmPasswordTextField.setText("");
                            
                            // Wait a moment before navigating to login so user can see the success message
                            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                            pause.setOnFinished(event -> navigateToLogin());
                            pause.play();
                        } else {
                            LOGGER.log(Level.WARNING, "No rows affected when updating password");
                            showStatus("Error updating password in database", true);
                        }
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No user found with username: {0}", username);
                    showStatus("No account found with that username", true);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting password", e);
            showStatus("Error resetting password: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate back to login view
     */
    @FXML
    private void navigateToLogin() {
        try {
            URL loginViewURL = getClass().getResource("/views/LoginView.fxml");
            if (loginViewURL == null) {
                // Try alternate locations
                loginViewURL = getClass().getResource("../views/LoginView.fxml");
                if (loginViewURL == null) {
                    loginViewURL = getClass().getResource("/main/resources/views/LoginView.fxml");
                    if (loginViewURL == null) {
                        throw new IOException("Could not find LoginView.fxml resource");
                    }
                }
            }
            
            Parent root = FXMLLoader.load(loginViewURL);
            Scene scene = new Scene(root);
            Stage stage = (Stage) backToLoginFromResetButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("GreenCompost Connect - Login");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to login", e);
            showStatus("Error navigating to login: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
    
    /**
     * Validate username
     */
    private boolean isValidUsername(String username) {
        // Debug info
        LOGGER.log(Level.INFO, "Validating username: {0}", username);
        
        // Just check if it's not empty - we'll validate for real by looking up in the database
        return username != null && !username.trim().isEmpty();
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
}