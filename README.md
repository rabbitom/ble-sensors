# BLESensor-Android
This is a demo app for connecting and collecting data from BLE enabled sensors.

## Dialog IoT Sensor Kit
Currently, we only support [Dialog IoT Sensor Kit](http://www.dialog-semiconductor.com/iotsensor). Please refer to class DialogIoTSensor in package net.erabbit.blesensor to find the code about it. And check [this Evothings project](https://evothings.com/dialog-iot-sensor-starter-guide/) in Javascript to find more.
 
Here are some key facts of this device:

Service UUID: 2ea78970-7d44-44bb-b097-26183f402400

### Read Device Information
Characteristic UUID:  2ea78970-7d44-44bb-b097-26183f402408  
Read this characteristic, then get what sensor is available and version of firmware from the response data:
- Bytes 0~6: Sensor Availablity (1: available, 0: unavailable)  
Byte0 - Accelerometer  
Byte1 - Gyroscope  
Byte2 - Magnetometer  
Byte3 - Barometer  
Byte4 - Temperature  
Byte5 - Humidity  
Byte6 - SFL(Sensor Fusion, if this is available, then Accelerometer, Gyroscope and Magetometer are all available, despite of the values of Byte0~2)
- Bytes 7~end: Firmware Version (ASCII String)

### Write Control Command
Characteristic UUID:  2ea78970-7d44-44bb-b097-26183f402409  
Just write a single byte:
- 0: Sensor Off
- 1: Sensor On
- 11: Read Settings

### Receive Control Command Reply(Notify)
Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f40240A  
In received data, Byte1 is command id, from Byte2 to end are command parameters  
When Byte1 = 11(Read Settings):  
- Byte3: Accelerometer Range  
3 - 2g  
5 - 4g  
8 - 8g  
12 - 16g  
- Byte5: Gyroscope Range  
0 - 2000  
1 - 1000  
2 - 500  
3 - 250  
4 - 125  

### Sensor Values(Notify)
To receive sensor value, be sure the sensor is on -  write control command 1 to turn all sensors on,  then enable notification of each sesnor charactersitic.  
In received notification data, value numbers start at Byte3, each number may contain 2 or 4 bytes(little endian), and the sensor value may contain multiple numbers, which is called "dimension" below:
#### ACCELEROMETER
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402401
- Bytes Length: 2
- Dimension: 3
- Unit: g
#### GYROSCOPE
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402402
- Bytes Length: 2
- Dimension: 3
- Unit:  deg/s
#### MAGNETOMETER
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402403
- Bytes Length: 2
- Dimension: 3
- Unit: uT
#### BAROMETER
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402404
- Bytes length: 4
- Dimension: 1
- Unit: Pa
#### HUMIDITY
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402405
- Bytes length: 4
- Dimension: 1
- Unit: %
#### TEMPERATURE
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402406
- Bytes length: 4
- Dimension: 1
- Unit: °C
#### SFL (Sensor Fusion)
- Characteristic UUID: 2ea78970-7d44-44bb-b097-26183f402407
- Bytes length: 2
- Dimension: 4

