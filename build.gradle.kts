import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

group = "org.example"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val projectFullName = "${project.name}-${project.version}.jar";

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    compileOnly(files("/home/post/dev/bukkit-libs/spigot.jar"))
    compileOnly(files("/home/post/dev/bukkit-libs/worldplugins/WorldLib/WorldLib-LATEST.jar"))
    compileOnly("net.luckperms:api:5.4")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set(projectFullName);
}

task("shadowAndCopy") {
    group = "Build"
    description = "Copies the jar into the location of the OUTPUT_PATH variable"
    dependsOn("shadowJar")

    val copyTask: (String) -> Copy = {
        tasks.create("copyTaskExec_$it", Copy::class) {
            val dest = System.getenv(it) ?: throw GradleException(
                    "Output path environment variable not set"
            )

            from(layout.buildDirectory.dir("libs"))
            into(dest)
        }
    }

    val deleteTask: (String) -> Delete = {
        tasks.create("deleteTaskExec_$it", Delete::class) {
            val fileDir = System.getenv(it) ?: throw GradleException(
                    "path environment variable not set"
            )
            val filePath = "$fileDir/$projectFullName"

            delete(filePath)
        }
    }

    fun build(pathEnvVar: String) {
        deleteTask(pathEnvVar).run { actions[0].execute(this) }
        copyTask(pathEnvVar).run { actions[0].execute(this) }
    }

    doLast {
        build("path")
    }
}
