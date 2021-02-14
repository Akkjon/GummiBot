package de.akkjon.pr.mbrm;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HTTPSConnection {

    private final String strUrl;
    private HttpsURLConnection conn;
    private final String method;
    private int responseCode;
    private String response = "";
    private boolean isConnectionSuccess = true;
    private int contentLength;
    private boolean isResponseSuccess = true;


    public int getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response;
    }

    public boolean isConnectionSuccess() {
        return isConnectionSuccess;
    }

    public boolean isResponseSuccess() {
        return isResponseSuccess;
    }

    public int getContentLength() {
        return contentLength;
    }

    public HTTPSConnection(String url) {
        this.strUrl = url;
        this.method = "GET";
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
            this.isConnectionSuccess = false;
            return;
        }
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
            isResponseSuccess = false;
            return;
        }
        closeConnection();
    }

    private void connect() throws IOException {
        URL url = new URL(this.strUrl);
        this.conn = (HttpsURLConnection) url.openConnection();

        this.conn.setRequestMethod(this.method);
        this.conn.connect();
    }

    private void read() throws IOException {
        this.responseCode = conn.getResponseCode();
        if (this.responseCode == 200) {
            this.contentLength = this.conn.getContentLength();
            InputStream in = this.conn.getInputStream();
            this.response = new String(in.readAllBytes());
            in.close();
        } else {
            this.isResponseSuccess = false;
        }
    }

    private void closeConnection() {
        this.conn.disconnect();
    }
}
