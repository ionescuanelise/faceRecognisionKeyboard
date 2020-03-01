package com.example.facerecognitionemojikeyboard;

import com.microsoft.projectoxford.face.contract.Face;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EmotionData {

    public double anger;
    public double contempt;
    public double disgust;
    public double fear;
    public double happiness;
    public double neutral;
    public double sadness;
    public double surprise;

    public static Emotion stringToEnum(String s) {
        switch (s) {
            case "anger":
                return Emotion.ANGER;
            case "contempt":
                return Emotion.CONTEMPT;
            case "disgust":
                return Emotion.DISGUST;
            case "fear":
                return Emotion.FEAR;
            case "happiness":
                return Emotion.HAPPINESS;
            case "neutral":
                return Emotion.NEUTRAL;
            case "sadness":
                return Emotion.SADNESS;
            case "surprise":
                return Emotion.SURPRISE;
        }
        return null;
    }

    EmotionData(Face faceData) {
        anger = faceData.faceAttributes.emotion.anger;
        contempt = faceData.faceAttributes.emotion.contempt;
        disgust = faceData.faceAttributes.emotion.disgust;
        fear = faceData.faceAttributes.emotion.fear;
        happiness = faceData.faceAttributes.emotion.happiness;
        neutral = faceData.faceAttributes.emotion.neutral;
        sadness = faceData.faceAttributes.emotion.sadness;
        surprise = faceData.faceAttributes.emotion.surprise;
    }

    EmotionData(JSONObject rawData) throws JSONException {
        JSONObject scores = (JSONObject) rawData.get("scores");
        anger = (double) scores.get("anger");
        contempt = (double) scores.get("contempt");
        disgust = (double) scores.get("disgust");
        fear = (double) scores.get("fear");
        happiness = (double) scores.get("happiness");
        neutral = (double) scores.get("neutral");
        sadness = (double) scores.get("sadness");
        surprise = (double) scores.get("surprise");
    }

    HashMap<Emotion, Double> exportMap() {
        HashMap<Emotion, Double> mapData = new HashMap<>();
        mapData.put(Emotion.ANGER, anger);
        mapData.put(Emotion.CONTEMPT, contempt);
        mapData.put(Emotion.DISGUST, disgust);
        mapData.put(Emotion.FEAR, fear);
        mapData.put(Emotion.HAPPINESS, happiness);
        mapData.put(Emotion.NEUTRAL, neutral);
        mapData.put(Emotion.SADNESS, sadness);
        mapData.put(Emotion.SURPRISE, surprise);

        return mapData;
    }

}
