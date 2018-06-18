package com.tanuj.session16assignment1;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    EditText dataEditText;
    TextView dataTextView;
    Button showDataButton, deleteFileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check Whether Permission Granted Or Not If Not Request for Write External Storage Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this,new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }

        // Type cast Edit Text From XML To Access in MainActivity
        dataEditText = (EditText) findViewById(R.id.dataEditText);

        // Type Cast Text View From XML To access in MainActivity
        dataTextView  = (TextView) findViewById(R.id.dataTextView);

        // Type Cast Button From XML to Access In MainActivity.
        showDataButton = (Button) findViewById(R.id.showDataButton);
        deleteFileButton = (Button) findViewById(R.id.deleteFileButton);

        // Set On Click Listener To Add Data Into File And Display using AsyncTask
        showDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Before Perform Async Action, Validate Whether External Storage Available or not,
                // If Available whether its readable or not...
                if (isExternalStorageAvailable() && ! isExternalStorageReadOnly())
                    // Create An Object of Async Task and Provide Edit Text Object to Read the contents. and Write to text file
                    new writeDataToFileAsync().execute(dataEditText);
                else
                    // Show Toast If External Storage Not Available / Ready Only...
                    Toast.makeText(MainActivity.this, "External Storage Not Available/ReadOnly", Toast.LENGTH_SHORT).show();

            }
        });

        // Set On Click Listener To Delete Button.
        deleteFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Validate External Storage Availability And Readable. If YEs proceed to find the file and delete.
                if (isExternalStorageAvailable() && ! isExternalStorageReadOnly()) {
                    // Read External Storage Directory from Android Environment class.
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();

                    // Set Text File Name [with which we have saved]
                    String fileName = "test.text";

                    // Get File By External Storage Directory along with file name
                    final File textFile = new File(externalStorageDirectory, fileName);
                    // Check Whehter file exists or not, if exists delete. if not exists. Show Toast, file not found
                    if (textFile.exists()) {
                        // Delete FIle
                        textFile.delete();
                        // Update In Text View the deletion success.
                        dataTextView.setText("File Deleted Successfully...");
                    }
                    else
                        // Toast File NOt Found
                        Toast.makeText(MainActivity.this, "File Not Found...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Create An Async Task Class to write Data into Text File.
    class writeDataToFileAsync extends AsyncTask<EditText, String, String>{

        // Override do in background method to perform write data action.
        @Override
        protected String doInBackground(EditText... editTexts) {

            // Declare Edit Text Object and which was passed from OnCreate Method of Async Task Call.
            final EditText fileContents  = (EditText)editTexts[0];

            // In Order to Access the UI Elements in Async Do background method. implement runOnUIThread Method.
            // this will allow the Async Task Do Background to access the UI element which are required to write data to text file
            runOnUiThread(new Runnable() {

                //Override Run method.
                @Override
                public void run() {

                    // Read External Storage Directory from Android Environment class.
                    File externalStorageDirectory = Environment.getExternalStorageDirectory();

                    // Set Text File Name [with which we have saved]
                    String fileName = "test.text";

                    // Get File By External Storage Directory along with file name
                    final File textFile = new File(externalStorageDirectory,fileName);

                    // Open File Output Stream To Write Data
                    FileOutputStream fileOutputStream = null;
                    try {
                        // Reference File Output Stream with respective file which we have referenced in File Object.
                        fileOutputStream = new FileOutputStream(textFile);

                        // Write Data to file by reading the contents from Edit Text and conver them to bytes in order
                        // for File Output Stream to write data.
                        fileOutputStream.write(fileContents.getText().toString().getBytes());

                        // Close File output Stream
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        //File not found error thrown
                        e.printStackTrace();
                    } catch (IOException e) {
                        // IO Exception
                        e.printStackTrace();
                    }

                }
            });
            return null;
        }

        // Override On Post Execute Method. this will execute upon completion of
        // doInBackground Method. Which will help call Another Async Task to Read Data which was written in the text file
        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);

            // Reference TextView To Write Output
            TextView textView = (TextView)findViewById(R.id.dataTextView);

            // Create Object of Async Task Class to read data from existing file.
           new readDataFromFileAsync().execute(textView);
        }
    }

    class readDataFromFileAsync extends AsyncTask<TextView, String, String>{

        @Override
        protected String doInBackground(TextView... textViews) {
            // Declare Text View Object and which was passed from PostExecute Method of Async Task.
            final TextView textView = (TextView)textViews[0];

            // Read External Storage Directory from Android Environment class.
            File externalStorageDirectory = Environment.getExternalStorageDirectory();

            // Set Text File Name [with which we have saved]
            String fileName = "test.text";

            // Get File By External Storage Directory along with file name
            final File textFile = new File(externalStorageDirectory,fileName);

            // In Order to Access the UI Elements in Async Do background method. implement runOnUIThread Method.
            // this will allow the Async Task Do Background to access the UI element which are required to write data to text file
            runOnUiThread(new Runnable() {
                //Override Run method.
                @Override
                public void run() {
                    //Reset Text View To Empty String.
                    textView.setText("");
                    if (textFile.exists())
                        try {
                            // Open File Input Stream To Reference with the available text file.
                            // This will help open Read Stream to file.
                            FileInputStream fileInputStream = new FileInputStream(textFile);

                            // Using Data Input Stream to the reference file from FileInput Stream.
                            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

                            // Read Data buffer using Buffer reader and read by line
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
                            String fileLine;

                            // Write Line To TextView. which will display stored information from text file.
                            while ((fileLine = bufferedReader.readLine()) != null) {
                                textView.setText(textView.getText().toString() + " " + fileLine);
                            }
                        } catch (FileNotFoundException e) {
                            // Catch File Not Found Exception.
                            textView.setText("File Not Found");
                            e.printStackTrace();
                        } catch (IOException e) {
                            // Catch IO Exception
                            textView.setText("IO Exception.");
                            e.printStackTrace();
                        }
                    else
                        // IN Case an file not found. return message to textview.
                        textView.setText("File Not Found" + textFile.toString());
                }
            });
            return null;
        }
    }

    // Validate External Storage Mode whether its read only or Writable ?
    private static boolean isExternalStorageReadOnly() {
        // Get External Storage State
        String extStorageState = Environment.getExternalStorageState();
        // Compare with Read only constant of Environment variable. return true if readonly
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        // False if Writable.
        return false;
    }

    // Validate Whether External Storage Exists or not.
    private static boolean isExternalStorageAvailable() {
        // Get External Storage State
        String extStorageState = Environment.getExternalStorageState();
        // Compare with Read only constant of Environment variable. return true if External Storage Exists.
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        // Return False if not exist.
        return false;
    }
}
