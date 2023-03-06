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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

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
                        //found this using a combination of this video: https://www.youtube.com/watch?v=1Q9QZ4Y6zqU
                        //and this stackoverflow post: https://stackoverflow.com/questions/40587168/simple-android-volley-example
                        //and ChatGPT3

                        String url = (builder.build().toString());
                        URL pixabay_url = new URL(url);
                        HttpURLConnection conn = (HttpURLConnection) pixabay_url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        Log.d("Response code", String.valueOf(conn.getResponseCode()));

                        int responsecode = conn.getResponseCode();
                        if(responsecode == 301){
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
                        String inputLine;
                        StringBuilder sb = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            sb.append(inputLine);
                            Log.d("Sb line", inputLine);
                        }
                        in.close();
                        JSONObject jsonObject = new JSONObject(sb.toString());
                        Log.d("JSON", jsonObject.toString());

                        JSONArray jsonArray = jsonObject.getJSONArray("hits");
                        JSONObject hit = jsonArray.getJSONObject(0);
                        String imageURL = hit.getString("webformatURL");
                        URL img_url = new URL(imageURL);


                        //RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        ArrayList<String> imageUrls = new ArrayList<>();

                        /**
                         * USING VOLLEY (301)

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONArray jsonArray = jsonObject.getJSONArray("hits");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject hit = jsonArray.getJSONObject(i);
                                        String imageURL = hit.getString("webformatURL");
                                        imageUrls.add(imageURL);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }, Throwable::printStackTrace);

                        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
                        */


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