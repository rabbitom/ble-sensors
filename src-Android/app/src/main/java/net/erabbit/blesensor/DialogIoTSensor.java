package net.erabbit.blesensor;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import net.erabbit.bluetooth.BleDevice;
import net.erabbit.common_lib.CoolUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Tom on 16/7/20.
 */
public class DialogIoTSensor extends BleDevice {

    public enum SensorFeature {
        ACCELEROMETER(  0, "2ea78970-7d44-44bb-b097-26183f402401", 3, "g", 2),
        GYROSCOPE(      1, "2ea78970-7d44-44bb-b097-26183f402402", 3, "deg/s", 2),
        MAGNETOMETER(   2, "2ea78970-7d44-44bb-b097-26183f402403", 3, "uT", 0),
        BAROMETER(      3, "2ea78970-7d44-44bb-b097-26183f402404", 1, "Pa", 0),//Pressure
        HUMIDITY(       5, "2ea78970-7d44-44bb-b097-26183f402405", 1, "%", 0),
        TEMPERATURE(    4, "2ea78970-7d44-44bb-b097-26183f402406", 1, "°C", 2),
        SFL(            6, "2ea78970-7d44-44bb-b097-26183f402407", 4, "", 0);

        private UUID uuid;
        private int keyOffset;
        private int dimension;
        private String unit;
        private int precision;

        private float rangeMin = 0;
        private float rangeMax = 0;

        public UUID getUuid() {
            return uuid;
        }

        public int getKeyOffset() {
            return keyOffset;
        }

        public int getDimension() {
            return dimension;
        }

        public float[] getValueRange() {
            return new float[]{rangeMin, rangeMax};
        }

        SensorFeature(int keyOffset, String uuidString, int dimension, String unit, int precision) {
            this.uuid = UUID.fromString(uuidString);
            this.keyOffset = keyOffset;
            this.dimension = dimension;
            this.unit = unit;
            this.precision = precision;
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

        static final int maxValueDimension = 4;

        private float[] values = new float[maxValueDimension];

        public float[] getValues() {
            return values;
        }

        public boolean parseValue(byte[] data, Settings settings) {
            switch(this) {
                case ACCELEROMETER://in g
//                    var ax = (evothings.util.littleEndianToInt16(data, 3) / sensitvity).toFixed(2);
//                    var ay = (evothings.util.littleEndianToInt16(data, 5) / sensitvity).toFixed(2);
//                    var az = (evothings.util.littleEndianToInt16(data, 7) / sensitvity).toFixed(2);
                    for(int i = 0; i< dimension; i++)
                        values[i] = (short) CoolUtility.toIntLE(data, 3+2*i, 2) / (float)settings.accelerometerRange.getSensitivity();
                    break;
                case GYROSCOPE://in deg/s
//                    var ax = (evothings.util.littleEndianToInt16(data, 3) / sensitvity).toFixed(2);
//                    var ay = (evothings.util.littleEndianToInt16(data, 5) / sensitvity).toFixed(2);
//                    var az = (evothings.util.littleEndianToInt16(data, 7) / sensitvity).toFixed(2);
                    for(int i = 0; i< dimension; i++)
                        values[i] = (short) CoolUtility.toIntLE(data, 3+2*i, 2) / settings.gyroScopeRange.getSensitivity();
                    break;
                case MAGNETOMETER://in micro Tesla
//                    var ax = evothings.util.littleEndianToInt16(data, 3);
//                    var ay = evothings.util.littleEndianToInt16(data, 5);
//                    var az = evothings.util.littleEndianToInt16(data, 7);
                    for(int i = 0; i< dimension; i++)
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
//                    var wx = evothings.util.littleEndianToInt16(data, 3);
//                    var ax = evothings.util.littleEndianToInt16(data, 5);
//                    var ay = evothings.util.littleEndianToInt16(data, 7);
//                    var az = evothings.util.littleEndianToInt16(data, 9);
                    for(int i=0; i<dimension; i++)
                        values[i] = (short)CoolUtility.toIntLE(data, 3+2*i, 2);
                    break;
                default:
                    return false;
            }
            if(calibrating) {
                for(int i=0; i<dimension; i++) {
                    if(values[i] < minValues[i])
                        minValues[i] = values[i];
                    if(values[i] > maxValues[i])
                        maxValues[i] = values[i];
                }
            }
            valueParsed = true;
            return true;
        }

        public String getValueString() {
            if(!valueParsed)
                return "no value";
            String prefix = "";
            String valueString = null;
            if(dimension == 1)
                valueString = valueString(values[0]);
            else {
                if(dimension == 3)
                    prefix = "[x,y,z] = ";
                valueString = "[";
                for(int i = 0; i< dimension; i++) {
                    if(i > 0)
                        valueString += ", ";
                    valueString += valueString(values[i]);
                }
                valueString += "]";
            }
            if(valueString != null)
                return prefix + valueString + " " + unit;
            return null;
        }

        public String getValueString(float value) {
            return valueString(value) + " " + unit;
        }

        private String valueString(float value) {
            if(precision > 0)
                return String.format(Locale.getDefault(), "%." + String.valueOf(precision) + "f", value);
            else
                return String.format(Locale.getDefault(), "%d", (int)value);
        }

        protected boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        protected ArrayList<SensorValueRate> rates;

        protected SensorValueRate rate;

        public ArrayList<SensorValueRate> getRates() {
            if(rates == null) {
                rates = new ArrayList<>();
                switch(this) {
                    case ACCELEROMETER:
                        rates.addAll(Arrays.asList(InertialRate.values()));
                        break;
                    case GYROSCOPE:
                        rates.addAll(Arrays.asList(InertialRate._25, InertialRate._50, InertialRate._100));
                        break;
                    case MAGNETOMETER:
                        return null;
                    case BAROMETER:
                    case HUMIDITY:
                    case TEMPERATURE:
                        rates.addAll(Arrays.asList(EnvironmentalRate.values()));
                        break;
                    case SFL:
                        rates.addAll(Arrays.asList(SensorFusionRate.values()));
                        break;
                }
            }
            return rates;
        }

        float minValues[] = new float[maxValueDimension];
        float maxValues[] = new float[maxValueDimension];

        boolean calibrating = false;

        public void startCalibration() {
            calibrating = true;
            System.arraycopy(values, 0, minValues, 0, dimension);
            System.arraycopy(values, 0, maxValues, 0, dimension);
        }

        public void stopCalibration() {
            calibrating = false;
        }

        public float[] getCalibratedValues() {
            float calibratedValues[] = new float[dimension];
            for(int i=0; i<dimension; i++) {
                if(maxValues[i] == minValues[i])
                    return null;
                float calibratedValue = (values[i] - minValues[i]) / (maxValues[i] - minValues[i]);
                calibratedValues[i] = Math.min(Math.max(calibratedValue, 0f), 1f);
            }
            return calibratedValues;
        }
    }

