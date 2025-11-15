#!/bin/bash

# Finds a JavaFX SDK directory that matches the requested version under the project javaFX folder.
# Arguments:
#   $1 - JavaFX version
#   $2 - Project root
find_bundled_javafx() {
    local javafx_version="$1"
    local project_root="$2"
    local javafx_root="${project_root}/javafx"

    if [ ! -d "$javafx_root" ]; then
        return 1
    fi

    local candidate
    for candidate in "${javafx_root}/javafx-sdk-${javafx_version}"*; do
        if [ ! -e "$candidate" ]; then
            continue
        fi
        if [ -d "${candidate}/lib" ]; then
            echo "$candidate"
            return 0
        fi
    done

    return 1
}

# Ensures that JAVAFX_HOME points to a JavaFX SDK compatible with the current platform.
# Arguments:
#   $1 - JavaFX version (used for resolving the bundled SDK directory)
#   $2 - Project root (where the bundled SDK might live)
ensure_javafx_home() {
    local javafx_version="$1"
    local project_root="$2"

    if [ -n "$JAVAFX_HOME" ] && [ -d "$JAVAFX_HOME/lib" ]; then
        echo "Using JavaFX from: $JAVAFX_HOME"
        return 0
    elif [ -n "$JAVAFX_HOME" ]; then
        echo "Warning: JAVAFX_HOME is set to '$JAVAFX_HOME' but the 'lib' directory was not found. Ignoring it." >&2
    fi

    local os_name
    os_name="$(uname -s)"
    local java_arch
    java_arch="$(java -XshowSettings:properties -version 2>&1 | grep -m 1 'os.arch' | awk '{print $3}')"
    if [ -z "$java_arch" ]; then
        java_arch="$(uname -m)"
    fi

    local bundled_dir=""
    bundled_dir="$(find_bundled_javafx "$javafx_version" "$project_root" 2>/dev/null)"

    local bundled_native=""
    if [ -n "$bundled_dir" ]; then
        local native_candidate
        for native_candidate in libprism_sw.dylib libprism_es2.dylib libprism_sw.so libprism_es2.so; do
            if [ -f "${bundled_dir}/lib/${native_candidate}" ]; then
                bundled_native="${bundled_dir}/lib/${native_candidate}"
                break
            fi
        done
    fi

    local apple_silicon="false"
    if [ "$os_name" = "Darwin" ] && { [ "$java_arch" = "aarch64" ] || [ "$java_arch" = "arm64" ]; }; then
        apple_silicon="true"
    fi

    local bundled_supports_arm="false"
    if [ -n "$bundled_native" ]; then
        if command -v file >/dev/null 2>&1 && file "$bundled_native" | grep -q 'arm64'; then
            bundled_supports_arm="true"
        elif [ "$apple_silicon" != "true" ]; then
            bundled_supports_arm="true"
        fi
    fi

    local can_use_bundled="false"
    if [ -n "$bundled_dir" ] && [ -d "${bundled_dir}/lib" ]; then
        can_use_bundled="true"
    fi

    if [ "$can_use_bundled" = "true" ]; then
        if [ "$apple_silicon" = "true" ] && [ "$bundled_supports_arm" = "false" ]; then
            echo "Bundled JavaFX SDK is Intel-only and cannot be used with the current Apple Silicon runtime ($java_arch)." >&2
        elif [ -d "${bundled_dir}/lib" ]; then
            export JAVAFX_HOME="$bundled_dir"
            echo "Using the bundled JavaFX SDK from $JAVAFX_HOME"
            return 0
        fi
    fi

    if command -v brew >/dev/null 2>&1; then
        local brew_prefix
        brew_prefix="$(brew --prefix openjfx 2>/dev/null)"
        if [ -n "$brew_prefix" ] && [ -d "$brew_prefix/libexec/lib" ]; then
            export JAVAFX_HOME="$brew_prefix/libexec"
            echo "Using JavaFX from Homebrew: $JAVAFX_HOME"
            return 0
        fi
    fi

    if [ "$apple_silicon" = "true" ]; then
        echo "ERROR: Apple Silicon runtime detected ($java_arch) but no compatible JavaFX SDK was found." >&2
        echo "Download the arm64 JavaFX SDK from https://gluonhq.com/products/javafx/, unzip it," >&2
        echo "and set JAVAFX_HOME to the extracted directory (e.g. JAVAFX_HOME=/path/to/javafx-sdk-${javafx_version}-aarch64)" >&2
        echo "or place it under ${project_root}/javafx/ so the scripts can find it automatically." >&2
    else
        echo "ERROR: Could not locate a JavaFX SDK. Set JAVAFX_HOME or place it in ${project_root}/javafx/." >&2
    fi
    return 1
}
