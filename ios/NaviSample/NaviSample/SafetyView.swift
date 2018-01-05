//
//  SafetyView.swift
//  rozeNavi
//
//  Created by 서유리 on 2017. 8. 4..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze
import RxSwift

class SafetyView: UIView {
    
    //Interval
    @IBOutlet weak var limitSpeed: UILabel! {
        didSet {
            guard limitSpeed != nil else {
                return
            }
            limitSpeed.text = ""
        }
    }
    @IBOutlet weak var spotImageView: UIImageView! {
        didSet {
            spotImageView.isHidden = true
        }
    }
    @IBOutlet weak var safetyRemainDistance: UILabel! {
        didSet {
            guard safetyRemainDistance != nil else {
                return
            }
            safetyRemainDistance.text = ""
        }
    }
    @IBOutlet weak var intervalAvgView: UIStackView! {
        didSet {
            intervalAvgView.isHidden = true
        }
    }
    @IBOutlet weak var intervalAvgLabel: UILabel!
    @IBOutlet weak var intervalAvgSpeedLabel: UILabel! {
        didSet {
            guard intervalAvgSpeedLabel != nil else {
                return
            }
            intervalAvgSpeedLabel.text = ""
        }
    }
    private var isIntervalShow: Bool = false {
        didSet {
            if !isIntervalShow {
                intervalAvgView.isHidden = true
            }
        }
    }
    private var timerSubscription: Disposable?
    private var remainIntervalTime: Int32 = -1

    
    func onIntervalSafetySpotChangedEvent(intervalGuidance: GIntervalSpeedSpotGuidance? = nil) {
        guard let interval = intervalGuidance else {
            isIntervalShow = false
            intervalAvgView.isHidden = true
            return
        }

        if let resid = GRGType.camSpeed.resourceName {
            isIntervalShow = true
//            spotImageView.image = UIImage(named: resid)
            
            spotImageView.image = UIImage(imageNameWithBundlePath: resid)
            limitSpeed.text = String(describing: interval.limitSpeed)
            safetyRemainDistance.attributedText = setStrokeText(inputString: changeDistanceMeasure(value: interval.remainDistance,
                                                                                                   measureType: .Kilometer))
            intervalAvgSpeedLabel.attributedText = setStrokeText(inputString: String(describing: ceil(interval.averageSpeed)))
            intervalAvgLabel.attributedText = setStrokeText(inputString: intervalAvgLabel.text!)
            safetyRemainDistance.isHidden = false
            limitSpeed.isHidden = false
            intervalAvgView.isHidden = false
        }
    }

    func showSafetySpotImage(safetySpotGuidance: GSafetySpotGuidance) -> UIImage? {
        // safetySpot.type에 맞는 image를 리턴함
        guard let returnImageName = safetySpotGuidance.safetySpot.type.resourceName else {
            return nil
        }
//        return UIImage(named: returnImageName)
        return UIImage(imageNameWithBundlePath: returnImageName)
    }

    func updateSafetySpotView(safetySpotGuidances: [GSafetySpotGuidance]) {

        //safetySpot 중 speedCamera 관련 SafetySpot 이 존재하면 SpeedCamera관련 image를 띄움
        var speedCameraSafetySpot: GSafetySpotGuidance? = nil
        for safetySpotGuidance in safetySpotGuidances {
            let safetySpotType = safetySpotGuidance.safetySpot.type
            if safetySpotType.isSpeedCamera {
                
                if safetySpotType == .camIntervalSpeedStart {
                    isIntervalShow = true
                }
                
                if let safetySpotImage = showSafetySpotImage(safetySpotGuidance: safetySpotGuidance) {
                    spotImageView.image = safetySpotImage

                    safetyRemainDistance.attributedText = setStrokeText(inputString: changeDistanceMeasure(value: safetySpotGuidance.remainDistance,  measureType: .Kilometer))
                    speedCameraSafetySpot = safetySpotGuidance
                    safetyRemainDistance.isHidden = false
                    spotImageView.isHidden = false
                    limitSpeed.text = String(describing: speedCameraSafetySpot!.safetySpot.limitSpeed)
                    limitSpeed.isHidden = false
                } else {
                    limitSpeed.isHidden = true
                    safetyRemainDistance.isHidden = true
                    spotImageView.isHidden = true
                }
                break
            }
        }
        
        //interval 정보 표시중에는 일반 safety를 이벤트를 처리하지 않음
        guard !isIntervalShow else {
            return
        }
        
        //safetySpot 중 speedCamera 관련 SafetySpot 이 존재하지 않으면 SafetySpotGuidances 중 가장 첫번째 spot을 꺼내어 표시
        if speedCameraSafetySpot == nil {
            limitSpeed.isHidden = true
            let firstguidance = safetySpotGuidances.first!
            if let safetySpotImage = showSafetySpotImage(safetySpotGuidance: firstguidance) {
                spotImageView.image = safetySpotImage
                spotImageView.isHidden = false

                safetyRemainDistance.attributedText = setStrokeText(inputString: changeDistanceMeasure(value: firstguidance.remainDistance,
                                                                                                       measureType: .Kilometer))
                safetyRemainDistance.isHidden = false
                
            } else {
                safetyRemainDistance.isHidden = true
                spotImageView.isHidden = true
            }
        }
    }
    
}
