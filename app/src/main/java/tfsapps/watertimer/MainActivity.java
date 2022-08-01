package tfsapps.watertimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
//アラーム関連
import android.media.MediaPlayer;
//ライト関連
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;


public class MainActivity extends AppCompatActivity {

    //設定DB関連
    private boolean auto_restart = false;
    private int timer_count = 15;
    private int notice_time = 1;
    private int interval_time = 5;
    private boolean notice_light = false;
    private boolean notice_alarm = true;
    private boolean notice_vibration = false;

    //ステータス
    final int MODE_NORMAL = 0;
    final int MODE_INTERVAL = 1;
    private int ActiveMode = 0;
    private boolean isActive = false;
    private boolean isPause = false;

    //  国設定
    private Locale _local;
    private String _language;
    private String _country;


    //タイマー値
    final long SEC_1 = 1000;
    final long MIN_1 = 60000;
    final long MAX_45 = 45 * 60000;
    final long INTERVAL = 10;
    private CountDown countDown;
    private NoticeCountDown NoticeCountDown;
    private TextView timerText;
    private SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss.SS", Locale.US);
//    private SimpleDateFormat dataFormat = new SimpleDateFormat("mm:ss", Locale.US);

    private long now_countNumber = 0;           //現在のタイマー値（カウントダウン中の値）
    private long countNumber = 0;
    private long org_countNumber = 0;
    private long countSecNumber = 0;

    //デバイス関係
    //バイブレーション
    static private Vibrator vibrator;
    //アラーム
    private MediaPlayer alarm;
    //ライト関連
    private CameraManager mCameraManager;
    private String mCameraId = null;
    static boolean islightON = false;
    private AudioManager am;

    private int now_volume;                     //現在の音量値
    private int init_volume;                    //アプリ起動時の音量値
    private SeekBar seek_volume;                //SeekBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  国設定
        _local = Locale.getDefault();
        _language = _local.getLanguage();
        _country = _local.getCountry();

        /* 初期化処理 */
        timerText = findViewById(R.id.timer);
        timerText.setText(dataFormat.format(0));

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        alarm = MediaPlayer.create(this, R.raw.alarm);

