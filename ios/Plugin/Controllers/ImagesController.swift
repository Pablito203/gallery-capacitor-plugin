
import SwiftUI

public class ImagesController : UIViewController {
    lazy var collectionView: UICollectionView = self.makeCollectionView()
    
    public required init() {
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
    
    private func setup() {
        view.addSubview(collectionView)
    }
    
    private func makeCollectionView() -> UICollectionView {
        let layout = UICollectionViewFlowLayout()
        layout.minimumInteritemSpacing = 2
        layout.minimumLineSpacing = 2

        let view = UICollectionView(frame: self.view.frame, collectionViewLayout: layout)
        view.dataSource = self
        view.delegate = self
        view.register(ImageCell.self, forCellWithReuseIdentifier: String(describing: ImageCell.self))

        return view
    }

}

extension ImagesController: UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return 60
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: String(describing: ImageCell.self), for: indexPath) as! ImageCell
        
        cell.backgroundColor = .blue
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {

        let size = (collectionView.bounds.size.width - 3 * 2)
          / 4
        return CGSize(width: 100, height: 100)
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
          cell.textView.text = "aaasdas"
      }
    
}