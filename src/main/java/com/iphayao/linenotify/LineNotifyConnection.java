package com.iphayao.linenotify;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LineNotifyConnection {
    private static final String URL_LINE_API = "https://notify-api.line.me/api/notify";
    private static HttpURLConnection preparedProperty(final String TOKEN) throws MalformedURLException, IOException {
        URL url = new URL(URL_LINE_API);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        StringBuilder Authorization = new StringBuilder("Bearer ");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", Authorization.append(TOKEN).toString());
        conn.setUseCaches(false);
        return conn;
    }
    
    //set body parameter for http request
    private static String preparedParameter(LineNotifyParameter l){
        StringBuilder urlParameters = new StringBuilder();
        urlParameters.append("message="+l.getMessage());
        if(l.getStickerId()>0 && l.getStickerPackageId()>0){
            urlParameters.append("&stickerPackageId="+l.getStickerId());
            urlParameters.append("&stickerId="+l.getStickerPackageId());
        }
        return urlParameters.toString();
    }

    //send data via http request
    public static int sendData(LineNotifyParameter lineData,final String TOKEN) throws IOException {
        String urlParameters = preparedParameter(lineData);
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        int statusCode = 500;
        HttpURLConnection conn = null;
        try {
	        conn = preparedProperty(TOKEN);
	        conn.setRequestProperty("charset", "utf-8");
	        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
	        conn.setUseCaches(false);
	        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
	            wr.write(postData);
	            wr.flush();
	        }
	        log.info("Limit Message"+conn.getHeaderFields().get("X-RateLimit-Remaining").toString());
	        if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
	             statusCode = conn.getResponseCode();
	            if (statusCode != 200) {
	                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
	            }
	        }
        } finally {
        	if(conn != null) {
        		conn.disconnect();
        	}
		}
        return statusCode;
    }
    
}
