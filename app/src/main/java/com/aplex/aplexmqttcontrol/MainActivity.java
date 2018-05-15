package com.aplex.aplexmqttcontrol;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements Icontract.IbaseView{
    private String TAG = "MainActivity";

    Spinner deviceOption;
    Spinner led;
    Spinner digitalTube;
    Spinner city;

    ImageView light[];
    ImageView ledImage[];
    ImageView statImage;
    TextView statText;
    TextView temp;

    Button subscribe;
    TextView digital;
    TextView digitalbackground;

    //数码管相关
    private Typeface typeface;
    // 设置一个常量，这里就是我们的数码管字体文件
    private static final String FONT_DIGITAL_7 = "fonts" + File.separator + "digital-7.ttf";

    Presenter presenter = new Presenter();

    Thread ledThread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        presenter.initPresenter(this, this);
        initView();
        presenter.initMqtt();
        setOnClickListener();
    }

    private void initView(){
        light = new ImageView[10];
        ledImage = new ImageView[8];
        deviceOption  = (Spinner)findViewById(R.id.devOptID);
        led = (Spinner)findViewById(R.id.ledID);
        city = (Spinner)findViewById(R.id.cityOptID);
        digitalTube = (Spinner)findViewById(R.id.digitalTubeID);

        subscribe = (Button) findViewById(R.id.subscribeID);
        digital = (TextView) findViewById(R.id.digitalID);
        digitalbackground = (TextView) findViewById(R.id.digitalbackgroundID);

        statImage = (ImageView)findViewById(R.id.statusImage);
        statText = (TextView)findViewById(R.id.statusText);
        temp = (TextView)findViewById(R.id.tempID);
        light[0] = (ImageView)findViewById(R.id.light1_ID);
        light[1] = (ImageView)findViewById(R.id.light2_ID);
        light[2] = (ImageView)findViewById(R.id.light3_ID);
        light[3] = (ImageView)findViewById(R.id.light4_ID);
        light[4] = (ImageView)findViewById(R.id.light5_ID);
        light[5] = (ImageView)findViewById(R.id.light6_ID);

        ledImage[0] = (ImageView)findViewById(R.id.led1_ID);
        ledImage[1] = (ImageView)findViewById(R.id.led2_ID);
        ledImage[2] = (ImageView)findViewById(R.id.led3_ID);
        ledImage[3] = (ImageView)findViewById(R.id.led4_ID);
        ledImage[4] = (ImageView)findViewById(R.id.led5_ID);
        ledImage[5] = (ImageView)findViewById(R.id.led6_ID);
        ledImage[6] = (ImageView)findViewById(R.id.led7_ID);
        ledImage[7] = (ImageView)findViewById(R.id.led8_ID);

        typeface = Typeface.createFromAsset(getAssets(), FONT_DIGITAL_7);
        // 设置字体
        digital.setTypeface(typeface);
        digitalbackground.setTypeface(typeface);
        digital.setText("888888");
        digitalbackground.setText("888888");
    }

    private void setOnClickListener(){

        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = subscribe.getText().toString();
                if(status.equals("Subscribe")){

                    status="Unsubscribe";
                    //订阅主题
                    presenter.mqttSubscribeTopic();
                    //发送led
                    presenter.publishLed();
                    //发送数码管
                    presenter.publishDigitalTube();
                    subscribe.setText(status);

                }else{
                    status = "Subscribe";
                    subscribe.setText(status);
                    presenter.mqttUnsubscribeTopic();
                }
            }
        });

        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] city = getResources().getStringArray(R.array.cityOptionValue);
                presenter.setCityValue(city[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        deviceOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] gateway = getResources().getStringArray(R.array.deviceOptionValue);
                presenter.setGatewayValue(gateway[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        led.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] value = getResources().getStringArray(R.array.ledStatusValue);
                presenter.setLedValue(Integer.valueOf(value[i]));
                Log.d(TAG, "led...");
                if(!presenter.getGatewayValue().equals("0")) {
                    presenter.mqttPublish();
                    ledShow(Integer.valueOf(value[i]), Integer.valueOf(value[i]));
                }
                ledShow(Integer.valueOf(value[i]), Integer.valueOf(value[i]));//测试，要去掉
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        digitalTube.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] res = getResources().getStringArray(R.array.digitalTubeID);
                presenter.setDigitalTubeValue(Integer.valueOf(res[i]));
                if(!presenter.getGatewayValue().equals("0")) {
                    presenter.publishDigitalTube();
                    digitalTubeShow(Integer.valueOf(res[i]));
                }
                digitalTubeShow(Integer.valueOf(res[i]));   //测试，到时候去掉
                //digital.setText(String.valueOf(digitalTubeShow(Integer.valueOf(res[i]))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        presenter.viewStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        presenter.viewStop();
    }

    int cntTmp = 0;
    @Override
    public void ledShow(final int status, int ledIndex) {

        if(ledThread != null){
            ledThread.interrupt();
            ledThread = null;
        }

        ledThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int tmp = 0;
                while (!Thread.currentThread().isInterrupted()) {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            switch (status){
                                case 1:
                                    for(int i=0; i<8; i++){
                                        ledImage[i].setImageResource(R.mipmap.led_green);
                                    }
                                    break;
                                case 2:
                                    for(int i=0; i<8; i++){
                                        ledImage[i].setImageResource(R.mipmap.led_gray);
                                    }
                                    break;
                                case 3:
                                    for(int i=0; i<8; i++){
                                        if(cntTmp%2==0){
                                            ledImage[i].setImageResource(R.mipmap.led_gray);
                                        }else {
                                            ledImage[i].setImageResource(R.mipmap.led_green);
                                        }
                                    }
                                    break;
                                case 4:
                                    for(int i=0; i<8; i++){
                                        if(cntTmp==i){
                                            ledImage[i].setImageResource(R.mipmap.led_green);
                                        }else{
                                            ledImage[i].setImageResource(R.mipmap.led_gray);
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    });

                    cntTmp = ++cntTmp%8;

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        cntTmp = 0;
                        break;
                    }
                }

            }
        });
        ledThread.start();
    }

    @Override
    public void digitalTubeShow(int num) {
        if(num > 9){
            return;
        }
//        String Num = String.valueOf(num);
        String Num  = String.format("%d%d%d%d%d%d%d%d", num,num,num,num,num,num,num,num);
        Log.d(TAG, "改变后为:"+Num);
        digital.setText(Num);
    }

    @Override
    public void netConnStatusShow(boolean isConn) {
        if(isConn){
            // 网络已连接上
            statImage.setImageResource(R.mipmap.led_blue);
            statText.setText("已连接");
            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
        }else{
            statImage.setImageResource(R.mipmap.led_gray);
            statText.setText("未连接");
        }
    }

    @Override
    public void buttonStatusShow(int bitValue) {
        for(int i=0; i<6; i++){
            if((bitValue>>i & 0x01) == 0x01){
                light[i].setImageResource(R.mipmap.led_green);
            }else{
                light[i].setImageResource(R.mipmap.led_gray);
            }
        }
    }

    @Override
    public void tempShow(String tempo) {
        temp.setText(tempo+"℃");
    }
}
