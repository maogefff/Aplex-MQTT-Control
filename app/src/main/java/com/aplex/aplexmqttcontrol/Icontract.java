package com.aplex.aplexmqttcontrol;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by aplex on 2018/5/15.
 */

public interface Icontract {
    interface IbaseModel{
//        String readNodeID();
        void mqttSubscribeTopic(String topic);
        void mqttUnsubscribeTopic(String topic);
        void mqttPublish(String topic, MqttMessage message);
    }
    interface IbaseView{
        void ledShow(int status, int ledIndex);
        void digitalTubeShow(int num);
        void netConnStatusShow(boolean isConn);
        void buttonStatusShow(int bitValue);
        void tempShow(String temp);
    }
    interface IbasePresenter{
        void initPresenter(IbaseView view, Context context);
        void initMqtt();
        void viewStop();
        void viewStart();
        void mqttPublish();        //发布
        void mqttSubscribeTopic();    //订阅接收主题
        void mqttUnsubscribeTopic();  //取消订阅
        void publishLed();
        void publishDigitalTube();

        int getLedValue();
        void setLedValue(int value);
        void setGatewayValue(String value);
        String getGatewayValue();
        void setDigitalTubeValue(int value);
        int getDigitalTubeValue();
        void setCityValue(String value);
        String getCityValue();
//        void mqtt
    }
}
