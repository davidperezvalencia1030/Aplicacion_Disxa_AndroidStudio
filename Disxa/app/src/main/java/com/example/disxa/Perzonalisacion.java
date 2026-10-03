package com.example.disxa;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class Perzonalisacion extends AppCompatActivity {

    private CardView cardGif1, cardGif2, cardGif3, cardGif4, cardGif5;
    private ImageView previewGif1, previewGif2, previewGif3, previewGif4, previewGif5;

    public static final String PREFS_NAME = "GifPrefs";
    public static final String KEY_GIF_SELECCIONADO = "gif_seleccionado";

    private final int[] gifDrawables = {
            R.drawable.perrito,
            R.drawable.gatocompleto,
            R.drawable.bano,
            R.drawable.obeja,
            R.drawable.dino
    };

    private final String[] gifNombres = {
            "Perrito", "Gato", "Baño", "Oveja", "Dino"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perzonalisacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardGif1 = findViewById(R.id.cardGif1);
        cardGif2 = findViewById(R.id.cardGif2);
        cardGif3 = findViewById(R.id.cardGif3);
        cardGif4 = findViewById(R.id.cardGif4);
        cardGif5 = findViewById(R.id.cardGif5);

        previewGif1 = findViewById(R.id.previewGif1);
        previewGif2 = findViewById(R.id.previewGif2);
        previewGif3 = findViewById(R.id.previewGif3);
        previewGif4 = findViewById(R.id.previewGif4);
        previewGif5 = findViewById(R.id.previewGif5);

        previewGif1.setImageResource(gifDrawables[0]);
        previewGif2.setImageResource(gifDrawables[1]);
        previewGif3.setImageResource(gifDrawables[2]);
        previewGif4.setImageResource(gifDrawables[3]);
        previewGif5.setImageResource(gifDrawables[4]);

        resaltarSeleccionActual();

        cardGif1.setOnClickListener(v -> seleccionarGif(0));
        cardGif2.setOnClickListener(v -> seleccionarGif(1));
        cardGif3.setOnClickListener(v -> seleccionarGif(2));
        cardGif4.setOnClickListener(v -> seleccionarGif(3));
        cardGif5.setOnClickListener(v -> seleccionarGif(4));
    }

    private void seleccionarGif(int index) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_GIF_SELECCIONADO, index).apply();
        resaltarSeleccionActual();
        Toast.makeText(this, gifNombres[index] + " seleccionado ✓", Toast.LENGTH_SHORT).show();
    }

    private void resaltarSeleccionActual() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int seleccionado = prefs.getInt(KEY_GIF_SELECCIONADO, 0);

        CardView[] cards = {cardGif1, cardGif2, cardGif3, cardGif4, cardGif5};

        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) continue;
            if (i == seleccionado) {
                cards[i].setCardElevation(12f);
                cards[i].setCardBackgroundColor(0xFFEDE9FF);
            } else {
                cards[i].setCardElevation(4f);
                cards[i].setCardBackgroundColor(0xFFFFFFFF);
            }
        }
    }
}