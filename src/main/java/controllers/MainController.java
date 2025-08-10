package controllers;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import com.greencompost.service.LocalService;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the main dashboard view
 */
public class MainController implements Initializable {
    // Static resources for other controllers
    private static User currentUserStatic;
    private static FoodItem foodItemControllerStatic;

    // Static access methods for other controllers
    public static User getCurrentUser() {
        if (currentUserStatic == null) {
            try {
                // Access Main class from proper package
                Class<?> mainClass = Class.forName("com.greencompost.main.Main");
                java.lang.reflect.Method getCurrentUserMethod = mainClass.getMethod("getCurrentUser");
                Object result = getCurrentUserMethod.invoke(null, (Object[]) null);
                if (result instanceof User) {
                    currentUserStatic = (User) result;
                }
            } catch (Exception e) {
                System.err.println("Error accessing Main.getCurrentUser: " + e.getMessage());
                // Create a default user as fallback
                currentUserStatic = new User();
                currentUserStatic.setUsername("Default User");
            }
        }
        return currentUserStatic;
    }

    public static FoodItem getFoodItemController() {
        if (foodItemControllerStatic == null) {
            try {
                // Access Main class from proper package
                Class<?> mainClass = Class.forName("com.greencompost.main.Main");
                java.lang.reflect.Method getFoodItemControllerMethod = mainClass.getMethod("getFoodItemController");
                Object result = getFoodItemControllerMethod.invoke(null, (Object[]) null);
                if (result instanceof FoodItem) {
                    foodItemControllerStatic = (FoodItem) result;
                }
            } catch (Exception e) {
                System.err.println("Error accessing Main.getFoodItemController: " + e.getMessage());
                // Create a new controller as fallback
                foodItemControllerStatic = new FoodItem();
            }
        }
        return foodItemControllerStatic;
    }

    // User interface components
    @FXML
    private Label userLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox dashboardView;

    // Dashboard statistics
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label expiringSoonLabel;
    @FXML
    private Label nearbyServicesLabel;

    // Navigation buttons
    @FXML
    private Button dashboardBtn;
    @FXML
    private MenuItem logFoodBtn;
    @FXML
    private MenuItem findServicesBtn;
    @FXML
    private MenuItem findNearbyServicesBtn;
    @FXML
    private MenuItem itemsBtn;
    @FXML
    private MenuItem statsBtn;
    @FXML
    private MenuItem donateBtn;
    @FXML
    private MenuItem compostBtn;
    @FXML
    private Button logoutButton;
    @FXML
    private MenuButton foodManagementMenu;
    @FXML
    private MenuButton servicesMenu;
    @FXML
    private MenuButton solutionsMenu;

    // Tables
    @FXML
    private TableView<FoodItem> expiringSoonTable;
    @FXML
    private TableColumn<FoodItem, String> itemNameColumn;
    @FXML
    private TableColumn<FoodItem, String> quantityColumn;
    @FXML
    private TableColumn<FoodItem, String> expiryColumn;
    @FXML
    private TableColumn<FoodItem, String> recommendationColumn;

    @FXML
    private TableView<LocalService> servicesTable;
    @FXML
    private TableColumn<LocalService, String> serviceNameColumn;
    @FXML
    private TableColumn<LocalService, String> serviceTypeColumn;
    @FXML
    private TableColumn<LocalService, String> distanceColumn;
    @FXML
    private TableColumn<LocalService, String> hoursColumn;

    // Data
    private User currentUser;
    private ObservableList<FoodItem> expiringSoonItems;
    private ObservableList<LocalService> nearbyServices;

    // Child views
    private Parent logFoodView;
    private Parent myItemsView;
    private Parent findServicesView;
    private Parent findNearbyServicesView;
    private Parent statsView;
    private Parent donateView;
    private Parent compostView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user using the static method
        currentUser = getCurrentUser();

        // Setup user info
        userLabel.setText("Welcome, " + currentUser.getUsername() + "!");

        // Setup tables
        setupExpiringSoonTable();
        setupServicesTable();

