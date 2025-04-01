import Photos

public class Album {
    var imageAssets: [PHAsset] = []
    let albumName: String
    
    init(collection: PHAssetCollection) {
        self.albumName = collection.localizedTitle ?? ""
        let itemsFetchResult = PHAsset.fetchAssets(in: collection, options: nil)
        itemsFetchResult.enumerateObjects { (asset, count, stop) in
            if asset.mediaType == .image {
                self.imageAssets.append(asset)
            }
        }
    }
}
