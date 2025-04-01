import Foundation
import UIKit
import MobileCoreServices

@objc public class GalleryCapacitor: NSObject {
    private var plugin: GalleryCapacitorPlugin?

    init(_ plugin: GalleryCapacitorPlugin?) {
        super.init()
        self.plugin = plugin
    }

    public func openImagePicker(maximumFilesCount: Int) {
        DispatchQueue.main.async {
            let picker = GalleryController()
            picker.delegate = self
            self.presentViewController(picker)
        }
    }

    public func getFileUrlByPath(_ path: String) -> URL? {
        guard let url = URL.init(string: path) else {
            return nil
        }
        if FileManager.default.fileExists(atPath: url.path) {
            return url
        } else {
            return nil
        }
    }

    private func presentViewController(_ viewControllerToPresent: UIViewController) {
        self.plugin?.bridge?.viewController?.present(viewControllerToPresent, animated: true, completion: nil)
    }

    private func dismissViewController(_ viewControllerToPresent: UIViewController, completion: (() -> Void)? = nil) {
        viewControllerToPresent.dismiss(animated: true, completion: completion)
        plugin?.notifyPickerDismissedListener()
    }

    public func getMimeTypeFromUrl(_ url: URL) -> String {
        let fileExtension = url.pathExtension as CFString
        guard let extUTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, fileExtension, nil)?.takeUnretainedValue() else {
            return ""
        }
        guard let mimeUTI = UTTypeCopyPreferredTagWithClass(extUTI, kUTTagClassMIMEType) else {
            return ""
        }
        return mimeUTI.takeRetainedValue() as String
    }

    public func getPathFromUrl(_ url: URL) -> String {
        return url.absoluteString
    }

    public func getNameFromUrl(_ url: URL) -> String {
        return url.lastPathComponent
    }

    public func getSizeFromUrl(_ url: URL) throws -> Int {
        let values = try url.resourceValues(forKeys: [.fileSizeKey])
        return values.fileSize ?? 0
    }

    private func saveTemporaryFile(_ sourceUrl: URL) throws -> URL {
        var directory = URL(fileURLWithPath: NSTemporaryDirectory())
        if let cachesDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first {
            directory = cachesDirectory
        }
        let targetUrl = directory.appendingPathComponent(sourceUrl.lastPathComponent)
        do {
            try deleteFile(targetUrl)
        }
        try FileManager.default.copyItem(at: sourceUrl, to: targetUrl)
        return targetUrl
    }

    private func deleteFile(_ url: URL) throws {
        if FileManager.default.fileExists(atPath: url.path) {
            try FileManager.default.removeItem(atPath: url.path)
        }
    }
}

extension GalleryCapacitor: GalleryControllerDelegate {
    public func galleryController(_ urls: [URL], _ controller: GalleryController) {
        dismissViewController(controller)
        var temporaryUrls: [URL] = []
        for url in urls {
            do {
                let temporaryUrl = try self.saveTemporaryFile(url)
                temporaryUrls.append(temporaryUrl)
            } catch {
                self.plugin?.handleDocumentPickerResult(urls: nil, error: self.plugin?.errorTemporaryCopyFailed)
                return
            }
        }
        self.plugin?.handleDocumentPickerResult(urls: temporaryUrls, error: nil)
    }
}
