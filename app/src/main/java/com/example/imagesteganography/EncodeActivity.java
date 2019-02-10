package com.example.imagesteganography;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class EncodeActivity extends AppCompatActivity {

    private static final int  SELECT_IMAGE_CODE = 1;
    private Uri globalUri;
    protected Bitmap coverImage;
    protected String secretMessage;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable("globalUri" , this.globalUri);
        savedInstanceState.putString("secretMessage" , this.secretMessage);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        this.globalUri = savedInstanceState.getParcelable("globalUri");
        this.secretMessage = savedInstanceState.getString("secretMessage");
        this.drawImageFromGlobalUri();
        EditText message = (EditText) findViewById(R.id.editText);
        message.setText(this.secretMessage);
    }
    private void setDefaultValues(){
        this.coverImage = null;
        this.secretMessage = null;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encode);

        this.setDefaultValues();
        Button selectImage = (Button) findViewById(R.id.selectImage);
        Button encodeMessage = (Button) findViewById(R.id.encodeMessage);


        selectImage.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v){
                        selectImageFromGallery();
                    }
                }
        );

        final EditText secretMessageFeild = (EditText) findViewById(R.id.editText);

        secretMessageFeild.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        secretMessage = secretMessageFeild.getText().toString();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }
        );

        encodeMessage.setOnClickListener(
                new Button.OnClickListener(){

                    @Override
                    public void onClick(View v){
                        if(globalUri == null){
                            Toast.makeText(getApplicationContext(), "Please select an Image", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(((EditText) findViewById(R.id.editText)).getText().toString().equals("")){
                            Toast.makeText(getApplicationContext() , "Please enter the Secret Message" , Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(secretMessage != null && globalUri != null){
//                            Toast.makeText(getApplicationContext() , "READY" , Toast.LENGTH_LONG).show();
                            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                            bmpFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bmp = null;
                            try{
                                bmp = MediaStore.Images.Media.getBitmap( getContentResolver() , globalUri);
                            }catch(Exception e){
                                Log.i("URI" , "Exception Handled");
                            }
                            coverImage = bmp;

                            secretMessage = ((EditText) findViewById(R.id.editText)).getText().toString();

                            startEncoding();
                        }
                    }
                }
        );
    }

    private void startEncoding(){
        LSBImageStego obj = new LSBImageStego(this.coverImage , this.secretMessage);

        if(!obj.checkEncodability()){
            Toast.makeText(getApplicationContext() , "Message cannot be Encoded.Try a different Message with different Length" , Toast.LENGTH_LONG).show();
            return;
        }
        obj.encode();
        this.writeToFile(obj.getCoverImage());


    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent data){
        if(resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            this.globalUri = imageUri;
            this.drawImageFromGlobalUri();
        }
    }

    private void selectImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/png");
        if(intent.resolveActivity(getPackageManager())!= null){
            startActivityForResult(intent , SELECT_IMAGE_CODE);
        }
    }

    private void drawImageFromGlobalUri(){
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(this.globalUri);
    }

    private void writeToFile(Bitmap bm){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".png";
        File file = new File(myDir, fname);
        Log.i("URI", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getApplicationContext() , "Image saved @"+fname , Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
