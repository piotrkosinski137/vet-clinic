# .claude Directory

This directory contains documentation for Claude Code to provide consistent and accurate assistance with this project.

## Documentation Files

### DEVELOPMENT.md
**Comprehensive development guide** covering:
- Complete local development setup
- Java and Maven configuration on Windows
- Docker services setup
- Running tests and the backend
- Authentication flow with Keycloak
- Code quality tools
- Common development workflows
- Troubleshooting guide

**Use this when**: You need detailed information about any aspect of development setup or workflow.

### QUICK_REFERENCE.md
**Quick command reference** with:
- Copy-paste ready commands for common tasks
- Environment setup shortcuts
- Maven, Docker, and authentication commands
- Key paths and ports reference

**Use this when**: You need to quickly execute a common task without reading detailed explanations.

### WINDOWS_SPECIFICS.md
**Windows-specific configuration** including:
- Path conventions (Windows vs Git Bash)
- Java and Maven on Windows
- Process management commands
- Docker Desktop on Windows
- Line endings and Git configuration
- Common Windows-specific issues and solutions

**Use this when**: You encounter Windows-specific issues or need to understand path/command differences.

### PROJECT_STRUCTURE.md
**Project architecture and structure** covering:
- Technology stack details
- Directory structure and module organization
- Database schema
- API endpoints
- Security and authentication flow
- Configuration profiles
- Testing strategy
- Build lifecycle

**Use this when**: You need to understand the project architecture, locate specific files, or understand how components interact.

### COMMAND_TEMPLATES.md
**Ready-to-use command templates** including:
- Environment variable setup
- Maven command templates with placeholders
- Build, test, and run commands
- Docker service management
- API testing with curl
- Database operations
- Useful bash aliases

**Use this when**: You need a complete, copy-paste ready command for any development task.

### TROUBLESHOOTING.md
**Comprehensive troubleshooting guide** covering:
- Maven and build issues
- Database connection problems
- Keycloak authentication issues
- Port conflicts
- Docker problems
- Windows-specific solutions
- Performance issues
- Preventive measures

**Use this when**: Something isn't working and you need to diagnose and fix the problem.

### settings.local.json
**Claude Code permissions** - Auto-managed file containing approved command patterns for this project.

## Quick Start

New to this project? Read in this order:

1. **PROJECT_STRUCTURE.md** - Understand what this project is and how it's organized
2. **DEVELOPMENT.md** - Set up your development environment
3. **COMMAND_TEMPLATES.md** or **QUICK_REFERENCE.md** - Bookmark for daily use

## Common Tasks

### I want to run the backend
See: **COMMAND_TEMPLATES.md** > "Run Application Commands" or **QUICK_REFERENCE.md** > "Run Backend"

### I want to run tests
See: **COMMAND_TEMPLATES.md** > "Test Commands" or **QUICK_REFERENCE.md** > "Common Maven Commands"

### I need a specific command template
See: **COMMAND_TEMPLATES.md** - Complete templates with placeholders for all common tasks

### I want to understand the project structure
See: **PROJECT_STRUCTURE.md** > "Directory Structure"

### I'm having Windows-specific issues
See: **WINDOWS_SPECIFICS.md** > "Common Issues and Solutions"

### Something isn't working
See: **TROUBLESHOOTING.md** - Comprehensive solutions for common problems

### I need detailed setup information
See: **DEVELOPMENT.md** > Complete development guide

## Key Information at a Glance

**Java Version**: 21
**Java Location**: `C:\Users\piotr\.jdks\ms-21.0.7`

**Maven Location**: `/c/Users/piotr/.m2/wrapper/dists/apache-maven-3.9.2-bin/5aq6rqcntpmkk4aam7p0t6i219/apache-maven-3.9.2/bin/mvn`

**Project Root**: `C:\Users\piotr\IdeaProjects\drag-n-drop\klinikaxp`

**Backend POM**: `C:/Users/piotr/IdeaProjects/drag-n-drop/klinikaxp/backend/pom.xml`

**Ports**:
- Backend: 8080
- PostgreSQL: 5432
- Keycloak: 8180

**Profiles**:
- `-Pfast`: Skip quality checks (development)
- `-Pci`: Run all quality checks (CI)

**Test Credentials**:
- Username: `user`
- Password: `user`

## Maintaining These Docs

These documentation files should be updated when:
- Development setup changes (new tools, versions, paths)
- New modules or features are added
- Build process changes
- New profiles or configurations are added
- Common issues and solutions are discovered

Keep the docs accurate and up-to-date to ensure Claude Code provides the best assistance.
