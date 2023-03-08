package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class WordlyActivity extends AppCompatActivity {

    /**
     * ImageView slideshow delay found on chatgpt3
     */
    private Handler handler;
    public Runnable runnable;

    private final int delay = 2000;
    public int index =0;
    public String next_word; //change when algo built

    public View decorView;

    ImageView star;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wordly);
        ImageHintExecutor ihe = new ImageHintExecutor();
        ImageView iv = (ImageView) findViewById(R.id.hint_image);
        iv.setImageResource(R.drawable.wordly);
        ArrayList<String> correct_path =  getIntent().getStringArrayListExtra("path");
        TextView tv1 = (TextView) findViewById(R.id.textView1);
        TextView tv4 = (TextView) findViewById(R.id.textView4);
        tv1.setText(correct_path.get(0));
        tv4.setText(correct_path.get(correct_path.size()-1));
        next_word = correct_path.get(1);
        Toast.makeText(getApplicationContext(), next_word, Toast.LENGTH_LONG).show();

        star = findViewById(R.id.gold_star);
        star.setVisibility(View.GONE);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animator animator = AnimatorInflater.loadAnimator(getApplicationContext(), R.animator.star_animator);
                animator.setTarget(star);
                animator.start();
            }

        });


        TextView tv2 = (TextView) findViewById(R.id.textView2);

        //tv2.setTextColor();

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WordlyActivity.this);
                builder.setTitle("Enter Text");

                // Set up the input
                final EditText input = new EditText(WordlyActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();
                        // Do something with the text
                        tv2.setText(text);
                        //tv2.setTextColor();
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

        ihe.execute(new ImageHintCallback() {
            @Override
            public void onComplete(ArrayList<Bitmap> images) {
                Looper.prepare();
                handler = new Handler(Looper.getMainLooper());

                runOnUiThread(runnable= () -> {
                    iv.setImageBitmap(images.get(index));
                    index = (index+1)%images.size();
                    handler.postDelayed(runnable, delay);

                });
                Looper.loop();
                handler.postDelayed(runnable, delay);
            }
        });
        Button hint = findViewById(R.id.hint_button);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Hint!", Toast.LENGTH_LONG).show();

            }
        });
    }
    interface ImageHintCallback {
        void onComplete(ArrayList<Bitmap> images);
    }
    public class ImageHintExecutor {
        public void execute(ImageHintCallback callback) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /**
                         * Autofilling with github copilot
                         * but I am just using the get cat facts activity as a template
                         * copilot does not seem to be much help other than filling in the rest of a line
                         * most of this is
                         */
                        // Build the URL object
                        //found this using a combination of this video: https://www.youtube.com/watch?v=1Q9QZ4Y6zqU

                        URL pixabay_url = new URL("https://pixabay.com/api/?key=34157164-7fb926e23417bdc9eb71940cc&q="+next_word+"&image_type=photo&pretty=true");
                        HttpURLConnection conn = (HttpURLConnection) pixabay_url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();


                        int responsecode = conn.getResponseCode();
                        if (responsecode == 301) {
                            //handles redirects
                            String newUrl = conn.getHeaderField("Location");

                            pixabay_url = new URL(newUrl);
                            conn = (HttpURLConnection) pixabay_url.openConnection();

                            conn.setRequestMethod("GET");
                            conn.connect();
                        }

                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        Log.d("BufferedReader", in.toString());
                        String inputLine = null;
                        StringBuilder sb = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            sb.append(inputLine);
                            Log.d("Sb line", inputLine);
                        }
                        in.close();
                        JSONObject jsonObject = new JSONObject(sb.toString());
                        Log.d("JSON", jsonObject.toString());

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
            thread.start();
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
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSystemUI();
        return super.onTouchEvent(event);
    }

    private void hideSystemUI() {
        decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }


}