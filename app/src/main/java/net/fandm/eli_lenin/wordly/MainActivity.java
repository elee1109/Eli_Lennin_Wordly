package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * ChatGPT3 derived code for flagging first time app launch
         */
        // Get a reference to the shared preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Check if the "firstTime" flag is set
        if (sharedPreferences.getBoolean("firstTime", true)) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", false);
            editor.apply();
            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, tutorialFragment);
            transaction.commit();


            // Do something for the first time, like show an introduction or tutorial screen
        } else {
            Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();
        }



    }
}