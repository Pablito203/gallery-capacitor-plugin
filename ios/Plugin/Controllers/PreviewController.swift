import UIKit
import Photos

public protocol PreviewControllerDelegate: AnyObject {
    func previewController(_ assets: [PHAsset], _ controller: PreviewController)
}

public class PreviewController : UIViewController {
    lazy var topView = makeToolbarView()
    lazy var backButton = makeBackButton()
    lazy var radioImageView: UIImageView = self.makeImageView()
    var radioChecked: Bool = true
  
    lazy var bottomView = makeToolbarView()
    lazy var doneButton = makeDoneButton()
    lazy var collectionView: UICollectionView = self.makeCollectionView()
    var assets: [PHAsset]
    var assetsToRemove: [PHAsset] = []
    public weak var delegate: PreviewControllerDelegate?
    var showToolbar: Bool = true
    
    public required init(_ assets: [PHAsset]) {
        self.assets = assets
        
        super.init(nibName: nil, bundle: nil)
        self.modalPresentationStyle = .fullScreen
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    private func setup() {
        collectionView.backgroundColor = .black
        view.addSubview(collectionView)
        self.setupTopView()
        self.setupBottomView()
    }
  
  
    private func setupTopView() {
      radioImageView.isUserInteractionEnabled = true
      radioImageView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(setCheckedAction)))
      
      [backButton, radioImageView].forEach {
          topView.addSubview($0)
      }

      self.view.addSubview(topView)
      
      Constraint.on(
          topView.leftAnchor.constraint(equalTo: topView.superview!.leftAnchor),
          topView.rightAnchor.constraint(equalTo: topView.superview!.rightAnchor),
          topView.topAnchor.constraint(equalTo: topView.superview!.topAnchor),
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

      radioImageView.layoutIfNeeded()
      self.setChecked()
    }

    
    private func setupBottomView() {
        [doneButton].forEach {
            bottomView.addSubview($0)
        }
        self.view.addSubview(bottomView)
        
        Constraint.on(
            bottomView.leftAnchor.constraint(equalTo: bottomView.superview!.leftAnchor),
            bottomView.rightAnchor.constraint(equalTo: bottomView.superview!.rightAnchor),
            bottomView.bottomAnchor.constraint(equalTo: bottomView.superview!.bottomAnchor),
            bottomView.heightAnchor.constraint(equalToConstant: 40 + Config.SafeArea.bottom)
        )
        
        doneButton.g_pin(on: .top, constant: 10)
        doneButton.g_pin(on: .right, constant: -20)
    }
  
    private func makeImageView() -> UIImageView {
        let imageView = UIImageView()
        imageView.clipsToBounds = true
        imageView.contentMode = .scaleAspectFit

        return imageView
    }
    
    private func makeDoneButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Salvar (\(assets.count))", for: UIControl.State())
        button.addTarget(self, action: #selector(doneAction), for: .touchUpInside)
        
        return button
    }
    
    private func makeToolbarButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setTitleColor(.white, for: .focused)
        button.setTitleColor(.white, for: UIControl.State())
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.titleLabel?.textAlignment = .center
        
        return button
    }
    
    private func makeCollectionView() -> UICollectionView {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        layout.minimumInteritemSpacing = 0
        layout.minimumLineSpacing = 0
        let view = UICollectionView(frame: self.view.frame, collectionViewLayout: layout)
        view.dataSource = self
        view.delegate = self
        view.register(PreviewCell.self, forCellWithReuseIdentifier: String(describing: PreviewCell.self))
        view.isPagingEnabled = true
        
        return view
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
        if radioChecked {
            radioImageView.image = GalleryBundle.image("ic_radio_on")
        } else {
            radioImageView.image = GalleryBundle.image("ic_radio_off")
        }
    }
  
    @objc func backAction() {
        self.dismiss(animated: false)
    }
    
    @objc func setCheckedAction() {
        radioChecked = !radioChecked
        self.setChecked()
      
        let index = Int((collectionView.contentOffset.x + collectionView.bounds.width / 2) / collectionView.bounds.width)
        let asset = assets[index]
      
        if !radioChecked  {
            assetsToRemove.append(asset)
        } else if let index = assetsToRemove.firstIndex(of: asset) {
            assetsToRemove.remove(at: index)
        }
        
        UIView.performWithoutAnimation {
            self.doneButton.setTitle("Salvar (\(assets.count - assetsToRemove.count))", for: UIControl.State())
            self.doneButton.layoutIfNeeded()
        }
    }

    
    @objc func doneAction() {
        for asset in assetsToRemove {
            if let index = assets.firstIndex(of: asset) {
                assets.remove(at: index)
            }
        }
        
        self.delegate?.previewController(assets, self)
    }
}

extension PreviewController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return assets.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String(describing: PreviewCell.self), for: indexPath) as! PreviewCell
        
        let asset = assets[(indexPath as NSIndexPath).item]
        cell.configure(asset, self)
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let size = collectionView.frame.size
        return CGSize(width: size.width, height: size.height)
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
    }
}

extension PreviewController: UICollectionViewDelegate {
    public func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let collectionView = scrollView as! UICollectionView
        
        let index = Int((collectionView.contentOffset.x + collectionView.bounds.width / 2) / collectionView.bounds.width)
        let asset = assets[index]
        self.radioChecked = !assetsToRemove.contains(asset)
        self.setChecked()
    }
}

extension PreviewController: PreviewCellDelegate {
    public func previewImageTap() {
        showToolbar = !showToolbar
        if !showToolbar {
            self.bottomView.alpha = 0
            self.topView.alpha = 0
        } else {
            self.bottomView.alpha = 1
            self.topView.alpha = 1
        }
    }
}
