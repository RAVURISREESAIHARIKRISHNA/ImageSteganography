package com.example.imagesteganography;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button encodeButton = (Button) findViewById(R.id.encode);
        Button decodeButton = (Button) findViewById(R.id.decode);

        encodeButton.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v){
//                        Toast.makeText(getApplicationContext() , "Encode" , Toast.LENGTH_LONG).show();
                        Intent encodeIntent = new Intent(MainActivity.this , EncodeActivity.class);
                        startActivity(encodeIntent);
                    }
                }
        );

        decodeButton.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v){
//                        Toast.makeText(getApplicationContext() , "Decode" , Toast.LENGTH_LONG).show();
                        Intent decodeIntent = new Intent(MainActivity.this , DecodeActivity.class);
                        startActivity(decodeIntent);
                    }
                }
        );
    }
}
