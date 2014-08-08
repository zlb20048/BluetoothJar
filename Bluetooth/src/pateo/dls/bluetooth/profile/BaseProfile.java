package pateo.dls.bluetooth.profile;

import android.bluetooth.BluetoothDevice;

/**
 * Created by zixiangliu on 14-8-1.
 */
public abstract class BaseProfile
{
    /**
     * Returns true if the user can initiate a connection, false otherwise.
     */
    public abstract boolean isConnectable();

    /**
     * Returns true if the user can enable auto connection for this profile.
     */
    public abstract boolean isAutoConnectable();

    public abstract boolean connect(BluetoothDevice device);

    public abstract boolean disconnect(BluetoothDevice device);

    public abstract int getConnectionStatus(BluetoothDevice device);

    public abstract boolean isProfileReady();

    /** Display order for device profile settings. */
    public abstract int getOrdinal();

    /**
     * Returns the string resource ID for the localized name for this profile.
     * @param device the Bluetooth device (to distinguish between PAN roles)
     */
    public abstract int getNameResource(BluetoothDevice device);
}
