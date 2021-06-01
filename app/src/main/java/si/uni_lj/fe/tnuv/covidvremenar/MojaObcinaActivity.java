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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class MojaObcinaActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidNetworking.initialize(getApplicationContext());
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show start activity

            startActivity(new Intent(MojaObcinaActivity.this, PozdravniZaslonActivity.class));
            //Toast.makeText(MojaObcinaActivity.this, "First Run", Toast.LENGTH_LONG)
                    //.show();
        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moja_obcina);

        LocalDate date = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays( 1 );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String datum = date.format(formatter);
        String datumVceraj = yesterday.format(formatter);
        //klic apija za pridobitev podatkov o potrjenih primerih za izbrano občino
        AndroidNetworking.get("https://api.sledilnik.org/api/municipalities?from="+datumVceraj+"&to="+datumVceraj)
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("dobim", response.toString());

                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("napaka","Ne morem dobiti seznama občin");

                    }
                });

        //dinamično nastavljanje imena občine glede na to katera je izbrana
        String imeObcine = getIntent().getStringExtra("IZBRANA_OBCINA");
        TextView naslovObcine = (TextView)findViewById(R.id.textView);
        naslovObcine.setText(imeObcine);

        //dinamično nastavljanja podatka o številu prebivalcev
        int stPrebivalcev = getIntent().getIntExtra("ST_PREBIVALCEV", 0);
        TextView prikazStevilaPrebivalcev = (TextView)findViewById(R.id.textView6);;
        prikazStevilaPrebivalcev.setText(String.valueOf(stPrebivalcev));

        //nastavljanje barve teksta za tedenski prirast, če gre gor je rdeče, če gre dol je zeleno, ter slike, če gre gor je dež, če gre dol je sonce
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
        }

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
                    case R.id.ukrepi:
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
