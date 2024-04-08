package win.lioil.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import win.lioil.bluetooth.ble.BleClientActivity;
import win.lioil.bluetooth.ble.BleServerActivity;
import win.lioil.bluetooth.bt.BtClientActivity;
import win.lioil.bluetooth.bt.BtServerActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查蓝牙开关
//        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter  adapter = mBluetoothManager.getAdapter();
        if (adapter == null) {
            APP.toast("本机没有找到蓝牙硬件或驱动！", 0);
            finish();
            return;
        } else {
            if (!adapter.isEnabled()) {
                //直接开启蓝牙
                adapter.enable();
                //跳转到设置界面
                //startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 112);
            }
        }

        // 检查是否支持BLE蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            APP.toast("本机不支持低功耗蓝牙！", 0);
            finish();
            return;
        }

        // Android 6.0动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            for (String str : permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 111);
                    break;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
            }
        }
    }

    public void btClient(View view) {
        startActivity(new Intent(this, BtClientActivity.class));
    }

    public void btServer(View view) {
        startActivity(new Intent(this, BtServerActivity.class));
    }

    public void bleClient(View view) {
        startActivity(new Intent(this, BleClientActivity.class));
    }

    public void bleServer(View view) {
        startActivity(new Intent(this, BleServerActivity.class));
    }
}
