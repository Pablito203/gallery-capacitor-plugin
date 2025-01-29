import SwiftUI

public class GalleryController : UIViewController {
    lazy var topView = makeToolbarView()
    lazy var bottomView = makeToolbarView()
    lazy var imagesButton = makeImagesButton()
    lazy var albunsButton = makeAlbunsButton()
    lazy var doneButton = makeDoneButton()
    lazy var previewButton = makePreviewButton()
    
    public required init() {
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
        view.backgroundColor = .white
        setupTopView()
        setupImagesView()
        setupBottomView()
    }
    
    private func setupTopView() {
        topView.addSubview(imagesButton)
        topView.addSubview(albunsButton)
        self.view.addSubview(topView)
        
        Constraint.on(
            topView.leftAnchor.constraint(equalTo: topView.superview!.leftAnchor),
            topView.rightAnchor.constraint(equalTo: topView.superview!.rightAnchor),
            topView.topAnchor.constraint(equalTo: topView.superview!.topAnchor),
            topView.heightAnchor.constraint(equalToConstant: 50 + Config.SafeArea.top),
            
            imagesButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5),
            albunsButton.widthAnchor.constraint(equalTo: topView.widthAnchor, multiplier: 0.5)
        )
        
        imagesButton.g_pin(on: .top, constant: Config.SafeArea.top)
        imagesButton.g_pin(on: .left)
        albunsButton.g_pin(on: .top, constant: Config.SafeArea.top)
        albunsButton.g_pin(on: .right)
    }
    
    private func setupImagesView() {
        let imagesController = ImagesController()
        let imagesView = imagesController.view!
        view.addSubview(imagesView)
        
        Constraint.on(
            imagesView.heightAnchor.constraint(equalToConstant: view.frame.size.height - (50 + Config.SafeArea.top) - (40 + Config.SafeArea.bottom))
        )
        imagesView.g_pin(on: .top, view: topView, on: .bottom)
    }
    
    private func setupBottomView() {
        bottomView.addSubview(doneButton)
        bottomView.addSubview(previewButton)
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
        view.backgroundColor = .black
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
        
        return button
    }
    
    private func makePreviewButton() -> UIButton {
        let button = makeToolbarButton()
        button.setTitle("Prévia", for: UIControl.State())
        
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
    
    @objc func changeTabAction(button: UIButton!) {
        button.setTitleColor(.white, for: UIControl.State())
        if button.tag == 1 {
            albunsButton.setTitleColor(.lightGray, for: UIControl.State())
        } else {
            imagesButton.setTitleColor(.lightGray, for: UIControl.State())
        }
    }
}