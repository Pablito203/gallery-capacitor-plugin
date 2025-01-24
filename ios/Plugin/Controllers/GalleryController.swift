import SwiftUI

public class GalleryController : UIViewController {
    lazy var topView = makeTopView()
    lazy var imagesButton = makeImagesButton()
    lazy var albunsButton = makeAlbunsButton()
    
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
    
    private func setup() {
        view.backgroundColor = .white
        topView.addSubview(imagesButton)
        topView.addSubview(albunsButton)
        view.addSubview(topView)
        
        var safeAreaInsetTop: CGFloat = 0
        if #available(iOS 11, *) {
          safeAreaInsetTop = UIApplication.shared.keyWindow?.safeAreaInsets.top ?? 0
        }

        Constraint.on(
          topView.leftAnchor.constraint(equalTo: topView.superview!.leftAnchor),
          topView.rightAnchor.constraint(equalTo: topView.superview!.rightAnchor),
          topView.topAnchor.constraint(equalTo: topView.superview!.topAnchor),
          topView.heightAnchor.constraint(equalToConstant: 40 + safeAreaInsetTop),
          
          imagesButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5),
          imagesButton.heightAnchor.constraint(equalTo: topView.heightAnchor, multiplier: 1),
          albunsButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5),
          albunsButton.heightAnchor.constraint(equalTo: topView.heightAnchor, multiplier: 1)
        )
    
        imagesButton.g_pin(on: .centerY)
        imagesButton.g_pin(on: .left)
        albunsButton.g_pin(on: .centerY)
        albunsButton.g_pin(on: .right)
    }
    
    private func makeTopView() -> UIView {
        let view = UIView()
        view.backgroundColor = .black
        return view
    }
    
    private func makeImagesButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setTitleColor(.white, for: .focused)
        button.setTitleColor(.white, for: UIControl.State())
        button.setTitle("Imagens", for: UIControl.State())
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.titleLabel?.textAlignment = .center
        
        return button
    }
    
    private func makeAlbunsButton() -> UIButton {
        let button = UIButton(type: .system)
        button.setTitleColor(.white, for: .focused)
        button.setTitleColor(.white, for: UIControl.State())
        button.setTitle("Albuns", for: UIControl.State())
        button.titleLabel?.font = UIFont.systemFont(ofSize: 16)
        button.titleLabel?.textAlignment = .center
        
        return button
    }
}