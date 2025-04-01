import UIKit
import Photos

public protocol PreviewControllerDelegate: AnyObject {
    func previewController(_ assets: [PHAsset], _ controller: PreviewController)
}

public class PreviewController : UIViewController {
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
        self.setupBottomView()
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
        cell.configure(asset, !assetsToRemove.contains(asset), self)
        if (showToolbar) {
            self.configureCellsAlpha(1)
        } else {
            self.configureCellsAlpha(0)
        }
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let size = collectionView.frame.size
        return CGSize(width: size.width, height: size.height)
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        
    }
    
    func configureCellsAlpha(_ alpha: CGFloat) {
        for case let cell as PreviewCell in collectionView.visibleCells {
            cell.topView.alpha = alpha
        }
    }
}

extension PreviewController: PreviewCellDelegate {
    public func previewBack() {
        self.dismiss(animated: false)
    }
    
    public func previewChecked(_ asset: PHAsset, _ selected: Bool) {
        if !selected  {
            assetsToRemove.append(asset)
        } else if let index = assetsToRemove.firstIndex(of: asset) {
            assetsToRemove.remove(at: index)
        }
        
        UIView.performWithoutAnimation {
            self.doneButton.setTitle("Salvar (\(assets.count - assetsToRemove.count))", for: UIControl.State())
            self.doneButton.layoutIfNeeded()
        }
    }
    
    public func previewImageTap() {
        showToolbar = !showToolbar
        if !showToolbar {
            self.bottomView.alpha = 0
            self.configureCellsAlpha(0)
        } else {
            self.bottomView.alpha = 1
            self.configureCellsAlpha(1)
        }
    }
}
