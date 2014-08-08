package pateo.dls.bluetooth.profile;

import android.bluetooth.BluetoothDevice;
import pateo.dls.bluetooth.listener.PBAPCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangliu on 14-8-8.
 */
public class BPAPProfile extends BaseProfile
{
    /**
     * TAG
     */
    private final static String TAG = BPAPProfile.class.getSimpleName();

    private final List<PBAPCallback> mCallback = new ArrayList<PBAPCallback>();

    @Override public boolean isConnectable()
    {
        return false;
    }

    @Override public boolean isAutoConnectable()
    {
        return false;
    }

    @Override public boolean connect(BluetoothDevice device)
    {
        return false;
    }

    @Override public boolean disconnect(BluetoothDevice device)
    {
        return false;
    }

    @Override public int getConnectionStatus(BluetoothDevice device)
    {
        return 0;
    }

    @Override public boolean isProfileReady()
    {
        return false;
    }

    @Override public int getOrdinal()
    {
        return 0;
    }

    @Override public int getNameResource(BluetoothDevice device)
    {
        return 0;
    }

    public void registerEventListener(PBAPCallback listener)
    {
        synchronized (mCallback)
        {
            if (!mCallback.contains(listener))
            {
                mCallback.add(listener);
            }
        }
    }

    /**
     * 同步CallLog
     * @return
     */
    public boolean startCallLogSync()
    {
        return false;
    }

    /**
     * 同步联系人
     * @return
     */
    public boolean startContactSync()
    {
        return false;
    }

    public void unregisterEventListener(PBAPCallback listener)
    {
        synchronized (mCallback)
        {
            mCallback.remove(listener);
        }
    }
}
