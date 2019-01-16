package com.arrkays.poutre;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;


public class SettingsActivity extends AppCompatActivity {
    Switch switch_vibration;
    Switch switch_darkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Res.store.getCurrentTheme().equals("Dark")) { setTheme(R.style.AppDarkTheme); }
        else { setTheme(R.style.AppLightTheme); }

        setContentView(R.layout.activity_settings);



        switch_vibration = findViewById(R.id.switch_vibration);
        switch_darkMode = findViewById(R.id.switch_darkMode);

        switch_vibration.setChecked(Res.vibration);
        if (Res.store.getCurrentTheme().equals("Dark")) { switch_darkMode.setChecked(true); }
        else { switch_darkMode.setChecked(false); }


        event();

    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void event(){
        // Activation ou non des vibrations
        switch_vibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Res.vibration = b;
            }
        });
        // Activation ou non du mode sombre
        switch_darkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch_darkMode.isChecked()) {
                    Res.store.setCurrentTheme("Dark");
                    recreate();
                }
                else {
                    Res.store.setCurrentTheme("Ligth");
                    recreate();
                }
            }
        });
    }
}
