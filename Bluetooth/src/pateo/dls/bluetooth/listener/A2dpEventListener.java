package pateo.dls.bluetooth.listener;

import pateo.dls.bluetooth.comm.BluetoothMediaInfo;

/**
 * Created by zixiangliu on 14-8-4.
 */
public interface A2dpEventListener
{
    /**
     * 返回当前的Media的信息
     *
     * @param mediaInfo BluetoothMediaInfo
     */
    public void onMediaInfoChange(BluetoothMediaInfo mediaInfo);

    /**
     * 返回当前的播放的时间
     *
     * @param duration duration
     */
    public void onPlaydurationChange(long duration);
}
