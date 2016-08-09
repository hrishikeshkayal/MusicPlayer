package codeeden.com.musicplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.ArrayList;

import codeeden.com.musicplayer.MainActivity;
import codeeden.com.musicplayer.R;

/**
 * Created by Hrishikesh on 7/27/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final IBinder mBinder = new MusicBinder();
    private ArrayList<String> songs;
    private String songId = "";
    private int trackId = 0;
    MediaPlayer mPlayer;
    private int length = 0;
    private NotificationManager nManager;

    public MusicService() {
    }

    public void setList(ArrayList<String> theSongs){
        songs=theSongs;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        System.out.println("MP  ::: trackId====>"+trackId);
        trackId++;
        if(trackId<songs.size() && trackId>=0){
            startMusic(songs.get(trackId).split("@@@@@")[0]);
        }else{
            trackId = 0;
        }
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);

        if (mPlayer != null) {
            mPlayer.setLooping(false);
            mPlayer.setVolume(100, 100);
        }


        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int
                    extra) {

                onError(mPlayer, what, extra);
                return true;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //mPlayer.start();
        return START_STICKY;
    }

    public void startMusic(String songID) {
        try {

            System.out.println("MP  ::: STARTED PROPERLY.....");

            songId = songID;
            if(mPlayer==null){
                mPlayer = new MediaPlayer();
            }

            if(!mPlayer.isPlaying()){
                mPlayer.stop();
            }

            String path ="";
            String name = "";
            String artName = "";
            for(int j=0;j<songs.size();j++){
                if(songs.get(j).split("@@@@@")[0].equalsIgnoreCase(songId)){
                    path = songs.get(j).split("@@@@@")[3];
                    name = songs.get(j).split("@@@@@")[2];
                    artName = songs.get(j).split("@@@@@")[1];
                    trackId = j;
                }
            }

            mPlayer.reset();
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            mPlayer.start();


            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.music_icon)
                            .setContentTitle("Now playing "+name)
                            .setContentText("Music by "+artName);
            int NOTIFICATION_ID = 12345;

            Intent targetIntent = new Intent(this, MainActivity.class);
            targetIntent.putExtra("SONG_ID",songId);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
            builder.setAutoCancel(true);
            nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nManager.notify(NOTIFICATION_ID, builder.build());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            length = mPlayer.getCurrentPosition();

        }
    }

    public void resumeMusic() {
        if (mPlayer.isPlaying() == false) {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }

    public void stopMusic() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;

        System.out.println("MP  ::: ENDED PROPERLY.....");
    }

    public void seekToCurrentPosition(int position){
        if(mPlayer != null){
            if(mPlayer.isPlaying()){
                int totalTime = mPlayer.getDuration()/1000;
                mPlayer.seekTo((totalTime*position)/100 *1000);
            }
        }
    }

    public int nextPlay(){
        trackId++;
        if(trackId<songs.size() && trackId>=0){
            startMusic(songs.get(trackId).split("@@@@@")[0]);
        }else{
            trackId = 0;
            startMusic(songs.get(trackId).split("@@@@@")[0]);
        }

        return trackId;
    }

    public int prevPlay(){
        trackId--;
        if(trackId<songs.size() && trackId>=0){
            startMusic(songs.get(trackId).split("@@@@@")[0]);
        }else{
            trackId = (songs.size()-1);
            startMusic(songs.get(trackId).split("@@@@@")[0]);
        }

        return trackId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }

        nManager.cancelAll();

        System.out.println("MP  ::: DESTROY");
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {

        Toast.makeText(this, "music player failed", Toast.LENGTH_SHORT).show();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        return false;
    }

    public int getTrackId(){
        return trackId;
    }

    public String showCurrentPosition(){
        if(mPlayer!=null)
        if(mPlayer.isPlaying()){
            int totalLength = mPlayer.getDuration()/1000;
            System.out.println("MP ::: totalLength ==== >"+totalLength);
            int currentPosition = mPlayer.getCurrentPosition()/1000;
            System.out.println("MP ::: currentPosition ==== >"+currentPosition);
            int seekBarPosition = ((currentPosition*100)/totalLength);
            System.out.println("MP ::: seekbarPosition ==== >"+seekBarPosition);

            String time = "";
            int currentLength = mPlayer.getCurrentPosition();
            int mintC = currentLength/60000;
            int secC = currentLength/1000 - (mintC*60);
            if(secC<10)
                time = mintC+":0"+secC;
            else
                time = mintC+":"+secC;




            return seekBarPosition+"---"+time;
        }
        return "0---0:00";
    }

    public String totalDuration(){
        String time = "0:00";
        if(mPlayer!=null)
        if(mPlayer.isPlaying()){
            int totalLength = mPlayer.getDuration();
            int mint = totalLength/60000;
            int sec = totalLength/1000 - (mint*60);
            if(sec<10)
                time = mint+":0"+sec;
            else
                time = mint+":"+sec;

            return time;


        }

        return "0:00";
    }

    public boolean isPlayingNow(){
        if(mPlayer!=null){
            return mPlayer.isPlaying();

        }else{
            return false;
        }
    }

}