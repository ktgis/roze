//
//  RouteSummaryTableViewCell.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 24..
//  Copyright © 2017년 kt. All rights reserved.
//

import UIKit

class RouteSummaryTableViewCell: UITableViewCell {

    @IBOutlet weak var arrivingTime: UILabel!
    @IBOutlet weak var takingTime: UILabel!
    @IBOutlet weak var takingDistance: UILabel!
    @IBOutlet weak var cost: UILabel!
    @IBOutlet weak var routeType: UILabel!
    @IBOutlet weak var detailRouteBtn: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
        
        if selected {
            takingTime.textColor = UIColor(hexString: "#dd1f26ff")
            arrivingTime.textColor = UIColor(hexString: "#1e2024ff")
            takingDistance.textColor = UIColor(hexString: "#1e2024ff")
            cost.textColor = UIColor(hexString: "#1e2024ff")
            routeType.textColor = UIColor(hexString: "#1e2024ff")
            detailRouteBtn.image(for: .highlighted)
            
        } else {
            
            takingTime.textColor = UIColor(hexString: "#9b9b9bff")
            arrivingTime.textColor = UIColor(hexString: "#9b9b9bff")
            takingDistance.textColor = UIColor(hexString: "#9b9b9bff")
            cost.textColor = UIColor(hexString: "#9b9b9bff")
            routeType.textColor = UIColor(hexString: "#9b9b9bff")
            detailRouteBtn.image(for: .normal)
        }
       
    }

}
//extension UIColor {
//    public convenience init?(hexString: String) {
//        let r, g, b, a: CGFloat
//
//        if hexString.hasPrefix("#") {
//            let start = hexString.index(hexString.startIndex, offsetBy: 1)
//            let hexColor = String(hexString[start...])
//
//            if hexColor.count == 8 {
//                let scanner = Scanner(string: hexColor)
//                var hexNumber: UInt64 = 0
//
//                if scanner.scanHexInt64(&hexNumber) {
//                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
//                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
//                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
//                    a = CGFloat(hexNumber & 0x000000ff) / 255
//
//                    self.init(red: r, green: g, blue: b, alpha: a)
//                    return
//                }
//            }
//        }
//
//        return nil
//    }
//}


