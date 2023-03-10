package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WordlyActivity extends AppCompatActivity {

    /**
     * ImageView slideshow delay found on chatgpt3
     */
    private Handler handler= null;
    public Runnable runnable;
    private final int delay = 2000;

    public int index =0;
    public String next_word; //change when algo built
    public View decorView;
    public wordArrayAdapter waa;
    public GridView gv;
    ImageView star;

    ArrayList<Integer> colors;

    ArrayList<String> correct_path;
    public int currWordIndex = 1;
    Thread iheThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wordly);
        ImageView iv = (ImageView) findViewById(R.id.hint_image);
        iv.setImageResource(R.drawable.wordly_icon);
        star = findViewById(R.id.gold_star);
        star.setVisibility(View.GONE);


        correct_path =  getIntent().getStringArrayListExtra("path");
        colors = new ArrayList<Integer>();
        int size = correct_path.size();
        for(int i = 0; i < size; i++) {
            colors.add(0);
        }

        waa = new wordArrayAdapter(this, R.layout.word_list_item, correct_path, colors);
        if(savedInstanceState != null) {
            correct_path = savedInstanceState.getStringArrayList("correct_path");
            colors = savedInstanceState.getIntegerArrayList("TextColors");
            next_word = correct_path.get(currWordIndex);
            waa.setData(correct_path, colors);
        }
        else{
            next_word = correct_path.get(1);
        }
        getNewImages(iv);
        gv = findViewById(R.id.word_list);
        gv.setAdapter(waa);
        waa.notifyDataSetChanged();

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WordlyActivity.this);
                builder.setTitle("Guess a word");

                // Set up the input
                final EditText input = new EditText(WordlyActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);


                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        String text = input.getText().toString().trim().toLowerCase(Locale.ROOT);
                        if (text.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please enter a word", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(text.charAt(text.length()-1) == ' '){ //gets rids of trailing space
                            text = text.substring(0, text.length()-1);
                        }

                        if (text.equals(correct_path.get(i)) ) {
                            // correct
                            TextView tv = (TextView) view;

                            if(currWordIndex!= correct_path.size()-2) Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();

                            tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                            colors.set(i, ContextCompat.getColor(getApplicationContext(), R.color.black));

                            waa.notifyDataSetChanged();
                            currWordIndex++;
                            next_word = correct_path.get(currWordIndex);

                            //This comes in handy. Kills the looper in the main ui thread without it crashing
                            if(handler != null){
                                handler.removeCallbacks(runnable);
                            }

                            getNewImages(iv);
                            Log.d("after incr: " + Integer.toString(currWordIndex), next_word);

                            if (currWordIndex == correct_path.size() - 1) {
                                star.setVisibility(View.VISIBLE);
                                executeStarAnimation(iv);
                            }
                        } else {
                            // incorrect
                            Toast.makeText(getApplicationContext(), "Incorrect!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        decorView = getWindow().getDecorView();

        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    hideSystemUI();
                }
            }
        });

        Button hint = findViewById(R.id.hint_button);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(next_word != null){
                    String hint = "";
                    String currWord = correct_path.get(currWordIndex -1);
                    //since the graph exists in the main activity this is a more straightforward method of obtaining hint.
                    for (int i =0; i < currWord.length(); i++){
                        if(currWord.charAt(i) != next_word.charAt(i)){
                            hint = String.valueOf(next_word.charAt(i));
                            break;

                        }
                    }
                    Toast.makeText(getApplicationContext(), hint, Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getApplicationContext(), "no hint 4 u >:3", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void executeStarAnimation(ImageView iv) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            handler.removeCallbacks(runnable);
            runOnUiThread(() -> {
                //sound found on pixabay: https://pixabay.com/sound-effects/search/yay/

                iv.setVisibility(View.GONE);
                ImageView hint_background = findViewById(R.id.hint_background);
                hint_background.setVisibility(View.GONE);
                star.setVisibility(View.VISIBLE);
                Animator animator = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.star_animator);
                animator.setTarget(star);
                animator.start();
                Toast.makeText(getApplicationContext(), "YOU WONNNNNNN!!!!", Toast.LENGTH_LONG).show();
            });
        });
    }

    /**
     * https://stackoverflow.com/questions/7597742/what-is-the-purpose-of-looper-and-how-to-use-it
     * @param iv
     * +(some chatgpt3)
     */
    private void getNewImages(ImageView iv) {
        ImageHintExecutor ihe = new ImageHintExecutor();
        ihe.execute(new ImageHintCallback() {
            @Override
            public void onComplete(ArrayList<Bitmap> images) {

                if(images != null){
                    if(images.size() > 0) {
                        Looper.prepare();
                        handler = new Handler(Looper.getMainLooper());
                        runOnUiThread(runnable = () -> {
                            iv.setImageBitmap(images.get(index));
                            index = (index + 1) % images.size();
                            handler.postDelayed(runnable, delay);
                        });
                        Looper.loop();
                        handler.postDelayed(runnable, delay);
                    }
                    else{
                        Log.d("IMAGES = 0", "IMAGES = 0");
                    }
                }
                else{
                    Log.d("IMAGES = NULL", "IMAGES = NULL");
                }

            }



        });
    }

    interface ImageHintCallback {
        void onComplete(ArrayList<Bitmap> images);
    }

    public class ImageHintExecutor {
        public void execute(ImageHintCallback callback) {
             iheThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /**
                         * All code from notes and https://pixabay.com/api/docs/
                         *
                         */


                        URL pixabay_url = new URL("https://pixabay.com/api/?key=34157164-7fb926e23417bdc9eb71940cc&q="+next_word+"&image_type=photo");
                        HttpURLConnection conn = (HttpURLConnection) pixabay_url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        Log.d("URL", pixabay_url.toString());
                        Log.d("SEARCH QUERY", next_word);

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        String inputLine = null;
                        StringBuilder sb = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            sb.append(inputLine);
                        }
                        in.close();
                        JSONObject jsonObject = new JSONObject(sb.toString());
                        Log.d("JSON", jsonObject.toString());
                        if (jsonObject.getInt("totalHits") == 0) {
                            Log.d("NO RESULTS", "NO RESULTS");
                            callback.onComplete(null);

                        }

                        JSONArray jsonArray = jsonObject.getJSONArray("hits");
                        ArrayList<Bitmap> bitmap_images = new ArrayList<>();
                        URL img_url = null;

                        int length = jsonArray.length();

                        if(length > 5) length = 5;
                        else length = jsonArray.length();

                        for (int i = 0; i < length; i++) {
                            JSONObject hit = jsonArray.getJSONObject(i);
                            String imageURL = hit.getString("webformatURL");

                            img_url = new URL(imageURL);
                            InputStream inptstrm = new BufferedInputStream(img_url.openStream());
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n = 0;
                            while (-1 != (n = inptstrm.read(buf))) {
                                out.write(buf, 0, n);
                            }

                            out.close();
                            in.close();
                            byte[] response = out.toByteArray();
                            Bitmap image = BitmapFactory.decodeByteArray(response, 0, response.length);
                            bitmap_images.add(image);
                        }

                        callback.onComplete(bitmap_images);
                    } catch (IOException e) {
                        Log.e("ImageHintExecutor", "IOException: " + e.getMessage());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Log.e("ImageHintExecutor", "JSONException: " + e.getMessage());
                        throw new RuntimeException(e);
                    }

                }
            });
            iheThread.start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void hideSystemUI() {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("correct_path", correct_path);
        savedInstanceState.putIntegerArrayList("TextColors", colors);
        savedInstanceState.putInt("index", currWordIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        correct_path = savedInstanceState.getStringArrayList("correct_path");
        colors = savedInstanceState.getIntegerArrayList("TextColors");
        currWordIndex = savedInstanceState.getInt("index");
        Log.d("CorrectPath", "Correct Path: " + correct_path.toString());
        Log.d("Colors", "Colors: " + colors.toString());
    }



}