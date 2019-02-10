package com.example.imagesteganography;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.Collections;

public class LSBImageStego {

    private Bitmap coverImage;
    private String secretMessage;
    private String Binary_OF_SecretMessage;
    private int originalSecretMessageLength;
    private int coverImage_rows;
    private int coverImage_columns;

    public LSBImageStego(Bitmap coverImage , String secretMessage){
        this.coverImage = coverImage.copy(Bitmap.Config.ARGB_8888 , true);
        this.coverImage_rows = this.coverImage.getHeight();
        this.coverImage_columns =  this.coverImage.getWidth();
        this.secretMessage = secretMessage;
        this.originalSecretMessageLength = this.secretMessage.length();
        this.makeBinaryStringFromMessage();
//        Log.i("URI" ,new Integer(this.coverImage_rows).toString());
//        Log.i("URI" , new Integer(this.coverImage_columns).toString());
//        Log.i("URI" , this.secretMessage);
//        Log.i("URI", new Integer(this.originalSecretMessageLength).toString());
    }

    public Bitmap getCoverImage(){
        return this.coverImage;
    }

    public String getSecretMessage(){
        return this.secretMessage;
    }

    public LSBImageStego(Bitmap coverImage){
        this.coverImage = coverImage.copy(Bitmap.Config.ARGB_8888 , true);
        this.coverImage_rows = this.coverImage.getHeight();
        this.coverImage_columns =  this.coverImage.getWidth();
    }


    public void decode(){
        this.getOriginalSecretMessageLength_FROM_Image();
        this.decodeMessageBinaryFromImage();
    }

    private void decodeMessageBinaryFromImage(){
        int retrieved = 0;
        StringBuilder sb = new StringBuilder();
        for(int row = 0 ; row <= (this.coverImage_rows - 1) -1 ; row++){
            for(int col = 0 ; col<= (this.coverImage_columns -1) ; col++){
                if(retrieved == this.originalSecretMessageLength*8){
                    this.secretMessage = this.decodeMsgFromBinary(sb.toString());
                    Log.i("URI","FOund MSG:"+this.secretMessage);
                    return;
                }
                //                get current RED Pixel Intensity
                int red = Color.red(this.coverImage.getPixel(col , row));

                //              GETTING ORIGINAL Binary of RED Channel
                String original = return8BinaryOfAscii(red);
                Log.i("URI" , "Original("+col+","+row+") =>"+original);

                String newLSBBits = original.substring(original.length()-2 , original.length());

                StringBuilder flip = new StringBuilder();
                if(newLSBBits.charAt(0)=='0'){
                    flip.append("1");
                }else{
                    flip.append("0");
                }

                if(newLSBBits.charAt(1)=='0'){
                    flip.append("1");
                }else{
                    flip.append("0");
                }

                newLSBBits = flip.toString();

                sb.append(newLSBBits);
                retrieved += 2;
            }
        }
    }

    private String decodeMsgFromBinary(String binary){
        StringBuilder sb = new StringBuilder();
        for(int i=0 ; i<= (binary.length() -8) ; i+=8 ){
            String charBinary = binary.substring(i , i+8);
            sb.append(new Character( (char) Integer.parseInt(charBinary , 2) ).toString());
        }

        return sb.toString();
    }

    public void encode(){
        this.encodeMessage();
        this.encodeMessageLength();
    }

