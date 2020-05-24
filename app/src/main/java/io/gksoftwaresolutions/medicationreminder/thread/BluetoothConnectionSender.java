package io.gksoftwaresolutions.medicationreminder.thread;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BluetoothConnectionSender extends Thread {
    private static final String TAG = "BluetoothConnectionSend";
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler handlerBluetoothIn;
    private Context context;
    private int handlerState;

    public BluetoothConnectionSender(BluetoothSocket socket, Handler handler, Context context, int state) {
        Log.d(TAG, "Init Thread Connection");
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        handlerBluetoothIn = handler;
        this.context = context;
        handlerState = state;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        // Se mantiene en modo escucha para determinar el ingreso de datos
        while (true) {
            try {
                bytes = mmInStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                // Envia los datos obtenidos hacia el evento via handler
                handlerBluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
            } catch (IOException e) {
                Log.d("Exception in thread", e.toString());
                break;
            }
        }
    }

    //Envio de trama
    public void write(String input) {
        try {
            Log.d(TAG, "Size input: " + input.getBytes().length);
            mmOutStream.write(input.getBytes());
        } catch (IOException e) {
            //si no es posible enviar datos se cierra la conexión
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(context, "La Conexión fallo", Toast.LENGTH_LONG).show();
        }
    }
}
