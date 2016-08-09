package codeeden.com.musicplayer.adaptor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import codeeden.com.musicplayer.R;

/**
 * Created by Hrishikesh on 7/25/2016.
 */
public class SongsListAdaptor extends BaseAdapter {

    private Context context;
    private Activity activity;
    private ArrayList<String> songsList = new ArrayList<String>();

    public SongsListAdaptor(Context context, Activity activity, ArrayList<String> songsList) {
        this.context = context;
        this.activity = activity;
        this.songsList = songsList;
    }

    @Override
    public int getCount() {
        return songsList.size();
    }

    @Override
    public String getItem(int i) {
        return songsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = activity.getLayoutInflater();
        row = inflater.inflate(R.layout.songs_list, parent, false);

        ((TextView) row.findViewById(R.id.songsName)).setText(songsList.get(i).split("@@@@@")[2].toString().toUpperCase());
        ((TextView) row.findViewById(R.id.songsPath)).setText(songsList.get(i).split("@@@@@")[1].toString());
        System.out.println("MP  ::: IN ADAPTOR ====>"+songsList.get(i).split("@@@@@")[2]);

        return row;
    }
}
