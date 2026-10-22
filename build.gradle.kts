import dev.isxander.mtk.manifests.spec.ModManifestSpec.DependencyType
import dev.isxander.mtk.manifests.spec.ModManifestSpec.Side
import dev.isxander.mtk.multiloader.utils.ngRuns
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("net.fabricmc.fabric-loom") version "1.18.4" apply false
    id("net.neoforged.gradle.userdev") version "7.1.39" apply false

    id("dev.isxander.mtk.multiloader") version "0.1.11"
    id("dev.isxander.mtk.manifests") version "0.1.5"
    id("dev.isxander.mtk.accessx") version "0.1.1"

    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    id("maven-publish")
}

fun prop(name: String): String =
    findProperty(name)?.toString() ?: throw IllegalArgumentException("Missing property: $name")

val minecraftVersion = prop("deps.mc")
val artifactVersion = "${prop("mod.version")}-$minecraftVersion"

group = "com.danrus.rpt"
version = artifactVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    // RPF is installed to mavenLocal from ../rpf. Its published Gradle metadata uses
    // loader capabilities instead of the default module capability, which Loom's
    // attribute-less resolve configurations cannot match, so consume the jars directly.
    mavenLocal {
        content { includeGroup("com.danrus") }
        metadataSources { artifact() }
    }
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases")
    maven("https://api.modrinth.com/maven")
}

// One canonical access-widener file. Fabric uses it directly, accessx derives
// the NeoForge access transformer from it.
val accessWidenerFile = layout.projectDirectory.file("accesswidener/rpt.accesswidener")

sourceSets.fabric.get().resources.srcDir("accesswidener")

val neoforgeAccessTransformer = accessx.convert("neoforge", sourceSets.neoforge.get()) {
    inputFiles.from(accessWidenerFile)
    outputFormat = accessx.AT
}

loom {
    accessWidenerPath.set(accessWidenerFile)
}

accessTransformers {
    file(neoforgeAccessTransformer.flatMap { it.outputFile })
}

// NeoGradle needs an explicit dependency from the AT-applying task to the accessx output.
tasks.matching { it.name == "neoFormApplyAccessTransformer" }.configureEach {
    dependsOn(neoforgeAccessTransformer)
}

