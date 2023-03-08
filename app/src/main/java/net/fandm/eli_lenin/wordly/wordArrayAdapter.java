package net.fandm.eli_lenin.wordly;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class wordArrayAdapter extends ArrayAdapter<String> {
    private ArrayList<String> words = new ArrayList<String>();
    private Context context;

    public wordArrayAdapter(Context context, int word_list_item, ArrayList<String> resource) {
        super(context, R.layout.word_list_item, resource);
        this.context = context;
        this.words =resource;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;
        if (convertView == null) {
            gridView = inflater.inflate(R.layout.word_list_item, null);
        } else {
            gridView = convertView;
        }

        TextView textView = gridView.findViewById(R.id.word_textview);
        textView.setText(words.get(position));


        return gridView;
    }
}
