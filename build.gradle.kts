plugins {
    id("java")
    // Apply the Shadow plugin for creating fat JARs
    id("com.gradleup.shadow") version "8.3.0"
    // Apply the Run-Paper plugin for running Paper Minecraft servers
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

val javaVersion: String by project
val junitVersion: String by project
val paperApiVersion: String by project
val hikariCpVersion: String by project
val mineCoreLibVersion: String by project
val vaultApiVersion: String by project
val placeholderApiVersion: String by project
val spiGuiVersion: String by project
val projectPackageName = "${project.group}.openkits"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        name = "placeholderApi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")
    compileOnly("com.github.MilkBowl:VaultAPI:${vaultApiVersion}") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("me.clip:placeholderapi:${placeholderApiVersion}")
    implementation("com.samjakob:SpiGUI:${spiGuiVersion}")
    implementation("com.zaxxer:HikariCP:${hikariCpVersion}")
    implementation(files("libs/MineCoreLib-${mineCoreLibVersion}.jar"))
}

// Disable the default JAR task
tasks.jar {
    enabled = false
}

// Configure the Shadow JAR task
tasks.shadowJar {
    archiveClassifier.set("") // Set the classifier for the JAR
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot" // Add custom manifest attributes
    }
    // Relocate packages to avoid conflicts
    relocate("me.clip", "${projectPackageName}.shadow.placeholderapi")
    relocate("com.samjakob.spigui", "${projectPackageName}.shadow.spigui")
    relocate("com.zaxxer.hikari", "${projectPackageName}.shadow.hikari")
    relocate("org.slf4j", "${projectPackageName}.shadow.slf4j")
}

// Ensure the Shadow JAR task runs during the build process
tasks.build {
    dependsOn(tasks.shadowJar)
}

// Configure additional tasks
tasks {
    // Configure the RunServer task for running a Paper server
    named<xyz.jpenilla.runpaper.task.RunServer>("runServer") {
        minecraftVersion("1.21") // Specify the Minecraft version
    }

    // Configure Java compilation settings
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8" // Set the file encoding
        val javaVersionInt = javaVersion.toInt()
        if (javaVersionInt >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(javaVersionInt) // Set the release version for Java
        }
    }

    // Process resources and expand placeholders in `plugin.yml`
    processResources {
        val props = mapOf("version" to project.version.toString()) // Define properties for resource filtering
        inputs.properties(props)
        filteringCharset = "UTF-8" // Set the charset for filtering
        filesMatching("plugin.yml") {
            expand(props) // Replace placeholders in `plugin.yml`
        }
    }
}