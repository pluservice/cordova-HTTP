/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class CordovaHttpPostJson extends CordovaHttp implements Runnable {

    private final int timeout;

    public CordovaHttpPostJson(String urlString, JSONObject jsonObj, Map<String, String> headers, final int timeout, CallbackContext callbackContext) {
        super(urlString, jsonObj, headers, callbackContext);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            this.setupSecurity(request);
            request.headers(this.getHeaders());
            request.acceptJson();
            request.contentType(HttpRequest.CONTENT_TYPE_JSON);
            request.readTimeout(timeout);
            request.connectTimeout(timeout);
            request.send(getJsonObject().toString());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
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
            } else if (e.getCause() instanceof SocketTimeoutException) {
                this.respondWithError(ERROR_CODES.CONNECTION_TIMEOUT, "Timeout");
            } else {
                this.respondWithError(ERROR_CODES.GENERIC_HTTP_REQUEST_EXCEPTION, "There was an error with the request");
            }
        } catch (Throwable t) {
            this.respondWithError(ERROR_CODES.INTERNAL_PLUGIN_ERROR, "Something evil happened!");
        }
    }
}
