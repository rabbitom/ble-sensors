package net.erabbit.blesensor;

import android.bluetooth.BluetoothDevice;

import net.erabbit.bluetooth.BleDevice;
import net.erabbit.common_lib.CoolUtility;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Tom on 16/7/20.
 */
public class DialogIoTSensor extends BleDevice {

    public enum SensorFeature {
        ACCELEROMETER(  "2ea78970-7d44-44bb-b097-26183f402401", 0, 3),
        GYROSCOPE(      "2ea78970-7d44-44bb-b097-26183f402402", 1, 3),
        MAGNETOMETER(   "2ea78970-7d44-44bb-b097-26183f402403", 2, 3),
        BAROMETER(      "2ea78970-7d44-44bb-b097-26183f402404", 3, 1),//Pressure
        HUMIDITY(       "2ea78970-7d44-44bb-b097-26183f402405", 5, 1),
        TEMPERATURE(    "2ea78970-7d44-44bb-b097-26183f402406", 4, 1),
        SFL(            "2ea78970-7d44-44bb-b097-26183f402407", 6, 3);

        private UUID uuid;
        private int keyOffset;
        private int valueCount;

        public UUID getUuid() {
            return uuid;
        }

        public int getKeyOffset() {
            return keyOffset;
        }

        SensorFeature(String uuidString, int keyOffset, int valueCount) {
            this.uuid = UUID.fromString(uuidString);
            this.keyOffset = keyOffset;
            this.valueCount = valueCount;
        }

//        public static SensorFeature findByUUID(UUID uuid) {
//            for (SensorFeature feature:
//                 SensorFeature.values()) {
//                if(uuid.equals(feature.getUuid()))
//                    return feature;
//            }
//            return null;
//        }

        private boolean valueParsed = false;

        private float[] values = new float[3];

        public float[] getValues() {
            return values;
        }

        public boolean parseValue(byte[] data, Settings settings) {
            switch(this) {
                case ACCELEROMETER://in g
//                    var ax = (evothings.util.littleEndianToInt16(data, 3) / sensitvity).toFixed(2);
//                    var ay = (evothings.util.littleEndianToInt16(data, 5) / sensitvity).toFixed(2);
//                    var az = (evothings.util.littleEndianToInt16(data, 7) / sensitvity).toFixed(2);
                    for(int i=0; i<valueCount; i++)
                        values[i] = (short) CoolUtility.toIntLE(data, 3+2*i, 2) / (float)settings.accelerometerRange.getSensitivity();
                    break;
                case GYROSCOPE://in deg/s
//                    var ax = (evothings.util.littleEndianToInt16(data, 3) / sensitvity).toFixed(2);
//                    var ay = (evothings.util.littleEndianToInt16(data, 5) / sensitvity).toFixed(2);
//                    var az = (evothings.util.littleEndianToInt16(data, 7) / sensitvity).toFixed(2);
                    for(int i=0; i<valueCount; i++)
                        values[i] = (short) CoolUtility.toIntLE(data, 3+2*i, 2) / settings.gyroScopeRange.getSensitivity();
                    break;
                case MAGNETOMETER://in micro Tesla
//                    var ax = evothings.util.littleEndianToInt16(data, 3);
//                    var ay = evothings.util.littleEndianToInt16(data, 5);
//                    var az = evothings.util.littleEndianToInt16(data, 7);
                    for(int i=0; i<valueCount; i++)
                        values[i] = (short) CoolUtility.toIntLE(data, 3+2*i, 2);
                    break;
                case BAROMETER:
                    //var pressure = (evothings.util.littleEndianToUint32(data, 3) * (1/100)).toFixed(0);
                    values[0] = CoolUtility.toIntLE(data, 3, 4);//in Pascal
                    break;
                case HUMIDITY:
                    //var humidity = (evothings.util.littleEndianToUint32(data, 3) * (1/1024)).toFixed(0);
                    values[0] = CoolUtility.toIntLE(data, 3, 4) * (1f / 1024);//in %
                    break;
                case TEMPERATURE:
                    //var temperature = (evothings.util.littleEndianToUint32(data, 3) * 0.01).toFixed(2);
                    values[0] = CoolUtility.toIntLE(data, 3, 4) * 0.01f;//in degree celsius
                    break;
                case SFL:
                    return false;
            }
            valueParsed = true;
            return true;
        }

