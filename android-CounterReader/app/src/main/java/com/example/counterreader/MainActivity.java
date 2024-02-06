package com.example.counterreader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity implements CustomCallback , LocationListener {

    private EditText edt_subscribeNumber ;
    private EditText txt_newRead ;
    private EditText txt_currentRead ;
    private EditText edt_newCounterNumber ;
    private EditText edt_currentCounterNumber ;
    private EditText edt_currentReadDate ;
    private EditText edt_notes ;

    ////
    private EditText edt_lat ;
    private EditText edt_long ;
    ////
    // counter status
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_Location = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private boolean newRead_flag = false;
    private boolean currentRead_flag = false;
    DatabaseHelper myDb;

    String isConnected = "false";
    static int counter = 0;
    static int upload_result = 0;
    //badge
    TextView textCartItemCount;
    ProgressDialog pdia = null;
    private static final String SHARED_PREF_NAME = "Badge_Counter";

    LocationManager locationManager;
    String lat = "" ;
    String longt = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDb = new DatabaseHelper(this);
        setContentView(R.layout.activity_main);

        //ActivityCompat.requestPermissions(this,new String[{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_Location);
        checkLocationPermission();
        getLocation();

        txt_newRead = (EditText)findViewById(R.id.txt_newRead);
        txt_currentRead = (EditText)findViewById(R.id.txt_currentRead);
        edt_subscribeNumber = (EditText)findViewById(R.id.edt_serviceNumber);
        edt_currentCounterNumber = (EditText)findViewById(R.id.edt_currentCounterNumber);
        edt_currentReadDate = (EditText)findViewById(R.id.edt_date_currentCounterNumber);
        edt_newCounterNumber = (EditText)findViewById(R.id.edt_newCounterNumber);
        edt_notes = (EditText)findViewById(R.id.notes);
        edt_currentReadDate.setText(getCurrentDate());
        radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);


        edt_lat = (EditText)findViewById(R.id.lat);
        edt_long = (EditText)findViewById(R.id.lon);
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    public void startQRScanner_current(View view) {
        startQRScanner();
        currentRead_flag = true;
    }

    public void startQRScanner_new(View view) {
        startQRScanner();

        currentRead_flag = false;
       // newRead_flag = true;
    }

    public void getData(View view) {
        viewAll();
    }

    public void getData10(View view) {
        viewAll(10);
    }

    public void getData20(View view) {
        viewAll(20);
    }

    public void saveData(View view)    {

           isConnected = "false" ;
           AddToDataBase();

           Log.d("isNetworkConnected: ","false");




    }

    private void startQRScanner() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result =   IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this,    "Cancelled",Toast.LENGTH_LONG).show();
            } else {
                updateText(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateText(String scanCode) {



        if (currentRead_flag == true){


            Log.d("click", "1");

            txt_currentRead.setText(scanCode);
            currentRead_flag = false;
          }

        else {
            Log.d("click", "2");
            txt_newRead.setText(scanCode);
            currentRead_flag = true;
          //  newRead_flag = false;
        }
    }

    public  void AddToDataBase() {

        int CounterStatus = 1;


        // get selected radio button from radioGroup
        int selectedId = radioSexGroup.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radioSexButton = (RadioButton) findViewById(selectedId);


        if (radioSexButton.getText().toString().contains("صالح")  ){
            CounterStatus = 1;
        }
        else{
            CounterStatus = 0;
        }


        Log.d("hhh", radioSexButton.getText().toString());


       if (txt_currentRead.getText().toString().equals("")  || edt_currentCounterNumber.getText().toString().equals("")
               || edt_currentCounterNumber.getText().toString().equals("")
               || edt_currentReadDate.getText().toString().equals("")

               || radioSexButton.getText().equals("")

               || txt_newRead.getText().toString().equals("")

               ){



           showMessage("","الرجاء ادخال كافة البيانات");
       }
       else{



           boolean isInserted = myDb.insertData(edt_currentCounterNumber.getText().toString(), txt_currentRead.getText().toString(),
                   edt_currentReadDate.getText().toString(),
                   CounterStatus,edt_notes.getText().toString(),txt_newRead.getText().toString(),  edt_subscribeNumber.getText().toString(), isConnected, edt_newCounterNumber.getText().toString()
                   , lat.toString(),longt.toString()



           );
           if(isInserted == true) {



               SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
               SharedPreferences.Editor editor = sp.edit();


               int count = sp.getInt("Key_count", 0);


               editor.putInt("Key_count", count + 1);

               editor.apply();


               setupBadge(String.valueOf(count + 1));


               showMessage("","تم حفظ البيانات بنجاح");

               clearData();
           }
           else
               showMessage("","حدث خطا، حاول مرة اخرى");

       }

   }

    public void viewAll(int limit) {

                Cursor res = myDb.getAllData();

             //   Log.d("c", String.valueOf(res.getCount()));


                        if(res.getCount() == 0) {
                            // show message
                            showMessage("Error","Nothing found");
                            return;
                        }

                        StringBuffer buffer = new StringBuffer();
                        int count = 0;

           try {
               if (res.moveToLast()) {

                   do {
                    //   Log.d("p",String.valueOf(res.getPosition()));
                       count = count + 1;

                       Log.d("count",String.valueOf(count));

                       if (count >limit){
                           showMessage("الداتا المخزنة",buffer.toString());
                           return;
                         //  Log.d("yes",String.valueOf(count));
                       }



                       buffer.append("Id :"+ res.getString(0)+"\n");


                       buffer.append("رقم الاشتراك : "+ res.getString(8)+"\n");


                       buffer.append("تاريخ القراءة : "+ res.getString(4)+"\n");
                       buffer.append("رقم العداد الحالي : "+ res.getString(3)+"\n");
                       buffer.append("القراءة الحالية : "+ res.getString(1)+"\n");


                       buffer.append("رقم العداد الجديد : "+ res.getString(5)+"\n");
                       buffer.append( "قراءة العداد الجديد : "+ res.getString(2)+"\n");


                       buffer.append("حالة العداد القديم : "+ res.getString(6)+"\n");
                       buffer.append(" الملاحظات : "+ res.getString(7)+"\n");

                       buffer.append(" الاحداث السيني: "+ res.getString(10)+"\n");
                       buffer.append(" الاحداث الصادي : "+ res.getString(11)+"\n");



                       if (res.getString(9).equals("true")){
                           buffer.append("التحميل على السيرفر :  "+ "تم التحميل"+"\n");
                       }
                       else{
                           buffer.append("التحميل على السيرفر :  "+ " لم يتم التحميل "+"\n");
                       }


                       buffer.append("------------------------------------------------------ "+ "\n");



                   } while (res.moveToPrevious());
               }

           }catch ( Exception e){


        }


                    }

    public void viewAll() {

        Cursor res = myDb.getAllData();

        //   Log.d("c", String.valueOf(res.getCount()));


        if(res.getCount() == 0) {
            // show message
            showMessage("Error","Nothing found");
            return;
        }

        StringBuffer buffer = new StringBuffer();


        try {
            if (res.moveToLast()) {

                do {


                    buffer.append("Id :"+ res.getString(0)+"\n");


                    buffer.append("رقم الاشتراك : "+ res.getString(8)+"\n");


                    buffer.append("تاريخ القراءة : "+ res.getString(4)+"\n");
                    buffer.append("رقم العداد الحالي : "+ res.getString(3)+"\n");
                    buffer.append("القراءة الحالية : "+ res.getString(1)+"\n");


                    buffer.append("رقم العداد الجديد : "+ res.getString(5)+"\n");
                    buffer.append( "قراءة العداد الجديد : "+ res.getString(2)+"\n");


                    buffer.append("حالة العداد القديم : "+ res.getString(6)+"\n");
                    buffer.append(" الملاحظات : "+ res.getString(7)+"\n");

                    buffer.append(" الاحداث السيني: "+ res.getString(10)+"\n");
                    buffer.append(" الاحداث الصادي : "+ res.getString(11)+"\n");



                    if (res.getString(9).equals("true")){
                        buffer.append("التحميل على السيرفر :  "+ "تم التحميل"+"\n");
                    }
                    else{
                        buffer.append("التحميل على السيرفر :  "+ " لم يتم التحميل "+"\n");
                    }


                    buffer.append("------------------------------------------------------ "+ "\n");



                } while (res.moveToPrevious());
            }

        }catch ( Exception e){


        }
        showMessage("الداتا المخزنة",buffer.toString());






        // Show all data

        // Log.d("buffer",buffer.toString() );
    }

    public void viewAll2(int limit) {

        Cursor res = myDb.getAllDataFrom2();

        //   Log.d("c", String.valueOf(res.getCount()));


        if(res.getCount() == 0) {
            // show message
            showMessage("Error","Nothing found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        int count = 0;

        try {
            if (res.moveToLast()) {

                do {
                    //   Log.d("p",String.valueOf(res.getPosition()));
                    count = count + 1;

                    Log.d("count",String.valueOf(count));

                    if (count >limit){
                        showMessage("الداتا المخزنة",buffer.toString());
                        return;
                        //  Log.d("yes",String.valueOf(count));
                    }



                    buffer.append("Id :"+ res.getString(0)+"\n");


                    buffer.append("رقم الاشتراك : "+ res.getString(8)+"\n");


                    buffer.append("تاريخ القراءة : "+ res.getString(4)+"\n");
                    buffer.append("رقم العداد الحالي : "+ res.getString(3)+"\n");
                    buffer.append("القراءة الحالية : "+ res.getString(1)+"\n");


                    buffer.append("رقم العداد الجديد : "+ res.getString(5)+"\n");
                    buffer.append( "قراءة العداد الجديد : "+ res.getString(2)+"\n");

                    buffer.append(" الاحداث السيني: "+ res.getString(10)+"\n");
                    buffer.append(" الاحداث الصادي : "+ res.getString(11)+"\n");



                    buffer.append("------------------------------------------------------ "+ "\n");



                } while (res.moveToPrevious());
            }

        }catch ( Exception e){


        }


    }

    public void postListDataToServer(int i,String Replace_Meter_Transaction_ID , String Transaction_Date, String Agreement_No, String Old_Meter_No, String Old_Meter_Reading, String New_Meter_No,String New_Meter_Reading , String Notes, int Old_Meter_Status_ID
    , String lat, String longt){



        Map<String, String> postData = new HashMap<>();

        postData.put("Replace_Meter_Transaction_ID", Replace_Meter_Transaction_ID);
        postData.put("Transaction_Date", Transaction_Date);
        postData.put("Agreement_No", Agreement_No);

        postData.put("Old_Meter_No", Old_Meter_No);
        postData.put("Old_Meter_Reading", Old_Meter_Reading);

        postData.put("New_Meter_No", New_Meter_Reading);
        postData.put("New_Meter_Reading",New_Meter_No );


        postData.put("Notes", Notes);
        postData.put("Old_Meter_Status_ID", (String.valueOf(Old_Meter_Status_ID)));
        postData.put("Latitude", lat);
        postData.put("Longitude", longt);

        Log.d("postdata", postData.toString());



//        HttpPostAsyncTask task = new HttpPostAsyncTask(postData, RequestType.REQUEST_TYPE_2, this);
//        task.execute("http://192.168.0.169:5135/api/Services/ReplaceWaterMeter");

        new RequestAsync(postData,i).execute();




    }

    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);

        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_Location);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
break;


                case MY_PERMISSIONS_REQUEST_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted, yay! Do the
                        // location-related task you need to do.
                        //Request location updates:
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 400, 1, this);

                        }
                        // locationManager.requestLocationUpdates(locationManager.getBestProvider(true), 400, 1, this);

                    } else {

                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.

                    }
                    return;
                }
            }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void clearData(){
        txt_newRead.setText("");
        txt_currentRead.setText("");
        edt_subscribeNumber.setText("");
        edt_currentCounterNumber.setText("");
        edt_notes.setText("");
        edt_lat.setText("");
        edt_long.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);



        final MenuItem menuItem = menu.findItem(R.id.action_refresh);

        View actionView = MenuItemCompat.getActionView(menuItem);

        textCartItemCount = (TextView) actionView.findViewById(R.id.cart_badge);

        SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);


        final int count = sp.getInt("Key_count", 0);

        Log.d("d",String.valueOf(count));

        setupBadge(String.valueOf(count));





        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);


                Cursor res = myDb.getAllData();


                if(res.getCount() == 0) {
                    // show message
                    showMessage("","لا يوجد داتا للتحميل");


                }

                else {

                    counter = 0;
                    upload_result = 0;


                    while (res.moveToNext()) {
                        if (res.getString(9).equals("false")) {
                            counter = counter + 1;
                        }
                    }

                    Log.d("counter", "counter :" + counter);


                    if (counter == 0) {
                        showMessage("", "لا يوجد داتا للتحميل");

                    } else {


                        if (isNetworkConnected()) {


                            Log.d("conect", "conect ");
                            Cursor res1 = myDb.getAllData();


                            pdia = new ProgressDialog(MainActivity.this);

                            pdia.setCancelable(false);
                            pdia.setCanceledOnTouchOutside(false);

                            pdia.setMessage("Loading...");
                            pdia.show();





                            while (res1.moveToNext()) {



                            Log.d("postion",String.valueOf(res1.getPosition()));

                                Log.d("", "isConnected :" + res1.getString(9));




                                if (res1.getString(9).equals("false")) {
                                    // post to server

                                    Log.d("", "post :" + "post");





                                     postListDataToServer(res1.getPosition(), res1.getString(0), res1.getString(4), res1.getString(8),
                                               res1.getString(3), res1.getString(1),
                                               res1.getString(5), res1.getString(2), res1.getString(7), res1.getInt(6),
                                               res1.getString(10),res1.getString(11)

                                       );




                                }


                            }
//                        }
//
//
//
//

                        }

                        else{
                            showMessage("", "لا يوجد اتصال بالانترنت حاليا، حاول مرة اخرى");
                        }

                    }
                }




            }
        });
       // setupBadge();
        return true;
    }

    private void setupBadge(String count) {

        if (textCartItemCount != null) {

            textCartItemCount.setVisibility(View.VISIBLE);
            textCartItemCount.setText(count);
         }
    }

    public String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
        String strDate =  mdformat.format(calendar.getTime());
        return  strDate;

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void completionHandler(Boolean success, RequestType type) {

        Log.d("s", success.toString());

        switch (type) {
            case REQUEST_TYPE_1:

                pdia.dismiss();
                Log.d("success", success.toString());

                if (success.toString().contains("false")){
                    showMessage("","حدث خطا بالاتصال، حاول مرة اخرى");


                }
                else{

                    showMessage("","تم التحميل بنجاح");
                    clearData();
                }



                // Do UI updates ON THE UI THREAD needed for response to REQUEST_TYPE_1 using the object that sent here
                break;
            case REQUEST_TYPE_2:

                Log.d("REQUEST_TYPE_2-counter", String.valueOf(counter));


                    pdia.dismiss();
                    showMessage("","تم التحميل بنجاح");


                SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();



                editor.putInt("Key_count", 0);

                editor.apply();
                setupBadge(String.valueOf("0"));


                    clearData();

                // Do something
                break;
            default: break;
        }
    }

    public class RequestAsync extends AsyncTask<String,String,String>  {

        JSONObject postData;
        int index;

        public RequestAsync(Map<String, String> postData, int Index) {
            if (postData != null) {
                this.postData = new JSONObject(postData);
                this.index = Index;
            }
        }

        @Override
        protected String doInBackground(String... strings) {


            try {
                //GET Request
                //return RequestHandler.sendGet("https://prodevsblog.com/android_get.php");

                // POST Request
//                JSONObject postDataParams = new JSONObject();
//                postDataParams.put("name", "Manjeet");
//                postDataParams.put("email", "manjeet@gmail.com");
//                postDataParams.put("phone", "+1111111111");



                //http://192.168.0.169:5135/api/Services/ReplaceWaterMeter
                //http://83.244.112.170:5135/api/Services/ReplaceWaterMeter

                return RequestHandler.sendPost("http://83.244.112.170:5135/api/Services/ReplaceWaterMeter", postData);
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String s) {



            if (s != null) {
                if (s.contains("Success")) {
                    MainActivity.counter = MainActivity.counter - 1;
                    Log.d("", "counter :" + String.valueOf(MainActivity.counter));




                        //update
  myDb.updateData( myDb.getItemIdByPosition( index).getString(0),  myDb.getItemIdByPosition( index).getString(1),  myDb.getItemIdByPosition( index).getString(3),
                                myDb.getItemIdByPosition( index).getString(4),
                                myDb.getItemIdByPosition( index).getInt(6),  myDb.getItemIdByPosition( index).getString(7),  myDb.getItemIdByPosition( index).getString(2),  myDb.getItemIdByPosition( index).getString(8), "true",  myDb.getItemIdByPosition( index).getString(5)
                             ,myDb.getItemIdByPosition( index).getString(10),myDb.getItemIdByPosition( index).getString(11)
                        );




                } else {
                    Log.d("", "counter :" + String.valueOf(MainActivity.counter));

                }

                if (MainActivity.counter == 0) {
                    if (s.contains("Success")) {



                        pdia.dismiss();
                        showMessage("","تم التحميل بنجاح");


                        SharedPreferences sp = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();



                        editor.putInt("Key_count", 0);

                        editor.apply();
                        setupBadge(String.valueOf("0"));


                        clearData();

                    } else {
                        Log.d("Status2:", "fail");

                    }


                }
            }


        }
    }

    @Override
    public void onLocationChanged(Location location) {

        lat =  String.valueOf(location.getLatitude());
        longt = String.valueOf(location.getLongitude());

        Log.d("Current Location:", location.getLatitude() + ", " + location.getLongitude());


      //  locationText.setText("Current Location: " + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {

        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(this)
//                        .setTitle(R.string.title_location_permission)
//                        .setMessage(R.string.text_location_permission)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(MainActivity.this,
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }



    }






