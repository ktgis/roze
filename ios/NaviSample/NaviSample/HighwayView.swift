//
//  HighwayView.swift
//  rozeNavi
//
//  Created by 서유리 on 2017. 7. 21..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze

class HighwayView: UIStackView {
    @IBOutlet weak var firstHighway: UIView!
    @IBOutlet weak var secondHighway: UIView!
    @IBOutlet weak var thirdHighway: UIView!
    
    @IBOutlet weak var firstHighwayDirectionLabel: UILabel! {
        didSet {
            firstHighwayDirectionLabel.text = ""
        }
    }
    @IBOutlet weak var firstHighwayTollLabel: UILabel! {
        didSet {
            firstHighwayTollLabel.text = ""
            firstHighwayTollLabel.isHidden = true
        }
    }
    @IBOutlet weak var firstHighwayRemainLabel: UILabel! {
        didSet {
            firstHighwayRemainLabel.text = ""
        }
    }
    
    @IBOutlet weak var secondHighwayDirectionLabel: UILabel! {
        didSet {
            secondHighwayDirectionLabel.text = ""
        }
    }
    @IBOutlet weak var secondHighwayTollLabel: UILabel! {
        didSet {
            secondHighwayTollLabel.text = ""
            secondHighwayTollLabel.isHidden = true
        }
    }
    @IBOutlet weak var secondHighwayRemainLabel: UILabel! {
        didSet {
            secondHighwayRemainLabel.text = ""
        }
    }
    
    @IBOutlet weak var thirdHighwayDirectionLabel: UILabel! {
        didSet {
            thirdHighwayDirectionLabel.text = ""
        }
    }
    @IBOutlet weak var thirdHighwayTollLabel: UILabel! {
        didSet {
            thirdHighwayTollLabel.text = ""
            thirdHighwayTollLabel.isHidden = true
        }
    }
    @IBOutlet weak var thirdHighwayRemainLabel: UILabel! {
        didSet {
            thirdHighwayRemainLabel.text = ""
        }
    }
    @IBOutlet weak var firstTrafficInfo: UIView!
    @IBOutlet weak var secondTrafficInfo: UIView!
    @IBOutlet weak var thirdTrafficInfo: UIView!
    //@IBOutlet weak var highwayLaneView: LaneView!

    
    private var guidances: [GHighwayGuidance]?
    private var firstDistance: Int32 = 0
    private var secondDistance: Int32 = 0
    private var thirdDistance: Int32 = 0
    
    override func layoutSubviews() {
        super.layoutSubviews()

    }
    
    func setup(view: UIView) {

        let r = 10.0
        let path = UIBezierPath(roundedRect: view.bounds,
                                byRoundingCorners: [.topLeft, .bottomLeft],
                                cornerRadii: CGSize(width: r, height: r))
        let mask = CAShapeLayer()
        mask.path = path.cgPath
        view.layer.mask = mask
    }
    
