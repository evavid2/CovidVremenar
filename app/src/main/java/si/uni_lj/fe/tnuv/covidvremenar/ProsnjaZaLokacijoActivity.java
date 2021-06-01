package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



public class ProsnjaZaLokacijoActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prosnja_za_lokacijo);
        Button buttonDa = findViewById(R.id.buttonDa);
        buttonDa.setOnClickListener(this);

        Button buttonNe = findViewById(R.id.buttonNe);
        buttonNe.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonDa:
                // do your code
                public void foo(this) {
                // when you need location
                // if inside activity context = this;

                SingleShotLocationProvider.requestSingleUpdate(this,
                        new SingleShotLocationProvider.LocationCallback() {
                            @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                                Log.d("Location", "my location is " + location.toString());
                            }
                        });
            }
                break;
            case R.id.buttonNe:
                //startActivity(new Intent(getApplicationContext()
                //        , IzbiraObcineActivity.class));
                //overridePendingTransition(0,0);
                break;
            default:
                break;
        }
    }
}