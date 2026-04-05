# BLOCKICK Architecture

This document provides a detailed technical overview of BLOCKICK's architecture, design patterns, and implementation details.

## 🏗️ Architecture Overview

BLOCKICK follows **Clean Architecture** principles with **MVVM** pattern, organized into three main layers:

```
┌─────────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                          │
│    (Jetpack Compose UI, ViewModels, Navigation)              │
├─────────────────────────────────────────────────────────────┤
│                    DOMAIN LAYER                              │
│      (Business Logic, DNS Engine, Blocking Rules)             │
├─────────────────────────────────────────────────────────────┤
│                      DATA LAYER                              │
│  (Room Database, DataStore, Repository, Network, Parsers)      │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Directory Structure

```
app/src/main/java/com/blockick/app/
├── data/                           # Data Layer
│   ├── db/                        # Room Database
│   │   ├── AppDatabase.kt        # Database definition
│   │   ├── dao/                  # Data Access Objects
│   │   │   ├── BlocklistDao.kt
│   │   │   ├── DomainDao.kt
│   │   │   ├── AllowlistDao.kt
│   │   │   └── QueryLogDao.kt
│   │   └── entities/             # Database entities
│   ├── preferences/              # DataStore
│   │   └── AppPreferences.kt    # User preferences
│   ├── repository/               # Repositories
│   │   └── BlocklistRepository.kt
│   └── parsers/                  # Blocklist parsers
│       ├── HostsParser.kt
│       ├── DomainParser.kt
│       ├── AdblockParser.kt
│       └── JsonParser.kt
│
├── domain/                        # Domain Layer
│   └── engine/                   # Core Business Logic
│       ├── DnsPacketProcessor.kt # DNS query processing
│       ├── BlocklistEngine.kt    # Domain matching
│       └── UpstreamResolver.kt   # DNS-over-HTTPS
│
├── ui/                           # Presentation Layer
│   ├── MainActivity.kt           # Main activity
│   ├── screens/                 # Screen composables
│   │   ├── home/
│   │   ├── filters/
│   │   ├── stats/
│   │   └── settings/
│   ├── components/             # Reusable UI components
│   ├── navigation/             # Navigation setup
│   ├── theme/                  # App theming
│   └── widget/                 # Home screen widget
│
├── vpn/                          # VPN Service
│   ├── LocalVpnService.kt      # Main VPN service
│   ├── VpnController.kt        # VPN state management
│   ├── VpnTileService.kt       # Quick settings tile
│   └── utils/                  # Packet utilities
│
├── worker/                        # Background Processing
│   ├── BlocklistUpdateWorker.kt # Periodic updates
│   └── WorkManagerScheduler.kt  # Work scheduling
│
├── receiver/                     # Broadcast Receivers
│   └── BootReceiver.kt          # Boot completed
│
├── di/                           # Dependency Injection
│   ├── DataModule.kt            # Data layer DI
│   └── NetworkModule.kt         # Network DI
│
└── util/                         # Utilities
    └── NotificationHelper.kt    # Notification management
```

## 🔄 Data Flow

### DNS Request Flow

```
User App Request
        ↓
   Local VPN Tunnel (LocalVpnService)
        ↓
   Packet Capture (IPv4/IPv6 UDP)
        ↓
   DnsPacketProcessor.processPacket()
        ↓
   ┌─────────────────────────────────┐
   │  1. Check Bypass Schedule       │
   │     (Skip filtering if active)  │
   └─────────────────────────────────┘
        ↓
   ┌─────────────────────────────────┐
   │  2. Check Safe Search          │
   │     (Redirect to safe search)   │
   └─────────────────────────────────┘
        ↓
   ┌─────────────────────────────────┐
   │  3. Check Blocklist Engine     │
   │     (Match against HashSet)     │
   └─────────────────────────────────┘
        ↓
   ┌─────────────────────────────────┐
   │  4. If blocked → NXDOMAIN      │
   │     If allowed → Upstream DNS  │
   └─────────────────────────────────┘
        ↓
   Log to QueryLogDao
        ↓
   Return Response
```

### Blocklist Update Flow

```
Scheduled Trigger (WorkManager)
        ↓
BlocklistUpdateWorker.doWork()
        ↓
Fetch URLs from BlocklistRepository
        ↓
Download Lists (OkHttp)
        ↓
Parse Lists (Hosts/Domain/Adblock Parser)
        ↓
Insert to Room Database (DomainDao)
        ↓
Update Statistics
        ↓
Reload BlocklistEngine
        ↓
