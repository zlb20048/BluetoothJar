package pateo.dls.bluetooth.listener;

/**
 * Created by zixiangliu on 14-8-4.
 */
public interface HesetEventListener
{
    /**
     * audioEstablelished
     */
    public void audioEstableished();

    /**
     * audio released
     */
    public void audioRelased();

    /**
     * 来电
     */
    public void inComingCall();

    /**
     * 去电
     */
    public void outGoingCall();

    /**
     * 正在通话
     */
    public void onGoingCall();

    public void serviceEstablished();

    public void serviceReleased();

    /**
     * 挂断电话
     */
    public void standby();
}
