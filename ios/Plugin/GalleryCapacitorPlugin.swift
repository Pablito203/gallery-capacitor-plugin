import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(GalleryCapacitorPlugin)
public class GalleryCapacitorPlugin: CAPPlugin {
    public let errorPickFileCanceled = "pickFiles canceled."
    public let errorTemporaryCopyFailed = "An unknown error occurred while creating a temporary copy of the file."
    public let errorUnsupportedFileTypeIdentifier = "Unsupported file type identifier."
    public let pickerDismissedEvent = "pickerDismissed"
    private var implementation: GalleryCapacitor?
    private var savedCall: CAPPluginCall?

    override public func load() {
        self.implementation = GalleryCapacitor(self)
    }

    @objc func pickFiles(_ call: CAPPluginCall) {
        savedCall = call

        let maximumFilesCount = call.getInt("maximumFilesCount", 15)

        implementation?.openImagePicker(maximumFilesCount: maximumFilesCount)
    }

    @objc func notifyPickerDismissedListener() {
        notifyListeners(pickerDismissedEvent, data: nil)
    }

    @objc override public func checkPermissions(_ call: CAPPluginCall) {
        call.resolve([
            "gallery": "granted"
        ])
    }

    @objc override public func requestPermissions(_ call: CAPPluginCall) {
        call.resolve([
            "gallery": "granted"
        ])
    }

    @objc func handleDocumentPickerResult(urls: [URL]?, error: String?) {
        guard let savedCall = savedCall else {
            return
        }
        if let error = error {
            savedCall.reject(error)
            return
        }
        guard let urls = urls else {
            savedCall.reject(errorPickFileCanceled)
            return
        }

        do {
            var result = JSObject()
            let filesResult = try urls.map {(url: URL) -> JSObject in
                var file = JSObject()

                file["mimeType"] = implementation?.getMimeTypeFromUrl(url) ?? ""
                file["name"] = implementation?.getNameFromUrl(url) ?? ""
                file["path"] = implementation?.getPathFromUrl(url) ?? ""
                file["size"] = try implementation?.getSizeFromUrl(url) ?? -1
                return file
            }
            result["files"] = filesResult
            savedCall.resolve(result)
        } catch let error as NSError {
            savedCall.reject(error.localizedDescription, nil, error)
            return
        }
    }
}
