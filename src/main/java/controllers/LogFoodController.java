package controllers;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import com.greencompost.service.LocalService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * Controller for the log food item view
 */
public class LogFoodController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField quantityField;
    @FXML private TextField unitField;
    @FXML private ComboBox<FoodItem.FoodCategory> categoryComboBox;
    @FXML private TextField daysField;
    @FXML private TextArea descriptionArea;
    
    @FXML private Button resetButton;
    @FXML private Button saveButton;
    
    @FXML private VBox successPane;
    @FXML private Label itemInfoLabel;
    @FXML private VBox recommendationPane;
    @FXML private Label serviceNameLabel;
    @FXML private Label serviceInfoLabel;
    @FXML private Button scheduleButton;
    
    private User currentUser;
    private FoodItem foodItemController;
    private FoodItem currentItem;
    private LocalService recommendedService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user and controller from MainController class
        currentUser = controllers.MainController.getCurrentUser();
        foodItemController = controllers.MainController.getFoodItemController();
        
        // Setup category options
        ObservableList<FoodItem.FoodCategory> categories = FXCollections.observableArrayList(
            FoodItem.FoodCategory.VEGETABLE,
            FoodItem.FoodCategory.FRUIT,
            FoodItem.FoodCategory.DAIRY,
            FoodItem.FoodCategory.GRAIN,
            FoodItem.FoodCategory.PROTEIN,
            FoodItem.FoodCategory.LEFTOVER_MEAL,
            FoodItem.FoodCategory.OTHER
        );
        categoryComboBox.setItems(categories);
        categoryComboBox.getSelectionModel().selectFirst();
        
        // Set default values
        unitField.setText("kg");
        daysField.setText("7");
        
        // Hide success pane initially
        successPane.setVisible(false);
    }
    
    /**
     * Handle saving a new food item
     */
    @FXML
    private void handleSave() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Please enter an item name");
                return;
            }
            
            double quantity;
            try {
                quantity = Double.parseDouble(quantityField.getText().trim());
                if (quantity <= 0) {
                    showAlert("Quantity must be greater than zero");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid quantity (e.g. 2.5)");
                return;
            }
            
            if (unitField.getText().trim().isEmpty()) {
                showAlert("Please enter a unit (e.g. kg, liters, pieces)");
                return;
            }
            
            int days;
            try {
                days = Integer.parseInt(daysField.getText().trim());
                if (days < 0) {
                    showAlert("Days until expiry must be zero or greater");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Please enter a valid number of days");
                return;
            }
            
            // Create expiry date
            LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
            
            // Get selected category
            FoodItem.FoodCategory category = categoryComboBox.getValue();
            
            // Create the item
            currentItem = foodItemController.addFoodItem(
                currentUser, 
                nameField.getText().trim(), 
                quantity, 
                unitField.getText().trim(), 
                expiryDate, 
                category
            );
            
            // Set description if entered
            if (!descriptionArea.getText().trim().isEmpty()) {
                currentItem.setDescription(descriptionArea.getText().trim());
            }
            
            // Show success message
            itemInfoLabel.setText(String.format(
                "%s - %.1f %s - Expires in %d days",
                currentItem.getName(),
                currentItem.getQuantity(),
                currentItem.getQuantityUnit(),
                days
            ));
            
            // Find recommended service
            LocalService.ServiceType recommendedType;
            if (expiryDate.isBefore(LocalDateTime.now().plusDays(1))) {
                // For items expiring very soon, composting might be better
                recommendedType = LocalService.ServiceType.COMPOSTING_FACILITY;
            } else {
                // For items with longer shelf life, donation might be better
                recommendedType = LocalService.ServiceType.FOOD_BANK;
            }
            
            recommendedService = LocalService.recommendBestService(currentUser, recommendedType);
            
            if (recommendedService != null) {
                serviceNameLabel.setText(recommendedService.getName());
                String operatingHoursStr = recommendedService.getOperatingHours().toString();
                String firstLineOfHours = operatingHoursStr.contains("\n") ? 
                    operatingHoursStr.split("\n")[0] : operatingHoursStr;
                
                serviceInfoLabel.setText(String.format(
                    "%s - %s",
                    recommendedService.getType().getDisplayName(),
                    "Operating hours: " + firstLineOfHours
                ));
                recommendationPane.setVisible(true);
            } else {
                recommendationPane.setVisible(false);
            }
            
            // Show success pane
            successPane.setVisible(true);
            
        } catch (Exception e) {
            showAlert("Error adding food item: " + e.getMessage());
        }
    }
    
    /**
     * Handle resetting the form
     */
    @FXML
    private void handleReset() {
        nameField.clear();
        quantityField.clear();
        unitField.setText("kg");
        categoryComboBox.getSelectionModel().selectFirst();
        daysField.setText("7");
        descriptionArea.clear();
        successPane.setVisible(false);
    }
    
    /**
     * Handle scheduling a pickup or drop-off
     */
    @FXML
    private void handleSchedule() {
        // This would typically open a dialog to schedule a pickup or drop-off
        // We'll just show an alert for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Schedule Pickup/Drop-off");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("This feature is not yet implemented. In the future, you'll be able to schedule pickups or drop-offs for your food items.");
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}