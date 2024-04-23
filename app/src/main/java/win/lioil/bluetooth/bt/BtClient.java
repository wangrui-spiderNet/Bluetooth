package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import win.lioil.bluetooth.util.Util;

/**
 * 客户端，与服务端建立长连接
 */
public class BtClient extends BtBase {
    public boolean isInUse = false;

    private String TAG = "BtClient";

    BtClient(int id, Listener listener) {
        super(id, listener);
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(int id, BluetoothDevice dev) {
        close(id);
        try {
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            Log.e(TAG, "创建Socket:" + socket);
            mSocketMap.put(id, socket);
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(id); //循环读取
                }
            });
        } catch (Throwable e) {
            close(id);
        }
    }
}