ngRuns {
    configureEach {
        if (name == "neoforgeClient") {
            programArguments(
                "--username", "Dev",
                "--uuid", "00000000000000000000000000000000",
                "--accessToken", "0"
            )
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    fabricLoader("net.fabricmc:fabric-loader:${prop("deps.fabric")}")
    neoforgeImplementation("net.neoforged:neoforge:${prop("deps.neoforge")}")

    fabricImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fapi")}")

    // RPF's API is compiled against the common jar and provided by the RPF mod at runtime.
    // The loader jars carry the mod manifests, so dev runs load RPF itself.
    commonCompileOnly("com.danrus:rpf:${prop("deps.rpf")}-$minecraftVersion")
    fabricImplementation("com.danrus:rpf:${prop("deps.rpf")}-$minecraftVersion:fabric@jar")
    neoforgeImplementation("com.danrus:rpf:${prop("deps.rpf")}-$minecraftVersion:neoforge@jar")

    commonImplementation("com.ezylang:EvalEx:${prop("deps.evalex")}")
    commonInclude("com.ezylang:EvalEx:${prop("deps.evalex")}")
}

manifests {
    val common = manifest {
        modId = prop("mod.id")
        version = prop("mod.version")
        displayName = prop("mod.name")
        description = prop("mod.description")
        authors = listOf(prop("mod.author"))
        homepage = "https://danrus1100.github.io/rtp-rpf-docs/"
        sourcesUrl = "https://github.com/Danrus1100/rpt"
        issueTrackerUrl = "https://github.com/Danrus1100/rpt/issues"
        licenses = listOf("MIT")
        iconPath = "assets/rpt/icon.png"

        dependency("minecraft", DependencyType.REQUIRED, "[$minecraftVersion,26.4)")
        dependency("rpf", DependencyType.REQUIRED, "[${prop("deps.rpf")},)")

        mixin("rpt.mixins.json", Side.CLIENT)
    }

    fabricModJson(sourceSets.fabric.get()) {
        from(common)

        client()
        entrypoint("client", "com.danrus.rpt.fabric.RptFabric")
        accessWidener("rpt.accesswidener")

        dependency("fabricloader", DependencyType.REQUIRED, "[${prop("deps.fabric")},)")
        dependency("fabric-api", DependencyType.REQUIRED, "[0.160.0,)")
    }

    neoForgeModsToml(sourceSets.neoforge.get()) {
        from(common)

        modLoader = "javafml"
        loaderVersion = "[4,)"
        accessTransformer(neoforgeAccessTransformer)

        dependency("neoforge", DependencyType.REQUIRED, "[${prop("deps.neoforge")},)")
    }
}

// modstitch-manifests 0.1.5 has no displayTest option. RPT is client-only,
// so NeoForge must not require it on the server.
tasks.named("generateNeoforgeNeoForgeModsToml") {
    doLast {
        outputs.files.files
            .filter { it.name == "neoforge.mods.toml" }
            .forEach { file ->
                val text = file.readText()
                if (!text.contains("displayTest")) {
                    file.writeText(
                        text.replace(Regex("""mods = \[\{.*}]""")) { match ->
                            match.value.removeSuffix("}]") + ", displayTest = 'IGNORE_ALL_VERSION'}]"
                        }
                    )
                }
            }
    }
}

publishMods {
    val modrinthToken = findProperty("modrinth-token")
    val curseforgeToken = findProperty("curseforge-token")
    val discordWebhookDR = findProperty("discord-webhook")
    val discordWebhookDRRU = findProperty("discord-webhook-dr-ru")
    val discordWebhookDry = findProperty("discord-webhook-dry")

    dryRun = false

    type = BETA

    file.set(tasks.named<Jar>("universalJar").flatMap { it.archiveFile })
    additionalFiles.from(tasks.named<Jar>("universalSourcesJar").flatMap { it.archiveFile })

    changelog = rootProject.file("CHANGELOG.md").readText()

    val loaders = prop("pub.target.platforms").split(' ')
    loaders.forEach(modLoaders::add)
    displayName = "RPT ${prop("mod.version")} for $minecraftVersion"
    version = artifactVersion

    val targets = prop("pub.target.versions").split(' ')
    modrinth {
        projectId = prop("publish.modrinth")
        accessToken = modrinthToken.toString()
        environment = ModrinthEnvironment.CLIENT_ONLY
        targets.forEach(minecraftVersions::add)
        requires("rpf")
    }

    curseforge {
        projectId = prop("publish.curseforge")
        accessToken = curseforgeToken.toString()
        projectSlug = prop("pub.slug")
        client = true
        server = false
        targets.forEach(minecraftVersions::add)
        requires("rpf")
    }

    discord("DR freak mods announcement") {
        webhookUrl = discordWebhookDR.toString()
        dryRunWebhookUrl = discordWebhookDry.toString()

        username = prop("mod.name")
        avatarUrl = "https://github.com/Danrus1100/rpt/blob/master/src/main/resources/assets/rpt/icon.png?raw=true"

        content = changelog.map {
            "# " + prop("mod.version") + " version here! \n\n" +
                rootProject.file("CHANGELOG.md").readText() + "\n\n<@&1426901890582581248>"
        }
    }
    discord("DR freak mods announcement (RU)") {
        webhookUrl = discordWebhookDRRU.toString()
        dryRunWebhookUrl = discordWebhookDry.toString()

        username = prop("mod.name")
        avatarUrl = "https://github.com/Danrus1100/rpt/blob/master/src/main/resources/assets/rpt/icon.png?raw=true"

        content = changelog.map {
            "# Версия " + prop("mod.version") + " вышла! \n\n" +
                rootProject.file("CHANGELOG_RU.md").readText() + "\n\n<@&1426901890582581248>"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.danrus"
            artifactId = "rpt"
            version = artifactVersion
        }
    }

    repositories {
        maven {
            name = "Shlakoblock"
            url = uri("https://maven.shlakoblock.com/releases")

            credentials {
                username = project.findProperty("shlakoblock-maven-username")?.toString()
                password = project.findProperty("shlakoblock-maven-password")?.toString()
            }
        }
    }
}
