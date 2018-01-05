//
//  VCUtil.swift
//  iRozeNavi
//
//  Created by moon on 2017. 11. 30..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import gmaps

/// Instantiate SearchViewController from storyboard,
/// caller can pass reference location to provide reference location for query
///
/// - Parameter refLocation: GCoord
/// - Returns: SearchViewController instance, returns nil when SearchViewController can't be instantiated from storyboard
func createSearchVC(refLocation: GCoord?) -> SearchViewController? {
    let storyBoard = UIStoryboard(name: "Search", bundle: Bundle.main)
    if let searchVC = storyBoard.instantiateInitialViewController() as? SearchViewController {
        searchVC.refLocation = refLocation
        return searchVC
    }
    return nil
}
