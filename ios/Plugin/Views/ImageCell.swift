import UIKit
import Photos

class ImageCell: UICollectionViewCell {

    lazy var textView: UITextView = self.makeTextView()

  // MARK: - Initialization

  override init(frame: CGRect) {
    super.init(frame: frame)

    setup()
  }

  required init?(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: - Setup

  func setup() {
    [textView].forEach {
      self.contentView.addSubview($0)
    }
      
      contentView.backgroundColor = .blue

    textView.g_pinEdges()
  }

  // MARK: - Controls

  private func makeTextView() -> UITextView {
    let textoUI = UITextView()
    textoUI.text = "Images"

    return textoUI
  }
}
