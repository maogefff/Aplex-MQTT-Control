# Aplex-MQTT-Control
## Android端通过MQTT接入百度云控制设备的应用

* [参考代码(剑锋web端控制)](https://github.com/ZengjfOS/ComputeX/blob/master/README.md)  
* [Android的MQTT实现方法](https://blog.csdn.net/qq_17250009/article/details/52774472)


## MQTT的安卓端添加
### 方法一：
#### 1. 添加依赖
```
repositories {
    maven {
        url "https://repo.eclipse.org/content/repositories/paho-releases/"
    }
}

dependencies {
    compile 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0'
    compile 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.0'
}
```

#### 2. 添加权限
```
 <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
```

#### 3. 注册服务
```
<service android:name="org.eclipse.paho.android.service.MqttService" />
```
  
## 传输协议
### 1. 主题
* 订阅主题：·"computex/"+城市+"/iot/" + 网关id + "/DataTransfer"·
* 发布主题：·"computex/"+城市+"/iot/" + 网关id + "/backend"·

### 2. 数据格式(JSON)
```
{
    "gateway_id":"5100",        // 网关设备id
    "device_id":1,              // 连接到网关的设备id
    "funcode":2,                // 功能码
    "value":1                   // 对应功能码的值
}
```

### Button

* `funcode`: `0x01`；
* `value`: 
  * 一个bit代表一个按键；
  * 一共6 bit；
  * 低位为第一个按键；

### Led

* `funcode`: `0x02`；
* `value`:
  * 全亮: `0x01`;
  * 全灭: `0x02`;
  * 闪烁: `0x03`;
  * 流水: `0x04`;

### Num

* `funcode`: `0x03`；
* `value`:
  * 显示0: `0x00`;
  * 显示1: `0x01`;
  * 显示2: `0x02`;
  * 显示3: `0x03`;
  * 显示4: `0x04`;
  * 显示5: `0x05`;
  * 显示6: `0x06`;
  * 显示7: `0x07`;
  * 显示8: `0x08`;
  * 显示9: `0x09`;

### Temp
* `funcode`: `0x04`
* `value` : `实时温度`
