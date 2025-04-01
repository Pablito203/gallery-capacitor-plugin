import UIKit

class Overlay : UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        setup()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setup() {
        self.backgroundColor = .black
        self.alpha = 0
    }
    
    public func show(_ visible: Bool) {
        if visible {
            self.alpha = 0.4
        } else {
            self.alpha = 0
        }
    }
}
