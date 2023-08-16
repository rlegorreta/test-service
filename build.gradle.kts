import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
}

group = "com.ailegorreta"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
}

extra["springCloudVersion"] = "2022.0.3-SNAPSHOT"
extra["ailegorreta-kit-version"] = "2.0.0"
extra["testKeycloakVersion"] = "2.3.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.retry:spring-retry")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("com.ailegorreta:ailegorreta-kit-commons-utils:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-resource-server-security:${property("ailegorreta-kit-version")}")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${property("testKeycloakVersion")}")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	systemProperty( "spring.profiles.active", "testdata")
}

tasks.named<BootBuildImage>("bootBuildImage") {
	environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17.*"))
	imageName.set("ailegorreta/${project.name}")
	docker {
		publishRegistry {
			username.set(project.findProperty("registryUsername").toString())
			password.set(project.findProperty("registryToken").toString())
			url.set(project.findProperty("registryUrl").toString())
		}
	}
}