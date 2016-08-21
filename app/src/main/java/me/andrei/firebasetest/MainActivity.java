package me.andrei.firebasetest;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import me.andrei.firebasetest.adapters.MainAdapter;
import me.andrei.firebasetest.models.Message;
import me.andrei.firebasetest.utils.MyUtils;

public class MainActivity extends AppCompatActivity {

  private Button button_send;
  private EditText editText_message;
  private FirebaseDatabase database;
  private DatabaseReference databaseReference;
  private ArrayList<Message> messagesList = new ArrayList<>();
  private ListView main_listview;
  private MainAdapter mainAdapter;
  private FirebaseAnalytics mFirebaseAnalytics;
  private String test_string;
  private String username;
  private MainActivity mContext;
  private TextView textView_is_typing;
  private MyCountDownTimmer isTypingTimmer = new MyCountDownTimmer(1000, 1000);
  private boolean isTyping = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mContext = MainActivity.this;

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    mFirebaseAnalytics.setUserProperty("user_type", "author");

    button_send = (Button) findViewById(R.id.button_send);
    editText_message = (EditText) findViewById(R.id.editText_message);
    textView_is_typing = (TextView) findViewById(R.id.textView_is_typing);
    main_listview = (ListView) findViewById(R.id.main_listview);
    username = getSharedPreferences("PREFS", 0).getString("username", "Anonymous");
    textView_is_typing.setVisibility(View.INVISIBLE);

    database = FirebaseDatabase.getInstance();
    databaseReference = database.getReference();

    mainAdapter = new MainAdapter(mContext, messagesList);
    main_listview.setAdapter(mainAdapter);

    test_string = null;

    button_send.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {

        //removes the "is typing" from the other person's screen
        isTypingTimmer.cancel();
        databaseReference.child("room-typing").child("irc").child(username).setValue(false);


        process_message(editText_message.getText().toString().trim());
        editText_message.setText("");
      }
    });

    databaseReference.child("users").child(MyUtils.generateUniqueUserId(mContext)).addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        username = dataSnapshot.getValue(String.class);
        if (username == null) {
          username = "Anonymous";
        }
      }

      @Override public void onCancelled(DatabaseError databaseError) {
      }
    });

    //------------------------------------------------------------
    databaseReference.child("db_messages").limitToLast(20).addChildEventListener(new ChildEventListener() {
      @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Message message = dataSnapshot.getValue(Message.class);
        messagesList.add(message);
        mainAdapter.notifyDataSetChanged();
        Log.d("message", message.toString());
      }

      @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d("onChildChanged", dataSnapshot.toString());
      }

      @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d("onChildRemoved", dataSnapshot.toString());
      }

      @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d("onChildMoved", dataSnapshot.toString());
      }

      @Override public void onCancelled(DatabaseError databaseError) {
        Log.d("onCancelled", databaseError.toString());
      }
    });

    editText_message.addTextChangedListener(new TextWatcher() {
      public void afterTextChanged(Editable s) {
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        isTypingTimmer.cancel();
        isTypingTimmer.start();
        if (!isTyping) {
          databaseReference.child("room-typing").child("irc").child(username).setValue(true);
          isTyping = true;
        }
      }
    });

    databaseReference.child("room-typing").child("irc").addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {

        HashMap<String, Boolean> hashMap = (HashMap<String, Boolean>) dataSnapshot.getValue();
        if (hashMap == null) {
          return;
        }
        if (hashMap.containsKey(username)) {
          hashMap.remove(username);
        }
        String output = "";
        for (String key : hashMap.keySet()) {
          if (hashMap.get(key).equals(true)) {
            output = output + key + " ";
          }
        }
        if (!output.isEmpty()) {
          textView_is_typing.setText(output + " is typing");
          textView_is_typing.setVisibility(View.VISIBLE);
        } else {
          textView_is_typing.setVisibility(View.INVISIBLE);
        }
      }

      @Override public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  private void process_message(String message) {
    if (message.length() < 1) {
      return;
    }



    //sends the db to the server.
    String key = databaseReference.child("db_messages").push().getKey();
    Message post = new Message(MyUtils.generateUniqueUserId(mContext), username, message, System.currentTimeMillis() / 1000L);
    Map<String, Object> postValues = post.toMap();
    Map<String, Object> childUpdates = new HashMap<>();
    childUpdates.put("/db_messages/" + key, postValues);
    databaseReference.updateChildren(childUpdates);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.main_activity, menu);
    return (super.onCreateOptionsMenu(menu));
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.username:

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Do you want to change your username ?");
        alert.setTitle(null);

        username = getSharedPreferences("PREFS", 0).getString("username", "Anonymous");
        edittext.setText(username);
        alert.setView(edittext);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            String new_username = edittext.getText().toString();
            mContext.getSharedPreferences("PREFS", 0).edit().putString("username", new_username).commit();
            username = new_username;
            databaseReference.child("users").child(MyUtils.generateUniqueUserId(mContext)).setValue(username);
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
          }
        });

        alert.show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public class MyCountDownTimmer extends CountDownTimer {

    public MyCountDownTimmer(long millisInFuture, long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override public void onTick(long l) {

    }

    @Override public void onFinish() {
      databaseReference.child("room-typing").child("irc").child(username).setValue(false);
      isTyping = false;
    }
  }
}