    public interface SensorValueRate {
        float getRate();
        String getValueString();
    }

    protected static final UUID UUID_INFO = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402408"); // Read Device Features

    public DialogIoTSensor(BluetoothDevice device) {
        super(device);
        //服务和特性UUID
        UUID_MAIN_SERVICE = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402400");
        UUID_MAIN_CONFIG = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402409");//CONTROL_POINT
        UUID_MAIN_DATA = UUID.fromString("2ea78970-7d44-44bb-b097-26183f40240A");//CONTROL_REPLY
        //From BME280 datasheet
        SensorFeature.HUMIDITY.rangeMin = 0;
        SensorFeature.HUMIDITY.rangeMax = 100;
        SensorFeature.BAROMETER.rangeMin =  30000;
        SensorFeature.BAROMETER.rangeMax = 110000;
        SensorFeature.TEMPERATURE.rangeMin = -40;
        SensorFeature.TEMPERATURE.rangeMax = 85;
    }

    @Override
    public void onConnect() {
        startReceiveData();
        readInfo();
    }

    private void readInfo() {
        ReadCharacteristic(btGatt, btService, UUID_INFO);
    }

    private enum ControlCommand {
        WriteSettings(10), ReadSettings(11), SensorOn(1), SensorOff(0);
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

    private enum InertialRate implements SensorValueRate {
        _0_78(1, "0.78"),
        _1_56(2, "1.56"),
        _3_12(3, "3.12"),
        _6_25(4, "6.25"),
        _12_5(5, "12.5"),
        _25(6, "25"),
        _50(7, "50"),
        _100(8, "100");
        private int key;
        private String value;
        public float getRate() {
            return 100 * (float)Math.pow(2, key - 8);
        }
        public String getValueString() {
            return value + " Hz";
        }
        InertialRate(int key, String value) {
            this.key = key;
            this.value = value;
        }
        public static InertialRate findByKey(int key) {
            for(InertialRate rate : InertialRate.values())
                if(rate.key == key)
                    return rate;
            return null;
        }
    }

    private enum EnvironmentalRate implements SensorValueRate {
        _0_5(1, "0.5"),
        _1(2, "1"),
        _2(4, "2");
        private int key;
        private String value;
        public float getRate() {
            return key / 2f;
        }
        public String getValueString() {
            return value + " Hz";
        }
        EnvironmentalRate(int key, String value) {
            this.key = key;
            this.value = value;
        }
        public static EnvironmentalRate findByKey(int key) {
            for(EnvironmentalRate rate : EnvironmentalRate.values())
                if(rate.key == key)
                    return rate;
            return null;
        }
    }

