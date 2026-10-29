# Thaumaturge — Fabric 26.3 port workspace

This directory is an isolated Fabric/Loom bootstrap workspace for the Minecraft 26.3 port. The existing root project remains the original NeoForge build until the port is migrated and validated.

## Requirements
- JDK 25
- Internet access for Gradle dependency resolution

## Build the bootstrap
From the repository root on Windows:

```powershell
.\gradlew.bat -p fabric-port build
```

Or enter this directory and invoke the wrapper in the parent directory:

```powershell
..\gradlew.bat build
```

The output JAR is written to `fabric-port/build/libs/`.

## Current scope
This is only the loader/build bootstrap, not a complete port of Thaumaturge. The main Thaumaturge sources still depend heavily on NeoForge (mod constructor/event buses, registries, attachments, configs, Curios, Lithostitched, worldgen and many other APIs). Those systems must be migrated incrementally before the original mod can be enabled on Fabric. Do not install this bootstrap JAR expecting the full mod content to work.
