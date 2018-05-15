package com.aplex.aplexmqttcontrol;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by aplex on 2018/5/15.
 */

public class Model implements Icontract.IbaseModel {
    @Override
    public void mqttSubscribeTopic(String topic) {

    }

    @Override
    public void mqttUnsubscribeTopic(String topic) {

    }

    @Override
    public void mqttPublish(String topic, MqttMessage message) {

    }
}
