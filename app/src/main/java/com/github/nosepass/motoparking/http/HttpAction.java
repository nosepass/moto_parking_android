package com.github.nosepass.motoparking.http;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.github.nosepass.motoparking.MyLog;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An http request.
 * @param <Result> the parsed form of http response
 */
public abstract class HttpAction<Result>
{
    private static final String TAG = "http.HttpAction";

    public int attempts = 0;
    protected List<NameValuePair> params = new ArrayList<NameValuePair>();
    protected int statusCode;
    protected boolean errors;
    private Exception exception;
    protected String resultString;
    protected Result result;
    private HttpUriRequest request;

    static {
        //CookieManager.getInstance().removeAllCookie();
    }

    public void executeHttpRequest() {
        try {
            MyLog.v(TAG, "downloading stuff");

            DefaultHttpClient httpclient = new DefaultHttpClient();
            setHttpParameters(httpclient);
            loadCookiesFromWebview(httpclient.getCookieStore(), getCookieUrl());
            request = createRequest();
            String url = request.getRequestLine().getUri();
            MyLog.v(TAG, "url=" + url);
            HttpResponse response = httpclient.execute(request);
            saveCookiesToWebview(httpclient.getCookieStore());
            statusCode = response.getStatusLine().getStatusCode();
            resultString = EntityUtils.toString(response.getEntity());
            MyLog.v(TAG, url + " got response " + statusCode + ":\n" + resultString);
            if (resultString == null || resultString.equals("")) {
                MyLog.e(TAG, "empty response");
            }
            errors = false;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            errors = true;
            exception = e;
        }
    }

    /**
     * Override to modify Apache httpclient options before the connection is made.
     */
    protected void setHttpParameters(DefaultHttpClient httpclient) {

    }

    public void abortRequest() {
        if (request != null) {
            try {
                request.abort();
            } catch (UnsupportedOperationException e) {
                MyLog.e(TAG, e);
            }
        }
    }

    /**
     * Parse this.resultString to populate this.result.
     */
    public void parseResult() { }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean hasErrors() {
        return errors;
    }

    public Exception getException() {
        return exception;
    }

    public static void buildParams(List<NameValuePair> params, String ... paramPairs) {
        if (paramPairs != null) {
            for (int i = 0; i < paramPairs.length - 1; i+=2) {
                String name = paramPairs[i];
                String value = "null";
                if ((i + 1) < paramPairs.length) {
                    value = paramPairs[i+1];
                } else {
                    Log.w(TAG, "odd number of params, tossing in filler for last one");
                }
                params.add(new BasicNameValuePair(name, value));
            }
        }
    }

    protected void buildParams(String ... paramPairs) {
        buildParams(params, paramPairs);
    }

    protected static JSONObject buildJson(String ... kvPairs) {
        JSONObject json = new JSONObject();
        if (kvPairs != null) {
            for (int i = 0; i < kvPairs.length - 1; i+=2) {
                String name = kvPairs[i];
                String value = "null";
                if ((i + 1) < kvPairs.length) {
                    value = kvPairs[i+1];
                } else {
                    MyLog.w(TAG, "odd number of params, tossing in filler for last one");
                }
                try {
                    json.put(name, value);
                } catch (JSONException e) {
                    MyLog.e(TAG, e);
                }
            }
        }
        return json;
    }

    /**
     * Create an HttpGet or HttpPost request pointed at a certain url
     */
    protected abstract HttpUriRequest createRequest();

    //protected void onSuccess() {}

    public static HttpGet createHttpGetWithQueryParams(List<NameValuePair> params, String url) {
        return new HttpGet(url + "?" + URLEncodedUtils.format(params, "utf-8"));
    }

    protected HttpGet createHttpGetWithQueryParams(String url) {
        return createHttpGetWithQueryParams(params, url);
    }

    public static HttpPost createPostWithFormParams(List<NameValuePair> params, String url) {
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            MyLog.e(TAG, e);
        }
        return post;
    }

    protected HttpPost createPostWithFormParams(String url) {
        return createPostWithFormParams(params, url);
    }

    /**
     * Override to set what domain cookies related to this request belong in
     */
    protected String getCookieUrl() {
        return "";
    }

    /**
     * this has to be called if no webviews are created before CookieManager.getInstance()
     * otherwise a mysterious SIGSEGV occurs on Android 4.2
     */
    public static void initWebviewCookies(Context c) {
        CookieSyncManager.createInstance(c);
    }

    private void loadCookiesFromWebview(CookieStore store, String domainUrl) {
        // this is disabled because I don't feel like calling initWebviewCookies
//        String cookies = CookieManager.getInstance().getCookie(domainUrl);
//        if (cookies == null) {
//            MyLog.v(TAG, "webview cookies are null");
//            cookies = "";
//        }
//        String domain = getHostname(domainUrl);
//        String[] cookieValues = cookies.split(";");
//        BasicClientCookie cookie;
//        for (int i = 0; i < cookieValues.length; i++) {
//            String[] split = cookieValues[i].split("=");
//            if (split.length == 2)
//                cookie = new BasicClientCookie(split[0], split[1]);
//            else
//                cookie = new BasicClientCookie(split[0], null);
//
//            cookie.setDomain(domain);
//            store.addCookie(cookie);
//        }
    }

    private void saveCookiesToWebview(CookieStore store) {
//        CookieManager cm = CookieManager.getInstance();
//        for (Cookie c : store.getCookies()) {
//            cm.setCookie(c.getDomain(), c.getName() + "=" + c.getValue());
//        }
    }

    private String getHostname(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            MyLog.e(TAG, e);
            return "";
        }
    }
}