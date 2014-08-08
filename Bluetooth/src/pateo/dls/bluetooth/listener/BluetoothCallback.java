package pateo.dls.bluetooth.listener;

import android.bluetooth.BluetoothDevice;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;

/**
 * Created by zixiangliu on 14-8-1.
 */
public interface BluetoothCallback
{
    /**
     * 蓝牙状态更改
     *
     * @param state BluetoothAdapter.STATE_OFF
     *              BluetoothAdapter.STATE_ON
     *              BluetoothAdapter.STATE_TURNING_OFF
     *              BluetoothAdapter.STATE_TURNING_ON
     *              BluetoothAdapter.ERROR
     */
    public void onBluetoothStateChanged(int state);

    /**
     * 扫描状态更改
     * @param mStarted
     */
    public void onScanningStateChanged(boolean mStarted);

    /**
     * 扫描到新设备
     * @param cachedDevice 扫描到新设备
     */
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice);

    /**
     * 设备名称更改
     * @param device 设备
     */
    public void onDeviceNameChange(CachedBluetoothDevice device);

    /**
     * 设备绑定状态更改
     * @param device
     * @param bondState
     * @param reason
     */
    public void onDeviceBondStateChanged(BluetoothDevice device, int bondState,
        int reason);

    /**
     * 设备被删除
     * @param cachedDevice
     */
    public void onDeviceDeleted(BluetoothDevice cachedDevice);

    /**
     * Device状态更改
     */
    public void onDeviceAttrChange();
}
