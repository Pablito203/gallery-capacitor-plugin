
import SwiftUI
import Photos

public class ImagesController : UIViewController {
    lazy var collectionView: UICollectionView = self.makeCollectionView()
    
    var assets: [PHAsset] = []
    var cart: Cart
    let once = Once()
    
    var albumName: String? = nil
    
    public required init(_ cart: Cart) {
        self.cart = cart
        super.init(nibName: nil, bundle: nil)
    }
    
    public required init(_ cart: Cart, _ album: Album) {
        self.cart = cart
        self.albumName = album.albumName
        self.assets = album.imageAssets
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
    
    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
          self.collectionView.collectionViewLayout.invalidateLayout()
        }
    }
    
    open override func viewDidLayoutSubviews() {
        let topViewHeight: CGFloat = 50 + Config.SafeArea.top
        let bottomViewHeight: CGFloat = Config.maximumFilesCount > 1 ? 40 : 0
        
        collectionView.g_updateBottomInset(topViewHeight + bottomViewHeight)
    }
    
    private func setup() {
        view.addSubview(collectionView)
    }
    
    private func makeCollectionView() -> UICollectionView {
        let layout = UICollectionViewFlowLayout()
        layout.minimumInteritemSpacing = 3
        layout.minimumLineSpacing = 3

        let view = UICollectionView(frame: self.view.frame, collectionViewLayout: layout)
        view.dataSource = self
        view.delegate = self
        view.register(ImageCell.self, forCellWithReuseIdentifier: String(describing: ImageCell.self))

        return view
    }
    
    
    func fetchImages() {
        let fetchOptions = PHFetchOptions()
        fetchOptions.predicate = NSPredicate(format: "mediaType == %d", PHAssetMediaType.image.rawValue)
        fetchOptions.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
        
        let allAssets = PHAsset.fetchAssets(with: fetchOptions)

        allAssets.enumerateObjects { (asset, _, _) in
            self.assets.append(asset)
        }
        
        DispatchQueue.main.async {
            self.collectionView.reloadData()
        }
    }
    
    @objc func closeAlbumAction() {
        self.dismiss(animated: false)
    }
}

extension ImagesController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return assets.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String(describing: ImageCell.self), for: indexPath) as! ImageCell
        
        let asset = assets[(indexPath as NSIndexPath).item]
        cell.configure(asset)
        configureCell(cell, indexPath)
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        let size = (collectionView.bounds.size.width - (Config.Grid.columnCount - 1) * Config.Grid.cellSpacing) / Config.Grid.columnCount
        return CGSize(width: size, height: size)
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if Config.maximumFilesCount == 1 && cart.assets.count == 1 { return }
        let item = assets[(indexPath as NSIndexPath).item]
        
        if cart.assets.contains(item) {
            cart.remove(item)
        } else {
            if cart.assets.count >= Config.maximumFilesCount {
                let alert = UIAlertController(title: "Limite de \(Config.maximumFilesCount) arquivos",
                                              message: "Você pode selecionar ate \(Config.maximumFilesCount) arquivos",
                                              preferredStyle: .alert)
                let action = UIAlertAction(title: "OK", style: .default, handler: nil)
                alert.addAction(action)
                
                self.present(alert, animated: true, completion: nil)
                return
            }
            cart.add(item)
        }
        
        configureCells()
    }
    
    func configureCells() {
        for case let cell as ImageCell in collectionView.visibleCells {
            if let indexPath = collectionView.indexPath(for: cell) {
                configureCell(cell, indexPath)
            }
        }
    }
    
    func configureCell(_ cell: ImageCell, _ indexPath: IndexPath) {
        let item = assets[(indexPath as NSIndexPath).item]
        
        if let index = cart.assets.firstIndex(where: { $0.localIdentifier == item.localIdentifier}) {
            cell.setSelected(true)
        } else {
            cell.setSelected(false)
        }
    }
}

extension ImagesController: PageAware {
  func pageDidShow() {
    if self.albumName != nil { return }
      
    once.run {
        self.fetchImages()
    }
  }
}
