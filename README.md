# GreenCompost Connect Application

A JavaFX application for managing food waste, connecting with local composting services, and tracking food inventory.

## Database Integration

The application uses SQLite for persistent data storage through the DatabaseManager class. This ensures that all data is preserved between application runs.

### Database Structure

The SQLite database (`greencompost.db`) contains the following tables:

1. **users** - Stores user information
   - id (PRIMARY KEY)
   - username (UNIQUE)
   - password
   - name
   - email
   - createdAt

2. **food_items** - Stores food items logged by users
   - id (PRIMARY KEY)
   - name
   - category (VEGETABLE, FRUIT, DAIRY, etc.)
   - quantity
   - expirationDate
   - status
   - userId (FOREIGN KEY to users.id)
   - createdAt

3. **services** - Stores local services information
   - id (PRIMARY KEY)
   - name
   - description
   - address
   - contactInfo
   - serviceType (FOOD_BANK, COMPOSTING_FACILITY, etc.)

4. **operating_hours** - Stores operating hours for services
   - id (PRIMARY KEY)
   - serviceId (FOREIGN KEY to services.id)
   - dayOfWeek (0-6, where 0 is Sunday)
   - openTime
   - closeTime

5. **events** - Stores scheduled events by services
   - id (PRIMARY KEY)
   - title
   - description
   - location
   - startTime
   - endTime
   - serviceId (FOREIGN KEY to services.id)

### Usage

The `DatabaseManager` class provides methods for:

- Creating and initializing the database
- User operations (save, find, authenticate)
- Food item operations (add, update, query)
- Local service operations (save, find)
- Event operations (save, find)

The database manager is implemented as a singleton to ensure a single connection throughout the application.

Sample data is automatically initialized when the application starts.

## Building the Project

The project uses Maven for dependency management. To build:

```
mvn clean package
```

## Running the Application

There are three ways to run the application:

### 1. Using the run.sh script (recommended for macOS/Linux)

```bash
./run.sh
```

This script will:
- Set up the JavaFX environment
- Compile the project with the Java module system
- Run the application

### 2. Using the run-simple.sh script (alternative for macOS/Linux)

```bash
./run-simple.sh
```

This script:
- Uses a simpler compilation approach without the module system
- May work better in some environments
- Provides more detailed error messages

### 3. Using Maven

```bash
mvn clean javafx:run
```

This approach uses Maven to:
- Compile the project
- Set up dependencies
- Run the JavaFX application

### 4. Using the JAR file (after building)

```bash
java -jar target/GreenCompostWaste-1.0-SNAPSHOT.jar
```

## Troubleshooting

If you encounter issues with running the application:

1. Make sure JavaFX SDK is properly located in the `javafx` directory
2. Check that the `run.sh` and `run-simple.sh` scripts have execute permissions
3. Verify that paths in the script match your system configuration

For permission errors:
```bash
chmod +x run.sh run-simple.sh
```

## Dependencies

- Java 17+
- JavaFX 21.0.2
- SQLite JDBC 3.45.1.0