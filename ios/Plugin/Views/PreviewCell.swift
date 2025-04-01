import UIKit
import Photos

public protocol PreviewCellDelegate: AnyObject {
    func previewBack()
    func previewChecked(_ asset: PHAsset, _ checked: Bool)
    func previewImageTap()
}

class PreviewCell: UICollectionViewCell {
    lazy var imageView: UIImageView = self.makeImageView()
    lazy var radioImageView: UIImageView = self.makeImageView()
    lazy var topView = self.makeToolbarView()
    lazy var backButton = makeBackButton()
    var delegate: PreviewCellDelegate?
    var asset: PHAsset?
    var checked: Bool = true
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func configure(_ asset: PHAsset, _ checked: Bool, _ controller: PreviewCellDelegate) {
        imageView.layoutIfNeeded()
        imageView.g_loadImage(asset)
        delegate = controller
        self.asset = asset
        self.checked = checked
        
        radioImageView.layoutIfNeeded()
        self.setChecked()
    }

    func setup() {
        [imageView].forEach {
            self.contentView.addSubview($0)
        }
        
        imageView.g_pinEdges()
        self.setupTopView()
    }
    
    private func setupTopView() {
        radioImageView.isUserInteractionEnabled = true
        radioImageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(setCheckedAction)))
        
        imageView.isUserInteractionEnabled = true
        imageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(imageTap)))
        
        [backButton, radioImageView].forEach {
            topView.addSubview($0)
        }
        self.contentView.addSubview(topView)
        
        Constraint.on(
            topView.leftAnchor.constraint(equalTo: contentView.leftAnchor),
            topView.rightAnchor.constraint(equalTo: contentView.rightAnchor),
            topView.topAnchor.constraint(equalTo: contentView.topAnchor),
            topView.heightAnchor.constraint(equalToConstant: 50 + Config.SafeArea.top),
            
            backButton.widthAnchor.constraint(equalToConstant: 50),
            backButton.heightAnchor.constraint(equalToConstant: 50),
            radioImageView.widthAnchor.constraint(equalToConstant: 40),
            radioImageView.heightAnchor.constraint(equalToConstant: 40)
        )
        
        radioImageView.g_pin(on: .top, constant: Config.SafeArea.top + 5)
        radioImageView.g_pin(on: .right, constant: -20)
        backButton.g_pin(on: .top, constant: Config.SafeArea.top)
        backButton.g_pin(on: .left)
    }
    
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFit

        return imageView
    }
    
    private func makeToolbarView() -> UIView {
        let view = UIView()
        view.backgroundColor = .black.withAlphaComponent(0.5)
        return view
    }
    
    private func makeBackButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "chevron.left"), for: .normal)
        button.addTarget(self, action: #selector(backAction), for: .touchUpInside)
        button.tintColor = .white
        
        return button
    }
    
    private func setChecked() {
        if checked {
            
            radioImageView.image = GalleryBundle.image("ic_radio_on")
        } else {
            radioImageView.image = GalleryBundle.image("ic_radio_off")
        }
    }
    
    @objc func backAction() {
        delegate?.previewBack()
    }
    
    @objc func setCheckedAction() {
        checked = !checked
        self.setChecked()
        delegate?.previewChecked(asset!, checked)
    }
    
    @objc func imageTap() {
        delegate?.previewImageTap()
    }

}