        // Load initial data
        refreshDashboard();

        // By default, show dashboard view
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardView);
        setActiveNavButton(dashboardBtn);

        // Preload other views
        loadChildViews();
    }
    
    /**
     * Set the active navigation item and highlight the corresponding menu
     * 
     * @param item The navigation item to mark as active
     */
    private void setActiveNavButton(Object item) {
        // Remove active class from main button
        if (dashboardBtn != null) {
            dashboardBtn.getStyleClass().remove("active-nav-button");
        }
        
        // Reset all menu buttons
        if (foodManagementMenu != null) {
            foodManagementMenu.getStyleClass().remove("active-nav-button");
        }
        if (servicesMenu != null) {
            servicesMenu.getStyleClass().remove("active-nav-button");
        }
        if (solutionsMenu != null) {
            solutionsMenu.getStyleClass().remove("active-nav-button");
        }
        
        // Handle different types of navigation items
        if (item instanceof Button) {
            // For the Dashboard button
            ((Button) item).getStyleClass().add("active-nav-button");
        } else if (item instanceof MenuItem) {
            // For menu items, highlight the parent menu
            MenuItem menuItem = (MenuItem) item;
            
            // Determine which menu the item belongs to
            MenuButton parentMenu = null;
            if (menuItem == logFoodBtn || menuItem == itemsBtn || menuItem == statsBtn) {
                parentMenu = foodManagementMenu;
            } else if (menuItem == findServicesBtn || menuItem == findNearbyServicesBtn) {
                parentMenu = servicesMenu;
            } else if (menuItem == donateBtn || menuItem == compostBtn) {
                parentMenu = solutionsMenu;
            }
            
            // Highlight the parent menu
            if (parentMenu != null) {
                parentMenu.getStyleClass().add("active-nav-button");
            }
        }
    }

    /**
     * Setup the expiring soon items table
     */
    private void setupExpiringSoonTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        quantityColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            return new SimpleStringProperty(
                    String.format("%.1f %s", item.getQuantity(), item.getQuantityUnit()));
        });

        expiryColumn.setCellValueFactory(cellData -> {
            FoodItem item = cellData.getValue();
            long days = item.getDaysUntilExpiry();
            String text = days <= 0 ? "Expired" : days + " days";
            return new SimpleStringProperty(text);
        });

        recommendationColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRecommendation()));

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

                            if (item.equals("Expired")) {
                                setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
                            } else if (item.equals("1 days") || item.equals("2 days")) {
                                setStyle("-fx-text-fill: #ff8f00; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #388e3c;");
                            }
                        }
                    }
                };
            }
        });

        // Initialize observable list
        expiringSoonItems = FXCollections.observableArrayList();
        expiringSoonTable.setItems(expiringSoonItems);
    }

    /**
     * Setup the nearby services table
     */
    private void setupServicesTable() {
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        serviceTypeColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getType().getDisplayName()));

        distanceColumn.setCellValueFactory(cellData -> {
            LocalService service = cellData.getValue();
            double distance = calculateDistance(
                    currentUser.getLatitude(), currentUser.getLongitude(),
                    service.getLatitude(), service.getLongitude());
            return new SimpleStringProperty(String.format("%.1f km", distance));
        });

        hoursColumn.setCellValueFactory(cellData -> {
            LocalService service = cellData.getValue();
            return new SimpleStringProperty(service.getOperatingHours().toString().split("\n")[0]);
        });

        // Initialize observable list
        nearbyServices = FXCollections.observableArrayList();
        servicesTable.setItems(nearbyServices);
    }

    /**
     * Load other views that will be shown when user navigates
     */
    private void loadChildViews() {
        try {
            // Try different paths to locate the FXML files
            URL logFoodURL = getClass().getResource("../views/LogFoodView.fxml");
            if (logFoodURL == null) {
                // Try alternative path
                logFoodURL = getClass().getResource("/views/LogFoodView.fxml");
            }
            if (logFoodURL == null) {
                throw new IOException("Cannot find LogFoodView.fxml");
            }
            logFoodView = FXMLLoader.load(logFoodURL);

            URL myItemsURL = getClass().getResource("../views/MyItemsView.fxml");
            if (myItemsURL == null) {
                myItemsURL = getClass().getResource("/views/MyItemsView.fxml");
            }
            if (myItemsURL == null) {
                throw new IOException("Cannot find MyItemsView.fxml");
            }
            myItemsView = FXMLLoader.load(myItemsURL);

            URL findServicesURL = getClass().getResource("../views/FindServicesView.fxml");
            if (findServicesURL == null) {
                findServicesURL = getClass().getResource("/views/FindServicesView.fxml");
            }
            if (findServicesURL == null) {
                throw new IOException("Cannot find FindServicesView.fxml");
            }
            findServicesView = FXMLLoader.load(findServicesURL);
            
            
            // Load the Find Nearby Services view
            URL findNearbyServicesURL = getClass().getResource("../views/FindNearbyServicesView.fxml");
            if (findNearbyServicesURL == null) {
                findNearbyServicesURL = getClass().getResource("/views/FindNearbyServicesView.fxml");
            }
            if (findNearbyServicesURL == null) {
                throw new IOException("Cannot find FindNearbyServicesView.fxml");
            }
            findNearbyServicesView = FXMLLoader.load(findNearbyServicesURL);

            URL statsURL = getClass().getResource("../views/StatsView.fxml");
            if (statsURL == null) {
                statsURL = getClass().getResource("/views/StatsView.fxml");
            }
            if (statsURL == null) {
                throw new IOException("Cannot find StatsView.fxml");
            }
            
            try {
                // Use a more robust approach for loading the stats view
                FXMLLoader statsLoader = new FXMLLoader(statsURL);
                statsView = statsLoader.load();
                System.out.println("Successfully loaded StatsView.fxml");
            } catch (Exception e) {
                System.err.println("Error loading StatsView.fxml: " + e.getMessage());
                e.printStackTrace();
                // Create a fallback view if loading fails
                statsView = new javafx.scene.layout.VBox();
                ((javafx.scene.layout.VBox)statsView).getChildren().add(
                    new javafx.scene.control.Label("Statistics view could not be loaded.")
                );
            }
            
            // Load the new Donate view
            URL donateURL = getClass().getResource("../views/DonateView.fxml");
            if (donateURL == null) {
                donateURL = getClass().getResource("/views/DonateView.fxml");
            }
            if (donateURL == null) {
                throw new IOException("Cannot find DonateView.fxml");
            }
            
            try {
                FXMLLoader donateLoader = new FXMLLoader(donateURL);
                donateView = donateLoader.load();
                System.out.println("Successfully loaded DonateView.fxml");
            } catch (Exception e) {
                System.err.println("Error loading DonateView.fxml: " + e.getMessage());
                e.printStackTrace();
                // Create a fallback view if loading fails
                donateView = new javafx.scene.layout.VBox();
                ((javafx.scene.layout.VBox)donateView).getChildren().add(
                    new javafx.scene.control.Label("Donate view could not be loaded.")
                );
            }
            
            // Load the new Compost view
            URL compostURL = getClass().getResource("../views/CompostView.fxml");
            if (compostURL == null) {
                compostURL = getClass().getResource("/views/CompostView.fxml");
            }
            if (compostURL == null) {
                throw new IOException("Cannot find CompostView.fxml");
            }
            
            try {
                FXMLLoader compostLoader = new FXMLLoader(compostURL);
                compostView = compostLoader.load();
                System.out.println("Successfully loaded CompostView.fxml");
            } catch (Exception e) {
                System.err.println("Error loading CompostView.fxml: " + e.getMessage());
                e.printStackTrace();
                // Create a fallback view if loading fails
                compostView = new javafx.scene.layout.VBox();
                ((javafx.scene.layout.VBox)compostView).getChildren().add(
                    new javafx.scene.control.Label("Compost view could not be loaded.")
                );
            }

        } catch (IOException e) {
            showStatus("Error loading views: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh dashboard data
     */
    private void refreshDashboard() {
        // Update statistics
        List<FoodItem> userItems = currentUser.getFoodItems();
        int totalItems = userItems.size();
        totalItemsLabel.setText(String.valueOf(totalItems));

        // Find expiring soon items
        List<FoodItem> expiring = FoodItem.findExpiringSoon();
        int expiringSoon = 0;
        for (FoodItem item : expiring) {
            if (userItems.contains(item)) {
                expiringSoon++;
            }
        }
        expiringSoonLabel.setText(String.valueOf(expiringSoon));

        // Find nearby services
        List<LocalService> services = LocalService.findNearbyServicesForUser(currentUser, 10);
        nearbyServicesLabel.setText(String.valueOf(services.size()));

        // Update tables
        expiringSoonItems.clear();
        expiringSoonItems.addAll(expiring);

        nearbyServices.clear();
        nearbyServices.addAll(services);
    }

    /**
     * Calculate distance between two points
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
     * Show a status message
     */
    private void showStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Switch to dashboard view
     */
    @FXML
    private void switchToDashboard() {
        refreshDashboard();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardView);
        showStatus("Dashboard");
        setActiveNavButton(dashboardBtn);
    }

    /**
     * Switch to log food view
     */
    @FXML
    private void switchToLogFood() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(logFoodView);
        showStatus("Log New Food Item");
        setActiveNavButton(logFoodBtn);
    }

    /**
     * Switch to find services view
     */
    @FXML
    private void switchToFindServices() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(findServicesView);
        showStatus("Advanced Search");
        setActiveNavButton(findServicesBtn);
    }
    
    
    /**
     * Switch to find nearby services view
     */
    @FXML
    private void switchToFindNearbyServices() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(findNearbyServicesView);
        showStatus("Find Nearby Services");
        setActiveNavButton(findNearbyServicesBtn);
    }

    /**
     * Switch to my items view
     */
    @FXML
    private void switchToItems() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(myItemsView);
        showStatus("My Food Items");
        setActiveNavButton(itemsBtn);
    }

    /**
     * Switch to statistics view
     */
    @FXML
    private void switchToStats() {
        contentArea.getChildren().clear();
        
        if (statsView != null) {
            contentArea.getChildren().add(statsView);
            showStatus("Statistics");
            setActiveNavButton(statsBtn);
        } else {
            // Handle case where statsView failed to load
            javafx.scene.layout.VBox errorView = new javafx.scene.layout.VBox();
            errorView.setAlignment(javafx.geometry.Pos.CENTER);
            errorView.setSpacing(10);
            
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Statistics view could not be loaded.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            
            javafx.scene.control.Button retryButton = new javafx.scene.control.Button("Retry Loading");
            retryButton.setOnAction(e -> {
                try {
                    // Try to reload the stats view
                    URL statsURL = getClass().getResource("/views/StatsView.fxml");
                    if (statsURL != null) {
                        statsView = FXMLLoader.load(statsURL);
                        switchToStats(); // Retry switch after reload
                    }
                } catch (Exception ex) {
                    showStatus("Failed to reload Statistics view: " + ex.getMessage());
                }
            });
            
            errorView.getChildren().addAll(errorLabel, retryButton);
            contentArea.getChildren().add(errorView);
            showStatus("Error: Statistics could not be loaded");
            setActiveNavButton(statsBtn);
        }
    }
    
    /**
     * Switch to donate view
     */
    @FXML
    private void switchToDonate() {
        contentArea.getChildren().clear();
        
        if (donateView != null) {
            contentArea.getChildren().add(donateView);
            showStatus("Donate Food");
            setActiveNavButton(donateBtn);
        } else {
            // Handle case where donateView failed to load
            javafx.scene.layout.VBox errorView = new javafx.scene.layout.VBox();
            errorView.setAlignment(javafx.geometry.Pos.CENTER);
            errorView.setSpacing(10);
            
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Donate view could not be loaded.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            
            javafx.scene.control.Button retryButton = new javafx.scene.control.Button("Retry Loading");
            retryButton.setOnAction(e -> {
                try {
                    // Try to reload the donate view
                    URL donateURL = getClass().getResource("/views/DonateView.fxml");
                    if (donateURL != null) {
                        FXMLLoader donateLoader = new FXMLLoader(donateURL);
                        donateView = donateLoader.load();
                        switchToDonate(); // Retry switch after reload
                    }
                } catch (Exception ex) {
                    showStatus("Failed to reload Donate view: " + ex.getMessage());
                }
            });
            
            errorView.getChildren().addAll(errorLabel, retryButton);
            contentArea.getChildren().add(errorView);
            showStatus("Error: Donate view could not be loaded");
            setActiveNavButton(donateBtn);
        }
    }
    
    /**
     * Switch to compost view
     */
    @FXML
    private void switchToCompost() {
        contentArea.getChildren().clear();
        
        if (compostView != null) {
            contentArea.getChildren().add(compostView);
            showStatus("Compost Food Waste");
            setActiveNavButton(compostBtn);
        } else {
            // Handle case where compostView failed to load
            javafx.scene.layout.VBox errorView = new javafx.scene.layout.VBox();
            errorView.setAlignment(javafx.geometry.Pos.CENTER);
            errorView.setSpacing(10);
            
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Compost view could not be loaded.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
            
            javafx.scene.control.Button retryButton = new javafx.scene.control.Button("Retry Loading");
            retryButton.setOnAction(e -> {
                try {
                    // Try to reload the compost view
                    URL compostURL = getClass().getResource("/views/CompostView.fxml");
                    if (compostURL != null) {
                        FXMLLoader compostLoader = new FXMLLoader(compostURL);
                        compostView = compostLoader.load();
                        switchToCompost(); // Retry switch after reload
                    }
                } catch (Exception ex) {
                    showStatus("Failed to reload Compost view: " + ex.getMessage());
                }
            });
            
            errorView.getChildren().addAll(errorLabel, retryButton);
            contentArea.getChildren().add(errorView);
            showStatus("Error: Compost view could not be loaded");
            setActiveNavButton(compostBtn);
        }
    }

    // Add these methods to your existing MainController class

    /**
     * Set the current user (called by LoginController)
     */
    public static void setCurrentUser(User user) {
        currentUserStatic = user;
    }

    /**
     * Handle user logout
     */
    @FXML
    private void handleLogout() {
        // Confirm logout
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Logout");
        confirmation.setHeaderText("Logout Confirmation");
        confirmation.setContentText("Are you sure you want to log out?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Load login view
                URL loginViewURL = getClass().getResource("/views/LoginView.fxml");
                if (loginViewURL == null) {
                    loginViewURL = getClass().getResource("../views/LoginView.fxml");
                }

                // Reset current user both in MainController and Main class
                currentUserStatic = null;
                
                try {
                    // Reset in Main class too
                    Class<?> mainClass = Class.forName("com.greencompost.main.Main");
                    java.lang.reflect.Method setCurrentUserMethod = mainClass.getMethod("setCurrentUser", User.class);
                    setCurrentUserMethod.invoke(null, new Object[] { null });
                } catch (Exception ex) {
                    System.err.println("Error resetting user in Main class: " + ex.getMessage());
                }
                
                FXMLLoader loader = new FXMLLoader(loginViewURL);
                Parent root = loader.load();

                // Create new scene
                Scene scene = new Scene(root);
                
                // Add CSS
                URL cssURL = getClass().getResource("/styles/styles.css");
                if (cssURL == null) {
                    cssURL = getClass().getResource("../styles/styles.css");
                }
                if (cssURL != null) {
                    scene.getStylesheets().add(cssURL.toExternalForm());
                }

                // Get current stage
                Stage stage = (Stage) logoutButton.getScene().getWindow();

                // Set new scene
                stage.setScene(scene);
                stage.setTitle("GreenCompost Connect - Login");
                stage.show();

            } catch (IOException e) {
                showStatus("Error loading login screen: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}