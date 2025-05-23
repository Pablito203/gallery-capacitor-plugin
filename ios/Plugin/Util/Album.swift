import Photos

public class Album {
    var imageAssets: [PHAsset] = []
    let albumName: String
    
    init(collection: PHAssetCollection) {
        self.albumName = collection.localizedTitle ?? ""
        let fetchOptions = PHFetchOptions()
        fetchOptions.predicate = NSPredicate(format: "mediaType == %d", PHAssetMediaType.image.rawValue)
        fetchOptions.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]

        let itemsFetchResult = PHAsset.fetchAssets(in: collection, options: fetchOptions)
        itemsFetchResult.enumerateObjects { (asset, count, stop) in
            self.imageAssets.append(asset)
        }
    }
}
