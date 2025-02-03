import UIKit
import Photos

class ImageCell: UICollectionViewCell {
    
	
    lazy var imageView: UIImageView = self.makeImageView()
    
    // MARK: - Initialization
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(_ asset: PHAsset) {
        imageView.layoutIfNeeded()
        imageView.g_loadImage(asset)
    }
    

  // MARK: - Setup

    func setup() {
        [imageView].forEach {
            self.contentView.addSubview($0)
        }
    }

  // MARK: - Controls

    private func makeTextView() -> UITextView {
        let textoUI = UITextView()
        textoUI.text = "Images"

        return textoUI
    }
    
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFill

        return imageView
    }
}
