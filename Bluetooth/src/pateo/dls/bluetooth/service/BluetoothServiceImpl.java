package pateo.dls.bluetooth.service;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import pateo.com.ApplicationApi.AppCmdHelper;
import pateo.com.global.Communication.AppCmdCallBack;
import pateo.com.global.Communication.AppDefine;
import pateo.com.global.Communication.ClientCallback;
import pateo.dls.bluetooth.comm.BluetoothDefalutValue;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;
import pateo.dls.bluetooth.helper.BluetoothStateHelper;
import pateo.dls.bluetooth.listener.BluetoothCallback;
import pateo.dls.bluetooth.manager.CachedBluetoothDeviceManager;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;
import pateo.dls.bluetooth.profile.A2dpProfile;
import pateo.dls.bluetooth.profile.HesetProfile;

/**
 * Created by zixiangliu on 14-7-30.
 */
public class BluetoothServiceImpl extends BluetoothInterface
{
    /**
     * TAG
     */
    private final static String TAG =
        BluetoothServiceImpl.class.getSimpleName();

    /**
     * 获取到当前的实例
     */
    private static BluetoothServiceImpl instance;

    /**
     * 获取到操作BluetoothAdapter实例
     */
    private LocalBluetoothManager mBluetoothAdapter = null;

    /**
     * mHesetProfile
     */
    private HesetProfile mHesetProfile = null;

    /**
     * mA2dpProfile
     */
    private A2dpProfile mA2dpProfile = null;

    /**
     * BluetoothDefalutValue
     */
    private BluetoothDefalutValue mBluetoothDefalutValue = null;

    /**
     * BluetoothStateHelper
     */
    private BluetoothStateHelper mBluetoothStateHelper = null;

    /**
     * CachedBluetoothDeviceManager
     */
    private CachedBluetoothDeviceManager mCachedBluetoothDeviceManager = null;

    private static AppCmdCallBack mAppCmdCallBack = new AppCmdCallBack()
    {
        @Override
        public void OnSourceEnter(AppDefine.eAppServiceType appEnter,
            boolean isParking, boolean isFrontShow)
        {

        }

        @Override
        public void OnSourceExit(AppDefine.eAppServiceType appExit,
            AppDefine.eAppServiceType appEnter)
        {

        }

        @Override
        public void OnPageShow(AppDefine.eAppServiceType appShow,
            boolean isParking)
        {

        }

        @Override
        public void OnPageHide(AppDefine.eAppServiceType appHide,
            AppDefine.eAppServiceType appShow)
        {

        }
    };

    /**
     * 获取到上下文
     */
    private final Context mContext;

    @Override
    public A2dpProfile getBluetoothA2dp()
    {
        return mA2dpProfile;
    }

    @Override
    public LocalBluetoothManager getLocalBluetoothAdapter()
    {
        return mBluetoothAdapter;
    }

    @Override
    public HesetProfile getBluetoothHeset()
    {
        return mHesetProfile;
    }

    @Override
    public void addBluetoothCallBack(
        BluetoothCallback bluetoothCallback)
    {
        mBluetoothStateHelper.registerListener(bluetoothCallback);
    }

    @Override
    public boolean RegisterClient(ClientCallback Callback)
    {
        return false;
    }

    @Override
    public void ClientState(byte state)
    {

    }

    @Override
    public boolean UnRegisterClient()
    {
        return false;
    }

    /**
     * 私有构造方法
     *
     * @param context 上下文
     */
    private BluetoothServiceImpl(Context context)
    {
        this.mContext = context;
        initData();
        initDefultData();
    }

    /**
     * 初始化默认值
     */
    private void initDefultData()
    {
        mBluetoothDefalutValue = new BluetoothDefalutValue(mContext);
        String name = mBluetoothDefalutValue.getBluetoothName();
        mBluetoothAdapter.setName(name);
        BluetoothDefalutValue.BluetoothState bluetoothState =
            mBluetoothDefalutValue.getBluetoothState();
        switch (bluetoothState)
        {
            case BLUETOOTH_ON:
                mBluetoothAdapter.enable();
                break;
            case BLUETOOTH_OFF:
                mBluetoothAdapter.disable();
                break;
        }
    }

    /**
     * 初始化相关数据
     */
    private void initData()
    {
        mCachedBluetoothDeviceManager =
            new CachedBluetoothDeviceManager(mContext);
        mBluetoothStateHelper = BluetoothStateHelper.getInstance(mContext,
            mCachedBluetoothDeviceManager);
        mBluetoothAdapter = LocalBluetoothManager.getInstance();
        mHesetProfile = HesetProfile.getInstance(mContext);
        mA2dpProfile = A2dpProfile.getInstace(mContext);
        mBluetoothStateHelper.addProfileAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
            new StateChangedHandler(mA2dpProfile));
        mBluetoothStateHelper.addProfileAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
            new StateChangedHandler(mHesetProfile));
    }

    /**
     * 获取到单实例对象
     *
     * @param context 上下文
     * @return
     */
    public static synchronized BluetoothServiceImpl getInstance(Context context)
    {
        if (null == instance)
        {
            instance = new BluetoothServiceImpl(context);
        }
        return instance;
    }

    /**
     * 设置当前的AppCmdCallBack
     *
     * @param intent
     */
    public static void doIntent(Intent intent)
    {
        if (intent != null)
        {
            AppCmdHelper.ProcessCmd(intent, mAppCmdCallBack);
        }
    }

    /**
     * Generic handler for connection state change events for the specified profile.
     */
    private class StateChangedHandler implements BluetoothStateHelper.Handler
    {
        final pateo.dls.bluetooth.profile.BaseProfile mProfile;

        StateChangedHandler(pateo.dls.bluetooth.profile.BaseProfile profile)
        {
            mProfile = profile;
        }

        public void onReceive(Context context, Intent intent,
            BluetoothDevice device)
        {
            CachedBluetoothDevice cachedDevice =
                mCachedBluetoothDeviceManager.findDevice(device);
            if (cachedDevice == null)
            {
                Log.w(TAG, "StateChangedHandler found new device: " + device);
                cachedDevice =
                    mCachedBluetoothDeviceManager.addDevice(mBluetoothAdapter,
                        device);
            }
            int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
            int oldState =
                intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, 0);
            if (newState == BluetoothProfile.STATE_DISCONNECTED &&
                oldState == BluetoothProfile.STATE_CONNECTING)
            {
                Log.i(TAG, "Failed to connect " + mProfile + " device");
            }

            cachedDevice.onProfileStateChanged(mProfile, newState);
            cachedDevice.refresh();
        }
    }
}
