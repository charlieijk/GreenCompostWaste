package controllers;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import com.greencompost.controller.FoodItem.FoodCategory;
import com.greencompost.controller.FoodItem.ItemStatus;
import com.greencompost.service.LocalService;
import com.greencompost.service.LocalService.ServiceType;
import com.greencompost.service.OperatingHours;
import com.greencompost.service.ScheduledEvent;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the Donate View, which allows users to donate food items to food banks
 */
public class DonateController implements Initializable {
    // User location section
    @FXML private Label locationLabel;
    
    // Food bank selection
    @FXML private ComboBox<LocalService> foodBankComboBox;
    
    // Food bank details
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label contactLabel;
    @FXML private Label distanceLabel;
    @FXML private VBox hoursPane;
    @FXML private ListView<String> guidelinesList;
    @FXML private ListView<String> acceptedItemsList;
    
    // Food item selection
    @FXML private CheckBox showOnlyExpiringCheck;
    @FXML private TableView<FoodItem> foodItemsTable;
    @FXML private TableColumn<FoodItem, CheckBox> selectColumn;
    @FXML private TableColumn<FoodItem, String> nameColumn;
    @FXML private TableColumn<FoodItem, String> quantityColumn;
    @FXML private TableColumn<FoodItem, String> categoryColumn;
    @FXML private TableColumn<FoodItem, String> expiryColumn;
    @FXML private TableColumn<FoodItem, String> statusColumn;
    
    // Add new item fields
    @FXML private TextField nameField;
    @FXML private ComboBox<FoodCategory> categoryComboBox;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextArea notesField;
    
    // Pickup options
    @FXML private RadioButton selfDeliveryRadio;
    @FXML private RadioButton requestPickupRadio;
    @FXML private Label pickupAvailabilityLabel;
    @FXML private GridPane pickupDetailsPane;
    @FXML private TextField addressField;
    @FXML private DatePicker pickupDatePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private TextArea instructionsField;
    
    // Preview and confirmation
    @FXML private TitledPane previewPane;
    @FXML private Label previewFoodBankLabel;
    @FXML private ListView<String> previewItemsList;
    @FXML private Label previewDeliveryLabel;
    @FXML private Label previewPickupHeaderLabel;
    @FXML private GridPane previewPickupDetails;
    @FXML private Label previewAddressLabel;
    @FXML private Label previewDateTimeLabel;
    @FXML private Label previewInstructionsLabel;
    @FXML private Button confirmButton;
    
    // Success message
    @FXML private VBox successPane;
    @FXML private Label successDetailsLabel;
    
    // Data models
    private User currentUser;
    private ObservableList<LocalService> foodBanks;
    private ObservableList<FoodItem> userFoodItems;
    private List<FoodItem> selectedFoodItems;
    private List<FoodItem> newFoodItems;
    
    // Common units for food quantities
    private final String[] commonUnits = {"kg", "g", "lb", "oz", "L", "ml", "pcs", "servings", "cups"};
    
    // Time slots for pickups
    private final String[] timeSlots = {
        "9:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "11:00 AM - 12:00 PM",
        "12:00 PM - 1:00 PM", "1:00 PM - 2:00 PM", "2:00 PM - 3:00 PM",
        "3:00 PM - 4:00 PM", "4:00 PM - 5:00 PM"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        currentUser = MainController.getCurrentUser();
        
        // Initialize data structures
        foodBanks = FXCollections.observableArrayList();
        userFoodItems = FXCollections.observableArrayList();
        selectedFoodItems = new ArrayList<>();
        newFoodItems = new ArrayList<>();
        
        // Setup food bank selector
        setupFoodBankSelector();
        
        // Setup food items table
        setupFoodItemsTable();
        
        // Setup new food item form
        setupNewItemForm();
        
        // Setup pickup options
        setupPickupOptions();
        
        // Load initial data
        loadData();
        
        // Set user location
        updateUserLocation();
    }
    
