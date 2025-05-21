import SwiftUI
import Photos
import Combine

public protocol GalleryControllerDelegate: AnyObject {
  func galleryController(_ urls: [URL], _ IcloudError: Bool, _ controller: GalleryController)
}

public class GalleryController : UIViewController {
    lazy var topView = makeToolbarView()
    lazy var bottomView = makeToolbarView()
    lazy var imagesButton = makeImagesButton()
    lazy var albunsButton = makeAlbunsButton()
    lazy var doneButton = makeDoneButton()
    lazy var previewButton = makePreviewButton()
    lazy var pagesController = makePagesController()
    lazy var albumLabel = makeAlbumTextView()
    lazy var backButton = makeBackButton()
    
    public var cart = Cart()
    public weak var delegate: GalleryControllerDelegate?
  
    private var IcloudError = false
    
    public required init() {
        super.init(nibName: nil, bundle: nil)
        self.modalPresentationStyle = .fullScreen
        cart.delegate = self
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    private func setup() {
        view.backgroundColor = .white
        setupTopView()
        setupPagesView()
        setupBottomView()
    }
    
    private func setupTopView() {
        [imagesButton, albunsButton, backButton, albumLabel].forEach {
            topView.addSubview($0)
        }
        self.view.addSubview(topView)
        
        Constraint.on(
            topView.leftAnchor.constraint(equalTo: topView.superview!.leftAnchor),
            topView.rightAnchor.constraint(equalTo: topView.superview!.rightAnchor),
            topView.topAnchor.constraint(equalTo: topView.superview!.topAnchor),
            topView.heightAnchor.constraint(equalToConstant: 50 + Config.SafeArea.top),
            
            imagesButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5),
            albunsButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5),
            backButton.widthAnchor.constraint(equalToConstant: 50),
            backButton.heightAnchor.constraint(equalToConstant: 50),
            albumLabel.widthAnchor.constraint(equalTo: topView.widthAnchor, constant: -50),
            albumLabel.heightAnchor.constraint(equalToConstant: 50)
        )
        
        imagesButton.g_pin(on: .top, constant: Config.SafeArea.top)
        imagesButton.g_pin(on: .left)
        albunsButton.g_pin(on: .top, constant: Config.SafeArea.top)
        albunsButton.g_pin(on: .right)
        backButton.g_pin(on: .top, constant: Config.SafeArea.top)
        backButton.g_pin(on: .left)
        albumLabel.g_pin(on: .top, constant: Config.SafeArea.top)
        albumLabel.g_pin(on: .left, view: backButton, on: .right)
        
