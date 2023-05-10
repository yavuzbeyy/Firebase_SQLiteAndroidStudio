package com.huns.yokdilkelimen;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Kelimeler extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    ArrayList<String> turkceKelimeler = new ArrayList<>();
    ArrayList<String> ingilizceKelimeler = new ArrayList<>();
    ArrayList<String> cumleler = new ArrayList<>();
    ArrayList<String> cumlelerTr = new ArrayList<>();

    TextView ingilizceKelime, turkceKelime, cumle ,kelimeID , cumleTr;

    Button casililacaklaraEkle , sonrakiKelime , backButton;

    SQLiteDatabase db;

    int reklamCounter = 0;

    private AdManagerAdView mAdManagerAdView;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelimeler);

        AdRequest sayacGecisiReklami = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-4812352403081392/9895603448", sayacGecisiReklami,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });



        mAdManagerAdView = findViewById(R.id.adManagerAdView);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdView.loadAd(adRequest);



        ingilizceKelime = findViewById(R.id.ingilizceKelimeTxt);
        turkceKelime    = findViewById(R.id.turkceKelimeTxt);
        cumle           = findViewById(R.id.ornekCumle);
        kelimeID        = findViewById(R.id.kelimeSayisi);
        cumleTr         = findViewById(R.id.turkceCumle);

        backButton      = findViewById(R.id.backButton);

        casililacaklaraEkle = findViewById(R.id.DeleteWordButton);
        sonrakiKelime       = findViewById(R.id.nextWordButton);

        firebaseFirestore = FirebaseFirestore.getInstance();
        getData();
        sqLitedbConnection();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

                AdView adView = new AdView(Kelimeler.this);

                adView.setAdSize(AdSize.BANNER);

                adView.setAdUnitId("ca-app-pub-4812352403081392/6989404355");
            }
        });

    }

    private void getData(){
        firebaseFirestore.collection("words").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if(error != null){
                Toast.makeText(Kelimeler.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }
            if (value != null){
                for(DocumentSnapshot snapshot : value.getDocuments()){
                  Map<String,Object> data = snapshot.getData();

                  ingilizceKelimeler.add((String) data.get("English"));

                  turkceKelimeler.add((String) data.get("Turkish"));

                  cumleler.add((String) data.get("cumle"));

                  cumlelerTr.add((String) data.get("trCumle"));

                  ilkKelimeleriYaz();

                }
            }
            }
        });

    }

    private void ilkKelimeleriYaz(){
        ingilizceKelime.setText(ingilizceKelimeler.get(0));
        turkceKelime.setText(turkceKelimeler.get(0));
        cumle.setText(cumleler.get(0));
        cumleTr.setText(cumlelerTr.get(0));
        kelimeID.setText("Kelime Sırası = 1");
        calisilacaklaraEkle(0);

    }



    public void calisilacaklaraEkle(int kayitIndex){

        int indexKayitSirasi = kayitIndex;

        casililacaklaraEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sqlString = "Insert Into Kelimeler(englishWord,turkishWord) VALUES(?,?)";
                SQLiteStatement sqLiteStatement = db.compileStatement(sqlString);
                sqLiteStatement.bindString(1,ingilizceKelimeler.get(indexKayitSirasi));
                sqLiteStatement.bindString(2,turkceKelimeler.get(indexKayitSirasi));
                sqLiteStatement.execute();


                ingilizceKelime.setText(ingilizceKelimeler.get(indexKayitSirasi));
                turkceKelime.setText(turkceKelimeler.get(indexKayitSirasi));
                cumle.setText(cumleler.get(indexKayitSirasi));
                cumleTr.setText(cumlelerTr.get(indexKayitSirasi));
                kelimeID.setText("Kelime Sırası = " + (indexKayitSirasi+1));



                MediaPlayer mediaPlayer;
                mediaPlayer = MediaPlayer.create(Kelimeler.this,R.raw.gecisefekti);
                mediaPlayer.start();

                Toast.makeText(getApplicationContext(),"Kelime Eklendi",Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void siradakiKelime(View view){

        reklamCounter = reklamCounter + 1;
        if (reklamCounter== 25 || reklamCounter== 70  || reklamCounter== 125){
            sayacReklamiGoster();
        }

        // İngilizce kelimeler listesinin size aralığında rastgele bir değişken al ve bunla havuzdan rastgele bir kelime getir.
        Random random = new Random();
        int rastgeleKelime = random.nextInt(ingilizceKelimeler.size());
        ingilizceKelime.setText(ingilizceKelimeler.get(rastgeleKelime));
        turkceKelime.setText(turkceKelimeler.get(rastgeleKelime));
        cumle.setText(cumleler.get(rastgeleKelime));
        cumleTr.setText(cumlelerTr.get(rastgeleKelime));
        kelimeID.setText("Kelime Sırası = " + (rastgeleKelime+1));

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(Kelimeler.this,R.raw.gecis);
        mediaPlayer.start();

        calisilacaklaraEkle(rastgeleKelime);
    }

    private void sayacReklamiGoster() {

        if (mInterstitialAd != null) {
            mInterstitialAd.show(Kelimeler.this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    public void geriGel(View view){

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(Kelimeler.this,R.raw.gecis);
        mediaPlayer.start();

        Intent intent = new Intent(Kelimeler.this,SecondPage.class);
        startActivity(intent);
    }

    public void sqLitedbConnection(){
        try {

            db = this.openOrCreateDatabase("UnutulanKelimeler",MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS Kelimeler(id INTEGER PRIMARY KEY ,englishWord VARCHAR,turkishWord VARCHAR)");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}