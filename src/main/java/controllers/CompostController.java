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
 * Controller for the Compost View, which allows users to compost food waste
 */
public class CompostController implements Initializable {
    // User location section
    @FXML private Label locationLabel;
    
    // Composting facility selection
    @FXML private ComboBox<LocalService> facilityComboBox;
    
    // Facility details
    @FXML private Label nameLabel;
    @FXML private Label addressLabel;
    @FXML private Label contactLabel;
    @FXML private Label distanceLabel;
    @FXML private VBox hoursPane;
    @FXML private ListView<String> guidelinesList;
    @FXML private ListView<String> acceptedItemsList;
    @FXML private ListView<String> notAcceptedItemsList;
    
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
    
    // Composting options
    @FXML private RadioButton selfDeliveryRadio;
    @FXML private RadioButton requestPickupRadio;
    @FXML private Label pickupAvailabilityLabel;
    @FXML private GridPane pickupDetailsPane;
    @FXML private CheckBox homeCompostingCheck;
    @FXML private ListView<String> homeTipsList;
    @FXML private TextField addressField;
    @FXML private DatePicker pickupDatePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private TextArea instructionsField;
    
    // Preview and confirmation
    @FXML private TitledPane previewPane;
    @FXML private Label previewMethodLabel;
    @FXML private Label previewDestinationLabel;
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
    private ObservableList<LocalService> compostingFacilities;
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
    
    // Home composting tips
    private final String[] homeCompostingTips = {
        "Layer 'green' (food waste) with 'brown' materials (leaves, paper) in a 1:3 ratio",
        "Keep compost moist but not wet - like a wrung-out sponge",
        "Turn your compost pile weekly to aerate it and speed up decomposition",
        "Chop items into smaller pieces to help them break down faster",
        "Avoid putting meat, dairy, and oils in home compost systems",
        "Use a covered bin to protect from pests and contain odors",
        "Add a handful of garden soil to introduce beneficial microorganisms",
        "Your compost is ready when it's dark, crumbly, and earthy-smelling"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        currentUser = MainController.getCurrentUser();
        
        // Initialize data structures
        compostingFacilities = FXCollections.observableArrayList();
        userFoodItems = FXCollections.observableArrayList();
        selectedFoodItems = new ArrayList<>();
        newFoodItems = new ArrayList<>();
        
        // Setup composting facility selector
        setupFacilitySelector();
        
        // Setup food items table
        setupFoodItemsTable();
        
        // Setup new food item form
        setupNewItemForm();
        
        // Setup composting options
        setupCompostingOptions();
        
        // Load initial data
        loadData();
        
        // Set user location
        updateUserLocation();
    }
    
