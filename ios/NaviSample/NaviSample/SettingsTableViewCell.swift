//
//  SettingCategoryTableViewCell.swift
//  iRozeNavi
//
//  Created by moon on 2017. 10. 31..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze

/// 상세 설정 화면으로 이동하는 cell
class DrillDownTableViewCell: UITableViewCell {
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var currentConfig: UILabel!
}

/// switcher 로 직접 설정하는 cell (하이패스 사용 여부)
class SwitchTableViewCell: UITableViewCell {
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var config: UISwitch!
    
    var viewModel: ConfigCategoryModel?
    
    @IBAction func valueChanged(_ sender: Any) {
        if let switcher = sender as? UISwitch {
            logDebug("switch value changed : \(switcher.isOn)")
            viewModel?.enabled = switcher.isOn
        }
    }
}
