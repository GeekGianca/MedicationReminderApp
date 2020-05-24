package io.gksoftwaresolutions.medicationreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.gksoftwaresolutions.medicationreminder.adapter.BluetoothModAdapter;
import io.gksoftwaresolutions.medicationreminder.model.BDevice;

public class BluetoothActivity extends AppCompatActivity {

    @BindView(R.id.stateOnOff)
    TextView stateOnOff;
    @BindView(R.id.bluetoothList)
    RecyclerView bluetoothList;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothModAdapter modAdapter;
    private List<BDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);
        bluetoothList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        bluetoothList.setLayoutManager(manager);
        deviceList = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            stateOnOff.setText("No soportado");
            stateOnOff.setTextColor(getResources().getColor(R.color.colorError));
            Toast.makeText(this, "El dispositivo no soporta el Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBt, REQUEST_ENABLE_BT);
            }else{
                stateOnOff.setText("Encendido");
                stateOnOff.setTextColor(getResources().getColor(R.color.colorSuccess));
                loadListDevices();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth Encendido", Toast.LENGTH_SHORT).show();
            loadListDevices();
            stateOnOff.setText("Encendido");
            stateOnOff.setTextColor(getResources().getColor(R.color.colorSuccess));
        } else {
            stateOnOff.setText("Apagado");
            stateOnOff.setTextColor(getResources().getColor(R.color.colorError));
            Toast.makeText(this, "No se Activo el Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.backButton)
    void back() {
        finish();
    }

    private void loadListDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BDevice dev = new BDevice(device.getAddress(), device.getName(), 0);
                deviceList.add(dev);
            }
        }
        modAdapter = new BluetoothModAdapter(deviceList, this);
        bluetoothList.setAdapter(modAdapter);
        modAdapter.notifyDataSetChanged();
    }
}
