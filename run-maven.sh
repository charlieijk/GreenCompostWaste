#!/bin/bash

# This script runs the GreenCompostWaste application using the Maven-built shaded JAR

# JavaFX version to use
JAVAFX_VERSION="21.0.2"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Build the project first
echo "Building project with Maven..."
mvn clean package -q

if [ $? -ne 0 ]; then
    echo "Maven build failed!"
    exit 1
fi

. "$SCRIPT_DIR/scripts/javafx-env.sh"
ensure_javafx_home "$JAVAFX_VERSION" "$SCRIPT_DIR" || exit 1

echo "Running GreenCompostWaste application..."

# Check if we're in a terminal (not GUI environment)
if [ -z "$DISPLAY" ] && [ "$(uname)" = "Darwin" ]; then
    echo "Note: Running in headless mode - GUI may not display properly"
fi

# Run with multiple fallback graphics options for macOS compatibility
java -Djava.awt.headless=false \
     -Djavafx.platform=desktop \
     -Djava.library.path="$JAVAFX_HOME/lib" \
     --module-path "$JAVAFX_HOME/lib" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
     --add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED \
     --add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED \
     --add-opens=javafx.base/javafx.event=ALL-UNNAMED \
     -jar target/GreenCompostWaste-1.0-SNAPSHOT.jar
