# frozen_string_literal: true
require 'xcodeproj'

root = File.expand_path(__dir__)
path = File.join(root, 'PolskiGrammarMac.xcodeproj')
project = Xcodeproj::Project.new(path)
project.root_object.compatibility_version = 'Xcode 14.0'
project.root_object.development_region = 'ru'

target = project.new_target(:application, 'PolskiGrammarMac', :osx, '14.0')
source = project.main_group.new_group('PolskiGrammarMac', 'PolskiGrammarMac')
%w[PolskiGrammarMacApp.swift MacFlashCardView.swift MacStyleBlockView.swift MacVocabularyCardView.swift RiveEffectOverlay.swift SwipeRating.swift].each do |file|
  target.source_build_phase.add_file_reference(source.new_file(file))
end

# FC-15: confetti.riv/again.riv (vendored, byte-identical to iosApp/PolskiGrammar/Rive — see
# THIRD_PARTY/credits.md) ride the app bundle as plain resources.
rive_group = source.new_group('Rive', 'Rive')
%w[confetti.riv again.riv].each do |file|
  target.resources_build_phase.add_file_reference(rive_group.new_file(file))
end

# FC-17: rive-ios SPM package (RiveRuntime), added the same way iosApp/generate_project.rb does —
# a remote package reference on the project, a product dependency on the target, and a build file
# in the Frameworks phase referencing that product. rive-ios supports macOS 13.1+, below this
# target's own 14.0 deployment target, so no bump is needed.
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
