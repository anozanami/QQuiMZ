package com.release.qquimz;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREF_NAME = "SearchHistory";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final int MAX_HISTORY_ITEMS = 10;

    private SharedPreferences preferences;
    private List<String> searchHistory;

    public SearchHistoryManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        searchHistory = getSearchHistory();
    }

    public void addSearchQuery(String query) {
        if (!searchHistory.contains(query)) {
            searchHistory.add(0, query);
            if (searchHistory.size() > MAX_HISTORY_ITEMS) {
                searchHistory.remove(searchHistory.size() - 1);
            }
            saveSearchHistory();
        }
    }

    private List<String> getSearchHistory() {
        String historyString = preferences.getString(KEY_SEARCH_HISTORY, "");
        if (historyString.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(historyString.split(",")));
    }

    private void saveSearchHistory() {
        String historyString = TextUtils.join(",", searchHistory);
        preferences.edit().putString(KEY_SEARCH_HISTORY, historyString).apply();
    }

    public List<String> getRecentSearches() {
        return searchHistory;
    }
}
