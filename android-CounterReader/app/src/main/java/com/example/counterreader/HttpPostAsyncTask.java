package com.example.counterreader;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpPostAsyncTask extends AsyncTask<String, Void, String> {
    // This is the JSON body of the post
    JSONObject postData;
    RequestType type;
    CustomCallback callback;


    // This is a constructor that allows you to pass in the JSON body
    public HttpPostAsyncTask(Map<String, String> postData, RequestType type, CustomCallback callback) {
        if (postData != null) {
            this.postData = new JSONObject(postData);
            this.type = type;
            this.callback = callback;
        }
    }

    // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
    @Override
    protected String doInBackground(String... params) {

        String result = null;
        try {
            result = "";
            // This is getting the url from the string we passed in
            URL url = new URL(params[0]);

            // Create the urlConnection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.setRequestMethod("POST");


            // OPTIONAL - Sets an authorization header
            // urlConnection.setRequestProperty("Authorization", "someAuthString");

            // Send the post body
            if (this.postData != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                writer.write(postData.toString());
                writer.flush();
            }

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 200) {

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                String response = convertInputStreamToString(inputStream);
                result = response;

                Log.d("response: ", response);


                switch (type) {
                    case REQUEST_TYPE_1:
                        // Use the response to create the object you need
                        // callback.completionHandler(true, type);
                        break;
                    case REQUEST_TYPE_2:
                        // Do something
                        break;
                    default:
                        break;
                }


                // From here you can convert the string to JSON with whatever JSON parser you like to use
                // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
            } else {
                // Status code is not 200
                // Do something to handle the error
            }

        } catch (Exception e) {


            // callback.completionHandler(false, type);
            Log.d("ddd", "pppp");
            Log.d("TAG", e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        switch (type) {
            case REQUEST_TYPE_1:

                if (result.contains("Success")){
                    Log.d("Status1:", "Success");
                    callback.completionHandler(true, type);
                }
                else{
                    Log.d("Status1:", "fail");
                    callback.completionHandler(false, type);
                }

                break;
            case REQUEST_TYPE_2:

                if (result.contains("Success")) {
                    MainActivity.counter = MainActivity.counter - 1;
                    Log.d("", "counter :" + String.valueOf(MainActivity.counter));
                    Log.d("REQUEST_TYPE_2", " 1");
                    MainActivity.upload_result = 1;
                }
                else{
                    Log.d("", "counter :" + String.valueOf(MainActivity.counter));
                    Log.d("REQUEST_TYPE_2", " 0");
                    MainActivity.upload_result = 0;
                }

                if (MainActivity.counter == 0){
                    if (result.contains("Success")){
                        Log.d("Status2:", "Success");
                        callback.completionHandler(true, type);
                    }
                    else{
                        Log.d("Status2:", "fail");
                        callback.completionHandler(false, type);
                    }
            }
            else{

            }
                break;
            default:
                break;
        }


    }

    private String convertInputStreamToString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
