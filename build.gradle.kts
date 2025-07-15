plugins {
    id("java")
}

val javaVersion: String by project
val junitVersion: String by project
val paperApiVersion: String by project
val hikariCpVersion: String by project
val mineCoreLibVersion: String by project
val vaultApiVersion: String by project
val placeholderApiVersion: String by project
val spiGuiVersion: String by project

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
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:${paperApiVersion}")
    compileOnly("com.github.MilkBowl:VaultAPI:${vaultApiVersion}") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("me.clip:placeholderapi:${placeholderApiVersion}")
    implementation("com.samjakob:SpiGUI:${spiGuiVersion}")
    implementation("com.zaxxer:HikariCP:${hikariCpVersion}")
    implementation(files("libs/MineCoreLib-${mineCoreLibVersion}.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("mojangJar") {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

    // Set .jar name
    archiveBaseName.set("openkits-mojang")

    // Set the duplicates strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Collect runtime classpath files
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })

    // Optionally, include the compiled classes
    from(sourceSets.main.get().output)
}

tasks.register<Jar>("spigotJar") {
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }

    // Set .jar name
    archiveBaseName.set("openkits-spigot")

    // Set the duplicates strategy
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // Collect runtime classpath files
    from({
        configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) }
    })

    // Optionally, include the compiled classes
    from(sourceSets.main.get().output)
}

tasks.register("buildJars") {
    dependsOn("mojangJar", "spigotJar")
}

tasks.named("build") {
    dependsOn("processResources")
    dependsOn("buildJars")
}

tasks.named<Jar>("jar") {
    enabled = false
}