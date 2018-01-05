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
import ObjectMapper
import place
import geom
/*
class SearchResult: Mappable {
    var apiId: String!
    var errCode: String!
    var errMsg: String?
    var resultData: SearchResultData!
    
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        errMsg <- map["ERRMSG"]
        errCode <- map["ERRCODE"]
        apiId <- map["APIID"]
        resultData <- map["RESULTDATA"]
    }
}

class SearchResultData: Mappable {
    var places: SearchPlaces!
    
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        places <- map["place"]
    }
}

public class SearchPlaces: Mappable {
    var count: String!
    var totalCount: String!
    public var data: [SearchPlaceData]?
    
    required public init?(map: Map) {
    }
    
    public func mapping(map: Map) {
        count <- map["Count"]
        totalCount <- map["TotalCount"]
        data <- map["Data"]
    }
}
*/

public class MappableGUtmk: Mappable {
    
    public var x: String!
    public var y: String!
    
    required public init?(map: Map) {
    }
    public init(utmk: GUtmk) {
        self.x = "\(utmk.x)"
        self.y = "\(utmk.y)"
    }
    public func mapping(map: Map) {
        x <- map["X"]
        y <- map["Y"]
    }
    
    public func toGUtmk() -> GUtmk {
        return GUtmk(x: Double(x)!, y: Double(y)!)
    }
}

public class SearchPlaceData: Mappable {
    public var name: String!
    public var x: String!
    public var y: String!
    public var address: String!
    public var newAddress: String?
    public var tel: String?
    
    public var multipleEntrances: [MappableGUtmk]?
    
    required public init?(map: Map) {
        
    }
    
    public init?(place: GPlace) {
        guard let utmkCoord = GUtmk.value(of: place.coord) else {
            return nil
        }
        self.name = place.detailName
        self.x = "\(utmkCoord.x)"
        self.y = "\(utmkCoord.y)"
        self.address = place.address.formattedAddress ?? ""
        self.newAddress = place.address.formattedNewAddress
        if let phones = place.phones?.representation {
            self.tel = (phones.isEmpty ? nil : phones[0])
        }
        if let multiEnts = place.multipleEntrances?.utmkEntrances {
            self.multipleEntrances = [MappableGUtmk]()
            self.multipleEntrances!.append(contentsOf: multiEnts.map(){utmk in return MappableGUtmk(utmk: utmk)})
        }
    }
    
    public func mapping(map: Map) {
        name <- map["NAME"]
        x <- map["X"]
        y <- map["Y"]
        address <- map["ADDR"]
        newAddress <- map["NEW_ADDR"]
        tel <- map["TEL"]
        multipleEntrances <- map["MULTIPLE_ENTRANCES"]
    }
    
    public func getLocation() -> GUtmk {
        return GUtmk(x: Double(x)!, y: Double(y)!)
    }
    
}


