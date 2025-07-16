plugins {
	alias(libs.plugins.shadow)
}

repositories {
	gradlePluginPortal()
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots")

	maven {
		url = uri("https://jitpack.io")
		content {
			includeGroup("com.github.johnrengelman")
			includeGroup("com.github.LeonMangler")
		}
	}
}

dependencies {
    implementation("net.md-5:bungeecord-api:1.20.2") {
        // brigadier is shaded into Bukkit at runtime; keep it off the compile‑path
        exclude(group = "com.mojang", module = "brigadier")
    }

    implementation("net.md-5:bungeecord-chat:1.20.2") {
        exclude(group = "com.mojang", module = "brigadier")
    }
}

version = "2.3.5"

tasks {
	withType<JavaCompile> {
		options.encoding = "UTF-8"
	}

	jar {
		archiveClassifier.set("noshade")
	}

	shadowJar {
		archiveClassifier.set("")
		archiveFileName.set("TabList-bungee-v${project.version}.jar")
	}

	build {
		dependsOn(shadowJar)
	}
}
