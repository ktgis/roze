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
import RxSwift
import place
import roze
import geom

@objc public protocol SearchManagerDelegate {
    //장소검색 -> SearchPlaces 를 Navi에게 넘김
    func didGetSearchResult(searchResult: GSearchedPlaces?, pageNumber: Int)
    func didFailSearch(error: GPlaceError)
    @objc optional func didGetKeywordSuggestions(_ suggestions: [GSuggest]?)
    @objc optional func didFailKeywordSuggestions(error: GPlaceError)
}


@objc public class SearchManager: NSObject {
    
    private static let numPerPage = 20
    
    public var delegate: SearchManagerDelegate?
    
    private let placeManager = GPlaceManager.instance
    
    public func searchPlace(_ placeName: String,
                            refCoord: GCoord?,
                            startPage: Int,
                            sortType: GSortType = GSortType.relevance) {
        let builder = GSearchOptionBuilder() { builder in
            builder.keyword = placeName
            builder.searchMode = .navigation
            builder.numberOfResults = SearchManager.numPerPage
            builder.startPage = startPage
            if let coord = refCoord {
                builder.sortRefCoord = GLatLng.value(of: coord)
            }
            builder.sortBy = sortType
            
        }
        
        placeManager.search(builder.build()) { (_ searchedPlaces: GSearchedPlaces?, _ error: GPlaceError?) -> Void in
            if let error = error {
                self.delegate?.didFailSearch(error: error)
                return
            }
            self.delegate?.didGetSearchResult(searchResult: searchedPlaces, pageNumber: startPage)
        }
        
        
//        SearchApi.searchLocation(query: placeName, option: 1, numOfResult: 20)
//            .observeOn(MainScheduler.instance)
//            .subscribe(onNext: { searchResult in
//                logInfo("search success")
//                self.delegate?.didSearchFininshed(searchResult: searchResult.resultData.places)
//            }, onError: { error in
//                logError("search fail")
//                self.delegate?.didSearchFailed()
//            }, onCompleted: {
//                logInfo("search complete")
//            }).addDisposableTo(disposeBag)
    }
    
    public func requestKeywordSuggestions(_ keyword: String,
                                          refCoord: GCoord?,
                                          categories: [GCategoryType]? = nil) {
        let builder = GAutoCompleteOptionBuilder() { builder in
            builder.keyword = keyword
            if let coord = refCoord {
                builder.coord = GLatLng.value(of: coord)
            }
            builder.category = categories
        }
        
        placeManager.autoComplete(builder.build()){(_ autoComplete: GAutoComplete?,
                                                                        _ error: GPlaceError?) -> Void in
            if let error = error {
                self.delegate?.didFailKeywordSuggestions?(error: error)
                return
            }
            self.delegate?.didGetKeywordSuggestions?(autoComplete?.suggests)
        }
    }
    
}

//extension RozeError {
//    static func create(from placeError: PlaceError) -> RozeError {
//        if let code = RozeError.RozeErrorResult(rawValue: placeError.code.rawValue) {
//            return RozeError(code: code, description: placeError.description)
//        } else {
//            return RozeError(code: .unknownError, description: placeError.description)
//        }
//    }
//}

