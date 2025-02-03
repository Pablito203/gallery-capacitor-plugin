
import SwiftUI
import Photos

public class ImagesController : UIViewController {
    lazy var collectionView: UICollectionView = self.makeCollectionView()
    
    var assets: [PHAsset] = []
    
    public required init() {
        super.init(nibName: nil, bundle: nil)
        PHPhotoLibrary.requestAuthorization { status in
            switch status {
            case .authorized:
                self.fetchImages()
            case .denied, .restricted:
                print("Access to photo library is denied or restricted.")
            case .notDetermined:
                print("Authorization status not determined.")
            default:
                break
            }
        }
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
        
        let allAssets = PHAsset.fetchAssets(with: fetchOptions)

        allAssets.enumerateObjects { (asset, _, _) in
            self.assets.append(asset)
            self.assets.append(asset)
            self.assets.append(asset)
            self.assets.append(asset)
            self.assets.append(asset)
            self.assets.append(asset)
        }
        
        collectionView.reloadData()
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
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
      let size = (collectionView.bounds.size.width - (Config.Grid.columnCount - 1) * Config.Grid.cellSpacing) / Config.Grid.columnCount
      return CGSize(width: size, height: size)
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {

    }
    
    func configureFrameViews() {
      for case let cell as ImageCell in collectionView.visibleCells {
        if let indexPath = collectionView.indexPath(for: cell) {
          configureFrameView(cell, indexPath: indexPath)
        }
      }
    }

    func configureFrameView(_ cell: ImageCell, indexPath: IndexPath) {
    }
    
}
