
import SwiftUI
import Photos

protocol AlbumClick: AnyObject {
    func openAlbum(_ album: Album)
}

public class AlbumsController : UIViewController {
    lazy var collectionView: UICollectionView = self.makeCollectionView()
    
    var albums: [Album] = []
    let once = Once()
    var cart: Cart
    var delegate: AlbumClick?
    
    public required init(_ cart: Cart) {
        self.cart = cart
        super.init(nibName: nil, bundle: nil)
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
        view.register(AlbumCell.self, forCellWithReuseIdentifier: String(describing: AlbumCell.self))

        return view
    }
    
    
    func fetchAlbums() {
        let smartAlbumsCollections = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: .any, options: nil)
        let albumsCollections = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: nil)
        
        enumerateCollection(smartAlbumsCollections)
        enumerateCollection(albumsCollections)
        
        DispatchQueue.main.async {
            self.collectionView.reloadData()
        }
    }
    
    func enumerateCollection(_ fetchResult: PHFetchResult<PHAssetCollection>) {
        fetchResult.enumerateObjects { (collection, index, stop) in
            let album = Album(collection: collection)
            if album.imageAssets.count > 0 {
                self.albums.append(album)
            }
        }
    }
}

extension AlbumsController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return albums.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String(describing: AlbumCell.self), for: indexPath) as! AlbumCell
        
        let album = albums[(indexPath as NSIndexPath).item]
        cell.configure(album)
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
      let size = (collectionView.bounds.size.width - (Config.Grid.columnCount - 1) * Config.Grid.cellSpacing) / Config.Grid.columnCount
      return CGSize(width: size, height: size)
    }

    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = albums[(indexPath as NSIndexPath).item]
        
        self.delegate?.openAlbum(item)
    }
}

extension AlbumsController: PageAware {
  func pageDidShow() {
    once.run {
        self.fetchAlbums()
    }
  }
}
