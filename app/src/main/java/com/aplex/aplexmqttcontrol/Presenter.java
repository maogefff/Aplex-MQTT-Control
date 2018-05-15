package com.aplex.aplexmqttcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

/**
 * Created by aplex on 2018/5/15.
 */

public class Presenter implements Icontract.IbasePresenter {

    private String TAG = "Presenter";

    Icontract.IbaseView view;
    Icontract.IbaseModel model;
    Context context;

    String serverURI = "baidumap.mqtt.iot.gz.baidubce.com";    //url
    Integer port = 1883;                                //端口
    String clientId;                            //客户端ID
    String userName = "baidumap/iotmap";            //用户名
    char[] password = "bjBb+EUd5rwfo9fBaZUMlwG8psde+abMx35m/euTUfE=".toCharArray();  //秘钥

    MqttConnectOptions options = null;
    MqttClient client = null;
    MemoryPersistence memPer = null;

    int ledValue;
    int digitalTubeValue;
    String gatewayValue;    //
    String cityValue;
    String subscribeTopic;    //
    String publishTopic;

    IsConnThread isConnThread = null;
    @Override
    public void initPresenter(Icontract.IbaseView view, Context context) {
        this.view = view;
        this.context = context;
        model = new Model();
    }

    @Override
    public void initMqtt() {
        //实例化MQTT连接对象
        options = new MqttConnectOptions();
        //清缓存
        options.setCleanSession(true);
        options.setUserName(userName);
        options.setPassword(password);
        options.setConnectionTimeout(5);
        options.setKeepAliveInterval(10);

        memPer = new MemoryPersistence();
        //实例化MQTT客户端对象
        Random random=new Random();
        clientId = "DeviceID-Android-"+String.valueOf(random.nextInt(999999));
        Log.d(TAG, "clientID："+clientId);
        try {
            client = new MqttClient("tcp://"+serverURI+":"+String.valueOf(port), clientId, memPer);
            client.setCallback(mqttCallback);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,"连接...");
                    client.connect(options);
                    Log.d(TAG,"连接成功...");
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(client.isConnected()){

                            view.netConnStatusShow(true);
                        }
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
                        view.buttonStatusShow(Integer.valueOf(value));
                    }
                });
            }else if(Integer.valueOf(funcode)==4){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
//                        temp.setText(value+"℃");
                        view.tempShow(value);
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
            isConnThread = new IsConnThread();
            isConnThread.start();
        }
    };

    class IsConnThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (!Thread.currentThread().isInterrupted() && !client.isConnected()){

                disCommHandler();
                try {
                    Thread.sleep(10000);
                    Log.d(TAG, "Threadid="+Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    public void viewStop() {
        if(isConnThread!=null){
            isConnThread.interrupt();
            isConnThread = null;
        }
    }

    @Override
    public void viewStart() {
        SharedPreferences sp = context.getSharedPreferences("mydata", 0);
        publishTopic = sp.getString("publishTopic", null);
        subscribeTopic = sp.getString("subscribeTopic", null);
    }

    private void disCommHandler(){
        if(client==null){
            Log.d(TAG, "init mqtt");
//            initMqtt();
        }else {
            Log.d(TAG, "reconnect mqtt");

            new Thread(new Runnable() {
                @Override
                public void run() {

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            view.netConnStatusShow(false);
                        }
                    });

                    try {
                        //可能阻塞
                        Log.d(TAG,"连接...");
                        client.connect(options);
                        Log.d(TAG,"连接成功...");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(client.isConnected()){
                                view.netConnStatusShow(true);
                            }
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void mqttPublish() {
        if(!client.isConnected()){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("gateway_id", gatewayValue);
            jsonObject.put("device_id", 1);
            jsonObject.put("funcode", 2);
            jsonObject.put("value", ledValue);
            Log.d(TAG, "发送：gateway_id="+gatewayValue+"; device_id="+1+"; funcode="+2+"; value="+ledValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            Log.d(TAG, "publishTopic="+publishTopic);
            client.publish(publishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mqttSubscribeTopic() {

        if(!client.isConnected()){
            return;
        }
        subscribeTopic = "computex/" + cityValue + "/iot/" + gatewayValue + "/DataTransfer";
        publishTopic = "computex/"+cityValue+"/iot/" + gatewayValue + "/backend";

        SharedPreferences sp = context.getSharedPreferences("mydata", 0);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("publishTopic", publishTopic);
        ed.putString("subscribeTopic", subscribeTopic);
        ed.apply();

        try {
            Log.d(TAG, "subscribeTopic: "+subscribeTopic);
            client.subscribe(subscribeTopic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publishLed(){
        if(!client.isConnected()){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("gateway_id", gatewayValue);
            jsonObject.put("device_id", 1);
            jsonObject.put("funcode", 2);
            jsonObject.put("value", ledValue);
            Log.d(TAG, "发送：gateway_id="+gatewayValue+"; device_id="+1+"; funcode="+2+"; value="+ledValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            Log.d(TAG, "publishTopic="+publishTopic);
            client.publish(publishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publishDigitalTube(){
        JSONObject jsonObject = new JSONObject();
        if(!client.isConnected()){
            return;
        }

        try {
            jsonObject.put("gateway_id", gatewayValue);
            jsonObject.put("device_id", 1);
            jsonObject.put("funcode", 3);
            jsonObject.put("value", digitalTubeValue);
            Log.d(TAG, "发送：gateway_id="+gatewayValue+"; device_id="+1+"; funcode="+3+"; value="+digitalTubeValue);

            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            Log.d(TAG, "publishTopic="+publishTopic);
            client.publish(publishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void mqttUnsubscribeTopic(){
        if(!client.isConnected()){
            return;
        }
        if(subscribeTopic != null){
            try {
                Log.d(TAG, "unsubscribeTopic: "+subscribeTopic);
                client.unsubscribe(subscribeTopic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getLedValue() {
        return ledValue;
    }

    @Override
    public void setLedValue(int value) {
        ledValue = value;
    }

    @Override
    public void setGatewayValue(String value) {
        gatewayValue = value;
    }

    @Override
    public String getGatewayValue() {
        return gatewayValue;
    }

    @Override
    public void setDigitalTubeValue(int value) {
        digitalTubeValue = value;
    }

    @Override
    public int getDigitalTubeValue() {
        return digitalTubeValue;
    }

    @Override
    public void setCityValue(String value) {
        cityValue = value;
    }

    @Override
    public String getCityValue() {
        return cityValue;
    }

}