    private enum SensorFusionRate implements SensorValueRate {
        _10(10),
        _15(15),
        _20(20),
        _25(25);
        private int key;
        public float getRate() {
            return (float)key;
        }
        public String getValueString() {
            return String.format(Locale.getDefault(), "%d Hz", key);
        }
        SensorFusionRate(int key) {
            this.key = key;
        }
        public static SensorFusionRate findByKey(int key) {
            for(SensorFusionRate rate : SensorFusionRate.values())
                if(rate.key == key)
                    return rate;
            return null;
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
        private byte sensorCombination;
        private AccelerometerRange accelerometerRange = AccelerometerRange._2G;
        private GyroscopeRange gyroScopeRange = GyroscopeRange._2000;
        private InertialRate accelerometerRate;
        private InertialRate gyroscopeRate;
        private byte magnetometerRate;
        private EnvironmentalRate environmentalRate;
        private SensorFusionRate sensorFusionRate;
        private byte sensorFusionRawDataEnable;
        private byte calibrationMode;
        private byte autoCalibrationMode;
        public void parse(byte[] data, int offset) {
            sensorCombination = data[offset];
            accelerometerRange = AccelerometerRange.findByKey(data[offset+1]);
            accelerometerRate = InertialRate.findByKey(data[offset+2]);
            gyroScopeRange = GyroscopeRange.findByKey(data[offset+3]);
            SensorFeature.ACCELEROMETER.rate = accelerometerRate;
            gyroscopeRate = InertialRate.findByKey(data[offset+4]);
            SensorFeature.GYROSCOPE.rate = gyroscopeRate;
            magnetometerRate = data[offset+5];
            environmentalRate = EnvironmentalRate.findByKey(data[offset+6]);
            SensorFeature.BAROMETER.rate = environmentalRate;
            SensorFeature.HUMIDITY.rate = environmentalRate;
            SensorFeature.TEMPERATURE.rate = environmentalRate;
            sensorFusionRate = SensorFusionRate.findByKey(data[offset+7]);
            SensorFeature.SFL.rate = sensorFusionRate;
            sensorFusionRawDataEnable = data[offset+8];
            calibrationMode = data[offset+9];
            autoCalibrationMode = data[offset+10];
        }
        public void write(byte[] data, int offset) {
            data[offset] = sensorCombination;
            data[offset+1] = (byte)accelerometerRange.key;
            data[offset+2] = (byte)accelerometerRate.key;
            data[offset+3] = (byte)gyroScopeRange.key;
            data[offset+4] = (byte)gyroscopeRate.key;
            data[offset+5] = magnetometerRate;
            data[offset+6] = (byte)environmentalRate.key;
            data[offset+7] = (byte)sensorFusionRate.key;
            data[offset+8] = sensorFusionRawDataEnable;
            data[offset+9] = calibrationMode;
            data[offset+10] = autoCalibrationMode;
        }
    }

    private Settings settings = new Settings();

    public void readSettings() {
        sendData(new byte[]{ControlCommand.ReadSettings.getId(), 0});
    }

    private void writeSettings() {
        byte[] command = new byte[12];
        command[0] = ControlCommand.WriteSettings.getId();
        settings.write(command, 1);
        sendData(command);
    }

    public void setSensorValueRate(SensorFeature feature, SensorValueRate rate) {
        switch(feature) {
            case ACCELEROMETER:
                settings.accelerometerRate = (InertialRate)rate;
                break;
            case GYROSCOPE:
                settings.gyroscopeRate = (InertialRate)rate;
                break;
            case BAROMETER:
            case HUMIDITY:
            case TEMPERATURE:
                settings.environmentalRate = (EnvironmentalRate)rate;
                break;
            case SFL:
                settings.sensorFusionRate = (SensorFusionRate)rate;
                break;
            default:
                return;
        }
        writeSettings();
    }

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

    public boolean isSensorOn() {
        return sensorOn;
    }

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

    public double getMagnetoAngle() throws Exception {
        float[] calibratedValues = SensorFeature.MAGNETOMETER.getCalibratedValues();
        if(calibratedValues != null) {
            float x = calibratedValues[0] - 0.5f;
            float y = 0.5f - calibratedValues[1];
            double angle;
            if(x == 0)
                angle = ((y > 0) ? 1 : -1) * Math.PI / 2;
            else {
                angle = Math.atan(y/x);
                if(x < 0)
                    angle += Math.PI;
            }
            if(angle < 0)
                angle += Math.PI * 2;
            return angle;
        }
        else
            throw new Exception("magnetometer not calibrated");
    }

    public String getMagnetoAngleString() {
        try {
            double angle = getMagnetoAngle();
            angle = angle / (Math.PI * 2) * 360;
            return String.format(Locale.getDefault(), " %d°", (int)angle);
        }
        catch (Exception ex) {
            Log.d("Dialog IoT Sensor", ex.getMessage());
            return "";
        }
    }

    public String getMagnetoDirectionString(String[] directions) {
        try {
            double angle = getMagnetoAngle();
            int index = (int)Math.round(angle / (Math.PI / 4)) % 8;
            return " " + directions[index];
        }
        catch (Exception ex) {
            Log.d("Dialog IoT Sensor", ex.getMessage());
            return "";
        }
    }
}
