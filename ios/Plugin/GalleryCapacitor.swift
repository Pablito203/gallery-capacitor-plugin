import Foundation
import PhotosUI
import Photos
import Capacitor
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
            if #available(iOS 14, *) {
                var configuration = PHPickerConfiguration(photoLibrary: PHPhotoLibrary.shared())
                configuration.selectionLimit = maximumFilesCount
                configuration.filter = .images
                let picker = PHPickerViewController(configuration: configuration)
                picker.delegate = self
                picker.modalPresentationStyle = .fullScreen
                self.presentViewController(picker)
            } else {
                let picker = UIImagePickerController()
                picker.delegate = self
                picker.sourceType = .photoLibrary
                picker.modalPresentationStyle = .fullScreen
                self.presentViewController(picker)
            }
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

extension GalleryCapacitor: UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIPopoverPresentationControllerDelegate {
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismissViewController(picker)
        plugin?.handleDocumentPickerResult(urls: nil, error: nil)
    }

    public func popoverPresentationControllerDidDismissPopover(_ popoverPresentationController: UIPopoverPresentationController) {
        plugin?.handleDocumentPickerResult(urls: nil, error: nil)
    }

    public func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        plugin?.handleDocumentPickerResult(urls: nil, error: nil)
    }

    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
        dismissViewController(picker) {
            if let url = info[.mediaURL] as? URL {
                do {
                    let temporaryUrl = try self.saveTemporaryFile(url)
                    self.plugin?.handleDocumentPickerResult(urls: [temporaryUrl], error: nil)
                } catch {
                    self.plugin?.handleDocumentPickerResult(urls: nil, error: self.plugin?.errorTemporaryCopyFailed)
                }
            } else {
                self.plugin?.handleDocumentPickerResult(urls: nil, error: nil)
            }
        }
    }
}

@available(iOS 14, *)
extension GalleryCapacitor: PHPickerViewControllerDelegate {
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        dismissViewController(picker)
        if results.first == nil {
            self.plugin?.handleDocumentPickerResult(urls: nil, error: nil)
            return
        }
        var temporaryUrls: [URL] = []
        var errorMessage: String?
        let dispatchGroup = DispatchGroup()
        for result in results {
            if errorMessage != nil {
                break
            }
            if result.itemProvider.hasItemConformingToTypeIdentifier(UTType.movie.identifier) {
                dispatchGroup.enter()
                result.itemProvider.loadFileRepresentation(forTypeIdentifier: UTType.movie.identifier, completionHandler: { url, error in
                    defer {
                        dispatchGroup.leave()
                    }
                    if let error = error {
                        errorMessage = error.localizedDescription
                        return
                    }
                    guard let url = url else {
                        errorMessage = self.plugin?.errorUnknown
                        return
                    }
                    do {
                        let temporaryUrl = try self.saveTemporaryFile(url)
                        temporaryUrls.append(temporaryUrl)
                    } catch {
                        errorMessage = self.plugin?.errorTemporaryCopyFailed
                    }
                })
            } else if result.itemProvider.hasItemConformingToTypeIdentifier(UTType.image.identifier) {
                dispatchGroup.enter()
                result.itemProvider.loadFileRepresentation(forTypeIdentifier: UTType.image.identifier, completionHandler: { url, error in
                    defer {
                        dispatchGroup.leave()
                    }
                    if let error = error {
                        errorMessage = error.localizedDescription
                        return
                    }
                    guard let url = url else {
                        errorMessage = self.plugin?.errorUnknown
                        return
                    }
                    do {
                        let temporaryUrl = try self.saveTemporaryFile(url)
                        temporaryUrls.append(temporaryUrl)
                    } catch {
                        errorMessage = self.plugin?.errorTemporaryCopyFailed
                    }
                })
            } else {
                errorMessage = self.plugin?.errorUnsupportedFileTypeIdentifier
            }
        }
        dispatchGroup.notify(queue: .main) {
            if let errorMessage = errorMessage {
                self.plugin?.handleDocumentPickerResult(urls: nil, error: errorMessage)
                return
            }
            self.plugin?.handleDocumentPickerResult(urls: temporaryUrls, error: nil)
        }
    }
}
