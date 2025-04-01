import UIKit
import Photos

class ImageCell: UICollectionViewCell {
    lazy var imageView: UIImageView = self.makeImageView()
    lazy var radioImageView: UIImageView = self.makeImageView()
    lazy var overlay: Overlay = self.makeOverlay()
    var circleLayer: CAShapeLayer?
    
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
        
        if Config.maximumFilesCount == 1 { return }
        
        radioImageView.layoutIfNeeded()
        radioImageView.image = GalleryBundle.image("ic_radio_off")
        
        if circleLayer == nil {
            circleLayer = CAShapeLayer()
            let circleDiameter = radioImageView.frame.width * 0.6
            let circlePath = UIBezierPath(ovalIn: CGRect(
                x: (radioImageView.frame.width - circleDiameter) / 2,
                y: (radioImageView.frame.height - circleDiameter) / 2,
                width: circleDiameter, height: circleDiameter))
            circleLayer!.path = circlePath.cgPath
            circleLayer!.fillColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5).cgColor
            radioImageView.layer.addSublayer(circleLayer!)
        }
    }

    func setup() {
        [imageView, overlay, radioImageView].forEach {
            self.contentView.addSubview($0)
        }
        
        Constraint.on(
            radioImageView.widthAnchor.constraint(equalTo: imageView.widthAnchor, multiplier: 0.25),
            radioImageView.heightAnchor.constraint(equalTo: imageView.widthAnchor, multiplier: 0.25)
        )
        
        imageView.g_pinEdges()
        overlay.g_pinEdges()
    }
    
    
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFill

        return imageView
    }
    private func makeOverlay() -> Overlay {
        let overlay = Overlay()

        return overlay
    }
    
    public func setSelected(_ selected: Bool) {
        if Config.maximumFilesCount == 1 { return }
        if selected {
            circleLayer?.isHidden = true
            overlay.show(true)
            radioImageView.image = GalleryBundle.image("ic_radio_on")
        } else {
            circleLayer?.isHidden = false
            overlay.show(false)
            radioImageView.image = GalleryBundle.image("ic_radio_off")
        }
    }
}
