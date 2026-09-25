# frozen_string_literal: true
require 'xcodeproj'

root = File.expand_path(__dir__)
path = File.join(root, 'PolskiGrammarMac.xcodeproj')
project = Xcodeproj::Project.new(path)
project.root_object.compatibility_version = 'Xcode 14.0'
project.root_object.development_region = 'ru'

target = project.new_target(:application, 'PolskiGrammarMac', :osx, '14.0')
source = project.main_group.new_group('PolskiGrammarMac', 'PolskiGrammarMac')
target.source_build_phase.add_file_reference(source.new_file('PolskiGrammarMacApp.swift'))

script = target.new_shell_script_build_phase('Build Kotlin macOS framework')
script.shell_script = <<~SH
  set -euo pipefail
  cd "${SRCROOT}/.."
  if [[ "${ARCHS}" != *arm64* ]]; then
    echo "Only macosArm64 is verified; Intel requires a separate Kotlin target" >&2
    exit 1
  fi
  ./gradlew :shared:linkDebugFrameworkMacosArm64 --console=plain
  rm -rf "${BUILT_PRODUCTS_DIR}/PolskiShared.framework"
  cp -R "shared/build/bin/macosArm64/debugFramework/PolskiShared.framework" "${BUILT_PRODUCTS_DIR}/PolskiShared.framework"
SH
script.always_out_of_date = '1'
target.build_phases.move(script, 0)

target.build_configurations.each do |config|
  config.build_settings['SWIFT_VERSION'] = '5.0'
  config.build_settings['MACOSX_DEPLOYMENT_TARGET'] = '14.0'
  config.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'dev.polski.grammarmatrix.mac'
  config.build_settings['PRODUCT_NAME'] = 'PolskiGrammarMac'
  config.build_settings['GENERATE_INFOPLIST_FILE'] = 'YES'
  config.build_settings['INFOPLIST_KEY_CFBundleDisplayName'] = 'Polski Grammar Matrix'
  config.build_settings['FRAMEWORK_SEARCH_PATHS'] = ['$(inherited)', '$(BUILT_PRODUCTS_DIR)']
  config.build_settings['OTHER_LDFLAGS'] = ['$(inherited)', '-framework', 'PolskiShared']
  config.build_settings['CODE_SIGN_STYLE'] = 'Automatic'
  config.build_settings['ARCHS'] = 'arm64'
end

project.save
scheme = Xcodeproj::XCScheme.new
scheme.configure_with_targets(target, nil, launch_target: true)
scheme.save_as(path, 'PolskiGrammarMac', true)
