//
//  LaneView.swift
//  rozeNavi
//
//  Created by 서유리 on 2017. 7. 13..
//  Copyright © 2017년 kt. All rights reserved.
//

import UIKit
import roze

class LaneView: UIView {
//    @IBOutlet weak var laneImagePannelView: UIView!
    
    private static let nonHipassLableString = "..."
    
    @IBOutlet weak var laneViewConstraint: NSLayoutConstraint!
    @IBOutlet weak var highwaylaneViewConstraint: NSLayoutConstraint!
    private static let laneWidth: CGFloat = 22.0
    override init(frame: CGRect) {
        super.init(frame: frame)
        //        self.frame = frame
    }
    
    required init?(coder aDecoder: NSCoder) {
        //        fatalError("init(coder:) has not been implemented")
        super.init(coder: aDecoder)
    }
    
    /// 차선 정보 표시
    /// Lane으로 부터 방향 타입(좌회전, 우회전 등)과 확장타입(고가, 버스전용, 지하차로 진,출입 등)을 받아
    /// 각각의 차선 이미지 Resource Id를 {@link LaneResourceManager}에서 가져와 이미지 View에 추가한다.
    func updateLanePannel(lane: GLane) -> Bool {
        let turnType = lane.turnType
        let exType = lane.exType
        var resList = [String]()
        
        let size = turnType.count
        guard size == exType.count else {
            return false
        }
        
        for i in 0..<size {
            if let resName = getLaneResource(exType: exType[i], turnType: turnType[i]) {
                resList.append(resName)
            }
        }
        
        if !resList.isEmpty {
            addLaneImage(resList: resList)
        }
        
        return true
    }
    
    /// HIPASS 차로 정보를 View에 set
    /// hipass lane info moved to TurnGuidance
    func updateHipassLanes(hipassLanes: Data, laneCnt: Int) -> Bool {
        
        let lanes: [UInt8] = Array(hipassLanes)
        var laneData: [Int] = Array(repeating: -1, count: laneCnt)
        for lane in lanes {
            let laneInt = Int(lane)
            laneData[laneInt] = laneInt+1
        }
        
        addHipassLaneLabels(hipassLaneData: laneData)
        return !self.subviews.isEmpty
     }
 
    
    /// 입력된 값에 따라 차선 이미지 ResName을 반환
    func getLaneResource(exType: GExtraLaneType, turnType: GLaneType) -> String? {
        
        if let resName: String = exType.resourceName {
            return resName
        } else {
            return exType.isHighlight ? turnType.highlightResourceName : turnType.resourceName
        }
        
    }
    
    /// 차로에 속한 모든 차선 이미지를 View에 추가
    func addLaneImage(resList: [String]) {
        

        self.subviews.forEach ({
            $0.removeFromSuperview()
        })
        let resListCount = resList.count
        laneViewConstraint.constant = CGFloat(resListCount) * LaneView.laneWidth + CGFloat(5.0)
        self.layoutIfNeeded()
        for i in 0..<resListCount {
            let laneImageView = UIImageView(image: UIImage(imageNameWithBundlePath: resList[i]))
//            let laneImageView = UIImageView(image: UIImage(named: resList[i]))
            self.addSubview(laneImageView)
            laneImageView.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint(item: laneImageView, attribute: .leading,
                               relatedBy: .equal,
                               toItem: self, attribute: .leading,
                               multiplier: 1.0,
                               constant: CGFloat(i)*LaneView.laneWidth).isActive = true
            NSLayoutConstraint(item: laneImageView, attribute: .top,
                               relatedBy: .equal,
                               toItem: self, attribute: .top,
                               multiplier: 1.0,
                               constant: 0.0).isActive = true
            NSLayoutConstraint(item: laneImageView, attribute: .width,
                               relatedBy: .greaterThanOrEqual,
                               toItem: self, attribute: .width,
                               multiplier: 7/120,
                               constant: LaneView.laneWidth).isActive = true
            NSLayoutConstraint(item: laneImageView, attribute: .height,
                               relatedBy: .equal,
                               toItem: self, attribute: .height,
                               multiplier: 1.0,
                               constant: 0.0).isActive = true

        }
    }
    
