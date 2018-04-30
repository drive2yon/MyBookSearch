package com.rydeenworks.mybooksearch;


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

    public void Init(String access_key_id, String secret_key, String associate_tag)
    {
        ACCESS_KEY_ID = access_key_id;
        SECRET_KEY = secret_key;
        ASSOCIATE_TAG = associate_tag;
    }

    @Override
    protected String doInBackground(String... keyword) {
        String xml = GetItemXml(keyword[0]);
        //xml -> html
        return xml;
    }


    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onSuccess(result);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onSuccess(String result);
    }


    protected String GetItemXml(String keyword) {

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        try{
            // リクエスト処理
            String requestUrl = getRequestUrl(keyword);
            URL url = new URL(requestUrl);
            httpURLConnection = (HttpURLConnection)url.openConnection();
            String charSet = "UTF-8";
            String method = "GET";
            httpURLConnection.setRequestMethod( method );
            InputStreamReader inputStreamReader = new InputStreamReader( httpURLConnection.getInputStream(), charSet );
            bufferedReader = new BufferedReader( inputStreamReader );

            // レスポンスのXMLを取得。
            String oneLine = null;
            String responseXml = "";
            while( true ){
                oneLine = bufferedReader.readLine();
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


    public static String getRequestUrl(String keyword) {

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

        String requestUrl = null;

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", ACCESS_KEY_ID);
        params.put("AssociateTag", ASSOCIATE_TAG);
        params.put("SearchIndex", "Books");
        params.put("Keywords", keyword);
        params.put("ResponseGroup", "Images,ItemAttributes,Small");

        requestUrl = helper.sign(params);

        return requestUrl;
    }
}
