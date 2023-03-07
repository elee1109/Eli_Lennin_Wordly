package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    Button play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button newPuzzle = findViewById(R.id.new_puzzle);
        newPuzzle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<String> words = readWordsFromFile();
                    String[] randomWords = selectRandomWords(words);
                    String toast = "Word 1: " + randomWords[0] + " Word 2: " + randomWords[1];
                    Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
                    EditText word1 = findViewById(R.id.start_word);
                    EditText word2 = findViewById(R.id.end_word);
                    word1.setText(randomWords[0]);
                    word2.setText(randomWords[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


        play = findViewById(R.id.Play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WordlyActivity.class);
                startActivity(intent);

            }
        });

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
    public ArrayList<String> readWordsFromFile() throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.words_test);

        ArrayList<String> words = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            words.add(line);
        }
        br.close();
        return words;
    }
    // randomly selects two words (not the same word) from list of words
    public String[] selectRandomWords(ArrayList<String> words) {
        Random random = new Random();
        Collections.shuffle(words);
        String word1 = words.get(random.nextInt(words.size()));
        String word2;
        do {
            word2 = words.get(random.nextInt(words.size()));
        } while (word1.equals(word2));
        return new String[] {word1, word2};
    }

}