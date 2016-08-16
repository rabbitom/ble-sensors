package net.erabbit.blesensor;

import net.erabbit.bluetooth.BleApplication;

/**
 * Created by Tom on 16/7/25.
 */
public class BLESensorApp extends BleApplication {

    protected static BLESensorApp instance;

    public static BLESensorApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
