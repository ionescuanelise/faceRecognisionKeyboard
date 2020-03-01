package com.example.facerecognitionemojikeyboard;

import android.util.Log;

import com.example.facerecognitionemojikeyboard.Emotion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONToEmoji {

    public static void main(String[] args) throws JSONException {
        JSONToEmoji test = new JSONToEmoji();

    }

    private Map<Emotion, Set<String>> emoToEmojis = new HashMap<>();

    public JSONToEmoji() throws JSONException {
        String rawData = JSONParser.loadJSONFromAsset(SimpleIME.getAppContext(), "emojisByEmotion.JSON");
        JSONObject data = new JSONObject(rawData);
        Iterator<String> iter = data.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            Emotion emo = EmotionData.stringToEnum(key);
            Set<String> emojis = new HashSet<String>();
            emoToEmojis.put(emo, emojis);
            JSONArray unicodes = data.getJSONArray(key);
            for (int i = 0; i < unicodes.length(); i++) {
                emojis.add((String) unicodes.get(i)); // TODO: does this work?
            }
        }
    }

    // make sure n <= size of emoji sets for each emotion
    public Set<String> getEmojis(Map<Emotion, Double> scores, int n) {

        Set<String> emojis = new HashSet<>();
        List<Emotion> sigEmos = getSignificantEmotions(scores);

        for (Emotion emo : sigEmos) {
            if (emojis.size() < n) {
                addEmojisByEmotion(emojis, emo, Math.min(n - emojis.size(), (int) Math.ceil(scores.get(emo) * (double) n)));
            } else {
                break;
            }
        }

        return emojis;
    }

    private void addEmojisByEmotion(Set<String> addTo, Emotion emo, int n) {
        Set<String> src = emoToEmojis.get(emo);
        Iterator iter = src.iterator();
        while (n > 0 && iter.hasNext()) {
            addTo.add((String) iter.next());
            n--;
        }
    }

    private List<Emotion> getSignificantEmotions(Map<Emotion, Double> scores) {
        List<Emotion> emotions = new LinkedList<>();
        for (Emotion emotion : scores.keySet()) {
            if (scores.get(emotion) > 0.1) {
                emotions.add(emotion);
            }
        }
        return emotions;
    }



}
