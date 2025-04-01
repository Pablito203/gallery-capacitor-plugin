import Photos

protocol CartDelegate: AnyObject {
  func cartChanged()
}

public class Cart {
    public var assets: [PHAsset] = []
    var delegate: CartDelegate?
    
    
    public func add(_ asset: PHAsset) {
        guard !assets.contains(asset) else { return }
        
        assets.append(asset)
        delegate?.cartChanged()
    }
    
    public func remove(_ asset: PHAsset) {
        guard let index = assets.firstIndex(of: asset) else { return }
        
        assets.remove(at: index)
        delegate?.cartChanged()
    }
    
    public func refreshCart(_ assets: [PHAsset]) {
        self.assets = assets
        delegate?.cartChanged()
    }
}
