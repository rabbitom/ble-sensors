# TI SensorTag 2015

## 概述

TI SensorTag 2015是Texus Instruments推出的传感器应用开发套件，其核心是CC2650无线连接SoC，支持BLE、6LoWPAN、Zigbee三种通信协议，同时包含一个温度传感器、一个九轴运动传感器、一个湿度传感器、一个高度和气压传感器、一个环境光传感器。

* [官方网站](http://www.ti.com/ww/en/wireless_connectivity/sensortag/index.html) - 其中[TearDown](http://www.ti.com/ww/en/wireless_connectivity/sensortag/tearDown.html)部分包含硬件组成图示
* [用户指南](http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide) - 包括完整的数据接口说明
* [Evothings: Texas Instruments](http://evothings.com/things/texasinstruments) - Evothings是一个使用JavaScript开发IoT移动应用的平台，上面有关于TI SensorTag的[文档](https://evothings.com/ti-sensortag-starter-kit/)和[示例代码](http://evothings.com/quick-guide-to-making-a-mobile-app-for-the-ti-sensortag-using-javascript/)

## 数据交互

TI SensorTag 2015的BLE数据接口按传感器组织，每种传感器对应一个服务，每个服务下包括数据、配置和间隔时间三个特征。下面以温度传感器为例说明。

### 温度

服务UUID：f000aa00-0451-4000-b000-000000000000

1. 启动和停止传感器

    写特征【f000aa02-0451-4000-b000-000000000000】，长度为1字节，0x01=启动，0x00=停止；读取此特征以获取当前启停状态。

1. 获取传感器数据

    传感器启动后，读特征【f000aa01-0451-4000-b000-000000000000】获取当前温度值；如果传感器停止，读到的值是0；打开此特征的通知，可实时接收温度更新。  
    数据长度是4个字节，前2个字节表示自身温度，后2个字节表示环境温度，无符号整数，低字节在前，将此值除以128即为摄氏温度。e.g.:  
    ```
    //[0x58, 0x0A, 0xAC, 0x0D]
    var myTemp = 0x0A58 / 128; // 20.6875˚C
    var ambientTemp = 0x0DAC / 128; // 27.34375˚C
    ```

1. 获取和设置间隔时间

    读字特征【f000aa03-0451-4000-b000-000000000000】可以获取和设置温度采集的间隔。  
    数据长度是1字节，以10ms为单位，范围从0x1E（300ms）到0xFF（2.55s），默认0x64（1秒）。

### 运动

服务UUID：f000aa80-0451-4000-b000-000000000000

1. 配置传感器

    读写特征【f000aa82-0451-4000-b000-000000000000】以获取和设置传感器的工作参数，数据长度为2字节。  
    
    第1字节：按位表示各传感器的启停，1=启动，0=停止
    
    Bit|对应传感器
    -|-
    0|陀螺仪Z轴
    1|陀螺仪Y轴
    2|陀螺仪X轴
    3|加速度计Z轴
    4|加速度计Y轴
    5|加速度计X轴
    6|磁力计
    7|运动唤醒（通过振动唤醒SensorTag）
    
    第2字节：表示加速度计的量程

    取值|量程
    -|-
    0|2G
    1|4G
    2|8G
    3|16G

1. 获取传感器数据

    读特征【f000aa81-0451-4000-b000-000000000000】以获取振动传感器数据，打开通知可接收更新。  
    数据长度为18字节，内容包括：陀螺仪X轴、Y轴、Z轴，加速度计X轴、Y轴、Z轴，磁力计X轴、Y轴、Z轴，共九组数据，每组2个字节（16位有符号整数），低字节在前。  
    对于陀螺仪，将数据除以65536，再乘以500，得到转速，取值在从-250到250之间，单位是度/秒；对于加速度计，将数据除以32768，再乘以量程，就得到相应方向的加速度，单位是G（一个重力加速度）；对于磁力计，数据无需转换，单位是uT（微特斯拉）。  
    e.g.:  
    ```
    // 配置为[0xFF, 0x02]，加速度计量程8G
    // 数据为[0x1D, 0xFC, 0xA8, 0xFF, 0xD9, 0xFF, 
    //       0x6F, 0x00, 0x71, 0x00, 0x1C, 0x10, 
    //       0x8F, 0xFD, 0x43, 0xFF, 0x8A, 0x04]
    var gyroX = (0xFC1D - 65536) / 65536 * 500; // -7.59 deg/s
    var gyroY = (0xFFA8 - 65536) / 65536 * 500; // -0.67 deg/s
    var gyroZ = (0xFFD9 - 65536) / 65536 * 500; // -0.30 deg/s
    var accX = 0x006F / 32768 * 8; // 0.03 G
    var accY = 0x0071 / 32768 * 8; // 0.03 G
    var accZ = 0x101C / 32768 * 8; // 1.00 G
    var magX = 0xFD8F - 65536; // -625 uT
    var magY = 0xFF34 - 65536; // -204 uT
    var magZ = 0x048A; // 1162 uT
    ```

1. 获取和设置间隔时间

    读写特征【f000aa83-0451-4000-b000-000000000000】获取和设置采集振动数据的间隔时间，规则与温度传感器一致，范围从100ms（0x0A）到2.55s（0xFF）。

### 湿度

服务UUID：f000aa20-0451-4000-b000-000000000000

1. 配置传感器

    读写特征【f000aa22-0451-4000-b000-000000000000】，规则同温度传感器。

1. 获取传感器数据

    读特征【f000aa21-0451-4000-b000-000000000000】获取湿度传感器数据，打开通知可接收实时数据。  
    数据长度为4字节，前2字节表示湿度传感器检测到的温度数据，16位无符号整数，低字节在前，将此值除以65536，然后乘以165，再减去40，即摄氏温度；后2字节表示相对湿度，16位无符号整数，低字节在前，先将低两位置0（与0xFFFC相与），然后除以65536，再乘以100，单位写作%RH。e.g.:  
    ```
    // [0x44, 0x69, 0x5C, 0xB2]
    var hTemp = 0x6944 / 65536 * 165 - 40; // 27.85 ˚C
    var hum = (0xB25C & 0xFFFC) / 65536 * 100; // 69.67 %
    ```

1. 获取和设置间隔时间

    读写特征【f000aa23-0451-4000-b000-000000000000】，规则与温度传感器一致，范围从100ms（0x0A）到2.55s（0xFF）。

### 气压

服务UUID：f000aa40-0451-4000-b000-000000000000

1. 配置传感器

    读写特征【f000aa42-0451-4000-b000-000000000000】，规则同温度传感器。

1. 获取传感器数据

    读特征【f000aa41-0451-4000-b000-000000000000】，可打开通知接收实时数据。  
	数据长度为6字节，前3字节是气压计测得的温度，后3字节是气压，都是无符号整数，除以100即得对应的取值，单位分别是˚C和hPa（百帕）。e.g.:  
    ```
    // [0x95, 0x0A, 0x00, 0xBC, 0x87, 0x01]
    var bTemp = 0x000A95 / 100; // 27.09 ˚C
    var pressure = 0x0187BC / 100; // 1002.84 hPa
    ```

1. 获取和设置间隔时间

    读写特征【f000aa44-0451-4000-b000-000000000000】，规则与温度传感器一致，范围从100ms（0x0A）到2.55s（0xFF）。

### 照度

服务UUID：f000aa70-0451-4000-b000-000000000000

1. 配置传感器

    读写特征【f000aa72-0451-4000-b000-000000000000】，规则同温度传感器。

1. 获取传感器数据

    读特征【f000aa71-0451-4000-b000-000000000000】，可打开通知接收实时数据。  
	数据长度为2字节，无符号整数，低字节在前。取低12位记作m，取高4位记作e，用m乘以2的e次幂再除以100，得到照度值，单位是Lux，e.g.:  
    ```
    var rawData = 0x2ABC;
    var m = rawData & 0x0FFF;
    var e = rawData >> 12;
    var l = m * Math.pow(2, e) / 100; // 109.92 Lux
    ```

1. 获取和设置间隔时间

    读写特征【f000aa73-0451-4000-b000-000000000000】，规则与温度传感器一致，范围从100ms（0x0A）到2.55s（0xFF），默认800ms（0x50）。
