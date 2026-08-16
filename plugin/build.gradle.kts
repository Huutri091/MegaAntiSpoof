plugins { id("com.gradleup.shadow") }
dependencies {
    implementation(project(":core"))
    implementation(project(":nms-v1_21"))
    implementation(project(":nms-v1_21_9"))
    implementation(project(":nms-v26"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.13.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")
}
tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
tasks.shadowJar {
    archiveBaseName.set("MegaAntiSpoof")
    archiveClassifier.set("")
    mergeServiceFiles()
}
tasks.build { dependsOn(tasks.shadowJar) }
