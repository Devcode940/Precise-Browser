# EAST WEB LITE 🧭✨

**EAST WEB LITE** is a professional, high-performance, and feature-complete mobile web browser tailored for Android. Built from the ground up using **Kotlin**, **Jetpack Compose**, and **Material Design 3 (M3)**, EAST WEB LITE combines lightweight, swift browsing pipelines with powerful productivity portals, such as a secure Password Vault, a Multi-Threaded Chunked Download Manager, a built-in PDF Viewer, and a comprehensive **AI Chat Hub**.

---

## 🎨 Creative Identity & Brand Design

- **App Name**: `EAST WEB LITE`
- **Visual Logo Philosophy**: An elegant, modernized abstract monogram of the letter **E** swept towards the east like a swift arrow, transitioning dynamically into a glossy cyan and deep blue digital wind rose. The design represents forward alignment, navigation, and state-of-the-art tech.
- **Aesthetic Direction**: Highly colorful, tactile, and immersive dark visual themes with generous negative space, active Material Design 3 ripple effects, elegant card frames, customized iconography, and crisp responsive styling.

---

## 🚀 Key Architectural Features

### 1. Multi-Tab Management Engine 🗂️
* Fully responsive navigation bar supporting live previews, dynamic creation, switching, and closing of multiple parallel browser instances.
* Live tracking of active session tab histories, title retrieval, and background session states.
* Supports customized simulation behaviors, such as **Incognito Mode** (private sandboxed sessions that avoid caching history or site state).

### 2. AI Chat Explorer & Workspace Hub ✨
* Quick-access launcher dialog for modern virtual assistants, accessed via the toolbar's sparkle icon (`Icons.Default.AutoAwesome`).
* Supported pre-configured AI providers:
  - **Google Gemini** (Deep reasoning, writing companion & Google workspace tools)
  - **OpenAI ChatGPT** (Industry leader in general messaging & memory features)
  - **Anthropic Claude** (Articulate precision, writing, and intensive software engineering)
  - **DeepSeek Chat** (Speedy deep reasoning and logic processing with DeepSeek-R1 logic engines)
  - **Microsoft Copilot** (Enterprise productivity and grounded search citations)
  - **Perplexity AI** (Source-cited modern search engines providing concise reports)
  - **HuggingChat** (Community-sourced open-weights sandbox ecosystem)
* Smart search capabilities allowing instant catalog searches, fast filtering via multi-select categories (e.g. *Multimodal*, *Research*, *General*, *Logic & Code*, *Open Source*), and direct quick actions to load the AI assistant either **in the current tab** or **in a new tab**.

### 3. Customizable Search Pattern Engine 🔍
* Dynamic routing of address-bar queries. Offers an advanced **Default Query URL Pattern** text controller matching exact custom query formats.
* Custom provider formats can use `%s` as a search-query wildcard (e.g., `https://myCustomSearchEngine.com/q=%s`).
* Integrated search provider presets automatically updated via the settings selection dropdown, including *DuckDuckGo*, *Google*, *Bing*, *Yahoo*, *Brave*, *Ecosia*, *Yandex*, and *Baidu*.

### 4. Advanced Security & Productivity Suites 🔒
* **Password Vault Screen**: Secure client-side credential manager protecting user accounts with local encryption capabilities and fast-autofill queries.
* **Integrated Download Manager**: A robust utility that displays raw chunked download streams, folder-style downloaded file browsers, and direct download states.
* **Embedded PDF Viewer Screen**: Seamless inline document reader resolving PDF streams locally via specialized document interfaces (`lite://pdf?url=...`).
* **Bookmarks & History Screens**: Interactive cataloging systems with custom folder labeling, category icons, search indexing, and bulk cleaning utilities.

---

## 🛠️ Technological Stack & Libraries

- **Programming Language**: 100% Modern [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for fully declarative, responsive, and tactile layouts.
- **Dependency & Design System**: [Material Design 3 (M3)](https://m3.material.io/) including fluid component guidelines, adaptive `WindowInsets` handling, dynamic ColorSchemes (including dark/light dynamic variations), and `Elevation` spacing.
- **State Management**: Model-View-ViewModel (MVVM) utilizing Android `ViewModel`, Kotlin Coroutines, and strict reactive `StateFlow` structures for error/loading states.
- **Integration Capabilities**:
  - `Chromium WebView` integrations for high-speed page parsing and script execution.
  - Direct local configurations for simulating varying *User Agent* signatures (providing compatibility for desktop-mode and legacy configurations).

---

## 📁 Project Directory Layout

```
.
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── example
│   │   │   │           ├── MainActivity.kt        # Primary host activity controlling the app container, root dialogs, toolbar & view routing
│   │   │   │           ├── ui
│   │   │   │           │   ├── screens
│   │   │   │           │   │   └── BrowserScreens.kt # Implementation of NewTab, Bookmarks, History, Downloads, PDF, Settings Screens, and AI shortcuts grid
│   │   │   │           │   └── theme
│   │   │   │           │       └── Theme.kt       # Color scheme configuration supporting Light Mode, Dark Mode, and Incognito styling
│   │   │   │           └── viewmodel
│   │   │   │               └── BrowserViewModel.kt # Reactive UI State, tab management, bookmarks actions, download pipelines, and settings persistence
│   │   │   └── res
│   │   │       ├── drawable
│   │   │       │   ├── ic_launcher_background.xml # Advanced linear-gradient vector background
│   │   │       │   ├── ic_launcher_foreground.xml # Central adaptive foreground visual resource containing the generated app wind-rose branding
│   │   │       │   └── img_app_icon_1781904840815.jpg # HIGH-QUALITY brand image generated for EAST WEB LITE app iconography
│   │   │       └── values
│   │   │           └── strings.xml                # Unified text resource referencing "EAST WEB LITE"
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── example
│   │                   └── ExampleRobolectricTest.kt # Robolectric unit tests checking critical flows
├── metadata.json                                  # AI Studio Platform Metadata synchronized with app_name resource
└── build.gradle.kts                               # Kotlin DSL build configurations for dependencies
```

---

## ⚙️ Configuration & Execution Guide

### Prerequisities & Environment
1. **JDK Version**: Java 17+
2. **Android SDK Level**: API 26 (Android 8.0 Oreo) as minimum, target SDK 34 (Android 14).
3. **Gradle System**: Gradle Kotlin DSL.

### Building and Linting
To compile and build the Android application package locally or through the build-chain, execute:
```bash
gradle assembleDebug
```

To run standard unit tests and verify application components:
```bash
gradle :app:testDebugUnitTest
```

---

## ✨ Summary of UX Best Practices Used

- **Accessibiltity first**: Every visual asset, descriptive badge, active button, and search input fields include contextual `contentDescription` mappings to support TalkBack and standard screen reader assistance.
- **Standardized Touch targets**: All active elements, including custom back/forward navigators, tabs, password reveal tools, and AI launcher actions support a minimum touch area footprint of **48dp x 48dp** to facilitate seamless mobile touch feedback.
- **Clean State Boundaries**: No fake or dead-end elements. The Password Vault, History log, custom Search pattern editor, and AI workspace list represent live functional services tied directly to ViewModel reactive flows.
- **Responsive Sizing**: Explicit fluid layout guidelines avoiding hardcoded container widths. Custom flow layers (`FlowRowLayout`) are designed dynamically to wrap shortcut badges gracefully across compact, medium, and wide landscape devices.
