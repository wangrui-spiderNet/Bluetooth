package win.lioil.bluetooth.bt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.util.BtReceiver;
import win.lioil.bluetooth.util.BtUtil;

public class BtClientActivity extends Activity implements BtReceiver.Listener, BtDevAdapter.Listener, View.OnClickListener {
    private TextView mTips;
    private TextView mLogs;
    private Button btnShake, btnVerify;
    private BtReceiver mBtReceiver;

    private String TAG = BtClientActivity.class.getSimpleName();

    private final BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);
    private final BtClient mClient1 = new BtClient(1, new BtBase.Listener() {
        @Override
        public void socketNotify(int state, Object obj) {
//            Log.e(TAG, "mClient1 状态: " + state);

            if (state == MSG) {
                Log.e(TAG, "mClient1消息: " + obj);
            }

            notifyMsg(state, obj);
        }

    });

    private void notifyMsg(int state, Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                mLogs.append(msg);
                break;
        }
        APP.toast(msg, 0);
    }

    private final BtClient mClient2 = new BtClient(2, new BtBase.Listener() {
        @Override
        public void socketNotify(int state, Object obj) {
//            Log.e(TAG, "mClient2 状态: " + state);

            if (state == MSG) {
                Log.e(TAG, "mClient2消息: " + obj);
            }

            notifyMsg(state, obj);
        }
    });

    private final BtClient mClient3 = new BtClient(3, new BtBase.Listener() {
        @Override
        public void socketNotify(int state, Object obj) {
//            Log.e(TAG, "mClient3 状态: " + state);

            if (state == MSG) {
                Log.e(TAG, "mClient3消息: " + obj);
            }
            notifyMsg(state, obj);
        }
    });

    private List<BtClient> mBtClients = new ArrayList<>();
    private Map<String, BtClient> mClientMap = new HashMap<>();


    @Override
    public void onClick(View view) {
        if (view.equals(btnShake)) {
            sendMsg(BtUtil.hexStringToByteArray("3000010000004E000000000040DA00000100FFFFFFFF0300000130303031003D0038573954513668624D4B6C56594D5A4C7A6C4F6B4C5A72376245674B466F3837335A634C384C59734A75494B5042547A386B696F564658796634544142"));
        } else if (view.equals(btnVerify)) {
            sendMsg(BtUtil.hexStringToByteArray("300201000000510000000000605700000400FFFFFFFF0300000130303032004000496571394767716237356939456249652D0CBDC39F11EE9B3B889BF4F2C0DD0E05F7CBD3234B7A0AD20F18BD5D84446254656E4E6E68766F6454614C4B776458"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);
        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mBtDevAdapter);
        mTips = findViewById(R.id.tv_tips);
        mLogs = findViewById(R.id.tv_log);

        btnShake = findViewById(R.id.btn_shake);
        btnVerify = findViewById(R.id.btn_verify);
        btnShake.setOnClickListener(this);
        btnVerify.setOnClickListener(this);

        mBtReceiver = new BtReceiver(this, this);//注册蓝牙广播

        mBtClients.add(mClient1);
        mBtClients.add(mClient2);
        mBtClients.add(mClient3);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }

            return;
        }

        BluetoothAdapter.getDefaultAdapter().startDiscovery();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);

        for (BtClient btClient : mBtClients) {
            btClient.unListener();
            btClient.close(btClient.mId);
        }
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {

        for (BtClient btClient : mBtClients) {
            if (!btClient.isInUse) {
                btClient.isInUse = true;
                if (btClient.isConnected(btClient.mId, dev)) {
                    APP.toast("已经连接了", 0);
                    return;
                }

                mClientMap.put(dev.getAddress(), btClient);

                Log.e(TAG, btClient.mId + "-发起连接");
                btClient.connect(btClient.mId, dev);

                break;
            }
        }

        APP.toast("正在连接...", 0);
        mTips.setText("正在连接...");
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
//        mBtDevAdapter.add(dev);
    }

    // 重新扫描
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }

    public void sendMsg(byte[] msgs) {

        for (BtClient client : mBtClients) {

            Log.e(TAG, client.mId + "-BtEngin状态:" + client.isInUse);

            if (client.isInUse) {
//                String msg = mInputMsg.getText().toString();
//                if (TextUtils.isEmpty(msg))
//                    APP.toast("消息不能空", 0);
//                else
                Log.e(TAG, client.mId + "-发送消息");

                client.sendMsg(client.mId, msgs);

            } else
                APP.toast("没有连接", 0);
        }
    }

}