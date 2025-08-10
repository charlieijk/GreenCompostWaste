package controllers;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the statistics view
 */
public class StatsController implements Initializable {

    @FXML private PieChart categoryChart;
    @FXML private BarChart<String, Number> expiryChart;
    @FXML private CategoryAxis expiryXAxis;
    @FXML private NumberAxis expiryYAxis;
    
    // Summary metrics
    @FXML private Label totalItemsLabel;
    @FXML private Label totalWeightLabel;
    @FXML private Label avgAgeLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private Label donatedLabel;
    @FXML private Label compostedLabel;
    
    // Detailed stats table
    @FXML private TableView<CategoryStat> statsTable;
    @FXML private TableColumn<CategoryStat, String> categoryColumn;
    @FXML private TableColumn<CategoryStat, Integer> countColumn;
    @FXML private TableColumn<CategoryStat, Double> weightColumn;
    @FXML private TableColumn<CategoryStat, Double> avgExpiryColumn;
    @FXML private TableColumn<CategoryStat, Double> percentColumn;
    
    // Export buttons
    @FXML private Button exportCsvButton;
    @FXML private Button printButton;
    
    private User currentUser;
    private List<FoodItem> userItems;
    
    /**
     * Static class to hold category statistics
     */
    public static class CategoryStat {
        private final String category;
        private final int count;
        private final double weight;
        private final double avgDaysUntilExpiry;
        private final double percentOfTotal;
        
        public CategoryStat(String category, int count, double weight, double avgDaysUntilExpiry, double percentOfTotal) {
            this.category = category;
            this.count = count;
            this.weight = weight;
            this.avgDaysUntilExpiry = avgDaysUntilExpiry;
            this.percentOfTotal = percentOfTotal;
        }
        
        public String getCategory() { return category; }
        public int getCount() { return count; }
        public double getWeight() { return weight; }
        public double getAvgDaysUntilExpiry() { return avgDaysUntilExpiry; }
        public double getPercentOfTotal() { return percentOfTotal; }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Get current user from MainController class
            currentUser = controllers.MainController.getCurrentUser();
            
            // Setup table columns (do this regardless of user to avoid null columns)
            setupTableColumns();
            
            // Initialize with empty data first to prevent null pointer exceptions
            initializeEmptyData();
            
