package si.uni_lj.fe.tnuv.covidvremenar;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.MenuItem;

        import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SlovenijaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slovenija);

        //Initialize And Assign Variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //Set Home Selected
        bottomNavigationView.setSelectedItemId(R.id.slovenija);

        //Perform ItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.mojaobcina:
                        startActivity(new Intent(getApplicationContext()
                                , MojaObcinaActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.slovenija:
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