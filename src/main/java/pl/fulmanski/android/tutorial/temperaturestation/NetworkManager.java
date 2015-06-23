package pl.fulmanski.android.tutorial.temperaturestation;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by T540p on 2015-05-20.
 */
public class NetworkManager {
    //An exception is thrown when application attempts to perform a networking operation in the main thread.
    void sendGET(URL _url){
        final URL url =  _url;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    _sendGET(url);
                } catch (Exception e) {
                    Log.e("TAG1", e.getMessage());
                }
            }
        });
        thread.start();
    }

    void _sendGET(URL url) {

        // this does no network IO
        HttpURLConnection conn=null ;//= url.openConnection();
        InputStream in;
        int http_status;
        try {
            conn = (HttpURLConnection)url.openConnection();
            // this opens a connection, then sends GET & headers
            in = conn.getInputStream();

            // can't get status before getInputStream.  If you try, you'll
            //  get a nasty exception.
            http_status = conn.getResponseCode();

            // better check it first
            if (http_status / 100 != 2) {
                // redirects, server errors, lions and tigers and bears! Oh my!
            }
        } catch (IOException e) {
            // Something horrible happened, as in a network error, or you
            //  foolishly called getResponseCode() before HUC was ready.
            // Essentially no methods of on "conn" now work, so don't go
            //  looking for help there.
            Log.e("TAG2", e.getMessage());
        }

//        try {
//            // now you can try to consume the data
//            //try_reading(in);
//        } catch (IOException e) {
//            // Network-IO lions and tigers and bears! Oh my!
//        } finally {
            // Do like Mom said and practice good hygiene.
            conn.disconnect();
//        }
    }

        void _sendPOST(URL url, byte[] payload) {

        // this does no network IO.
        HttpURLConnection conn=null;// = (HttpURLConnection)url.openConnection();


        InputStream in;
        OutputStream out=null;
        int http_status;
        try {
            conn = (HttpURLConnection)url.openConnection();
            // tells HUC that you're going to POST; still no IO.
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(payload.length); // still no IO
            // this opens a connection, then sends POST & headers.
            out = conn.getOutputStream();

            // At this point, the client may already have received a 4xx
            //  or 5xx error, but don't you dare call getResponseCode()
            //  or HUC will hit you with an exception.
        } catch (IOException e) {
            // some horrible networking error, don't try any methods on "conn".
        }
        try {

            // now we can send the body
            out.write(payload);

            // NOW you can look at the status.
            http_status = conn.getResponseCode();
            if (http_status / 100 != 2) {
                // Dear me, dear me
            }
        } catch (IOException e) {
            // Network-IO lions and tigers and bears! Oh my!
        }

        // presumably you're interested in the response body
        try {

            // Unlike the identical call in the previous example, this
            //  provokes no network IO.
            in = conn.getInputStream();
            //try_reading(in);
        } catch (IOException e) {
            // Network-IO lions and tigers and bears! Oh my!
        } finally {
            conn.disconnect(); // Let's practice good hygiene
        }
    }
}
