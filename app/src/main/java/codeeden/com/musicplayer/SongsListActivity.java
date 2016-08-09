package codeeden.com.musicplayer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import codeeden.com.musicplayer.adaptor.SongsListAdaptor;

public class SongsListActivity extends AppCompatActivity {

    private ListView songList;
    private ProgressDialog pd;
    private SongsListAdaptor songsListAdaptor;
    private ArrayList<String> mainPlayList;
    private ArrayList<String> tmpList = new ArrayList<>();
    private EditText searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                init();

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            }
        }else{

            init();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();

                } else {
                    Toast.makeText(getApplicationContext(),"Sorry Your Permission is denied",Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }

    private void init(){
        songList = (ListView) findViewById(R.id.songList);
        searchItem = (EditText) findViewById(R.id.searchItem);

        pd = new ProgressDialog(this);
        pd.setMessage("loading.....");
        pd.setCanceledOnTouchOutside(false);

        searchItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tmpList = new ArrayList<String>();
                for(int j=0;j<mainPlayList.size();j++){
                    if(mainPlayList.get(j).split("@@@@@")[2].toString().toUpperCase().startsWith(charSequence.toString().toUpperCase())){
                        tmpList.add(mainPlayList.get(j));
                    }else{
                        System.out.println("MP  ::: search else");
                    }
                }

                songsListAdaptor = new SongsListAdaptor(getApplicationContext(),SongsListActivity.this,tmpList);
                songList.setAdapter(songsListAdaptor);
                songsListAdaptor.notifyDataSetChanged();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });



        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("MP  ::: PLAYER ====>"+mainPlayList.size());
                Intent in = new Intent(SongsListActivity.this,MainActivity.class);
                if(tmpList.size()>0){
                    in.putExtra("SONG_ID",tmpList.get(i).split("@@@@@")[0].toString());
                    in.putExtra("FORCED","1");
                    startActivity(in);

                }else{
                    in.putExtra("SONG_ID",mainPlayList.get(i).split("@@@@@")[0].toString());
                    in.putExtra("FORCED","1");
                    startActivity(in);

                }
            }
        });

        pd.show();
        new MediaListFetch().execute();
    }




    class MediaListFetch extends AsyncTask {

        /* Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
         File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
         File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);*/
        final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";
        private ArrayList<String> songsList = new ArrayList<String>();
        private String mp3Pattern = ".mp3";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Object[] objects) {
            getPlayList();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            for(int i=0;i<songsList.size();i++){

                System.out.println("MP  ::: songsList ====> "+songsList.get(i));
            }
            songsListAdaptor = new SongsListAdaptor(getApplicationContext(),SongsListActivity.this,songsList);
            songList.setAdapter(songsListAdaptor);
            songsListAdaptor.notifyDataSetChanged();
            mainPlayList = songsList;



            pd.hide();
        }

        /**
         * Function to read all mp3 files and store the details in
         * ArrayList
         * */

        public void getPlayList(){
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION
            };
            ContentResolver musicResolver = SongsListActivity.this.getContentResolver();
            Cursor cursor = musicResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    null);



            int cont = 0;

            while(cursor.moveToNext()){
                songsList.add(cursor.getString(0) + "@@@@@" + cursor.getString(1) + "@@@@@" +   cursor.getString(2) + "@@@@@" +   cursor.getString(3) + "@@@@@" +  cursor.getString(4) + "@@@@@" +  cursor.getString(5));
                System.out.println("MP  ::: Song====>"+songsList.get(cont));
                cont++;
            }
        }

        /*public void getPlayList(String path) {
            System.out.println(path);
            if (path != null) {
                File home = new File(path);
                File[] listFiles = home.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File file : listFiles) {
                        System.out.println(file.getAbsolutePath());
                        if (file.isDirectory()) {
                            scanDirectory(file);
                        } else {
                            addSongToList(file);
                        }
                    }
                }
            }

        }

        private void scanDirectory(File directory) {
            if (directory != null) {
                File[] listFiles = directory.listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File file : listFiles) {
                        if (file.isDirectory()) {
                            scanDirectory(file);
                        } else {
                            addSongToList(file);
                        }

                    }
                }
            }
        }*/

        /*private void addSongToList(File song) {
            if (song.getName().endsWith(mp3Pattern)) {
                HashMap<String, String> songMap = new HashMap<String, String>();
                songMap.put("songTitle",
                        song.getName().substring(0, (song.getName().length() - 4)));
                songMap.put("songPath", song.getPath());

                // Adding each song to SongList
                songsList.add(songMap);
            }
        }*/


    }

}
