package tfsapps.watertimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private boolean auto_restart = false;
    private int timer_count = 15;
    private int interval_time = 5;
    private boolean notice_light = false;
    private boolean notice_alarm = true;
    private boolean notice_vibration = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //  メニュー
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //  メニュー選択時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent1 = new Intent(this, CrSetActivity.class);
            startActivity(intent1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.v("LifeCycle", "------------------------------>onStart");

        //DBのロード
        /* データベース */
        //helper = new MyOpenHelper(this);
        //AppDBInitRoad();

        //  設定関連
        String temp = "";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        auto_restart = sharedPreferences.getBoolean("auto_retry", false);
        temp = sharedPreferences.getString("timer_count", "15");
        timer_count = Integer.parseInt(temp);
        temp = sharedPreferences.getString("interval_time", "5");
        interval_time = Integer.parseInt(temp);
        notice_light = sharedPreferences.getBoolean("notice_light", false);
        notice_alarm = sharedPreferences.getBoolean("notice_alarm", true);
        notice_vibration = sharedPreferences.getBoolean("notice_vibration", false);

        /*
        //  アラーム種類が変更？
        if (bgm_name != bgm_str) {
            if (bgm.isPlaying() == true) {
                bgm.stop();
                bgm = null;
                startflag = 0;
            }
            bgm_name = bgm_str;
            bgm = MediaPlayer.create(this, R.raw.alarm);
        }

        //  アラーム自動スタート制御
        if (auto_alarm_flag == false) {
            if (bgm.isPlaying() == false) {
                btnStartDisp();
                startflag = 0;
            }
        } else {
            if (bgm.isPlaying() == false) {
                bgm.setLooping(true);
                bgm.start();
            }
            startflag = 1;
            stopcount = 0;
            btnStopDisp();
        }
        //  アラーム停止制御
        if (alarm_stop_flag == false) stopmax = 1;
        else stopmax = 10;
        */
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v("LifeCycle", "------------------------------>onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("LifeCycle", "------------------------------>onPause");
        //  DB更新
//        AppDBUpdated();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.v("LifeCycle", "------------------------------>onRestart");
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.v("LifeCycle", "------------------------------>onStop");
        //  DB更新
//        AppDBUpdated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("LifeCycle", "------------------------------>onDestroy");

        //  DB更新
/*        AppDBUpdated();

        if (bgm.isPlaying() == true) {
            bgm.stop();
            bgm = null;
        }

 */
    }
}