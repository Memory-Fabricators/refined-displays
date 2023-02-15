import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	`maven-publish`

	alias(libs.plugins.kotlin)
	alias(libs.plugins.quilt.loom)
	alias(libs.plugins.ktlint)
}

val archives_base_name: String by project
base.archivesName.set(archives_base_name)

val javaVersion = 19

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
dependencies {
	minecraft(libs.minecraft)
	mappings(
		variantOf(libs.quilt.mappings) {
			classifier("intermediary-v2")
		},
	)

	modImplementation(libs.quilt.loader)
	modImplementation(libs.qfapi)
	modImplementation(libs.qkl)
}

tasks {
	withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = javaVersion.toString()
			languageVersion = libs.plugins.kotlin.get().version.requiredVersion.substringBeforeLast('.')
		}
	}

	withType<JavaCompile> {
		options.encoding = "UTF-8"
		options.isDeprecation = true
		options.release.set(javaVersion)
	}

	processResources {
		filteringCharset = "UTF-8"
		inputs.property("version", project.version)

		filesMatching("quilt.mod.json") {
			expand(
				mapOf(
					"version" to project.version,
				),
			)
		}
	}

	javadoc {
		options.encoding = "UTF-8"
	}

	wrapper {
		distributionType = Wrapper.DistributionType.BIN
	}

	jar {
		from("LICENSE") {
			rename { "LICENSE_$archives_base_name" }
		}
	}
}

java {
	withSourcesJar()
	withJavadocJar()
}

// Configure the maven publication
publishing {
	publications {
		register<MavenPublication>("Maven") {
			from(components.getByName("java"))
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
