import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.bundling.Jar
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.api.credentials.HttpHeaderCredentials
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    signing
    id("kotlin-parcelize")
    alias(libs.plugins.version.check)
    alias(libs.plugins.vanniktech.maven.publish)
}

val versionMajor = 1
val versionMinor = 2
val versionPatch = 5
val versionName = "$versionMajor.$versionMinor.$versionPatch"
val archiveName = "com.tpay.sdk-$versionName"

android {
    namespace = "com.tpay.sdk"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
        setProperty("archivesBaseName", archiveName)

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("11")
    }
}

dependencies {
    // Android
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment.ktx)

    // Google Pay
    implementation(libs.google.play.services.wallet)

    // Inject
    implementation(libs.javax.inject)

    // Material
    implementation(libs.google.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}

dokka {
    dokkaSourceSets.main {
        enableAndroidDocumentationLink = false
    }
}

tasks.register<Jar>("generateDocumentation") {
    archiveClassifier.set("javadoc")
    dependsOn(tasks.named("dokkaGeneratePublicationJavadoc"))
    from(tasks.named("dokkaGeneratePublicationJavadoc").map { it.outputs })
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from("src/main/kotlin/com/tpay/sdk/api")
}

val isSigningDisabled = project.hasProperty("skipSigning") ||
    (project.hasProperty("mavenPublishSigningEnabled") &&
     !(project.property("mavenPublishSigningEnabled") as String).toBoolean())

gradle.taskGraph.whenReady {
    if (isSigningDisabled) {
        tasks.withType<Sign>().configureEach {
            enabled = false
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

afterEvaluate {
    signing {
        useInMemoryPgpKeys(
            System.getenv("SIGNING_KEY"),
            System.getenv("SIGNING_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }

    publishing {
        repositories {
            maven {
                name = "GitlabRegistry"
                val registryUrl = System.getenv("GITLAB_REGISTRY_URL")
                if (registryUrl != null) {
                    url = uri(registryUrl)
                    credentials(HttpHeaderCredentials::class) {
                        name = "Job-Token"
                        value = System.getenv("CI_JOB_TOKEN")
                    }
                    authentication {
                        create<HttpHeaderAuthentication>("header")
                    }
                }
            }

            maven {
                name = "Local"
                url = uri("${layout.buildDirectory.get()}/tpayMaven/")
            }
        }
    }

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
        signAllPublications()

        coordinates("com.tpay", "sdk", versionName)

        pom {
            name.set("Tpay SDK")
            description.set("Tpay - online payment operator for Android")
            url.set("https://tpay.com/")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("http://www.opensource.org/licenses/mit-license.php")
                }
            }
            scm {
                url.set("https://github.com/tpay-com/tpay-android")
                connection.set("scm:git:git://github.com/tpay-com/tpay-android.git")
                developerConnection.set("scm:git:git://github.com/tpay-com/tpay-android.git")
            }
            developers {
                developer {
                    id.set("com.tpay.devs")
                    name.set("Tpay Auto Commit")
                }
            }
        }
    }
}
