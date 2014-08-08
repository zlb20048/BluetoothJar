package pateo.dls.bluetooth.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;
import pateo.dls.bluetooth.listener.BluetoothCallback;
import pateo.dls.bluetooth.manager.CachedBluetoothDeviceManager;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zixiangliu on 14-7-31.
 */
public class BluetoothStateHelper
{
    /**
     * TAG
     */
    private final static String TAG =
        BluetoothStateHelper.class.getSimpleName();

    /**
     * mContext
     */
    private Context mContext;

    /**
     * 单实例
     */
    private static BluetoothStateHelper instance;

    /**
     * CachedBluetoothDeviceManager
     */
    private CachedBluetoothDeviceManager mDeviceManager = null;

    /**
     * mFilter
     */
    private IntentFilter mFilter = null;

    /**
     * 回调的信息
     */
    private Collection<BluetoothCallback> mCallbacks =
        new ArrayList<BluetoothCallback>();

    /**
     * 将当前的值回调出去
     */
    private Map<String, Handler> mHandlerMap = new HashMap<String, Handler>();

    public interface Handler
    {
        void onReceive(Context context, Intent intent, BluetoothDevice device);
    }

    /**
     * 广播回调状态
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            Log.d(TAG, "action ------> " + action);
            BluetoothDevice device = intent
                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Handler mHandler = mHandlerMap.get(action);
            if (null != mHandler)
            {
                mHandler.onReceive(context, intent, device);
            }
        }
    };

    private BluetoothStateHelper(Context context,
        CachedBluetoothDeviceManager mDeviceManager)
    {
        this.mContext = context;
        this.mDeviceManager = mDeviceManager;
        mFilter = new IntentFilter();
        registerReceiver();
    }

    public static synchronized BluetoothStateHelper getInstance(Context context,
        CachedBluetoothDeviceManager cachedBluetoothDeviceManager)
    {
        if (instance == null)
        {
            instance =
                new BluetoothStateHelper(context, cachedBluetoothDeviceManager);
        }
        return instance;
    }

    private void addHandler(String action, Handler handler)
    {
        mHandlerMap.put(action, handler);
        mFilter.addAction(action);
    }

    public void addProfileAction(String action, Handler handler)
    {
        mHandlerMap.put(action, handler);
        mFilter.addAction(action);
    }

    private void registerReceiver()
    {
        // Bluetooth on/off broadcasts
        addHandler(BluetoothAdapter.ACTION_STATE_CHANGED,
            new AdapterStateChangedHandler());

        // Discovery broadcasts
        addHandler(BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            new ScanningStateChangedHandler(true));
        addHandler(BluetoothAdapter.ACTION_DISCOVERY_FINISHED,
            new ScanningStateChangedHandler(false));
        addHandler(BluetoothDevice.ACTION_FOUND, new DeviceFoundHandler());
        addHandler(BluetoothDevice.ACTION_DISAPPEARED,
            new DeviceDisappearedHandler());
        addHandler(BluetoothDevice.ACTION_NAME_CHANGED,
            new NameChangedHandler());

        // Pairing broadcasts
        addHandler(BluetoothDevice.ACTION_BOND_STATE_CHANGED,
            new BondStateChangedHandler());
        addHandler(BluetoothDevice.ACTION_PAIRING_CANCEL,
            new PairingCancelHandler());

        // Fine-grained state broadcasts
        addHandler(BluetoothDevice.ACTION_CLASS_CHANGED,
            new ClassChangedHandler());
        addHandler(BluetoothDevice.ACTION_UUID, new UuidChangedHandler());

        // Dock event broadcasts
        addHandler(Intent.ACTION_DOCK_EVENT, new DockEventHandler());

        mContext.registerReceiver(mBroadcastReceiver, mFilter);
    }

    public void unRegisterReceiver()
    {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 注册回调信息
     *
     * @param callBack 回调的信息
     */
    public void registerListener(BluetoothCallback callBack)
    {
        synchronized (mCallbacks)
        {
            if (!mCallbacks.contains(callBack))
            {
                mCallbacks.add(callBack);
            }
        }
    }

    /**
     * 取消注册回调
     *
     * @param callBack
     */
    public void unRegisterListener(BluetoothCallback callBack)
    {
        synchronized (mCallbacks)
        {
            mCallbacks.remove(callBack);
        }
    }

    /**
     * 状态更改的消息
     */
    private class AdapterStateChangedHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR);
            // send callback to update UI and possibly start scanning
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onBluetoothStateChanged(state);
                }
            }

        }
    }

    private class ScanningStateChangedHandler implements Handler
    {
        private final boolean mStarted;

        ScanningStateChangedHandler(boolean started)
        {
            mStarted = started;
        }

        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onScanningStateChanged(mStarted);
                }
            }
        }

    }

    private class DeviceFoundHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
                Short.MIN_VALUE);
            BluetoothClass btClass =
                intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            // TODO Pick up UUID. They should be available for 2.1 devices.
            // Skip for now, there's a bluez problem and we are not getting uuids even for 2.1.
            // callback to UI to create Preference for new device
            CachedBluetoothDevice cachedDevice =
                mDeviceManager.findDevice(device);
            if (cachedDevice == null)
            {
                cachedDevice =
                    mDeviceManager.addDevice(LocalBluetoothManager.getInstance(),
                        device);
                Log.d(TAG,
                    "DeviceFoundHandler created new CachedBluetoothDevice: "
                        + cachedDevice);
                // callback to UI to create Preference for new device
                dispatchDeviceAdded(cachedDevice);
            }
            cachedDevice.setRssi(rssi);
            cachedDevice.setBtClass(btClass);
            cachedDevice.setName(name);
            cachedDevice.setVisible(true);

        }

        private void dispatchDeviceAdded(CachedBluetoothDevice cachedDevice)
        {
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onDeviceAdded(cachedDevice);
                }
            }
        }
    }

    /**
     * 名称更改
     */
    private class NameChangedHandler implements Handler
    {
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            CachedBluetoothDevice cachedDevice =
                mDeviceManager.findDevice(device);
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onDeviceNameChange(cachedDevice);
                }
            }
        }
    }

    /**
     * 配对状态更改
     */
    private class BondStateChangedHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            if (device == null)
            {
                Log.e(TAG, "ACTION_BOND_STATE_CHANGED with no EXTRA_DEVICE");
                return;
            }
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                BluetoothDevice.ERROR);
            int reason = intent.getIntExtra(BluetoothDevice.EXTRA_REASON,
                BluetoothDevice.ERROR);
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onDeviceBondStateChanged(device,
                        bondState,
                        reason);
                }
            }
        }
    }

    private class DeviceDisappearedHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            synchronized (mCallbacks)
            {
                for (BluetoothCallback callback : mCallbacks)
                {
                    callback.onDeviceDeleted(device);
                }
            }

        }
    }

    private class PairingCancelHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            if (device == null)
            {
                Log.e(TAG, "ACTION_PAIRING_CANCEL with no EXTRA_DEVICE");
                return;
            }
        }
    }

    private class ClassChangedHandler implements Handler
    {
        @Override
        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {

        }
    }

    private class UuidChangedHandler implements Handler
    {
        @Override public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {

        }
    }

    private class DockEventHandler implements Handler
    {
        @Override public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {

        }
    }
}
