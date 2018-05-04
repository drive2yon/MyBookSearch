package com.rydeenworks.mybooksearch;

import java.util.EventListener;

public interface BookLoadEventListener extends EventListener {
    void OnBookLoad(String bookTitle, String url);
    void OnAddAmazonBookHistory(String bookTitle, String isbn10);
}
