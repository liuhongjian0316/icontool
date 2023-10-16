import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "work.aijiu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("androidx.datastore:datastore-preferences-core:1.1.0-dev01")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "iconTool"
            packageVersion = "1.0.0"
            description = "导出Android 图标资源"
            vendor = "aijiu"

            linux {
                packageVersion = "1.0.0"
                debPackageVersion = "1.0.0"
                rpmPackageVersion = "1.0.0"
                iconFile.set(project.file("launcher/icon.png"))
            }
            macOS {
                packageVersion = "1.0.0"
                dmgPackageVersion = "1.0.0"
                pkgPackageVersion = "1.0.0"
                dockName = "iconTool"

                packageBuildVersion = "1.0.0"
                dmgPackageBuildVersion = "1.0.0"
                pkgPackageBuildVersion = "1.0.0"
                iconFile.set(project.file("launcher/icon.icns"))
            }
            windows {
                packageVersion = "1.0.0"
                msiPackageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
                iconFile.set(project.file("launcher/icon.ico"))
            }
        }
    }
}