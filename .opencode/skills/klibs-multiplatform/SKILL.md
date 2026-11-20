---
name: klibs Multiplatform
description: Use the official klibs.io MCP server to check the latest stable Kotlin and Compose Multiplatform versions, audit dependencies, and pick KMP libraries that support the required targets. Load before adding, upgrading, or troubleshooting any dependency.
---

# klibs.io MCP — Kotlin Multiplatform dependency intelligence

The project connects to the official JetBrains klibs.io MCP server:

- Endpoint: `https://api.klibs.io/mcp` (Streamable HTTP, no authentication)
- Configuration: `opencode.jsonc` → `mcp.servers.klibs`
- Upstream documentation: `https://github.com/JetBrains/klibs-io/blob/master/integrations/mcp/README.md`

## Tools

| Tool | Purpose |
| --- | --- |
| `searchProjects` | Search the KMP catalog by keywords, platform, and target filters. Returns project packages with `latestVersion` and `latestStableVersion`. |
| `getLatestVersion` | Return `latestVersion` and `latestStableVersion` for one `<groupId>:<artifactId>`. |

In Code Mode call them as `tools.klibs.searchProjects(...)` and
`tools.klibs.getLatestVersion(...)`. Outside Code Mode they appear as
`klibs_searchProjects` and `klibs_getLatestVersion`.

## When to use this skill

1. Before adding any dependency to `shared/build.gradle.kts` or `androidApp/build.gradle.kts`.
2. Before bumping a version in `gradle/libs.versions.toml`.
3. When a library does not resolve, has no iOS/Android artifact, or a transitive conflict appears.
4. When the user asks for "the latest stable version" or a dependency audit.
5. When choosing between a JetBrains (`org.jetbrains.*`) and an AndroidX artifact for KMP.

## Workflow

1. Read `gradle/libs.versions.toml` and the relevant `build.gradle.kts`.
2. For every candidate package, call `getLatestVersion` with the exact `groupId` and `artifactId`.
3. Prefer `latestStableVersion`. Never propose `alpha`, `beta`, `rc`, `dev`, or `-compat` builds unless the user explicitly asks for them.
4. For a new library, call `searchProjects` with `platforms` such as `["common","androidJvm","ios"]` and, when needed, `targetGroupFilters` (for example `[{"JVM":[]},{"IOS":[]}]`) to confirm the published targets.
5. Check compatibility with the project baseline: Kotlin `2.4.21`, Compose Multiplatform `1.12.2`, AGP `9.2.1`, Gradle `9.4.1`, `compileSdk 37`, `minSdk 24`, JVM target 21.
6. Apply the minimum safe change, then validate with Gradle (see below).
7. Record the result in `docs/dependency-audit.md` when the change is a version bump.

## Project conventions

- Versions live only in `gradle/libs.versions.toml`; do not hardcode coordinates in module build files.
- Prefer `libs.*` accessors over string coordinates.
- The `shared` module targets Android plus `iosX64`, `iosArm64`, and `iosSimulatorArm64`; every `commonMain` dependency must publish those targets.
- Keep Gradle Wrapper at `9.4.1` unless the user asks to change it. AGP `9.2.1` is the newest stable AGP compatible with that Gradle version.
- `kotlinx-datetime` stays on the `0.6.x` API line; `0.7+` contains breaking changes and is not a drop-in upgrade.

## Validation commands

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --stacktrace
.\gradlew.bat :androidApp:assembleDebug --stacktrace
```

Only the first command is required for a dependency-only change unless Android resources or the widget are affected.

## Failure handling

- If the MCP server is unreachable, fall back to Maven Central / Google Maven metadata and say that the MCP result was unavailable.
- If `packageFound` is `false`, the coordinate is wrong or the library is not indexed; verify `groupId`/`artifactId` before concluding it does not exist.
- If `latestStableVersion` is `null`, the package has no stable release; keep the current version and report the fact instead of guessing.
