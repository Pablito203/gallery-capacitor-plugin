import UIKit
import SwiftUI
import Photos

class AlbumCell: UICollectionViewCell {
    lazy var imageView: UIImageView = self.makeImageView()
    lazy var label: UILabel = self.makeLabel()
    lazy var labelView: UIView = self.makeLabelView()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(_ album: Album) {
        imageView.layoutIfNeeded()
        imageView.g_loadImage(album.imageAssets.first!)
        label.text = album.albumName
        label.layoutIfNeeded()
        if let gradientLayer = labelView.layer.sublayers?.first(where: { $0 is CAGradientLayer }) as? CAGradientLayer {
            gradientLayer.frame = label.bounds
        }
    }

    func setup() {
        [imageView, labelView].forEach {
            self.contentView.addSubview($0)
        }
        
        labelView.addSubview(label)
        
        imageView.g_pinEdges()
        labelView.g_pinDownward()
        label.g_pinEdges()
    }
    
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFill

        return imageView
    }
    private func makeLabelView() -> UIView {
        let labelView = UIView()
        
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = [UIColor.black.withAlphaComponent(0).cgColor, UIColor.black.withAlphaComponent(1).cgColor]
        gradientLayer.locations = [0.0, 1.0]
    
        labelView.layer.insertSublayer(gradientLayer, at: 0)
        
        return labelView
    }
    
    private func makeLabel() -> UILabel {
        let label = UILabel()
        label.numberOfLines = 1
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        
        return label
    }
}
