package pateo.dls.bluetooth.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import pateo.dls.bluetooth.comm.CachedBluetoothDevice;
import pateo.dls.bluetooth.profile.BaseProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zixiangliu on 14-8-1.
 */
public class LocalProfileManager
{
    /**
     * TAG
     */
    private final static String TAG = LocalProfileManager.class.getSimpleName();

    /**
     * LocalProfileManager
     */
    private static LocalProfileManager instance;

    /**
     * baseProfiles
     */
    private List<BaseProfile> baseProfiles = null;

    private LocalProfileManager()
    {
        baseProfiles = new ArrayList<BaseProfile>();
    }

    public static LocalProfileManager getInstance()
    {
        if (instance == null)
        {
            instance = new LocalProfileManager();
        }
        return instance;
    }

    public List<BaseProfile> getBaseProfiles()
    {
        return baseProfiles;
    }

    /**
     * 增加当前可以连接的Profile
     * @param profile 连接的Profile
     */
    public void addProfile(BaseProfile profile)
    {
        synchronized (baseProfiles)
        {
            if (!baseProfiles.contains(profile))
            {
                baseProfiles.add(profile);
            }
        }
    }

    /**
     * 取消当前可用的Profile
     * @param profile
     */
    public void removeProfile(BaseProfile profile)
    {
        synchronized (baseProfiles)
        {
            baseProfiles.remove(profile);
        }
    }
}
