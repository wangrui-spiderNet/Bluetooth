package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.util.Util;
import win.lioil.bluetooth.util.Utils;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private Listener mListener;
    private boolean isRead;
    private boolean isSending;

    BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
//            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            InputStream inputStream = mSocket.getInputStream();
            isRead = true;

            byte[] result = new byte[0];
            while (isRead) {

                Log.e(">>>>>>>>>>", "接收到消息");

                byte[] buffer = new byte[256];
                // 等待有数据
                while (inputStream.available() == 0) {
                    if (System.currentTimeMillis() < 0)
                        break;
                }

                int num = inputStream.read(buffer);
                byte[] temp = new byte[result.length + num];
                System.arraycopy(result, 0, temp, 0, result.length);
                System.arraycopy(buffer, 0, temp, result.length, num);
                result = temp;
                notifyUI(Listener.MSG, "接收短消息：" + Utils.bytesToHexString(result));

                Log.e(">>>>>>>>>>", "接收到消息：" + Utils.bytesToHexString(result));
//                while (isRunning()) {
//                    try {
//                        int num = inputStream.read(buffer);
//                        byte[] temp = new byte[result.length + num];
//                        System.arraycopy(result, 0, temp, 0, result.length);
//                        System.arraycopy(buffer, 0, temp, result.length, num);
//                        result = temp;
//                        if (inputStream.available() == 0)
//                            break;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        onBluetoothAction.onConnectFailed(romoteDevice, e.getMessage());
//                        break;
//                    }
//                }
//                try {
//
//                    if (result.length == 0) {
//                        return;
//                    }
//
//                    onBluetoothAction.onReceiveBytes(result);
//
//                    if (modeType == CMDConfig.CMD_TYPE_HL) {
//                        if (msgQueue.get(byte2Hex(result).substring(2, 4)) != null) {
////                                LogUtils.logBlueTooth("已接收到消息，移除");
//                            Runnable runnable = msgQueue.get(byte2Hex(result).substring(2, 4));
//                            mHandler.removeCallbacks(runnable);
//                            msgQueue.remove(runnable);
//                        }
//                    } else if (modeType == CMDConfig.CMD_TYPE_OTA) {
//                        String code = byte2Hex(result).substring(4, 6);
//                        switch (code) {
//                            case CMDConfig.CMD_80:
//                            case CMDConfig.CMD_81:
//                            case CMDConfig.CMD_82:
//                            case CMDConfig.CMD_84:
//                            case CMDConfig.CMD_04:
//                            default:
//                                msgTimeCode = code;
//                                break;
//                            case CMDConfig.CMD_83:
//                                msgTimeCode = byte2Hex(result).substring(12, 20);
//
//                                break;
//
//                        }
//
//                        LogUtils.logBlueTooth("已接收到消息，移除：" + msgTimeCode);
//                        Runnable runnable = msgOtaQueue.get(msgTimeCode);
//                        mHandler.removeCallbacks(runnable);
//                        msgOtaQueue.remove(msgTimeCode);
//
//                        LogUtils.logBlueTooth("消息队列大小：" + msgOtaQueue.keySet().size());
//                    }
//
//                    // 清空
//                    result = new byte[0];
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    onBluetoothAction.onConnectFailed(romoteDevice, e.getMessage());
//                }
            }

//            while (isRead) { //死循环读取
//                switch (in.readInt()) {
//                    case FLAG_MSG: //读取短消息
//                        String msg = in.readUTF();
//                        notifyUI(Listener.MSG, "接收短消息：" + msg);
//                        break;
//                    case FLAG_FILE: //读取文件
//                        Util.mkdirs(FILE_PATH);
//                        String fileName = in.readUTF(); //文件名
//                        long fileLen = in.readLong(); //文件长度
//                        // 读取文件内容
//                        long len = 0;
//                        int r;
//                        byte[] b = new byte[4 * 1024];
//                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
//                        notifyUI(Listener.MSG, "正在接收文件(" + fileName + "),请稍后...");
//                        while ((r = in.read(b)) != -1) {
//                            out.write(b, 0, r);
//                            len += r;
//                            if (len >= fileLen)
//                                break;
//                        }
//                        notifyUI(Listener.MSG, "文件接收完成(存放在:" + FILE_PATH + ")");
//                        break;
//                }
//            }
        } catch (Throwable e) {
            close();
        }
    }

    /**
     * 发送短消息
     */
    public void sendBytes(byte[] msg) {
        if (checkSend()) return;
        isSending = true;
        try {
//            mSocket.getOutputStream().write(FLAG_MSG); //消息标记
            mSocket.getOutputStream().write(msg);
            mSocket.getOutputStream().flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String filePath) {
        if (checkSend()) return;
        isSending = true;
        Util.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    notifyUI(Listener.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    while ((r = in.read(b)) != -1)
                        mOut.write(b, 0, r);
                    mOut.flush();
                    notifyUI(Listener.MSG, "文件发送完成.");
                } catch (Throwable e) {
                    close();
                }
                isSending = false;
            }
        });
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
    public void close() {
        try {
            isRead = false;
            mSocket.close();
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
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
