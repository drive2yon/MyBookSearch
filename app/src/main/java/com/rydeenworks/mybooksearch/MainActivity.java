package com.rydeenworks.mybooksearch;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import static com.rydeenworks.mybooksearch.MainActivity.ViewLayout.*;

public class MainActivity extends AppCompatActivity implements BookLoadEventListner {
    enum ViewLayout {
        VIEW_LAYOUT_MULTI,
        VIEW_LAYOUT_BOOK,
        VIEW_LAYOUT_CALIL,
    }
    ViewLayout viewLayout = VIEW_LAYOUT_MULTI;

    WebView bookWebView;
    WebView calilWebView;
    BookWebViewClient bookWebViewClient = new BookWebViewClient();
    CalilWebViewClient calilWebViewClient = new CalilWebViewClient();
    WebView focusedView;

    String lastBookWebUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCalilWebView();

        initBookWebView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if ( bookWebView != null ) {
            // Get intent, action and MIME type
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            String bookPageUrl = "https://www.google.co.jp";
            if (Intent.ACTION_SEND.equals(action) && type != null && "text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    bookPageUrl = sharedText;
                }
            }

            //異なるページの場合は表示更新する
            if ( lastBookWebUrl == null || !lastBookWebUrl.equals(bookPageUrl)) {
                bookWebView.loadUrl(bookPageUrl);
                bookWebView.requestFocus(View.FOCUS_DOWN | View.FOCUS_UP);
                lastBookWebUrl = bookPageUrl;

                if( calilWebView != null ) {
                    calilWebView.loadUrl("https://calil.jp");
                    calilWebView.requestFocus(View.FOCUS_DOWN|View.FOCUS_UP);
                }
            }
        }
    }

    void initBookWebView() {
        if(bookWebView == null) {
            bookWebView = (WebView) findViewById(R.id.webView_main);
            bookWebView.setWebViewClient(bookWebViewClient);
            bookWebView.getSettings().setJavaScriptEnabled(true);
            bookWebView.setFocusable(true);
            bookWebView.setFocusableInTouchMode(true);
            bookWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                                OnViewFocus(v);
                            }
                            break;
                    }
                    return false;
                }
            });

            bookWebViewClient.SetEventListener(this);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void initCalilWebView() {
        if(calilWebView == null) {
            calilWebView = (WebView) findViewById(R.id.webView_calil);
            //リンクをタップしたときに標準ブラウザを起動させない
            calilWebView.setWebViewClient(calilWebViewClient);
            //jacascriptを許可する
            calilWebView.getSettings().setJavaScriptEnabled(true);
            calilWebView.setFocusable(true);
            calilWebView.setFocusableInTouchMode(true);
            calilWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                                OnViewFocus(v);
                            }
                            break;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void OnBookLoad(String bookTitle) {
        if( bookTitle != null ) {
            calilWebViewClient.SetFormString(calilWebView, bookTitle);
            Toast toast = Toast.makeText(this, bookTitle, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, "本のタイトルが取れませんでした", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void OnViewFocus(View view)
    {
        if(view == bookWebView) {
            focusedView = (WebView) view;
        } else if (view == calilWebView) {
            focusedView = (WebView) view;
        } else {
            focusedView = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch_layout:
                switchLayout();
                break;
            case R.id.menu_show_recommend_page:
                showRecommendPage();
                break;
        }
        return true;
    }

    protected void switchLayout() {
        switch (viewLayout) {
            case VIEW_LAYOUT_MULTI:
                calilWebView.setVisibility(View.GONE);
                viewLayout = VIEW_LAYOUT_BOOK;
                break;
            case VIEW_LAYOUT_BOOK:
                bookWebView.setVisibility(View.GONE);
                calilWebView.setVisibility(View.VISIBLE);
                viewLayout = VIEW_LAYOUT_CALIL;
                break;
            case VIEW_LAYOUT_CALIL:
                bookWebView.setVisibility(View.VISIBLE);
                viewLayout = VIEW_LAYOUT_MULTI;
                break;
        }
    }

    protected void showRecommendPage() {
        AmazonRequest request = new AmazonRequest();
        request.Init(getString(R.string.access_key_id), getString(R.string.secret_access_key), getString(R.string.associate_tag));
        request.setListener(createListener());
        request.execute("xamarin");
    }

    private AmazonRequest.Listener createListener() {
        return new AmazonRequest.Listener() {
            @Override
            public void onSuccess(String html) {
                if(html != null) {
//                bookWebView.loadData();
                }
            }
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 端末の戻るボタンでブラウザバック
        if( keyCode == KeyEvent.KEYCODE_BACK ) {
            WebView webView = null;
            switch (viewLayout) {
                case VIEW_LAYOUT_MULTI:
                    webView = focusedView;
                    break;
                case VIEW_LAYOUT_BOOK:
                    webView = bookWebView;
                    break;
                case VIEW_LAYOUT_CALIL:
                    webView = calilWebView;
                    break;
            }
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode,  event);
    }
}
