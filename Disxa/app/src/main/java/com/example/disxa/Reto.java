package com.example.disxa;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import java.util.Locale;
import java.util.Random;

public class Reto extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private TextView tvTexto;
    private TextView tvStatus;
    private ImageButton btnMicrofono;
    private Button btnReset;
    private ImageView gifPersonaje;

    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private long tiempoInicio;
    private int aciertos = 0;
    private int errores = 0;
    private String textoActual;

    // ── Mismo arreglo que en Perzonalisacion.java — mismo orden ──
    private final int[] gifDrawables = {
            R.drawable.perrito,
            R.drawable.gatocompleto,
            R.drawable.bano,
            R.drawable.obeja,
            R.drawable.dino
    };

    private final String[] textos = {
            "El perro corre por el parque todos los dias.",
            "La niña juega con una pelota roja en el jardin.",
            "Los pajaros vuelan sobre los arboles al amanecer.",
            "Mi amigo estudia matematicas despues de la escuela.",
            "El gato duerme tranquilamente junto a la ventana.",
            "Las flores crecen con agua y luz del sol.",
            "La tortuga camina despacio por el camino.",
            "El autobus llega temprano cada mañana.",
            "Los niños leen cuentos en la biblioteca.",
            "La lluvia cae suavemente durante la tarde."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reto);

        tvTexto      = findViewById(R.id.tvTexto);
        tvStatus     = findViewById(R.id.tvStatus);
        btnMicrofono = findViewById(R.id.btnMicrofono);
        btnReset     = findViewById(R.id.btnReset);
        gifPersonaje = findViewById(R.id.gifPersonaje);

        Random random = new Random();
        textoActual = textos[random.nextInt(textos.length)];
        tvTexto.setText(textoActual);

        // ✅ Carga el GIF elegido en Perzonalisacion
        cargarGifSeleccionado();

        setupSpeechRecognizer();

        btnMicrofono.setOnClickListener(v -> {
            if (checkPermission()) {
                if (isListening) stopListening();
                else             startListening();
            } else {
                requestPermission();
            }
        });

        btnReset.setOnClickListener(v -> reiniciarJuego());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se actualiza si el usuario regresó desde Perzonalisacion
        cargarGifSeleccionado();
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

            @Override public void onReadyForSpeech(Bundle params) { tvStatus.setText("Escuchando..."); }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEvent(int eventType, Bundle params) { }
            @Override public void onPartialResults(Bundle partialResults) { }

            @Override
            public void onEndOfSpeech() {
                isListening = false;
            }

            @Override
            public void onError(int error) {
                isListening = false;
                tvStatus.setText("Intenta nuevamente");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    evaluarLectura(matches.get(0));
                }
                isListening = false;
            }
        });
    }

    private void startListening() {
        tiempoInicio = System.currentTimeMillis();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");
        speechRecognizer.startListening(intent);
        isListening = true;
        tvStatus.setText("Lee el texto en voz alta");
    }

    private void stopListening() {
        speechRecognizer.stopListening();
        isListening = false;
    }

    // ─── Evaluación ─────────────────────────────────────────────────────────

    private void evaluarLectura(String textoUsuario) {
        String textoCorrecto = textoActual.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-záéíóúñü\\s]", "");
        textoUsuario = textoUsuario.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-záéíóúñü\\s]", "");

        String[] palabrasCorrectas = textoCorrecto.split("\\s+");
        String[] palabrasUsuario   = textoUsuario.split("\\s+");

        aciertos = 0;
        errores  = 0;

        int minimo = Math.min(palabrasCorrectas.length, palabrasUsuario.length);
        for (int i = 0; i < minimo; i++) {
            if (palabrasCorrectas[i].equals(palabrasUsuario[i])) aciertos++;
            else                                                   errores++;
        }
        if (palabrasCorrectas.length > palabrasUsuario.length)
            errores += palabrasCorrectas.length - palabrasUsuario.length;

        mostrarResultado();
    }

    private void mostrarResultado() {
        long segundos = (System.currentTimeMillis() - tiempoInicio) / 1000;
        int    total  = aciertos + errores;
        double precision = total > 0 ? (aciertos * 100.0) / total : 0;

        String estrellas = precision >= 90 ? "⭐⭐⭐" : precision >= 70 ? "⭐⭐" : "⭐";

        new AlertDialog.Builder(this)
                .setTitle("Resultado del reto")
                .setMessage(estrellas
                        + "\n\nTiempo: "    + segundos  + " segundos"
                        + "\n\nAciertos: "  + aciertos
                        + "\nErrores: "     + errores
                        + "\nPrecisión: "   + String.format(Locale.getDefault(), "%.2f", precision) + "%")
                .setPositiveButton("Reintentar", (d, w) -> reiniciarJuego())
                .setNegativeButton("Cerrar",     (d, w) -> finish())
                .show();
    }

    private void reiniciarJuego() {
        aciertos = 0;
        errores  = 0;
        textoActual = textos[new Random().nextInt(textos.length)];
        tvTexto.setText(textoActual);
        tvStatus.setText("Presiona el micrófono para comenzar");
    }

    // ─── Permisos ────────────────────────────────────────────────────────────

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
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
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }

    public void ret(View vista) {
        Intent intent = new Intent(this, disxa_menu.class);
        startActivity(intent);
    }
}