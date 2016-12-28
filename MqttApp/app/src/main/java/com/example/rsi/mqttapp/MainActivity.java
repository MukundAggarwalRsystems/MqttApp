package com.example.rsi.mqttapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Calendar;

import static android.R.id.progress;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    private static SeekBar seekbar_led1;
    private static TextView textview_led1;

    private static EditText PubMsg;
    private static TextView textView_ledcolor;

    static String MQTTHOST = "tcp://m13.cloudmqtt.com:10945";
    static String USERNAME = "rsystems";
    static String PASSWORD = "rsystems";
    String topicStr = "andtest";

    MqttAndroidClient client;
    TextView textView_subtetext;
    MqttConnectOptions options;

    boolean Connect_status;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_subtetext = (TextView) findViewById(R.id.textView_subtetext);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                clientId);
        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        /************** Notification *****************/

        /*********************************************/

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    setSubsciption();
                    Connect_status = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                    Connect_status = false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

                textView_subtetext.setText(new String(message.getPayload()));
                vibrator.vibrate(500);
                showNotification(topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        seekbar();
    }

    public void showNotification(String topic, String messageString){

        Calendar.getInstance().getTime().toString();
        long when = System.currentTimeMillis();

        //the message that will be displayed as the ticker
        String ticker = topic + " " + messageString;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(topic);
        builder.setContentText(messageString);
        builder.setWhen(when);
        //builder.setLights(0xFFFFFF,5000,5000);
        builder.setTicker(ticker);

        Intent intent = new Intent(this, Notify.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Notify.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //builder.setSound(uri);
        //vibrate
        //long[] v = {500,1000,500,1000};
        //builder.setVibrate(v);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());
    }

    public void seekbar(){
        seekbar_led1 = (SeekBar)findViewById(R.id.seekBar_Led1);
        textview_led1 = (TextView)findViewById(R.id.textView_Led1);

        seekbar_led1.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    String topic = topicStr;
                    int progress_value;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        textview_led1.setText(" " + progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        textview_led1.setText(" " + progress_value);

                        String message = Integer.toString(progress_value);

                        try {
                            client.publish(topic, message.getBytes(), 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void pub(View v) {
        String topic = topicStr;

        PubMsg = (EditText) findViewById(R.id.editText_pub);
        String message = PubMsg.getText().toString();
        if(Connect_status == true) {
            try {
                client.publish(topic, message.getBytes(), 0, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
        }
    }

    public void pub_LedOn(View v) {
        String topic = topicStr;
        String message = "1Led On";
        textView_ledcolor = (TextView)findViewById(R.id.textView_ledcolor);

        if(Connect_status == true) {
            try {
                client.publish(topic, message.getBytes(), 0, false);
                textView_ledcolor.setBackgroundColor(Color.parseColor("#CC0000"));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
        }
    }

    public void pub_LedOff(View v) {
        String topic = topicStr;
        String message = "0Led Off";
        textView_ledcolor = (TextView) findViewById(R.id.textView_ledcolor);

        if (Connect_status = true){
            try {
                client.publish(topic, message.getBytes(), 0, false);
                textView_ledcolor.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else{
                Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
            }
    }
    private void setSubsciption() {
        try {
            client.subscribe(topicStr, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void conn(View v) {
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    setSubsciption();
                    Connect_status = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconn(View v) {
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "DisConnected", Toast.LENGTH_LONG).show();
                    Connect_status = false;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Could Not DisConnected", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
