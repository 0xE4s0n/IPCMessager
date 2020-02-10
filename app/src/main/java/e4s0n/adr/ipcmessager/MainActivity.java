package e4s0n.adr.ipcmessager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button bu;
    private EditText ed;
    private TextView tv;
    private final static int MES_FROM_CLIENT = 0;
    private static final int MES_FROM_SERVER = 1;
    private StringBuilder sb;
    private Messenger messenger;
    private final Messenger getReplyMessenger = new Messenger(new MessengerHandler());

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case MES_FROM_SERVER:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sb.append(msg.getData().getString("client")+"\n");
                            tv.setText(sb.toString());
                        }
                    });
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);
            Message msg = Message.obtain(null,MES_FROM_CLIENT);
            Bundle bundle = new Bundle();
            bundle.putString("service","Client connected!");
            msg.setData(bundle);
            msg.replyTo = getReplyMessenger;
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            bu.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bu = findViewById(R.id.button);
        bu.setEnabled(false);
        ed = findViewById(R.id.editText);
        tv = findViewById(R.id.textView);
        bu.setOnClickListener(this);
        sb = new StringBuilder();
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("e4s0n.adr.ipcmessager_server","e4s0n.adr.ipcmessager_server.MessagerService");
        intent.setComponent(componentName);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        String str = ed.getText().toString();
        sb.append(str+"\n");
        tv.setText(sb.toString());
        ed.setText("");
        Message msg = Message.obtain(null,MES_FROM_CLIENT);
        Bundle bundle = new Bundle();
        bundle.putString("service",str);
        msg.setData(bundle);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
