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
import UIKit
import roze
import place

class SearchResultTableController: NSObject{
    unowned let viewController: SearchViewController
    
    var tableData: [SearchPlaceData]!
    
    var currentPage: Int = 0
    var totalResultCount: Int = 0
    
    init(viewController: SearchViewController) {
        self.viewController = viewController
        tableData = []
    }
}

extension SearchResultTableController: UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        guard let data = tableData else {
            return 0
        }
        guard data.count > 0 else {
            return 0
        }
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "ResultCell", for: indexPath)
        
        if let placeCell = cell as? SearchTableViewCell {
            let cellData = tableData![indexPath.row]
            placeCell.placeTitle.text = cellData.name
            placeCell.address.text = cellData.address
        }
        // do loadMore if we rechaed tableview bottom and totalCount is bigger than current last item
        if indexPath.row == tableData.count - 1 && indexPath.row < totalResultCount - 1 {
            viewController.searchNextPage()
        }
        return cell
    }
}

extension SearchResultTableController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 75.0
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        viewController.searchBar.resignFirstResponder()
        tableView.deselectRow(at: indexPath, animated: true)
        
        viewController.placeData = tableData[indexPath.row]
        viewController.recentPlaces?.addPlace(tableData[indexPath.row])
        
        viewController.performSegue(withIdentifier: SearchViewController.unwindSegueIdentifier, sender: self)
        
    }
    
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        viewController.searchBar.resignFirstResponder()
    }
}

class SuggestionsTableController: NSObject{
    
    unowned let viewController: SearchViewController
    
    var tableData: [GSuggest]!
    
    init(viewController: SearchViewController) {
        self.viewController = viewController
        tableData = []
    }
}

extension SuggestionsTableController: UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        guard let data = tableData else {
            return 0
        }
        guard data.count > 0 else {
            return 0
        }
        return data.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "SuggestionCell", for: indexPath)
        if let suggestCell = cell as? SuggestionTableViewCell {
            suggestCell.title.text = tableData[indexPath.row].name
        }
        return cell
    }
}

extension SuggestionsTableController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        // hide suggestions table view and do actual search
        let keyword = tableData[indexPath.row].name
        viewController.searchBar.text = keyword
        tableView.isHidden = true
        viewController.searchPlace(keyword)
    }
    
    func scrollViewWillBeginDragging(_ scrollView: UIScrollView) {
        viewController.searchBar.resignFirstResponder()
    }
}

/// Search UI,
/// when user seletects one from search result, selected location info is stored at placeData property
/// @IBAction annotated unwindToContainerVC(segue: UIStoryboardSegue) function of previous ViewController will be invoked.
class SearchViewController: UIViewController {
    static let unwindSegueIdentifier = "unwind"
    
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var searchTableView: UITableView!
    @IBOutlet weak var suggestionsTableView: UITableView!
    
    var searchResultTableController: SearchResultTableController?
    var suggestionsTableController: SuggestionsTableController?
    
    var searchManager: SearchManager?
    var recentPlaces: RecentPlaces?
    var placeData: SearchPlaceData?
    
    //properties to load next page
    var searchKeyword: String?
    
    var refLocation: GCoord?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Todo: read recent places from json file
        searchBar.becomeFirstResponder()
        let tableController = SearchResultTableController(viewController: self)
        searchTableView.delegate = tableController
        searchTableView.dataSource = tableController
        searchResultTableController = tableController
        
        let suggestionsController = SuggestionsTableController(viewController: self)
        suggestionsTableView.delegate = suggestionsController
        suggestionsTableView.dataSource = suggestionsController
        suggestionsTableController = suggestionsController
        
