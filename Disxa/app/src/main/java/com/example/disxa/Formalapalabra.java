package com.example.disxa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Formalapalabra extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formalapalabra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void reg(View vista){
        Intent intent = new Intent(this,disxa_menu.class);
        startActivity(intent);
    }
    public void nivel1f(View vista){
        Intent intent = new Intent(this,nivel1For.class);
        startActivity(intent);
    }
    public void nivel2f(View vista){
        Intent intent = new Intent(this,nivel2For.class);
        startActivity(intent);
    }
    public void nivel3f(View vista){
        Intent intent = new Intent(this,nivel3For.class);
        startActivity(intent);
    }
}