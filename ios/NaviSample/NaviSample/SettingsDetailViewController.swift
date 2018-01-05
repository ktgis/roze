//
//  SettingsDetailViewController.swift
//  iRozeNavi
//
//  Created by moon on 2017. 10. 31..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze

protocol LabeledDataModel {
    
    /// 설정 항목 label 을 구성하기 위해 사용됨
    ///
    /// - Returns: cell 의 label 에 들어갈 문자열
    func getLabel() -> String
    
    /// 설정 항목의 ckecked (또는 switch on) 여부를 결정하기 위해 사용됨
    ///
    /// - Returns: 해당 설정 항목이 enable 되어 있으면 true, disable 되어 있으면 false
    func isSelected() -> Bool
}

/// 차량 종류 설정을 위한 ViewModel
struct CarTypeDataModel: LabeledDataModel {
    let type: GCarType
    let label: String
    
    func getLabel() -> String {
        return label
    }
    
    func isSelected() -> Bool {
        return GNavigationOptions.instance.carType == type
    }
}

/// 유종 설정을 위한 ViewModel
struct GasTypeDataModel: LabeledDataModel {
    let type: GEnergyType
    let label: String
    
    func getLabel() -> String {
        return label
    }
    func isSelected() -> Bool {
        return GNavigationOptions.instance.energyType == type
    }
}

/// 음성 종류 설정을 위한 ViewModel
struct VoiceTypeDataModel: LabeledDataModel {
    let type: GVoiceType
    let label: String
    
    func getLabel() -> String {
        return label
    }
    func isSelected() -> Bool {
        return GNavigationOptions.instance.voiceType == type
    }
}

enum SafetyConfig {
    case fixedSpeedCamera
    case movableSpeedCamera
    case signalViolationCamera
    case busCamera
    case trafficCamera
    case stopCamera
    case overloadCamera
    case interruptCamera
    case cctv
    case shoulder
    case sharpCurve
    case accidentBlackSpot
    case laneDecrease
    case rockSlide
    case slipperySurface
    case speedBump
    case fog
    case fall
    case railroadCrossing
    case scarp
    case deerCrossing
    case lowestGasStation
}


/// 안전운행 관련 음성 발성 여부 설정을 위한 ViewModel
struct SafetyConfigDataModel: LabeledDataModel {
    let config: SafetyConfig
    let label: String
    
    /// switch on/off 에 연동되는 property
    var enabled: Bool {
        get {
            let soundOptions = GNavigationOptions.instance.soundOptions
            switch config {
            case .fixedSpeedCamera:
                return soundOptions.fixedSpeedCamera
            case .movableSpeedCamera:
                return soundOptions.movableSpeedCamera
            case .signalViolationCamera:
                return soundOptions.signalViolationCamera
            case .busCamera:
                return soundOptions.busCamera
            case .trafficCamera:
                return soundOptions.trafficCamera
            case .stopCamera:
                return soundOptions.stopCamera
            case .overloadCamera:
                return soundOptions.overloadCamera
            case .interruptCamera:
                return soundOptions.interruptCamera
            case .cctv:
                return soundOptions.cctv
            case .shoulder:
                return soundOptions.shoulder
            case .sharpCurve:
                return soundOptions.sharpCurve
            case .accidentBlackSpot:
                return soundOptions.accidentBlackSpot
            case .laneDecrease:
                return soundOptions.laneDecrease
            case .rockSlide:
                return soundOptions.rockSlide
            case .slipperySurface:
                return soundOptions.slipperySurface
            case .speedBump:
                return soundOptions.speedBump
            case .fog:
                return soundOptions.fog
            case .fall:
                return soundOptions.fall
            case .railroadCrossing:
                return soundOptions.railroadCrossing
            case .scarp:
                return soundOptions.scarp
            case .deerCrossing:
                return soundOptions.deerCrossing
            case .lowestGasStation:
                return soundOptions.lowestGasStation
            }
        }
        set {
            let soundOptions = GNavigationOptions.instance.soundOptions
            switch config {
            case .fixedSpeedCamera:
                soundOptions.fixedSpeedCamera = newValue
            case .movableSpeedCamera:
                soundOptions.movableSpeedCamera = newValue
            case .signalViolationCamera:
                soundOptions.signalViolationCamera = newValue
            case .busCamera:
                soundOptions.busCamera = newValue
            case .trafficCamera:
                soundOptions.trafficCamera = newValue
            case .stopCamera:
                soundOptions.stopCamera = newValue
            case .overloadCamera:
                soundOptions.overloadCamera = newValue
            case .interruptCamera:
                soundOptions.interruptCamera = newValue
            case .cctv:
                soundOptions.cctv = newValue
            case .shoulder:
                soundOptions.shoulder = newValue
            case .sharpCurve:
                soundOptions.sharpCurve = newValue
            case .accidentBlackSpot:
                soundOptions.accidentBlackSpot = newValue
            case .laneDecrease:
                soundOptions.laneDecrease = newValue
            case .rockSlide:
                soundOptions.rockSlide = newValue
            case .slipperySurface:
                soundOptions.slipperySurface = newValue
            case .speedBump:
                soundOptions.speedBump = newValue
            case .fog:
                soundOptions.fog = newValue
            case .fall:
                soundOptions.fall = newValue
            case .railroadCrossing:
                soundOptions.railroadCrossing = newValue
            case .scarp:
                soundOptions.scarp = newValue
            case .deerCrossing:
                soundOptions.deerCrossing = newValue
            case .lowestGasStation:
                soundOptions.lowestGasStation = newValue
            }
        }
    }
    
