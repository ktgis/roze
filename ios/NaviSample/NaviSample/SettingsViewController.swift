//
//  SettingsViewController.swift
//  iRozeNavi
//
//  Created by moon on 2017. 10. 30..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze
import RxSwift
import Toast_Swift

enum CellType {
    case drillDown
    case switcher
}

enum ConfigId {
    case carType
    case gasType
    case useHipass
    case voiceType
    case safetyConfig
}


/// tebleViewCell 을 위한 ViewModel 객체
struct ConfigCategoryModel {
    /// 설정 종류
    let configId: ConfigId
    /// label
    let title: String
    /// 상세 설정으로 이동 또는 switch로 직접 설정
    let cellType: CellType
    /// 현재 설정된 값의 label, drillDown type 의 cell 을 위해 사용된다.
    var currentConfig: String {
        get {
            let options = GNavigationOptions.instance
            switch configId {
            case .carType:
                return SettingsUtil.label(options.carType)
            case .voiceType:
                return SettingsUtil.label(options.voiceType)
            case .gasType:
                return SettingsUtil.label(options.energyType)
            default:
                return ""
            }
        }
    }
    /// 설정이 on/off 인지 여부, useHipass 를 위해서만 사용된다.
    var enabled: Bool {
        get {
            if configId ==  .useHipass {
                return GNavigationOptions.instance.hasHipass
            } else {
                return false
            }
        }
        set {
            if configId == .useHipass {
                GNavigationOptions.instance.hasHipass = newValue
            } else {
                return
            }
        }
    }
}

class SettingsViewController: UITableViewController {
    
    private let cellModels = [(sectionTitle:"차량 설정", cells:[
                                ConfigCategoryModel(configId: .carType,
                                                    title: "차량 종류",
                                                    cellType: .drillDown),
                                ConfigCategoryModel(configId: .gasType,
                                                    title: "유류 종류",
                                                    cellType: .drillDown),
                                ConfigCategoryModel(configId: .useHipass,
                                                    title: "하이패스 사용",
                                                    cellType: .switcher)]),
                            (sectionTitle:"음성 설정", cells:[
                                ConfigCategoryModel(configId: .voiceType,
                                                    title: "음성 종류",
                                                    cellType: .drillDown),
                                ConfigCategoryModel(configId: .safetyConfig,
                                                    title: "안전운전 안내",
                                                    cellType: .drillDown),])]
    
    var activityIndicator: UIActivityIndicatorView?
    
    var selectedIndex: IndexPath?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = "Settings"
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        self.navigationController?.setNavigationBarHidden(true, animated: false)
        
        if self.isMovingFromParentViewController {
            logDebug("Settings view will be popped from navigation controller, saving current config.")
            saveConfig()
        }
    }
    
    func saveConfig() {
        if activityIndicator != nil && activityIndicator!.isAnimating {
            return
        }
        showActivityIndicator()
        let _ = Observable<Bool>.create { observer in
            if let err = GNavigationManager.instance.refreshSoundData() {
                //error while refreshing sound data
                observer.onError(err)
            } else {
                do {
                    try GNavigationOptions.instance.saveConfig()
                } catch {
                    observer.onError(error)
                }
            }
            observer.onNext(true)
            observer.onCompleted()
            return Disposables.create()
        }.subscribeOn(SerialDispatchQueueScheduler(qos: .default))
            .observeOn(MainScheduler.instance)
            .subscribe(onNext: { result in
            logDebug("Saving config success? \(result)")
        }, onError: { error in
            logError("Error while saving RozeOptions into file : \(error.localizedDescription)")
            self.hideActivityIndicator()
            self.view.makeToast("설정을 저장하지 못했습니다.", duration: 2.0, position: .center)
        }, onCompleted: {
            self.hideActivityIndicator()
            self.view.makeToast("성공적으로 설정을 저장했습니다.", duration: 2.0, position: .center)
            
        })
    }
    
    
    // MARK: Methods for Navigating
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if "showSettingsDetail" == segue.identifier {
            if let selected = tableView.indexPathForSelectedRow {
                if let detailVC = segue.destination as? SettingsDetailViewController {
                    // 설정 상세 화면 VC 로 설정 종류 정보를 전달한다.
                    detailVC.configCategory = cellModels[selected.section].cells[selected.row]
                    selectedIndex = selected
                }
                tableView.deselectRow(at: selected, animated: true)
            }
        }
    }
    /// 설정 상세 화면에서 exit segue 에 의해 실행된다. 변경 사항을 적용한다.
    @IBAction func refreshData(segue: UIStoryboardSegue) {
        if let selected = selectedIndex {
            tableView.reloadRows(at: [selected], with:UITableViewRowAnimation.none)
        }
    }
    
    // MARK: Overrides of UITableViewDataSsource
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellModel = cellModels[indexPath.section].cells[indexPath.row]
        switch cellModel.cellType {
        case .drillDown:
            if let cell = tableView.dequeueReusableCell(withIdentifier: "drillDownCell", for: indexPath) as? DrillDownTableViewCell {
                cell.title.text = cellModel.title
                cell.currentConfig.text = cellModel.currentConfig
                return cell
            }
        case .switcher:
            if let cell = tableView.dequeueReusableCell(withIdentifier: "switcherCell", for: indexPath) as? SwitchTableViewCell {
                cell.title.text = cellModel.title
                cell.config.isOn = cellModel.enabled
                cell.viewModel = cellModel
            }
        }
        return UITableViewCell()
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return cellModels[section].cells.count
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return cellModels.count
    }
    
    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        return cellModels[section].sectionTitle
    }
    
    // MARK: Overrides of UITableViewDelegate
    
    override func tableView(_ tableView: UITableView, shouldHighlightRowAt indexPath: IndexPath) -> Bool {
        // switch cell 은 tap 으로 highlight 되지 않는다.
        return isSelectableCell(cellModels[indexPath.section].cells[indexPath.row].cellType)
    }

    override func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        // switch cell 은 선택가능하지 않다.
        if isSelectableCell(cellModels[indexPath.section].cells[indexPath.row].cellType) {
            return indexPath
        }
        return nil
    }
    
    // MARK: UI related util methods
    
    private func isSelectableCell(_ cellType: CellType) -> Bool {
        switch cellType {
        case .drillDown:
            return true
        case .switcher:
            return false
        }
    }
    
    private func showActivityIndicator() {
        logDebug("trying to show activity indicator..")
        if let indicator = self.activityIndicator {
            indicator.isHidden = false
            indicator.startAnimating()
        } else {
            let indicator = UIActivityIndicatorView(frame: self.view.frame)
            indicator.activityIndicatorViewStyle = .gray
            indicator.isHidden = false
            indicator.startAnimating()
            self.view.addSubview(indicator)
            self.activityIndicator = indicator
        }
        tableView.resignFirstResponder()
    }
    
    private func hideActivityIndicator() {
        logDebug("trying to hide activity indicator..")
        activityIndicator?.isHidden = true
        activityIndicator?.stopAnimating()
        tableView.becomeFirstResponder()
    }
}
