package pateo.dls.bluetooth.comm;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;
import pateo.dls.bluetooth.manager.LocalProfileManager;
import pateo.dls.bluetooth.profile.BaseProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zixiangliu on 14-7-31.
 */
public class CachedBluetoothDevice
{
    private final static String TAG =
        CachedBluetoothDevice.class.getSimpleName();

    private Context mContext;

    private static CachedBluetoothDevice instance;

    private LocalBluetoothManager localBluetoothManager;

    private BluetoothDevice mDevice;

    private boolean mConnectAfterPairing = false;

    private List<BaseProfile> profiles = null;

    private Map<BaseProfile, Integer> mProfileConnectState = null;

    private boolean isVisible = false;

    /**
     * 设备名称
     */
    private String mName;

    private List<Callback> mCallbacks = new ArrayList<Callback>();

    public void onProfileStateChanged(BaseProfile mProfile, int newState)
    {

    }

    public void refresh()
    {

    }

    public interface Callback
    {
        void onDeviceAttrChange();
    }

    /**
     * 增加Device状态回调
     * @param callback
     */
    public void addCallback(Callback callback)
    {
        synchronized (mCallbacks)
        {
            if (!mCallbacks.contains(callback))
            {
                mCallbacks.add(callback);
            }
        }
    }

    /**
     * 更改状态回调
     * @param callback
     */
    public void removeCallback(Callback callback)
    {
        synchronized (mCallbacks)
        {
            mCallbacks.remove(callback);
        }
    }

    public CachedBluetoothDevice(
        Context mContext, LocalBluetoothManager adapter, BluetoothDevice device)
    {
        this.mContext = mContext;
        this.localBluetoothManager = adapter;
        this.mDevice = device;
        profiles = LocalProfileManager.getInstance().getBaseProfiles();
        mProfileConnectState = new HashMap<BaseProfile, Integer>();
    }

    /**
     * connect All profile
     */
    public void connect()
    {
        if (!ensurePaired())
        {
            return;
        }
        synchronized (profiles)
        {
            for (BaseProfile manager : profiles)
            {
                manager.connect(mDevice);
            }
        }
    }

    /**
     * 是否可见
     * @return
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }

    private boolean ensurePaired()
    {
        if (getBondState() == BluetoothDevice.BOND_NONE)
        {
            startPairing();
            return false;
        }
        else
        {
            return true;
        }
    }

    public int getBondState()
    {
        return mDevice.getBondState();
    }

    boolean startPairing()
    {
        // Pairing is unreliable while scanning, so cancel discovery
        if (localBluetoothManager.isDiscovering())
        {
            localBluetoothManager.cancelDiscovery();
        }

        if (!mDevice.createBond())
        {
            return false;
        }

        mConnectAfterPairing = true;  // auto-connect after pairing
        return true;
    }

    /**
     * disConnect All the connect Profile
     */
    public void disConnect()
    {
        synchronized (profiles)
        {
            for (BaseProfile manager : profiles)
            {
                manager.disconnect(mDevice);
            }
        }
    }

    public BluetoothDevice getDevice()
    {
        return mDevice;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        if (!mName.equals(name))
        {
            if (TextUtils.isEmpty(name))
            {
                mName = mDevice.getAddress();
            }
            else
            {
                mName = name;
                mDevice.setAlias(name);
            }
            dispatchAttributesChanged();
        }
    }

    /**
     * 通知Device更改
     */
    private void dispatchAttributesChanged()
    {
        synchronized (mCallbacks)
        {
            for (Callback callback : mCallbacks)
            {
                callback.onDeviceAttrChange();
            }
        }
    }

    public void refreshName()
    {
        mName = mDevice.getAliasName();
        if (TextUtils.isEmpty(mName))
        {
            mName = mDevice.getAddress();
            Log.d(TAG, "Device has no name (yet), use address: " + mName);
        }
        dispatchAttributesChanged();
    }

    public void refreshBtClass()
    {
        dispatchAttributesChanged();
    }

    public void onUuidChanged()
    {

    }

    public void clearProfileConnectionState()
    {

    }

    public void setRssi(short rssi)
    {
    }

    public void setBtClass(BluetoothClass btClass)
    {
    }

    /**
     * Checks whether we are connected to this device (any profile counts).
     *
     * @return Whether it is connected.
     */
    public boolean isConnected()
    {
        for (BaseProfile profile : profiles)
        {
            int status = getProfileConnectionState(profile);
            if (status == BluetoothProfile.STATE_CONNECTED)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取到当前的连接状态
     * @param profile 当前的Profile
     * @return
     */
    private int getProfileConnectionState(BaseProfile profile)
    {
        if (mProfileConnectState == null ||
            mProfileConnectState.get(profile) == null)
        {
            // If cache is empty make the binder call to get the state
            int state = profile.getConnectionStatus(mDevice);
            mProfileConnectState.put(profile, state);
        }
        return mProfileConnectState.get(profile);
    }
}
