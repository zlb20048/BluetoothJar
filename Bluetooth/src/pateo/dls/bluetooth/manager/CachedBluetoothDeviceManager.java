/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pateo.dls.bluetooth.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CachedBluetoothDeviceManager manages the set of remote Bluetooth devices.
 */
public final class CachedBluetoothDeviceManager
{

    private static final String TAG =
        CachedBluetoothDeviceManager.class.getSimpleName();

    private Context mContext;

    private final List<CachedBluetoothDevice> mCachedDevices =
        new ArrayList<CachedBluetoothDevice>();

    public CachedBluetoothDeviceManager(Context context)
    {
        mContext = context;
    }

    public synchronized Collection<CachedBluetoothDevice> getCachedDevicesCopy()
    {
        return new ArrayList<CachedBluetoothDevice>(mCachedDevices);
    }

    public static boolean onDeviceDisappeared(
        CachedBluetoothDevice cachedDevice)
    {
        cachedDevice.setVisible(false);
        return cachedDevice.getBondState() == BluetoothDevice.BOND_NONE;
    }

    public void onDeviceNameUpdated(BluetoothDevice device)
    {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null)
        {
            cachedDevice.refreshName();
        }
    }

    /**
     * Search for existing {@link CachedBluetoothDevice} or return null
     * if this device isn't in the cache. Use {@link #addDevice}
     * to create and return a new {@link CachedBluetoothDevice} for
     * a newly discovered {@link android.bluetooth.BluetoothDevice}.
     *
     * @param device the address of the Bluetooth device
     * @return the cached device object for this device, or null if it has
     * not been previously seen
     */
    public CachedBluetoothDevice findDevice(BluetoothDevice device)
    {
        for (CachedBluetoothDevice cachedDevice : mCachedDevices)
        {
            if (cachedDevice.getDevice().equals(device))
            {
                return cachedDevice;
            }
        }
        return null;
    }

    /**
     * Create and return a new {@link CachedBluetoothDevice}. This assumes
     * that {@link #findDevice} has already been called and returned null.
     *
     * @param device the address of the new Bluetooth device
     * @return the newly created CachedBluetoothDevice object
     */
    public CachedBluetoothDevice addDevice(LocalBluetoothManager adapter,
        BluetoothDevice device)
    {
        CachedBluetoothDevice newDevice =
            new CachedBluetoothDevice(mContext, adapter, device);
        mCachedDevices.add(newDevice);
        return newDevice;
    }

    /**
     * Attempts to get the name of a remote device, otherwise returns the address.
     *
     * @param device The remote device.
     * @return The name, or if unavailable, the address.
     */
    public String getName(BluetoothDevice device)
    {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null)
        {
            return cachedDevice.getName();
        }

        String name = device.getAliasName();
        if (name != null)
        {
            return name;
        }

        return device.getAddress();
    }

    /**
     * 扫描状态更改
     * @param started
     */
    public synchronized void onScanningStateChanged(boolean started)
    {
        if (!started)
            return;

        // If starting a new scan, clear old visibility
        // Iterate in reverse order since devices may be removed.
        for (int i = mCachedDevices.size() - 1; i >= 0; i--)
        {
            CachedBluetoothDevice cachedDevice = mCachedDevices.get(i);
            cachedDevice.setVisible(false);
        }
    }

    public synchronized void onBtClassChanged(BluetoothDevice device)
    {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null)
        {
            cachedDevice.refreshBtClass();
        }
    }

    public synchronized void onUuidChanged(BluetoothDevice device)
    {
        CachedBluetoothDevice cachedDevice = findDevice(device);
        if (cachedDevice != null)
        {
            cachedDevice.onUuidChanged();
        }
    }

    public synchronized void onBluetoothStateChanged(int bluetoothState)
    {
        // When Bluetooth is turning off, we need to clear the non-bonded devices
        // Otherwise, they end up showing up on the next BT enable
        if (bluetoothState == BluetoothAdapter.STATE_TURNING_OFF)
        {
            for (int i = mCachedDevices.size() - 1; i >= 0; i--)
            {
                CachedBluetoothDevice cachedDevice = mCachedDevices.get(i);
                if (cachedDevice.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    cachedDevice.setVisible(false);
                    mCachedDevices.remove(i);
                }
                else
                {
                    // For bonded devices, we need to clear the connection status so that
                    // when BT is enabled next time, device connection status shall be retrieved
                    // by making a binder call.
                    cachedDevice.clearProfileConnectionState();
                }
            }
        }
    }

    private void log(String msg)
    {
        Log.d(TAG, msg);
    }
}
