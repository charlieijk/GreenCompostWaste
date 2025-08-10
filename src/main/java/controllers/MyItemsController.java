package controllers;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Controller for the my food items view
 */
public class MyItemsController implements Initializable {

    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<FoodItem> itemsTable;
    @FXML private TableColumn<FoodItem, String> nameColumn;
    @FXML private TableColumn<FoodItem, String> quantityColumn;
    @FXML private TableColumn<FoodItem, String> categoryColumn;
    @FXML private TableColumn<FoodItem, String> expiryColumn;
    @FXML private TableColumn<FoodItem, String> statusColumn;
    @FXML private Button removeButton;
    @FXML private Button donateButton;
    @FXML private Button compostButton;
    @FXML private GridPane detailsPane;
    @FXML private VBox noSelectionPane;
    
    // Detail labels
    @FXML private Label itemNameLabel;
    @FXML private Label quantityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label createdLabel;
    @FXML private Label expiryLabel;
    @FXML private Label daysLeftLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label recommendationLabel;
    
    private User currentUser;
    private ObservableList<FoodItem> allItems;
    private FilteredList<FoodItem> filteredItems;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from App class
        currentUser = controllers.MainController.getCurrentUser();
        
        // Setup table columns
        setupTableColumns();
        
        // Initialize item list
        allItems = FXCollections.observableArrayList(currentUser.getFoodItems());
        filteredItems = new FilteredList<>(allItems);
        itemsTable.setItems(filteredItems);
        
        // Setup filter combo box
        setupFilterComboBox();
        
        // Setup search field
        setupSearchField();
        
        // Add table selection listener with explicit implementation to avoid unused parameter warnings
        itemsTable.getSelectionModel().selectedItemProperty().addListener(
            new javafx.beans.value.ChangeListener<FoodItem>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends FoodItem> observable,
                                  FoodItem oldValue, FoodItem newValue) {
                    showItemDetails(newValue);
                }
            }
        );
        
        // Initially hide details pane
        detailsPane.setVisible(false);
        noSelectionPane.setVisible(true);
        
        // Disable action buttons initially
        removeButton.setDisable(true);
        donateButton.setDisable(true);
        compostButton.setDisable(true);
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        quantityColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new SimpleStringProperty(
                String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit())
            );
        });
        
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory().toString())
        );
        
        expiryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedExpiryDate())
        );
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString())
        );
        
        // Style based on expiry status - use Callback to avoid lambda parameter warning
        expiryColumn.setCellFactory(new javafx.util.Callback<TableColumn<FoodItem, String>, TableCell<FoodItem, String>>() {
            @Override
            public TableCell<FoodItem, String> call(TableColumn<FoodItem, String> param) {
                return new TableCell<FoodItem, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            
                            FoodItem foodItem = getTableView().getItems().get(getIndex());
                            if (foodItem.isExpired()) {
                                setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;"); // Red for expired
                            } else if (foodItem.isExpiringSoon()) {
                                setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;"); // Orange for expiring soon
                            } else {
                                setStyle("-fx-text-fill: #388e3c;"); // Green for good
                            }
                        }
                    }
                };
            }
        });
    }
    
    /**
     * Setup filter combo box
     */
    private void setupFilterComboBox() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
            "All Categories",
            "Vegetable",
            "Fruit",
            "Dairy",
            "Grain",
            "Protein",
            "Leftover Meal",
            "Other"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.getSelectionModel().selectFirst();
        
        // Use explicit implementation without lambda to avoid parameter warnings
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            new javafx.beans.value.ChangeListener<String>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends String> observable, 
                                  String oldValue, String newValue) {
                    applyFilters();
                }
            }
        );
    }
    
    /**
     * Setup search field
     */
    private void setupSearchField() {
        // Use explicit implementation without lambda to avoid parameter warnings
        searchField.textProperty().addListener(
            new javafx.beans.value.ChangeListener<String>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends String> observable, 
                                  String oldValue, String newValue) {
                    applyFilters();
                }
            }
        );
    }
    
    /**
     * Apply filters based on category and search text
     */
    private void applyFilters() {
        String category = filterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase().trim();
        
        // Create predefined predicates to avoid lambda parameter warnings
        final Predicate<FoodItem> ALWAYS_TRUE = new Predicate<FoodItem>() {
            @Override
            public boolean test(FoodItem t) {
                return true;
            }
        };
        
        Predicate<FoodItem> categoryFilter;
        if (category.equals("All Categories")) {
            categoryFilter = ALWAYS_TRUE;
        } else {
            categoryFilter = item -> item.getCategory().toString().equals(category.toUpperCase());
        }
        
        Predicate<FoodItem> searchFilter;
        if (searchText.isEmpty()) {
            searchFilter = ALWAYS_TRUE;
        } else {
            searchFilter = item -> 
                item.getName().toLowerCase().contains(searchText) ||
                item.getDescription().toLowerCase().contains(searchText);
        }
        
        filteredItems.setPredicate(categoryFilter.and(searchFilter));
    }
    
    /**
     * Show details for a selected item
     */
    private void showItemDetails(FoodItem item) {
        if (item == null) {
            detailsPane.setVisible(false);
            noSelectionPane.setVisible(true);
            removeButton.setDisable(true);
            donateButton.setDisable(true);
            compostButton.setDisable(true);
            return;
        }
        
        // Set details
        itemNameLabel.setText(item.getName());
        quantityLabel.setText(String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        categoryLabel.setText(item.getCategory().toString());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        createdLabel.setText(item.getCreatedAt().format(formatter));
        expiryLabel.setText(item.getExpiryDate().format(formatter));
        
        long daysUntil = item.getDaysUntilExpiry();
        daysLeftLabel.setText(daysUntil + " days");
        
        // Style days left label based on expiry status
        if (item.isExpired()) {
            daysLeftLabel.setText("EXPIRED");
            daysLeftLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;"); // Red for expired
        } else if (item.isExpiringSoon()) {
            daysLeftLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;"); // Orange for expiring soon
        } else {
            daysLeftLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;"); // Green for good
        }
        
        statusLabel.setText(item.getStatus().toString());
        descriptionArea.setText(item.getDescription());
        recommendationLabel.setText(item.getRecommendation());
        
        // Show details pane
        detailsPane.setVisible(true);
        noSelectionPane.setVisible(false);
        
        // Enable action buttons
        removeButton.setDisable(false);
        donateButton.setDisable(false);
        compostButton.setDisable(false);
    }
    
    /**
     * Handle removing a selected item
     */
    @FXML
    private void handleRemove() {
        FoodItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Remove Item");
        confirmation.setHeaderText("Remove Food Item");
        confirmation.setContentText("Are you sure you want to remove " + selectedItem.getName() + "?");
        
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            currentUser.removeFoodItem(selectedItem);
            allItems.remove(selectedItem);
            showItemDetails(null);
        }
    }
    
    /**
     * Handle donating a selected item
     */
    @FXML
    private void handleDonate() {
        FoodItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        
        selectedItem.setStatus(FoodItem.ItemStatus.DONATED);
        itemsTable.refresh();
        showItemDetails(selectedItem);
    }
    
    /**
     * Handle composting a selected item
     */
    @FXML
    private void handleCompost() {
        FoodItem selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        
        selectedItem.setStatus(FoodItem.ItemStatus.COMPOSTED);
        itemsTable.refresh();
        showItemDetails(selectedItem);
    }
}