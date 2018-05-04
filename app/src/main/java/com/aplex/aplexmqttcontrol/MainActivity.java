package com.aplex.aplexmqttcontrol;

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

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";

    String serverURI = "baidumap.mqtt.iot.gz.baidubce.com";    //url
    Integer port = 1883;                                //端口
    String clientId;                            //客户端ID
    String userName = "baidumap/iotmap";            //用户名
    char[] password = "bjBb+EUd5rwfo9fBaZUMlwG8psde+abMx35m/euTUfE=".toCharArray();  //秘钥

    Spinner deviceOption;
    Spinner led;
    Spinner digitalTube;

    ImageView light[];
    ImageView statImage;
    TextView statText;
    TextView temp;

    MqttConnectOptions options = null;
    MqttClient client = null;

    String ledValue;
    String digitalTubeValue;
    String gatewayValue;
    String topic;

    Button testButtonID;
    Button testConnID;
    int status = 0;
    Byte tmp;

    void test(){
        testButtonID = findViewById(R.id.testButtonID);
        testConnID = findViewById(R.id.testConnID);

        testConnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "断开连接");
                    client.disconnect();
                    statImage.setImageResource(R.mipmap.led_gray);
                    statText.setText("未连接");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        testButtonID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                Log.d(TAG, "22222222222 status="+status);
                switch (status){
                    case 0:
                        tmp = 1;
                        break;
                    case 1:
                        tmp = (1<<1);
                        break;
                    case 2:
                        tmp = (1<<2);
                        break;
                    case 3:
                        tmp = (1<<3);
                        break;
                    case 4:
                        tmp = (1<<4);
                        break;
                    case 5:
                        tmp = (1<<5);
                        break;
                    case 6:
                        tmp = 0x7f;
                        break;
                }
                status= (++status)%7;
                try {
                    jsonObject.put("gateway_id", gatewayValue);
                    jsonObject.put("device_id", String.valueOf(1));
                    jsonObject.put("funcode", String.valueOf(1));
                    jsonObject.put("value", String.valueOf(tmp));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(jsonObject.toString().getBytes());
                    client.publish(topic, message);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initMqtt();
        setOnClickListener();

        test();
    }

    private void initView(){
        light = new ImageView[10];

        deviceOption  = (Spinner)findViewById(R.id.devOptID);
        led = (Spinner)findViewById(R.id.ledID);
        digitalTube = (Spinner)findViewById(R.id.digitalTubeID);

        statImage = (ImageView)findViewById(R.id.statusImage);
        statText = (TextView)findViewById(R.id.statusText);
        temp = (TextView)findViewById(R.id.tempID);
        light[0] = (ImageView)findViewById(R.id.light1_ID);
        light[1] = (ImageView)findViewById(R.id.light2_ID);
        light[2] = (ImageView)findViewById(R.id.light3_ID);
        light[3] = (ImageView)findViewById(R.id.light4_ID);
        light[4] = (ImageView)findViewById(R.id.light5_ID);
        light[5] = (ImageView)findViewById(R.id.light6_ID);
    }

    private void setOnClickListener(){

        deviceOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject jsonObject = new JSONObject();
                String currentTopic;
                String[] gateway = getResources().getStringArray(R.array.deviceOptionValue);
                gatewayValue = gateway[i];

                if(!client.isConnected()){
                    Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
                    initMqtt();
                    return;
                }

                if(!gatewayValue.equals("0")){
                    currentTopic = "computex/iot/" + gatewayValue + "/DataTransfer";
                    if(topic != null){
                        unsubscribeTopic(topic);
                    }
                    topic = currentTopic;
                    subscribeTopic(topic);
                    publishLed();
                    publishDigitalTube();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0; i<6; i++){
                                light[i].setImageResource(R.mipmap.led_gray);
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        led.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] value = getResources().getStringArray(R.array.ledStatusValue);
                ledValue = value[i];
                if(!gatewayValue.equals("0")) {
                    publishLed();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        digitalTube.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] res = getResources().getStringArray(R.array.digitalTubeID);
                digitalTubeValue = res[i];
                if(!gatewayValue.equals("0")) {
                    publishDigitalTube();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void publishLed(){
        if(!client.isConnected()){
            Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("gateway_id", gatewayValue);
            jsonObject.put("device_id", String.valueOf(1));
            jsonObject.put("funcode", String.valueOf(2));
            jsonObject.put("value", ledValue);
            Log.d(TAG, "发送：gateway_id="+gatewayValue+"; device_id="+1+"; funcode="+2+"; value="+ledValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private  void publishDigitalTube(){
        JSONObject jsonObject = new JSONObject();
        if(!client.isConnected()){
            Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
            return;
        }
        //发送数码管
        try {
            jsonObject.put("gateway_id", gatewayValue);
            jsonObject.put("device_id", String.valueOf(1));
            jsonObject.put("funcode", String.valueOf(3));
            jsonObject.put("value", digitalTubeValue);
            Log.d(TAG, "发送：gateway_id="+gatewayValue+"; device_id="+1+"; funcode="+3+"; value="+digitalTubeValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void subscribeTopic(String topic){
        if(!client.isConnected()){
            Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            client.subscribe(topic);
            Log.d(TAG, "subscribeTopic: "+topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribeTopic(String topic){
        if(!client.isConnected()){
            Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Log.d(TAG, "unsubscribeTopic: "+topic);
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void initMqtt(){
        //实例化MQTT连接对象
        options = new MqttConnectOptions();
        //清缓存
        options.setCleanSession(true);
        options.setUserName(userName);
        options.setPassword(password);
        options.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        options.setKeepAliveInterval(20);

        MemoryPersistence memPer = new MemoryPersistence();
        //实例化MQTT客户端对象
        Random  random=new Random();
        clientId = "DeviceID-Android-"+String.valueOf(random.nextInt(999999));
        try {
            client = new MqttClient("tcp://"+serverURI+":"+String.valueOf(port), clientId, memPer);
            //回调函数
            client.setCallback(mqttCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "连接中...");
                    client.connect(options);
                    Log.d(TAG, "连接成功...");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        statImage.setImageResource(R.mipmap.led_blue);
                        statText.setText("已连接");
                    }
                });
            }
        }).start();
    }

    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String msg = new String(message.getPayload());
            JSONObject jsonPayload = new JSONObject(msg);
            String gateway = jsonPayload.getString("gateway_id");
            String device = jsonPayload.getString("device_id");
            String funcode = jsonPayload.getString("funcode");
            final String value = jsonPayload.getString("value");

            Log.d(TAG, "接收：gateway_id="+gateway+"; device_id="+device+"; funcode="+funcode+"; value="+value);
            //按键状态
            if(Integer.valueOf(funcode)==1){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0; i<6; i++){
                            if((Integer.valueOf(value)>>i & 0x01) == 0x01){
                                light[i].setImageResource(R.mipmap.led_green);
                            }else{
                                light[i].setImageResource(R.mipmap.led_gray);
                            }
                        }
                    }
                });
            }else if(Integer.valueOf(funcode)==4){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        temp.setText(value+"℃");
                    }
                });
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.d(TAG, "deliveryComplete");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.d(TAG, "Mqtt出事连接，重连");
            Toast.makeText(MainActivity.this, "已断开，请重新连接", Toast.LENGTH_SHORT).show();
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            initMqtt();
        }
    };
}
