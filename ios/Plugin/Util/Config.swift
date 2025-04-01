import SwiftUI

public struct Config {
    public static var maximumFilesCount = 4
    
    public struct SafeArea {
        public static var top = UIApplication.shared.keyWindow?.safeAreaInsets.top ?? 0
        public static var bottom = UIApplication.shared.keyWindow?.safeAreaInsets.bottom ?? 0
    }
    
    public struct Grid {
        public static var columnCount: CGFloat = 3
        public static var cellSpacing: CGFloat = 3
    }
}
