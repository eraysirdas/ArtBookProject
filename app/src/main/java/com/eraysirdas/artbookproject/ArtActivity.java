package com.eraysirdas.artbookproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.eraysirdas.artbookproject.databinding.ActivityArtBinding;
import com.eraysirdas.artbookproject.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissonLauncher; //izinler string oalrak istenir
    Bitmap bitmap;
    SQLiteDatabase database;
    String info,sqlStrings,currentName,currentArtistName,currentYear;
    int artId;
    byte[] currentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();
        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        Intent intent = getIntent();
        info = intent.getStringExtra("info");
        artId = intent.getIntExtra("artId",0);

        if(info.equals("new")){

            binding.editTextName.setText("");
            binding.editTextPname.setText("");
            binding.editTextYear.setText("");
            binding.savebtn.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.image);

        }else if (info.equals("old")){
            binding.savebtn.setVisibility(View.INVISIBLE);
            binding.editTextName.setEnabled(false);
            binding.editTextPname.setEnabled(false);
            binding.editTextYear.setEnabled(false);
            binding.imageView.setEnabled(false);
            sqlOperation();
        }
        else{
            // Eski veriyi yükle
            binding.savebtn.setVisibility(View.VISIBLE); // Güncelleme butonunu görünür yap
            sqlOperation();
        }
    }

    private void sqlOperation() {
        try {

            Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[]{String.valueOf(artId)});
            int artNameIx =cursor.getColumnIndex("artname");
            int painterNameIx = cursor.getColumnIndex("paintername");
            int yearIx = cursor.getColumnIndex("year");
            int imageIx = cursor.getColumnIndex("image");

            while(cursor.moveToNext()){

                currentName = cursor.getString(artNameIx);
                currentArtistName = cursor.getString(painterNameIx);
                currentYear = cursor.getString(yearIx);
                currentImage = cursor.getBlob(imageIx);


                binding.editTextName.setText(currentName);
                binding.editTextPname.setText(currentArtistName);
                binding.editTextYear.setText(currentYear);

                byte[] bytes = currentImage;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                binding.imageView.setImageBitmap(bitmap);
            }
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(View view){

        String name=binding.editTextName.getText().toString();
        String artistName=binding.editTextPname.getText().toString();
        String year=binding.editTextYear.getText().toString();

        byte[] byteArray;
        if (bitmap == null) {
            byteArray = currentImage;  // use the current image
        } else {
            Bitmap smallImage=makeSmallerImage(bitmap,300);//bitmap dedığım secılen ve bıtmape donusturulen resım
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //sql için resmi 0-1 halıne getırıyoruz
            smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);//resmin formatını kalıtesını ve bytearray adresını verdık
            byteArray =outputStream.toByteArray();//arraye donusturduk ve bunu byteArray degıskenıne attık
        }
        try {
            if(info.equals("new")){
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR,year VARCHAR,image BLOB)");
                sqlStrings= ("INSERT INTO arts (artname,paintername,year,image) VALUES(?, ?, ?, ?)");
            }
            else if(info.equals("update")){
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("UPDATE arts SET ");

                if (!name.equals(currentName)) {
                    queryBuilder.append("artname = ?, ");
                }
                if (!artistName.equals(currentArtistName)) {
                    queryBuilder.append("paintername = ?, ");
                }
                if (!year.equals(currentYear)) {
                    queryBuilder.append("year = ?, ");
                }
                if (!Arrays.equals(byteArray, currentImage)) {
                    queryBuilder.append("image = ?, ");
                }

                sqlStrings = queryBuilder.substring(0, queryBuilder.length() - 2);
                sqlStrings += " WHERE id = " + artId;
            }
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlStrings);

            int bindIndex = 1;

            if (!name.equals(currentName)) {
                sqLiteStatement.bindString(bindIndex++, name);
            }
            if (!artistName.equals(currentArtistName)) {
                sqLiteStatement.bindString(bindIndex++, artistName);
            }
            if (!year.equals(currentYear)) {
                sqLiteStatement.bindString(bindIndex++, year);
            }
            if (!Arrays.equals(byteArray, currentImage)) {
                sqLiteStatement.bindBlob(bindIndex++, byteArray);
            }

            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // intent ile diğer sayfa acılmadan once tum dıger acılan ıslmelerı kapatır.
        startActivity(intent);

    }

    public Bitmap makeSmallerImage( Bitmap image, int maximumSize)
    {
        int width=image.getWidth();//resmin geniliğini alır
        int height =image.getHeight();//resmin yüksekliğini alır

        float bitmapRatio =(float) width/(float) height;//resmin yatay mı dıkey mı oldugunun konrolu ıcın yazdık eger bırden buytukse yatay kucukse dıkey

        if(bitmapRatio>1) {
            //landscape image
            width=maximumSize;
            height=(int)(width/bitmapRatio);//yukseklıgı buyuk kenar olan genıslıgın ıkı kenarın oranına bolduk.Olculu kuculmesı ıcın
        }else
        {
            //porte image
            height =maximumSize;
            width=(int)(height*bitmapRatio);//aynı mantıgı yukseklık ıcın yaptık
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void selectedImage(View view){
    if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){ //izin verilmemiş ise
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Snackbar.make(view,"Permisson needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);//hangi izni istedıgımızı strın golarak ısıtıyor
                }
            }).show();
        }
        else{
            permissonLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }
    else //izin verildi yani gaelriye erişiyorsak
        {
            Intent intentTogallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentTogallery); //galerıyı acmak ıcın intenti istiyor

        }

    }
    private void registerLauncher(){
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult =result.getData();//buradaki getdata ıntenti verir ancak alltaki getdata uri iyi yanı resmın adresini verir
                    if(intentFromResult!=null){ //bos bir seş donmemiş ise
                        Uri imageDAta= intentFromResult.getData();//artık resım adresını bılıyoruz
                        //binding.imageView.setImageURI(imageDAta);

                        try {
                            if(Build.VERSION.SDK_INT>=28){ // yazdıgımız bıtmap android surumu 28 ve ustu olanlarda calıstıgı ıcın bu konbtrolu yazdık
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageDAta);
                                bitmap= ImageDecoder.decodeBitmap(source); //image datasını bitmape cevırdık
                                binding.imageView.setImageBitmap(bitmap);
                            }
                            else{//28 ve altı ıcın getbıtmap kullandık hepsı ıcın kullanmıyoruz cunku tedavulden kalkma ıhtımalı varmıs
                                bitmap=MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageDAta);
                                binding.imageView.setImageBitmap(bitmap);
                            }

                        }
                        catch (Exception e){
                            e.printStackTrace();//hataları logcatte gösterir
                        }
                    }
                }
            }
        });

        permissonLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() { //
            @Override
            public void onActivityResult(Boolean result) {
                if(result){ //eğer şartı sağlarsa izin verldi
                    Intent intentTogallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentTogallery);
                }
                else { //izin verilmedi
                    Toast.makeText(ArtActivity.this,"Permisson need!",Toast.LENGTH_LONG).show();
                }
            }

    });
    }
}