        changeTopViewButtons(false)
    }
    
    private func setupPagesView() {
        addChild(pagesController)
        let pagesView = pagesController.view!
        view.addSubview(pagesView)
        pagesController.didMove(toParent: self)
        
        pagesView.g_pinDownward()
        pagesView.g_pin(on: .top, view: topView, on: .bottom)
    }
    
    private func makePagesController() -> PagesController {
        let imagesController = ImagesController(cart)
        let albumsController = AlbumsController(cart)
        albumsController.delegate = self
        
        let controllers: [UIViewController] = [imagesController, albumsController]
        
        return PagesController(controllers: controllers)
    }
    
    private func setupBottomView() {
        if Config.maximumFilesCount == 1 { return }
        
        [doneButton, previewButton].forEach {
            bottomView.addSubview($0)
        }
        self.view.addSubview(bottomView)
        
        Constraint.on(
            bottomView.leftAnchor.constraint(equalTo: topView.superview!.leftAnchor),
            bottomView.rightAnchor.constraint(equalTo: topView.superview!.rightAnchor),
            bottomView.bottomAnchor.constraint(equalTo: topView.superview!.bottomAnchor),
            bottomView.heightAnchor.constraint(equalToConstant: 40 + Config.SafeArea.bottom)
        )
        
        doneButton.g_pin(on: .top, constant: 10)
        doneButton.g_pin(on: .right, constant: -20)
        previewButton.g_pin(on: .top, constant: 10)
        previewButton.g_pin(on: .left, constant: 20)
    }
    
    private func makeToolbarView() -> UIView {
        let view = UIView()
        view.backgroundColor = UIColor(red: 20/255, green: 21/255, blue: 23/255, alpha: 1)
        return view
    }
    
    private func makeImagesButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Imagens", for: UIControl.State())
        button.tag = 1
        button.addTarget(self, action: #selector(changeTabAction), for: .touchUpInside)
        
        return button
    }
    
    private func makeAlbunsButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Albuns", for: UIControl.State())
        button.setTitleColor(.lightGray, for: UIControl.State())
        button.tag = 2
        button.addTarget(self, action: #selector(changeTabAction), for: .touchUpInside)
        
        return button
    }
    
    private func makeDoneButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Avançar (0)", for: UIControl.State())
        button.tag = 3
        button.addTarget(self, action: #selector(doneAction), for: .touchUpInside)
        
        return button
    }
    
    private func makePreviewButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Prévia", for: UIControl.State())
        button.addTarget(self, action: #selector(previewAction), for: .touchUpInside)
        button.isHidden = true
        
        return button
    }
    
    private func makeBackButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setImage(UIImage(systemName: "chevron.left"), for: .normal)
        button.addTarget(self, action: #selector(backAction), for: .touchUpInside)
        button.tintColor = .white
        
        return button
    }
    
    private func makeAlbumTextView() -> UILabel {
        let textView = UILabel()
        textView.font = UIFont.systemFont(ofSize: 16)
        textView.textColor = .white
        
        return textView
    }
    
    private func makeToolbarButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setTitleColor(.white, for: .focused)
        button.setTitleColor(.white, for: UIControl.State())
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.titleLabel?.textAlignment = .center
        
        return button
    }
    
    private func getURLFromAssets(_ completion: @escaping ([URL]) -> Void) {
        var urls: [URL] = []
        let dispatchGroup = DispatchGroup()
        
      for asset in cart.assets {
        dispatchGroup.enter()
        let options = PHImageRequestOptions()
        options.isSynchronous = false
        options.isNetworkAccessAllowed = true
        options.deliveryMode = .highQualityFormat
        
        PHImageManager.default().requestImageDataAndOrientation(for: asset, options: options, resultHandler: {(data, uti, orientation, info) in
          defer {
              dispatchGroup.leave()
          }
          
          if let info = info,
             let isInCloud = info[PHImageResultIsInCloudKey] as? Bool,
             isInCloud,
             data == nil {
              self.IcloudError = true
              return
          }

          if let data = data {
            let tempDir = FileManager.default.temporaryDirectory
            let fileURL = tempDir.appendingPathComponent(UUID().uuidString).appendingPathExtension("jpg")
            
            if let image = UIImage(data: data),
               let jpgData = image.jpegData(compressionQuality: 1.0) {
              do {
                try jpgData.write(to: fileURL)
                urls.append(fileURL)
              } catch {}
            }
          }
          
        })
      }
        
      dispatchGroup.notify(queue: .main) {
          completion(urls)
      }
    }
    
    private func changeTopViewButtons(_ albumOpen: Bool) {
        backButton.isHidden = !albumOpen
        albumLabel.isHidden = !albumOpen
        imagesButton.isHidden = albumOpen
        albunsButton.isHidden = albumOpen
    }
    
    private func done() {
        getURLFromAssets() { urls in
            self.delegate?.galleryController(urls, self.IcloudError, self)
        }
    }
    
    @objc func changeTabAction(button: UIButton!) {
        button.setTitleColor(.white, for: UIControl.State())
        if button.tag == 1 {
            albunsButton.setTitleColor(.lightGray, for: UIControl.State())
        } else {
            imagesButton.setTitleColor(.lightGray, for: UIControl.State())
        }
        
        pagesController.scrollTo(index: button.tag - 1, animated: false)
    }
    
    @objc func doneAction() {
        self.done()
    }
    
    @objc func backAction() {
        self.pagesController.removeExtraPage()
        changeTopViewButtons(false)
    }
    
    @objc func previewAction() {
        let previewController = PreviewController(cart.assets)
        previewController.delegate = self
        self.present(previewController, animated: false)
    }
}

extension GalleryController: CartDelegate {
    func cartChanged() {
        if (Config.maximumFilesCount == 1) {
            self.done()
            return
        }
        
        UIView.performWithoutAnimation {
            self.doneButton.setTitle("Avançar (\(cart.assets.count))", for: UIControl.State())
            self.doneButton.layoutIfNeeded()
            if self.previewButton.isHidden != (cart.assets.count == 0) {
                self.previewButton.isHidden = cart.assets.count == 0
                self.previewButton.layoutIfNeeded()
            }
        }
    }
}

extension GalleryController: AlbumClick {
    func openAlbum(_ album: Album) {
        self.pagesController.addExtraPage(ImagesController(cart, album))
        albumLabel.text = album.albumName
        changeTopViewButtons(true)
    }
}

extension GalleryController: PreviewControllerDelegate {
    public func previewController(_ assets: [PHAsset], _ controller: PreviewController) {
        self.cart.refreshCart(assets)
        self.pagesController.refreshImagesController()
        controller.dismiss(animated: false)
    }
}