        //カメラ初期化
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                mCameraId = cameraId;
            }
        }, new android.os.Handler());

        /*音*/
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        now_volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        /* SeekBar */
        seek_volume = (SeekBar)findViewById(R.id.seekBar);
        seek_volume.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    //ツマミをドラッグした時
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (notice_alarm == true) {
                            now_volume = seekBar.getProgress();
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, now_volume, 0);
                        }
                        screenDisplay();
                    }
                    //ツマミに触れた時
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        if (notice_alarm == false) {
//                            seekBar.setProgress(now_volume);
                        }
                    }
                    //ツマミを離した時
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (notice_alarm == false) {
//                            seekBar.setProgress(now_volume);
                        }
                    }
                }
        );

        screenDisplay();
    }

    /**********************************************************************
        表示処理
     *********************************************************************/
    /* ステータスの表示（コップ画面） */
    public void screenDisplayStatus(){
        ImageView img_cup = (ImageView) findViewById(R.id.image_status);
        TextView txt_cup = (TextView) findViewById(R.id.text_status);

        String mess_break = getString(R.string.intervaling);
        int temp = 0;

        if (ActiveMode == MODE_INTERVAL){
            txt_cup.setText(mess_break);
//            txt_cup.setTextColor(getResources().getColor(R.color.my_green));
//            img_cup.setImageResource(R.drawable.per000);
            return;
        }

        if ((isActive == false && now_countNumber == 0) || (isActive == false && isPause == false)) {
            txt_cup.setText("100%");
//            txt_cup.setTextColor(getResources().getColor(R.color.my_blue));
            img_cup.setImageResource(R.drawable.per100);
        }
        else {
            temp = (int)((now_countNumber * 100) / org_countNumber);
//            temp = (now_countNumber / countNumber) * 100;
            txt_cup.setText("" + temp + "%");
//            txt_cup.setTextColor(getResources().getColor(R.color.my_blue)   );

            if (temp > 75){
                img_cup.setImageResource(R.drawable.per100);
            }
            else if (temp > 50){
                img_cup.setImageResource(R.drawable.per075);
            }
            else if (temp > 25){
                img_cup.setImageResource(R.drawable.per050);
            }
            else if (temp > 0){
                img_cup.setImageResource(R.drawable.per025);
            }
            else{
                img_cup.setImageResource(R.drawable.per000);
            }
        }

    }
    /* ボタンなどの画面パーツの表示 */
    public void screenDisplay(){
        Button btn_start = (Button) findViewById(R.id.btn_start);
        Button btn_clear = (Button) findViewById(R.id.btn_clear);
        String mess = "";

        seek_volume.setProgress(now_volume);

        screenDisplayStatus();

        if (isActive == true) {
            if (isPause == false)   {    mess += "STOP";    }
            else                    {    mess += "START";   }
            btn_start.setText(mess);

            btn_start.setBackgroundTintList(null);
            btn_start.setTextColor(getColor(R.color.material_on_background_disabled));
            btn_start.setBackgroundResource(R.drawable.btn_round2);

            btn_clear.setBackgroundTintList(null);
            btn_clear.setTextColor(getColor(R.color.design_default_color_error));
            btn_clear.setBackgroundResource(R.drawable.btn_round2);
        }
        else{
            timerText.setText(dataFormat.format(countNumber));

            btn_start.setBackgroundTintList(null);
            mess += "START";
            btn_start.setText(mess);
            btn_start.setTextColor(getColor(R.color.design_default_color_primary_variant));
            btn_start.setBackgroundResource(R.drawable.btn_round2);

            btn_clear.setBackgroundTintList(null);
            btn_clear.setTextColor(getColor(R.color.design_default_color_error));
            btn_clear.setBackgroundResource(R.drawable.btn_round2);
        }
    }

    /**********************************************************************
        ボタン処理
     *********************************************************************/
    public void timerStartExec(){

        if (isActive == false ) {
//test_make
//countNumber = 5000;

            if (countNumber > 0) {
                if (countDown != null){
                    countDown.cancel();
                    countDown = null;
                }
                // タイマー関連（インスタンス生成）
                org_countNumber = countNumber;
                countDown = new CountDown(countNumber, INTERVAL);
                countDown.start();
                isActive = true;
                isPause = false;
            }
            screenDisplay();
        }
        else{
            /* タイマー途中 */
            if (now_countNumber > 0){
                if (isPause == true) {
                    if (countDown != null){
                        countDown.cancel();
                        countDown = null;
                    }
                    countNumber = now_countNumber;
                    countDown = new CountDown(countNumber, INTERVAL);
                    countDown.start();
                    isActive = true;
                    isPause = false;
                }
                else{
                    if (countDown != null){
                        countDown.cancel();
                    }
                    isPause = true;
                    DeviceOff();
                }
                screenDisplay();
            }
            else{
                DeviceOff();
            }
        }
    }
    /* ボタン：開始 */
    public void onStart(View v) throws InterruptedException {
        timerStartExec();
    }
    /* ボタン：クリア */
    public void onClear(View v){
        if (isActive == true){
            DeviceOff();
        }
        timer_Setting(timer_count);

        if (countDown != null){
            countDown.cancel();
        }
        isActive = false;
        isPause = false;
        ActiveMode = MODE_NORMAL;
        timerText.setText(dataFormat.format(0));
        screenDisplay();
    }
    /* 自動スタート処理 */
    public void AutoStart(){

        /* 自動スタートSWが「無効：FALSE」の時 */
        if (auto_restart == false){
            ActiveMode = MODE_NORMAL;
            return;
        }

        isActive = false;
        isPause = false;

        // モードの切り替え
        switch (ActiveMode){
            case MODE_INTERVAL: ActiveMode = MODE_NORMAL;
                                timer_Setting(timer_count);
                                break;
            default:            ActiveMode = MODE_INTERVAL;
                                timer_Setting(interval_time);
                                break;
        }
        screenDisplay();
        timerStartExec();

        timer_Sec_Setting(notice_time);
        // 通知用タイマー
        if (NoticeCountDown != null){
            NoticeCountDown.cancel();
        }
        NoticeCountDown = new NoticeCountDown(countSecNumber, INTERVAL);
        NoticeCountDown.start();

    }


    public void timer_Setting(int temp_time){
        countNumber = (temp_time * MIN_1);
        if (countNumber > MAX_45){
            countNumber = MAX_45;
        }
    }
    public void timer_Sec_Setting(int temp_time){
        countSecNumber = (temp_time * SEC_1);
        if (countSecNumber > MIN_1){    //SEC_1 x 60
            countSecNumber = MIN_1;
        }
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
        temp = sharedPreferences.getString("notice_time", "1");
        notice_time = Integer.parseInt(temp);
        temp = sharedPreferences.getString("interval_time", "5");
        interval_time = Integer.parseInt(temp);
        notice_light = sharedPreferences.getBoolean("notice_light", false);
        notice_alarm = sharedPreferences.getBoolean("notice_alarm", true);
        notice_vibration = sharedPreferences.getBoolean("notice_vibration", false);

        timer_Setting(timer_count);
        screenDisplay();
    }

    /**********************************************************************
        デバイス関連（アラーム、バイブ、ライト）
     *********************************************************************/
    //ライトの制御
    public void lightControl(boolean isOn)
    {
        boolean _flag = false;
        //カメラ正常認識できていない場合
        if(mCameraId == null){
            return;
        }
        if (isOn == false){
            _flag = false;
        }else{
            _flag = true;
        }
        try {
            mCameraManager.setTorchMode(mCameraId, _flag);
        } catch (CameraAccessException e) {
            //エラー処理
            e.printStackTrace();
            return;
        }
        islightON = _flag;
    }
    //ON処理　ライト・バイブレーション・音
    public void DeviceOn()
    {
        //アラーム
        if (notice_alarm == true) {
            alarm.setLooping(true);
            alarm.start();
        }
        //バイブレーション
        if (notice_vibration) {
            long vibratePattern[] = {300, 1000, 300, 1000};/* OFF→ON→OFF→ON*/
            vibrator.vibrate(vibratePattern, 1);
        }
        //ライト
        if (notice_light == true) {
            lightControl(true);
        }
    }
    //OFF処理　ライト・バイブレーション・音
    public void DeviceOff()
    {
        //アラーム
        alarm.pause();
        //バイブレーション
        vibrator.cancel();
        //ライト
        lightControl(false);
    }

    /**********************************************************************
         タイマー処理
     *********************************************************************/
    /*
        カウントダウン処理
     */
    class CountDown extends CountDownTimer {
        private long nowcount;

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            nowcount = millisInFuture;
        }
        @Override
        public void onFinish() {
            // 完了
            //テキスト

            timerText.setText(dataFormat.format(0));
            now_countNumber = 0;

            if (ActiveMode == MODE_NORMAL)  DeviceOn();
            else                            DeviceOff();

            AutoStart();
        }
        public void onPause1(){
            onPause();
        }
        public void onResume1(){
            onResume1();
        }
        public void onRestart(){
            onRestart();
        }
        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            now_countNumber = millisUntilFinished;
            // 残り時間を分、秒、ミリ秒に分割
            long mm = millisUntilFinished / 1000 / 60;
            long ss = millisUntilFinished / 1000 % 60;
            long ms = (millisUntilFinished - ss * 1000 - mm * 1000 * 60)/10;
//            timerText.setText(String.format("%1$02d:%2$02d.%2$02d", mm, ss, ms));
            timerText.setText(String.format("%2d:%2$02d.%2$02d", mm, ss, ms));
//            timerText.setText(String.format("%1$02d:%2$02d", mm, ss));
            timerText.setText(dataFormat.format(millisUntilFinished));
//            if (ActiveMode == MODE_NORMAL)  timerText.setTextColor(getResources().getColor(R.color.my_blue));
//            else                            timerText.setTextColor(getResources().getColor(R.color.my_green));
            screenDisplayStatus();
        }
    }

    class NoticeCountDown extends CountDownTimer {
        private long nowcount;

        NoticeCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            nowcount = millisInFuture;
        }
        @Override
        public void onFinish() {
            DeviceOff();
        }
        /*
        public void onPause1(){
            onPause();
        }
        public void onResume1(){
            onResume1();
        }
        public void onRestart(){
            onRestart();
        }
        */
        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
        }
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
*/
        //アラーム
        if (alarm != null) {
            if (alarm.isPlaying() == true) {
                alarm.stop();
                alarm = null;
            }
        }
        //カメラ
        if (mCameraManager != null)
        {
            mCameraManager = null;
        }

    }
}