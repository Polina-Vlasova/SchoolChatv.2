package vlasova.school.by.schoolchatv2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import io.socket.client.IO;
import io.socket.emitter.Emitter;

public class MainActivity2 extends AppCompatActivity {

    LinearLayout ll;
    ScrollView sv;
    Context context;
    String nickname;
    io.socket.client.Socket socket;
    ImageButton send;
    EditText message;
    int numUsers;
    ArrayList<String> users;
    ArrayList<Integer> colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        try {
            socket = IO.socket("http://46.101.96.234");
        } catch (URISyntaxException e) {
            addServerMessage("Connection Error");
        }
        users = new ArrayList<>();
        colors = new ArrayList<>();
        ll = (LinearLayout) findViewById(R.id.linearLayout);
        sv = (ScrollView) findViewById(R.id.scrollView);
        context = getApplicationContext();
        nickname = getIntent().getStringExtra("nickname");
        send = (ImageButton) findViewById(R.id.imageButton);
        message = (EditText) findViewById(R.id.editText);
        socket.connect();
        addServerMessage("Welcome to Socket.IO Chat –");
        socket.emit("add user", nickname.trim());
        socket.on("login", onLogin);
        socket.on("new message", onNewMessage);
        socket.on("user joined", onUserJoined);
        socket.on("user left", onUserLeft);
        socket.on("typing", onTyping);
        socket.on("stop typing", onStopTyping);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("new message", message.getText().toString().trim());
                add(message.getText().toString(), nickname);
                message.setText("");
            }
        });


    }
    Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    TextView newMessage = new TextView(context);
                    newMessage.setText("is typing");
                    newMessage.setTextSize(15);
                    newMessage.setTextColor(Color.GRAY);
                    TextView user = new TextView(context);
                    user.setText(username + "  ");
                    user.setTextSize(20);

                    if(users.indexOf(username) != -1){
                        user.setTextColor(colors.get(users.indexOf(username)));
                    } else {
                        Random rnd = new Random();
                        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                        colors.add(color);
                        users.add(username);
                        user.setTextColor(color);
                    }

                    LinearLayout newLayout = new LinearLayout(context);
                    newLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    newLayout.setLayoutParams(lp);
                    newLayout.addView(user);
                    newLayout.addView(newMessage);
                    newLayout.setGravity(Gravity.CENTER_VERTICAL);
                    newLayout.setId(R.id.typing);
                    ll.addView(newLayout);
                    sv.fullScroll(ScrollView.FOCUS_DOWN);
                }

            });
        }
    };
    Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ll.removeView(findViewById(R.id.typing));
                }

            });
        }
    };
    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String mess;
                    try {
                        username = data.getString("username");
                        mess = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    add(mess, username);
                }

            });
        }
    };
    Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    if (numUsers < 2)
                        addServerMessage("There's " + numUsers + " participant");
                    else
                        addServerMessage("There are " + numUsers + " participants");
                }

            });
        }
    };
    Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String mess = "";
                    try {
                        numUsers = data.getInt("numUsers");
                        mess = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addServerMessage(mess + " left");
                    if (numUsers < 2)
                        addServerMessage("There's " + numUsers + " participant");
                    else
                        addServerMessage("There are " + numUsers + " participants");
                }

            });
        }
    };
    Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String mess = "";
                    try {
                        numUsers = data.getInt("numUsers");
                        mess = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addServerMessage(mess + " joined");
                    if (numUsers < 2)
                        addServerMessage("There's " + numUsers + " participant");
                    else
                        addServerMessage("There are " + numUsers + " participants");
                }

            });
        }
    };

    public void add(String mess, String name) {

        TextView newMessage = new TextView(context);
        newMessage.setText(mess);
        newMessage.setTextSize(15);
        newMessage.setTextColor(Color.BLACK);
        TextView user = new TextView(context);
        user.setText(name + "  ");
        user.setTextSize(20);

        if(users.indexOf(name) != -1){
            user.setTextColor(colors.get(users.indexOf(name)));
        } else {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            colors.add(color);
            users.add(name);
            user.setTextColor(color);
        }

        LinearLayout newLayout = new LinearLayout(context);
        newLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        newLayout.setLayoutParams(lp);
        newLayout.addView(user);
        newLayout.addView(newMessage);
        newLayout.setGravity(Gravity.CENTER_VERTICAL);

        ll.addView(newLayout);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    public void addServerMessage(String s) {
        TextView newMessage = new TextView(context);
        newMessage.setText(s);
        newMessage.setTextColor(Color.GRAY);
        newMessage.setGravity(Gravity.CENTER_HORIZONTAL);
        ll.addView(newMessage);
        sv.fullScroll(ScrollView.FOCUS_DOWN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket.connected()) {
            socket.disconnect();
            socket.off("new message", onNewMessage);
            socket.off("user joined", onUserJoined);
            socket.off("user left", onUserLeft);
            socket.off("login", onLogin);
            socket.off("typing", onTyping);
            socket.off("stop typing", onStopTyping);
        }
    }

}
