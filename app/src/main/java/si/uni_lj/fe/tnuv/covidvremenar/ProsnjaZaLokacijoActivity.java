package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class ProsnjaZaLokacijoActivity extends AppCompatActivity implements View.OnClickListener {
    FusedLocationProviderClient fusedLocationProviderClient;

    public int pridobiStPrebivalcev(List<Address> addresses){
        final int[] stPrebivalcev = {0};
        //pridobimo se podatek o številu prebivalcev obcine in pošljemo v shared preferences
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get("https://api.sledilnik.org/api/municipalities-list")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i=0;i<response.length();i++) {
                            try {
                                JSONObject obcina = response.getJSONObject(i);
                                if(obcina.optString("name").equals(String.valueOf(addresses.get(0).getAdminArea()))){
                                    Log.i("stPrebivalcev", ""+obcina.getInt("population"));
                                    stPrebivalcev[0] = obcina.getInt("population");
                                    break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("napaka","Ne morem dobiti seznama občin");
                    }
                });
        return stPrebivalcev[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prosnja_za_lokacijo);
        Button buttonDa = findViewById(R.id.buttonDa);
        buttonDa.setOnClickListener(this);

        Button buttonNe = findViewById(R.id.buttonNe);
        buttonNe.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Preveri, če je bila občina že izbrana
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        //Če je občina bila že izbrana, spremeni tekst v aktivnosti
        if(!isFirstRun) {
            TextView uporabaLokacije = (TextView)findViewById(R.id.textView3);
            uporabaLokacije.setText("Ali želite za spremembo občine uporabiti zaznavo lokacije?");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonDa: {
                // Če je uporabnik sprejel dostop do lokacije
                if (ActivityCompat.checkSelfPermission(ProsnjaZaLokacijoActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(ProsnjaZaLokacijoActivity.this, "Pridobivanje lokacije...", Toast.LENGTH_SHORT)
                            .show();
                    // Pridobi zadnjo znano lokacijo
                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                        //Log.i("I made it", "to location listener");
                        Location location = task.getResult();
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(ProsnjaZaLokacijoActivity.this,
                                        Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);

                                Log.i("Zaznana lokacija", String.valueOf(addresses.get(0)));

                                //Če objekt lokacije vsebuje AdminArea , odpri MojaObcinaActivity z imenom te občine
                                if(addresses.get(0).getAdminArea() != null) {
                                    Toast.makeText(ProsnjaZaLokacijoActivity.this,
                                            "Zaznana občina: " + String.valueOf(addresses.get(0).getAdminArea()), Toast.LENGTH_LONG)
                                            .show();
                                    //obcina.putExtra("IZBRANA_OBCINA", String.valueOf(addresses.get(0).getAdminArea()));
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("IZBRANA_OBCINA", String.valueOf(addresses.get(0).getAdminArea()));
                                    int prebivalci = pridobiStPrebivalcev(addresses);
                                    editor.putInt("ST_PREBIVALCEV",prebivalci);
                                    editor.apply();

                                    Intent obcina = new Intent(getApplicationContext(), MojaObcinaActivity.class);
                                    //Set isFirstRun to false in order to skip directly to MojaObcinaActivity next time
                                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                                            .putBoolean("isFirstRun", false).apply();

                                    //Začni MojaObcinaActivity
                                    startActivity(obcina);
                                }

                                //Če objekt lokacije ne vsebuje AdminArea (oz. je ta enak null), preusmeri na izbiro občine

                                else {
                                    Log.i("Lokacija","Občina ni bila zaznana");
                                    Toast.makeText(ProsnjaZaLokacijoActivity.this, "Občina ni bila najdena.", Toast.LENGTH_SHORT)
                                            .show();
                                    startActivity(new Intent(getApplicationContext()
                                            , IzbiraObcineActivity.class));
                                    overridePendingTransition(0, 0);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Toast.makeText(ProsnjaZaLokacijoActivity.this, "Lokacija ni bila najdena.", Toast.LENGTH_LONG)
                                    .show();
                            startActivity(new Intent(getApplicationContext()
                                    , IzbiraObcineActivity.class));
                            overridePendingTransition(0, 0);
                        }
                    });
                }
                else {
                    ActivityCompat.requestPermissions(ProsnjaZaLokacijoActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                    Toast.makeText(ProsnjaZaLokacijoActivity.this, "Prosim, ponovno pritisnite DA.", Toast.LENGTH_LONG)
                            .show();
                }
            }
            break;
            case R.id.buttonNe:
                startActivity(new Intent(getApplicationContext()
                        , IzbiraObcineActivity.class));
                overridePendingTransition(0, 0);
                break;
            default:
                break;
        }
    }
}