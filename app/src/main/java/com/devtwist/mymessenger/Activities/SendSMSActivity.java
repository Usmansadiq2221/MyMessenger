package com.devtwist.mymessenger.Activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devtwist.mymessenger.AlarmReceiver;
import com.devtwist.mymessenger.R;

import java.util.Calendar;


public class SendSMSActivity extends AppCompatActivity {
    private TimePicker _timePicker;
    private ImageView _sendSms, _cancelSTimer;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private TextView _setTimerText, _sendToWhatsapp, _sendToSms, _sTimelineText, _sMessengerText;
    private EditText _enterSms, _enterContactNo;
    private LinearLayout _optionLayout, _messageLayout;
    private ListView _contactList;
    private Intent intent;
    private String type;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_smsactivity);

        _timePicker = findViewById(R.id._timePicker);
        _sendSms = (ImageView) findViewById(R.id._sendSms);
        _setTimerText = findViewById(R.id._setSMSTimerText);
        _enterContactNo = findViewById(R.id._enterContactNo);
        _enterSms = findViewById(R.id._enterSms);
        _sendToSms = findViewById(R.id._sendToSms);
        _sendToWhatsapp = findViewById(R.id._sendtoWhatsapp);
        _optionLayout = findViewById(R.id._optionsLayout);
        _messageLayout = findViewById(R.id._messageLayout);
        _contactList = findViewById(R.id._contactList);
        _sTimelineText = findViewById(R.id._sTimelineText);
        _sMessengerText = findViewById(R.id._sMessengerText);
        _cancelSTimer = findViewById(R.id._cancelSTimer);
        type = "";


        _sTimelineText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(SendSMSActivity.this, TimelineActivity.class);
                startActivity(intent);
            }
        });

        _sMessengerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(SendSMSActivity.this, MessengerActivity.class);
                startActivity(intent);
            }
        });

        //setting visibility of timepicker and massage Layout...
        _sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_enterSms.getText().length()>0) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                    _timePicker.setVisibility(View.VISIBLE);
                    _setTimerText.setVisibility(View.VISIBLE);
                    _cancelSTimer.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(SendSMSActivity.this, "Enter SMS", Toast.LENGTH_SHORT).show();
                }
                //_messageLayout.setVisibility(View.GONE);
                //_optionLayout.setVisibility(View.VISIBLE);
            }
        });

        _enterContactNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null, null);
                startManagingCursor(cursor);

                //String contactName = (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                //String contactNumber = ContactsContract.CommonDataKinds.Phone.NUMBER;
                //String contactId = ContactsContract.CommonDataKinds.Phone._ID;

                String[] from = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone._ID};

                int[] to = {android.R.id.text1,android.R.id.text2};
                SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(SendSMSActivity.this, android.R.layout.simple_list_item_2,cursor,from,to);
                _contactList.setAdapter(cursorAdapter);
                _contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                 */
            }
        });

        _enterContactNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                try {
                    _contactList.setVisibility(View.VISIBLE);
                    Uri filterUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(_enterContactNo.getText().toString().trim()));
                    String[] projectionString = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone._ID};
                    Cursor cursor = getContentResolver().query(filterUri, projectionString, null, null, null);
                    startManagingCursor(cursor);

                    //String contactName = (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    //String contactNumber = ContactsContract.CommonDataKinds.Phone.NUMBER;
                    //String contactId = ContactsContract.CommonDataKinds.Phone._ID;


                    int[] to = {R.id._contactNameItem, R.id._contactNumberItem};
                    SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(SendSMSActivity.this, R.layout.contact_list_item, cursor, projectionString, to);
                    _contactList.setAdapter(cursorAdapter);
                    _contactList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    _contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            _enterContactNo.setVisibility(View.GONE);
                            _enterContactNo.setText(((TextView) view.findViewById(R.id._contactNumberItem)).getText().toString());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //for sending message to whatsapp...
        _sendToWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "Whatsapp";
                //setTime();
                _optionLayout.setVisibility(View.GONE);
                //_messageLayout.setVisibility(View.VISIBLE);
                _timePicker.setVisibility(View.VISIBLE);
                _setTimerText.setVisibility(View.VISIBLE);
            }
        });

        //for sending SMS...
        _sendToSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "SMS";
                //setTime();
                _optionLayout.setVisibility(View.GONE);
                //_messageLayout.setVisibility(View.VISIBLE);
                _timePicker.setVisibility(View.VISIBLE);
                _setTimerText.setVisibility(View.VISIBLE);
            }
        });

        _cancelSTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _cancelSTimer.setVisibility(View.GONE);
                _setTimerText.setVisibility(View.GONE);
                _timePicker.setVisibility(View.GONE);
            }
        });

        //get time by setting visibility of timer...
        _setTimerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
                //sendWhatsappMessage();
                _cancelSTimer.setVisibility(View.GONE);
                _setTimerText.setVisibility(View.GONE);
                _timePicker.setVisibility(View.GONE);
                _sendSms.setVisibility(View.VISIBLE);
                _enterSms.setText("");
                _messageLayout.setVisibility(View.VISIBLE);
                //_optionLayout.setVisibility(View.VISIBLE);
            }
        });



    }

    private void sendWhatsappMessage(){
        try {
            String smsText = _enterSms.getText().toString().trim();
            String whatsappNumber = _enterContactNo.getText().toString().trim();
            Uri uri = Uri.parse("smsto:" + whatsappNumber);
            Intent whatsappIntent = new Intent(Intent.ACTION_SENDTO, uri);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, smsText);
            whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(whatsappIntent);
        } catch (Exception e) {
            Log.i("WhatsappError", e.getMessage().toString());
        }
    }

    private void setTime(){
        type = "SMS";
        long time;
        Calendar calendar = Calendar.getInstance();
        String contactNo = "", enterSMS = "";
        contactNo = _enterContactNo.getText().toString().trim();
        enterSMS = _enterSms.getText().toString().trim();
        // calendar is called to get current time in hour and minute
        calendar.set(Calendar.HOUR_OF_DAY, _timePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, _timePicker.getCurrentMinute());

        // using intent i have class AlarmReceiver class which inherits
        // BroadcastReceiver
        Intent intent = new Intent(SendSMSActivity.this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("contactNo", contactNo);
        bundle.putString("sms", enterSMS);
        bundle.putString("type", type);
        intent.putExtras(bundle);
        intent.setAction("BackgroundProcess");
        // we call broadcast using pendingIntent
        final int id = (int) System.currentTimeMillis();
        pendingIntent = PendingIntent.getBroadcast(SendSMSActivity.this, id, intent, PendingIntent.FLAG_ONE_SHOT);

        time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));

        if (System.currentTimeMillis() > time) {
            // setting time as AM and PM
            if (calendar.AM_PM == 0)
                time = time + (1000 * 60 * 60 * 12);
            else
                time = time + (1000 * 60 * 60 * 24);
        }
        // Alarm rings continuously until toggle button is turned off
        //alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 10000, pendingIntent);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (time * 1000), pendingIntent);


        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,time,pendingIntent);
        _enterContactNo.setVisibility(View.VISIBLE);

    }

    /*
    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("Contact Name", "Name: " + name);
                        Log.i("Contact Number", "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }
     */
}