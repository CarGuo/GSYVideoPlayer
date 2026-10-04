/** Offline policy for this unpublished recovery tree. No credential access. */
class ReleaseVersionGuard {
    static final String CANDIDATE = '13.3.0-ffmpeg5-platformrate-r3-codec2-java-r1-SNAPSHOT'

    static void validate(String committed, String effective, String manual, String tag,
                         boolean releaseIntent) {
        if (committed != CANDIDATE || effective != committed) {
            throw new IllegalArgumentException('Recovery version must remain ' + CANDIDATE +
                    '; stable coordinates and command-line version overrides are forbidden.')
        }
        if (manual != null && !manual.isEmpty() && manual != effective) {
            throw new IllegalArgumentException('Manual release version differs from PROJ_VERSION.')
        }
        if (tag != null && !tag.isEmpty()) {
            String normalized = tag.startsWith('v') ? tag.substring(1) : tag
            if (normalized != effective) {
                throw new IllegalArgumentException('Release tag differs from PROJ_VERSION.')
            }
        }
        if (releaseIntent) {
            throw new IllegalArgumentException('This FFmpeg5 recovery candidate is UNPUBLISHED. ' +
                    'Remote publishing, release creation and publication signing are locked; ' +
                    'release requires reviewed source changes after artifact/device validation.')
        }
    }

    static boolean forbiddenTask(String name, Collection<String> taskTypes = []) {
        String n = name.tokenize(':').last().toLowerCase(Locale.ROOT)
        // Publication signing/publishing is blocked by type even when renamed.
        if (taskTypes.any { it in ['org.gradle.plugins.signing.Sign',
                'org.gradle.api.publish.maven.tasks.PublishToMavenRepository',
                'org.gradle.api.publish.ivy.tasks.PublishToIvyRepository'] }) {
            return true
        }
        // AGP's local APK config writer is not publication signing. Match BOTH
        // the actual AGP superclass and its task prefix, never a name-only allowlist.
        boolean localSigningConfigWriter = n.startsWith('signingconfigwriter') &&
                taskTypes.contains('com.android.build.gradle.internal.tasks.SigningConfigWriterTask')
        // Local POM/AAR generation and MavenLocal remain usable. Task graph checking
        // also catches Gradle task abbreviations and aggregate/dependency invocations.
        return (n.startsWith('publish') && !n.endsWith('tomavenlocal')) ||
                (n.startsWith('sign') && !localSigningConfigWriter) || n.contains('sonatype') ||
                n.contains('stagingrepository') || n.startsWith('upload')
    }
}