Show Notification
```

## 🗄️ Database Schema

BLOCKICK uses **Room** database with 8 tables:

### 1. `blocklists` - Blocklist Metadata

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| name | TEXT | Display name |
| url | TEXT | Download URL |
| format | TEXT | hosts/domain/adblock/json |
| enabled | INTEGER | 1 = enabled |
| lastUpdated | INTEGER | Timestamp |
| entryCount | INTEGER | Number of rules |

### 2. `domains` - Parsed Domains

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| domain | TEXT | Domain name (indexed) |
| blocklistId | INTEGER | Source blocklist |
| isWildcard | INTEGER | 1 = wildcard match |

**Index:** `domain` (for fast lookups)

### 3. `allowlist` - User Whitelist

| Column | Type | Description |
|--------|------|-------------|
| domain | TEXT | Allowed domain (PK) |
| addedAt | INTEGER | Timestamp |

### 4. `user_blocklist` - User Blacklist

| Column | Type | Description |
|--------|------|-------------|
| domain | TEXT | Blocked domain (PK) |
| addedAt | INTEGER | Timestamp |

### 5. `query_logs` - Recent Queries

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| domain | TEXT | Queried domain |
| isBlocked | INTEGER | 1 = blocked |
| queryType | TEXT | DNS record type |
| timestamp | INTEGER | Time |

**Limit:** 1000 rows (auto-cleanup)

### 6. `daily_stats` - Statistics

| Column | Type | Description |
|--------|------|-------------|
| date | TEXT | YYYY-MM-DD (PK) |
| blockedCount | INTEGER | Blocked queries |
| totalCount | INTEGER | Total queries |

### 7. `excluded_apps` - App Exclusions

| Column | Type | Description |
|--------|------|-------------|
| packageName | TEXT | Package name (PK) |
| excludedAt | INTEGER | Timestamp |

### 8. `custom_blocklists` - Custom URLs

| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key |
| name | TEXT | Display name |
| url | TEXT | List URL |
| format | TEXT | hosts/domain/adblock |
| enabled | INTEGER | 1 = enabled |
| lastUpdated | INTEGER | Timestamp |

## ⚙️ Key Components

### 1. DnsPacketProcessor

**Location:** `domain/engine/DnsPacketProcessor.kt`

**Responsibilities:**
- Parse incoming DNS packets
- Extract domain from query
- Coordinate filtering logic
- Create DNS responses
- Log queries

**Flow:**
```kotlin
suspend fun processPacket(queryData: ByteArray): ByteArray? {
    // 1. Parse packet
    val query = Message(queryData)
    val domain = query.question.name
    
    // 2. Check bypass schedule
    if (isBypassActive()) {
        return upstreamResolver.resolve(queryData)
    }
    
    // 3. Check Safe Search
    val safeSearchTarget = getSafeSearchTarget(domain)
    if (safeSearchTarget != null) {
        return createSafeSearchResponse(safeSearchTarget)
    }
    
    // 4. Check blocklist
    if (blocklistEngine.shouldBlock(domain)) {
        return createNxDomainResponse()
    }
    
    // 5. Forward upstream
    return upstreamResolver.resolve(queryData)
}
```

### 2. BlocklistEngine

**Location:** `domain/engine/BlocklistEngine.kt`

**Algorithm:**
- Uses `HashSet<String>` for O(1) lookups
- Loads all domains from Room on initialization
- Checks exact match first
- Falls back to subdomain matching

**Performance:**
- 1M domains ≈ 150MB RAM
- Lookup time: <1ms
- Preloaded on VPN start

### 3. LocalVpnService

**Location:** `vpn/LocalVpnService.kt`

**VPN Configuration:**
```kotlin
Builder()
    .setSession("BLOCKICK")
    .addAddress("10.1.1.2", 32)      // IPv4
    .addAddress("fd00:1:1:1::2", 128) // IPv6
    .addDnsServer("10.1.1.1")         // VPN DNS
    .addRoute("0.0.0.0", 0)           // Route all
    .setMtu(1500)
