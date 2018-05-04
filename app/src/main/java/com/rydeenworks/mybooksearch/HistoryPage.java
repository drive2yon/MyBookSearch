package com.rydeenworks.mybooksearch;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryPage {
    private final int HISTORY_MAX_NUM = 20;

    public void AddHistory (Context context, String keyword, String isbn10) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_history_file_key), Context.MODE_PRIVATE);

        //登録済みの履歴は追加しない
        if( isExistHistoryKeyword(context, sharedPref, keyword) ) {
            return;
        }
        requestAmazonBookURL(context, isbn10);
    }

    private AmazonRequest.Listener createListener() {
        return new AmazonRequest.Listener() {
            @Override
            public void onSuccess(Context context, String responseXML) {
                if(responseXML != null) {
                    //XMLからURL取得する
                    String title = "";
                    String url = "";
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.preference_history_file_key), Context.MODE_PRIVATE);
                    int lastIndex = getLastIndex(context, sharedPref);
                    lastIndex = incrementLastIndex(lastIndex);

                    {
                        Pattern pattern = Pattern.compile("<DetailPageURL>https://www\\.amazon\\.co\\.jp/.*</DetailPageURL>");
                        Matcher matcher = pattern.matcher(responseXML);
                        if (matcher.find()) {
                            url = matcher.group();
                            url = url.substring(15, url.length()-16);
                        }
                    }

                    {
                        Pattern pattern = Pattern.compile("<Title>.*</Title>");
                        Matcher matcher = pattern.matcher(responseXML);
                        if (matcher.find()) {
                            title = matcher.group();
                            title = title.substring(7, title.length()-8);
                        }
                    }

                    if(title.isEmpty() || url.isEmpty()) {
                        return;
                    }

                    saveHistory(sharedPref, lastIndex, title, url);
                    saveLastIndex(context, sharedPref, lastIndex);
                }
            }
        };
    }


    private void requestAmazonBookURL(Context context, String isbn10) {
        AmazonRequest request = new AmazonRequest();
        request.Init(context,
                context.getString(R.string.access_key_id),
                context.getString(R.string.secret_access_key),
                context.getString(R.string.associate_tag));
        request.setListener(createListener());
        request.execute( AmazonRequest.AMAZON_REQUEST_TYPE_ITEM_LOOKUP, isbn10);
    }

    private int incrementLastIndex(int lastIndex) {
        lastIndex++;
        if(lastIndex == HISTORY_MAX_NUM) {
            lastIndex = 0;
        }
        return lastIndex;
    }

    private int decrementLastIndex(int lastIndex) {
        lastIndex--;
        if(lastIndex == -1) {
            lastIndex = HISTORY_MAX_NUM -1;
        }
        return lastIndex;
    }

    private int getLastIndex(Context context, SharedPreferences sharedPref) {
        int lastIndex = -1;
        lastIndex = sharedPref.getInt(context.getString(R.string.history_last_index_key), lastIndex);
        return lastIndex;
    }

    private void saveLastIndex(Context context, SharedPreferences sharedPref, int lastIncdx) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.history_last_index_key), lastIncdx);
        editor.commit();
    }

    private void saveHistory(SharedPreferences sharedPref, int lastIndex, String keyword, String url) {
        SharedPreferences.Editor editor = sharedPref.edit();
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(keyword);
        jsonArray.put(url);
        editor.putString(Integer.toString(lastIndex), jsonArray.toString());
        editor.commit();
    }

    private ArrayList< JSONArray > getHistoryList(Context context, SharedPreferences sharedPref) {
        ArrayList<JSONArray> historyList = new ArrayList<>();

        final int lastIndex = getLastIndex(context, sharedPref);
        int currIndex = lastIndex;
        do {
            String strJson = sharedPref.getString(Integer.toString(currIndex), "");
            if(strJson.isEmpty()) {
                break;
            }
            try {
                JSONArray jsonAry = new JSONArray(strJson);
                historyList.add(jsonAry);
            } catch(Exception ex) {
                ex.printStackTrace();
                break;
            }
            currIndex = decrementLastIndex(currIndex);
        }while (currIndex != lastIndex);
        return  historyList;
    }

    private boolean isExistHistoryKeyword(Context context, SharedPreferences sharedPref, final String keyword) {
        ArrayList<JSONArray> historyList = getHistoryList(context, sharedPref);
        try {
            for (JSONArray jsonArray : historyList) {
                if(keyword.equals(jsonArray.get(0))) {
                    return true;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return false;
    }

    public String GetWebPage (Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_history_file_key), Context.MODE_PRIVATE);

        ArrayList<JSONArray> historyList = getHistoryList(context, sharedPref);
        String webPage = createHtml(context, historyList);
        return webPage;
    }

    private String createHtml(Context context, ArrayList<JSONArray> historyList) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        htmlBuilder.append("<title>");
        htmlBuilder.append(context.getString(R.string.web_page_title));
        htmlBuilder.append("</title>");
        htmlBuilder.append("<style type=\"text/css\">");
        htmlBuilder.append(".header {");
        htmlBuilder.append("background-color: #bdf7f1;");
        htmlBuilder.append("}");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");

        htmlBuilder.append("<body>");
        htmlBuilder.append("<div class=\"header\">");
        htmlBuilder.append("履歴一覧");
        htmlBuilder.append("</div>");
        try {
            htmlBuilder.append("<br>");
            for (JSONArray jsonArray : historyList) {
                // 普通のリンクを設定
                htmlBuilder.append("<a href=\"");
                htmlBuilder.append(jsonArray.get(1));   //URL
                htmlBuilder.append("\">");
                htmlBuilder.append(jsonArray.get(0));   //keyword
                htmlBuilder.append("</a>");
                htmlBuilder.append("<hr>");
                htmlBuilder.append("<br>");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            return "";
        }

        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }
}
