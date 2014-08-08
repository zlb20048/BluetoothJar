package pateo.dls.bluetooth.comm;

import android.content.Context;
import pateo.frameworks.api.XmlOperator;

/**
 * Created by zixiangliu on 14-8-5.
 */
public class BluetoothDefalutValue
{
    /**
     * TAG
     */
    private final static String TAG =
        BluetoothDefalutValue.class.getSimpleName();

    /**
     * BLUETOOTH_DEFAULT_PATH
     */
    private final static String BLUETOOTH_DEFAULT_PATH =
        "bluetooth_defalut_value";

    /**
     * 蓝牙名称
     */
    private final static String BLUETOOTH_NAME = "bluetooth_name";

    /**
     * 蓝牙的Pincde
     */
    private final static String BLUETOOTH_PIN_CODE = "bluetooth_pin_code";

    /**
     * 蓝牙的状态
     */
    private final static String BLUETOOTH_STATE = "bluetooth_state";

    /**
     * 默认的pincode
     */
    private final static String BLUETOOTH_PIN_CODE_DEFALUT = "0000";

    /**
     * 默认的蓝牙设备的名称
     */
    private final static String BLUETOOTH_NAME_DEFALUT = "Astorid";

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * XmlOperator
     */
    private XmlOperator mDataInterface;

    /**
     * 构造方法
     *
     * @param context context
     */
    public BluetoothDefalutValue(Context context)
    {
        mContext = context;
        mDataInterface = new XmlOperator(mContext, BLUETOOTH_DEFAULT_PATH);
        mDataInterface.Create();
    }

    /**
     * 保存蓝牙的名称
     *
     * @param name 蓝牙的名称
     */
    public void saveBluetoothName(String name)
    {
        mDataInterface.WriteString(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_NAME,
            name);
    }

    /**
     * 获取蓝牙的名称
     *
     * @return 当前的蓝牙的名称
     */
    public String getBluetoothName()
    {
        return mDataInterface.ReadString(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_NAME,
            BLUETOOTH_NAME_DEFALUT);
    }

    /**
     * 保存BluetoothPinCode
     *
     * @param pincode
     */
    public void saveBluetoothPinCode(String pincode)
    {
        mDataInterface.WriteString(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_PIN_CODE,
            pincode);
    }

    /**
     * 获取蓝牙PinCode
     *
     * @return 当前的蓝牙的PinCode
     */
    public String getBluetoothPinCode()
    {
        return mDataInterface.ReadString(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_PIN_CODE,
            BLUETOOTH_PIN_CODE_DEFALUT);
    }

    /**
     * 保存BluetoothPinCode
     *
     * @param state
     */
    public void saveBluetoothState(BluetoothState state)
    {
        mDataInterface.WriteInt(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_STATE,
            state.ordinal());
    }

    /**
     * 获取蓝牙状态
     *
     * @return 当前的蓝牙的状态
     */
    public BluetoothState getBluetoothState()
    {
        return convert(mDataInterface.ReadInt(BLUETOOTH_DEFAULT_PATH,
            BLUETOOTH_STATE,
            BluetoothState.BLUETOOTH_ON.ordinal()));
    }

    /**
     * 蓝牙的各种状态
     */
    public enum BluetoothState
    {
        BLUETOOTH_ON, BLUETOOTH_OFF, BLUETOOTH_TURN_ON, BLUETOOTH_TURN_OFF, BLUETOOTH_ERROR
    }

    /**
     * 转换蓝牙的状态
     * @param state 当前的状态
     * @return 当前的状态
     */
    private BluetoothState convert(int state)
    {
        if (state == 0)
        {
            return BluetoothState.BLUETOOTH_ON;
        }
        else if (state == 1)
        {
            return BluetoothState.BLUETOOTH_OFF;
        }
        else if (state == 2)
        {
            return BluetoothState.BLUETOOTH_TURN_ON;
        }
        else if (state == 3)
        {
            return BluetoothState.BLUETOOTH_TURN_OFF;
        }
        else
        {
            return BluetoothState.BLUETOOTH_ERROR;
        }
    }
}
