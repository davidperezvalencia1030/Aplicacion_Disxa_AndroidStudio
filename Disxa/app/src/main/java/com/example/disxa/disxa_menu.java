package com.example.disxa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class disxa_menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_disxa_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void lectura(View vista){
        Intent intent = new Intent(this,Lecturaguiada.class);
        startActivity(intent);
    }
    public void forma(View vista) {
        Intent intent = new Intent(this, Formalapalabra.class);
        startActivity(intent);
    }
    public void reto(View vista) {
        Intent intent = new Intent(this, Reto.class);
        startActivity(intent);
    }
    public void aso(View vista) {
        Intent intent = new Intent(this, Asocialaletra.class);
        startActivity(intent);
    }
    public void per(View vista) {
        Intent intent = new Intent(this, Perzonalisacion.class);
        startActivity(intent);
    }
}