# How to Run Main.java

## Quick Start

### ⭐ Option 1: Using Cursor/VS Code (Easiest - Recommended!)

**Method A: Using Run Button (Simplest)**
1. Open `code/Main.java` in Cursor
2. Click the **▶ Run** button above the `main` method
3. Or press `Ctrl+F5` (Run without debugging)

**Method B: Using Debug Panel**
1. Press `F5` or click "Run and Debug" in the sidebar
2. Select **"Run Main"** from the dropdown
3. Click the green play button

**Note:** A `launch.json` file has been created in `.vscode/` folder with configurations for:
- Run Main (game menu)
- Run NumberGame (direct)
- Run OneCardGame (direct)

**Why this is best:** Cursor automatically handles Maven dependencies (including JavaFX) - no manual classpath configuration needed!

---

### Option 2: Using Maven Exec Plugin

**Note:** Requires Maven in your PATH. If `mvn` command doesn't work, use Option 1 instead!

```bash
# Compile and run
mvn compile exec:java
```

This will:
1. Compile all source files
2. Run the `Main` class
3. Display the game menu

---

### Option 3: Using PowerShell Script (Easy Command-Line Alternative)

A PowerShell script `compile-and-run.ps1` has been created in the project root. Simply run:

```powershell
.\compile-and-run.ps1
```

This script will:
1. Check if JavaFX dependencies are downloaded
2. Compile all Java files with JavaFX module path
3. Run `Main` if compilation succeeds

**Note:** Requires JavaFX to be downloaded first (run `mvn dependency:resolve` or use Cursor's Run button once to download dependencies).

---

### Option 4: Using Java Command Directly (PowerShell - Not Recommended)

**⚠️ Warning:** This is complex and error-prone. **Use Option 1 (Cursor Run button) instead!**

If you must use command line, run this **entire block** in PowerShell:

```powershell
# Set JavaFX paths (run all these together)
$javafxPath = "$env:USERPROFILE\.m2\repository\org\openjfx"
$javafxControls = "$javafxPath\javafx-controls\17.0.2\javafx-controls-17.0.2.jar"
$javafxGraphics = "$javafxPath\javafx-graphics\17.0.2\javafx-graphics-17.0.2.jar"
$javafxBase = "$javafxPath\javafx-base\17.0.2\javafx-base-17.0.2.jar"
$javafxControlsWin = "$javafxPath\javafx-controls\17.0.2\javafx-controls-17.0.2-win.jar"
$javafxGraphicsWin = "$javafxPath\javafx-graphics\17.0.2\javafx-graphics-17.0.2-win.jar"
$javafxBaseWin = "$javafxPath\javafx-base\17.0.2\javafx-base-17.0.2-win.jar"
$javafxModulePath = "$javafxPath\javafx-controls\17.0.2;$javafxPath\javafx-graphics\17.0.2;$javafxPath\javafx-base\17.0.2"
$classpath = "$javafxControls;$javafxGraphics;$javafxBase;$javafxControlsWin;$javafxGraphicsWin;$javafxBaseWin"

# Compile (must use --module-path for JavaFX)
cd code
javac --module-path $javafxModulePath --add-modules javafx.controls -cp $classpath *.java numbergame/*.java onecardgame/*.java onecardgame/cards/*.java wordgame/*.java

# Run (if compilation succeeded)
if ($LASTEXITCODE -eq 0) {
    java --module-path $javafxModulePath --add-modules javafx.controls -cp ".;$classpath" Main
}
cd ..
```

**Why this is hard:** JavaFX requires module system configuration, not just classpath. Cursor handles this automatically!

---

### Option 3: Using Java Command Directly (PowerShell - Advanced)

## What Main.java Does

The `Main` class provides a **game menu** where you can choose:

- **W** - Word Game (Trivia game)
- **N** - Number Game (JavaFX GUI game)
- **M** - One Card Game (Card game)
- **Q** - Quit

### Example Usage:

```
Welcome to the game land!!

Press W : Word game
Press N : Number game
Press M : One card game
Press Q : Quit the game
Enter your choice: W
```

---

## Troubleshooting

### JavaFX Module Path Error

If you get errors like:
```
Error: JavaFX runtime components are missing
```

**Solution:** Make sure JavaFX is in your classpath. The Maven dependency should handle this, but if running directly with `java`, you need:

```bash
java --module-path <path-to-javafx> --add-modules javafx.controls Main
```

### Cannot Find Main Class

**Solution:** Make sure you've compiled first:
```bash
mvn clean compile
```

### Input/Output Issues

- The games use `System.in` for input, so make sure your terminal supports interactive input
- Word Game reads from `./src` directory - make sure those files exist

### JavaFX Compilation Errors (package javafx.* does not exist)

If you see errors like:
```
error: package javafx.application does not exist
error: package javafx.scene does not exist
```

**This happens when:** You try to compile directly with `javac Main.java` without JavaFX in the module path. When you compile `Main.java`, it also needs to compile `NumberGame.java` which uses JavaFX.

**These are NOT code bugs - your code is correct!** This is a compilation configuration issue.

**Solutions:**
1. **Best:** Use Cursor's Run button (Option 1) - it handles dependencies automatically!
2. **Easy:** Use the PowerShell script (Option 3) - `.\compile-and-run.ps1`
3. **Alternative:** Use Maven (Option 2) if you have `mvn` in your PATH
4. **Last resort:** Use the manual PowerShell commands in Option 4 above (complex and error-prone)

---

## Recommended: Use Cursor's Run Button!

The easiest way is to simply click the **▶ Run** button in Cursor above the `main` method. Cursor's Java extension automatically:
- Detects Maven dependencies
- Configures JavaFX module path
- Sets up the classpath
- Compiles and runs your code

No command-line needed!
