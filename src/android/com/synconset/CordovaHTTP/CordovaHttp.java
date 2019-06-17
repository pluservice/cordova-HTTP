/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CordovaHttp {
    protected static final String TAG = "CordovaHTTP";
    protected static final String CHARSET = "UTF-8";

    private static AtomicBoolean sslPinning = new AtomicBoolean(false);
    private static AtomicBoolean acceptAllCerts = new AtomicBoolean(false);
    private static AtomicBoolean validateDomainName = new AtomicBoolean(true);
    private String urlString;
    private Map<?, ?> params;
    private JSONObject jsonObject;
    private Map<String, String> headers;
    private CallbackContext callbackContext;

    public CordovaHttp(String urlString, JSONObject jsonObj, Map<String, String> headers, CallbackContext callbackContext) {
        this.urlString = urlString;
        this.jsonObject = jsonObj;
        this.headers = headers;
        this.callbackContext = callbackContext;
    }

    public CordovaHttp(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        this.urlString = urlString;
        this.params = params;
        this.headers = headers;
        this.callbackContext = callbackContext;
    }

    public static void enableSSLPinning(boolean enable) {
        sslPinning.set(enable);
        if (enable) {
            acceptAllCerts.set(false);
        }
    }

    public static void acceptAllCerts(boolean accept) {
        acceptAllCerts.set(accept);
        if (accept) {
            sslPinning.set(false);
        }
    }

    public static void validateDomainName(boolean accept) {
        validateDomainName.set(accept);
    }

    protected String getUrlString() {
        return this.urlString;
    }

    protected Map<?, ?> getParams() {
        return this.params;
    }

    protected JSONObject getJsonObject() {
        return this.jsonObject;
    }

    protected Map<String, String> getHeaders() {
        return this.headers;
    }

    protected CallbackContext getCallbackContext() {
        return this.callbackContext;
    }

    protected HttpRequest setupSecurity(HttpRequest request) {
        if (acceptAllCerts.get()) {
            request.trustAllCerts();
        }
        if (!validateDomainName.get()) {
            request.trustAllHosts();
        }
        if (sslPinning.get()) {
            request.pinToCerts();
        }
        return request;
    }

    protected void respondWithError(ERROR_CODES status, String msg) {
        try {
            JSONObject response = new JSONObject();
            response.put("status", status.getValue());
            response.put("error", msg);
            this.callbackContext.error(response);
        } catch (JSONException e) {
            this.callbackContext.error(msg);
        }
    }

    protected void addResponseHeaders(HttpRequest request, JSONObject response) throws JSONException {
        Map<String, List<String>> headers = request.headers();
        Map<String, String> parsed_headers = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            if ((key != null) && (!value.isEmpty())) {
                parsed_headers.put(key, value.get(0));
            }
        }
        response.put("headers", new JSONObject(parsed_headers));
    }

//    protected void respondWithError(int status, String msg) {
//        try {
//            JSONObject response = new JSONObject();
//            response.put("status", status);
//            response.put("error", msg);
//            this.callbackContext.error(response);
//        } catch (JSONException e) {
//            this.callbackContext.error(msg);
//        }
//    }

//    protected void respondWithError(String msg) {
//        this.respondWithError(ERROR_CODES.LEGACY_PLUGIN_ERROR, msg);
//    }

    protected enum ERROR_CODES {
        HOST_NOT_RESOLVED(0),
        CONNECTION_TIMEOUT(-1001),
        OFFLINE(-1009),
        INTERNAL_PLUGIN_ERROR(666),
        // Potrei sembrare stronzo a mettere lo stesso codice ma ï¿½ perchï¿½ prima era tutto un 500
        // e non vorrei spaccare qualche comportamento strano lato app!
        JSON_EXCEPTION(500),
        HANDSHAKE_FAILED(500),
        GENERIC_HTTP_REQUEST_EXCEPTION(500),
        URI_SYNTAX_EXCEPTION(500),
        INVALID_PARAMS_EXCEPTION(500),
        LEGACY_PLUGIN_ERROR(500);

        public final int value;

        ERROR_CODES(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
