package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class ProsnjaZaLokacijoActivity extends AppCompatActivity implements View.OnClickListener {
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_prosnja_za_lokacijo);
        Button buttonDa = findViewById(R.id.buttonDa);
        buttonDa.setOnClickListener(this);

        Button buttonNe = findViewById(R.id.buttonNe);
        buttonNe.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
                                    Intent obcina = new Intent(getApplicationContext(), MojaObcinaActivity.class);
                                    obcina.putExtra("IZBRANA_OBCINA", String.valueOf(addresses.get(0).getAdminArea()));

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