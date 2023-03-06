package net.fandm.eli_lenin.wordly;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WordlyActivity extends AppCompatActivity {

    final private String API_KEY = "34157164-7fb926e23417bdc9eb71940cc";
    private String next_word = "corn"; //change when algo built

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordly);
        Button hint = findViewById(R.id.hint_button);
        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Hint!", Toast.LENGTH_LONG).show();
                ImageHintExecutor ihe = new ImageHintExecutor();
                ihe.execute(new ImageHintCallback() {
                    @Override
                    public void onComplete(Bitmap image) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView iv = (ImageView) findViewById(R.id.hint_image);
                                iv.setImageBitmap(image);
                            }
                        });
                    }
                });


            }
        });
    }
    interface ImageHintCallback {
        void onComplete(Bitmap image);
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
                         */
                        // Set up the base URL for the Pixabay API
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("https")
                                .authority("pixabay.com")
                                .appendPath("api");

                        // Add query parameters to the URL
                        builder.appendQueryParameter("key", API_KEY);
                        builder.appendQueryParameter("q", next_word);
                        builder.appendQueryParameter("image_type", "photo");



                        // Build the URL object

                        URL url = new URL(builder.build().toString());
                        Log.d("URL", url.toString());
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        Log.d("Connection", urlConnection.toString());
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        /**
                         * This is the part that is not working
                         * I am getting a 301 error
                         * I am not sure how to fix it

                        String redirect = urlConnection.getHeaderField("Location");
                        if (redirect != null){
                            urlConnection = (HttpURLConnection) new URL(redirect).openConnection();
                        }
                        */

                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        Log.d("BufferedReader", "BufferdReader initialized");
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        urlConnection.disconnect();
                        Log.d("BufferedReader", sb.toString());
                        JSONObject json = new JSONObject(sb.toString());
                        Log.d("JSON", json.toString());
                        String imageURL = json.getJSONArray("hits").getJSONObject(0).getString("webformatURL");
                        Log.d("Image URL", imageURL);
                        url = new URL(imageURL);
                        InputStream in = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int n = 0;
                        while (-1 != (n = in.read(buf))) {
                            out.write(buf, 0, n);
                        }

                        byte[] response = out.toByteArray();
                        Bitmap image = BitmapFactory.decodeByteArray(response, 0, response.length);
                        callback.onComplete(image);
                        out.close();
                        in.close();

                        } catch (IOException e) {
                        e.printStackTrace();
                        } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
            thread.start();
        }
    }

}