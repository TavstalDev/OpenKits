plugins {
    id("java")
}

group = "io.github.tavstal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "jitpack"
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
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("com.zaxxer:HikariCP:4.0.3")
}

tasks.test {
    useJUnitPlatform()
}