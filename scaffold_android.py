import os

def create_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

base_dir = r"F:\Projects\SplitEasy\android"

# settings.gradle.kts
create_file(os.path.join(base_dir, "settings.gradle.kts"), """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "SplitEasy"
include(":app")
""")

# build.gradle.kts (Root)
create_file(os.path.join(base_dir, "build.gradle.kts"), """
buildscript {
    ext["compose_version"] = "1.5.0"
}
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
}
""")

# gradle.properties
create_file(os.path.join(base_dir, "gradle.properties"), """
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
""")

# app/build.gradle.kts
create_file(os.path.join(base_dir, "app", "build.gradle.kts"), """
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.spliteasy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.spliteasy"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}
""")

# AndroidManifest.xml
create_file(os.path.join(base_dir, "app", "src", "main", "AndroidManifest.xml"), """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.spliteasy">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.SplitEasy">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SplitEasy">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
""")

# MainActivity.kt
create_file(os.path.join(base_dir, "app", "src", "main", "java", "com", "spliteasy", "MainActivity.kt"), """
package com.spliteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("SplitEasy Backend is Running!")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
""")

# strings.xml
create_file(os.path.join(base_dir, "app", "src", "main", "res", "values", "strings.xml"), """
<resources>
    <string name="app_name">SplitEasy</string>
</resources>
""")

# themes.xml
create_file(os.path.join(base_dir, "app", "src", "main", "res", "values", "themes.xml"), """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.SplitEasy" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
""")

print("Android scaffolding complete.")
