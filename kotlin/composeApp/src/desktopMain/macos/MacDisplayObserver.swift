import AppKit
import Foundation

// A line-oriented, macOS-only companion for the Compose/JVM window.
// stdout is reserved for protocol snapshots; diagnostics go to stderr.
final class DisplayObserver {
    private var appearanceObservation: NSKeyValueObservation?
    private var accessibilityObservation: NSObjectProtocol?
    private var previous: String?

    func start() {
        let app = NSApplication.shared
        app.setActivationPolicy(.prohibited)
        appearanceObservation = app.observe(\.effectiveAppearance, options: [.initial, .new]) { [weak self] _, _ in
            self?.publish()
        }
        accessibilityObservation = NSWorkspace.shared.notificationCenter.addObserver(
            forName: NSWorkspace.accessibilityDisplayOptionsDidChangeNotification,
            object: NSWorkspace.shared,
            queue: .main
        ) { [weak self] _ in self?.publish() }
        publish()
        app.run()
    }

    private func publish() {
        let dark = NSApp.effectiveAppearance.bestMatch(from: [.darkAqua, .aqua]) == .darkAqua
        let workspace = NSWorkspace.shared
        let line = "v1 dark=\(dark ? 1 : 0) motion=\(workspace.accessibilityDisplayShouldReduceMotion ? 1 : 0) contrast=\(workspace.accessibilityDisplayShouldIncreaseContrast ? 1 : 0)"
        guard line != previous else { return }
        previous = line
        print(line)
        fflush(stdout)
    }

    deinit {
        appearanceObservation?.invalidate()
        if let accessibilityObservation {
            NSWorkspace.shared.notificationCenter.removeObserver(accessibilityObservation)
        }
    }
}

let observer = DisplayObserver()
observer.start()
