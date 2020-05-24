package io.gksoftwaresolutions.medicationreminder.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.gksoftwaresolutions.medicationreminder.MainActivity;
import io.gksoftwaresolutions.medicationreminder.R;
import io.gksoftwaresolutions.medicationreminder.model.BDevice;

public class BluetoothModAdapter extends RecyclerView.Adapter<BluetoothViewHolder> {
    private List<BDevice> deviceList;
    private Context context;

    public BluetoothModAdapter(List<BDevice> deviceList, Context context) {
        this.deviceList = deviceList;
        this.context = context;
    }

    @NonNull
    @Override
    public BluetoothViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth_layout, parent, false);
        return new BluetoothViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothViewHolder holder, int position) {
        BDevice d = deviceList.get(position);
        holder.nameDevice.setText(d.getDeviceName());
        holder.uuidDevice.setText(d.getUuid());
        holder.contentBt.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog)
                    .setTitle("Iniciar conexión")
                    .setMessage("¿Desea iniciar una conexión con el dispositivo " + d.getDeviceName() + "?")
                    .setPositiveButton("Conectar",
                            (dialog, which) -> {
                                Intent connectedDevice = new Intent(context, MainActivity.class);
                                connectedDevice.putExtra("device", d.getUuid());
                                context.startActivity(connectedDevice);
                                ((AppCompatActivity) context).finish();
                                dialog.dismiss();
                            }).setNegativeButton("Cancelar",
                    (dialog, which) -> dialog.dismiss()).create().show();
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}

class BluetoothViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.uuidDevice)
    TextView uuidDevice;
    @BindView(R.id.nameDevice)
    TextView nameDevice;
    @BindView(R.id.contentBt)
    ConstraintLayout contentBt;

    BluetoothViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