    /**
     * Set up the composting facility selector and details display
     */
    private void setupFacilitySelector() {
        facilityComboBox.setItems(compostingFacilities);
        
        // Set the display string for facilities in the combo box
        facilityComboBox.setCellFactory(param -> new ListCell<LocalService>() {
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
        facilityComboBox.setButtonCell(new ListCell<LocalService>() {
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
        facilityComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateFacilityDetails(newValue);
                } else {
                    clearFacilityDetails();
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
                    if (getIndex() < foodItemsTable.getItems().size()) {
                        FoodItem item = foodItemsTable.getItems().get(getIndex());
                        if (isSelected) {
                            if (!selectedFoodItems.contains(item)) {
                                selectedFoodItems.add(item);
                            }
                        } else {
                            selectedFoodItems.remove(item);
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= foodItemsTable.getItems().size()) {
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
     * Setup composting options and radio button listeners
     */
    private void setupCompostingOptions() {
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
                
                // Show warning if pickup isn't available for selected facility
                if (isPickup) {
                    LocalService selectedFacility = facilityComboBox.getValue();
                    pickupAvailabilityLabel.setVisible(
                        selectedFacility == null || !selectedFacility.isPickupAvailable());
                } else {
                    pickupAvailabilityLabel.setVisible(false);
                }
            }
        );
        
        // Set up home composting tips
        homeTipsList.getItems().addAll(homeCompostingTips);
        
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
        
        // Listen for home composting checkbox changes
        homeCompostingCheck.selectedProperty().addListener(
            (observable, oldValue, newValue) -> {
                // If home composting is selected, disable the delivery options
                boolean isHomeComposting = newValue;
                selfDeliveryRadio.setDisable(isHomeComposting);
                requestPickupRadio.setDisable(isHomeComposting);
                
                // Hide pickup details if home composting is selected
                if (isHomeComposting) {
                    pickupDetailsPane.setVisible(false);
                    pickupAvailabilityLabel.setVisible(false);
                } else {
                    // Restore pickup details visibility based on radio button selection
                    pickupDetailsPane.setVisible(requestPickupRadio.isSelected());
                    
                    // Restore pickup availability warning if needed
                    if (requestPickupRadio.isSelected()) {
                        LocalService selectedFacility = facilityComboBox.getValue();
                        pickupAvailabilityLabel.setVisible(
                            selectedFacility == null || !selectedFacility.isPickupAvailable());
                    }
                }
            }
        );
    }
    
    /**
     * Load initial data - composting facilities and user's food items
     */
    private void loadData() {
        // Load composting facilities
        List<LocalService> facilities = LocalService.getAllServices().stream()
            .filter(service -> service.getType() == ServiceType.COMPOSTING_FACILITY || 
                               service.getType() == ServiceType.COMMUNITY_GARDEN ||
                               service.getType() == ServiceType.URBAN_FARM)
            .collect(Collectors.toList());
        
        // Calculate distances for each facility
        if (currentUser != null) {
            for (LocalService facility : facilities) {
                double distance = calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    facility.getLatitude(), facility.getLongitude());
                facility.setCalculatedDistance(distance);
            }
            
            // Sort by distance
            facilities.sort((s1, s2) -> 
                Double.compare(s1.getCalculatedDistance(), s2.getCalculatedDistance()));
        }
        
        compostingFacilities.setAll(facilities);
        
        // Select the nearest facility if available
        if (!compostingFacilities.isEmpty()) {
            facilityComboBox.getSelectionModel().selectFirst();
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
        
        // Apply filter for expired/expiring items if selected
        if (showOnlyExpiringCheck.isSelected()) {
            items = items.stream()
                .filter(item -> item.isExpired() || item.isExpiringSoon())
                .collect(Collectors.toList());
        }
        
        // Only show items that are available (not already donated or composted)
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
     * Update facility details when a new one is selected
     */
    private void updateFacilityDetails(LocalService facility) {
        if (facility == null) {
            clearFacilityDetails();
            return;
        }
        
        // Basic details
        nameLabel.setText(facility.getName());
        addressLabel.setText(facility.getAddress());
        contactLabel.setText(facility.getContactInfo());
        distanceLabel.setText(String.format("%.1f km", facility.getCalculatedDistance()));
        
        // Operating hours
        hoursPane.getChildren().clear();
        OperatingHours hours = facility.getHours();
        if (hours != null) {
            for (DayOfWeek day : hours.getOpenDays()) {
                OperatingHours.TimeSlot slot = hours.getHours(day);
                if (slot != null) {
                    Label hourLabel = new Label(day.toString() + ": " + slot.toString());
                    hoursPane.getChildren().add(hourLabel);
                }
            }
        }
        
        // Guidelines (we're reusing donation guidelines for composting guidelines)
        guidelinesList.getItems().clear();
        guidelinesList.getItems().addAll(facility.getDonationGuidelines());
        
        // Accepted items
        acceptedItemsList.getItems().clear();
        acceptedItemsList.getItems().addAll(facility.getAcceptedItems());
        
        // Non-accepted items
        notAcceptedItemsList.getItems().clear();
        notAcceptedItemsList.getItems().addAll(facility.getNonAcceptedItems());
        
        // Update pickup availability warning if needed
        if (requestPickupRadio.isSelected() && !homeCompostingCheck.isSelected()) {
            pickupAvailabilityLabel.setVisible(!facility.isPickupAvailable());
        }
    }
    
    /**
     * Clear facility details when no facility is selected
     */
    private void clearFacilityDetails() {
        nameLabel.setText("");
        addressLabel.setText("");
        contactLabel.setText("");
        distanceLabel.setText("");
        hoursPane.getChildren().clear();
        guidelinesList.getItems().clear();
        acceptedItemsList.getItems().clear();
        notAcceptedItemsList.getItems().clear();
    }
    
    /**
     * Handle use current location button
     */
    @FXML
    private void handleUseCurrentLocation() {
        updateUserLocation();
        
        // Refresh facility distances
        if (currentUser != null) {
            for (LocalService facility : compostingFacilities) {
                double distance = calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    facility.getLatitude(), facility.getLongitude());
                facility.setCalculatedDistance(distance);
            }
            
            // Sort by distance
            compostingFacilities.sort((s1, s2) -> 
                Double.compare(s1.getCalculatedDistance(), s2.getCalculatedDistance()));
            
            // Refresh the combo box
            LocalService selected = facilityComboBox.getValue();
            facilityComboBox.setItems(FXCollections.observableArrayList(compostingFacilities));
            
            // Re-select the previously selected item, or the first item if none was selected
            if (selected != null) {
                facilityComboBox.setValue(selected);
            } else if (!compostingFacilities.isEmpty()) {
                facilityComboBox.getSelectionModel().selectFirst();
            }
        }
    }
    
    /**
     * Handle find nearest facility button
     */
    @FXML
    private void handleFindNearest() {
        if (!compostingFacilities.isEmpty()) {
            facilityComboBox.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Handle get directions button
     */
    @FXML
    private void handleGetDirections() {
        LocalService facility = facilityComboBox.getValue();
        if (facility == null) {
            showAlert("No Facility Selected", "Please select a composting facility first.", Alert.AlertType.WARNING);
            return;
        }
        
        // In a real app, this would open a maps application or show directions
        // For now, just show a dialog with the address
        showAlert("Directions to " + facility.getName(), 
                 "Address: " + facility.getAddress() + "\n" +
                 "Distance: " + String.format("%.1f km", facility.getCalculatedDistance()),
                 Alert.AlertType.INFORMATION);
    }
    
    /**
     * Handle contact facility button
     */
    @FXML
    private void handleContactFacility() {
        LocalService facility = facilityComboBox.getValue();
        if (facility == null) {
            showAlert("No Facility Selected", "Please select a composting facility first.", Alert.AlertType.WARNING);
            return;
        }
        
        // In a real app, this would open email or phone
        // For now, just show a dialog with the contact info
        showAlert("Contact " + facility.getName(), 
                 "Contact: " + facility.getContactInfo(),
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
                showAlert("Missing Information", "Please enter a name for the food waste item.", Alert.AlertType.WARNING);
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
            showAlert("Item Added", "The new food waste item has been added to your composting plan.", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            showAlert("Error", "Failed to add new food item: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Handle preview compost plan button
     */
    @FXML
    private void handlePreviewCompost() {
        // Validate item selection
        if (selectedFoodItems.isEmpty()) {
            showAlert("Missing Information", "Please select at least one food item to compost.", Alert.AlertType.WARNING);
            return;
        }
        
        // Check which composting method is selected
        boolean isHomeComposting = homeCompostingCheck.isSelected();
        
        if (!isHomeComposting) {
            // If not home composting, validate facility selection
            LocalService facility = facilityComboBox.getValue();
            
            if (facility == null) {
                showAlert("Missing Information", "Please select a composting facility.", Alert.AlertType.WARNING);
                return;
            }
            
            // Validate pickup details if requesting pickup
            if (requestPickupRadio.isSelected()) {
                if (!facility.isPickupAvailable()) {
                    showAlert("Pickup Not Available", 
                             "The selected facility does not offer pickup service. Please choose self-delivery or select a different facility.",
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
            
            // Update preview UI with facility info
            updatePreview(facility);
        } else {
            // For home composting
            updateHomeCompostingPreview();
        }
        
        // Show preview pane
        previewPane.setVisible(true);
        
        // Enable confirm button
        confirmButton.setDisable(false);
    }
    
    /**
     * Update the preview pane with facility composting details
     */
    private void updatePreview(LocalService facility) {
        // Method type
        previewMethodLabel.setText("Composting at facility:");
        
        // Facility details
        previewDestinationLabel.setText(facility.getName() + " - " + facility.getAddress());
        
        // Item list
        previewItemsList.getItems().clear();
        for (FoodItem item : selectedFoodItems) {
            previewItemsList.getItems().add(
                item.getName() + " - " + 
                String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        }
        
        // Delivery method
        if (selfDeliveryRadio.isSelected()) {
            previewDeliveryLabel.setText("Self-delivery to composting facility");
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
     * Update the preview pane with home composting details
     */
    private void updateHomeCompostingPreview() {
        // Method type
        previewMethodLabel.setText("Home composting method:");
        
        // No facility for home composting
        previewDestinationLabel.setText("At-home composting system");
        
        // Item list
        previewItemsList.getItems().clear();
        for (FoodItem item : selectedFoodItems) {
            previewItemsList.getItems().add(
                item.getName() + " - " + 
                String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        }
        
        // No delivery for home composting
        previewDeliveryLabel.setText("Items will be composted at home");
        previewPickupHeaderLabel.setVisible(false);
        previewPickupDetails.setVisible(false);
    }
    
    /**
     * Handle confirm composting button
     */
    @FXML
    private void handleConfirmCompost() {
        boolean isHomeComposting = homeCompostingCheck.isSelected();
        LocalService facility = isHomeComposting ? null : facilityComboBox.getValue();
        
        try {
            // Process each selected food item
            for (FoodItem item : selectedFoodItems) {
                // Add new items to user's collection if they're not already there
                if (newFoodItems.contains(item)) {
                    currentUser.addFoodItem(item);
                    FoodItem.allFoodItems.add(item);
                }
                
                // Update status
                item.setStatus(ItemStatus.COMPOSTED);
            }
            
            // Create a scheduled event for the composting if facility pickup requested
            if (!isHomeComposting && requestPickupRadio.isSelected() && facility != null) {
                LocalDate pickupDate = pickupDatePicker.getValue();
                String timeSlot = timeSlotComboBox.getValue();
                
                // Parse time slot to get start time
                String startTimeStr = timeSlot.split(" - ")[0];
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
                LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
                
                // Create the event
                ScheduledEvent event = new ScheduledEvent(
                    "Food Waste Pickup for Composting", 
                    "Pickup from " + addressField.getText(),
                    pickupDate.atTime(startTime),
                    facility,
                    currentUser
                );
                
                // Add composting details to event notes
                StringBuilder notes = new StringBuilder("Items to pick up for composting:\n");
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
            showSuccessMessage(facility);
            
        } catch (Exception e) {
            showAlert("Error", "Failed to process composting plan: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Show success message after composting is confirmed
     */
    private void showSuccessMessage(LocalService facility) {
        // Update success message
        boolean isHomeComposting = homeCompostingCheck.isSelected();
        
        StringBuilder detailsBuilder = new StringBuilder();
        
        if (isHomeComposting) {
            detailsBuilder.append("Your food waste has been marked for home composting. ")
                          .append("Remember to follow the home composting guidelines for best results.");
        } else {
            String method = selfDeliveryRadio.isSelected() ? "self-delivery" : "pickup";
            
            detailsBuilder.append("Your composting plan with ").append(facility.getName()).append(" has been scheduled for ");
            
            if (selfDeliveryRadio.isSelected()) {
                detailsBuilder.append("self-delivery.");
            } else {
                detailsBuilder.append("pickup on ")
                            .append(pickupDatePicker.getValue().toString())
                            .append(" during ")
                            .append(timeSlotComboBox.getValue())
                            .append(".");
            }
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
        confirmation.setTitle("Cancel Composting");
        confirmation.setHeaderText("Cancel Composting Process");
        confirmation.setContentText("Are you sure you want to cancel this composting plan? All entered information will be lost.");
        
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
        
        // Reset composting options
        selfDeliveryRadio.setSelected(true);
        selfDeliveryRadio.setDisable(false);
        requestPickupRadio.setDisable(false);
        homeCompostingCheck.setSelected(false);
        
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