package com.example.simplechatclient;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class ExampleWebServiceModel extends AbstractModel {

    private static final String TAG = "ExampleWebServiceModel";

    private static final String GET_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String POST_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String USERNAME = "USER";

    private MutableLiveData<JSONObject> jsonData;

    private String outputText;
    private String newMessage;

    private final ExecutorService requestThreadExecutor;
    private final Runnable httpGetRequestThread, httpPostRequestThread, httpDeleteRequestThread;
    private Future<?> pending;

    public ExampleWebServiceModel() {

        requestThreadExecutor = Executors.newSingleThreadExecutor();

        httpGetRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", GET_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

        httpPostRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", POST_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };

        httpDeleteRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("DELETE", POST_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };



    }

    public void initDefault() {

        newMessage = null;
        sendGetRequest();

    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String newText) {

        String oldText = this.outputText;
        this.outputText = newText;

        Log.i(TAG, "Output Text Change: From " + oldText + " to " + newText);

        firePropertyChange(DefaultController.ELEMENT_OUTPUT_PROPERTY, oldText, newText);

    }

    // Start GET Request (called from Controller)

    public void sendGetRequest() {
        httpGetRequestThread.run();
    }

    // Start POST Request (called from Controller)

    public void sendPostRequest(String message) {
        newMessage = message;

        httpPostRequestThread.run();
    }

    // Start DELETE Request (called from Controller)
    public void sendDeleteRequest() {
        httpDeleteRequestThread.run();
    }


    // Setter / Getter Methods for JSON LiveData

    private void setJsonData(JSONObject json) {

        this.getJsonData().postValue(json);

        try{
            String messages = json.getString("messages").toString();

            if(messages.equals("")){
                setOutputText("Nothing to show here. Post a message above to begin chatting...");
            }
            else{
                setOutputText(messages);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

    public MutableLiveData<JSONObject> getJsonData() {
        if (jsonData == null) {
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }

    // Private Class for HTTP Request Threads

    private class HTTPRequestTask implements Runnable {

        private static final String TAG = "HTTPRequestTask";
        private final String method, urlString;

        HTTPRequestTask(String method, String urlString) {
            this.method = method;
            this.urlString = urlString;
        }

        @Override
        public void run() {
            JSONObject results = doRequest(urlString);
            setJsonData(results);
        }

        /* Create and Send Request */
        private JSONObject doRequest(String urlString) {

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject output = null;
            JSONObject results = null;

            /* Log Request Data */

            try {

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Create Request */

                URL url = new URL(urlString);

                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000); /* ten seconds */
                conn.setConnectTimeout(15000); /* fifteen seconds */

                conn.setRequestMethod(method);
                conn.setDoInput(true);

                /* Add Request Parameters (if any) */

                if (method.equals("POST") ) {

                    conn.setDoOutput(true);

                    // Create request parameters (these will be echoed back by the example API)

                    StringBuilder p = new StringBuilder();

                    output = new JSONObject();

                    output.put("name", USERNAME);
                    output.put("message", newMessage);


                    // Reset newMessage value
                    newMessage = null;

                    // Write parameters to request body

                    OutputStream out = conn.getOutputStream();
                    out.write(output.toString().getBytes());
                    out.flush();
                    out.close();

                }

                /* Send Request */

                conn.connect();

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Get Reader for Results */

                int code = conn.getResponseCode();

                if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                    /* Read Response Into StringBuilder */

                    do {
                        line = reader.readLine();
                        Log.i(TAG, "Line: " + line);
                        if (line != null)
                            r.append(line);
                    }
                    while (line != null);

                }

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Parse Response as JSON */

                results = new JSONObject(r.toString());


            }
            catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            }
            finally {
                if (conn != null) { conn.disconnect(); }
            }

            /* Finished; Log and Return Results */

            Log.d(TAG, " JSON: " + r.toString());

            return results;

        }

    }

}