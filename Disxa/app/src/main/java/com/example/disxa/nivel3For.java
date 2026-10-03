package com.example.disxa;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class nivel3For extends AppCompatActivity {

    private EditText txtRespuesta;
    private Button btnComprobar;
    private TextView txtNivel;
    private ImageView imgPerrito;

    // ── Mismo arreglo que en Perzonalisacion.java — mismo orden ──
    private final int[] gifDrawables = {
            R.drawable.perrito,
            R.drawable.gatocompleto,
            R.drawable.bano,
            R.drawable.obeja,
            R.drawable.dino
    };

    private final String[] palabras = {
            "AGUDO",
            "ADAPTACION",
            "ESPINA",
            "INFECCION",
            "PANTORRILLA"
    };

    private int indice = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nivel3_for);

        txtRespuesta = findViewById(R.id.txtRespuesta);
        btnComprobar = findViewById(R.id.btnComprobar);
        txtNivel     = findViewById(R.id.txtNivel);
        imgPerrito   = findViewById(R.id.imgperrito);


        cargarGifSeleccionado();

        mostrarPalabra();

        btnComprobar.setOnClickListener(v -> {

            String respuesta = txtRespuesta.getText().toString().trim().toUpperCase();

            if (respuesta.equals(palabras[indice])) {

                Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();

                indice++;

                if (indice < palabras.length) {
                    txtRespuesta.setText("");
                    mostrarPalabra();
                } else {
                    txtNivel.setText("Nivel completado");
                    Toast.makeText(this, "¡Felicidades! Completaste las 5 palabras", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(nivel3For.this, Formalapalabra.class);
                    startActivity(intent);
                    finish();
                }

            } else {
                Toast.makeText(this, "Palabra incorrecta, intenta nuevamente", Toast.LENGTH_SHORT).show();
            }
        });
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
        int indiceGif   = prefs.getInt(Perzonalisacion.KEY_GIF_SELECCIONADO, 0);
        int drawableRes = gifDrawables[indiceGif];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            AnimatedImageDrawable gif =
                    (AnimatedImageDrawable) getDrawable(drawableRes);
            if (gif != null) {
                gif.start();
                imgPerrito.setImageDrawable(gif);
            }
        } else {
            imgPerrito.setImageResource(drawableRes);
        }
    }

    // ─── Lógica ─────────────────────────────────────────────────────────────

    private void mostrarPalabra() {
        txtNivel.setText("Palabra " + (indice + 1) + " de 5");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Memoriza la palabra")
                .setMessage(palabras[indice])
                .setCancelable(false)
                .create();

        dialog.show();

        new Handler().postDelayed(dialog::dismiss, 10000);
    }

    public void regre(View vista) {
        Intent intent = new Intent(this, Formalapalabra.class);
        startActivity(intent);
    }
}