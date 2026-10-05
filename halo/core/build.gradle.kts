import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.anthropic:anthropic-java:2.68.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

// Grades real Claude replies against the conversation rules. Needs ANTHROPIC_API_KEY and spends API credit.
tasks.register<JavaExec>("eval") {
    group = "verification"
    description = "Runs Halo's test cases against real Claude and grades the replies."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("halo.evaluation.RunEvalKt")
}
