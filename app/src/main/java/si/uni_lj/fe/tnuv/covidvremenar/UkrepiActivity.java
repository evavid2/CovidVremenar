package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class UkrepiActivity extends AppCompatActivity {
    //stevilo cepljenih: [prvaDozaDanes,prvaDozaDoZdaj, drugaDozaDanes, drugaDozaDoZdaj]
    private int[] steviloCepljenih = new int[4];
    //steviloOdmerkovDoSedaj
    private int odmerkiDoSedaj = 0;
    //število odmerkov po proizvajalcu
    private int[] janssen_moderna_pfizer_az = new int [4];

    String vcerajsnjiDatum = new String("2020-12-31");
    //deleži proizvajalcev ter imena za graf
    private float[] deleziProizvajalcev = new float[4];
    private String[] proizvajalci = {"Janssen","Moderna","Pfizer","AstraZeneca"};

    //deklaracija grafa
    PieChart grafDelezProizvajalcevCepiv;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    public int pridobiPodatkeCepljenja(String datum) {
        AndroidNetworking.get("https://api.sledilnik.org/api/vaccinations?from="+datum+"&to="+datum)
                .setTag("cepljenje")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONArray response) {
                        //Log.i("JSON Cepljenja", response.toString());
                        if(response.length() > 0) {
                            try {
                                JSONObject PodatkiCepljenja = response.getJSONObject(0);
                                //Log.i("JSON OBJECT CEPLJENJE",PodatkiCepljenja.toString());
                                //Log.i("prva doza",PodatkiCepljenja.getString("administered"));

                                //Zapiši podatke za danes in do zdaj v tabelo steviloCepljenih
                                JSONObject prvaDoza = PodatkiCepljenja.getJSONObject("administered");
                                JSONObject drugaDoza = PodatkiCepljenja.getJSONObject("administered2nd");

                                steviloCepljenih[0] = prvaDoza.getInt("today");
                                steviloCepljenih[1] = prvaDoza.getInt("toDate");

                                steviloCepljenih[2] = drugaDoza.getInt("today");
                                steviloCepljenih[3] = drugaDoza.getInt("toDate");

                                //Izlušči podatke o posameznem proizvajalcu cepiva
                                odmerkiDoSedaj = PodatkiCepljenja.getInt("usedToDate");
                                //Log.i("odmerkiDoSedaj", String.valueOf(odmerkiDoSedaj));
                                JSONObject odmerkiPoProizvajalcu = PodatkiCepljenja.getJSONObject("usedByManufacturer");

                                //zapiši podatke o odmerkih proizvajalcev
                                janssen_moderna_pfizer_az[0] = odmerkiPoProizvajalcu.getInt("janssen");
                                janssen_moderna_pfizer_az[1] = odmerkiPoProizvajalcu.getInt("moderna");
                                janssen_moderna_pfizer_az[2] = odmerkiPoProizvajalcu.getInt("pfizer");
                                janssen_moderna_pfizer_az[3] = odmerkiPoProizvajalcu.getInt("az");
                                //Log.i("janssen", String.valueOf(janssen_moderna_pfizer_az[0]));

                                if(odmerkiDoSedaj > 0) {
                                    for (int i = 0; i < janssen_moderna_pfizer_az.length; i++) {
                                        deleziProizvajalcev[i] = (float)janssen_moderna_pfizer_az[i] / (float)odmerkiDoSedaj;
                                        //Log.i(proizvajalci[i], String.valueOf(deleziProizvajalcev[i]));
                                    }

                                    TextView prvaDozaDanes = (TextView)findViewById(R.id.tvPrvaDozaDanes);
                                    prvaDozaDanes.setText(String.valueOf(steviloCepljenih[0]));

                                    TextView prvaDozaDoDanes = (TextView)findViewById(R.id.tvPrvaDozaDoDanes);
                                    prvaDozaDoDanes.setText(String.valueOf(steviloCepljenih[1]));

                                    TextView drugaDozaDanes = (TextView)findViewById(R.id.tvDrugaDozaDanes);
                                    drugaDozaDanes.setText(String.valueOf(steviloCepljenih[2]));

                                    TextView drugaDozaDoDanes = (TextView)findViewById(R.id.tvDrugaDozaDoDanes);
                                    drugaDozaDoDanes.setText(String.valueOf(steviloCepljenih[3]));


                                    //izrisi graf deleza proizvajalcev cepiv
                                    grafDelezProizvajalcevCepiv = (PieChart) findViewById(R.id.GrafCepljenje);
                                    Description description = grafDelezProizvajalcevCepiv.getDescription();
                                    description.setText("Delež odmerkov cepiva po proizvajalcu");
                                    description.setTextSize(14f);
                                    description.setYOffset(-13f);

                                    grafDelezProizvajalcevCepiv.setNoDataText("Ni bilo mogoče izrisati grafa");
                                    grafDelezProizvajalcevCepiv.setRotationEnabled(true);
                                    //grafDelezProizvajalcevCepiv.setRotation(270f);
                                    //grafDelezProizvajalcevCepiv.setUsePercentValues(true);
                                    //grafDelezProizvajalcevCepiv.setCenterTextColor(Color.BLACK);
                                    grafDelezProizvajalcevCepiv.setHoleRadius(25f);
                                    grafDelezProizvajalcevCepiv.setTransparentCircleAlpha(0);
                                    //grafDelezProizvajalcevCepiv.setMaxAngle(180f);
                                    //grafDelezProizvajalcevCepiv.setCenterTextSize(10);
                                    //grafDelezProizvajalcevCepiv.setDrawEntryLabels(true);
                                    //grafDelezProizvajalcevCepiv.setEntryLabelTextSize(20);
                                    //More options just check out the documentation!

                                    dodajPodatke();
                                }
                                else Log.e("odmerkiDoSedaj", String.valueOf(odmerkiDoSedaj));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else pridobiPodatkeCepljenja(vcerajsnjiDatum);

                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("Networking error:", String.valueOf(error));
                    }
                });
        return 1;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ukrepi);

        AndroidNetworking.initialize(getApplicationContext());

        LocalDate date = LocalDate.now();
        LocalDate yesterday = LocalDate.now().minusDays( 1 );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        String datum = date.format(formatter);
        vcerajsnjiDatum = yesterday.format(formatter);
        //Log.i("DatumCepljenje",datum);

        pridobiPodatkeCepljenja(datum);



        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.cepljenje);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.cepljenje:
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
                    case R.id.mojaobcina:
                        startActivity(new Intent(getApplicationContext()
                                , MojaObcinaActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void dodajPodatke() {
        //podatki za graf so v arraylistu
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        //Dodaj podatke v Arrayliste iz prejsnjih Arrayov
        for(int i = 0; i < janssen_moderna_pfizer_az.length; i++){
            yEntrys.add(new PieEntry(janssen_moderna_pfizer_az[i], proizvajalci[i]));
        }

        for(int i = 0; i < proizvajalci.length; i++){
            xEntrys.add(proizvajalci[i]);
        }

        //naredi podatkovni set
        PieDataSet podatkovniSet = new PieDataSet(yEntrys, "");
        //podatkovniSet.setSliceSpace(2);
        podatkovniSet.setValueTextSize(14);
        podatkovniSet.setValueTextColor(0xffffffff);
        

        //dodaj barve
        ArrayList<Integer> barve = new ArrayList<>();
        barve.add(Color.argb(255,62,165,195));
        barve.add(Color.argb(255,0,123,152));
        barve.add(Color.argb(255,0,84,111));
        barve.add(Color.argb(255,0,48,73));

        podatkovniSet.setColors(barve);

        //dodaj legendo
        Legend legend = grafDelezProizvajalcevCepiv.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);

        //naredi PieData objekt
        PieData podatki = new PieData(podatkovniSet);
        grafDelezProizvajalcevCepiv.setData(podatki);
        grafDelezProizvajalcevCepiv.invalidate();

    }

}