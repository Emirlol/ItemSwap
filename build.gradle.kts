plugins {
	id("fabric-loom") version "1.7-SNAPSHOT"
	kotlin("jvm") version "2.0.0"
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
version = property("mod_version") as String
group = property("maven_group") as String

val fabricApiVersion = property("fabric_api_version") as String
val fabricKotlinVersion = property("fabric_kotlin_version") as String
val yaclVersion = property("yacl_version") as String
val modmenuVersion = property("modmenu_version") as String

dependencies {
	// To change the versions see the gradle.properties file
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
}

kotlin {
	jvmToolchain(21)
}

idea {
	module {
		excludeDirs.addAll(listOf(file("run"), file(".kotlin")))
	}
}

