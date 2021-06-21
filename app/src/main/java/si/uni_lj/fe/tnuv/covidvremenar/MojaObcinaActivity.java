package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MojaObcinaActivity extends AppCompatActivity {
    public JSONObject vrniObjektPodatkovObcine(JSONArray response, String imeObcine) {
        try {
            JSONObject jsonobject = response.getJSONObject(0);
            JSONObject jsonobjectRegij = jsonobject.getJSONObject("regions");
            JSONArray keys = jsonobjectRegij.names();
            //for loop gre čez vsako regijo in v value shrani objekt s ključi, ki so imena občin v posamezni regiji
            for (int i = 0; i < Objects.requireNonNull(keys).length(); i++) {
                String key = keys.getString(i);
                JSONObject jsonobjectObcin = jsonobjectRegij.getJSONObject(key);
                JSONArray imenaObcin = jsonobjectObcin.names();
                for (int j = 0; j < Objects.requireNonNull(imenaObcin).length(); j++) {
                    String trenutnaObcina = imenaObcin.getString(j);
                    //predpocesiranje imen, da dobimo ustreznega s tistim iz podatkov apija
                    if (imeObcine != null) {
                        //najprej pregledam če vsebuje vezaj in če ga, pustim vezaj in zbrišem presledke okrog
                        Pattern pattern = Pattern.compile("-");
                        Matcher matcher = pattern.matcher(imeObcine);
                        boolean isStringContainsSpecialCharacter = matcher.find();
                        int iend;
                        String imeObcineUpdate = imeObcine;
                        if (isStringContainsSpecialCharacter) {
                            iend = imeObcine.indexOf("-");
                            String podImePrviDel;
                            String vmesniDel;
                            String podImeDrugiDel;
                            podImePrviDel = imeObcine.substring(0, iend - 1);
                            vmesniDel = imeObcine.substring(iend, iend + 2);
                            podImeDrugiDel = imeObcine.substring(iend + 2);
                            vmesniDel = vmesniDel.replaceAll(" ", "");
                            imeObcineUpdate = podImePrviDel + vmesniDel + podImeDrugiDel;
                            Log.i("rez:", imeObcineUpdate);
                        }
                        //pregledam ce vsebuje piko-slov., krajšano za slovenskih
                        Pattern pika;
                        pika = Pattern.compile(getString(R.string.znak_pika));
                        Matcher ujemanje = pika.matcher(imeObcineUpdate);
                        boolean aliVsebujePiko = ujemanje.find();
                        if (aliVsebujePiko) {
                            iend = imeObcineUpdate.indexOf(".");
                            if(iend != -1) {
                                String podImePrviDel;
                                String vmesniDel;
                                String podImeDrugiDel;
                                podImePrviDel = imeObcine.substring(0, iend - 4);
                                vmesniDel = imeObcine.substring(iend - 4, iend);
                                podImeDrugiDel = imeObcine.substring(iend + 1);
                                vmesniDel = "Slovenskih";
                                imeObcineUpdate = podImePrviDel + vmesniDel + podImeDrugiDel;
                            }
                        }
                        //odstranimo oz zamenjamo vse presledke iz imena
                        imeObcineUpdate = (imeObcineUpdate).replaceAll(" ", "_");
                        //pretvorimo velike zacetnice v male
                        imeObcineUpdate = imeObcineUpdate.toLowerCase();
                        //pregledamo ce vsebuje / in če vsebuje, vzamemo samo prvo besedo do /
                        pattern = Pattern.compile("/");
                        matcher = pattern.matcher(imeObcineUpdate);
                        isStringContainsSpecialCharacter = matcher.find();
                        if (isStringContainsSpecialCharacter) {
                            iend = imeObcineUpdate.indexOf("/");
                            imeObcineUpdate = imeObcineUpdate.substring(0, iend); //this will give abc
                        }
                        /*Log.i("ime obcine prava", imeObcineUpdate);
                        Log.i("ime obcine iz apija", trenutnaObcina);*/
                        if (trenutnaObcina.equals(imeObcineUpdate)) {
                            //pridobimo podatke za izbrano občino in jih izpišemo v aktivnosti
                            JSONObject podatkiZaObcino = jsonobjectObcin.getJSONObject(imeObcineUpdate);
                            return podatkiZaObcino;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    //funkcija, ki glede na podan datum pridobi število aktivnih primerov za podano občino in jih izpiše v aktivnosti
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    public int pridobiPodatkeZaObcino(String datum, LocalDate date, Integer stevec, String imeObcine) {
        stevec++;
        //funkcija se pokliče največ 7-krat, drugače ni podatkov, če je uspeh vrne 1, če ne 0
        if (stevec <= 7) {
            //klic apija za pridobitev podatkov o potrjenih primerih za izbrano občino
            Integer finalStevec = stevec;
            AndroidNetworking.get("https://api.sledilnik.org/api/municipalities?from=" + datum + "&to=" + datum)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONArray(new JSONArrayRequestListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.length() == 0) {
                                //ce ni podatka za podan datum probavamo za dan prej dokler ne dobimo podatkov
                                LocalDate danPrej = date.minusDays(1);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                String novDatum = danPrej.format(formatter);
                                pridobiPodatkeZaObcino(novDatum, danPrej, finalStevec, imeObcine);
                            } else {
                                //dobimo json array za podan datum in izluščimo št potrjenih primerov za občino ter to zapišemo v pogled aktivnosti
                                JSONObject podatkiZaObcino = vrniObjektPodatkovObcine(response, imeObcine);
                                final int[] stAktivnih = {0};
                                final int[] stPotrjenihDoZdaj = {0};
                                try {
                                    if(podatkiZaObcino != null) {
                                        if(!podatkiZaObcino.isNull("activeCases")) {
                                            stAktivnih[0] = podatkiZaObcino.getInt("activeCases");
                                            TextView prikazPotrjenihPrimerov = findViewById(R.id.textView7);
                                            prikazPotrjenihPrimerov.setText(String.valueOf(stAktivnih[0]));
                                        }
                                        if(!podatkiZaObcino.isNull("confirmedToDate")) {
                                            stPotrjenihDoZdaj[0] = podatkiZaObcino.getInt("confirmedToDate");
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                final int[] danasnjiNovi = {0};
                                final int[] trenutni = {0};
                                final int[] aktivniPrejsnji = {0};
                                LocalDate danasnjiDan = date;
                                LocalDate danPrej = danasnjiDan;
                                //pridobimo datum za en dan prej
                                danPrej = danasnjiDan.minusDays(1);
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                                String novDatum = danPrej.format(formatter);
                                AndroidNetworking.get("https://api.sledilnik.org/api/municipalities?from=" + novDatum + "&to=" + novDatum)
                                        .build()
                                        .getAsJSONArray(new JSONArrayRequestListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                //dobimo json array za podan datum in izluščimo št potrjenih primerov za občino
                                                JSONObject podatkiZaObcino = vrniObjektPodatkovObcine(response, imeObcine);
                                                try {
                                                    if(podatkiZaObcino != null) {
                                                        trenutni[0] = podatkiZaObcino.getInt("confirmedToDate");
                                                        aktivniPrejsnji[0] = podatkiZaObcino.getInt("activeCases");
                                                    }
                                                    else{
                                                        Log.i("aha", "tole ne gre");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                //nastavimo st novih primerov v pogled
                                                danasnjiNovi[0] = stPotrjenihDoZdaj[0] - trenutni[0];
                                                TextView prikazNovihPrimerov = findViewById(R.id.textViewSteviloNovih);
                                                prikazNovihPrimerov.setText(String.valueOf(danasnjiNovi[0]));
                                                //pogledamo če št aktivnih narašča glede na prejsnji dan
                                                //nastavljanje barve teksta za tedenski prirast, če gre gor je rdeče, če gre dol je zeleno, ter slike, če gre gor je dež, če gre dol je sonce
                                                TextView tedenskiPrirast =(TextView)findViewById(R.id.textView8);
                                                ImageView slikaVremena = (ImageView)findViewById(R.id.imageView10);
                                                Log.i("stAktivnih danes", ""+stAktivnih[0]);
                                                Log.i("aktivni prej:", ""+aktivniPrejsnji[0]);
                                                if(stAktivnih[0] <= aktivniPrejsnji[0]){
                                                    tedenskiPrirast.setText("Pada");
                                                    tedenskiPrirast.setTextColor(Color.GREEN);
                                                    slikaVremena.setImageResource(R.mipmap.soncek);
                                                }
                                                else if(stAktivnih[0] > aktivniPrejsnji[0]){
                                                    tedenskiPrirast.setText("Narašča");
                                                    tedenskiPrirast.setTextColor(Color.RED);
                                                    slikaVremena.setImageResource(R.mipmap.dezevno_vreme);
                                                }
                                                else{
                                                    tedenskiPrirast.setTextColor(Color.BLACK);
                                                    slikaVremena.setImageResource(R.mipmap.soncek);
                                                }
                                            }
                                            @Override
                                            public void onError(ANError error) {
                                                Log.e("napaka", "Ne morem dobiti seznama občin");

                                            }
                                        });
                            }
                            //napišemo še napis v vednost na kateri datum so prikazani podatki
                            TextView obvestiloODatumu = (TextView) findViewById(R.id.textViewObvestiloODatumu);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                            String koncniDatum = date.format(formatter);
                            obvestiloODatumu.setText("Prikazani so najnovejši podatki za dan: " + koncniDatum);
                        }

                        @Override
                        public void onError(ANError error) {
                            Log.e("napaka", "Ne morem dobiti seznama občin");

                        }
                    });
            return 1;
        } else {
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidNetworking.initialize(getApplicationContext());
        //Preveri ali je bila občina že izbrana
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show start activity

            startActivity(new Intent(MojaObcinaActivity.this, PozdravniZaslonActivity.class));
            //Toast.makeText(MojaObcinaActivity.this, "First Run", Toast.LENGTH_LONG)
                    //.show();
        }

        // Uncomment if you want to only show PozdravniZaslon once

        //getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
        //        .putBoolean("isFirstRun", false).apply();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moja_obcina);


        //dinamično nastavljanje imena občine glede na to katera je izbrana
        //String imeObcine = getIntent().getStringExtra("IZBRANA_OBCINA");
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        String imeObcine = prefs.getString("IZBRANA_OBCINA", "No name defined");
        TextView naslovObcine = (TextView)findViewById(R.id.textView);
        naslovObcine.setText(imeObcine);

        //dinamično nastavljanja podatka o številu prebivalcev
        //int stPrebivalcev = getIntent().getIntExtra("ST_PREBIVALCEV", 0);
        int stPrebivalcev = prefs.getInt("ST_PREBIVALCEV", 0);
        TextView prikazStevilaPrebivalcev = (TextView)findViewById(R.id.textView6);
        prikazStevilaPrebivalcev.setText(String.valueOf(stPrebivalcev));

        //klic apija za pridobitev podatkov o potrjenih primerih za izbrano občino
        LocalDate date = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays( 1 );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String datum = date.format(formatter);
        String datumVceraj = yesterday.format(formatter);
        int stevecPonovitev = 0;
        pridobiPodatkeZaObcino(datum, date, stevecPonovitev, imeObcine);


        /*//nastavljanje barve teksta za tedenski prirast, če gre gor je rdeče, če gre dol je zeleno, ter slike, če gre gor je dež, če gre dol je sonce
        TextView tedenskiPrirast =(TextView)findViewById(R.id.textView8);
        ImageView slikaVremena = (ImageView)findViewById(R.id.imageView10);
        String prirast = getString(R.string.procent_tedenskega_prirasta);
        char ch1 = prirast.charAt(0);
        if(ch1 == '+'){
            tedenskiPrirast.setTextColor(Color.RED);
            slikaVremena.setImageResource(R.mipmap.dezevno_vreme);
        }
        else if(ch1 == '-'){
            tedenskiPrirast.setTextColor(Color.GREEN);
            slikaVremena.setImageResource(R.mipmap.soncek);
        }
        else{
            tedenskiPrirast.setTextColor(Color.BLACK);
            slikaVremena.setImageResource(R.mipmap.soncek);
        }*/

        //spodnja navigacija
        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.mojaobcina);
        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.mojaobcina:
                        return true;
                    case R.id.slovenija:
                        startActivity(new Intent(getApplicationContext()
                                , SlovenijaActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext()
                                , AboutActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cepljenje:
                        startActivity(new Intent(getApplicationContext()
                                , UkrepiActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }
}
