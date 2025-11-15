# GreenCompostWaste - Fixes Summary

This document summarizes the fixes applied to resolve the errors in the GreenCompostWaste application.

## Database Issues

1. **Missing 'remember_me' column**:
   - Added the missing column to the users table:
   ```sql
   ALTER TABLE users ADD COLUMN remember_me INTEGER DEFAULT 0
   ```

2. **Auto-commit mode errors**:
   - Fixed the transaction handling in `DatabaseManager.java`
   - Modified the rollback mechanism to remove the unnecessary auto-commit check before rollback

## FXML Resources

1. **StatsView.fxml stylesheet reference**:
   - Added the missing stylesheet reference:
   ```xml
   stylesheets="@../styles/styles.css"
   ```

## CSS Styling

1. **Missing CSS variables**:
   - Added the missing `-gc-info` CSS variable:
   ```css
   -gc-info: #1976d2;
   ```

## Running the Application

To run the application, you need JavaFX installed. Here are the steps to install and run it:

1. **Install JavaFX using Homebrew**:
   ```bash
   brew install openjfx
   ```

2. **Set the JAVAFX_HOME environment variable**:
   ```bash
   export JAVAFX_HOME="$(brew --prefix openjfx)/libexec"
   ```

3. **Run the application**:
   ```bash
   cd /Users/charlie/GreenCompostWaste
   sh run.sh
   ```

Alternatively, you can download and use JavaFX from the official website:
1. Download JavaFX SDK from [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
2. Extract it to a directory
3. Set the JAVAFX_HOME environment variable to point to the extracted location
4. Run the application

> **Apple Silicon note:** The bundled JavaFX SDK in this repository only contains Intel binaries. If you're using an Apple Silicon Mac, install the arm64 build via `brew install openjfx` (or download the aarch64 JavaFX SDK) and set `JAVAFX_HOME` accordingly so the launch scripts can find a compatible toolkit.

## Configuration for Future Use

To permanently configure JavaFX on your system, add the following to your `~/.zshrc` or `~/.bashrc` file:

```bash
export JAVAFX_HOME="/path/to/javafx-sdk"
```

Then restart your terminal or run `source ~/.zshrc` (or `source ~/.bashrc`) to apply the changes.
