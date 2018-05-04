package com.rydeenworks.mybooksearch;

import android.graphics.Bitmap;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookWebViewClient extends WebViewClient {
    private BookLoadEventListener eventListener;
    private final String AmazonUrl = "https://www.amazon.co.jp/";
    private boolean isFirstFinish = true;

    public void SetEventListener(BookLoadEventListener listener) {
        eventListener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        isFirstFinish = true;
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if(!isFirstFinish) {    //AJAX対策として１回目のfinishしか処理しないようにする
            return;
        }
        isFirstFinish = false;

        if(eventListener != null) {
            if(url.startsWith(AmazonUrl))
            {
                getBookTitleAmazon(view, url);
            } else {
                eventListener.OnBookLoad( view.getTitle(), url );
            }
        }

    }

    private String getISBN(final String amazonUrl) {
        //"https://www.amazon.co.jp/" を取り除いて検索する(ただし最後のバックスラッシュは残す)
        String target = amazonUrl.substring(AmazonUrl.length()-1);
        Pattern pattern = Pattern.compile("/[0-9]{9}[0-9X]");
        Matcher matcher = pattern.matcher(target);

        if (matcher.find()) {
            String isbn = matcher.group();
            return isbn.substring(1);   //最初のバックスラッシュを削除
        }
        return "";
    }
    private void getBookTitleAmazon(final WebView view, final String url ) {
        String script = "document.getElementById('title').innerHTML;";    //productTitle
        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String retValue) {
                if( retValue == null || "null".equals(retValue) ) {
                    isFirstFinish = true;
                    return;
                }

                int idx = retValue.indexOf("\\n\\u003Cspan class=\\\"a-size-medium a-color-secondary a-text-normal\\\">\\u003C/span>\\n");
                if(idx == -1) {
                    eventListener.OnBookLoad( null, url);
                } else {
                    String bookTitle = retValue.substring(1, idx);
                    eventListener.OnBookLoad(bookTitle, url);

                    String isbn = getISBN(url);
                    if(!isbn.isEmpty()) {
                        eventListener.OnAddAmazonBookHistory(bookTitle, isbn);
                    }
                }
            }
        });

    }
}
