package achivementtrackerbyamit.example.achivetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

public class DashboardActivity extends AppCompatActivity {

    private final int GALLERY_INTENT_CODE = 993;
    private final int CAMERA_INTENT_CODE = 990;

    TextView name,consis,left,goal_lft_pert, notes;
    TextView Tdays, Dleft, Sdate, Edate; //StreakOverview
    RelativeLayout rel;
    String id = "";
    String currentUserID;
    ImageView descButton;
    String description;
    long Days;
    String goal_end, goal_create;
    //RecyclerView recyclerView;
    MCalendarView mCalendarView;
    ArrayList<DateData> dataArrayList;
    private StorageReference UserProfileImagesRef;
    ProgressDialog progressDialog;
    DatabaseReference RootRef,HelloREf,newRef;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
    private Handler handler = new Handler();
    private Runnable runnable;
    ImageView extendedFloatingShareButton;
    ImageView extendedFloatingEditButton;
    ImageView deleteGoal;
    ImageButton add_img;
    CircleImageView goalPic;
    private String EVENT_DATE_TIME = "null";
    private String DATE_FORMAT = "dd/M/yyyy hh:mm:ss";
    String GoalName;
    public static final String ADD_TRIP_VALUE= DashboardActivity.class.getName();
    public static final String ADD_TRIP_TAG="ADD_TRIP_TAG";
    public static final String ADD_TRIP_DATA_KEY="ADD_TRIP_DATA_KEY";
    View Leave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        InitializationMethod();

         clearCalendar();
         highLightDate();

        //recyclerView.setLayoutManager(new LinearLayoutManager(DashboardActivity.this));



        RetriveData();

        extendedFloatingShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View gh = findViewById(R.id.relative_for_snap);
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                share(screenShot(gh));
            }
        });

        ImageView shareStreak = findViewById(R.id.streakButOV);
        shareStreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View gh = findViewById(R.id.streakOV);
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                share(screenShot(gh));
            }
        });

        ImageView shareNotes = findViewById(R.id.shareButNotes);
        shareNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View gh = findViewById(R.id.streakNote);
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                share(screenShot(gh));
            }
        });


        ImageView shareCal = findViewById(R.id.shareCal);
        shareCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View gh = findViewById(R.id.history_calendarViewGroup);
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                share(screenShot(gh));
            }
        });

        extendedFloatingEditButton.setOnClickListener(view-> sendData());

        deleteGoal = findViewById(R.id.delete_goal);
        deleteGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Alert dialog for confirming deletion of goal
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(DashboardActivity.this,R.style.AlertDialogTheme1);
                builder.setTitle("Alert!");
                builder.setMessage("Are you sure you want to delete this?");
                builder.setBackground(getResources().getDrawable(R.drawable.material_dialog_box , null));
                builder.setCancelable(false);

                // If yes chosen, then delete the goal and go back to the main activity
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RootRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(DashboardActivity.this, "Goal deleted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                else
                                    Toast.makeText(DashboardActivity.this, "Failed to delete goal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                // If no chosen, then close the dialog box
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowOptionsforProfilePic();
            }
        });



        descButton = findViewById(R.id.desc_button);
        descButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(DashboardActivity.this,R.style.AlertDialogTheme1);
                builder.setTitle(name.getText().toString());
                builder.setMessage(description);
                builder.setBackground(getResources().getDrawable(R.drawable.material_dialog_box , null));
                builder.setIcon(R.drawable.ic_info);
                builder.show();
            }
        });

        Leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLeaveDays();
            }
        });


    }

    private void ShowOptionsforProfilePic() {
        new MaterialAlertDialogBuilder(DashboardActivity.this).setBackground(getResources().getDrawable(R.drawable.material_dialog_box)).setTitle("Change profile photo").setItems(new String[]{"Choose from gallery", "Take a new picture"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i)
                {
                    // Choosing image from gallery
                    case 0:
                        // Defining Implicit Intent to mobile gallery
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(
                                        intent,
                                        "Select Image from here..."),
                                GALLERY_INTENT_CODE);
                        break;

                    // Clicking a new picture using camera
                    case 1:
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(cameraIntent,CAMERA_INTENT_CODE);
                        }
                        startActivityForResult(cameraIntent,CAMERA_INTENT_CODE);
                        break;
                }
            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && data!=null)
        {
            Uri uri = (Uri) data.getData();
            switch (requestCode)
            {
                // Image received from gallery
                case GALLERY_INTENT_CODE:
                    try {
                        // Converting the image uri to bitmap
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        goalPic.setImageBitmap(bitmap);
                        uploadGoalPic(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                // Image received from camera
                case CAMERA_INTENT_CODE:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    goalPic.setImageBitmap(bitmap);
                    uploadGoalPic(bitmap);
                    break;
            }
        }
    }

    private void uploadGoalPic(Bitmap bitmap) {


        StorageReference storageReference = UserProfileImagesRef.child ( id + ".jpg");


        showProgressDialog();

        // Converting image bitmap to byte array for uploading to firebase storage
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("Target")
                .child(id+".jpeg");
        byte[] pfp = baos.toByteArray();

        // Uploading the byte array to firebase storage
        storageReference.putBytes(pfp).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // Getting url of the image uploaded to firebase storage
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                // Setting the image url as the user_image property of the user in the database
                                String pfpUrl = task.getResult().toString();
                                RootRef.child(id).child("goal_image").setValue(pfpUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Goal picture updated", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Failed to upload goal picture", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed to upload goal picture", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Failed to upload goal picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendData() {

        Intent intent= new Intent(DashboardActivity.this, AddGoalActivity.class);
        intent.putExtra(ADD_TRIP_TAG,ADD_TRIP_VALUE);
        intent.putExtra(ADD_TRIP_DATA_KEY,id);
        startActivity(intent);

    }


    private void InitializationMethod() {
        Intent intent = getIntent();
        id = intent.getStringExtra("LISTKEY");

        UserProfileImagesRef = FirebaseStorage.getInstance ().getReference ().child ( "Goal Images" );
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid ();
        RootRef= FirebaseDatabase.getInstance ().getReference ().child("Users").child(currentUserID).child("Goals").child("Active");
        HelloREf = FirebaseDatabase.getInstance ().getReference ().child("Users").child(currentUserID).child("Goals").child("Active").child(id).child("Win");

        newRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        name = findViewById(R.id.desc_goal_name);
        extendedFloatingShareButton = findViewById(R.id.share_Sss);
        extendedFloatingEditButton = findViewById(R.id.edit_goal_btn);
        consis = findViewById(R.id.desc_goal_const);
        left = findViewById(R.id.desc_goal_left);
        mCalendarView= findViewById(R.id.history_calendarView);
        goal_lft_pert = findViewById(R.id.desc_goal_leftper);
        // rel= findViewById(R.id.RelativeLayout);
        //recyclerView = findViewById(R.id.history_recyler);

        add_img = findViewById(R.id.add_img);
        goalPic = findViewById(R.id.imageIcon);

        //Streak Overview
        Tdays = findViewById(R.id.totalDays);
        Dleft = findViewById(R.id.daysLeft);
        Sdate = findViewById(R.id.startDate);
        Edate = findViewById(R.id.endDate);

        //Notes
        notes = findViewById(R.id.Notes);

        Leave = findViewById(R.id.LeaveButton);

    }

    // Here is the second progress Dialog Box
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(DashboardActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_diaglog);
        progressDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
//        Runnable progressRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (confirmation != 1) {
//                    progressDialog.cancel();
//                    Toast.makeText(DashboardActivity.this, "Fetching data from Firebase", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//
//        Handler pdCanceller = new Handler();
//        pdCanceller.postDelayed(progressRunnable, 5000);
    }


    private Bitmap screenShot(View view) {
        View screenView = view;
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void share(Bitmap bitmap){



        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap , "IMG_" + Calendar.getInstance().getTime(), null);


        if (!TextUtils.isEmpty(pathofBmp)){
            Uri uri = Uri.parse(pathofBmp);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Tracky : track your Goal");
            //Retrieve value of completed goal using shared preferences from RetreiveData() function
            String goal_cmpltd = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).getString("goal_completed","");
            //Retreive value of consistency using shared preferences from RetreiveData() function
            String goal_consistency = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).getString("consistency","");
            //Retreive goal name using shared preferences from RetreiveData() function
            String goal_name = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).getString("goal_name","");
            //Retreive name using Shared preference from Retrieve data function
            String user_name = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).getString("name","");
            //Code to add Text with image
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi , I am "+user_name+" using this Tracky : Track your goal Application" +
                    " and by using this I measured my "+goal_name+" goal and be happy that I keep my consistency as "+goal_consistency+
                    "%. And I have also completed my goal "+goal_cmpltd+"%.So happy to share with you . #tracky #track #goal"
            );
            // Here You need to add code for issue
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "hello hello"));
        }

    }

    private void getLeaveDays() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(DashboardActivity.this); //Created alert Dialog
        mydialog.setTitle("How many days of break you need?"); //Title of EditText
        final EditText weightInput = new EditText(DashboardActivity.this);
        weightInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        mydialog.setView(weightInput);
        mydialog.setPositiveButton("Request Break", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String myText = weightInput.getText().toString(); //Saving Entered name in String
                int Days = Integer.parseInt(myText);
                if(myText.isEmpty())
                    Toast.makeText(DashboardActivity.this, "Please input number of Days", Toast.LENGTH_SHORT).show();
                else
                    askLeave(Days);
            }
        });
        mydialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel(); //cancel button
            }
        });
        mydialog.show();
    }

    private void askLeave(int days) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Date endDate = dateFormat.parse(goal_end);
            Date today = new Date();
            String nDate = dateFormat.format(today);
            Date updatedToday = dateFormat.parse(nDate);
            long diff = endDate.getTime() - updatedToday.getTime();
            long Days = diff / (24 * 60 * 60 * 1000);
            int d = (int) Days - days;
            if(d < 1)
                Toast.makeText(this, "Please select less days" , Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(this, "Leave Granted!", Toast.LENGTH_SHORT).show();
                updateGoal(days);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void updateGoal(long days) {
        Toast.makeText(this, ""+days, Toast.LENGTH_SHORT).show();

        int intt = (int) days;

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");

        String goal_created_date = goal_create;

        Date create_date = null;
        try {
            create_date = simpleDateFormat2.parse(goal_created_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Date today = new Date();

        Date today_date_after_increse = new Date(today.getTime() + (1000 * 60 * 60 * 24 * intt));
        Date create_date_after_increse = new Date(create_date.getTime() + (1000 * 60 * 60 * 24 * intt));


        String today_date_after_increse_string = simpleDateFormat2.format(today_date_after_increse);
        String create_date_after_increse_string = simpleDateFormat2.format(create_date_after_increse);

     //   Toast.makeText(DashboardActivity.this, today_date_after_increse_string+"\n"+create_date_after_increse_string, Toast.LENGTH_SHORT).show();

        HashMap<String,Object> onlineStat = new HashMap<> (  );
        onlineStat.put ( "TodayTime", create_date_after_increse_string);
        onlineStat.put ("Status", "OnBreak");
        onlineStat.put ("BreakEndDate", today_date_after_increse_string);

        RootRef.child(id)
                .updateChildren ( onlineStat );
    }


    @Override
    public void onStart() {
        super.onStart ();

        showProgressDialog();


    }



    private void RetriveData() {

        RootRef.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) return;
                String goal_string = snapshot.child ( "GoalName" ).getValue ().toString ();
                //Shared Preference to use the goal name in share() function
                PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).edit().putString("goal_name",goal_string).commit();
                goal_end = snapshot.child ( "EndTime" ).getValue ().toString ();
                goal_create = snapshot.child ( "TodayTime" ).getValue ().toString ();
                if(snapshot.child("Goal_Description").getValue()!=null)
                    description = String.valueOf(snapshot.child("Goal_Description").getValue());
                Object pfpUrl = snapshot.child("goal_image").getValue();
                if (pfpUrl != null) {
                    // If the url is not null, then adding the image
                    Picasso.get().load(pfpUrl.toString()).placeholder(R.drawable.ic_google).error(R.drawable.ic_google).into(goalPic);
                }

                Date today = new Date();
                String todaay = simpleDateFormat.format(today);



                int count_nodes = (int) snapshot.child("Win").getChildrenCount();

                int io = 0;

                if((DayReturn(todaay,goal_create))>=0){
                    String dt = ConsistentFn(count_nodes,todaay,goal_create);
                    io = GoalCOmpleteFn(todaay,goal_create,goal_end);
                    //Shared Preference to use the value of 'io' in share() function
                    PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).edit().putString("goal_completed", String.valueOf(io)).commit();

                    HashMap<String,Object> onlineStat = new HashMap<> (  );
                    onlineStat.put ( "Consistency", dt);
                    RootRef.child(id)
                            .updateChildren ( onlineStat );
                }


                String goal_const = snapshot.child ( "Consistency" ).getValue ().toString ();
                //Shared Preference to use the value of 'goal_const' in share() function
                PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).edit().putString("consistency", goal_const).commit();

                int const_int = Integer.parseInt(String.valueOf(goal_const));

                PieChart mPieChart = (PieChart) findViewById(R.id.piechart1);

                mPieChart.addPieSlice(new PieModel("Done", const_int, Color.parseColor("#0F9D58")));
                mPieChart.addPieSlice(new PieModel("Not Done", (100-const_int), Color.parseColor("#DB4437")));

                mPieChart.startAnimation();

                goal_lft_pert.setText("Completed :" +String.valueOf(io)+" %");


                PieChart mPieChart2 = (PieChart) findViewById(R.id.piechart2);

                mPieChart2.addPieSlice(new PieModel("Done", io, Color.parseColor("#4285F4")));
                mPieChart2.addPieSlice(new PieModel("Not Done", (100-io), Color.parseColor("#F4B400")));

                mPieChart2.startAnimation();


                name.setText(goal_string);
                GoalName = goal_string;
                consis.setText("Consistency :" +goal_const+" %");
                EVENT_DATE_TIME = goal_end;
                countDownStart();

                progressDialog.dismiss();




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });
        //Name fetching from Firebase to use in share() function
        newRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("name").getValue ().toString ();
                PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this).edit().putString("name",username).commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static int GoalCOmpleteFn(String todaay, String goal_create, String goal_end) {

        float gh = (DayReturn(todaay,goal_create)+1)*100/(DayReturn(goal_end,goal_create)+1);
        return Math.round(gh);
    }

    private void countDownStart() {
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 1000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                    Date event_date = dateFormat.parse(EVENT_DATE_TIME);
                    Date current_date = new Date();
                    Date created = dateFormat.parse(goal_create);
                    if (!current_date.after(event_date)) {
                        long diff = event_date.getTime() - current_date.getTime();
                        long diffCreate = (event_date.getTime() - created.getTime()) / (24 * 60 * 60 * 1000);
                        Days = diff / (24 * 60 * 60 * 1000);
                        long Hours = diff / (60 * 60 * 1000) % 24;
                        long Minutes = diff / (60 * 1000) % 60;
                        long Seconds = diff / 1000 % 60;
                        long totaldays= event_date.getTime()/(24 * 60 * 60 * 1000);
                        long percent= (Days*100/totaldays);
                        //StreakOvewview Data
                        Tdays.setText(String.format("%02d",diffCreate)+"d");
                        Dleft.setText(String.format("%02d",Days)+"d");
                        Sdate.setText(goal_create.substring(0,10).trim());
                        Edate.setText(goal_end.substring(0,10).trim());
                        notes.setText(description);
                        left.setText(String.format("%02d",Days)+" days "+String.format("%02d", Hours)+" hours "+String.format("%02d", Minutes)+" minutes "+String.format("%02d", Seconds)+" seconds ");
                        if(percent<=33) {
                            left.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
                            rel.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.lightred));
                        }
                        else if(percent<=66)
                        {
                            left.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.yellow));
                            rel.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.lightyellow));
                        }
                        else
                        {
                            left.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green));
                            rel.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.lightgreen));
                        }
                    } else {

                        handler.removeCallbacks(runnable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }

    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    public static long DayReturn(String high,String low){

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd/M/yyyy");
        Date date1=null,date2 = null;
        try {
            date2 = simpleDateFormat2.parse(low);
            date1 = simpleDateFormat2.parse(high);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        long different = date1.getTime() - date2.getTime();


        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        return elapsedDays;
    }


    public String ConsistentFn(int node,String today_date,String create_date){


        float fl  = (float)(node*100)/(DayReturn(today_date,create_date)+1);
        int iu = Math.round(fl);
        return String.valueOf(iu);

    }


    public void AlarmAct(View view) {
        Intent i = new Intent(getApplicationContext(), AlarmActivity.class); //Pass to AlarmActivity Class
        i.putExtra("GoalName", GoalName); //Passing Goal Name
        startActivity(i);
    }

    private void   highLightDate(){

        //ArrayList<DateData> dataArrayList;
        HelloREf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    dataArrayList = new ArrayList<DateData>();
                    for(DataSnapshot snapshot1:snapshot.getChildren()){

                        String strDate= snapshot1.getKey();
                        // Log.d("ParseException",strDate);
                        int day = Integer.parseInt(strDate.substring(0,strDate.indexOf("-")));
                        int month= Integer.parseInt(strDate.substring(3,strDate.lastIndexOf("-")));
                        int year= Integer.parseInt(strDate.substring(strDate.lastIndexOf("-")+1,9));
                        DateData date= new DateData(year,month,day);
                        dataArrayList.add(date);

                    }
                    // MCalendarView mCalendarView= findViewById(R.id.history_calendarView);
                    for(int i=0; i< dataArrayList.size();i++){

                        DateData date= dataArrayList.get(i);

                        mCalendarView.markDate(date.getYear(),
                                date.getMonth(),
                                date.getDay());

                        mCalendarView.setMarkedStyle(MarkStyle.BACKGROUND,Color.BLUE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearCalendar();
    }

    private void clearCalendar(){

        MarkedDates markedDates= mCalendarView.getMarkedDates();

        ArrayList<DateData> currDataList= markedDates.getAll();

        for(int i=0; i<currDataList.size();i++){

            DateData data= currDataList.get(i);

            mCalendarView.unMarkDate(data.getYear(),data.getMonth(),data.getDay());
        }
    }

}