plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.kotlin)
	alias(libs.plugins.modPublish)
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

val modName = property("mod_name") as String
val modId = property("mod_id") as String
group = property("maven_group") as String
version = "${libs.versions.modVersion.get()}+${libs.versions.minecraft.get()}"

dependencies {
	// To change the versions, see the gradle.properties file
	minecraft(libs.minecraft)
	mappings(libs.yarn)
	modImplementation(libs.fabricLoader)

	modImplementation(libs.fabricApi)
	modImplementation(libs.fabricLanguageKotlin)
	modImplementation(libs.yacl)
	modImplementation(libs.modMenu)
}

tasks {
	processResources {
		val props = mapOf(
			"name" to modName,
			"mod_id" to modId,
			"version" to version,
			"minecraft_version" to libs.versions.minecraft.get(),
			"loader_version" to libs.versions.fabricLoader.get(),
			"fabric_kotlin_version" to libs.versions.fabricLanguageKotlin.get(),
			"modmenu_version" to libs.versions.modMenu.get(),
			"yacl_version" to libs.versions.yacl.get()
		)
		inputs.properties(props)
		filesMatching("fabric.mod.json") {
			expand(props)
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

publishMods {
	file = tasks.remapJar.get().archiveFile
	modLoaders.add("fabric")
	type = STABLE
	displayName = "Item Swap ${libs.versions.modVersion.get()} for ${libs.versions.minecraft.get()}"
	changelog = """
		- Updated dependencies and changed how the config works slightly
	""".trimIndent()
	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "baQgLwO4"
		minecraftVersions.addAll("1.21", "1.21.1")
		requires("fabric-api")
		requires("fabric-language-kotlin")
		requires("yacl")
		optional("modmenu")
		featured = true
	}
}