    func getLabel() -> String {
        return label
    }
    
    func isSelected() -> Bool {
        return self.enabled
    }
}

class SettingsDetailViewController: UITableViewController {
    var configCategory: ConfigCategoryModel?
    var data: [LabeledDataModel]?
    
    /// 차량, 음성, 유종 설정에서 현재 선택된 항목의 index
    var selectedIndex = -1 {
        // cell checkmark 를 결정하기 위한 property observer
        willSet {
            if selectedIndex >= 0 && selectedIndex != newValue {
                if let prevCell = tableView.cellForRow(at: IndexPath(row: selectedIndex, section: 0)) {
                    prevCell.accessoryType = .none
                }
                if let currCell = tableView.cellForRow(at: IndexPath(row: newValue, section: 0)) {
                    currCell.accessoryType = .checkmark
                }
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        if let category = configCategory {
            self.navigationItem.title = category.title
        } else {
            self.navigationItem.title = "Unknown"
        }
        
        guard let category = configCategory else {
            clearData()
            return
        }
        // 설정 항목에 따라 다른 data 를 loading
        switch category.configId {
        case .carType:
            loadCarTypeData()
        case .gasType:
            loadGasTypeData()
        case .voiceType:
            loadVoiceTypeData()
        case .safetyConfig:
            loadSafetyConfigData()
        default:
            clearData()
        }
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        self.navigationController?.setNavigationBarHidden(true, animated: false)
    }

    // MARK: overrides of UITableViewDataSource
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let data = self.data {
            return data.count
        }
        return 0
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard let model =  data?[indexPath.row] else {
            return UITableViewCell()
        }
        
        guard let category = configCategory else {
            return UITableViewCell()
        }
        
        switch category.configId {
        case .carType, .gasType, .voiceType:
            if let settingCell = tableView.dequeueReusableCell(withIdentifier: "typeSelectCell", for: indexPath)
                as? SettingsDetailTableViewCell {
                settingCell.title.text = model.getLabel()
                if model.isSelected() {
                    settingCell.accessoryType = .checkmark
                    selectedIndex = indexPath.row
                } else {
                    settingCell.accessoryType = .none
                }
                return settingCell
            }
        case .safetyConfig:
            if let safetyCell = tableView.dequeueReusableCell(withIdentifier: "switcherCell", for: indexPath)
                                as? SafetySettingTableViewCell,
                let safetyConfigModel = model as? SafetyConfigDataModel{
                safetyCell.title.text = model.getLabel()
                safetyCell.switcher.isOn = model.isSelected()
                safetyCell.viewModel = safetyConfigModel
                return safetyCell
            }
        default:
            return UITableViewCell()
        }
        
        return UITableViewCell()
    }
    
    override func tableView(_ tableView: UITableView, shouldHighlightRowAt indexPath: IndexPath) -> Bool {
        
        guard let category = configCategory else {
            return false
        }
        /// safety config 은 highlight 안된다. .useHipass 는 들어올 일 없다.
        return isSelctable(category.configId)
    }
    
    override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        guard let category = configCategory else {
            return nil
        }
        /// safety config 은 선택 불가능. .useHipass 는 들어올 일 없다.
        if isSelctable(category.configId) {
            return indexPath
        }
        return nil
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if let carTypeData = data as? [CarTypeDataModel] {
            GNavigationOptions.instance.carType = carTypeData[indexPath.row].type
        } else if let gasTypeData = data as? [GasTypeDataModel] {
            GNavigationOptions.instance.energyType = gasTypeData[indexPath.row].type
        } else if let voiceTypeData = data as? [VoiceTypeDataModel] {
            GNavigationOptions.instance.voiceType = voiceTypeData[indexPath.row].type
        }
        selectedIndex = indexPath.row
        performSegue(withIdentifier: "dataSelectionSegue", sender: nil)
        
        tableView.deselectRow(at: indexPath, animated: true)
    }

    // MARK: ui 관련 util private methods
    
