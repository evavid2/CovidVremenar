package si.uni_lj.fe.tnuv.covidvremenar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class IzbiraObcineActivity extends AppCompatActivity {
    ArrayList<String> obcine=new ArrayList<>();
    public static String[] GetStringArray(ArrayList<String> arr)
    {
        // declaration and initialise String Array
        String str[] = new String[arr.size()];
        // ArrayList to Array Conversion
        for (int j = 0; j < arr.size(); j++) {
            // Assign each value to String array
            str[j] = arr.get(j);
        }
        return str;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.get("https://api.sledilnik.org/api/municipalities-list")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i("uspeh", response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonobject = response.getJSONObject(i);
                                obcine.add(jsonobject.getString("name"));
                            }
                            String[] str = GetStringArray(obcine);
                            str[0] = "Izberi doma??o ob??ino:";
                            // Selection of the spinner
                            Spinner spinner = (Spinner) findViewById(R.id.spinner_obcine);
                            // Application of the Array to the Spinner
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(IzbiraObcineActivity.this,   R.layout.spinner_style, str);
                            spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item); // The drop down view
                            spinner.setAdapter(spinnerArrayAdapter);
                            String textFromSpinner = spinner.getSelectedItem().toString();
                            Log.i("izbrana opcija:", textFromSpinner);

                            //glede na izbrano ob??ino preusmerim na MojaObcinaActivity in zraven podam podatke o imenu izbrane ob??ine in ??tevilu prebivalcev v ob??ini
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?>arg0, View view, int arg2, long arg3) {
                                    String selected_val=spinner.getSelectedItem().toString();
                                    int selected_index = spinner.getSelectedItemPosition();
                                    if(!selected_val.equals("Izberi doma??o ob??ino:")){
                                        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("IZBRANA_OBCINA", selected_val);  // Saving string
                                        editor.apply(); // commit changes
                                        Intent intent = new Intent(getApplicationContext(), MojaObcinaActivity.class);
                                        //intent.putExtra("IZBRANA_OBCINA", selected_val);
                                        //pridobim podatek o ??tevilu prebivalcev za izbrano ob??ino
                                        try {
                                            JSONObject jsonobject = response.getJSONObject(selected_index);
                                            int stPrebivalcev = jsonobject.getInt("population");
                                            editor.putInt("ST_PREBIVALCEV", stPrebivalcev);
                                            editor.apply();
                                            //intent.putExtra("ST_PREBIVALCEV", stPrebivalcev);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        //Set isFirstRun to false in order to skip directly to MojaObcinaActivity next time
                                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                                                .putBoolean("isFirstRun", false).apply();

                                        startActivity(intent);
                                    }
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {
                                    // TODO Auto-generated method stub
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("napaka","Ne morem dobiti seznama ob??in");
                    }
                });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_izbira_obcine);
    }

}