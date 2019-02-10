package com.example.imagesteganography;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class DecodeActivity extends AppCompatActivity {

    private static final int  SELECT_IMAGE_CODE = 1;
    private Uri globalUri;
    protected Bitmap coverImage;
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable("globalUri" , this.globalUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        this.globalUri = savedInstanceState.getParcelable("globalUri");
        this.drawImageFromGlobalUri();

    }
    private void setDefaultValues(){
        this.coverImage = null;
    }
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data){
        if(resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            this.globalUri = imageUri;
            this.drawImageFromGlobalUri();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        this.setDefaultValues();
        Button selectImage = (Button) findViewById(R.id.button2);
        Button decodeMessage = (Button) findViewById(R.id.button3);


        selectImage.setOnClickListener(
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        selectImageFromGallery();
                    }
                }
        );

        decodeMessage.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v){
                        if(globalUri == null){
                            Toast.makeText(getApplicationContext() , "Please Select an Image" , Toast.LENGTH_LONG).show();
                            return;
                        }
                        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                        bmpFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                        Bitmap bmp = null;
                        try{
                            bmp = MediaStore.Images.Media.getBitmap( getContentResolver() , globalUri);
                        }catch(Exception e){
                            Log.i("URI" , "Exception Handled");
                        }
                        coverImage = bmp;
                        decodeImage();
                    }
                }
        );

    }
    private void selectImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/png");
        if(intent.resolveActivity(getPackageManager())!= null){
            startActivityForResult(intent , SELECT_IMAGE_CODE);
        }
    }
    private void decodeImage(){
        LSBImageStego obj = new LSBImageStego(this.coverImage);
        obj.decode();
        Toast.makeText(getApplicationContext() , obj.getSecretMessage() ,Toast.LENGTH_LONG).show();
    }
    private void drawImageFromGlobalUri(){
        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageURI(this.globalUri);
    }
}
