package pateo.dls.bluetooth.service;

import android.os.Binder;
import pateo.com.global.Communication.ServiceInterface;
import pateo.dls.bluetooth.listener.BluetoothCallback;
import pateo.dls.bluetooth.manager.LocalBluetoothManager;
import pateo.dls.bluetooth.profile.A2dpProfile;
import pateo.dls.bluetooth.profile.HesetProfile;

/**
 * Created by zixiangliu on 14-7-30.
 */
public abstract class BluetoothInterface extends Binder
    implements ServiceInterface
{
    /**
     * 获取到A2dp对象
     * @return 当前的A2dp的对象
     */
    public abstract A2dpProfile getBluetoothA2dp();

    /**
     * 获取到本地的BluetoothAdpater
     * @return
     */
    public abstract LocalBluetoothManager getLocalBluetoothAdapter();

    /**
     * 获取到通话的实例
     * @return
     */
    public abstract HesetProfile getBluetoothHeset();

    /**
     * 蓝牙一些状态的回调
     * @param bluetoothCallback 回调当前蓝牙的各种状态
     */
    public abstract void addBluetoothCallBack(
        BluetoothCallback bluetoothCallback);
}
