package com.example.disxa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Asocialaletra extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView    tvLetraGrande, tvNombreObjeto, tvEstadoMic, tvResultado,
            tvFeedback, tvCorrectas, tvIncorrectas;
    private ImageView   ivObjeto;
    private ImageButton btnMic;
    private GridLayout  gridLetras;

    private TextToSpeech     tts;
    private SpeechRecognizer speechRecognizer;
    private boolean ttsReady = false, isListening = false;

    private char letraActual = 'A';
    private int  correctas   = 0, incorrectas = 0;
    private static final int REQ_MIC = 101;

    private final Map<Character, String> nombreObjeto = new HashMap<Character, String>() {{
        put('A',"Avion");    put('B',"Barco");    put('C',"Casa");
        put('D',"Delfin");   put('E',"Estrella"); put('F',"Flor");
        put('G',"Gato");     put('H',"Helicoptero"); put('I',"Iglesia");
        put('J',"Jirafa");   put('K',"Kiwi");     put('L',"Leon");
        put('M',"Manzana");  put('N',"Nube");      put('O',"Oso");
        put('P',"Pato");     put('Q',"Queso");     put('R',"Raton");
        put('S',"Sol");      put('T',"Tren");      put('U',"Uva");
        put('V',"Vaca");     put('W',"Wifi");      put('X',"Xilofono");
        put('Y',"Yogur");    put('Z',"Zapato");
    }};

    private final Map<Character, String> nombreLetra = new HashMap<Character, String>() {{
        put('A',"a");    put('B',"be");   put('C',"ce");  put('D',"de");
        put('E',"e");    put('F',"efe");  put('G',"ge");  put('H',"hache");
        put('I',"i");    put('J',"jota"); put('K',"ka");  put('L',"ele");
        put('M',"eme");  put('N',"ene");  put('O',"o");   put('P',"pe");
        put('Q',"cu");   put('R',"erre"); put('S',"ese"); put('T',"te");
        put('U',"u");    put('V',"uve");  put('W',"doble uve");
        put('X',"equis"); put('Y',"ye"); put('Z',"zeta");
    }};

    private final Map<Character, TextView> letterTiles = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_asocialaletra);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        bindViews();
        tts = new TextToSpeech(this, this);
        buildGrid();
        actualizarPantalla();
        checkMicPermission();

        btnMic.setOnClickListener(v -> toggleListening());
        ivObjeto.setOnClickListener(v -> hablarLetraYObjeto());
        tvLetraGrande.setOnClickListener(v -> hablarLetraYObjeto());
    }

    private void bindViews() {
        tvLetraGrande  = findViewById(R.id.tvLetraGrande);
        tvNombreObjeto = findViewById(R.id.tvNombreObjeto);
        ivObjeto       = findViewById(R.id.ivObjeto);
        tvEstadoMic    = findViewById(R.id.tvEstadoMic);
        tvResultado    = findViewById(R.id.tvResultado);
        tvFeedback     = findViewById(R.id.tvFeedback);
        btnMic         = findViewById(R.id.btnMic);
        tvCorrectas    = findViewById(R.id.tvCorrectas);
        tvIncorrectas  = findViewById(R.id.tvIncorrectas);
        gridLetras     = findViewById(R.id.gridLetras);
    }

    private void buildGrid() {
        gridLetras.removeAllViews();
        letterTiles.clear();
        gridLetras.setColumnCount(6);

        for (char l : "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
            TextView tile = new TextView(this);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            p.rowSpec    = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            p.setMargins(4, 4, 4, 4);
            p.width  = 0;
            p.height = dpToPx(48);
            tile.setLayoutParams(p);
            tile.setText(String.valueOf(l));
            tile.setTextSize(17f);
            tile.setTypeface(null, android.graphics.Typeface.BOLD);
            tile.setGravity(android.view.Gravity.CENTER);
            tile.setClickable(true);
            tile.setFocusable(true);
            tile.setBackgroundResource(R.drawable.bg_tile_normal);
            tile.setTextColor(0xFF2D2D2D);
            final char letra = l;
            tile.setOnClickListener(v -> seleccionarLetra(letra));
            gridLetras.addView(tile);
            letterTiles.put(l, tile);
        }
    }

    private void seleccionarLetra(char l) {
        letraActual = l;
        actualizarPantalla();
        resetFeedback();
        for (Map.Entry<Character, TextView> e : letterTiles.entrySet()) {
            TextView t = e.getValue();
            if (e.getKey() == l) {
                t.setBackgroundResource(R.drawable.bg_tile_active);
                t.setTextColor(0xFFFFFFFF);
            } else if (t.getTag() == null) {
                t.setBackgroundResource(R.drawable.bg_tile_normal);
                t.setTextColor(0xFF2D2D2D);
            }
        }
        hablarLetraYObjeto();
    }

    private void actualizarPantalla() {
        tvLetraGrande.setText(String.valueOf(letraActual));
        tvLetraGrande.setTextColor(0xFFFF6B35);

        String objeto = nombreObjeto.getOrDefault(letraActual, "");
        tvNombreObjeto.setText(objeto);

        String resName = "img_" + Character.toLowerCase(letraActual);
        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
        ivObjeto.setImageResource(resId != 0 ? resId : R.drawable.bg_image_placeholder);

        TextView tile = letterTiles.get(letraActual);
        if (tile != null) {
            tile.setBackgroundResource(R.drawable.bg_tile_active);
            tile.setTextColor(0xFFFFFFFF);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int r = tts.setLanguage(new Locale("es", "MX"));
            ttsReady = r != TextToSpeech.LANG_MISSING_DATA
                    && r != TextToSpeech.LANG_NOT_SUPPORTED;
        }
    }

    private void hablarLetraYObjeto() {
        if (!ttsReady) return;
        tts.stop();

        tvLetraGrande.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .setDuration(120)
                .withEndAction(() ->
                        tvLetraGrande.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(120)
                                .start()
                ).start();

        String objeto = nombreObjeto.getOrDefault(letraActual, "");
        tts.speak(letraActual + " de " + objeto, TextToSpeech.QUEUE_FLUSH, null, "tts1");
    }

    private void checkMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
        } else {
            setupSpeechRecognizer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == REQ_MIC && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            setupSpeechRecognizer();
        } else {
            tvEstadoMic.setText("Permiso de microfono denegado");
        }
    }

    private void setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            tvEstadoMic.setText("Reconocimiento de voz no disponible");
            btnMic.setEnabled(false);
            return;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                runOnUiThread(() -> {
                    tvEstadoMic.setText("Escuchando...");
                    tvResultado.setText("Di el nombre de la letra " + letraActual);
                    tvResultado.setTextColor(0xFF999999);
                    btnMic.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                    btnMic.setImageResource(R.drawable.ic_mic_active);
                });
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float v) {}
            @Override public void onBufferReceived(byte[] b) {}
            @Override public void onEndOfSpeech() { isListening = false; }
            @Override
            public void onError(int error) {
                isListening = false;
                runOnUiThread(() -> {
                    resetMicUI();
                    tvResultado.setText(error == SpeechRecognizer.ERROR_NO_MATCH
                            ? "No te entendi. Intenta de nuevo"
                            : "Error. Intenta de nuevo");
                });
            }
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                runOnUiThread(() -> resetMicUI());
                ArrayList<String> m =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (m != null && !m.isEmpty())
                    runOnUiThread(() -> verificarRespuesta(m.get(0).trim().toLowerCase()));
            }
            @Override public void onPartialResults(Bundle r) {}
            @Override public void onEvent(int t, Bundle p) {}
        });
    }

    private void toggleListening() {
        if (speechRecognizer == null) { setupSpeechRecognizer(); return; }
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            resetMicUI();
        } else {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            speechRecognizer.startListening(i);
        }
    }

    private void resetMicUI() {
        tvEstadoMic.setText("Presiona para hablar");
        btnMic.setImageResource(R.drawable.ic_mic);
        btnMic.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFF0F0F0));
    }

    private void verificarRespuesta(String spoken) {
        String letraStr = String.valueOf(Character.toLowerCase(letraActual));
        String phonetic = nombreLetra.getOrDefault(letraActual, letraStr).toLowerCase();
        String objeto   = nombreObjeto.getOrDefault(letraActual, "").toLowerCase();

        boolean correcto = spoken.equals(letraStr)
                || spoken.equals(phonetic)
                || spoken.contains(phonetic)
                || spoken.equals(objeto)
                || spoken.contains(objeto)
                || spoken.equals("la letra " + letraStr)
                || spoken.startsWith(letraStr + " ");

        TextView tile = letterTiles.get(letraActual);

        if (correcto) {
            correctas++;
            tvCorrectas.setText(String.valueOf(correctas));
            tvLetraGrande.setTextColor(0xFF27AE60);
            tvResultado.setText("Correcto! \"" + spoken + "\"");
            tvResultado.setTextColor(0xFF27AE60);
            if (tile != null) {
                tile.setBackgroundResource(R.drawable.bg_tile_correct);
                tile.setTextColor(0xFF1B7A42);
                tile.setTag("correct");
            }
            mostrarFeedback(true, "Excelente! " + letraActual
                    + " de " + nombreObjeto.getOrDefault(letraActual, ""));
            tts.speak("Muy bien!", TextToSpeech.QUEUE_ADD, null, "praise");

        } else {
            incorrectas++;
            tvIncorrectas.setText(String.valueOf(incorrectas));
            tvLetraGrande.setTextColor(0xFFE74C3C);
            tvResultado.setText("Dijiste \"" + spoken + "\"");
            tvResultado.setTextColor(0xFFE74C3C);
            if (tile != null && !"correct".equals(tile.getTag())) {
                tile.setBackgroundResource(R.drawable.bg_tile_wrong);
                tile.setTextColor(0xFFC0392B);
                tile.setTag("wrong");
            }
            String hint = "Es la letra " + phonetic + ", de " + objeto;
            mostrarFeedback(false, hint);
            tts.speak(hint, TextToSpeech.QUEUE_ADD, null, "hint");
        }
    }

    private void mostrarFeedback(boolean correcto, String msg) {
        tvFeedback.setText((correcto ? "Excelente! " : "Casi! ") + msg);
        tvFeedback.setBackgroundResource(correcto
                ? R.drawable.bg_feedback_correct
                : R.drawable.bg_feedback_wrong);
        tvFeedback.setTextColor(correcto ? 0xFF1B7A42 : 0xFFC0392B);
        tvFeedback.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> tvFeedback.setVisibility(View.GONE), 3500);
    }

    private void resetFeedback() {
        tvFeedback.setVisibility(View.GONE);
        tvResultado.setText("Di el nombre de la letra en voz alta");
        tvResultado.setTextColor(0xFF999999);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (speechRecognizer != null) speechRecognizer.destroy();
    }
    public void rega(View vista) {
        Intent intent = new Intent(this, disxa_menu.class);
        startActivity(intent);
    }
}