    /// create and append hipass info icon and labels
    ///
    /// - Parameter highwayLaneData: hipass lane data
    /// 예를 들어서 10 차선까지 있고 1,2,7,8,9 가 하이패스라면
    /// [1, 2, -1, -1, -1, -1, 7, 8, 9, -1]
    func addHipassLaneLabels(hipassLaneData: [Int]) {

        self.subviews.forEach ({
            $0.removeFromSuperview()
        })
        
        var labelArray = [UILabel]()
        for laneData in hipassLaneData {
            if let lastLabel = labelArray.last,
                LaneView.nonHipassLableString == lastLabel.text,
                laneData == -1 {
                continue
            }
            
            let laneLabel = UILabel()
            laneLabel.textAlignment = NSTextAlignment.center
            
            if laneData == -1 {
                laneLabel.text = LaneView.nonHipassLableString
            } else {
                laneLabel.text = String(laneData)
                laneLabel.font = UIFont.systemFont(ofSize: 20.0)
                laneLabel.textColor = UIColor(red: CGFloat(52 / 255.0),
                                              green: CGFloat(120 / 255.0),
                                              blue: CGFloat(246 / 255.0),
                                              alpha: CGFloat(1.0))
            }
            
            labelArray.append(laneLabel)
        }

        if labelArray.last!.text! == LaneView.nonHipassLableString {
            labelArray.removeLast()
        }
        
        highwaylaneViewConstraint.constant = CGFloat(labelArray.count + 1) * LaneView.laneWidth + CGFloat(10.0)
        self.layoutIfNeeded()

        let highwayLaneicon = UIImageView(image: UIImage(imageNameWithBundlePath: "lane_hipass"))
        self.addSubview(highwayLaneicon)
        highwayLaneicon.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint(item: highwayLaneicon, attribute: .leading,
                           relatedBy: .equal,
                           toItem: self, attribute: .leading,
                           multiplier: 1.0,
                           constant: 0.0).isActive = true
        NSLayoutConstraint(item: highwayLaneicon, attribute: .top,
                           relatedBy: .equal,
                           toItem: self, attribute: .top,
                           multiplier: 1.0,
                           constant: 0.0).isActive = true
        NSLayoutConstraint(item: highwayLaneicon, attribute: .width,
                           relatedBy: .greaterThanOrEqual,
                           toItem: self, attribute: .width,
                           multiplier: 7/120,
                           constant: LaneView.laneWidth).isActive = true
        NSLayoutConstraint(item: highwayLaneicon, attribute: .height,
                           relatedBy: .equal,
                           toItem: self, attribute: .height,
                           multiplier: 1.0,
                           constant: 0.0).isActive = true
        
        
        //for i in 0..<highwayLaneCount {
        for (i, highwayLabel) in labelArray.enumerated() {
            self.addSubview(highwayLabel)
            highwayLabel.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint(item: highwayLabel, attribute: .leading,
                               relatedBy: .equal,
                               toItem: highwayLaneicon, attribute: .trailing,
                               multiplier: 1.0,
                               constant: CGFloat(i)*LaneView.laneWidth).isActive = true
            NSLayoutConstraint(item: highwayLabel, attribute: .top,
                               relatedBy: .equal,
                               toItem: self, attribute: .top,
                               multiplier: 1.0,
                               constant: 0.0).isActive = true
            NSLayoutConstraint(item: highwayLabel, attribute: .width,
                               relatedBy: .greaterThanOrEqual,
                               toItem: self, attribute: .width,
                               multiplier: 7/120,
                               constant: LaneView.laneWidth).isActive = true
            NSLayoutConstraint(item: highwayLabel, attribute: .height,
                               relatedBy: .equal,
                               toItem: self, attribute: .height,
                               multiplier: 1.0,
                               constant: 0.0).isActive = true
        }
    }
}
