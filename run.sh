#!/bin/bash

# This script runs the GreenCompostWaste application with JavaFX

# JavaFX version to use
JAVAFX_VERSION="21.0.2"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

. "$SCRIPT_DIR/scripts/javafx-env.sh"
ensure_javafx_home "$JAVAFX_VERSION" "$SCRIPT_DIR" || exit 1

# Ensure SQLite JDBC driver is available
SQLITE_JAR="$SCRIPT_DIR/sqlite-jdbc.jar"
if [ ! -f "$SQLITE_JAR" ]; then
  echo "ERROR: SQLite JDBC driver not found at $SQLITE_JAR"
  echo "Please run this script from the project root directory."
  exit 1
fi

# Common module path including JavaFX and SQLite driver
MODULE_PATH="$JAVAFX_HOME/lib:$SQLITE_JAR"

# Clean bin directory
echo "Cleaning bin directory..."
rm -rf bin
mkdir -p bin/views bin/styles

# Copy resources
echo "Copying resources..."
mkdir -p bin/views bin/styles

# Try different possible locations for resource files
# Try copying from src/main/resources first, then fallback to other locations
if [ -d "src/main/resources/views" ]; then
    echo "Copying FXML from src/main/resources/views"
    cp -r src/main/resources/views/*.fxml bin/views/ 2>/dev/null
elif [ -d "src/views" ]; then
    echo "Copying FXML from src/views"
    cp -r src/views/*.fxml bin/views/ 2>/dev/null
else
    echo "Warning: Could not find FXML files in expected locations"
    # Search for fxml files recursively under src
    echo "Searching for FXML files under src..."
    find src -name "*.fxml" -exec cp {} bin/views/ \; 2>/dev/null
fi

if [ -d "src/main/resources/styles" ]; then
    echo "Copying CSS from src/main/resources/styles"
    cp -r src/main/resources/styles/*.css bin/styles/ 2>/dev/null
elif [ -d "src/styles" ]; then
    echo "Copying CSS from src/styles"
    cp -r src/styles/*.css bin/styles/ 2>/dev/null
else
    echo "Warning: Could not find CSS files in expected locations"
    # Search for css files recursively under src
    echo "Searching for CSS files under src..."
    find src -name "*.css" -exec cp {} bin/styles/ \; 2>/dev/null
fi

# Compile the project
echo "Compiling GreenCompostWaste..."
javac --module-path "$MODULE_PATH" \
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
      -d bin $(find src -name "*.java")

# Run the application
echo "Running GreenCompostWaste..."
java --module-path "bin:$MODULE_PATH" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
     -m GreenCompostWaste/com.greencompost.main.Main
