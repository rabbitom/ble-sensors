# Dialog IoT Sensor Kit

Dialog IoT Sensor Kit is a product of Dialog Semiconductor, it combines an inertial measurement sensor(acceleration & angular speed), a magnetometer and an environmental sensor(air pressure, humidity & temperature) together. Please check its [Official Webpage](http://www.dialog-semiconductor.com/iotsensor) for more information.  
There is a [3rd party open source project](https://evothings.com/dialog-iot-sensor-starter-guide/) about it, which is written in JavaScript.  
Following are some key facts about this device:

## Bluetooth GATT Description

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

## Interpretation of Sensor Values

### Accelerometer
Here's a picture showing which direction the x, y, z axes are pointing relative to the device it self.
Place the sensor kit above a desk surface, with the led side on top, then z is at the same direction of gravity (acceleration[z] = 1g).
And if you put it vertically, just like a lower case letter "d", then acceleration[x] = 1g.  
![Image not displayed](DialogIoTSensorKit-Accelerometer.jpg "x, y, z axes of the device")

### Gyroscope
The gyroscope measures how fast the device rotate.
Look at the led side, that is the same direction of axis z, now if the device rotate in clockwise direction, the rotation speed[z] will be negtive.
Unit of gyroscope value is deg/s, that is how many degrees per second.
![Image not displayed](DialogIoTSensorKit-Gyroscope.jpg "rotation speed of the device")
