// dl.google.com is unreachable from this network: the two domestic mirrors
// carry the same google()/mavenCentral() content and are consulted first.
// The canonical repos stay last as fallbacks for networks where they work.
pluginManagement {
    repositories {
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "goutou-junshi"
include(":app")
