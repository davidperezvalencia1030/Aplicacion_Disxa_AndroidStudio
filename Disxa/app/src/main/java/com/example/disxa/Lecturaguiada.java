package com.example.disxa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Lecturaguiada extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lecturaguiada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void nivel1(View vista){
        Intent intent = new Intent(this,lecturanivel1.class);
        startActivity(intent);
    }
    public void regresa(View vista){
        Intent intent = new Intent(this,disxa_menu.class);
        startActivity(intent);
    }
    public void nivel2(View vista){
        Intent intent = new Intent(this,lecturanivel2.class);
        startActivity(intent);
    }
    public void nivel3(View vista){
        Intent intent = new Intent(this,lecturanivel3.class);
        startActivity(intent);
    }
}