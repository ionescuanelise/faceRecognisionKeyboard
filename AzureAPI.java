package com.example.facerecognitionemojikeyboard;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

public class AzureAPI {

    private final String apiEndpoint =
            "https://westeurope.api.cognitive.microsoft.com/face/v1.0";
    private String apiKey;
    private FaceServiceClient faceServiceClient;

    AzureAPI() {

        // Read in API keys
        String rawApiKeys = JSONParser.loadJSONFromAsset(SimpleIME.getAppContext(),
                "apiKeys.json");
        JSONObject apiKeys;
        try {
            apiKeys = new JSONObject(rawApiKeys);
            apiKey = apiKeys.getString("faceApi");
            faceServiceClient =
                    new FaceServiceRestClient(apiEndpoint, apiKey);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

//        Log.d("AZUREAPI", "Api Key: " + apiKey);
    }

    public CompletableFuture<Face[]> sendBitmap(final Bitmap imageBitmap) {

        // Prepare image for upload
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectFaces =
                new AsyncTask<InputStream, String, Face[]>() {

                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... inputStreams) {
                        try {
                            FaceServiceClient.FaceAttributeType[] attributes = {FaceServiceClient.FaceAttributeType.Emotion};
                            Face[] result = faceServiceClient.detect(
                                    inputStreams[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    attributes          // returnFaceAttributes:
                                /* new FaceServiceClient.FaceAttributeType[] {
                                    FaceServiceClient.FaceAttributeType.Age,
                                    FaceServiceClient.FaceAttributeType.Gender }
                                */
                            );
                            Log.d("AZUREAPI", "Finished call");
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = e.getMessage();
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        Log.d("AZUREAPI", "Started Detection");
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        if (!exceptionMessage.equals("")) {
                            Log.d("AZUREAPI", "Error: " + exceptionMessage);
                            return;
                        }

                        if (result == null || result.length == 0) {
                            Log.d("AZUREAPI", "No face found");
                            return;
                        }

                        EmotionData faceEmotion = new EmotionData(result[0]);
                        // TODO: Call function on face emotion
                        System.out.println(faceEmotion);

                        JSONToEmoji jtoe = null;
                        try {
                            jtoe = new JSONToEmoji();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Set<String> s = jtoe.getEmojis(faceEmotion.exportMap(), 1);
                        System.out.println("s.get(0) = " + s.iterator().next());
                    }

                };

        CompletableFuture<Face[]> future = CompletableFuture.supplyAsync(() -> {
                detectFaces.execute(inputStream);
                try {
                    return detectFaces.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
        });
        return future;
    }

}
