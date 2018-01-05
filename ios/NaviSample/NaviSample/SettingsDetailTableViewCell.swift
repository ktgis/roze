//
//  SettingsDetailTableViewCell.swift
//  iRozeNavi
//
//  Created by moon on 2017. 11. 1..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze

class SettingsDetailTableViewCell: UITableViewCell {
    @IBOutlet weak var title: UILabel!
}

class SafetySettingTableViewCell: UITableViewCell {
    
    @IBOutlet weak var title: UILabel!
    @IBOutlet weak var switcher: UISwitch!
    
    var viewModel: SafetyConfigDataModel?
    
    @IBAction func valueChanged(_ sender: Any) {
        if let switcher = sender as? UISwitch {
            logDebug("switch value changed : \(switcher.isOn)")
            viewModel?.enabled = switcher.isOn
        }
    }
}
