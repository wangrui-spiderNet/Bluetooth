package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙活动回调
 */
public interface OnBluetoothAction {
    /**
     * 当发现新设备
     *
     * @param device 设备
     */
    void onFoundDevice(BluetoothDevice device);

    /**
     * 当连接成功
     */
    void onConnectSuccess(BluetoothDevice device);

    /**
     * 握手通过
     */
    void onMsgTimeOut(byte[] msg);

    /**
     * 当连接成功
     */
    void onNewDeviceConnect(BluetoothDevice device);

    /**
     * 状态变化
     *
     * @param device
     */
    void onStateChanged(BluetoothDevice device);

    /**
     * 断开连接
     *
     * @param device
     */
    void onDisconnected(BluetoothDevice device);

    /**
     * 当连接失败
     *
     * @param msg 失败信息
     */
    void onConnectFailed(BluetoothDevice romoteDevice, String msg);

    /**
     * 当接收到 byte 数组
     *
     * @param bytes 内容
     */
    void onReceiveBytes(byte[] bytes);

    /**
     * 当调用接口发送了 byte 数组
     *
     * @param bytes 内容
     */
    void onSendBytes(byte[] bytes);

    void onSocketFail();

}