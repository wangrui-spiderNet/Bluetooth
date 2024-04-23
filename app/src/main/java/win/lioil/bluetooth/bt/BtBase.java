package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.util.BtUtil;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";

    Map<Integer, BluetoothSocket> mSocketMap = new HashMap<>();
    Map<Integer, DataOutputStream> mOutStreamMap = new HashMap<>();
    Map<Integer, DataInputStream> mInStreamMap = new HashMap<>();

    //    private DataOutputStream mOut;
    private Listener mListener;
    private boolean isRunning;
    private boolean isSending;
    int mId = 0;
    private final String TAG = "BtBase";

    BtBase(int id, Listener listener) {
        mListener = listener;
        mId = id;
        Log.e(TAG, "创建BtClient ID:" + id);
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(int id) {
        BluetoothSocket socket = mSocketMap.get(id);
        Log.e(TAG, "使用Socket:" + id + socket);

        try {
            if (!socket.isConnected())
                socket.connect();

            notifyUI(Listener.CONNECTED, socket.getRemoteDevice());
            DataOutputStream mOut = new DataOutputStream(socket.getOutputStream());
            mOutStreamMap.put(id, mOut);
            mInStreamMap.put(id, new DataInputStream(socket.getInputStream()));

            isRunning = true;

            while (isRunning) {
                byte[] buffer = new byte[256];
                byte[] result = new byte[0];

                // 等待有数据
                while (mInStreamMap.get(id).available() == 0) {
//                    if (System.currentTimeMillis() < 0 || mSocket == null || !mSocket.isConnected()) {
//                        notifyUI(BtStateListener.ON_SOCKET_CLOSE, null);
//                        break;
//                    }
                }

                while (isRunning) {//循环读取
                    try {
                        int num = mInStreamMap.get(id).read(buffer);
//                            logD(TAG,"容许最大长度Transmit:" + socket.getMaxTransmitPacketSize());
                        byte[] temp = new byte[result.length + num];
                        System.arraycopy(result, 0, temp, 0, result.length);
                        System.arraycopy(buffer, 0, temp, result.length, num);
                        result = temp;

                        notifyUI(Listener.MSG, id + "-返回消息:" + BtUtil.bytesToHexString(result));
//                        Log.e(TAG, id + "-返回消息：" + BtUtil.bytesToHexString(result));

                        if (mInStreamMap.get(id).available() == 0)
                            break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
//                        notifyErrorOnUI("1-" + e.getMessage());
                        break;
                    }
                }

                try {

//                    if (result.length == 0) {
//                        return;
//                    }
//
//                    // 清空
//                    result = new byte[0];
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyUI(-1, "2-" + e.getMessage());
                }
            }

        } catch (Throwable e) {
            close(id);
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(int id, byte[] byteArray) {
//        if (checkSend()) return;
        isSending = true;
        try {
            mOutStreamMap.get(id).write(byteArray);
            mOutStreamMap.get(id).flush();
//            notifyUI(Listener.MSG, id + "-发送消息：" + BtUtil.bytesToHexString(byteArray));

            Log.e(TAG, "发送消息：" + BtUtil.bytesToHexString(byteArray));

        } catch (Throwable e) {
            Log.e(TAG, "异常消息：" + e.getMessage());
            close(id);
        }
        isSending = false;
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close(int id) {
        try {
            isRunning = false;
            mSocketMap.get(id).close();
            Log.e(TAG, "close:" + id);
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(int id, BluetoothDevice dev) {
        boolean connected = (mSocketMap.get(id) != null && mSocketMap.get(id).isConnected());
        if (dev == null)
            return connected;
        return connected && mSocketMap.get(id).getRemoteDevice().equals(dev);
    }

    // ============================================通知UI===========================================================
    private boolean checkSend() {
        if (isSending) {
            APP.toast("正在发送其它数据,请稍后再发...", 0);
            return true;
        }
        return false;
    }

    private void notifyUI(final int state, final Object obj) {
        APP.runUi(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null)
                        mListener.socketNotify(state, obj);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;

        void socketNotify(int state, Object obj);
    }
}
