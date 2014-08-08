package pateo.dls.bluetooth.listener;

/**
 * Created by zixiangliu on 14-8-8.
 */
public interface PBAPCallback
{
    void onCallLogItemCountDetermined(int count);

    //    void onCallLogItemFetched(BluetoothCallLog item);

    void onContactItemCountDetermined(int count);

    //    void onContactItemFetched(BluetoothContact item);

    //    void onSyncStateChanged(BluetoothPbap.EventListener.SyncState preState,
    //        BluetoothPbap.EventListener.SyncState state);
}
