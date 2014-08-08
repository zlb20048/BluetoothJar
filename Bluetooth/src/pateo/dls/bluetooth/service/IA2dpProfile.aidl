package pateo.dls.bluetooth.service;

interface IA2dpProfile
{
    /**
     * Returns true if the user can initiate a connection, false otherwise.
     */
    boolean isConnectable();

    /**
     * Returns true if the user can enable auto connection for this profile.
     */
    boolean isAutoConnectable();

    boolean connect(in BluetoothDevice device);

    boolean disconnect(in BluetoothDevice device);

    int getConnectionStatus(in BluetoothDevice device);

    boolean isProfileReady();

    /** Display order for device profile settings. */
    int getOrdinal();

    /**
     * Returns the string resource ID for the localized name for this profile.
     * @param device the Bluetooth device (to distinguish between PAN roles)
     */
    int getNameResource(in BluetoothDevice device);
}