```

**Packet Processing:**
- Captures UDP packets on port 53 (DNS)
- Parses IPv4/IPv6 headers
- Extracts DNS payload
- Processes through DnsPacketProcessor
- Returns response

### 4. UpstreamResolver

**Location:** `domain/engine/UpstreamResolver.kt`

**DNS-over-HTTPS Implementation:**
```kotlin
suspend fun resolve(queryBytes: ByteArray): ByteArray? {
    val provider = getDnsProvider() // Cloudflare/Google/Quad9
    val url = "https://${provider}/dns-query"
    
    val request = Request.Builder()
        .url(url)
        .post(queryBytes.toRequestBody("application/dns-message"))
        .build()
    
    return okHttpClient.execute(request)
}
```

## 🎨 UI Architecture

### Screen Composition

Each screen follows this pattern:

```kotlin
@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    // Collect state
    val state by viewModel.state.collectAsState()
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Event.ShowError -> showSnackbar(event.message)
                is Event.Navigate -> navigate(event.route)
            }
        }
    }
    
    // Render UI
    FeatureContent(
        state = state,
        onAction = viewModel::handleAction
    )
}
```

### Navigation

Uses Jetpack Navigation Compose:

```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Filters : Screen("filters")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
```

## 🔌 Dependency Injection

Uses **Hilt** for dependency injection:

### Modules

**DataModule.kt** - Database and preferences
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideDatabase(): AppDatabase {
        return Room.databaseBuilder(...)
            .build()
    }
    
    @Provides
    fun provideBlocklistDao(db: AppDatabase): BlocklistDao {
        return db.blocklistDao()
    }
}
```

**NetworkModule.kt** - Network clients
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .build()
    }
}
```

## 🔄 Background Processing

### WorkManager Configuration

**Periodic Updates:**
```kotlin
val updateRequest = PeriodicWorkRequestBuilder<BlocklistUpdateWorker>(
    frequency, TimeUnit.DAYS
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    )
    .build()

WorkManager.enqueueUniquePeriodicWork(
    "blocklist_update",
    ExistingPeriodicWorkPolicy.KEEP,
    updateRequest
)
```

**Constraints:**
- Network: Required (WiFi recommended)
- Battery: Not low
- Retry: Exponential backoff

## 🛡️ Security

### Network Security

**network_security_config.xml:**
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**Implications:**
- No HTTP traffic
- Only HTTPS for blocklist downloads
- System CA certificates trusted

### DNS Privacy

**Upstream Providers:**
- All use DNS-over-HTTPS (DoH)
- Encrypted queries
- No plaintext DNS

### Local Processing

- All filtering on-device
- No external servers
- No analytics or tracking
- Open source for transparency

## 📊 Performance Optimizations

### Database

**Indexes:**
```kotlin
@Dao
interface DomainDao {
    @Query("SELECT * FROM domains WHERE domain = :domain")
    fun findByDomain(domain: String): Domain
    
    @Query("SELECT * FROM domains WHERE domain LIKE :pattern")
    fun findSubdomains(pattern: String): List<Domain>
}
```

**Batch Operations:**
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertAll(domains: List<Domain>) // Bulk insert
```

### Memory

**Domain Storage:**
- HashSet for O(1) lookups
- Lazy loading on demand
- Automatic cleanup of old logs

**Compose:**
- Stable keys for lists
- `remember` for expensive operations
- `derivedStateOf` for computed values

## 🧪 Testing Strategy

### Unit Tests

**Location:** `app/src/test/java/`

**Test Categories:**
- Parser tests (Hosts, Adblock, etc.)
- Engine tests (blocking logic)
- Repository tests (data operations)
- ViewModel tests (state management)

### Integration Tests

**Location:** `app/src/androidTest/java/`

**Coverage:**
- Database operations
- VPN service interactions
- UI component tests

## 🔧 Build Configuration

### Version Catalog

**Location:** `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.1.0"
compose = "2024.12.01"
hilt = "2.54"
room = "2.6.1"

[libraries]
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
hilt-android = { group = "com.google.dagger", name = "hilt-android" }
```

### Build Variants

- **Debug**: Development builds, logging enabled
- **Release**: Optimized, minified, ProGuard enabled

## 📱 Platform Support

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

**Features by API Level:**
- API 26+: Full functionality
- API 29+: Enhanced battery optimizations
- API 33+: Improved notification permissions

## 🔄 Future Enhancements

Potential improvements for future versions:

1. **IPv6-only networks** - Enhanced support
2. **Custom DNS protocols** - DNS-over-TLS, DNSCrypt
3. **Statistics export** - CSV/JSON export
4. **Backup/Restore** - Configuration backup
5. **Widgets** - Multiple widget types
6. **Automation** - Tasker integration

---

For more information, see:
- [User Guide](USER_GUIDE.md) - Feature documentation
- [Contributing](CONTRIBUTING.md) - Development guidelines
- [GitHub Issues](https://github.com/YOUR_USERNAME/BLOCKICK/issues) - Known issues and features
