package com.rydeenworks.mybooksearch;


import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/*
 * This class shows how to make a simple authenticated call to the
 * Amazon Product Advertising API.
 *
 * See the README.html that came with this sample for instructions on
 * configuring and running the sample.
 */
public class AmazonRequest extends AsyncTask<String, Void, String> {

    public final static String AMAZON_REQUEST_TYPE_ITEM_LOOKUP = "ItemLookup";
    public final static String AMAZON_REQUEST_TYPE_ITEM_SEARCH = "ItemSearch";
    private Context context;
    private int requestCount = 0;
    private final static int REQUEST_RETRY_MAX = 3;

    /*
     * Your Access Key ID, as taken from the Your Account page.
     */
    private static String ACCESS_KEY_ID;

    /*
     * Your Secret Key corresponding to the above ID, as taken from the
     * Your Account page.
     */
    private static String SECRET_KEY;

    private static String ASSOCIATE_TAG;

    /*
     * Use the end-point according to the region you are interested in.
     */
    private static final String ENDPOINT = "webservices.amazon.co.jp";

    private Listener listener;

    public void Init(Context context, String access_key_id, String secret_key, String associate_tag)
    {
        this.context = context;
        ACCESS_KEY_ID = access_key_id;
        SECRET_KEY = secret_key;
        ASSOCIATE_TAG = associate_tag;
    }

    @Override
    protected String doInBackground(String... keyword) {
        if (keyword.length != 2) {
            return null;
        }

        String searchType = keyword[0];
        String requestUrl = null;
        if (AMAZON_REQUEST_TYPE_ITEM_LOOKUP.equals(searchType)) {
            String isbn10 = keyword[1];
            requestUrl = GetItemLookupRequestUrl(isbn10);
        } else if (AMAZON_REQUEST_TYPE_ITEM_SEARCH.equals(searchType)) {
            //NO TESTED
            String searchWord = keyword[1];
            requestUrl = GetItemSearchRequestUrl(searchWord);
        }

        if (requestUrl == null) {
            return null;
        }
        String xml = null;
        while (requestCount < REQUEST_RETRY_MAX) {
            requestCount++;
            xml = GetAmazonRequest(requestUrl);
            if(xml != null) {
                break;
            }
        }
        return xml;
    }


    @Override
    protected void onPostExecute(String result) {
        if (listener == null || context == null) {
            return;
        }

        if ( result == null ) {
            return;
        }

        listener.onSuccess(context, result);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        //only support ItemLookup
        void onSuccess(Context context, String result);
    }

    private String GetAmazonRequest(String requestUrl) {

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        try{
            // リクエスト処理
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            String charSet = "UTF-8";
            String method = "GET";
            httpURLConnection.setRequestMethod( method );
            InputStreamReader inputStreamReader = new InputStreamReader( httpURLConnection.getInputStream(), charSet );
            bufferedReader = new BufferedReader( inputStreamReader );

            // レスポンスのXMLを取得。
            String responseXml = "";
            while( true ){
                String oneLine = bufferedReader.readLine();
                // 行がNULLの場合
                if(oneLine == null){
                    break;
                    // レスポンスの文字列へ行を追加
                }else{
                    responseXml += oneLine;
                }
            }
            return responseXml;
        }catch( Exception e ){
            // exceptionの時はnullを返す
            System.out.println(e.toString());
            return null;
        }finally{

            if( bufferedReader != null ){
                try{
                    bufferedReader.close();
                }catch( IOException e ){
                    // エラー時は無処理
                }
            }
            if( httpURLConnection != null ){
                httpURLConnection.disconnect();
            }
        }

    }

    private String GetItemLookupRequestUrl(String isbn10) {
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Map<String, String> params = new HashMap<>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemLookup");
        params.put("AWSAccessKeyId", ACCESS_KEY_ID);
        params.put("AssociateTag", ASSOCIATE_TAG);
        params.put("IdType", "ISBN");
        params.put("ItemId", isbn10);
        params.put("SearchIndex", "Books");

        String requestUrl = helper.sign(params);
        return requestUrl;
    }


    private String GetItemSearchRequestUrl(String keyword) {

        /*
         * Set up the signed requests helper.
         */
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Map<String, String> params = new HashMap<>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", ACCESS_KEY_ID);
        params.put("AssociateTag", ASSOCIATE_TAG);
        params.put("SearchIndex", "Books");
        params.put("Keywords", keyword);
        params.put("ResponseGroup", "Images,ItemAttributes,Small");

        String requestUrl = helper.sign(params);
        return requestUrl;
    }
}
