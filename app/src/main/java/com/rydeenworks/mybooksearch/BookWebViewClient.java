package com.rydeenworks.mybooksearch;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BookWebViewClient extends WebViewClient {
    protected BookLoadEventListner eventListner;
    final String AmazonUrl = "https://www.amazon.co.jp/";

    public void SetEventListener(BookLoadEventListner listner) {
        eventListner = listner;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if(eventListner != null) {
            if(url.contains(AmazonUrl))
            {
                getBookTitleAmazon(view);
            } else {
                eventListner.OnBookLoad( view.getTitle() );
            }
        }
    }

    private void getBookTitleAmazon( final WebView view ) {
        String script = "document.getElementById('title').innerHTML;";    //productTitle
        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String retValue) {
                if( retValue == null || "null".equals(retValue) ) {
                    eventListner.OnBookLoad(view.getTitle());
                    return;
                }


                Log.d("script result", retValue);
                int idx = retValue.indexOf("\\n\\u003Cspan class=\\\"a-size-medium a-color-secondary a-text-normal\\\">\\u003C/span>\\n");
                if(idx != -1) {
                    retValue = retValue.substring(1, idx);
                }
                eventListner.OnBookLoad(retValue);
            }
        });
    }
}