    /// 고속도로 안내점 정보를 개별 View에 전달
    func setHighwayGuidances(guidance: [GHighwayGuidance]) {
        firstHighway.isHidden = true
        secondHighway.isHidden = true
        thirdHighway.isHidden = true
        self.guidances = guidance
        
        for i in 0..<guidance.count {
            let highwayGuidance = guidance[i]
            setHighwayItems(index: i, hg: highwayGuidance, hide: false)
        }
    }
    
  
    /// 개별 view에 고속도로 안내점 정보를 Set
    /// index - 리스트의 고속도로 안내점 index
    /// hg - 고속도로 안내점 정보
    func setHighwayItems(index: Int, hg: GHighwayGuidance, hide: Bool) {
        var highwayStr = ""
        
        if hg.type == .tg {
            //Highway 요금 정보
            let tgGuidance = hg as! GTGGuidance
            let tollcost = tgGuidance.toll
            if  tollcost != 0 {
                highwayStr.append(String(format: " 요금:%d원", tollcost))
            }
        } else if hg.type == .ra {
            highwayStr = "졸음쉼터"
        }
        switch index {
        case 0:
            firstDistance = hg.distance
            if let extraStr = setExtraData(hg: hg) {
                highwayStr.append(extraStr)
            }
            firstHighwayDirectionLabel.text = hg.name
            firstHighwayTollLabel.text = highwayStr
            firstHighwayTollLabel.isHidden = !(highwayStr.count > 0)
            //Todo : set extraData
            firstTrafficInfo.backgroundColor = updateHighwayTrafficColor(trafficInfo: hg.trafficInfo.rawValue)
            firstHighway.isHidden = hide
        case 1:
            
            if let extraStr = setExtraData(hg: hg) {
                highwayStr.append(extraStr)
            }
            secondHighwayDirectionLabel.text = hg.name
            secondDistance = hg.distance
            secondHighwayTollLabel.text = highwayStr
            secondHighwayTollLabel.isHidden = !(highwayStr.count > 0)
            secondTrafficInfo.backgroundColor = updateHighwayTrafficColor(trafficInfo: hg.trafficInfo.rawValue)
            secondHighway.isHidden = hide
        case 2:
            if let extraStr = setExtraData(hg: hg) {
                highwayStr.append(extraStr)
            }
            thirdHighwayDirectionLabel.text = hg.name
            thirdDistance = hg.distance
            thirdHighwayTollLabel.text = highwayStr
            thirdHighwayTollLabel.isHidden = !(highwayStr.count > 0)
            thirdTrafficInfo.backgroundColor = updateHighwayTrafficColor(trafficInfo: hg.trafficInfo.rawValue)
            thirdHighway.isHidden = hide
        default:
            break
        }
        
    }
    /// 교통정보 반영
    func updateHighwayTrafficColor(trafficInfo: Int16) -> UIColor {
        switch trafficInfo {
        case 1:
            //정체
            return UIColor.red
        case 2:
            //서행
            return UIColor.yellow
        case 3:
            //원할
            return UIColor.green
        default:
            return UIColor.lightGray
        }
    }
    
    /// 휴계소 정보를 View에 Set한다.
    ///- hg 고속도로 정보
    func setExtraData(hg: GHighwayGuidance) -> String? {
        if hg.type != .sa {
            return nil
        }
        
        let sa = hg as! GSAGuidance
        guard let gasStations = sa.gasStations else {
            return nil
        }
        for gasSt in gasStations {
            if gasSt.price != -1 {
                return "\(String(gasSt.price))원"
            }
        }
        
        return nil
    }
 
    private func setImage(imageName: String) -> UIImage {
//        return UIImage(named: imageName)!
        return UIImage(imageNameWithBundlePath: imageName)!
    }
    
    /// 현재 위치에서 각 고속도로 안내점까지의 거리를 표시
    /// 라이브러리에서는 현재 위치에서 첫번째 아이템까지의 거리만 전달한다.
    /// 첫번째 이후 안내점은 전달된 거리를 이용하여 변화량을 계산하여 App에서 계산하도록 한다.
    public func updateDistance(distance: Int32) {
        let remainDistance = firstDistance - distance
        
        //hipass lane info moved to TurnGuidance
//        if let gds = guidances {
//            if !gds.isEmpty && distance < 600 {
//                let firstGuidance = gds[0]
//                if firstGuidance.type == .tg {
//                    let tg = firstGuidance as! TGGuidance
//                    updateHipassLanes(tg: tg)
//                }
//            } else {
//                updateHipassLanes(tg: nil)
//            }
//        } else {
//            updateHipassLanes(tg: nil)
//        }
        
        firstHighwayRemainLabel.text = changeDistanceMeasure(value: distance, measureType: .Kilometer)
        secondHighwayRemainLabel.text = changeDistanceMeasure(value: (secondDistance - remainDistance), measureType: .Kilometer)
        thirdHighwayRemainLabel.text = changeDistanceMeasure(value: (thirdDistance - remainDistance), measureType: .Kilometer)
        
    }
}
    