        public String getValueString() {
            if(!valueParsed)
                return "no value";
            switch(this) {
                case ACCELEROMETER:
                    return String.format(Locale.getDefault(), "[x,y,z] = [%.2f, %.2f, %.2f] g", values[0], values[1], values[2]);
                case GYROSCOPE:
                    return String.format(Locale.getDefault(), "[x,y,z] = [%.2f, %.2f, %.2f] deg/s", values[0], values[1], values[2]);
                case MAGNETOMETER:
                    return String.format(Locale.getDefault(), "[x,y,z] = [%d, %d, %d] uT", (int)values[0], (int)values[1], (int)values[2]);
                case BAROMETER:
                    return String.format(Locale.getDefault(), "%d Pa", (int)values[0]);
                case HUMIDITY:
                    return String.format(Locale.getDefault(), "%d %%", (int)values[0]);
                case TEMPERATURE:
                    return String.format(Locale.getDefault(), "%.2f °C", values[0]);
                case SFL:
                    return "not parsed";
            }
            return null;
        }

        protected boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }
    }

    protected static final UUID UUID_INFO = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402408"); // Read Device Features

    public DialogIoTSensor(BluetoothDevice device) {
        super(device);
        //服务和特性UUID
        UUID_MAIN_SERVICE = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402400");
        UUID_MAIN_CONFIG = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402409");//CONTROL_POINT
        UUID_MAIN_DATA = UUID.fromString("2ea78970-7d44-44bb-b097-26183f40240A");//CONTROL_REPLY
    }

    @Override
    public void onConnect() {
//        startReceiveData();//start from activity
        readInfo();
    }

    private void readInfo() {
        ReadCharacteristic(btGatt, btService, UUID_INFO);
    }

    private enum ControlCommand {
        ReadSettings(11), SensorOn(1), SensorOff(0);
        private byte id;
        public byte getId() {
            return id;
        }
        public static ControlCommand findById(byte commandId) {
            for(ControlCommand command : ControlCommand.values())
                if(command.getId() == commandId)
                    return command;
            return null;
        }
        ControlCommand(int commandId) {
            this.id = (byte)commandId;
        }
    }

    private void readSettings() {
        sendData(new byte[]{ControlCommand.ReadSettings.getId(), 0});
    }

    @Override
    protected void onReceiveData(byte[] data) {
        byte commandId = data[1];
        ControlCommand command = ControlCommand.findById(commandId);
        if(command != null)
            switch(command) {
                case ReadSettings:
                    settings.parse(data, 2);
                    break;
                case SensorOn:
                    sensorOn = true;
                    onValueChange(VALUE_OF_SENSOR_SWITCH, 1);
                    break;
                case SensorOff:
                    sensorOn = false;
                    onValueChange(VALUE_OF_SENSOR_SWITCH, 0);
                    break;
            }
    }

    private enum AccelerometerRange {
        _2G(3, 2), _4G(5, 4), _8G(8, 8), _16G(12, 16);
        private int key;
        private int value;
        public int getSensitivity() {
            return 32768 / value;
        }
        public static AccelerometerRange findByKey(int key) {
            for(AccelerometerRange range : AccelerometerRange.values())
                if(range.key == key)
                    return range;
            return null;
        }
        AccelerometerRange(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private enum GyroscopeRange {
        _2000(0, 2000), _1000(1, 1000), _500(2, 500), _250(3, 250), _125(4, 125);
        private int key;
        private int value;
        public float getSensitivity() {
            return 32800f / value;
        }
        public static GyroscopeRange findByKey(int key) {
            for (GyroscopeRange range: GyroscopeRange.values()) {
                if(range.key == key)
                    return range;
            }
            return null;
        }
        GyroscopeRange(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

//    instance.configuration.BASIC = {
//                SENSOR_COMBINATION: 		instance.enums.SENSOR_COMBINATION._all,
//                ACCELEROMETER_RANGE: 		instance.enums.ACCELEROMETER_RANGE._2,
//                ACCELEROMETER_RATE: 		instance.enums.ACCELEROMETER_RATE._100,
//                GYROSCOPE_RANGE: 			instance.enums.GYROSCOPE_RANGE._2000,
//                GYROSCOPE_RATE: 			instance.enums.GYROSCOPE_RATE._100,
//                MAGNETOMETER_RATE: 			instance.enums.MAGNETOMETER_RATE._0,
//                ENVIRONMENTAL_SENSORS_RATE: instance.enums.ENVIRONMENTAL_SENSORS_RATE._2,
//                SENSOR_FUSION_RATE: 		instance.enums.SENSOR_FUSION_RATE._10,
//                SENSOR_FUSION_RAW_DATA_ENABLE: 	instance.enums.SENSOR_FUSION_RAW_DATA_ENABLE._enabled,
//                CALIBRATION_MODE: 			instance.enums.CALIBRATION_MODE._static,
//                AUTO_CALIBRATION_MODE: 		instance.enums.AUTO_CALIBRATION_MODE._basic,
//    }
    private class Settings {
        private AccelerometerRange accelerometerRange = AccelerometerRange._2G;
        private GyroscopeRange gyroScopeRange = GyroscopeRange._2000;
        public void parse(byte[] data, int offset) {
            accelerometerRange = AccelerometerRange.findByKey(data[offset+1]);
            gyroScopeRange = GyroscopeRange.findByKey(data[offset+3]);
        }
    }

    private Settings settings = new Settings();

    private void addFeature(SensorFeature feature) {
        if(!features.contains(feature))
            features.add(feature);
    }

    @Override
    protected void onReceiveData(UUID uuid, byte[] data) {
        super.onReceiveData(uuid, data);
        if(uuid.equals(UUID_INFO)) {
            for (SensorFeature feature:
                    SensorFeature.values()) {
                if(data[feature.getKeyOffset()] == 1) {
                    addFeature(feature);
                    if(feature == SensorFeature.SFL) {
                        addFeature(SensorFeature.ACCELEROMETER);
                        addFeature(SensorFeature.GYROSCOPE);
                        addFeature(SensorFeature.MAGNETOMETER);
                    }
                }
            }
            if(data.length > 7)
                firmwareVersion = new String(data, 7, data.length-7);
            super.onConnect();
        }
        else {
            //SensorFeature feature = SensorFeature.findByUUID(uuid);
            for(SensorFeature feature : features) {
                if(uuid.equals(feature.getUuid())) {
                    if(feature.parseValue(data, settings))
                        onValueChange(VALUE_OF_SENSOR_FEATURE, features.indexOf(feature));
                    break;
                }
            }
        }
    }

    public static final int VALUE_OF_SENSOR_SWITCH = 1;
    public static final int VALUE_OF_SENSOR_FEATURE = 2;

    protected ArrayList<SensorFeature> features = new ArrayList<>();

//    public SensorFeature[] getAvailableFeatures() {
//        SensorFeature[] availableFeatures = new SensorFeature[features.size()];
//        return features.toArray(availableFeatures);
//    }

    public int getFeatureCount() {
        return features.size();
    }

    public SensorFeature getFeature(int index) {
        return features.get(index);
    }

    private static final String FIRMWARE_VERSION_UNKNOWN = "unknown";
    private String firmwareVersion = FIRMWARE_VERSION_UNKNOWN;

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    private boolean sensorOn = false;

    public void switchSensorFeature(SensorFeature sensorFeature, boolean onOff) {
        if(onOff) {
            if(!sensorOn)
                switchSensor(true);
            EnableNotification(btGatt, btService, sensorFeature.getUuid());
        }
        else
            DisableNotification(btGatt, btService, sensorFeature.getUuid());
        sensorFeature.enabled = onOff;
    }

    public void switchSensor(boolean onOff) {
        sendData(new byte[]{onOff ? ControlCommand.SensorOn.getId() : ControlCommand.SensorOff.getId()});
    }
}
