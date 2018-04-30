package com.rydeenworks.mybooksearch;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CalilWebViewClient extends WebViewClient {
    protected String bookTitle;

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        bookTitle = null;
    }
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if(bookTitle != null) {
            SetFormString(view, bookTitle);
            bookTitle = null;
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        bookTitle = null;
    }

    public void SetFormString(WebView view, String title) {
		/* 読み込み終了 */
        String script = "document.getElementById('query').value ='" + title +"';";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String retValue) {
//                    Log.d("script result", retValue);
                }
            });
        }
        else {
            view.loadUrl("javascript:" + script);
        }
    }
}
