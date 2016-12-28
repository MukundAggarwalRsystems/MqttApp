package com.example.rsi.mqttcolorpick;

import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static Button button_chooseColor;
    private static TextView textView_colorcode;
    private static TextView textView_red;
    private static TextView textView_green;
    private static TextView textView_blue;
    private static TextView textView_ack;
    RelativeLayout layout;

    ColorPickerDialog colorPickerDialog;
    public int color = Color.parseColor("#33b5e5");

    /************* MQTT ******************/
    static String MQTTHOST = "tcp://m13.cloudmqtt.com:10945";
    static String USERNAME = "rsystems";
    static String PASSWORD = "rsystems";
    String topic_red = "home/red";
    String topic_green = "home/green";
    String topic_blue = "home/blue";
    String topic_ack = "andtest";

    MqttAndroidClient client;
    MqttConnectOptions options;
    /**************************************/
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_colorcode = (TextView)findViewById(R.id.textView_colorcode);
        textView_red = (TextView)findViewById(R.id.textView_red);
        textView_green = (TextView)findViewById(R.id.textView_green);
        textView_blue = (TextView)findViewById(R.id.textView_blue);
        textView_ack = (TextView)findViewById(R.id.textView_ack);

        layout = (RelativeLayout) findViewById(R.id.layout);

        button_chooseColor = (Button)findViewById(R.id.button_chooseColor);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST,
                clientId);
        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    setSubsciption();
                    //Connect_status = true;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                    //Connect_status = false;
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

                textView_ack.setText(new String(message.getPayload()));
                vibrator.vibrate(500);
                //showNotification(topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    private void setSubsciption() {
        try {
            client.subscribe(topic_ack, 0);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void colorpick(View v){
        colorPickerDialog = new ColorPickerDialog(MainActivity.this, this.color);
        colorPickerDialog.setAlphaSliderVisible(true);
        colorPickerDialog.setHexValueEnabled(true);
        colorPickerDialog.setTitle("Color Choosed :");
        colorPickerDialog.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {
                color = i;
                layout.setBackgroundColor(color);
                textView_colorcode.setText("#" + Integer.toString(color));
                int red=   (color >> 16) & 0xFF;
                int green= (color >> 8) & 0xFF;
                int blue=  (color >> 0) & 0xFF;
                textView_red.setText("" + red);
                textView_green.setText("" + green);
                textView_blue.setText("" + blue);
                pub_red(red);
                pub_green(green);
                pub_blue(blue);

            }
        });
        colorPickerDialog.show();
    }

    public void pub_red(int red_color) {
        String topic = topic_red;
        String message = Integer.toString(red_color);

        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void pub_green(int green_color) {
        String topic = topic_green;
        String message = Integer.toString(green_color);

        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void pub_blue(int blue_color) {
        String topic = topic_blue;
        String message = Integer.toString(blue_color);

        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void pub() {
        String topic = topic_ack;

        String message = "Connected to Mqtt Color Pick App";
        try {
            client.publish(topic, message.getBytes(), 0, false);
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
                    pub();
                    setSubsciption();
                    //Connect_status = true;
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
}
