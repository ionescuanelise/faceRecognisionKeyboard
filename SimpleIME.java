package com.example.facerecognitionemojikeyboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.microsoft.projectoxford.face.contract.Face;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SimpleIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    public static final int RESULT_OK = -1;

    public static final String KEY_RECEIVER = "KEY_RECEIVER";

    public static final String KEY_MESSAGE = "KEY_MESSAGE";

    private KeyboardView kv;
    private Keyboard keyboard;

    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;


    private JSONToEmoji jsonToEmoji;
    private boolean caps = false;
    private Handler handler;


    private static Context context;

    private String[] emojis;
    private InputConnection ic;

    private boolean numbers = false;
    private boolean emojis_active = false;
    private boolean ready = false;

    private void updateEmojis(Face[] faces) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


        ic = getCurrentInputConnection();
        EmotionData emotionData = new EmotionData(faces[0]);

        Log.d("SIMPLEIME", "Calling jsonToEmoji");


        Set<String> emojis = jsonToEmoji.getEmojis(emotionData.exportMap(), 5);
        Log.d("SIMPLEIME", "Length of emojis: " + emojis.size());
        this.emojis = emojis.toArray(new String[emojis.size()]);
        String toCommit = this.emojis[0];


        Log.d("SIMPLEIME", "Finished jsonToEmoji");
        Log.d("SIMPLEIME", "Committing: " + toCommit);

        Log.d("SIMPLEIME", "Array length: " + this.emojis.length);

        sendEmoji(toCommit);
        updateEmojis();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setView(true);
            }
        });
    }

    private void updateEmojis() {
        Log.d("SIMPLEIME", "Updating emotion");
        for (int j = 0; j < 5; j++) {
            if (emojis != null && emojis.length > j) {
                Log.d("SIMPLEIME", "Enterd if statement");
                Keyboard.Key key = findKey(keyboard, -100 - j);
                int i2 = Integer.valueOf(this.emojis[j], 16);
                key.label = new String(Character.toChars(i2));
            }

        }
        ready = true;
    }

    private Keyboard.Key findKey(Keyboard keyboard, int primaryCode) {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes[0] == primaryCode) {
                return key;
            }
        }
        return null;
    }

    @Override
    public void onPress(int primaryCode) {
    }


    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onCreate() {
        // Handler will get associated with the current thread, 
        // which is the main thread.
        handler = new Handler(getMainLooper());

        super.onCreate();
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public View onCreateInputView() {
        emojis_active = false;
        // Setting context
        setView(true);
        context = getApplicationContext();
        try {
            jsonToEmoji = new JSONToEmoji();
        } catch (Exception e) {
            e.printStackTrace();
        }

        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);

        setView(true);
        return kv;
    }

    private View setView(boolean iSet) {
        if (numbers && !emojis_active) {
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
            keyboard = new Keyboard(this, R.xml.numbers);
        } else if (!numbers && !emojis_active){
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
            keyboard = new Keyboard(this, R.xml.qwerty);
        } else if (numbers && emojis_active) {
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
            keyboard = new Keyboard(this, R.xml.qwerty_with_emojis);
        } else { // !numbers && emojis_active
            kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
            keyboard = new Keyboard(this, R.xml.numbers_with_emojis);
        }

        if (emojis_active) {
            updateEmojis();
        }

        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        if (iSet) {
            setInputView(kv);
        }

        return kv;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendEmoji(String code) {
        ic = getCurrentInputConnection();
        int i = Integer.valueOf(code, 16);
        String s = new String(Character.toChars(i));
        Log.d("SIMPLEIME", "Sending emoji to keyboard: " + code);
        ic.commitText(s, 1);
    }



    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        ic = getCurrentInputConnection();
        playClick(primaryCode);
        if (ready){
            for (int j = 0; j < 5; j++) {
                if (emojis != null && emojis.length > j) {
                    Keyboard.Key key = findKey(keyboard, -100 - j);
                    int i2 = Integer.valueOf(this.emojis[j], 16);
                    key.label = new String(Character.toChars(i2));
                }

            }
        }
        keyboard.setShifted(caps);
        kv.invalidateAllKeys();
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case -10:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(KEY_RECEIVER, new MessageReceiver());
                startActivity(intent);
                emojis_active = true;
                numbers = false;
                setView(true);

                break;

            case -6:
                numbers = !numbers;
                setView(true);
                break;

            // Handling emojis
            case -100:
                if (emojis.length > 0) {
                    sendEmoji(emojis[0]);
                }
                break;
            case -101:
                if (emojis.length > 1) {
                    sendEmoji(emojis[1]);
                }
                break;
            case -102:
                if (emojis.length > 2) {
                    sendEmoji(emojis[2]);
                }
                break;
            case -103:
                if (emojis.length > 3) {
                    sendEmoji(emojis[3]);
                }
                break;
            case -104:
                if (emojis.length > 4) {
                    sendEmoji(emojis[4]);
                }
                break;
            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code), 1);
        }
    }

    class MessageReceiver extends ResultReceiver {

        public MessageReceiver() {
            // Pass in a handler or null if you don't care about the thread
            // on which your code is executed.
            super(null);
        }

        /**
         * Called when there's a result available.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Define and handle your own result codes
            if (resultCode != RESULT_OK) {
                return;
            }

            // Let's assume that a successful result includes a message.

            Bitmap bitmap = (Bitmap) resultData.getParcelable(KEY_MESSAGE);


            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            Log.d("SIMPLEIME", "Starting azure api");
            AzureAPI a = new AzureAPI();
            CompletableFuture<Face[]> future = a.sendBitmap(bitmap);
            future.thenApply((Face[] faces) -> {
                Log.d("SIMPLEIME", "Starting then apply");
                if (faces == null || faces.length == 0) {
                    Log.d("SIMPLEIME", "Stopping due to no face");
                    showToast("No face detected");
                    System.out.println("no face");
                } else {
                    updateEmojis(faces);
                }
                return faces;
            });
        }

    }


    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
    }

    public static Context getAppContext() {
        return context;
    }

}