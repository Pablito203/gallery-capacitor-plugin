import UIKit
import Foundation

class GalleryBundle {

  static func image(_ named: String) -> UIImage? {
      let bundle = Bundle.module
      return UIImage(named: named, in: bundle, compatibleWith: nil)
  }
}

extension Bundle {
    static var module: Bundle = {
        Bundle(for: GalleryCapacitorPlugin.self)
    }()
}
