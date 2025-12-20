# Windows-Specific Configuration

This document covers Windows-specific quirks and configurations for this development environment.

## Path Conventions

### Git Bash vs Windows Paths

When using Git Bash on Windows, paths can be represented in multiple ways:

**Windows Style**:
```
C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp
```

**Git Bash (Unix-style)**:
```
/c/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp
```

**Maven -f flag** (requires Windows-style with forward slashes):
```
C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml
```

## Java on Windows

### Java Installation Location

```
C:\Users\piotr\.jdks\ms-21.0.7
```

In Git Bash:
```
/c/Users/piotr/.jdks/ms-21.0.7
```

### Setting JAVA_HOME in Different Shells

**Git Bash**:
```bash
export JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7"
```

**PowerShell**:
```powershell
$env:JAVA_HOME = "C:\Users\piotr\.jdks\ms-21.0.7"
```

**CMD**:
```cmd
set JAVA_HOME=C:\Users\piotr\.jdks\ms-21.0.7
```

## Maven Wrapper

### Location

```
C:\Users\piotr\.m2\wrapper\dists\apache-maven-3.9.2-bin\5aq6rqcntpmkk4aam7p0t6i219\apache-maven-3.9.2\bin\
```

Contains:
- `mvn` (Unix shell script)
- `mvn.cmd` (Windows batch file)

### Using Maven on Windows

**Git Bash** (use Unix script):
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" \
  /c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn \
  clean compile -f "C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml"
```

**CMD** (use .cmd file):
```cmd
set JAVA_HOME=C:\Users\piotr\.jdks\ms-21.0.7
C:\Users\piotr\.m2\wrapper\dists\apache-maven-3.9.2-bin\5aq6rqcntpmkk4aam7p0t6i219\apache-maven-3.9.2\bin\mvn.cmd clean compile -f C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp\backend\pom.xml
```

**PowerShell** (use .cmd file):
```powershell
$env:JAVA_HOME = "C:\Users\piotr\.jdks\ms-21.0.7"
& "C:\Users\piotr\.m2\wrapper\dists\apache-maven-3.9.2-bin\5aq6rqcntpmkk4aam7p0t6i219\apache-maven-3.9.2\bin\mvn.cmd" clean compile -f "C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp\backend\pom.xml"
```

## Process Management on Windows

### Find Process Using a Port

```bash
# Find process on port 8080
netstat -ano | findstr :8080
```

Output example:
```
TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       12345
```

The last number (12345) is the PID.

### Kill a Process

**Using taskkill**:
```bash
taskkill /PID 12345 /F
```

**Kill all Java processes** (PowerShell):
```powershell
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
```

**Kill all Java processes** (CMD):
```cmd
taskkill /F /IM java.exe
```

## Docker on Windows

### Docker Desktop

Ensure Docker Desktop is running before executing any Docker commands.

Check Docker status:
```bash
docker ps
```

### Docker Compose

Docker Compose comes bundled with Docker Desktop on Windows.

```bash
# Check version
docker-compose --version
```

### Volume Mounts

When using volume mounts in docker-compose.yml on Windows, Docker Desktop handles path translation automatically:

```yaml
volumes:
  - ./docker/keycloak:/opt/keycloak/data/import
```

This works the same on Windows and Unix systems.

## Line Endings

### Git Configuration

Recommended Git configuration for Windows:

```bash
# Check current setting
git config --global core.autocrlf

# Set to true for Windows (recommended)
git config --global core.autocrlf true
```

This ensures:
- Files are checked out with CRLF (Windows style)
- Files are committed with LF (Unix style)

### IDE Configuration

For IntelliJ IDEA:
- File > Settings > Editor > Code Style
- Line separator: Unix and macOS (\n)

## Common Issues and Solutions

### Issue: Maven Command Not Found

**Problem**: `mvn: command not found`

**Solution**: Use the full path to Maven wrapper:
```bash
/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn
```

### Issue: Java Version Mismatch

**Problem**: Wrong Java version being used

**Solution**: Always set JAVA_HOME explicitly:
```bash
JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7" mvn clean compile
```

### Issue: Permission Denied on Scripts

**Problem**: Cannot execute shell scripts

**Solution**: Use Git Bash or WSL, or use .cmd/.bat equivalents on Windows CMD/PowerShell.

### Issue: Port Already in Use

**Problem**: `Port 8080 is already in use`

**Solution**:
```bash
# Find the process
netstat -ano | findstr :8080

# Kill it
taskkill /PID <PID> /F
```

### Issue: Docker Not Starting

**Problem**: Docker commands fail with connection errors

**Solution**:
1. Ensure Docker Desktop is running
2. Check Docker Desktop status in system tray
3. Restart Docker Desktop if needed

### Issue: Testcontainers Fails

**Problem**: Integration tests fail to start containers

**Solution**:
1. Ensure Docker Desktop is running
2. Check Docker has enough resources (Memory/CPU)
3. Check Docker is accessible from the command line:
   ```bash
   docker ps
   ```

## Windows Terminal Tips

### Copy/Paste in Git Bash

- **Copy**: Ctrl + Insert or select text and right-click
- **Paste**: Shift + Insert or right-click

### Windows Terminal Configuration

If using Windows Terminal, recommended settings:

```json
{
  "profiles": {
    "defaults": {
      "fontFace": "Cascadia Mono",
      "fontSize": 10,
      "colorScheme": "One Half Dark"
    },
    "list": [
      {
        "name": "Git Bash",
        "commandline": "C:\\Program Files\\Git\\bin\\bash.exe",
        "icon": "C:\\Program Files\\Git\\mingw64\\share\\git\\git-for-windows.ico",
        "startingDirectory": "%USERPROFILE%"
      }
    ]
  }
}
```

## Environment Variables Reference

### User Environment Variables

These are set at the user level in Windows:

- `JAVA_HOME`: Not set by default, must be set manually or in shell
- `PATH`: Includes various Windows system directories

### Session Environment Variables

Set these in your shell session:

```bash
# Git Bash
export JAVA_HOME="/c/Users/piotr/.jdks/ms-21.0.7"
export PATH="$JAVA_HOME/bin:$PATH"
```

## File System Considerations

### Case Sensitivity

Windows file system is case-insensitive by default:
- `MyFile.java` = `myfile.java` = `MYFILE.JAVA`

This can cause issues when working with Git repositories that have case-sensitive file names.

### Path Length Limits

Windows has a 260-character path length limit (MAX_PATH).

If you encounter path too long errors:
1. Move project closer to root (e.g., `C:\Projects\klinikaxp`)
2. Enable long paths in Windows 10+:
   ```
   Computer Configuration > Administrative Templates > System > Filesystem > Enable Win32 long paths
   ```

## Recommended Tools

### Git Bash

Installed with Git for Windows, provides Unix-like shell on Windows.

Location: `C:\Program Files\Git\bin\bash.exe`

### Windows Terminal

Modern terminal emulator from Microsoft, supports multiple shells.

Install from Microsoft Store or winget:
```
winget install Microsoft.WindowsTerminal
```

### Docker Desktop

Required for running Docker containers on Windows.

Download: https://www.docker.com/products/docker-desktop

## Quick Command Reference

### Navigating in Git Bash

```bash
# Home directory
cd ~

# Project directory
cd /c/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp

# Windows C: drive root
cd /c/
```

### Converting Paths

```bash
# Windows to Git Bash
C:\Users\piotr\file.txt  →  /c/Users/piotr/file.txt

# Git Bash to Windows
/c/Users/piotr/file.txt  →  C:\Users\piotr\file.txt

# Maven -f flag (hybrid)
C:/Users/piotr/file.txt
```
