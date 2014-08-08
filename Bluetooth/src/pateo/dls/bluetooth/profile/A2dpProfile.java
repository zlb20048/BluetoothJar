package pateo.dls.bluetooth.profile;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import pateo.dls.bluetooth.comm.BluetoothMediaInfo;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;
import pateo.dls.bluetooth.listener.A2dpEventListener;
import pateo.dls.bluetooth.manager.CachedBluetoothDeviceManager;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;
import pateo.dls.bluetooth.manager.LocalProfileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangliu on 14-7-30.
 */
public class A2dpProfile extends BaseProfile
{
    /**
     * TAG
     */
    private final static String TAG =
        A2dpProfile.class.getSimpleName();

    /**
     * context
     */
    private final Context mContext;

    /**
     * A2dp是否准备好了
     */
    private boolean mIsProfileReady = false;

    /**
     * BluetoothA2dpManager
     */
    private static A2dpProfile instance = null;

    /**
     * BluetoothA2dp
     */
    private BluetoothA2dp mService = null;

    /**
     * CachedBluetoothDeviceManager
     */
    private CachedBluetoothDeviceManager mDeviceManager;

    /**
     * mA2dpEventListeners
     */
    private List<A2dpEventListener> mA2dpEventListeners =
        new ArrayList<A2dpEventListener>();

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
                mService = (BluetoothA2dp)bluetoothProfile;
                mIsProfileReady = true;
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
                    device.onProfileStateChanged(A2dpProfile.this,
                        BluetoothProfile.STATE_CONNECTED);
                    device.refresh();
                }

                LocalProfileManager.getInstance().addProfile(instance);
                // TODO 注册回调给到Bluetooth
            }

            @Override
            public void onServiceDisconnected(int i)
            {
                mService = null;
                mIsProfileReady = false;
                LocalProfileManager.getInstance().removeProfile(instance);
            }
        };

    /**
     * 私有构造方法
     *
     * @param context
     */
    private A2dpProfile(Context context)
    {
        mContext = context;
        bindA2dp();
    }

    /**
     * 绑定当前的A2dp
     *
     * @return
     */
    public boolean bindA2dp()
    {
        return BluetoothAdapter.getDefaultAdapter()
            .getProfileProxy(mContext, listener,
                BluetoothProfile.A2DP);
    }

    /**
     * 通知MediaInfo更改
     *
     * @param mediaInfo 更改后的MediaInfo
     */
    private void notifyMediaInfoChange(BluetoothMediaInfo mediaInfo)
    {
        synchronized (mA2dpEventListeners)
        {
            for (A2dpEventListener mA2dpEventListener : mA2dpEventListeners)
            {
                mA2dpEventListener.onMediaInfoChange(mediaInfo);
            }
        }
    }

    /**
     * 播放时间更改
     *
     * @param duration duration
     */
    private void notifyDurationChange(long duration)
    {
        synchronized (mA2dpEventListeners)
        {
            for (A2dpEventListener mA2dpEventListener : mA2dpEventListeners)
            {
                mA2dpEventListener.onPlaydurationChange(duration);
            }
        }
    }

    /**
     * a2dp是否连接成功
     *
     * @return 当前的是否成功
     */
    public boolean isProfileReady()
    {
        return mIsProfileReady;
    }

    @Override public int getOrdinal()
    {
        return 0;
    }

    @Override public int getNameResource(BluetoothDevice device)
    {
        return 0;
    }

    /**
     * 获取到当前的实例
     *
     * @param context context
     * @return
     */
    public synchronized static A2dpProfile getInstace(Context context)
    {
        if (instance == null)
        {
            instance = new A2dpProfile(context);
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

    /**
     * 连接A2dp
     *
     * @param device 当前连接的对象
     * @return
     */
    public boolean connect(BluetoothDevice device)
    {
        if (mService == null)
        {
            return false;
        }
        List<BluetoothDevice> sinks = getConnectedDevices();
        if (sinks != null)
        {
            for (BluetoothDevice sink : sinks)
            {
                mService.disconnect(sink);
            }
        }
        return mService.connect(device);
    }

    /**
     * disconnect
     *
     * @param device device
     * @return
     */
    public boolean disconnect(BluetoothDevice device)
    {
        if (mService == null)
        {
            return false;
        }
        // Downgrade priority as user is disconnecting the headset.
        if (mService.getPriority(device) > BluetoothProfile.PRIORITY_ON)
        {
            mService.setPriority(device, BluetoothProfile.PRIORITY_ON);
        }
        return mService.disconnect(device);
    }

    @Override
    public int getConnectionStatus(BluetoothDevice device)
    {
        if (mService == null)
        {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
        return mService.getConnectionState(device);
    }

    /**
     * A2dp是否正在播放
     *
     * @return 当前是否正在播放
     */
    public boolean isA2dpPlaying()
    {
        if (mService == null)
        {
            return false;
        }
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (!sinks.isEmpty())
        {
            if (mService.isA2dpPlaying(sinks.get(0)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * A2dp远程控制，控制如播放、暂停、快进、快退等
     *
     * @return
     */
    public boolean remoteControl()
    {
        return false;
    }

    /**
     * 获取当前的service的状态
     *
     * @return 当前的service的状态
     */
    public List<BluetoothDevice> getConnectedDevices()
    {
        if (mService == null)
        {
            return new ArrayList<BluetoothDevice>(0);
        }
        return mService.getDevicesMatchingConnectionStates(
            new int[] {BluetoothProfile.STATE_CONNECTED,
                BluetoothProfile.STATE_CONNECTING,
                BluetoothProfile.STATE_DISCONNECTING});
    }

    /**
     * 注册歌曲信息回调
     *
     * @param listener 当前的歌曲信息回调
     */
    public void registerAvrcpEventListener(A2dpEventListener listener)
    {
        synchronized (mA2dpEventListeners)
        {
            if (!mA2dpEventListeners.contains(listener))
            {
                mA2dpEventListeners.add(listener);
            }
        }
    }

    /**
     * 取消注册回调
     *
     * @param listener 之前注册的歌曲信息回调接口
     */
    public void unregisterAvrcpEventListener(A2dpEventListener listener)
    {
        synchronized (mA2dpEventListeners)
        {
            mA2dpEventListeners.remove(listener);
        }
    }
}
