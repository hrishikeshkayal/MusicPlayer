package codeeden.com.musicplayer;

import android.Manifest;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;

import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;

import codeeden.com.musicplayer.service.MusicService;


public class MainActivity extends AppCompatActivity {

    private ImageButton play,stop,back,nextPlay,prevPlay,loop;
    private SeekBar seekbar;
    private TextView startingTime,endingTime,songfullname;
    private Handler hdl = new Handler();
    private Runnable playerCallback;
    private String path = "";
    private String name = "";
    private String SONG_ID = "";
    private String FORCED = "0";
    private boolean flag = false;
    private boolean stoped = false;
    private boolean mIsBound = false;
    private ArrayList<String> mainPlayList = new ArrayList<String>();
    private MusicService mServ;
    private ServiceConnection musicConnection;
    private Intent playIntent;

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                musicConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(musicConnection);
            mIsBound = false;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent in = getIntent();

        SONG_ID = in.getStringExtra("SONG_ID");
        FORCED = in.getStringExtra("FORCED");

        getPlayList();

        for(int j=0;j<mainPlayList.size();j++){
            if(mainPlayList.get(j).split("@@@@@")[0].equalsIgnoreCase(SONG_ID)){
                path = mainPlayList.get(j).split("@@@@@")[3];
                name = mainPlayList.get(j).split("@@@@@")[2];
            }
        }




        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {


            } else {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

            }
        }


        loop = (ImageButton) findViewById(R.id.loop);
        play = (ImageButton) findViewById(R.id.play);
        stop = (ImageButton) findViewById(R.id.stop);
        back = (ImageButton) findViewById(R.id.back);
        nextPlay = (ImageButton) findViewById(R.id.nextPlay);
        prevPlay = (ImageButton) findViewById(R.id.prevPlay);

        seekbar = (SeekBar) findViewById(R.id.seekbar);
        startingTime = (TextView) findViewById(R.id.startingTime);
        endingTime = (TextView) findViewById(R.id.endingTime);
        songfullname = (TextView) findViewById(R.id.songfullname);

        songfullname.setText(name);

        loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int position = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                position = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mServ.seekToCurrentPosition(position);
            }
        });


        playerCallback =new Runnable() {
            @Override
            public void run() {
                String currentPosition[] = mServ.showCurrentPosition().split("---");
                String duration = mServ.totalDuration();
                int track = mServ.getTrackId();


                startingTime.setText(currentPosition[1]);
                seekbar.setProgress(Integer.parseInt(currentPosition[0]));
                endingTime.setText(duration);

                name = mainPlayList.get(track).split("@@@@@")[2];
                songfullname.setText(name);

                hdl.postDelayed(this,500);

            }
        };

        nextPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int track = mServ.nextPlay();
                songfullname.setText(mainPlayList.get(track).split("@@@@@")[2]);
                SONG_ID = mainPlayList.get(track).split("@@@@@")[0];
                play.setImageResource(R.drawable.pause_icon);
            }
        });

        prevPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int track = mServ.prevPlay();
                songfullname.setText(mainPlayList.get(track).split("@@@@@")[2]);
                SONG_ID = mainPlayList.get(track).split("@@@@@")[0];
                play.setImageResource(R.drawable.pause_icon);
            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!stoped){

                    if(!flag){
                        flag = true;
                        mServ.pauseMusic();
                        play.setImageResource(R.drawable.play_icon);
                        System.out.println("MP  ::: PAUSE CLICKED");

                        hdl.removeCallbacks(playerCallback);


                    }else{
                        flag = false;
                        mServ.resumeMusic();
                        play.setImageResource(R.drawable.pause_icon);
                        System.out.println("MP  ::: PLAY CLICKED");

                        hdl.removeCallbacks(playerCallback);
                        hdl.postDelayed(playerCallback,500);
                    }
                }else{
                    stoped = false;
                    mServ.startMusic(SONG_ID);
                    play.setImageResource(R.drawable.pause_icon);
                    System.out.println("MP  ::: PLAY DEFAULT CLICKED");

                    hdl.removeCallbacks(playerCallback);
                    hdl.postDelayed(playerCallback,500);
                }


            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hdl.removeCallbacks(playerCallback);
                play.setImageResource(R.drawable.play_icon);
                mServ.stopMusic();
                stoped = true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hdl.removeCallbacks(playerCallback);
                finish();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        hdl.removeCallbacks(playerCallback);
        hdl.postDelayed(playerCallback,500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hdl.removeCallbacks(playerCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Toast.makeText(getApplicationContext(),"Sorry Your Permission is denied",Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }



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
        ContentResolver musicResolver = MainActivity.this.getContentResolver();
        Cursor cursor = musicResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);



        int cont = 0;

        while(cursor.moveToNext()){
            mainPlayList.add(cursor.getString(0) + "@@@@@" + cursor.getString(1) + "@@@@@" +   cursor.getString(2) + "@@@@@" +   cursor.getString(3) + "@@@@@" +  cursor.getString(4) + "@@@@@" +  cursor.getString(5));
            System.out.println("MP  ::: Song====>"+mainPlayList.get(cont));
            cont++;
        }

        musicConnection =new ServiceConnection(){

            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                //get service
                mServ = binder.getService();
                mServ.setList(mainPlayList);
                mIsBound = true;

                if(!mServ.isPlayingNow())
                    mServ.startMusic(SONG_ID);

                if(FORCED!=null)
                    if(FORCED.equalsIgnoreCase("1"))
                        mServ.startMusic(SONG_ID);


                play.setImageResource(R.drawable.pause_icon);
                hdl.postDelayed(playerCallback,500);
            }

            public void onServiceDisconnected(ComponentName name) {
                mServ = null;
            }
        };

        playIntent = new Intent(this, MusicService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);


    }





}
