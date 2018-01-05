/*
 * Copyright (c) 2017 kt corp. All rights reserved.
 *
 * This is a proprietary software of kt corp, and you may not use this file
 * except in compliance with license agreement with kt corp. Any redistribution
 * or use of this software, with or without modification shall be strictly
 * prohibited without prior written approval of kt corp, and the copyright
 * notice above does not evidence any actual or intended publication of such
 * software.
 */

import Foundation
import roze
import ObjectMapper

class RecentPlaces: Mappable {
    var places: [SearchPlaceData]!
    
    required public init?(map: Map) {
    }
    
    public func mapping(map: Map) {
        places <- map["Places"]
    }
    

    static var fileUrl: URL = {
        let documentDirUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first
        return documentDirUrl!.appendingPathComponent("RecentPlaces.json")
    }()
    
    func addPlace(_ placeData: SearchPlaceData) {
        if let indexElement = places.index(where: {$0.name.isEqual(placeData.name)}) {
            places.remove(at: indexElement)
        }
        
        places.insert(placeData, at: 0)
        
        saveRecentPlaces()
    }
    
    func saveRecentPlaces() {
        let json = self.toJSON()
        
        DispatchQueue.global().async {
            do {
                let data = try JSONSerialization.data(withJSONObject: json, options: [])
                try data.write(to: RecentPlaces.fileUrl, options: [])
            } catch {
                logError("Could not save recent places!: \(error.localizedDescription)")
            }
        }
    }
    
    static func loadRecentPlaces() -> RecentPlaces? {
        if let jsonData = try? Data(contentsOf: RecentPlaces.fileUrl, options: .mappedIfSafe) {
            guard let jsonString = String(data: jsonData, encoding: .utf8) else {
                print("Invalid recent places data!")
                return RecentPlaces(JSON: ["Places": []])
            }
            return Mapper<RecentPlaces>().map(JSONString: jsonString)
            
        } else {
            print("Could not load recent places!")
            return RecentPlaces(JSON: ["Places": []])
        }

    }


}
