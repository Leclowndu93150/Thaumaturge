import com.diffplug.spotless.LineEnding
import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.external.javadoc.CoreJavadocOptions
import org.gradle.jvm.tasks.Jar

plugins {
    id("dev.prism")
    id("com.diffplug.spotless") version "8.9.0"
}

fun prop(name: String): String = providers.gradleProperty(name).get()

val minecraftVersion = prop("minecraft_version")

group = prop("mod_group_id")
version = prop("mod_version")

repositories {
    mavenCentral()
}

spotless {
    lineEndings = LineEnding.UNIX
    java {
        target(fileTree("src") { include("**/*.java") })
        palantirJavaFormat("2.96.0")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

prism {
    curseMaven()
    modrinthMaven()
    maven("BlameJared", "https://maven.blamejared.com/")
    maven("IllusiveSoulworks", "https://maven.theillusivec4.top/")
    maven("FTB", "https://maven.ftb.dev/releases")

    metadata {
        modId = prop("mod_id")
        name = prop("mod_name")
        description = prop("mod_description")
        license = prop("mod_license")
        prop("mod_authors").split(",").forEach { author(it.trim()) }
        expand("minecraft_version_range", prop("minecraft_version_range"))
        expand("neoforge_version_range", prop("neo_version_range"))
        expand("neoforge_loader_version_range", prop("loader_version_range"))
    }

    version(minecraftVersion) {
        parchmentMinecraftVersion = prop("parchment_minecraft_version")
        parchmentMappingsVersion = prop("parchment_mappings_version")

        neoforge {
            loaderVersion = prop("neo_version")

            dependencies {
                implementation("maven.modrinth:lithostitched:1.8.0-neoforge-21.1")

                compileOnly("mezz.jei:jei-$minecraftVersion-common-api:${prop("jei_version")}")
                compileOnly("mezz.jei:jei-$minecraftVersion-neoforge-api:${prop("jei_version")}")
                compileOnly("mezz.jei:jei-$minecraftVersion-neoforge:${prop("jei_version")}")
                runtimeOnly("mezz.jei:jei-$minecraftVersion-neoforge:${prop("jei_version")}")

                compileOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion:api")
                runtimeOnly("top.theillusivec4.curios:curios-neoforge:${prop("curios_version")}+$minecraftVersion")

                compileOnly("maven.modrinth:jade:${prop("jade_version")}")
                runtimeOnly("maven.modrinth:jade:${prop("jade_version")}")

                compileOnly("maven.modrinth:distanthorizons:${prop("distant_horizons_version")}")
                compileOnly("maven.modrinth:vdjF5PL5:${prop("dynamic_trees_version")}")
                compileOnly("maven.modrinth:iris:${prop("iris_version")}")
            }

            rawProject {
                val sourceSets = extensions.getByType<SourceSetContainer>()
                val main = sourceSets.getByName("main")
                extensions.configure<JavaPluginExtension> {
                    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
                    withJavadocJar()
                }
                tasks.withType<Javadoc>().configureEach {
                    (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
                    isFailOnError = false
                }
                tasks.withType<JavaCompile>().configureEach {
                    options.compilerArgs.addAll(listOf("-Xmaxerrs", "2000"))
                }
                dependencies {
                    add("compileOnly", "org.jspecify:jspecify:1.0.0")
                    add("compileOnly", "dev.ftb.mods:ftb-library-neoforge:${prop("ftb_library_version")}") {
                        (this as ExternalModuleDependency).isTransitive = false
                    }
                    add("testImplementation", platform("org.junit:junit-bom:5.11.4"))
                    add("testImplementation", "org.junit.jupiter:junit-jupiter")
                    add("testImplementation", "org.mockito:mockito-core:5.15.2")
                    add("testImplementation", "mezz.jei:jei-$minecraftVersion-common-api:${prop("jei_version")}")
                    add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
                }
                tasks.withType<Test>().configureEach {
                    useJUnitPlatform()
                    systemProperty("thaumaturge.testResourceRoot", file("src/main/resources").absolutePath)
                }
                extensions.configure<NeoForgeExtension> {
                    unitTest {
                        enable()
                        testedMod.set(mods.getByName(prop("mod_id")))
                    }
                    runs.named("client") { gameDirectory.set(file("run")) }
                    runs.named("server") {
                        gameDirectory.set(file("run"))
                        programArgument("--nogui")
                    }
                    runs.create("gameTestServer") {
                        type.set("gameTestServer")
                        gameDirectory.set(file("run-gametest"))
                    }
                    runs.configureEach {
                        systemProperty("neoforge.enabledGameTestNamespaces", prop("mod_id"))
                        systemProperty("forge.logging.markers", "REGISTRIES")
                        logLevel.set(org.slf4j.event.Level.DEBUG)
                    }
                }
                val syncDynamicTreesTreePack = tasks.register<Sync>("syncDynamicTreesTreePack") {
                    from(main.resources) {
                        include("trees/**")
                        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
                    }
                    includeEmptyDirs = false
                    into(layout.buildDirectory.dir("classes/java/main/trees"))
                    mustRunAfter(tasks.named("compileJava"))
                }
                tasks.matching { it.name in listOf("runClient", "runServer", "runGameTestServer") }.configureEach {
                    dependsOn(syncDynamicTreesTreePack)
                }
                val removeDynamicTreesTreePackForJar = tasks.register<Delete>("removeDynamicTreesTreePackForJar") {
                    delete(layout.buildDirectory.dir("classes/java/main/trees"))
                }
                tasks.named<Jar>("jar") {
                    dependsOn(removeDynamicTreesTreePackForJar)
                    exclude("com/leclowndu93150/thaumaturge/debug/**")
                }
            }
        }
    }

    publishing {
        changelogFile = "changelog.md"

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = prop("curseforge_id")
        }

        github {
            repository = prop("github_repo")
            tagName = "${prop("mod_version")}-$minecraftVersion"
        }

        maven {
            name = "Leclown"
            url = prop("leclown_maven_url")
            credentialsFromEnv("MAVEN_USER", "MAVEN_PASS")
        }

        dependencies {
            requires("curios")
            requires("lithostitched")
            optional("jei")
        }
    }
}
