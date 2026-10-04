// Run with Groovy 3.x; no Android SDK, network, credentials or publishing required.
def root = new File(args.length ? args[0] : '.').canonicalFile
def guard = new GroovyClassLoader().parseClass(new File(root, 'gradle/ReleaseVersionGuard.groovy'))
int count = 0
def test = { String name, Closure body -> body(); count++; println("PASS ${name}") }
def rejects = { Closure body ->
    boolean rejected = false
    try { body() } catch (IllegalArgumentException expected) { rejected = true }
    assert rejected
}
def v = guard.CANDIDATE
test('local candidate build') { guard.validate(v, v, null, null, false) }
test('matching manual version') { guard.validate(v, v, v, null, false) }
test('matching v tag') { guard.validate(v, v, null, 'v' + v, false) }
test('matching bare tag') { guard.validate(v, v, null, v, false) }
test('stable committed coordinates forbidden') { rejects { guard.validate('13.2.1', '13.2.1', null, null, false) } }
test('stable override forbidden') { rejects { guard.validate(v, '13.2.1', null, null, false) } }
test('other candidate override forbidden') { rejects { guard.validate(v, '13.3.0-ffmpeg5-other', null, null, false) } }
test('manual mismatch') { rejects { guard.validate(v, v, '13.2.1', null, false) } }
test('tag mismatch') { rejects { guard.validate(v, v, null, 'v13.2.1', false) } }
test('tag whitespace rejected') { rejects { guard.validate(v, v, null, 'v' + v + ' ', false) } }
test('no release switch unlock') { rejects { guard.validate(v, v, v, 'v' + v, true) } }
test('missing effective version rejected') { rejects { guard.validate(v, null, null, null, false) } }
test('remote publish routes blocked') {
    ['publish', 'publishReleasePublicationToGsyvideoplayerRepository',
     ':gsyVideoPlayer:publishMavenCentralPublicationToSonatypeRepository',
     'publishAllPublicationsToOtherRepository'].each { assert guard.forbiddenTask(it) }
}
test('signing and Nexus lifecycle blocked') {
    ['signMavenCentralPublication', 'initializeSonatypeStagingRepository',
     'closeAndReleaseSonatypeStagingRepository', 'uploadArchives'].each { assert guard.forbiddenTask(it) }
}
test('local packaging and MavenLocal permitted') {
    ['assembleDebug', 'assembleRelease', 'generatePomFileForReleasePublication',
     'publishToMavenLocal', 'publishReleasePublicationToMavenLocal',
     'verifyCastDependencyIsolation', 'verifyReleaseVersion'].each { assert !guard.forbiddenTask(it) }
}
test('typed local AGP config writers permitted') {
    ['signingConfigWriterDebug', 'signingConfigWriterRelease'].each {
        assert !guard.forbiddenTask(it,
                ['com.android.build.gradle.internal.tasks.SigningConfigWriterTask'])
    }
}
test('untyped or wrong-type signing config names remain blocked') {
    assert guard.forbiddenTask('signingConfigWriterDebug')
    assert guard.forbiddenTask('signingConfigWriterDebug', ['org.gradle.plugins.signing.Sign'])
}
test('renamed publication actions remain blocked by type') {
    ['org.gradle.plugins.signing.Sign',
     'org.gradle.api.publish.maven.tasks.PublishToMavenRepository',
     'org.gradle.api.publish.ivy.tasks.PublishToIvyRepository'].each {
        assert guard.forbiddenTask('innocentName', [it])
        assert guard.forbiddenTask('publishToMavenLocal', [it])
    }
}
test('workflow preflight before build or secrets') {
    String m = new File(root, '.github/workflows/publish-maven-central.yml').text
    assert m.contains('GSY_RELEASE_VERSION: ${{ inputs.version }}')
    assert m.indexOf('verifyReleaseVersion -PreleaseIntent=true') < m.indexOf('- name: Import GPG key')
    String r = new File(root, '.github/workflows/release.yml').text
    assert r.count('verifyReleaseVersion -PreleaseIntent=true') == 2
    assert r.indexOf('verifyReleaseVersion -PreleaseIntent=true') < r.indexOf('- name: Build APK')
    assert r.lastIndexOf('verifyReleaseVersion -PreleaseIntent=true') < r.indexOf('- name: Publish Package')
}
test('settings graph hook, version and credential wiring') {
    String s = new File(root, 'gradle/release-version-settings.gradle').text
    assert s.contains('gradle.taskGraph.whenReady') && s.contains('graph.allTasks')
    assert s.contains("providers.gradleProperty('PROJ_VERSION')")
    assert s.contains("GITHUB_REF_TYPE") && s.contains("GITHUB_REF_NAME")
    assert s.contains('p.version = effective')
    String b = new File(root, 'build.gradle').text
    assert b.contains('com.android.tools.build:gradle:8.9.1')
    assert b.contains('if (readUser && readToken)')
    assert !b.find(/gh[pousr]_[A-Za-z0-9_]+/)
    assert !b.contains("?: 'carsmallguo'")
    Properties p = new Properties(); new File(root, 'gradle.properties').withInputStream { p.load(it) }
    assert p.getProperty('PROJ_VERSION') == v
}
println("${count}/${count} release guard tests passed (policy and wiring; not Android builds).")
