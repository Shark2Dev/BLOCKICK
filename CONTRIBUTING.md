# Contributing to BLOCKICK

Thank you for your interest in contributing to BLOCKICK! This document provides guidelines and instructions for contributing to this project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Pull Request Process](#pull-request-process)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing](#testing)
- [Reporting Issues](#reporting-issues)

## 📜 Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](.github/CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to [your email here].

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have:

- **Android Studio Ladybug (2024.2+)** or newer
- **JDK 17** (verify with `java -version`)
- **Android SDK** with Compile SDK 35
- **Git** installed on your system
- **Gradle 8.11.1** (included via wrapper)

### Repository Structure

```
BLOCKICK/
├── app/src/main/java/com/blockick/app/
│   ├── data/           # Data layer (repositories, database, preferences)
│   ├── domain/         # Business logic (DNS engine, blocking)
│   ├── ui/            # Presentation layer (screens, components)
│   ├── vpn/           # VPN service implementation
│   ├── worker/        # Background workers (WorkManager)
│   └── di/            # Dependency injection modules
├── docs/              # Documentation
└── gradle/            # Gradle wrapper
```

## 🛠️ Development Setup

### 1. Fork and Clone

```bash
# Fork on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/BLOCKICK.git
cd BLOCKICK
```

### 2. Open in Android Studio

1. File → Open
2. Select the `BLOCKICK` folder
3. Wait for Gradle sync to complete
4. Accept any prompts to update Gradle or dependencies

### 3. Verify Build

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

If all commands succeed, your setup is complete!

## 🔄 Making Changes

### Branch Naming Convention

Use descriptive branch names:

```
feature/add-new-filter-format
fix/blocklist-parsing-error
enhancement/improve-widget-ui
docs/update-readme
```

Avoid generic names like `fix`, `update`, or `test`.

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(dns): add support for DNS-over-HTTPS
fix(blocklist): handle malformed hosts file
docs(readme): add installation instructions
refactor(engine): improve domain lookup performance
```

### Commit Frequency

- Make small, focused commits
- Commit early and often
- Each commit should be self-contained

## 📤 Pull Request Process

### Before Submitting

1. **Update your branch**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run all checks**
   ```bash
   ./gradlew test
   ./gradlew lint
   ./gradlew assembleDebug
   ```

3. **Verify tests pass**
   - All existing tests must pass
   - Add tests for new features
   - Update tests for changed features

### Creating the Pull Request

1. **Push your branch**
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Open Pull Request**
   - Go to the repository on GitHub
   - Click "New Pull Request"
   - Select your branch
   - Fill in the PR template

3. **PR Template Checklist**
   ```markdown
   ## Description
   [Brief description of changes]

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Manual testing performed
   - [ ] No existing tests broken

   ## Screenshots (if UI change)
   [Before/After screenshots]

   ## Additional Notes
   [Any additional information]
   ```

### PR Review Process

- PRs require approval from maintainers
- Address review comments promptly
- Keep PRs focused - one feature or fix per PR
- Be responsive to feedback

## 📏 Code Style Guidelines

### Kotlin Conventions

Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

**Formatting:**
- Indentation: 4 spaces (no tabs)
- Line length: 120 characters max
- No unnecessary blank lines
- Use `Kotlin` style imports

**Naming:**
- Classes: `PascalCase` (e.g., `BlocklistEngine`)
- Functions: `camelCase` (e.g., `processPacket`)
- Constants: `SCREAMING_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
- Variables: `camelCase` (e.g., `upstreamDns`)

**Documentation:**
- Document public APIs with KDoc
- Use meaningful names
- Avoid abbreviations (except well-known ones)

**Example:**
```kotlin
/**
 * Processes DNS packets and applies blocking rules.
 *
 * @param packetData Raw DNS packet bytes
 * @return Processed DNS response, or null if invalid
 */
suspend fun processPacket(packetData: ByteArray): ByteArray? {
    // Implementation
}
```

### Jetpack Compose Guidelines

**Component Organization:**
```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    FeatureContent(
        state = state,
        onAction = viewModel::handleAction
    )
}

@Composable
private fun FeatureContent(
    state: FeatureState,
    onAction: (FeatureAction) -> Unit
) {
    // Implementation
}
```

**State Management:**
- Use `StateFlow` for UI state
- Use `SharedFlow` for one-time events
- Keep state immutable
- Use data classes for state

### Performance Guidelines

**Avoid:**
- Unnecessary recomposition
- Creating objects in `Composable` functions
- Heavy operations on main thread

**Prefer:**
- `remember` for expensive computations
- `derivedStateOf` for derived values
- `LaunchedEffect` for coroutines
- Lazy loading for lists

## 🧪 Testing

### Unit Tests

**Location:** `app/src/test/java/`

**Naming:** `ClassNameTest.kt`

**Example:**
```kotlin
class BlocklistEngineTest {
    
    @Test
    fun `should block domain in blocklist`() {
        val engine = BlocklistEngine(blockedDomains)
        
        assertTrue(engine.shouldBlock("ads.example.com"))
    }
    
    @Test
    fun `should allow domain not in blocklist`() {
        val engine = BlocklistEngine(blockedDomains)
        
        assertFalse(engine.shouldBlock("safe.example.com"))
    }
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.blockick.app.BlocklistEngineTest"

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### UI Testing

For Compose UI tests, use `androidTest`:

```kotlin
@ComposeTest
fun `displays correct count`() {
    composeTestRule.setContent {
        StatCard(count = 42)
    }
    
    composeTestRule.onNodeWithText("42").assertExists()
}
```

## 🐛 Reporting Issues

### Before Creating an Issue

1. **Search existing issues** - avoid duplicates
2. **Verify with latest version** - check if issue still exists
3. **Gather information:**
   - Android version
   - Device model
   - App version
   - Steps to reproduce
   - Expected vs actual behavior

### Issue Template

```markdown
## Bug Description
[Clear description of the bug]

## Steps to Reproduce
1. [First step]
2. [Second step]
3. [Third step]

## Expected Behavior
[What should happen]

## Actual Behavior
[What actually happens]

## Environment
- Android Version: [e.g., Android 14]
- Device: [e.g., Pixel 7]
- App Version: [e.g., v0.1.0]
- Build: [e.g., Debug/Release]

## Logs
```
[Include relevant logcat output here]
```

## Additional Context
[Any other relevant information]
```

## 🎯 Areas Needing Contribution

Looking for ideas? Here are areas that need work:

- [ ] Additional blocklist parsers
- [ ] More DNS providers
- [ ] Enhanced statistics and charts
- [ ] Export/import configuration
- [ ] Widget improvements
- [ ] Internationalization (i18n)
- [ ] Additional language support

Check the [Issues](https://github.com/YOUR_USERNAME/BLOCKICK/issues) page for more!

## 📝 License

By contributing to BLOCKICK, you agree that your contributions will be licensed under the Apache License 2.0.

## 🙏 Thank You!

Every contribution, no matter how small, helps make BLOCKICK better for everyone. Thank you for taking the time to contribute!

---

**Questions?** Feel free to open an issue or reach out to the maintainers.
