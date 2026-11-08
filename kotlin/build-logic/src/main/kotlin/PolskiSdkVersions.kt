/** Single source of truth for Android SDK levels and JVM toolchain used across all modules.
 *  Every build.gradle.kts references these constants instead of hardcoding the numbers. */
object PolskiSdkVersions {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
    const val JVM_TOOLCHAIN = 21
}
