package pateo.dls.bluetooth.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;
import pateo.dls.bluetooth.listener.HesetEventListener;
import pateo.dls.bluetooth.manager.CachedBluetoothDeviceManager;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;
import pateo.dls.bluetooth.manager.LocalProfileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangliu on 14-7-30.
 */
public class HesetProfile extends BaseProfile
{
    /**
     * TAG
     */
    private final static String TAG =
        HesetProfile.class.getSimpleName();

    /**
     * 上下文
     */
    private final Context mContext;

    /**
     * BluetoothHeadset
     */
    private BluetoothHeadset mService;

    /**
     * 是否准备
     */
    private boolean isProfileReady = false;

    /**
     * CachedBluetoothDeviceManager
     */
    private CachedBluetoothDeviceManager mDeviceManager;

    /**
     * 实例对象
     */
    private static HesetProfile instance;

    /**
     * hetset
     */
    private List<HesetEventListener> mHesetEventListeners =
        new ArrayList<HesetEventListener>();

    /**
     * ServiceListener
     */
    private BluetoothProfile.ServiceListener listener =
        new BluetoothProfile.ServiceListener()
        {
            @Override
            public void onServiceConnected(int i,
                BluetoothProfile bluetoothProfile)
            {
                mService = (BluetoothHeadset)bluetoothProfile;
                isProfileReady = true;
                List<BluetoothDevice> deviceList =
                    mService.getConnectedDevices();
                while (!deviceList.isEmpty())
                {
                    BluetoothDevice nextDevice = deviceList.remove(0);
                    CachedBluetoothDevice device =
                        mDeviceManager.findDevice(nextDevice);
                    // we may add a new device here, but generally this should not happen
                    if (device == null)
                    {
                        Log.w(TAG,
                            "A2dpProfile found new device: "
                                + nextDevice);
                        device =
                            mDeviceManager.addDevice(LocalBluetoothManager.getInstance(),
                                nextDevice);
                    }
                    device.onProfileStateChanged(HesetProfile.this,
                        BluetoothProfile.STATE_CONNECTED);
                    device.refresh();
                }
                LocalProfileManager.getInstance().addProfile(instance);
                // TODO 注册当前的HFP状态的回调
            }

            @Override
            public void onServiceDisconnected(int i)
            {
                isProfileReady = false;
                LocalProfileManager.getInstance().removeProfile(instance);
            }
        };

    private HesetProfile(Context context)
    {
        this.mContext = context;
        bindHeset();
    }

    public void bindHeset()
    {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(mContext, listener,
            BluetoothProfile.HEADSET);
    }

    /**
     * 获取到BluetoothHesetManager
     *
     * @param context
     * @return
     */
    public static synchronized HesetProfile getInstance(
        Context context)
    {
        if (instance == null)
        {
            instance = new HesetProfile(context);
        }
        return instance;
    }

    @Override
    public boolean isConnectable()
    {
        return true;
    }

    @Override
    public boolean isAutoConnectable()
    {
        return true;
    }

    @Override
    public boolean connect(BluetoothDevice device)
    {
        if (mService == null)
        {
            return false;
        }
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (sinks != null)
        {
            for (BluetoothDevice sink : sinks)
            {
                mService.disconnect(sink);
            }
        }
        return mService.connect(device);

    }

    @Override
    public boolean disconnect(BluetoothDevice device)
    {
        if (mService == null)
        {
            return false;
        }
        List<BluetoothDevice> deviceList = mService.getConnectedDevices();
        if (!deviceList.isEmpty() && deviceList.get(0).equals(device))
        {
            // Downgrade priority as user is disconnecting the headset.
            if (mService.getPriority(device) > BluetoothProfile.PRIORITY_ON)
            {
                mService.setPriority(device, BluetoothProfile.PRIORITY_ON);
            }
            return mService.disconnect(device);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int getConnectionStatus(BluetoothDevice device)
    {
        return 0;
    }

    @Override
    public boolean isProfileReady()
    {
        return isProfileReady;
    }

    @Override
    public int getOrdinal()
    {
        return 0;
    }

    @Override
    public int getNameResource(BluetoothDevice device)
    {
        return 0;
    }

    /**
     * AudioOn
     * @return
     */
    public boolean isAudioOn()
    {
        if (mService != null)
        {
            return mService.isAudioOn();
        }
        return false;
    }

    /**
     * 注册当前的回调状态
     *
     * @param listener 当前的回调的状态
     */
    public void registerEventListener(HesetEventListener listener)
    {
        synchronized (mHesetEventListeners)
        {
            if (!mHesetEventListeners.contains(listener))
            {
                mHesetEventListeners.add(listener);
            }
        }
    }

    /**
     * 取消注册当前的事件回调
     *
     * @param listener
     */
    public void unRegisterEventListener(HesetEventListener listener)
    {
        synchronized (mHesetEventListeners)
        {
            mHesetEventListeners.remove(listener);
        }
    }

    private void audioEstableished()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.audioEstableished();
            }
        }
    }

    private void audioRelased()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.audioRelased();
            }
        }
    }

    private void inComingCall()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.inComingCall();
            }
        }
    }

    private void onGoingCall()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.onGoingCall();
            }
        }
    }

    private void serviceEstablished()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.serviceEstablished();
            }
        }
    }

    private void outGoingCall()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.outGoingCall();
            }
        }
    }

    private void serviceReleased()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.serviceReleased();
            }
        }
    }

    private void standby()
    {
        synchronized (mHesetEventListeners)
        {
            for (HesetEventListener mHesetEventListener : mHesetEventListeners)
            {
                mHesetEventListener.standby();
            }
        }
    }
}
