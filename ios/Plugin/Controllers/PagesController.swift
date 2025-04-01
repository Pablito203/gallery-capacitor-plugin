import UIKit

protocol PageAware: AnyObject {
  func pageDidShow()
}

class PagesController: UIViewController {
    
    var controllers: [UIViewController]
    
    lazy var scrollView: UIScrollView = self.makeScrollView()
    lazy var scrollViewContentView: UIView = UIView()
    
    var selectedIndex: Int = 0
    let once = Once()
    var contraintRightAlbum: NSLayoutConstraint?
    
    required init(controllers: [UIViewController]) {
        self.controllers = controllers
        
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setup()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        guard scrollView.frame.size.width > 0 else {
            return
        }
        
        once.run {
            DispatchQueue.main.async {
                self.scrollTo(index: self.selectedIndex, animated: false)
            }
            
            notify()
        }
    }
    
    override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        let index = selectedIndex
        
        coordinator.animate(alongsideTransition: { context in
            self.scrollTo(index: index, animated: context.isAnimated)
        }) { _ in }
        
        super.viewWillTransition(to: size, with: coordinator)
    }
    
    func makeScrollView() -> UIScrollView {
        let scrollView = UIScrollView()
        scrollView.isPagingEnabled = true
        scrollView.isScrollEnabled = false
        scrollView.showsHorizontalScrollIndicator = false
        scrollView.alwaysBounceHorizontal = false
        scrollView.bounces = false
        scrollView.delegate = self
        
        return scrollView
    }
    
    func setup() {
        view.addSubview(scrollView)
        scrollView.addSubview(scrollViewContentView)
        
        if #available(iOS 11.0, *) {
            scrollView.contentInsetAdjustmentBehavior = .never
        }
        
        scrollView.g_pinUpward()
        scrollView.g_pinDownward()
        scrollViewContentView.g_pinEdges()
        
        for (i, controller) in controllers.enumerated() {
            addChild(controller)
            scrollViewContentView.addSubview(controller.view)
            controller.didMove(toParent: self)
            
            controller.view.g_pin(on: .top)
            controller.view.g_pin(on: .bottom)
            controller.view.g_pin(on: .width, view: scrollView)
            controller.view.g_pin(on: .height, view: scrollView)
            
            if i == 0 {
                controller.view.g_pin(on: .left)
            } else {
                controller.view.g_pin(on: .left, view: self.controllers[i-1].view, on: .right)
            }
            
            if i == self.controllers.count - 1 {
                contraintRightAlbum = controller.view.g_pin(on: .right)
            }
        }
    }
    
    func scrollTo(index: Int, animated: Bool) {
        guard !scrollView.isTracking && !scrollView.isDragging && !scrollView.isZooming else {
            return
        }
        
        let point = CGPoint(x: scrollView.frame.size.width * CGFloat(index), y: scrollView.contentOffset.y)
        scrollView.setContentOffset(point, animated: animated)
    }
    
    func updateAndNotify(_ index: Int) {
        guard selectedIndex != index else { return }
        
        selectedIndex = index
        notify()
    }
    
    func notify() {
        if let controller = controllers[selectedIndex] as? PageAware {
            controller.pageDidShow()
        }
    }
    
    func addExtraPage(_ controller: UIViewController) {
        contraintRightAlbum!.isActive = false
        self.controllers.append(controller)
        addChild(controller)
        scrollViewContentView.addSubview(controller.view)
        controller.didMove(toParent: self)
        controller.view.g_pin(on: .top)
        controller.view.g_pin(on: .bottom)
        controller.view.g_pin(on: .width, view: scrollView)
        controller.view.g_pin(on: .height, view: scrollView)
        
        controller.view.g_pin(on: .left, view: self.controllers[controllers.count - 2].view, on: .right)
        controller.view.g_pin(on: .right)
        scrollTo(index: controllers.count - 1, animated: false)
    }
    
    func removeExtraPage() {
        let controller = self.controllers.last!
        self.controllers.remove(at: controllers.count - 1)
        controller.willMove(toParent: nil)
        controller.view.removeFromSuperview()
        controller.removeFromParent()
        contraintRightAlbum!.isActive = true
        refreshImagesController()
        
        scrollTo(index: controllers.count - 1, animated: false)
    }
    
    func refreshImagesController() {
        var imagesController: ImagesController
        if contraintRightAlbum!.isActive {
            imagesController = self.controllers.first as! ImagesController
        } else {
            imagesController = self.controllers.last! as! ImagesController
        }
        imagesController.configureCells()
    }
}

extension PagesController: UIScrollViewDelegate {

  func scrollViewDidScroll(_ scrollView: UIScrollView) {
    let index = Int(round(scrollView.contentOffset.x / scrollView.frame.size.width))
    if index >= controllers.count || index < 0 {
        return
    }
    updateAndNotify(index)
  }
}

