import org.gradle.kotlin.dsl.version

plugins {
    id("dev.kikugie.stonecutter")

    id("fabric-loom") version "1.16-SNAPSHOT" apply false
}
stonecutter active "1.21.8"