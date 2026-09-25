# frozen_string_literal: true
require 'xcodeproj'

root = File.expand_path(__dir__)
path = File.join(root, 'PolskiGrammar.xcodeproj')
project = Xcodeproj::Project.new(path)
project.root_object.compatibility_version = 'Xcode 14.0'
project.root_object.development_region = 'ru'

target = project.new_target(:application, 'PolskiGrammar', :ios, '17.0')
source = project.main_group.new_group('PolskiGrammar', 'PolskiGrammar')
source_ref = source.new_file('PolskiGrammarApp.swift')
target.source_build_phase.add_file_reference(source_ref)
assets_ref = source.new_file('Assets.xcassets')
target.resources_build_phase.add_file_reference(assets_ref)

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
