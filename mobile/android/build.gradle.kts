allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}

// Plugins tiers (file_picker, flutter_plugin_android_lifecycle...) exigent un
// compileSdk récent : on aligne tous les modules Android sur 36 minimum.
// IMPORTANT : ce bloc DOIT précéder evaluationDependsOn(":app") — afterEvaluate
// échoue sur un projet déjà évalué.
subprojects {
    afterEvaluate {
        when (val android = extensions.findByName("android")) {
            is com.android.build.api.dsl.LibraryExtension -> {
                if ((android.compileSdk ?: 0) < 36) android.compileSdk = 36
            }
            is com.android.build.api.dsl.ApplicationExtension -> {
                if ((android.compileSdk ?: 0) < 36) android.compileSdk = 36
            }
        }
    }
}

subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