    /**
     * Set up the food bank selector and details display
     */
    private void setupFoodBankSelector() {
        foodBankComboBox.setItems(foodBanks);
        
        // Set the display string for food banks in the combo box
        foodBankComboBox.setCellFactory(param -> new ListCell<LocalService>() {
            @Override
            protected void updateItem(LocalService item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + 
                            String.format("%.1f km", item.getCalculatedDistance()) + ")");
                }
            }
        });
        
        // Display format for the selection
        foodBankComboBox.setButtonCell(new ListCell<LocalService>() {
            @Override
            protected void updateItem(LocalService item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + 
                            String.format("%.1f km", item.getCalculatedDistance()) + ")");
                }
            }
        });
        
        // Handle selection changes
        foodBankComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateFoodBankDetails(newValue);
                } else {
                    clearFoodBankDetails();
                }
            }
        );
    }
    
    /**
     * Set up the food items table with selection checkboxes
     */
    private void setupFoodItemsTable() {
        // Setup columns
        selectColumn.setCellFactory(createCheckBoxCellFactory());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        quantityColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new SimpleStringProperty(
                String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        });
        
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory().toString()));
        
        expiryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedExpiryDate()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        // Add listener to the filter checkbox
        showOnlyExpiringCheck.selectedProperty().addListener(
            (observable, oldValue, newValue) -> filterFoodItems());
        
        // Set data source
        foodItemsTable.setItems(userFoodItems);
    }
    
    /**
     * Create a cell factory for checkboxes in the table
     */
    private Callback<TableColumn<FoodItem, CheckBox>, TableCell<FoodItem, CheckBox>> createCheckBoxCellFactory() {
        return param -> new TableCell<FoodItem, CheckBox>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setSelected(false);
                checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    FoodItem item = foodItemsTable.getItems().get(getIndex());
                    if (isSelected) {
                        if (!selectedFoodItems.contains(item)) {
                            selectedFoodItems.add(item);
                        }
                    } else {
                        selectedFoodItems.remove(item);
                    }
                });
            }
            
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Check if this item is already selected
                    FoodItem foodItem = foodItemsTable.getItems().get(getIndex());
                    checkBox.setSelected(selectedFoodItems.contains(foodItem));
                    setGraphic(checkBox);
                }
            }
        };
    }
    
    /**
     * Setup the form for adding new food items
     */
    private void setupNewItemForm() {
        // Setup category combo box
        categoryComboBox.getItems().addAll(FoodCategory.values());
        categoryComboBox.setValue(FoodCategory.OTHER);
        
        // Setup units combo box
        unitComboBox.getItems().addAll(commonUnits);
        unitComboBox.setValue("kg");
        
        // Setup date picker with default value (today)
        expiryDatePicker.setValue(LocalDate.now());
    }
    
    /**
     * Setup pickup options and radio button listeners
     */
    private void setupPickupOptions() {
        // Create toggle group programmatically if not done in FXML
        ToggleGroup deliveryOptions = new ToggleGroup();
        selfDeliveryRadio.setToggleGroup(deliveryOptions);
        requestPickupRadio.setToggleGroup(deliveryOptions);
        
        // Default to self delivery
        selfDeliveryRadio.setSelected(true);
        
        // Listen for changes in delivery method
        deliveryOptions.selectedToggleProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean isPickup = newValue == requestPickupRadio;
                pickupDetailsPane.setVisible(isPickup);
                
                // Show warning if pickup isn't available for selected food bank
                if (isPickup) {
                    LocalService selectedFoodBank = foodBankComboBox.getValue();
                    pickupAvailabilityLabel.setVisible(
                        selectedFoodBank == null || !selectedFoodBank.isPickupAvailable());
                } else {
                    pickupAvailabilityLabel.setVisible(false);
                }
            }
        );
        
        // Set up time slot combo box
        timeSlotComboBox.getItems().addAll(timeSlots);
        if (!timeSlotComboBox.getItems().isEmpty()) {
            timeSlotComboBox.setValue(timeSlotComboBox.getItems().get(0));
        }
        
        // Set default date for pickup
        pickupDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Set default address
        if (currentUser != null && currentUser.getLocation() != null) {
            addressField.setText(currentUser.getLocation());
        }
    }
    
    /**
     * Load initial data - food banks and user's food items
     */
    private void loadData() {
        // Load food banks
        List<LocalService> donationServices = LocalService.findFoodDonationServices();
        
        // Calculate distances for each service
        if (currentUser != null) {
            for (LocalService service : donationServices) {
                double distance = calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    service.getLatitude(), service.getLongitude());
                service.setCalculatedDistance(distance);
            }
            
            // Sort by distance
            donationServices.sort((s1, s2) -> 
                Double.compare(s1.getCalculatedDistance(), s2.getCalculatedDistance()));
        }
        
        foodBanks.setAll(donationServices);
        
        // Select the nearest food bank if available
        if (!foodBanks.isEmpty()) {
            foodBankComboBox.getSelectionModel().selectFirst();
        }
        
        // Load user's food items
        filterFoodItems();
    }
    
    /**
     * Filter food items based on user settings
     */
    private void filterFoodItems() {
        if (currentUser == null) return;
        
        List<FoodItem> items = currentUser.getFoodItems();
        
        // Apply filter for non-expired items if selected
        if (showOnlyExpiringCheck.isSelected()) {
            items = items.stream()
                .filter(item -> !item.isExpired())
                .collect(Collectors.toList());
        }
        
        // Only show items that are available for donation
        items = items.stream()
            .filter(item -> item.getStatus() == ItemStatus.AVAILABLE)
            .collect(Collectors.toList());
        
        userFoodItems.setAll(items);
    }
    
    /**
     * Update user location display
     */
    private void updateUserLocation() {
        if (currentUser != null && currentUser.getLocation() != null) {
            locationLabel.setText(currentUser.getLocation());
        } else {
            locationLabel.setText("Not specified");
        }
    }
    
    /**
     * Update food bank details when a new one is selected
     */
    private void updateFoodBankDetails(LocalService foodBank) {
        if (foodBank == null) {
            clearFoodBankDetails();
            return;
        }
        
        // Basic details
        nameLabel.setText(foodBank.getName());
        addressLabel.setText(foodBank.getAddress());
        contactLabel.setText(foodBank.getContactInfo());
        distanceLabel.setText(String.format("%.1f km", foodBank.getCalculatedDistance()));
        
        // Operating hours
        hoursPane.getChildren().clear();
        OperatingHours hours = foodBank.getHours();
        if (hours != null) {
            for (DayOfWeek day : hours.getOpenDays()) {
                OperatingHours.TimeSlot slot = hours.getHours(day);
                if (slot != null) {
                    Label hourLabel = new Label(day.toString() + ": " + slot.toString());
                    hoursPane.getChildren().add(hourLabel);
                }
            }
        }
        
        // Donation guidelines
        guidelinesList.getItems().clear();
        guidelinesList.getItems().addAll(foodBank.getDonationGuidelines());
        
        // Accepted items
        acceptedItemsList.getItems().clear();
        acceptedItemsList.getItems().addAll(foodBank.getAcceptedItems());
        
        // Update pickup availability warning if needed
        if (requestPickupRadio.isSelected()) {
            pickupAvailabilityLabel.setVisible(!foodBank.isPickupAvailable());
        }
    }
    
    /**
     * Clear food bank details when no food bank is selected
     */
    private void clearFoodBankDetails() {
        nameLabel.setText("");
        addressLabel.setText("");
        contactLabel.setText("");
        distanceLabel.setText("");
        hoursPane.getChildren().clear();
        guidelinesList.getItems().clear();
        acceptedItemsList.getItems().clear();
    }
    
    /**
     * Handle use current location button
     */
    @FXML
    private void handleUseCurrentLocation() {
        updateUserLocation();
        
        // Refresh food bank distances
        if (currentUser != null) {
            for (LocalService service : foodBanks) {
                double distance = calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    service.getLatitude(), service.getLongitude());
                service.setCalculatedDistance(distance);
            }
            
            // Sort by distance
            foodBanks.sort((s1, s2) -> 
                Double.compare(s1.getCalculatedDistance(), s2.getCalculatedDistance()));
            
            // Refresh the combo box
            LocalService selected = foodBankComboBox.getValue();
            foodBankComboBox.setItems(FXCollections.observableArrayList(foodBanks));
            
            // Re-select the previously selected item, or the first item if none was selected
            if (selected != null) {
                foodBankComboBox.setValue(selected);
            } else if (!foodBanks.isEmpty()) {
                foodBankComboBox.getSelectionModel().selectFirst();
            }
        }
    }
    
    /**
     * Handle find nearest food bank button
     */
    @FXML
    private void handleFindNearest() {
        if (!foodBanks.isEmpty()) {
            foodBankComboBox.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Handle get directions button
     */
    @FXML
    private void handleGetDirections() {
        LocalService foodBank = foodBankComboBox.getValue();
        if (foodBank == null) {
            showAlert("No Food Bank Selected", "Please select a food bank first.", Alert.AlertType.WARNING);
            return;
        }
        
        // In a real app, this would open a maps application or show directions
        // For now, just show a dialog with the address
        showAlert("Directions to " + foodBank.getName(), 
                 "Address: " + foodBank.getAddress() + "\n" +
                 "Distance: " + String.format("%.1f km", foodBank.getCalculatedDistance()),
                 Alert.AlertType.INFORMATION);
    }
    
    /**
     * Handle contact food bank button
     */
    @FXML
    private void handleContactFoodBank() {
        LocalService foodBank = foodBankComboBox.getValue();
        if (foodBank == null) {
            showAlert("No Food Bank Selected", "Please select a food bank first.", Alert.AlertType.WARNING);
            return;
        }
        
        // In a real app, this would open email or phone
        // For now, just show a dialog with the contact info
        showAlert("Contact " + foodBank.getName(), 
                 "Contact: " + foodBank.getContactInfo(),
                 Alert.AlertType.INFORMATION);
    }
    
    /**
     * Handle add new food item button
     */
    @FXML
    private void handleAddNewItem() {
        try {
            // Validate fields
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Missing Information", "Please enter a name for the food item.", Alert.AlertType.WARNING);
                return;
            }
            
            double quantity;
            try {
                quantity = Double.parseDouble(quantityField.getText().trim());
                if (quantity <= 0) {
                    throw new NumberFormatException("Quantity must be positive");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Quantity", "Please enter a valid positive number for quantity.", Alert.AlertType.WARNING);
                return;
            }
            
            String unit = unitComboBox.getValue();
            FoodCategory category = categoryComboBox.getValue();
            LocalDate expiryDate = expiryDatePicker.getValue();
            
            if (expiryDate == null) {
                showAlert("Missing Information", "Please select an expiry date.", Alert.AlertType.WARNING);
                return;
            }
            
            // Create a new food item
            LocalDateTime expiryDateTime = expiryDate.atTime(23, 59);
            FoodItem item = new FoodItem(name, quantity, unit, expiryDateTime, category);
            
            // Set the owner
            item.setOwner(currentUser);
            
            // Add description if provided
            if (notesField.getText() != null && !notesField.getText().trim().isEmpty()) {
                item.setDescription(notesField.getText().trim());
            }
            
            // Add to selected items
            selectedFoodItems.add(item);
            
            // Keep track of new items
            newFoodItems.add(item);
            
            // Clear the form
            nameField.clear();
            quantityField.clear();
            notesField.clear();
            
            // Show confirmation
            showAlert("Item Added", "The new food item has been added to your donation.", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            showAlert("Error", "Failed to add new food item: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle preview donation button
     */
    @FXML
    private void handlePreviewDonation() {
        LocalService foodBank = foodBankComboBox.getValue();
        
        // Validate food bank selection
        if (foodBank == null) {
            showAlert("Missing Information", "Please select a food bank.", Alert.AlertType.WARNING);
            return;
        }
        
        // Validate item selection
        if (selectedFoodItems.isEmpty()) {
            showAlert("Missing Information", "Please select at least one food item to donate.", Alert.AlertType.WARNING);
            return;
        }
        
        // Validate pickup details if requesting pickup
        if (requestPickupRadio.isSelected()) {
            if (!foodBank.isPickupAvailable()) {
                showAlert("Pickup Not Available", 
                         "The selected food bank does not offer pickup service. Please choose self-delivery or select a different food bank.",
                         Alert.AlertType.WARNING);
                return;
            }
            
            if (addressField.getText().trim().isEmpty()) {
                showAlert("Missing Information", "Please enter your pickup address.", Alert.AlertType.WARNING);
                return;
            }
            
            if (pickupDatePicker.getValue() == null) {
                showAlert("Missing Information", "Please select a pickup date.", Alert.AlertType.WARNING);
                return;
            }
            
            if (timeSlotComboBox.getValue() == null) {
                showAlert("Missing Information", "Please select a pickup time slot.", Alert.AlertType.WARNING);
                return;
            }
        }
        
        // Update preview UI
        updatePreview(foodBank);
        
        // Show preview pane
        previewPane.setVisible(true);
        
        // Enable confirm button
        confirmButton.setDisable(false);
    }
    
    /**
     * Update the preview pane with donation details
     */
    private void updatePreview(LocalService foodBank) {
        // Food bank details
        previewFoodBankLabel.setText(foodBank.getName() + " - " + foodBank.getAddress());
        
        // Item list
        previewItemsList.getItems().clear();
        for (FoodItem item : selectedFoodItems) {
            previewItemsList.getItems().add(
                item.getName() + " - " + 
                String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        }
        
        // Delivery method
        if (selfDeliveryRadio.isSelected()) {
            previewDeliveryLabel.setText("Self-delivery to food bank");
            previewPickupHeaderLabel.setVisible(false);
            previewPickupDetails.setVisible(false);
        } else {
            previewDeliveryLabel.setText("Pickup requested");
            previewPickupHeaderLabel.setVisible(true);
            previewPickupDetails.setVisible(true);
            
            // Pickup details
            previewAddressLabel.setText(addressField.getText());
            previewDateTimeLabel.setText(
                pickupDatePicker.getValue().toString() + ", " + timeSlotComboBox.getValue());
            previewInstructionsLabel.setText(
                instructionsField.getText() != null ? instructionsField.getText() : "");
        }
    }
    
    /**
     * Handle confirm donation button
     */
    @FXML
    private void handleConfirmDonation() {
        LocalService foodBank = foodBankComboBox.getValue();
        
        try {
            // Process each selected food item
            for (FoodItem item : selectedFoodItems) {
                // Add new items to user's collection if they're not already there
                if (newFoodItems.contains(item)) {
                    currentUser.addFoodItem(item);
                    FoodItem.allFoodItems.add(item);
                }
                
                // Update status
                item.setStatus(ItemStatus.SCHEDULED_FOR_PICKUP);
            }
            
            // Create a scheduled event for the donation if pickup requested
            if (requestPickupRadio.isSelected()) {
                LocalDate pickupDate = pickupDatePicker.getValue();
                String timeSlot = timeSlotComboBox.getValue();
                
                // Parse time slot to get start time
                String startTimeStr = timeSlot.split(" - ")[0];
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
                
                // Create the event
                ScheduledEvent event = new ScheduledEvent(
                    "Food Donation Pickup", 
                    "Pickup from " + addressField.getText(),
                    pickupDate.atTime(startTime),
                    foodBank,
                    currentUser
                );
                
                // Add donation details to event notes
                StringBuilder notes = new StringBuilder("Items to pick up:\n");
                for (FoodItem item : selectedFoodItems) {
                    notes.append("- ").append(item.getName()).append(" (")
                         .append(String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()))
                         .append(")\n");
                }
                
                if (instructionsField.getText() != null && !instructionsField.getText().trim().isEmpty()) {
                    notes.append("\nSpecial instructions:\n").append(instructionsField.getText());
                }
                
                event.setNotes(notes.toString());
            }
            
            // Show success message
            showSuccessMessage(foodBank);
            
        } catch (Exception e) {
            showAlert("Error", "Failed to process donation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Show success message after donation is confirmed
     */
    private void showSuccessMessage(LocalService foodBank) {
        // Update success message
        String method = selfDeliveryRadio.isSelected() ? "self-delivery" : "pickup";
        
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Your donation to ").append(foodBank.getName()).append(" has been scheduled for ");
        
        if (selfDeliveryRadio.isSelected()) {
            detailsBuilder.append("self-delivery.");
        } else {
            detailsBuilder.append("pickup on ")
                         .append(pickupDatePicker.getValue().toString())
                         .append(" during ")
                         .append(timeSlotComboBox.getValue())
                         .append(".");
        }
        
        successDetailsLabel.setText(detailsBuilder.toString());
        
        // Hide other UI elements
        previewPane.setVisible(false);
        
        // Show success message
        successPane.setVisible(true);
    }
    
    /**
     * Handle return to dashboard button from success screen
     */
    @FXML
    private void handleReturnToDashboard() {
        // Get main controller reference and switch to dashboard
        MainController mainController = null;
        try {
            // Try to get the current scene's controller
            javafx.scene.Scene scene = successPane.getScene();
            if (scene != null) {
                javafx.scene.Parent root = scene.getRoot();
                if (root != null && root.getUserData() instanceof MainController) {
                    mainController = (MainController) root.getUserData();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting main controller: " + e.getMessage());
        }
        
        if (mainController != null) {
            // Call the main controller's method to switch to dashboard
            try {
                java.lang.reflect.Method switchMethod = 
                    mainController.getClass().getDeclaredMethod("switchToDashboard");
                switchMethod.setAccessible(true);
                switchMethod.invoke(mainController);
            } catch (Exception e) {
                System.err.println("Error switching to dashboard: " + e.getMessage());
            }
        } else {
            // Fallback: Just hide the success message
            successPane.setVisible(false);
        }
    }
    
    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        // Confirm cancellation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Donation");
        confirmation.setHeaderText("Cancel Donation Process");
        confirmation.setContentText("Are you sure you want to cancel this donation? All entered information will be lost.");
        
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Reset to initial state
            selectedFoodItems.clear();
            newFoodItems.clear();
            resetForms();
            
            // Hide preview and success panes
            previewPane.setVisible(false);
            successPane.setVisible(false);
            
            // Disable confirm button
            confirmButton.setDisable(true);
        }
    }
    
    /**
     * Reset all input forms to default values
     */
    private void resetForms() {
        // Reset food item selection
        filterFoodItems();
        
        // Reset new item form
        nameField.clear();
        quantityField.clear();
        categoryComboBox.setValue(FoodCategory.OTHER);
        unitComboBox.setValue("kg");
        expiryDatePicker.setValue(LocalDate.now());
        notesField.clear();
        
        // Reset pickup options
        selfDeliveryRadio.setSelected(true);
        addressField.setText(currentUser != null && currentUser.getLocation() != null ? 
                           currentUser.getLocation() : "");
        pickupDatePicker.setValue(LocalDate.now().plusDays(1));
        if (!timeSlotComboBox.getItems().isEmpty()) {
            timeSlotComboBox.setValue(timeSlotComboBox.getItems().get(0));
        }
        instructionsField.clear();
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}