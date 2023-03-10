package net.fandm.eli_lenin.wordly;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class wordArrayAdapter extends ArrayAdapter<String> {
    private ArrayList<String> words = new ArrayList<String>();

    private ArrayList<Integer> colors = new ArrayList<Integer>();
    private Context context;

    public wordArrayAdapter(Context context, int word_list_item, ArrayList<String> resource, ArrayList<Integer> text_colors) {
        super(context, R.layout.word_list_item, resource);
        this.context = context;
        this.words =resource;
        this.colors = text_colors;

    }
    public void add(int position, String word) {
        words.add(position, word);
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
        if(position == 0 || position == words.size()-1) {
            textView.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            int color = colors.get(position-1);
            textView.setTextColor(color);
        }
        textView.setText(words.get(position));
        return gridView;
    }

    public void setData(ArrayList<String> words, ArrayList<Integer> t_colors) {
        this.words = words;
        this.colors = t_colors;
    }
}
