plugins {
	`java-platform`
	id("junitbuild.publishing-conventions")
}

description = "${rootProject.description} (Bill of Materials)"

dependencies {
	constraints {
		val mavenizedProjects: List<Project> by rootProject.extra
		mavenizedProjects.sorted()
				.filter { it.name != "junit-platform-console-standalone" }
				.forEach { api("${it.group}:${it.name}:${it.version}") }
	}
}

publishing.publications.named<MavenPublication>("maven") {
	from(components["javaPlatform"])
	pom {
		description = "This Bill of Materials POM can be used to ease dependency management " +
				"when referencing multiple JUnit artifacts using Gradle or Maven."
		withXml {
			val filteredContent = asString().replace("\\s*<scope>compile</scope>".toRegex(), "")
			asString().clear().append(filteredContent)
		}
	}
}

tasks.withType<GenerateMavenPom>().configureEach {
	doLast {
		val destinationFile = destination.asFile.get()
		val xml = destinationFile.readText()
		require(xml.indexOf("<dependencies>") == xml.lastIndexOf("<dependencies>")) {
			"BOM must contain exactly one <dependencies> element but contained multiple:\n$destinationFile"
		}
		require(xml.contains("<dependencyManagement>")) {
			"BOM must contain a <dependencyManagement> element:\n$destinationFile"
		}
		require(!xml.contains("<scope>")) {
			"BOM must not contain <scope> elements:\n$destinationFile"
		}
	}
}
