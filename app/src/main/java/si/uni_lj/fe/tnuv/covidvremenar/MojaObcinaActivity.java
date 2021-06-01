package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class MojaObcinaActivity extends AppCompatActivity {

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

 /*       AndroidNetworking.get("https://api.sledilnik.org/api/municipalities-list")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("uspeh", response.toString());
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("napaka","Ne morem dobiti seznama občin");

                    }
                });
*/
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