    private func isSelctable(_ configId: ConfigId) -> Bool {
        switch configId {
        case .carType, .gasType, .voiceType:
            return true
        case .safetyConfig, .useHipass:
            return false
        }
    }
    
    // MARK: data loading 관련 private methods
    
    private func clearData() {
        data?.removeAll()
    }
        
    private func loadCarTypeData() {
        var carTypeData = [CarTypeDataModel]()
        carTypeData.append(CarTypeDataModel(type: .motorcycle, label: SettingsUtil.label(.motorcycle)))
        carTypeData.append(CarTypeDataModel(type: .car, label: SettingsUtil.label(.car)))
        carTypeData.append(CarTypeDataModel(type: .midCar, label: SettingsUtil.label(.midCar)))
        carTypeData.append(CarTypeDataModel(type: .heavyCar, label: SettingsUtil.label(.heavyCar)))
        carTypeData.append(CarTypeDataModel(type: .heavyTruck, label: SettingsUtil.label(.heavyTruck)))
        carTypeData.append(CarTypeDataModel(type: .specificGoods, label: SettingsUtil.label(.specificGoods)))
        carTypeData.append(CarTypeDataModel(type: .smallCar, label: SettingsUtil.label(.smallCar)))
        
        data = carTypeData
        
    }
    
    private func loadGasTypeData() {
        var gasTypeData = [GasTypeDataModel]()
        gasTypeData.append(GasTypeDataModel(type: .gasoline, label: SettingsUtil.label(.gasoline)))
        gasTypeData.append(GasTypeDataModel(type: .premiumGasoline, label: SettingsUtil.label(.premiumGasoline)))
        gasTypeData.append(GasTypeDataModel(type: .diesel, label: SettingsUtil.label(.diesel)))
        gasTypeData.append(GasTypeDataModel(type: .kerosene, label: SettingsUtil.label(.kerosene)))
        gasTypeData.append(GasTypeDataModel(type: .lpg, label: SettingsUtil.label(.lpg)))
        gasTypeData.append(GasTypeDataModel(type: .cng, label: SettingsUtil.label(.cng)))
        gasTypeData.append(GasTypeDataModel(type: .lng, label: SettingsUtil.label(.lng)))
        gasTypeData.append(GasTypeDataModel(type: .electricity, label: SettingsUtil.label(.electricity)))
        
        data = gasTypeData
    }
    
    private func loadVoiceTypeData() {
        var voiceTypeData = [VoiceTypeDataModel]()
        voiceTypeData.append(VoiceTypeDataModel(type: .soft, label: SettingsUtil.label(.soft)))
        voiceTypeData.append(VoiceTypeDataModel(type: .sporty, label: SettingsUtil.label(.sporty)))
        
        data = voiceTypeData
    }
    
    private func loadSafetyConfigData() {
        var safetyData = [SafetyConfigDataModel]()
        safetyData.append(SafetyConfigDataModel(config: .fixedSpeedCamera, label: "고정식 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .movableSpeedCamera, label: "이동식 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .signalViolationCamera, label: "신호위반 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .busCamera, label: "버스전용 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .trafficCamera, label: "교통정보 수집 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .stopCamera, label: "주정차 단속 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .overloadCamera, label: "과적차량 단속 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .interruptCamera, label: "끼어들기 단속 카메라"))
        safetyData.append(SafetyConfigDataModel(config: .cctv, label: "방범 CCTV"))
        safetyData.append(SafetyConfigDataModel(config: .shoulder, label: "갓길단속"))
        safetyData.append(SafetyConfigDataModel(config: .sharpCurve, label: "급회전 구간"))
        safetyData.append(SafetyConfigDataModel(config: .accidentBlackSpot, label: "사고다발 구간"))
        safetyData.append(SafetyConfigDataModel(config: .laneDecrease, label: "좁아지는 지역"))
        safetyData.append(SafetyConfigDataModel(config: .rockSlide, label: "낙석주의"))
        safetyData.append(SafetyConfigDataModel(config: .slipperySurface, label: "미끄럼주의"))
        safetyData.append(SafetyConfigDataModel(config: .speedBump, label: "과속방지턱"))
        safetyData.append(SafetyConfigDataModel(config: .fog, label: "안개주의"))
        safetyData.append(SafetyConfigDataModel(config: .fall, label: "추락주의"))
        safetyData.append(SafetyConfigDataModel(config: .railroadCrossing, label: "철길건널목"))
        safetyData.append(SafetyConfigDataModel(config: .scarp, label: "급경사"))
        safetyData.append(SafetyConfigDataModel(config: .deerCrossing, label: "야생동물보호"))
        safetyData.append(SafetyConfigDataModel(config: .lowestGasStation, label: "최저가주유소"))
        data = safetyData
    }
    
}