        recentPlaces = RecentPlaces.loadRecentPlaces()
        searchResultTableController?.tableData = recentPlaces!.places
        searchTableView.reloadData()
    }
    
    //search user input keyword
    func searchPlace(_ keyword: String) {
        if searchManager == nil {
            searchManager = SearchManager()
            searchManager!.delegate = self
        }
        searchKeyword = keyword
        searchResultTableController?.currentPage = 0
        searchManager?.searchPlace(keyword, refCoord: refLocation, startPage: 0)
    }
    
    //try to load next page for current search keyword
    func searchNextPage() {
        guard let keyword = searchKeyword else {
            return
        }
        if searchManager == nil {
            searchManager = SearchManager()
            searchManager!.delegate = self
        }
        searchResultTableController?.currentPage += 1
        let page = searchResultTableController?.currentPage ?? 0
        searchManager?.searchPlace(keyword, refCoord: refLocation, startPage: page)
    }
    
    //try to load keyword suggestions for user input
    func requestKeywordSuggestions(for keyword: String) {
        if searchManager == nil {
            searchManager = SearchManager()
            searchManager!.delegate = self
        }
        searchManager?.requestKeywordSuggestions(keyword, refCoord: refLocation)
    }

}

extension SearchViewController: SearchManagerDelegate {
    func didGetSearchResult(searchResult: GSearchedPlaces?, pageNumber: Int) {
        
        guard let searchResult = searchResult else {
            return
        }
        
        searchResultTableController?.totalResultCount = searchResult.totalCount
        if pageNumber == 0 {
            searchResultTableController?.tableData.removeAll()
        }
        
        for place in searchResult.places {
            if let data = SearchPlaceData(place: place) {
                searchResultTableController?.tableData.append(data)
            }
        }
        searchTableView.reloadData()
        
        let firstIndex = IndexPath(row: 0, section: 0)
        if pageNumber == 0 && searchTableView.cellForRow(at: firstIndex) != nil{
            searchTableView.scrollToRow(at: firstIndex, at: .top, animated: true)
        }
    }
    
    func didFailSearch(error: GPlaceError) {
        self.view.makeToast("didSearchFailed")

        searchResultTableController?.tableData.removeAll()
        searchTableView.reloadData()
    }
    
    func didGetKeywordSuggestions(_ suggestions: [GSuggest]?) {
        
        guard let suggestions = suggestions else {
            return
        }
        
        if suggestions.isEmpty {
            // ignore empty result
            return
        }
        suggestionsTableController?.tableData.removeAll()
        for suggestion in suggestions {
            suggestionsTableController?.tableData.append(suggestion)
        }
        suggestionsTableView.isHidden = false
        suggestionsTableView.reloadData()
    }
    
    func didFailKeywordSuggestions(error: GPlaceError) {
        logDebug("Keyword suggestions failure.")
        suggestionsTableController?.tableData.removeAll()
        suggestionsTableView.reloadData()
    }
}

extension SearchViewController: UISearchBarDelegate {
    
    func searchBarTextDidBeginEditing(_ searchBar: UISearchBar) {
        searchBar.becomeFirstResponder()
        searchBar.showsCancelButton = true
        
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        logDebug("--- searchBarSearchButtonClicked")
        guard let searchText = searchBar.text else {
            return
        }
        
        guard searchText.count > 0 else {
            return
        }
        
        searchPlace(searchText)
        searchBar.text = ""
        searchBar.resignFirstResponder()
        suggestionsTableView.isHidden = true
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        
        searchBar.text = ""
        searchBar.resignFirstResponder()
        suggestionsTableView.isHidden = true
        self.dismiss(animated: true, completion: nil)
        
    }
    
    func searchBar(_ searchBar: UISearchBar, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        //TODO: block special characters(ex, emoji)
        return true
    }
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        let trimmed = searchText.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        if trimmed.count == 0 {
            searchResultTableController?.currentPage = 0
            searchKeyword = nil
            searchResultTableController?.tableData = recentPlaces!.places
            searchTableView.reloadData()
            suggestionsTableView.isHidden = true
            return
        }
        //TODO: should do nothing when last character is single consonant with no vowel?
        requestKeywordSuggestions(for: trimmed)
    }
}
