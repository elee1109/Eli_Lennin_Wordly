package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    Button play;
    Graph graph= new Graph();
    ArrayList<String> words;
    private ArrayList<String> sendPath= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button newPuzzle = findViewById(R.id.new_puzzle);



        try {
            words = readWordsFromFile();
            createGraph();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        newPuzzle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPuzzle();
            }
        });


        play = findViewById(R.id.Play);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText startWord = findViewById(R.id.start_word);
                EditText endWord = findViewById(R.id.end_word);
                String start = startWord.getText().toString();
                String end = endWord.getText().toString();
                ArrayList<String> potentialPath = graph.shortestPath(start, end);
                if(potentialPath == null){
                    Toast.makeText(MainActivity.this, "No path found", Toast.LENGTH_SHORT).show();
                }
                else if(potentialPath.size() > 8){
                    Toast.makeText(MainActivity.this, "Path too long", Toast.LENGTH_SHORT).show();
                }
                else if(potentialPath.size() == 1){
                    Toast.makeText(MainActivity.this, "Start and end words are the same", Toast.LENGTH_SHORT).show();
                }
                else if(potentialPath.size() == 2){
                    Toast.makeText(MainActivity.this, "Start and end words are too similar", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendPath = potentialPath;
                    Intent intent = new Intent(getApplicationContext(), WordlyActivity.class);
                    Log.d("sendPath", sendPath.toString());

                    intent.putStringArrayListExtra("path", sendPath);
                    startActivity(intent);
                }


            }
        });

        /**
         * ChatGPT3 derived code for flagging first time app launch
         */
        // Get a reference to the shared preferences object
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Check if the "firstTime" flag is set
        if (sharedPreferences.getBoolean("firstTime", false)) {

            Log.d("firstTime", " not first time");


        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
            TutorialFragment tutorialFragment = new TutorialFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container, tutorialFragment);
            transaction.commit();

        }



    }
    public void createGraph(){
        graphExecutor ge = new graphExecutor();
        ge.execute(new graphExecutorCallback() {
            @Override
            public void onGraphCreated(Graph graph) {
                Log.d("graph", "graph created");
                createPuzzle();
            }
        });
    }
    interface graphExecutorCallback{
        void onGraphCreated(Graph graph);
    }
    public class graphExecutor {
        public void execute(graphExecutorCallback callback){
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    graph.buildGraph(words);
                    callback.onGraphCreated(graph);
                }
            });
        }
    }

    public void createPuzzle(){

            String[] randomWords = selectRandomWords(words);
            String startWord = randomWords[0];
            String endWord = randomWords[1];


            ArrayList<String> path = graph.shortestPath(startWord, endWord);
            if (path == null || path.size() > 8 || path.size() < 3) {

                createPuzzle();

            } else {
                Log.d("HIT", "HIT, EDIT TEXTS SHOULD BE SET");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        EditText word1 = findViewById(R.id.start_word);
                        EditText word2 = findViewById(R.id.end_word);
                        word1.setText(randomWords[0]);
                        word2.setText(randomWords[1]);
                        sendPath = path;
                    }
                });


            }


    }
    public ArrayList<String> readWordsFromFile() throws IOException {

        ArrayList<String> words = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(R.raw.words_gwicks);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            if(line.length() == 4){
                words.add(line);
            }
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
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("correctPath", sendPath);
    }
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sendPath = savedInstanceState.getStringArrayList("correctPath");

    }

}