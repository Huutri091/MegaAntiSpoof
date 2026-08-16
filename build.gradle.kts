plugins {
    java
    id("com.gradleup.shadow") version "9.2.2" apply false
}

allprojects {
    group = "vn.megacitymc.megaantispoof"
    version = "1.0.8"
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

subprojects {
    apply(plugin = "java-library")
    java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
