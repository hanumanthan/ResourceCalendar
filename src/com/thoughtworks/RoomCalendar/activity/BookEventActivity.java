package com.thoughtworks.RoomCalendar.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.thoughtworks.RoomCalendar.R;
import com.thoughtworks.RoomCalendar.domain.BookingDetails;
import com.thoughtworks.RoomCalendar.domain.EventDetails;
import com.thoughtworks.RoomCalendar.utils.BookEventTasker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class BookEventActivity extends Activity {

    EditText eventNameText;
    EditText organizerText;
    TimePicker startTimePicker;
    TimePicker endTimePicker;
    Button okButton;
    Button cancelButton;
    Context context;
    private ArrayList<EventDetails> eventDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_event);
        context = this;
        Intent intent = getIntent();
        eventDetails = (ArrayList<EventDetails>) intent.getSerializableExtra("eventDetails");

        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        eventNameText = (EditText) findViewById(R.id.eventNameText);
        organizerText = (EditText) findViewById(R.id.organizer);
        startTimePicker = (TimePicker) findViewById(R.id.startTimePicker);
        startTimePicker.setIs24HourView(true);
        startTimePicker.setCurrentHour(hour);
        startTimePicker.setCurrentMinute(min);

        endTimePicker = (TimePicker) findViewById(R.id.endTimePicker);
        endTimePicker.setIs24HourView(true);
        endTimePicker.setCurrentHour(hour);
        endTimePicker.setCurrentMinute(min);


        okButton = (Button) findViewById(R.id.okay_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        registerListeners();

    }

    private void registerListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startHomeActivity();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                alertDialogBuilder.setTitle("Confirmation");

                alertDialogBuilder
                        .setMessage("Are you sure you want to book the event with the id " + organizerText.getText()+". The event wont be booked if the id is not valid.")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Calendar startTime = Calendar.getInstance();
                                startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getCurrentHour());
                                startTime.set(Calendar.MINUTE, startTimePicker.getCurrentMinute());

                                Calendar endTime = Calendar.getInstance();
                                endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getCurrentHour());
                                endTime.set(Calendar.MINUTE, endTimePicker.getCurrentMinute());

                                boolean isEventOverlap = false;

                                if (eventDetails != null && eventDetails.size() > 0) {
                                    for (EventDetails events : eventDetails) {
                                        if ((startTime.getTimeInMillis() > events.getStartTime() && startTime.getTimeInMillis() < events.getEndTime()) ||
                                                (endTime.getTimeInMillis() > events.getStartTime() && endTime.getTimeInMillis() < events.getEndTime())) {
                                            isEventOverlap = true;
                                            break;
                                        }
                                    }
                                }
                                if (!isEventOverlap) {
                                    BookEventTasker eventTasker = bookEvent(startTime, endTime);
                                    try {
                                        System.out.println(eventTasker.get());
                                        Toast.makeText(context, "Event booked", Toast.LENGTH_LONG).show();
                                        startHomeActivity();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(context, "Event rejected. An event already exists in the given time", Toast.LENGTH_LONG).show();
                                    startHomeActivity();
                                }

                            }

                            private BookEventTasker bookEvent(Calendar startTime, Calendar endTime) {
                                BookEventTasker eventTasker = new BookEventTasker(context);
                                BookingDetails bookingDetails = new BookingDetails(eventNameText.getText().toString(), organizerText.getText().toString(), startTime, endTime);
                                eventTasker.execute(bookingDetails);
                                return eventTasker;
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();

            }
        });
    }

    private void startHomeActivity() {
        Intent intent = new Intent(context, RoomCalendarActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
