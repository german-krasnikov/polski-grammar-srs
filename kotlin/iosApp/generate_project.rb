# frozen_string_literal: true
require 'xcodeproj'

root = File.expand_path(__dir__)
path = File.join(root, 'PolskiGrammar.xcodeproj')
project = Xcodeproj::Project.new(path)
project.root_object.compatibility_version = 'Xcode 14.0'
project.root_object.development_region = 'ru'

target = project.new_target(:application, 'PolskiGrammar', :ios, '17.0')
source = project.main_group.new_group('PolskiGrammar', 'PolskiGrammar')
%w[PolskiGrammarApp.swift FlashCardView.swift VocabularyCardView.swift SwipeToRate.swift RiveEffectOverlay.swift PagingTabBar.swift].each do |file|
  target.source_build_phase.add_file_reference(source.new_file(file))
end
assets_ref = source.new_file('Assets.xcassets')
target.resources_build_phase.add_file_reference(assets_ref)

# FC-15/FC2-06/09/10: confetti.riv/again.riv/chain-complete.riv (vendored from rive-ios/
# rive-android sample assets and the Rive Marketplace, see THIRD_PARTY/credits.md) ride the app
# bundle as plain resources, same as Assets.xcassets above. `rings.riv` (the flip-in-progress ring
# cue) was removed on this host in D3 (`Plans/Kotlin/FlipCardRivePlan.md`, no ring/circle effect
# anywhere) along with `RiveFlipRingsOverlay`.
rive_group = source.new_group('Rive', 'Rive')
%w[confetti.riv again.riv chain-complete.riv].each do |file|
  target.resources_build_phase.add_file_reference(rive_group.new_file(file))
end

# FC-17: rive-ios SPM package (RiveRuntime), added the way xcodeproj (gem 1.27.0) exposes it —
# a remote package reference on the project, a product dependency on the target, and a build file
# in the Frameworks phase referencing that product (mirrors what Xcode itself writes for "Add
# Package Dependency…"). Verified against the actual installed xcodeproj gem before writing this,
# per the plan's §6 "xcodeproj SPM API" open risk.
rive_package = project.new(Xcodeproj::Project::Object::XCRemoteSwiftPackageReference)
rive_package.repositoryURL = 'https://github.com/rive-app/rive-ios'
rive_package.requirement = { 'kind' => 'upToNextMajorVersion', 'minimumVersion' => '6.27.0' }
project.root_object.package_references << rive_package

rive_product = project.new(Xcodeproj::Project::Object::XCSwiftPackageProductDependency)
rive_product.package = rive_package
rive_product.product_name = 'RiveRuntime'
target.package_product_dependencies << rive_product

rive_build_file = project.new(Xcodeproj::Project::Object::PBXBuildFile)
rive_build_file.product_ref = rive_product
target.frameworks_build_phase.files << rive_build_file

script = target.new_shell_script_build_phase('Build Kotlin framework')
script.shell_script = <<~SH
  set -euo pipefail
  cd "${SRCROOT}/.."
  if [[ "${PLATFORM_NAME}" == "iphonesimulator" ]]; then
    KOTLIN_TARGET="IosSimulatorArm64"
    KOTLIN_FOLDER="iosSimulatorArm64"
  else
    KOTLIN_TARGET="IosArm64"
    KOTLIN_FOLDER="iosArm64"
  fi
  ./gradlew ":shared:linkDebugFramework${KOTLIN_TARGET}" --console=plain
  rm -rf "${BUILT_PRODUCTS_DIR}/PolskiShared.framework"
  cp -R "shared/build/bin/${KOTLIN_FOLDER}/debugFramework/PolskiShared.framework" "${BUILT_PRODUCTS_DIR}/PolskiShared.framework"
SH
script.always_out_of_date = '1'
target.build_phases.move(script, 0)

project.build_configurations.each do |config|
  config.build_settings['SWIFT_VERSION'] = '5.0'
  config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '17.0'
end

target.build_configurations.each do |config|
  config.build_settings['SWIFT_VERSION'] = '5.0'
  config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '17.0'
  config.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'dev.polski.grammarmatrix.ios'
  config.build_settings['PRODUCT_NAME'] = 'PolskiGrammar'
  config.build_settings['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'AppIcon'
  config.build_settings['GENERATE_INFOPLIST_FILE'] = 'YES'
  config.build_settings['INFOPLIST_KEY_CFBundleDisplayName'] = 'Polski Grammar Matrix'
  config.build_settings['INFOPLIST_KEY_UILaunchScreen_Generation'] = 'YES'
  config.build_settings['INFOPLIST_KEY_UISupportedInterfaceOrientations'] = 'UIInterfaceOrientationPortrait UIInterfaceOrientationPortraitUpsideDown UIInterfaceOrientationLandscapeLeft UIInterfaceOrientationLandscapeRight'
  config.build_settings['INFOPLIST_KEY_UIApplicationSceneManifest_Generation'] = 'YES'
  config.build_settings['TARGETED_DEVICE_FAMILY'] = '1,2'
  config.build_settings['FRAMEWORK_SEARCH_PATHS'] = ['$(inherited)', '$(BUILT_PRODUCTS_DIR)']
  config.build_settings['OTHER_LDFLAGS'] = ['$(inherited)', '-framework', 'PolskiShared']
  config.build_settings['CODE_SIGN_STYLE'] = 'Automatic'
end

tests = project.new_target(:ui_test_bundle, 'PolskiGrammarUITests', :ios, '17.0')
test_group = project.main_group.new_group('PolskiGrammarUITests', 'PolskiGrammarUITests')
tests.source_build_phase.add_file_reference(test_group.new_file('PolskiGrammarUITests.swift'))
# Tester-only perf harness for FlipCardRivePlan.md §5 (variants A/B/C); adds no production code.
tests.source_build_phase.add_file_reference(test_group.new_file('FlipRivePerfUITests.swift'))
# FC2-05's correctness proof (§12.1, R1): mid-flip screenshots/assertions, not performance.
tests.source_build_phase.add_file_reference(test_group.new_file('FlipCorrectnessUITests.swift'))
# D2: the vocabulary card's own whole-panel flip correctness proof.
tests.source_build_phase.add_file_reference(test_group.new_file('VocabularyFlipUITests.swift'))
tests.add_dependency(target)
tests.build_configurations.each do |config|
  config.build_settings['SWIFT_VERSION'] = '5.0'
  config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '17.0'
  config.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'dev.polski.grammarmatrix.ios.uitests'
  config.build_settings['GENERATE_INFOPLIST_FILE'] = 'YES'
  config.build_settings['TEST_TARGET_NAME'] = 'PolskiGrammar'
  config.build_settings['TARGETED_DEVICE_FAMILY'] = '1,2'
end

project.save
scheme = Xcodeproj::XCScheme.new
scheme.configure_with_targets(target, tests, launch_target: true)
scheme.save_as(path, 'PolskiGrammar', true)
