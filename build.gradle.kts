import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

group = "org.example"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.codemc.io/repository/maven-public/")
}

val projectFullName = "${project.name}-LATEST.jar";

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("/home/post/dev/bukkit-libs/WorldLib-LATEST.jar"))
    compileOnly(files("/home/post/dev/bukkit-libs/PlayerPoints-LATEST.jar"))
    compileOnly(files("/home/post/dev/bukkit-libs/yPoints-LATEST.jar"))
    compileOnly("net.luckperms:api:5.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2");
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed")
    }
}

tasks.withType<ShadowJar> {
    archiveFileName.set(projectFullName);
}

task("shadowAndCopy") {
    group = "Build"
    description = "Copies the jar into the location of the OUTPUT_PATH variable"
    dependsOn("shadowJar")

    val copyTask: (String, Int) -> Copy = { dest, id ->
        tasks.create("copyTaskExec_$id", Copy::class) {
            from(layout.buildDirectory.dir("libs"))
            into(dest)
        }
    }

    val deleteTask: (String, Int) -> Delete = { dir, id ->
        tasks.create("deleteTaskExec_$id", Delete::class) {
            val filePath = "$dir/$projectFullName"
            delete(filePath)
        }
    }

    fun build(dirsRaw: String) {
        System.getenv(dirsRaw).split(",").forEachIndexed { idx, dest ->
            deleteTask(dest, idx).run { actions[0].execute(this) }
            copyTask(dest, idx).run { actions[0].execute(this) }
        }
    }

    doLast {
        build("paths")
    }
}
