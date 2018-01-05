//
//  TBTView.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 28..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze

class TBTView: UIView {
    
    @IBOutlet weak var firstImage: UIImageView! {
        didSet {
            firstImage.isHidden = true
        }
    }
    @IBOutlet weak var firstDirName: UILabel! {
        didSet {
            guard firstDirName != nil else {
                return
            }
            firstDirName.text = ""
        }
    }
    @IBOutlet weak var firstRemainDistance: UILabel! {
        didSet {
            guard firstRemainDistance != nil else {
                return
            }
            firstRemainDistance.text = ""
        }
    }
    
    @IBOutlet weak var secondImage: UIImageView! {
        didSet {
            secondImage.isHidden = true
        }
    }
    
    @IBOutlet weak var secondRemainDistance: UILabel! {
        didSet {
            guard secondRemainDistance != nil else {
                return
            }
            secondRemainDistance.text = ""
        }
        
    }
    
    /// TBT뷰 업데이트
    func didChangeTurn(turnGuidances: [GTurnGuidance]) {
        if let firstTurnGuidance = turnGuidances.first {
            var firstTrunDirName: String = ""

            let directionNames = firstTurnGuidance.directionNames
            if directionNames.count > 0 {
                firstTrunDirName.append(directionNames.joined(separator: " "))
            } else {
                firstTrunDirName = firstTurnGuidance.nodeName
            }
            
            if let toll = firstTurnGuidance.toll {
                if toll != 0 {
                    let tollFormat = NumberFormatter.localizedString(from: NSNumber(value: toll),
                                                                     number: NumberFormatter.Style.decimal)
                    firstTrunDirName.append("(요금: " + tollFormat + "원)")
                }
            }

            firstDirName.text =  firstTrunDirName
            firstImage.image = UIImage(imageNameWithBundlePath: firstTurnGuidance.turnCode.tbtResourceName!)
//            firstImage.image = UIImage(named: firstTurnGuidance.turnCode.tbtResourceName!)
            firstImage.isHidden = false
            let distanceText = changeDistanceMeasure(value: firstTurnGuidance.nextDistance,
                                                     measureType: .Kilometer)
            firstRemainDistance.text = distanceText
        }
        
        if turnGuidances.count >= 2 {
            let secondTurnGuidance = turnGuidances[1]
//            secondImage.image = UIImage(named: secondTurnGuidance.turnCode.tbtResourceName!)
             secondImage.image = UIImage(imageNameWithBundlePath: secondTurnGuidance.turnCode.tbtResourceName!)
            secondImage.isHidden = false
            secondRemainDistance.text = changeDistanceMeasure(value: secondTurnGuidance.nextDistance,
                                                                           measureType: .Kilometer)
        } else {
            secondImage.image = nil
            secondImage.isHidden = true
            secondRemainDistance.text = ""
        }
    }
    
    /// 현재 위치에서 onTurnChangedEvent 전달된 첫 번째 TBT 까지의 거리를 전달
    func didChangeTurnDistance(distance: Int32) {
        firstRemainDistance.text = changeDistanceMeasure(value: distance,
                                                         measureType: .Kilometer)
    }

}
