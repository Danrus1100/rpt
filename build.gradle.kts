plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("maven-publish")
    id("java")
}

fun opt(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

fun prop(name: String) : String {
    return findProperty(name)?.toString() ?: throw IllegalArgumentException("Missing property: $name")
}

fun propExists(name: String) = project.properties.containsKey(name)

val mainBranch = "multiversion"
val minecraft = property("deps.mc") as String

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.parchmentmc.org")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.shlakoblock.com/releases")
}

loom {
    accessWidenerPath = project.file("src/main/resources/rpt.accesswidener")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

stonecutter{
    replacements {
        string {
            direction = eval(current.version, ">=1.21.11")
            replace(
                "import net.minecraft.client.model.ArmorStandModel;",
                "import net.minecraft.client.model.object.armorstand.ArmorStandModel;"
            )
            replace(
                "import net.minecraft.client.model.PlayerModel;",
                "import net.minecraft.client.model.player.PlayerModel;"
            )
            replace(
                "import net.minecraft.client.renderer.RenderType;",
                "import net.minecraft.client.renderer.rendertype.RenderType;"
            )
        }

        string {
            direction = eval(current.version, ">=1.21.11")
            replace("ResourceLocation", "Identifier")
        }

        string {
            direction = eval(current.version, ">=1.21.11")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
        }

        string {
            direction = eval(current.version, ">=1.21.10")
            replace("PlayerRenderState", "AvatarRenderState")
        }
    }
}

dependencies {
    fun implementationAndInclude(dependencyNotation: Any) {
        implementation(dependencyNotation)
        include(dependencyNotation)
    }

    minecraft("com.mojang:minecraft:${prop("deps.mc")}")
    mappings(loom.layered() {
        officialMojangMappings()
        opt("deps.parchment") {
            parchment("org.parchmentmc.data:parchment-${prop("deps.mc")}:${it}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fapi")}")
    modImplementation("com.danrus:rpf:${prop("deps.rpf")}-${prop("deps.mc")}") {
        exclude(group = "maven.modrinth", module = "my_totem_doll")
    }

    opt("deps.iris") {
        modImplementation("maven.modrinth:sodium:${prop("deps.sodium")}")
        modImplementation("maven.modrinth:iris:${it}")

        implementation("org.antlr:antlr4-runtime:4.13.1")
        implementation("io.github.douira:glsl-transformer:3.0.0-pre3")
        implementation("org.anarres:jcpp:1.4.14")
    }

    implementationAndInclude("com.ezylang:EvalEx:3.6.0")
    implementationAndInclude("com.danrus:bb4j:1.1-SNAPSHOT")
}

tasks.processResources {
    inputs.property("id", findProperty("mod.id"))
    inputs.property("name", findProperty("mod.name"))
    inputs.property("version", findProperty("mod.version"))
    inputs.property("mcdep", findProperty("mod.mcdep"))
    inputs.property("minecraft_version", findProperty("deps.mc"))
    inputs.property("description", findProperty("mod.description"))
    inputs.property("author", findProperty("mod.author"))

    val map = mapOf(
        "id" to findProperty("mod.id"),
        "name" to findProperty("mod.name"),
        "version" to findProperty("mod.version"),
        "mcdep" to findProperty("mod.mcdep"),
        "minecraft_version" to findProperty("deps.mc"),
        "description" to findProperty("mod.description"),
        "author" to findProperty("mod.author")
    )

    filesMatching("fabric.mod.json") { expand(map) }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

base {
    archivesName.set(findProperty("mod.id") as String)
}

val artifactVersion = "${prop("mod.version")}-${minecraft}"

publishMods {
    val modrinthToken = findProperty("modrinth-token")
    val curseforgeToken = findProperty("curseforge-token")
    val discordWebhookDR = findProperty("discord-webhook")
    val discordWebhookDRRU = findProperty("discord-webhook-dr-ru")
    val discordWebhookDry = findProperty("discord-webhook-dry")

    dryRun = false

    type = STABLE

    file.set(tasks.named("remapJar").flatMap { (it as org.gradle.jvm.tasks.Jar).archiveFile })

    changelog = rootProject.file("CHANGELOG.md").readText()

    val loaders = prop("pub.target.platforms").split(' ')
    loaders.forEach(modLoaders::add)
    displayName = "PRT ${prop("mod.version")} for ${minecraft}"
    version = artifactVersion

    val targets = prop("pub.target.versions").split(' ')
    modrinth {
        projectId = prop("publish.modrinth")
        accessToken = modrinthToken.toString()
        targets.forEach(minecraftVersions::add)
    }

    curseforge {
        projectId = prop("publish.curseforge")
        accessToken = curseforgeToken.toString()
        projectSlug = prop("pub.slug")
        targets.forEach(minecraftVersions::add)
    }

    if (targets.contains("1.21.8") && loaders.contains("fabric")) {
        discord ("DR freak mods anonuncement") {
            webhookUrl = discordWebhookDR.toString()
            dryRunWebhookUrl = discordWebhookDry.toString()

            username  = prop("mod.name")
            avatarUrl = "https://github.com/Danrus1100/rpt/blob/master/src/main/resources/assets/rpt/icon.png?raw=true"

            content = changelog.map{ "# " + prop("mod.version") + " version here! \n\n" + rootProject.file("CHANGELOG.md").readText() +"\n\n<@&1426901890582581248>" }
        }
        discord ("DR freak mods anonuncement (RU)") {
            webhookUrl = discordWebhookDRRU.toString()
            dryRunWebhookUrl = discordWebhookDry.toString()

            username  = prop("mod.name")
            avatarUrl = "https://github.com/Danrus1100/rpt/blob/master/src/main/resources/assets/rpf/icon.png?raw=true"

            content = changelog.map{ "# Версия " + prop("mod.version") + " вышла! \n\n" + rootProject.file("CHANGELOG_RU.md").readText() +"\n\n<@&1426901890582581248>" }
        }
    }
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            groupId = "com.danrus"
            artifactId = "rpt"
            version = artifactVersion

            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            pom {
                name.set("rpt")
            }
        }
    }

    repositories {
        maven {
            name = "ShlakoblockReleases"
            url = uri("https://maven.shlakoblock.com/releases")

            credentials {
                username = project.findProperty("shlakoblock-maven-username")?.toString()
                password = project.findProperty("shlakoblock-maven-password")?.toString()
            }
        }

        maven {
            name = "ShlakoblockSnapshots"
            url = uri("https://maven.shlakoblock.com/snapshots")

            credentials {
                username = project.findProperty("shlakoblock-maven-username")?.toString()
                password = project.findProperty("shlakoblock-maven-password")?.toString()
            }
        }
    }
}


stonecutter {
//    constants {
//        "rprenames" to propExists("deps.rprenames")
//    }
    val isRpRenames = propExists("deps.rprenames")
    constants["rprenames"] = isRpRenames
}

version = findProperty("mod.version") as String + "-" +findProperty("deps.mc") as String
