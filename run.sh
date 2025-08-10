#!/bin/bash

# This script runs the GreenCompostWaste application with JavaFX

# JavaFX version to use
JAVAFX_VERSION="21.0.2"

# Check if JavaFX SDK path is set
if [ -z "$JAVAFX_HOME" ]; then
  # Use the bundled JavaFX SDK
  if [ -d "$(pwd)/javafx/javafx-sdk-${JAVAFX_VERSION}" ]; then
    echo "Using the bundled JavaFX SDK"
    export JAVAFX_HOME="$(pwd)/javafx/javafx-sdk-${JAVAFX_VERSION}"
  else
    echo "ERROR: JavaFX SDK not found at $(pwd)/javafx/javafx-sdk-${JAVAFX_VERSION}"
    echo "Please run the script from the project root directory."
    exit 1
  fi
fi

echo "Using JavaFX from: $JAVAFX_HOME"

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
javac --module-path $JAVAFX_HOME/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -d bin $(find src -name "*.java")

# Run the application
echo "Running GreenCompostWaste..."
java --module-path $JAVAFX_HOME/lib:bin --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -m GreenCompostWaste/com.greencompost.main.Main
