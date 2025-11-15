#!/bin/bash

# This script runs the GreenCompostWaste application with JavaFX without module system

# JavaFX version to use
JAVAFX_VERSION="21.0.2"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

. "$SCRIPT_DIR/scripts/javafx-env.sh"
ensure_javafx_home "$JAVAFX_VERSION" "$SCRIPT_DIR" || exit 1

# Define class paths
JAVAFX_CLASSPATH="$JAVAFX_HOME/lib/*"
SQLITE_CLASSPATH="$SCRIPT_DIR/sqlite-jdbc.jar"

# Clean temp_build directory
echo "Cleaning temp_build directory..."
rm -rf temp_build
mkdir -p temp_build/bin/views temp_build/bin/styles

# Copy resources
echo "Copying resources..."
mkdir -p temp_build/bin/views temp_build/bin/styles

# Try different possible locations for resource files
# Try copying from src/main/resources first, then fallback to other locations
if [ -d "src/main/resources/views" ]; then
    echo "Copying FXML from src/main/resources/views"
    cp -r src/main/resources/views/*.fxml temp_build/bin/views/ 2>/dev/null
elif [ -d "src/views" ]; then
    echo "Copying FXML from src/views"
    cp -r src/views/*.fxml temp_build/bin/views/ 2>/dev/null
else
    echo "Warning: Could not find FXML files in expected locations"
    # Search for fxml files recursively under src
    echo "Searching for FXML files under src..."
    find src -name "*.fxml" -exec cp {} temp_build/bin/views/ \; 2>/dev/null
fi

if [ -d "src/main/resources/styles" ]; then
    echo "Copying CSS from src/main/resources/styles"
    cp -r src/main/resources/styles/*.css temp_build/bin/styles/ 2>/dev/null
elif [ -d "src/styles" ]; then
    echo "Copying CSS from src/styles"
    cp -r src/styles/*.css temp_build/bin/styles/ 2>/dev/null
else
    echo "Warning: Could not find CSS files in expected locations"
    # Search for css files recursively under src
    echo "Searching for CSS files under src..."
    find src -name "*.css" -exec cp {} temp_build/bin/styles/ \; 2>/dev/null
fi

# Compile the project (excluding module-info.java)
echo "Compiling GreenCompostWaste..."

# Check which directory structure to use
if [ -d "src/main/java" ]; then
  echo "Using src/main/java structure"
  javac --add-modules java.sql \
        -d temp_build/bin -cp "$JAVAFX_CLASSPATH:$SQLITE_CLASSPATH" \
        $(find src/main/java -name "*.java" | grep -v module-info.java)
else
  echo "Using src structure"
  javac --add-modules java.sql \
        -d temp_build/bin -cp "$JAVAFX_CLASSPATH:$SQLITE_CLASSPATH" \
        $(find src -name "*.java" | grep -v -e module-info.java -e ".class")
fi

# Run the application
echo "Running GreenCompostWaste..."
java --module-path "$JAVAFX_HOME/lib" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql \
     -cp "temp_build/bin:$JAVAFX_CLASSPATH:$SQLITE_CLASSPATH" \
     --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED \
     --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED \
     com.greencompost.main.Main
