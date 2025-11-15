# GreenCompost Connect Application

GreenCompostWaste is a quick Java/Maven project I built for extra credit whilst finishing up at UCC. It has since grown into a full-featured JavaFX application for managing food waste, connecting with local composting services, and tracking food inventory.

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

### Quick Start (Apple Silicon)

If you're on an Apple Silicon Mac, the bundled JavaFX SDK (`javafx/javafx-sdk-21.0.2`) is Intel-only. Bring your own arm64 build and point the scripts at it:

1. Download the latest macOS/aarch64 JavaFX SDK from [Gluon](https://gluonhq.com/products/javafx/) (for example `openjfx-21.0.2_osx-aarch64_bin-sdk.zip`) and unzip it.
2. Copy the extracted folder into this repo (or anywhere else on disk) and keep track of the path. A simple option is:
   ```bash
   mkdir -p "$REPO_ROOT/javafx"
   mv ~/Downloads/javafx-sdk-21.0.2 "$REPO_ROOT/javafx/javafx-sdk-21.0.2-aarch64"
   ```
3. Run the app by explicitly pointing `JAVAFX_HOME` at that folder:
   ```bash
   cd /path/to/GreenCompostWaste
   JAVAFX_HOME="$PWD/javafx/javafx-sdk-21.0.2-aarch64" ./run-simple.sh
   ```
   `run-simple.sh` skips the module system and is the quickest way to confirm the UI loads; once that works you can switch to `./run.sh` or `mvn clean javafx:run` if you want module-aware builds.

The Apple Silicon runtime only needs the `lib` folder, so using the `21.0.2` scripts with a newer JavaFX drop (e.g. 24.x) is fine—just keep the folder name consistent or set `JAVAFX_HOME` before launching.

### Other Run Modes

There are three more ways to run the application:

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

### JavaFX SDK Selection

The `run*.sh` scripts automatically try to locate a JavaFX SDK. On Intel-based macOS machines they will reuse the bundled SDK under `javafx/javafx-sdk-21.0.2`. When running on Apple Silicon they check for a user-provided SDK (via `JAVAFX_HOME`) before falling back to any arm64 bundle you drop under `javafx/` (for example `javafx/javafx-sdk-21.0.2-aarch64`). If neither option is available, set the `JAVAFX_HOME` environment variable to point at a JavaFX installation that matches your CPU architecture.

### Manual JavaFX setup (Apple Silicon)

Homebrew no longer ships the `openjfx` formula/cask, so the easiest path is to download the arm64 SDK directly from Gluon:

```bash
cd ~/Downloads
curl -L -o openjfx-sdk.zip https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_osx-aarch64_bin-sdk.zip
unzip openjfx-sdk.zip
REPO_ROOT=/path/to/GreenCompostWaste
mkdir -p "$REPO_ROOT/javafx"
mv ~/Downloads/javafx-sdk-21.0.2 "$REPO_ROOT/javafx/javafx-sdk-21.0.2-aarch64"
cd "$REPO_ROOT"
JAVAFX_HOME="$PWD/javafx/javafx-sdk-21.0.2-aarch64" ./run.sh
```

You can keep the extracted SDK anywhere on disk if you prefer—just point `JAVAFX_HOME` to the folder that contains the `lib` directory from the download. As an alternative, install a JDK distribution that bundles JavaFX (for example `brew install --cask liberica-jdk17-full`) and set `JAVAFX_HOME` to that JDK’s `Contents/Home`.

## Troubleshooting

If you encounter issues with running the application:

1. Make sure an arm64 JavaFX SDK is available (either by setting `JAVAFX_HOME` or by placing the extracted SDK inside the `javafx/` directory)
2. Check that the `run.sh` and `run-simple.sh` scripts have execute permissions
3. Verify that paths in the script match your system configuration
4. If you see `Graphics Device initialization failed for : sw`, verify that the JavaFX SDK matches your CPU architecture. Apple Silicon users should download the arm64 build or install a JavaFX-enabled JDK and point `JAVAFX_HOME` at it.

For permission errors:
```bash
chmod +x run.sh run-simple.sh
```

## Dependencies

- Java 17+
- JavaFX 21.0.2
- SQLite JDBC 3.45.1.0
