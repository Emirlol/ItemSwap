plugins {
	id("fabric-loom") version "1.7-SNAPSHOT"
	kotlin("jvm") version "2.0.10"
	id("me.modmuss50.mod-publish-plugin") version "0.6.0"
}

repositories {
	mavenCentral()
	maven("https://maven.isxander.dev/releases") {
		name = "Xander Maven"
	}
	maven("https://maven.terraformersmc.com/releases") {
		name = "Terraformers"
	}
}

val minecraftVersion = property("minecraft_version") as String
val yarnMappings = property("yarn_mappings") as String
val loaderVersion = property("fabric_loader_version") as String

val modName = property("mod_name") as String
val modId = property("mod_id") as String
val modVersion = property("mod_version") as String
version = "$modVersion+$minecraftVersion"
group = property("maven_group") as String

val fabricApiVersion = property("fabric_api_version") as String
val fabricKotlinVersion = property("fabric_kotlin_version") as String
val yaclVersion = property("yacl_version") as String
val modmenuVersion = property("modmenu_version") as String

dependencies {
	// To change the versions, see the gradle.properties file
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings("net.fabricmc:yarn:$yarnMappings:v2")
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

	modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
	modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
	modImplementation("dev.isxander:yet-another-config-lib:$yaclVersion")
	modImplementation("com.terraformersmc:modmenu:$modmenuVersion")
}

tasks {
	processResources {
		filesMatching("fabric.mod.json") {
			expand(
				mapOf(
					"version" to version,
					"minecraft_version" to minecraftVersion,
					"loader_version" to loaderVersion,
					"name" to modName,
					"mod_id" to modId,
					"fabric_kotlin_version" to fabricKotlinVersion,
					"modmenu_version" to modmenuVersion,
					"yacl_version" to yaclVersion
				)
			)
		}
		filesMatching("assets/$modId/lang/*.json") {
			expand(
				mapOf(
					"namespace" to modId
				)
			)
		}
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesName.get()}"}
		}
	}
}

base {
	archivesName = modName
}

kotlin {
	jvmToolchain(21)
}

idea {
	module {
		excludeDirs.addAll(listOf(file("run"), file(".kotlin")))
	}
}

publishMods {
	file = tasks.remapJar.get().archiveFile
	modLoaders.add("fabric")
	type = STABLE
	displayName = "Item Swap $modVersion for $minecraftVersion"
	changelog = """
		## Mod Overhaul
		Complete rewrite of the mod. Now, the configuration is much more fluid, and the mod renders stuff to indicate what is mapped to what.
		- Added a key to configure slot swaps. Hold the key and drag your cursor to a valid slot and release the key to create a mapping. Now, when you use shift-click, the items in those 2 slots will swap. This is only possible between a hotbar slot and an inventory slot (including armor slots), so there's a maximum of 9 mappings possible at the same time.
		- Added a key to reset the mappings. Press the key twice within 500ms (configurable) to reset all mappings.
		- Updated some dependencies.
	""".trimIndent()
	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "baQgLwO4"
		minecraftVersions.addAll("1.20.5", "1.20.6")
		requires("fabric-api")
		requires("fabric-language-kotlin")
		requires("yacl")
		optional("modmenu")
		featured = true
	}
}

