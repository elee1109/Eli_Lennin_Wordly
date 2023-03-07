package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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


public class WordlyActivity extends AppCompatActivity {

    /**
     * ImageView slideshow delay found on chatgpt3
     */
    private Handler handler;
    public Runnable runnable;

    private final int delay = 2000;
    public int index =0;
    public String next_word; //change when algo built

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordly);
        ImageHintExecutor ihe = new ImageHintExecutor();
        ImageView iv = (ImageView) findViewById(R.id.hint_image);
        iv.setImageResource(R.drawable.wordly);
        next_word =  getIntent().getStringArrayListExtra("path").get(0);
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
                        Log.d("Response code", String.valueOf(conn.getResponseCode()));

                        int responsecode = conn.getResponseCode();
                        if (responsecode == 301) {
                            //handles redirects
                            String newUrl = conn.getHeaderField("Location");
                            Log.d("New URL", newUrl);
                            pixabay_url = new URL(newUrl);
                            conn = (HttpURLConnection) pixabay_url.openConnection();
                            Log.d("Connection", "Reconnected");
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
                            Log.d("Image URL", imageURL);
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

}