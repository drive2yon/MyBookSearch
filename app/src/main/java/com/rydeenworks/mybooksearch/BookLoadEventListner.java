package com.rydeenworks.mybooksearch;

import java.util.EventListener;

public interface BookLoadEventListner extends EventListener {
    void OnBookLoad(String bookTitle);
}
