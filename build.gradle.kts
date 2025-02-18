plugins {
    id("java")
}

group = "io.github.tavstal"
version = "1.0.0"


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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation("com.samjakob:SpiGUI:1.3.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.zaxxer:HikariCP:4.0.3")
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