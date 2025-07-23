plugins {
    java
}

group = "ru.SocialMoods"
version = "2.1.0"

repositories {
    mavenCentral()
    maven("https://repo.lanink.cn/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.nukkitx.com/snapshot")
}

dependencies {
    compileOnly("cn.nukkit:Nukkit:MOT-SNAPSHOT")
    compileOnly("com.github.MEFRREEX:FormConstructor:3.1.0")
    compileOnly("me.onebone:economyapi:2.0.2")
}

tasks {
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
    
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
} 