    private void getOriginalSecretMessageLength_FROM_Image(){
        StringBuilder sb = new StringBuilder();
        for(int col = 0; col<= this.coverImage_columns-1 ; col++){
            //                get current Pixel Intensities
            int alpha = Color.alpha(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int red = Color.red(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int green = Color.green(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int blue = Color.blue(this.coverImage.getPixel(col , this.coverImage_rows -1));

            //              GETTING ORIGINAL Binary of RED Channel
            String original = return8BinaryOfAscii(red);

            sb.append(original.substring(original.length()-2 , original.length()));
        }
        Log.i("URI" , "LENGHT FOUND:" + Integer.parseInt(sb.toString() , 2));
        this.originalSecretMessageLength = Integer.parseInt(sb.toString() , 2);
    }

    private void encodeMessageLength(){
        String originalBinary_OF_MsgLength = Integer.toBinaryString(this.originalSecretMessageLength);
        String encodableBinary = this.getEncodableBinaryOfMsgLengthBinary(originalBinary_OF_MsgLength);
        int count = 0;
        int remaining = encodableBinary.length();
        for(int col = 0 ; col <= this.coverImage_columns -1 ; col++){
            if(count > remaining - 2){
                return;
            }

            //                get current Pixel Intensities
            int alpha = Color.alpha(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int red = Color.red(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int green = Color.green(this.coverImage.getPixel(col , this.coverImage_rows -1));
            int blue = Color.blue(this.coverImage.getPixel(col , this.coverImage_rows -1));

            //              GETTING ORIGINAL Binary of RED Channel
            String original = return8BinaryOfAscii(red);
            Log.i("URI" , "Original("+col+","+(this.coverImage_rows -1)+") =>"+original);

            //                Get Modified String by Changing LSB 2 Bits
            String newLSBBits = encodableBinary.substring(count , count +2);
            count += 2;

            Log.i("URI" , "New LSBs("+col+","+(this.coverImage_rows -1)+") =>"+newLSBBits);


            String modified = original.substring(0 , original.length() - 2) + newLSBBits;

            Log.i("URI" , "Modified("+col+","+(this.coverImage_rows -1)+") =>"+modified);

            int modifiedRed = Integer.valueOf(modified , 2);


//                Writing Pixel Back
            this.coverImage.setHasAlpha(false);
            this.coverImage.setPixel(col , this.coverImage_rows -1 , Color.argb(alpha , modifiedRed , green , blue));

        }
    }

    private String getEncodableBinaryOfMsgLengthBinary(String originalBinary){
        return String.join("" , Collections.nCopies(
                (this.coverImage_columns * 2 - (originalBinary.length())), "0")) + originalBinary;
    }

    private void encodeMessage(){
        int count = 0;
        int remaining = this.Binary_OF_SecretMessage.length();
        for(int row = 0 ; row <= (this.coverImage_rows - 1) -1 ; row++){
            for(int col = 0 ; col<= (this.coverImage_columns -1) ; col++){

                if(count > remaining - 2){
                    return;
                }

                //                get current Pixel Intensities
                int alpha = Color.alpha(this.coverImage.getPixel(col , row));
                int red = Color.red(this.coverImage.getPixel(col , row));
                int green = Color.green(this.coverImage.getPixel(col , row));
                int blue = Color.blue(this.coverImage.getPixel(col , row));

//              GETTING ORIGINAL Binary of RED Channel
                String original = return8BinaryOfAscii(red);
                Log.i("URI" , "Original("+col+","+row+") =>"+original);

//                Get Modified String by Changing LSB 2 Bits
                String newLSBBits = this.Binary_OF_SecretMessage.substring(count , count +2);
                count += 2;

                StringBuilder flip = new StringBuilder();
                if(newLSBBits.charAt(0)=='0'){
                    flip.append("1");
                }else{
                    flip.append("0");
                }

                if(newLSBBits.charAt(1)=='0'){
                    flip.append("1");
                }else{
                    flip.append("0");
                }

                newLSBBits = flip.toString();

                Log.i("URI" , "New LSBs("+col+","+row+") =>"+newLSBBits);


                String modified = original.substring(0 , original.length() - 2) + newLSBBits;

                Log.i("URI" , "Modified("+col+","+row+") =>"+modified);

                int modifiedRed = Integer.valueOf(modified , 2);

//                Writing Pixel Back
                this.coverImage.setHasAlpha(false);
                this.coverImage.setPixel(col , row , Color.argb(alpha , modifiedRed , green , blue));
            }
        }
    }

    public boolean checkEncodability(){
        return this.Binary_OF_SecretMessage.length() <= ((this.coverImage_rows - 1)*this.coverImage_columns*2 ) ;
    }

    private void makeBinaryStringFromMessage(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<= this.originalSecretMessageLength-1 ; i++){
            int ascii = (int) this.secretMessage.charAt(i);
            sb.append(this.return8BinaryOfAscii(ascii));
        }
        this.Binary_OF_SecretMessage = sb.toString();
        Log.i("URI" ,"BINARY MESSAGE:"+ this.Binary_OF_SecretMessage);
    }

    private String return8BinaryOfAscii(int ascii){
        String binary = Integer.toBinaryString(ascii);
        return String.join("" , Collections.nCopies(8-(binary.length()) , "0")) + binary;
    }
}
