#!/usr/bin/env python3
"""Run real Gradle hooks in an offline, credential-free, Android-free fixture.
Usage: python3 gradle/tests/release-guard-integration.py /path/to/gradle
"""
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

root = Path(__file__).resolve().parents[2]
gradle = str(Path(sys.argv[1]).resolve())
with tempfile.TemporaryDirectory(prefix='gsy-release-guard-') as tmp:
    fixture = Path(tmp)
    for name in ['gradle/ReleaseVersionGuard.groovy', 'gradle/release-version-settings.gradle',
                 'gradle/tests/release-version-guard-test.groovy', 'gradle.properties',
                 '.github/workflows/publish-maven-central.yml', '.github/workflows/release.yml',
                 'build.gradle']:
        dest = fixture / name
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(root / name, dest)
    # The real project buildscript and all repositories/plugins are not loaded.
    # The config-writer fixture reproduces the exact AGP8.9.1 class/name shape;
    (fixture / 'build.gradle').write_text('''
apply plugin: 'signing'
apply plugin: 'maven-publish'
def writerType = new GroovyClassLoader(getClass().classLoader).parseClass(
    'package com.android.build.gradle.internal.tasks; abstract class SigningConfigWriterTask extends org.gradle.api.DefaultTask {}')
tasks.register('signingConfigWriterDebug', writerType) {
    doLast { println('SAFE_FIXTURE_ACTION local AGP config writer') }
}
tasks.register('signingConfigWriterSpoof', org.gradle.plugins.signing.Sign)
tasks.register('innocentSignature', org.gradle.plugins.signing.Sign)
tasks.register('innocentPublisher', org.gradle.api.publish.maven.tasks.PublishToMavenRepository)
['assembleDebug', 'publishReleasePublicationToMavenLocal',
 'publishReleasePublicationToGsyvideoplayerRepository', 'signMavenCentralPublication',
 'closeAndReleaseSonatypeStagingRepository'].each { n ->
    tasks.register(n) { doLast { println('SAFE_FIXTURE_ACTION ' + n) } }
}
tasks.named('assembleDebug') { dependsOn 'signingConfigWriterDebug' }
tasks.register('indirect') { dependsOn 'publishReleasePublicationToGsyvideoplayerRepository' }
''')
    (fixture / 'settings.gradle').write_text("apply from: 'gradle/release-version-settings.gradle'\n")
    (fixture / 'gradle/tests/release-version-guard-test.groovy').write_text("println('SAFE_FIXTURE_TEST_ACTION')\n")
    env = {k: os.environ[k] for k in ['PATH', 'JAVA_HOME', 'HOME'] if k in os.environ}
    env['GRADLE_USER_HOME'] = str(fixture / 'user-home')
    cases = [
        ('local build and MavenLocal', ['verifyReleaseVersion', 'verifyReleaseGuardTests', 'assembleDebug', 'publishToMavenLocal'], {}, True, None),
        ('stable override', ['assembleDebug', '-PPROJ_VERSION=13.2.1'], {}, False, 'stable coordinates'),
        ('manual mismatch', ['assembleDebug'], {'GSY_RELEASE_VERSION': '13.2.1'}, False, 'Manual release version'),
        ('tag mismatch', ['assembleDebug'], {'GITHUB_REF_TYPE': 'tag', 'GITHUB_REF_NAME': 'v13.2.1'}, False, 'Release tag'),
        ('release intent locked', ['verifyReleaseVersion', '-PreleaseIntent=true'], {}, False, 'UNPUBLISHED'),
        ('abbreviated remote task', ['pRPTG'], {}, False, 'blocked tasks'),
        ('indirect publication', ['indirect'], {}, False, 'blocked tasks'),
        ('signing', ['signMavenCentralPublication'], {}, False, 'blocked tasks'),
        ('spoofed config writer using real Sign type', ['signingConfigWriterSpoof'], {}, False, 'blocked tasks'),
        ('renamed publication Sign type', ['innocentSignature'], {}, False, 'blocked tasks'),
        ('renamed remote Maven publisher type', ['innocentPublisher'], {}, False, 'blocked tasks'),
        ('Nexus lifecycle', ['closeAndReleaseSonatypeStagingRepository'], {}, False, 'blocked tasks'),
        ('configuration cache forbidden', ['assembleDebug', '--configuration-cache'], {}, False, 'requires --no-configuration-cache'),
    ]
    for name, args, extra, success, reason in cases:
        result = subprocess.run([gradle, '--offline', '--no-daemon', '--no-configuration-cache',
                                 '--max-workers=2', '--no-parallel', '-Dorg.gradle.jvmargs=-Xmx512m',
                                 '-p', str(fixture)] + args, env={**env, **extra},
                                text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        assert (result.returncode == 0) == success, name + '\n' + result.stdout
        if success:
            assert 'SAFE_FIXTURE_TEST_ACTION' in result.stdout, result.stdout
            assert 'SAFE_FIXTURE_ACTION local AGP config writer' in result.stdout, result.stdout
        if reason:
            assert reason in result.stdout, name + '\n' + result.stdout
            assert 'SAFE_FIXTURE_ACTION' not in result.stdout, name + ': task ran before guard'
        print('PASS ' + name, flush=True)
    print(f'{len(cases)}/{len(cases)} actual Gradle fixture checks passed; no Android or network tasks.')
