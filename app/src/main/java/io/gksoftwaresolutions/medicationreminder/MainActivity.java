package io.gksoftwaresolutions.medicationreminder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.gksoftwaresolutions.medicationreminder.adapter.ReminderAdapter;
import io.gksoftwaresolutions.medicationreminder.model.Pill;
import io.gksoftwaresolutions.medicationreminder.thread.BluetoothConnectionSender;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.contentReminders)
    RecyclerView contentReminders;

    private TextInputLayout layoutNamePill;
    private TextInputEditText productNameInput;
    private TextInputLayout layoutDatePill;
    private TextInputEditText inputDatePill;
    private TextInputLayout layoutTimePill;
    private TextInputEditText inputTimePill;
    private Button selectDate;
    private Button selectTime;
    private AlertDialog mAlert;
    private MaterialDatePicker.Builder<Long> builderPick;
    private MaterialDatePicker<Long> picker;
    private TimePickerDialog timePickerDialog;
    private List<Pill> pills;
    private String deviceToConnect;
    private ProgressDialog mDialog;
    private ReminderAdapter adapterRem;

    Handler handlerBluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private StringBuilder dataStringInput = new StringBuilder();
    private BluetoothConnectionSender connectedThread;
    private static final UUID UUIDBT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Paper.init(this);
        toolbar.setTitle(getString(R.string.label_string_title));
        setSupportActionBar(toolbar);
        contentReminders.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        contentReminders.setLayoutManager(manager);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Long today = MaterialDatePicker.todayInUtcMilliseconds();
        builderPick = MaterialDatePicker.Builder.datePicker();
        builderPick.setTitleText(getString(R.string.label_string_title_pick));
        builderPick.setSelection(today);
        picker = builderPick.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            TimeZone timeZoneUTC = TimeZone.getDefault();
            // It will be negative, so that's the -1
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            // Create a date format, then a date object with our offset
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date(selection + offsetFromUTC);
            Log.d("Date Selected", "Date: " + date.toString());
            inputDatePill.setText(simpleFormat.format(date));
        });
        Dexter.withContext(this)
                .withPermission(Manifest.permission.BLUETOOTH)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        Toast.makeText(MainActivity.this, "Permisos de Bluetooth garantizados", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

        if (getIntent().getExtras() != null) {
            deviceToConnect = getIntent().getExtras().getString("device");
            if (deviceToConnect != null) {
                Paper.book().write("deviceConnect", deviceToConnect);
            } else {
                deviceToConnect = Paper.book().read("deviceConnect");
            }
        } else {
            deviceToConnect = Paper.book().read("deviceConnect");
        }

        handlerBluetoothIn = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    dataStringInput.append(readMessage);
                    int endOfLineIndex = dataStringInput.indexOf("#");
                    if (endOfLineIndex > 0) {
                        String inputData = dataStringInput.substring(0, endOfLineIndex);
                        Log.d("Data", inputData);
                        dataStringInput.delete(0, dataStringInput.length());
                    }
                }
            }
        };
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkIfBtIsOn();
    }

    private void checkIfBtIsOn() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no soporta el Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enablebt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enablebt, REQUEST_ENABLE_BT);
            } else {
                Log.d("ONLINE", "=======================");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_alarm:
                if (deviceToConnect != null && bluetoothSocket != null) {
                    View itemView = getLayoutInflater().inflate(R.layout.add_reminder_layout, null, false);
                    layoutNamePill = itemView.findViewById(R.id.layoutNamePill);
                    productNameInput = itemView.findViewById(R.id.productNameInput);
                    layoutDatePill = itemView.findViewById(R.id.layoutDatePill);
                    inputDatePill = itemView.findViewById(R.id.inputDatePill);
                    layoutTimePill = itemView.findViewById(R.id.layoutTimePill);
                    inputTimePill = itemView.findViewById(R.id.inputTimePill);
                    selectDate = itemView.findViewById(R.id.selectDate);
                    selectTime = itemView.findViewById(R.id.selectTime);

                    selectDate.setOnClickListener(v -> picker.showNow(getSupportFragmentManager(), "DATE_PICKER"));
                    selectTime.setOnClickListener(v -> {
                        Calendar mCurrentTime = Calendar.getInstance();
                        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
                        int minute = mCurrentTime.get(Calendar.MINUTE);
                        timePickerDialog = new TimePickerDialog(MainActivity.this, (view, hourOfDay, minute1) -> inputTimePill.setText(String.format("%s:%s", hourOfDay, minute1)), hour, minute, true);
                        timePickerDialog.setTitle("Selecciona la hora");
                        timePickerDialog.show();
                    });
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog)
                            .setMessage("Registra tus datos de tu recordatorio")
                            .setPositiveButton("Aceptar", null);
                    builder.setView(itemView);
                    mAlert = builder.create();
                    mAlert.setOnShowListener(dialog -> {
                        Button accept = ((AlertDialog) dialog).getButton(dialog.BUTTON_POSITIVE);
                        accept.setOnClickListener(v -> {
                            if (productNameInput.getText().toString().isEmpty()) {
                                layoutNamePill.setError("Ingrese un nombre para el medicamento.");
                                return;
                            } else {
                                layoutNamePill.setError(null);
                            }
                            if (inputDatePill.getText().toString().isEmpty()) {
                                layoutDatePill.setError("Debe ingresar una fecha");
                                return;
                            } else {
                                layoutDatePill.setError(null);
                            }
                            if (inputTimePill.getText().toString().isEmpty()) {
                                layoutTimePill.setError("Debe ingresar una hora.");
                                return;
                            } else {
                                layoutTimePill.setError(null);
                            }
                            Pill pill = new Pill();
                            pill.setDate(inputDatePill.getText().toString());
                            pill.setTime(inputTimePill.getText().toString());
                            pill.setName(productNameInput.getText().toString());
                            pill.setState("Pendiente");
                            connectedThread.write(String.format("%s#", pill.toString()));
                            pills.add(pill);
                            Paper.book().write("pills", pills);
                            Toast.makeText(MainActivity.this, "Agregado correctamente.", Toast.LENGTH_SHORT).show();
                            mAlert.dismiss();
                            reloadList();
                        });
                    });
                    mAlert.show();
                } else {
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog)
                            .setTitle("Sin Bluetooth disponible")
                            .setMessage("Debe seleccionar un dispositivo bluetooth y adicional, debe verificar que tiene el dispositivo de bluetooth encendido.")
                            .setPositiveButton("Aceptar", (dialog, which) -> {
                                checkIfBtIsOn();
                                dialog.dismiss();
                            })
                            .create()
                            .show();
                }
                break;
            case R.id.connect_device:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadList() {
        pills = Paper.book().read("pills");
        adapterRem = new ReminderAdapter(pills, this);
        contentReminders.setAdapter(adapterRem);
        adapterRem.notifyDataSetChanged();
    }

    private BluetoothSocket createConnectionSecure(BluetoothDevice bDevice) throws IOException {
        return bDevice.createRfcommSocketToServiceRecord(UUIDBT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Paper.book().contains("pills")) {
            reloadList();
        } else {
            pills = new ArrayList<>();
            Paper.book().write("pills", pills);
            reloadList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceToConnect != null) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceToConnect);
            try {
                bluetoothSocket = createConnectionSecure(device);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                connectedThread = new BluetoothConnectionSender(bluetoothSocket, handlerBluetoothIn, this, handlerState);
                connectedThread.start();
            } catch (IOException e) {
                Log.e("IOException onResume", e.toString());
                Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (bluetoothSocket != null)
                bluetoothSocket.close();
            Log.d("onPause", "Connection Close");
        } catch (IOException e) {
            Log.d("IOException On Pause", e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null)
                bluetoothSocket.close();
            Log.d("BTSocket", "Connection Close");
        } catch (IOException e) {
            Log.d("IOException on Destroy", e.toString());
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Necesita los permisos");
        builder.setMessage("Para poder realizar los registros adecuadamente, debe facilitar el acceso del dispositivo al modulo de bluetooth.");
        builder.setPositiveButton("Ir a configuracion", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}
