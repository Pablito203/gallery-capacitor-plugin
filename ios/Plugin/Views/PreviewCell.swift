import UIKit
import Photos

public protocol PreviewCellDelegate: AnyObject {
    func previewImageTap()
}

class PreviewCell: UICollectionViewCell {
    lazy var imageView: UIImageView = self.makeImageView()
    var delegate: PreviewCellDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(_ asset: PHAsset, _ controller: PreviewCellDelegate) {
        imageView.layoutIfNeeded()
        imageView.g_loadImage(asset)
        delegate = controller
    }

    func setup() {
        [imageView].forEach {
            self.contentView.addSubview($0)
        }
        
        imageView.g_pinEdges()
    }
    
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFit
        imageView.isUserInteractionEnabled = true
        imageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(imageTap)))

        return imageView
    }
    
    private func makeToolbarView() -> UIView {
        let view = UIView()
        view.backgroundColor = .black.withAlphaComponent(0.5)
        return view
    }
    
    @objc func imageTap() {
        delegate?.previewImageTap()
    }

}
