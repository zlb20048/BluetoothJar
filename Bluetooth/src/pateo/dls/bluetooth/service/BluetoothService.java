package pateo.dls.bluetooth.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import pateo.com.global.Communication.Server;

/**
 * @author zixiangliu
 */
public class BluetoothService extends Server
{
    /**
     * TAG
     */
    private final static String TAG = BluetoothService.class.getSimpleName();

    /**
     * 获取到当前的Bluetooth实例
     */
    private BluetoothInterface mInstance = null;

    @Override
    public IBinder GetServiceImplement(Intent intent)
    {
        return (IBinder)mInstance;
    }

    @Override
    public void Create()
    {
        mInstance = BluetoothServiceImpl.getInstance(this);
    }

    @Override
    public void Close()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void Destroy()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void OnStartCommand(Intent intent)
    {
        super.OnStartCommand(intent);
        Log.d(TAG, "OnStartCommand() : BluetoothService");
        BluetoothServiceImpl.doIntent(intent);
    }
}