            if (currentUser != null) {
                userItems = currentUser.getFoodItems();
                
                // If user has no items, use sample data for demonstration
                if (userItems == null || userItems.isEmpty()) {
                    System.out.println("User has no food items, using sample data");
                    userItems = createSampleFoodItems();
                }
                
                // Load statistics with actual or sample data
                loadStatistics();
            } else {
                System.err.println("Warning: Current user is null in StatsController");
                // Use sample data when user is null
                userItems = createSampleFoodItems();
                loadStatistics();
            }
        } catch (Exception e) {
            System.err.println("Error initializing StatsController: " + e.getMessage());
            e.printStackTrace();
            
            // Even if an error occurs, try to show some content
            try {
                userItems = createSampleFoodItems();
                loadStatistics();
            } catch (Exception ex) {
                System.err.println("Failed to load fallback statistics: " + ex.getMessage());
                // Show error message in UI
                showErrorMessage();
            }
        }
    }
    
    /**
     * Initialize charts and metrics with empty data to prevent null pointer exceptions
     */
    private void initializeEmptyData() {
        // Initialize pie chart with empty data
        categoryChart.setData(FXCollections.observableArrayList());
        
        // Initialize bar chart with empty data
        expiryChart.getData().clear();
        
        // Initialize table with empty data
        statsTable.setItems(FXCollections.observableArrayList());
        
        // Set default values for metrics
        totalItemsLabel.setText("0");
        totalWeightLabel.setText("0 kg");
        avgAgeLabel.setText("0 days");
        expiringSoonLabel.setText("0");
        donatedLabel.setText("0");
        compostedLabel.setText("0");
    }
    
    /**
     * Show error message when statistics cannot be loaded
     */
    private void showErrorMessage() {
        // Create simple error message for charts
        ObservableList<PieChart.Data> errorData = FXCollections.observableArrayList(
            new PieChart.Data("Error Loading Data", 1)
        );
        categoryChart.setData(errorData);
        
        // Reset other components
        expiryChart.getData().clear();
        statsTable.setItems(FXCollections.observableArrayList());
        
        // Set default values for metrics
        totalItemsLabel.setText("--");
        totalWeightLabel.setText("--");
        avgAgeLabel.setText("--");
        expiringSoonLabel.setText("--");
        donatedLabel.setText("--");
        compostedLabel.setText("--");
    }
    
    /**
     * Create sample food items for demonstration when user has no data
     */
    private List<FoodItem> createSampleFoodItems() {
        List<FoodItem> sampleItems = new ArrayList<>();
        
        // Create sample food items with different categories and expiry dates
        FoodItem apple = new FoodItem();
        apple.setName("Apples");
        apple.setQuantity(2.0);
        apple.setQuantityUnit("kg");
        apple.setCategory(FoodItem.FoodCategory.FRUIT);
        apple.setExpiryDate(java.time.LocalDateTime.now().plusDays(3));
        sampleItems.add(apple);
        
        FoodItem bread = new FoodItem();
        bread.setName("Bread");
        bread.setQuantity(1.0);
        bread.setQuantityUnit("loaf");
        bread.setCategory(FoodItem.FoodCategory.GRAIN);
        bread.setExpiryDate(java.time.LocalDateTime.now().plusDays(1));
        sampleItems.add(bread);
        
        FoodItem milk = new FoodItem();
        milk.setName("Milk");
        milk.setQuantity(2.0);
        milk.setQuantityUnit("liters");
        milk.setCategory(FoodItem.FoodCategory.DAIRY);
        milk.setExpiryDate(java.time.LocalDateTime.now().plusDays(5));
        sampleItems.add(milk);
        
        FoodItem pasta = new FoodItem();
        pasta.setName("Pasta");
        pasta.setQuantity(0.5);
        pasta.setQuantityUnit("kg");
        pasta.setCategory(FoodItem.FoodCategory.GRAIN);
        pasta.setExpiryDate(java.time.LocalDateTime.now().plusDays(30));
        sampleItems.add(pasta);
        
        FoodItem lasagna = new FoodItem();
        lasagna.setName("Leftover Lasagna");
        lasagna.setQuantity(1.0);
        lasagna.setQuantityUnit("portion");
        lasagna.setCategory(FoodItem.FoodCategory.LEFTOVER_MEAL);
        lasagna.setExpiryDate(java.time.LocalDateTime.now().plusDays(1));
        lasagna.setStatus(FoodItem.ItemStatus.DONATED);
        sampleItems.add(lasagna);
        
        FoodItem vegetables = new FoodItem();
        vegetables.setName("Mixed Vegetables");
        vegetables.setQuantity(1.5);
        vegetables.setQuantityUnit("kg");
        vegetables.setCategory(FoodItem.FoodCategory.VEGETABLE);
        vegetables.setExpiryDate(java.time.LocalDateTime.now().minusDays(1));
        vegetables.setStatus(FoodItem.ItemStatus.COMPOSTED);
        sampleItems.add(vegetables);
        
        return sampleItems;
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        
        weightColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getWeight()).asObject()
        );
        weightColumn.setCellFactory(new javafx.util.Callback<TableColumn<CategoryStat, Double>, TableCell<CategoryStat, Double>>() {
            @Override
            public TableCell<CategoryStat, Double> call(TableColumn<CategoryStat, Double> param) {
                return new TableCell<CategoryStat, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(String.format("%.1f kg", item));
                        }
                    }
                };
            }
        });
        
        avgExpiryColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAvgDaysUntilExpiry()).asObject()
        );
        avgExpiryColumn.setCellFactory(new javafx.util.Callback<TableColumn<CategoryStat, Double>, TableCell<CategoryStat, Double>>() {
            @Override
            public TableCell<CategoryStat, Double> call(TableColumn<CategoryStat, Double> param) {
                return new TableCell<CategoryStat, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(String.format("%.1f days", item));
                        }
                    }
                };
            }
        });
        
        percentColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPercentOfTotal()).asObject()
        );
        percentColumn.setCellFactory(new javafx.util.Callback<TableColumn<CategoryStat, Double>, TableCell<CategoryStat, Double>>() {
            @Override
            public TableCell<CategoryStat, Double> call(TableColumn<CategoryStat, Double> param) {
                return new TableCell<CategoryStat, Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(String.format("%.1f%%", item * 100));
                        }
                    }
                };
            }
        });
    }
    
    /**
     * Load statistics
     */
    private void loadStatistics() {
        // Initialize charts and metrics
        updateCategoryChart();
        updateExpiryChart();
        updateSummaryMetrics();
        updateDetailedStats();
    }
    
    /**
     * Update the category pie chart
     */
    private void updateCategoryChart() {
        // Count items by category
        Map<FoodItem.FoodCategory, Integer> categoryCounts = new EnumMap<>(FoodItem.FoodCategory.class);
        
        for (FoodItem item : userItems) {
            FoodItem.FoodCategory category = item.getCategory();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<FoodItem.FoodCategory, Integer> entry : categoryCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(
                formatCategoryName(entry.getKey().toString()), 
                entry.getValue()
            ));
        }
        
        categoryChart.setData(pieChartData);
        categoryChart.setTitle("");
    }
    
    /**
     * Update the expiry timeline bar chart
     */
    private void updateExpiryChart() {
        // Group items by expiry timeline
        Map<String, Integer> expiryGroups = new LinkedHashMap<>();
        expiryGroups.put("Expired", 0);
        expiryGroups.put("Today", 0);
        expiryGroups.put("Tomorrow", 0);
        expiryGroups.put("This Week", 0);
        expiryGroups.put("Next Week", 0);
        expiryGroups.put("Later", 0);
        
        for (FoodItem item : userItems) {
            long days = item.getDaysUntilExpiry();
            
            if (days <= 0) {
                expiryGroups.put("Expired", expiryGroups.get("Expired") + 1);
            } else if (days == 0) {
                expiryGroups.put("Today", expiryGroups.get("Today") + 1);
            } else if (days == 1) {
                expiryGroups.put("Tomorrow", expiryGroups.get("Tomorrow") + 1);
            } else if (days <= 7) {
                expiryGroups.put("This Week", expiryGroups.get("This Week") + 1);
            } else if (days <= 14) {
                expiryGroups.put("Next Week", expiryGroups.get("Next Week") + 1);
            } else {
                expiryGroups.put("Later", expiryGroups.get("Later") + 1);
            }
        }
        
        // Create bar chart data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Food Items");
        
        for (Map.Entry<String, Integer> entry : expiryGroups.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        expiryChart.getData().clear();
        expiryChart.getData().add(series);
    }
    
    /**
     * Update summary metrics
     */
    private void updateSummaryMetrics() {
        int totalItems = userItems.size();
        totalItemsLabel.setText(String.valueOf(totalItems));
        
        // Calculate total weight (convert to kg if possible)
        double totalWeight = 0;
        for (FoodItem item : userItems) {
            String unit = item.getQuantityUnit().toLowerCase();
            if (unit.contains("kg")) {
                totalWeight += item.getQuantity();
            } else if (unit.contains("g") && !unit.contains("kg")) {
                // Convert grams to kg
                totalWeight += item.getQuantity() / 1000.0;
            }
            // Other units are ignored for weight calculation
        }
        totalWeightLabel.setText(String.format("%.1f kg", totalWeight));
        
        // Calculate average item age
        double totalAge = 0;
        for (FoodItem item : userItems) {
            totalAge += (System.currentTimeMillis() - item.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()) / (1000.0 * 60 * 60 * 24);
        }
        double avgAge = totalItems > 0 ? totalAge / totalItems : 0;
        avgAgeLabel.setText(String.format("%.1f days", avgAge));
        
        // Count expiring soon
        int expiringSoon = 0;
        for (FoodItem item : userItems) {
            if (item.isExpiringSoon()) {
                expiringSoon++;
            }
        }
        expiringSoonLabel.setText(String.valueOf(expiringSoon));
        
        // Count by status
        int donated = 0;
        int composted = 0;
        
        for (FoodItem item : userItems) {
            if (item.getStatus() == FoodItem.ItemStatus.DONATED) {
                donated++;
            } else if (item.getStatus() == FoodItem.ItemStatus.COMPOSTED) {
                composted++;
            }
        }
        
        donatedLabel.setText(String.valueOf(donated));
        compostedLabel.setText(String.valueOf(composted));
    }
    
    /**
     * Update detailed statistics
     */
    private void updateDetailedStats() {
        // Group items by category
        Map<FoodItem.FoodCategory, List<FoodItem>> categoryGroups = userItems.stream()
            .collect(Collectors.groupingBy(FoodItem::getCategory));
        
        // Calculate stats for each category
        ObservableList<CategoryStat> stats = FXCollections.observableArrayList();
        
        for (Map.Entry<FoodItem.FoodCategory, List<FoodItem>> entry : categoryGroups.entrySet()) {
            List<FoodItem> items = entry.getValue();
            int count = items.size();
            
            // Calculate total weight (convert to kg if possible)
            double weight = 0;
            for (FoodItem item : items) {
                String unit = item.getQuantityUnit().toLowerCase();
                if (unit.contains("kg")) {
                    weight += item.getQuantity();
                } else if (unit.contains("g") && !unit.contains("kg")) {
                    // Convert grams to kg
                    weight += item.getQuantity() / 1000.0;
                }
                // Other units are ignored for weight calculation
            }
            
            // Calculate average days until expiry
            double totalDays = 0;
            for (FoodItem item : items) {
                totalDays += item.getDaysUntilExpiry();
            }
            double avgDays = count > 0 ? totalDays / count : 0;
            
            // Calculate percent of total
            double percent = (double) count / userItems.size();
            
            stats.add(new CategoryStat(
                formatCategoryName(entry.getKey().toString()),
                count,
                weight,
                avgDays,
                percent
            ));
        }
        
        // Sort by count (descending)
        stats.sort(Comparator.comparing(CategoryStat::getCount).reversed());
        
        statsTable.setItems(stats);
    }
    
    /**
     * Format a category name (e.g. "VEGETABLE" -> "Vegetable")
     */
    private String formatCategoryName(String category) {
        if (category == null || category.isEmpty()) {
            return "";
        }
        
        category = category.toLowerCase();
        return category.substring(0, 1).toUpperCase() + category.substring(1);
    }
    
    /**
     * Handle exporting stats to CSV
     */
    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Statistics");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("food_waste_statistics.csv");
        
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                writer.println("Category,Count,Weight (kg),Avg Days Until Expiry,Percent of Total");
                
                // Write data
                for (CategoryStat stat : statsTable.getItems()) {
                    writer.printf("%s,%d,%.2f,%.2f,%.2f%%\n",
                        stat.getCategory(),
                        stat.getCount(),
                        stat.getWeight(),
                        stat.getAvgDaysUntilExpiry(),
                        stat.getPercentOfTotal() * 100
                    );
                }
                
                // Write summary
                writer.println();
                writer.println("Summary Statistics,");
                writer.printf("Total Items,%d\n", userItems.size());
                
                double totalWeight = 0;
                for (FoodItem item : userItems) {
                    if (item.getQuantityUnit().toLowerCase().contains("kg")) {
                        totalWeight += item.getQuantity();
                    }
                }
                writer.printf("Total Weight,%.2f kg\n", totalWeight);
                
                int expiringSoon = 0;
                for (FoodItem item : userItems) {
                    if (item.isExpiringSoon()) {
                        expiringSoon++;
                    }
                }
                writer.printf("Items Expiring Soon,%d\n", expiringSoon);
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Statistics exported successfully to " + file.getPath());
                alert.showAndWait();
                
            } catch (IOException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText(null);
                alert.setContentText("Error exporting statistics: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Handle printing a report
     */
    @FXML
    private void handlePrint() {
        // This would typically print a report
        // We'll just show an alert for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Report");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("This feature is not yet implemented. In the future, you'll be able to print detailed waste statistics reports.");
        alert.showAndWait();
    }
}