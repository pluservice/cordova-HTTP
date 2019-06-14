/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class CordovaHttpPost extends CordovaHttp implements Runnable {

    private final int timeout;

    public CordovaHttpPost(String urlString, Map<?, ?> params, Map<String, String> headers, int timeout, CallbackContext callbackContext) {
        super(urlString, params, headers, callbackContext);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            request.form(this.getParams());
            request.readTimeout(timeout);
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            this.addResponseHeaders(request, response);
            response.put("status", code);
            if (code >= 200 && code < 300) {
                response.put("data", body);
                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError(ERROR_CODES.JSON_EXCEPTION, "There was an error generating the response");
        } catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(ERROR_CODES.HOST_NOT_RESOLVED, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError(ERROR_CODES.HANDSHAKE_FAILED, "SSL handshake failed");
            } else {
                this.respondWithError(ERROR_CODES.GENERIC_HTTP_REQUEST_EXCEPTION, "There was an error with the request");
            }
        }
    }
}
