package com.example.disxa;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class lecturanivel2 extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static final String TEXTO_NIVEL1 =
            "El cielo está azul.\n" +
                    "No hay nubes.\n" +
                    "\n" +
                    "Luis sale de casa.\n" +
                    "Camina hacia el parque.\n" +
                    "Le gusta el aire libre.\n" +
                    "\n" +
                    "En el parque hay árboles.\n" +
                    "También hay flores de colores.\n";

    private final int[] gifDrawables = {
            R.drawable.perrito,
            R.drawable.gatocompleto,
            R.drawable.bano,
            R.drawable.obeja,
            R.drawable.dino
    };

    private TextView    tvHighlightedText;
    private ImageButton btnMicrophone;
    private Button      btnReset;
    private TextView    tvStatus;
    private View        micPulseRing;
    private ImageView   gifPersonaje;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;

    private List<String> targetWords;
    private int currentWordIndex = 0;

    // ─── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturanivel1);

        tvHighlightedText = findViewById(R.id.tvHighlightedText);
        btnMicrophone     = findViewById(R.id.btnMicrophone);
        btnReset          = findViewById(R.id.btnReset);
        tvStatus          = findViewById(R.id.tvStatus);
        micPulseRing      = findViewById(R.id.micPulseRing);
        gifPersonaje      = findViewById(R.id.gifPersonaje);


        cargarGifSeleccionado();

        renderHighlighted(TEXTO_NIVEL1, new ArrayList<>(), new ArrayList<>());
        tvStatus.setText("Presiona el micrófono para comenzar");

        setupSpeechRecognizer();

        btnMicrophone.setOnClickListener(v -> {
            if (checkPermission()) {
                if (isListening) stopListening();
                else             startListening();
            } else {
                requestPermission();
            }
        });

        btnReset.setOnClickListener(v -> resetSession());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se actualiza el GIF si el usuario regresó desde Perzonalisacion
        cargarGifSeleccionado();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }

    // ─── GIF ────────────────────────────────────────────────────────────────

    private void cargarGifSeleccionado() {
        SharedPreferences prefs = getSharedPreferences(
                Perzonalisacion.PREFS_NAME, MODE_PRIVATE
        );
        int indice      = prefs.getInt(Perzonalisacion.KEY_GIF_SELECCIONADO, 0);
        int drawableRes = gifDrawables[indice];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AnimatedImageDrawable gif =
                    (AnimatedImageDrawable) getDrawable(drawableRes);
            if (gif != null) {
                gif.start();
                gifPersonaje.setImageDrawable(gif);
            }
        } else {
            gifPersonaje.setImageResource(drawableRes);
        }
    }

    // ─── Speech Recognizer ──────────────────────────────────────────────────

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override public void onReadyForSpeech(Bundle p) { tvStatus.setText("Escuchando… habla ahora"); }
            @Override public void onBeginningOfSpeech()       { tvStatus.setText("Procesando tu voz…"); }
            @Override public void onRmsChanged(float r)       { }
            @Override public void onBufferReceived(byte[] b)  { }
            @Override public void onEvent(int e, Bundle p)    { }

            @Override
            public void onEndOfSpeech() {
                tvStatus.setText("Analizando…");
                setMicState(false);
            }

            @Override
            public void onError(int error) {
                setMicState(false);
                String msg;
                switch (error) {
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        msg = "No se reconoció ninguna palabra. Intenta de nuevo."; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        msg = "Tiempo agotado. Presiona el micrófono para reintentar."; break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        msg = "Error de audio. Verifica tu micrófono."; break;
                    default:
                        msg = "Error al reconocer voz (código " + error + ")";
                }
                tvStatus.setText(msg);
            }

            @Override
            public void onResults(Bundle results) {
                setMicState(false);
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) processSpokenText(matches.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> partial =
                        partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (partial != null && !partial.isEmpty())
                    tvStatus.setText("Escuchando: \"" + partial.get(0) + "\"");
            }
        });
    }

    private void startListening() {
        if (currentWordIndex == 0) {
            String clean = TEXTO_NIVEL1.replaceAll("[^a-zA-Z\u00C0-\u00FC\\s]", "");
            targetWords = Arrays.asList(clean.trim().split("\\s+"));
        }

        setMicState(true);
        tvStatus.setText("Preparando micrófono…");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,                  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,                        "es-MX");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,             "es-MX");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,                 true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,                     3);
        speechRecognizer.startListening(intent);
    }

    private void stopListening() {
        speechRecognizer.stopListening();
        setMicState(false);
        tvStatus.setText("Presiona el micrófono para continuar");
    }

    // ─── Text Processing ────────────────────────────────────────────────────

    private void processSpokenText(String spokenText) {
        String cleanSpoken = spokenText.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\u00E0-\u00FC\\s]", "").trim();
        String[] spokenWords = cleanSpoken.split("\\s+");

        String cleanTarget = TEXTO_NIVEL1.replaceAll("[^a-zA-Z\u00C0-\u00FC\\s]", "");
        String[] rawTargetWords = cleanTarget.trim().split("\\s+");

        List<Integer> correctIndices = new ArrayList<>();
        List<Integer> wrongIndices   = new ArrayList<>();

        for (int i = 0; i < currentWordIndex; i++) correctIndices.add(i);

        int spokenIdx = 0;
        int targetIdx = currentWordIndex;

        while (spokenIdx < spokenWords.length && targetIdx < rawTargetWords.length) {
            String spoken = normalize(spokenWords[spokenIdx]);
            String target = normalize(rawTargetWords[targetIdx]);

            if (spoken.equals(target)) {
                correctIndices.add(targetIdx);
                targetIdx++;
                spokenIdx++;
            } else {
                if (targetIdx + 1 < rawTargetWords.length &&
                        spoken.equals(normalize(rawTargetWords[targetIdx + 1]))) {
                    wrongIndices.add(targetIdx);
                    targetIdx++;
                } else {
                    wrongIndices.add(targetIdx);
                    targetIdx++;
                    spokenIdx++;
                }
            }
        }

        currentWordIndex = targetIdx;
        renderHighlighted(TEXTO_NIVEL1, correctIndices, wrongIndices);

        if (currentWordIndex >= rawTargetWords.length) {
            tvStatus.setText("✅ ¡Texto completo! Presiona Reset para intentar de nuevo.");
        } else {
            int remaining = rawTargetWords.length - currentWordIndex;
            tvStatus.setText("Faltan " + remaining + " palabras. Presiona el micrófono para continuar.");
        }
    }

    private String normalize(String word) {
        return word.toLowerCase(Locale.ROOT)
                .replace("\u00E1","a").replace("\u00E9","e").replace("\u00ED","i")
                .replace("\u00F3","o").replace("\u00FA","u").replace("\u00FC","u")
                .replace("\u00E0","a").replace("\u00E8","e").replace("\u00EC","i")
                .replace("\u00F2","o").replace("\u00F9","u")
                .replaceAll("[^a-z\u00F1]", "");
    }

    // ─── Highlighted Text Rendering ─────────────────────────────────────────

    private void renderHighlighted(String fullText,
                                   List<Integer> correctIdx,
                                   List<Integer> wrongIdx) {
        SpannableStringBuilder sb = new SpannableStringBuilder(fullText);

        List<int[]> wordRanges = new ArrayList<>();
        boolean inWord    = false;
        int     wordStart = -1;

        for (int i = 0; i <= fullText.length(); i++) {
            boolean isLetter = i < fullText.length() && Character.isLetter(fullText.charAt(i));
            if (isLetter && !inWord)      { wordStart = i; inWord = true; }
            else if (!isLetter && inWord) { wordRanges.add(new int[]{wordStart, i}); inWord = false; }
        }

        int colorCorrect = 0xFF2ECC71;
        int colorWrong   = 0xFFE74C3C;
        int colorNeutral = 0xFF2C3E50;

        sb.setSpan(new ForegroundColorSpan(colorNeutral), 0, sb.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        for (int wi = 0; wi < wordRanges.size(); wi++) {
            int[] range = wordRanges.get(wi);
            int color;
            if      (correctIdx.contains(wi)) color = colorCorrect;
            else if (wrongIdx.contains(wi))   color = colorWrong;
            else                              color = colorNeutral;

            sb.setSpan(new ForegroundColorSpan(color), range[0], range[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (correctIdx.contains(wi))
                sb.setSpan(new BackgroundColorSpan(0x332ECC71), range[0], range[1],
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            else if (wrongIdx.contains(wi))
                sb.setSpan(new BackgroundColorSpan(0x33E74C3C), range[0], range[1],
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvHighlightedText.setText(sb);
    }

    // ─── UI State ───────────────────────────────────────────────────────────

    private void setMicState(boolean listening) {
        isListening = listening;
        btnMicrophone.setImageResource(listening ? R.drawable.ic_mic_active : R.drawable.ic_mic);
        micPulseRing.setVisibility(listening ? View.VISIBLE : View.INVISIBLE);
        btnMicrophone.setAlpha(listening ? 1f : 0.9f);
    }

    private void resetSession() {
        currentWordIndex = 0;
        targetWords      = null;
        renderHighlighted(TEXTO_NIVEL1, new ArrayList<>(), new ArrayList<>());
        tvStatus.setText("Presiona el micrófono para comenzar");
        setMicState(false);
    }

    // ─── Permissions ────────────────────────────────────────────────────────

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startListening();
            else
                Toast.makeText(this, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show();
        }
    }

    public void regresar(View vista) {
        Intent intent = new Intent(this, Lecturaguiada.class);
        startActivity(intent);
    }
}