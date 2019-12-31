/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mohamed.blue11.Bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.LogPrinter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.blue11.FileDialog;
import com.example.mohamed.blue11.GraphViewer;
import com.example.mohamed.blue11.R;
import com.example.mohamed.blue11.WifiChatService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment implements Constants {

    Thread checkingInitThread;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    public ListView mConversationView;
    private Button mSendFileButton, mRunButton, mSaveButton, mContinuousRunningButton, mRecordButton;
    private TextView mChannel1;
    private TextView mChannel2;
    private TextView mChannel3;
    private TextView mChannel4;
    private TextView mChannel5;
    private TextView mChannel6;
    private TextView mChannel7;
    private TextView mChannel8;

    private TextView channel1Val, channel2Val, channel3Val, channel4Val, channel5Val, channel6Val, channel7Val, channel8Val;

    double[] channel = new double[9];

    //Edit Text for all channels to write the name of each channel
    public EditText EtChannel1, EtChannel2, EtChannel3, EtChannel4, EtChannel5, EtChannel6, EtChannel7, EtChannel8;   // developer mode
    public EditText EditChannel1, EditChannel2, EditChannel3, EditChannel4, EditChannel5, EditChannel6, EditChannel7, EditChannel8;  //user mode
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;


    private Double[][] dataCal = new Double[10][9];
    DecimalFormat dX = new DecimalFormat("##0.00000", new DecimalFormatSymbols(Locale.US));


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member objects for the Bluetooth and for wifi services
     */
    private BluetoothChatService mChatService = null;
    private WifiChatService mWifiChatService = null;

    WifiManager wifi;                           // Wifi manager to enable wifi
    Timer timer = new Timer();                  // Time label for incoming messages

    // Booleans to determine buttons press
    private Boolean isContinue = false;  //check if run continuously button is pressed
    private Boolean isSavingMulti = false;
    private Boolean isInit = false;
    private boolean timercancel = false;
    private boolean isRunning = false;  //check if data is transmitting

    // To check if file already exists
    private Boolean isfileExisted = false;
    private Boolean isfileSaver = false;
    private Boolean isfileRecorded = false;


    private Boolean autoSave = false;
    // server name used when updating the list view in the UI Activity
    private String mConnectedServer = null;
    /*
    /*
     * strings
     */
    private String runFile = "/Save & View 2015/SingleCommands.txt";
    private String content = "Date/Time\t";
    private String strCal;
    private String channelnames;

    private int sleeptime = 100;
    private CheckBox autosave;


    LinearLayout Linearlayout1, Linearlayout2;

    /*
     * counters
     */
    private int Runcount = 0;
    private int saveCount = 0;
    private int saveCountCont = 0;

    // LogFile to save the data in text file
    File logFileFilesaver = null;
    File logFileRecorder = null;

    // File dialog to navigate to a file
    private FileDialog fileDialog;
    private AlertDialog.Builder dialogBuilder;
    private String portNumber;

    // Get the default adapters: Bluetooth and Wifi
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(getActivity(),
                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get local wifi
        wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
        // If the adapter is null, then wifi is not supported
        if (wifi == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Wifi is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    private int STORAGE_PERMISSION_CODE = 1;


    // Turn BT on if it is not
    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }



    // Cancel bluetooth on destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
            mWifiChatService.stop();
        }
    }

    // Call start() on ChatService object to accept connection request (Server Part)
    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    // Inflate bluetooth layout on this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    // Setup ConversationView, Buttons and channels' Textviews and EditTexts
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mRunButton = (Button) view.findViewById(R.id.button_run);
        mContinuousRunningButton = (Button) view.findViewById(R.id.button_run_cont);
        mSaveButton = (Button) view.findViewById(R.id.button_save);
        mSendFileButton = (Button) view.findViewById(R.id.button_send_file);
        mRecordButton = (Button) view.findViewById(R.id.button_record);
        mChannel1 = (TextView) view.findViewById(R.id.TextView1_1);
        mChannel2 = (TextView) view.findViewById(R.id.TextView1_2);
        mChannel3 = (TextView) view.findViewById(R.id.TextView1_3);
        mChannel4 = (TextView) view.findViewById(R.id.TextView1_4);
        mChannel5 = (TextView) view.findViewById(R.id.TextView2_1);
        mChannel6 = (TextView) view.findViewById(R.id.TextView2_2);
        mChannel7 = (TextView) view.findViewById(R.id.TextView2_3);
        mChannel8 = (TextView) view.findViewById(R.id.TextView2_4);
        EtChannel1 = (EditText) view.findViewById(R.id.channel1);
        EtChannel2 = (EditText) view.findViewById(R.id.channel2);
        EtChannel3 = (EditText) view.findViewById(R.id.channel3);
        EtChannel4 = (EditText) view.findViewById(R.id.channel4);
        EtChannel5 = (EditText) view.findViewById(R.id.channel5);
        EtChannel6 = (EditText) view.findViewById(R.id.channel6);
        EtChannel7 = (EditText) view.findViewById(R.id.channel7);
        EtChannel8 = (EditText) view.findViewById(R.id.channel8);

        Linearlayout1 = (LinearLayout) view.findViewById(R.id.all);
        Linearlayout2 = (LinearLayout) view.findViewById(R.id.layoutAll2);

        Linearlayout1.setVisibility(View.INVISIBLE);
        mConversationView.setVisibility(View.INVISIBLE);


        EditChannel1 = (EditText) view.findViewById(R.id.ch1);   //user model
        //    EditChannel1.setMovementMethod(null);
        //     EditChannel1.setCursorVisible(true);
        EditChannel2 = (EditText) view.findViewById(R.id.ch2);
        EditChannel3 = (EditText) view.findViewById(R.id.ch3);
        EditChannel4 = (EditText) view.findViewById(R.id.ch4);
        EditChannel5 = (EditText) view.findViewById(R.id.ch5);
        EditChannel6 = (EditText) view.findViewById(R.id.ch6);
        EditChannel7 = (EditText) view.findViewById(R.id.ch7);
        EditChannel8 = (EditText) view.findViewById(R.id.ch8);

        channel1Val = (TextView) view.findViewById(R.id.ch1Val);   //developer model
        channel2Val = (TextView) view.findViewById(R.id.ch2Val);
        channel3Val = (TextView) view.findViewById(R.id.ch3Val);
        channel4Val = (TextView) view.findViewById(R.id.ch4Val);
        channel5Val = (TextView) view.findViewById(R.id.ch5Val);
        channel6Val = (TextView) view.findViewById(R.id.ch6Val);
        channel7Val = (TextView) view.findViewById(R.id.ch7Val);
        channel8Val = (TextView) view.findViewById(R.id.ch8Val);


        autosave = (CheckBox) view.findViewById(R.id.autosave);

        ReadCal();

        setChannelName();

    }

    public int setChannelName() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/Save & View 2015/Setup.txt");
        try {
            channelnames = readFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = channelnames.split("\n");
        EditChannel1.setText(lines[0].replace("\r", ""));
        EditChannel2.setText(lines[1].replace("\r", ""));
        EditChannel3.setText(lines[2].replace("\r", ""));
        EditChannel4.setText(lines[3].replace("\r", ""));
        EditChannel5.setText(lines[4].replace("\r", ""));
        EditChannel6.setText(lines[5].replace("\r", ""));
        EditChannel7.setText(lines[6].replace("\r", ""));
        EditChannel8.setText(lines[7].replace("\r", ""));

        String sleepPeriod = lines[8].substring(lines[8].indexOf("=") + 1).trim();
        sleeptime = Integer.parseInt(sleepPeriod);

        String port = lines[9];

        StringTokenizer st = new StringTokenizer(port);
        String[] portnumber = new String[st.countTokens() + 1];

        int index = 0;
        while (st.hasMoreTokens()) {
            portnumber[index] = st.nextToken();
            index++;
        }

        portNumber=portnumber[1];
        return sleeptime;
    }

    public int setChannelNameUp() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/Save & View 2015/Setup.txt");
        try {
            channelnames = readFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //   StringTokenizer st = new StringTokenizer(channelnames);
        String[] temp = channelnames.split("\n");

        String number = "";
        number = temp[8].substring(temp[8].lastIndexOf("=") + 1).trim();

        sleeptime = Integer.parseInt(number);

        return sleeptime;
    }


    public double CalculateAll(double rowData, int chName) {
        double CalData = dataCal[0][chName] + dataCal[1][chName] * rowData + dataCal[2][chName] * Math.pow(rowData, 2) + dataCal[3][chName] * Math.pow(rowData, 3) + dataCal[4][chName] * Math.pow(rowData, 4) + dataCal[5][chName]
                * Math.log(rowData + 0.000000000001) + dataCal[6][chName] * Math.exp((dataCal[7][chName] * rowData)) + dataCal[8][chName] * Math.pow(rowData, (dataCal[9][chName] + 0.000000000001));

        return CalData;
    }

    public void ReadCal() {

        File sdcard = Environment.getExternalStorageDirectory();
        // Create a command file with the specified name
        File file = new File(sdcard, "/Save & View 2015/Caldata.txt");
        File folder = new File("sdcard/Save & View 2015");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File fileInit = new File(sdcard, "/Save & View 2015/InitCommands.txt");
        //   File folder = new File("sdcard/DL141E");

        if (!fileInit.exists())
            try {
                fileInit.createNewFile();
                String content = "[o,0:1:2:3:4:5:6:7\r\n[c,1:1:0:1\r\n[a,1000\r\n[b,1\r\n[d\r\n[e\r\n";
                FileWriter fw = new FileWriter(fileInit.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        File fileCont = new File(sdcard, "/Save & View 2015/RecordCommands.txt");
        //   File folder = new File("sdcard/DL141E");
        if (!fileCont.exists())
            try {
                fileCont.createNewFile();
                //    String content = "[a,1000\r\n[b,5\r\n[d\r\n[e\r\n[s\r\n";
                String content = "[a,8000\r\n[b,1000\r\n[d\r\n[s\r\n";//
                FileWriter fw = new FileWriter(fileCont.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

///**************************************************************
        File setup = new File(sdcard, "/Save & View 2015/Setup.txt");
        //   File folder = new File("sdcard/DL141E");
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!setup.exists())
            try {
                setup.createNewFile();
                String content = "Channel 1\r\nChannel 2\r\nChannel 3\r\nChannel 4\r\nChannel 5\r\nChannel 6" +
                        "\r\nChannel 7\r\nChannel 8\r\nRun Period [ms]=1000\r\nPort 8080\r\n";
                FileWriter fw = new FileWriter(setup.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

//****************************************************************************'


        File fileRun = new File(sdcard, "/Save & View 2015/SingleCommands.txt");
        // File folder = new File("sdcard/DL141E");

        if (!fileRun.exists()) {
            try {
                fileRun.createNewFile();
                String content = "[a,8000\r\n[b,1\r\n[d\r\n[s\r\n";
                //    String content = "[a,1000\r\n[b,1\r\n[d\r\n[e\r\n[s\r\n";
                FileWriter fw = new FileWriter(fileRun.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                String content = "Ck\tChannel 1\tChannel 2\tChannel 3\tChannel 4\tChannel 5\tChannel 6\tChannel 7\tChannel 8\r\n" +
                        "Ck0\t-0.04\t-0.04\t-0.04\t-0.04\t-0.04\t-0.04\t-0.04\t-0.04\r\n" +
                        "Ck1\t0.000163\t0.000163\t0.000163\t0.000163\t0.000163\t0.000163\t0.000163\t0.000163\r\n" +
                        "Ck2\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck3\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck4\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck5\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck6\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck7\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck8\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n" +
                        "Ck9\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\t0.0\r\n";
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        try {
            strCal = readFromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String CalMessage = strCal.substring(strCal.indexOf("Ck0"), strCal.length() - 1);
        StringTokenizer st = new StringTokenizer(CalMessage);
        String[] split = new String[st.countTokens() + 1];

        int index = 0;
        while (st.hasMoreTokens()) {
            split[index] = st.nextToken();
            index++;
        }

        String caldata[][] = new String[10][9];

        int dataIndex = 0;
        for (int samplesRow = 0; samplesRow < 10; samplesRow++) {
            for (int samplesColumn = 0; samplesColumn < 9; samplesColumn++) {
                if (dataIndex < split.length - 1) {
                    caldata[samplesRow][samplesColumn] = (split[dataIndex]);
                    if (split[dataIndex].contains(".")) {

                        dataCal[samplesRow][samplesColumn] = Double.parseDouble(split[dataIndex]);
                        }

                    dataIndex++;
                }
            }
        }


    }


    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {


        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        // set the conversation adapters
        mConversationView.setAdapter(mConversationArrayAdapter);


        autosave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read configuration file and execute commands in there.
                if (autosave.isChecked()) {

                    autoSave = true;
                } else {
                    autoSave = false;
                }


            }
        });


        //  mConversationView.setVisibility(View.GONE);
        // Initialize the Run button with a listener that for click events
        mRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read configuration file and execute commands in there.
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    isInit = false;
                    saveCountCont = 0;
                    saveCount = 0;
                    isSavingMulti = false;
                    isfileRecorded = false;

                    CheckRunFile crf = new CheckRunFile(runFile);
                    new Thread(crf).start();

                }
            }
        });

        // Initialize the Record button with a listener that for click events
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    isInit = false;
                    isfileRecorded = true;
                    // Access the SD-card
                    File sdcard = Environment.getExternalStorageDirectory();
                    // Create a command file with the specified name
                    File file = new File(sdcard, "Save & View 2015/RecordCommands.txt");

                    if (!file.exists())
                        try {
                            file.createNewFile();
                            String content = "[a,8000\r\n[b,1000\r\n[d\r\n[e\r\n[s\r\n";
                            FileWriter fw = new FileWriter(file.getAbsoluteFile());
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(content);
                            bw.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    popupRecord();


                    saveCountCont++;
                    isSavingMulti = true;

                }
            }
        });

        // Initialize 'ContinuousRunning' button with a listener that for click events
        mContinuousRunningButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isSavingMulti = false;
                saveCountCont = 0;
                isInit = false;
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    // Notify the user if bluetooth not connected and return
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                } else if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {

                    isContinue = true;
                    Runcount++;
                    if (Runcount == 1) {
                        //    timercancel = false;
                        if (timercancel) {
                            timer = new Timer();
                            timer.schedule(new ScheduledTask(), 0, sleeptime); //run commands every 1 second continuously
                        } else {
                            timer.schedule(new ScheduledTask(), 0, sleeptime);
                        }
                    }
                    if (Runcount == 2) {
                        timer.cancel();
                        timercancel = true;
                        Runcount = 0;
                        isContinue = false;
                   //     isSavingMulti=false;
                    }
                }
            }
        });

        // Initialize 'Save' button with a listener that for click events
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCountCont = 0;
                isSavingMulti = false;
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;

                } else if (!isRunning) {
                    Toast.makeText(getActivity(), "No data has been obtained..", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isRunning && !isContinue && !autoSave) {
                    saveCount++;
                    switch (saveCount) {
                        case 1:
                            SaveData();         // save the data that is currently displayed.
                            break;
                        case 2:
                            Toast.makeText(getActivity(), "Data has already been saved", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getActivity(), "Data has already been saved", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else if (isRunning && isContinue) {
                    SaveData();                 // save the data that is currently displayed.
                }

            }
        });
// Initialize 'SendFile' button with a listener that for click events
        mSendFileButton.setOnClickListener(new View.OnClickListener() {

            //  String fileName;
            public void onClick(View v) {
                final File mPath = new File(Environment.getExternalStorageDirectory()
                        + "//DIR//");
                fileDialog = new FileDialog(getActivity(), mPath);
                fileDialog.setFileEndsWith(".txt");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public void fileSelected(File files) {
                        Log.d(getClass().getName(), "selected file " + files.toString());

                        File file = new File(files.toString());

                        String message = null;

                        try {
                            message = readFromFile(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String filePath = files.toString();
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        mWifiChatService.writeInteger(message);
                        mWifiChatService.writeInteger(fileName);
                        mWifiChatService.write(fileName.getBytes());
                        mWifiChatService.write(message.getBytes());

                        if (mWifiChatService.getState() == WifiChatService.STATE_CONNECTED) {
                             Toast toast = Toast.makeText(getActivity(), fileName + " is sent", Toast.LENGTH_SHORT);   //20160215
                            toast.show();
                        }
                        else{
                            Toast toast = Toast.makeText(getActivity(), "Not connected to server", Toast.LENGTH_SHORT);   //20160215
                            toast.show();
                        }
                    }
                });

                fileDialog.showDialog();


            }
        });
        // Initialize BluetoothChatService object for bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);
        // Initialize WifiChatService object for Wifi connections
        mWifiChatService = new WifiChatService(getActivity(), nHandler);
    }


    /************************************************************************
     * Namn: readFromFile
     * <p/>
     * Purpose: Creat file inputstream and read copntent from the file as format of String
     * <p/>
     * Input: File
     * <p/>
     * Output: String
     * <p/>
     * Invocation:
     ************************************************************************/

    public String readFromFile(File file) throws IOException {
        String ret = "";
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } finally {
            in.close();
        }
        ret = new String(bytes);
        return ret;
    }


    /************************************************************************
     * Namn: SaveData
     * <p/>
     * Purpose: Save the current displaying data to the log file
     * <p/>
     * Input: void
     * <p/>
     * Output: void
     * <p/>
     * Invocation: appendLog
     ************************************************************************/

    public void SaveData() {
        saveCountCont = 0;
        if (!autoSave) {
            String log1 = mChannel1.getText().toString();
            String log2 = mChannel2.getText().toString();
            String log3 = mChannel3.getText().toString();
            String log4 = mChannel4.getText().toString();
            String log5 = mChannel5.getText().toString();
            String log6 = mChannel6.getText().toString();
            String log7 = mChannel7.getText().toString();
            String log8 = mChannel8.getText().toString();
            String TotalLog = log1 + "\t" + log2 + "\t" + log3 + "\t" + log4 + "\t" + log5 + "\t" + log6 + "\t" + log7 + "\t" + log8 + "\r";
            appendLog(TotalLog);
            final Toast toast = Toast.makeText(getActivity(), "Data is saved", Toast.LENGTH_SHORT);   //20160215
            toast.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 200);
        }

    }

    public void AutoSave() {
        String log1 = mChannel1.getText().toString() + "\t";
        String log2 = mChannel2.getText().toString() + "\t";
        String log3 = mChannel3.getText().toString() + "\t";
        String log4 = mChannel4.getText().toString() + "\t";
        String log5 = mChannel5.getText().toString() + "\t";
        String log6 = mChannel6.getText().toString() + "\t";
        String log7 = mChannel7.getText().toString() + "\t";
        String log8 = mChannel8.getText().toString() + "\r";
        String TotalLog = log1 + log2 + log3 + log4 + log5 + log6 + log7 + log8;
        appendLog(TotalLog);
        final Toast toast = Toast.makeText(getActivity(), "Data is saved", Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 200);
    }


    /************************************************************************
     * Creat a nested class to execute soecific commands in certain intervals
     ************************************************************************/
    public class ScheduledTask extends TimerTask {
        public void run() {
            //     CheckRunFile crf=new CheckRunFile(runFile);
            //     new Thread(crf).start();
            RunFile(runFile);
        }
    }


    /************************************************************************
     * Namn: RunFile
     * <p/>
     * Purpose: send a command to the display *
     * <p/>
     * Input: corresponding command file that will be executed
     * <p/>
     * Output: void
     * <p/>
     * Invocation: sendMessage()
     ************************************************************************/

    public void RunFile(String fileName) {
        File sdcard = Environment.getExternalStorageDirectory();
        //Get the text file
        File file = new File(sdcard, fileName);
        File folder = new File("sdcard/Save & View 2015");
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[s")) {

                } else {
                    sendMessage(line + "\r");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
    }

    /*
     * Handler object or 'receiver' which will handle the messages that are sent through
     * wifi from the server. According to those messages, UI Activity will be updated
     * all sent and received messages will be showed on list view as well as it can display
     * different toasts to the user
     */
    Handler nHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case WifiChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedServer));
                            mConversationArrayAdapter.clear();
                            break;
                        case WifiChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case WifiChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    // display the sent message on the list view
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // display the received message on the list view
                    mConversationArrayAdapter.add(mConnectedServer + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save name of the connected device
                    mConnectedServer = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedServer, Toast.LENGTH_SHORT).show();
                    }
                    break;
                // Display toast with the content of the message 'msg'
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            isRunning = true;
        }
    }


    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    public int getRecord() {
        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/Save & View 2015/RecordCommands.txt");
        try {
            channelnames = readFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringTokenizer st = new StringTokenizer(channelnames);
        String number = "NULL";
        if (st.hasMoreTokens()) {
            while (st.hasMoreTokens()) {
                st.nextToken();
                number = st.nextToken();
                break;
            }
        }
        number = number.substring(3);
        int sampleNumber = Integer.parseInt(number);
        return sampleNumber;
    }

    public String checkingMSG = "";
    /**
     * The Handler that gets information back from the BluetoothChatService
     * its function is similar to the above mentioned handler for wifi
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    // Extract time of the incoming data from the message

                    checkingMSG = readMessage;


                    if (readMessage.contains("END")) {
                        if (isSavingMulti) {
                            // extract the message and save each string in 'split' array
                            String logMessage = readMessage.substring(0, readMessage.indexOf("END"));
                            StringTokenizer st = new StringTokenizer(logMessage);
                            String[] split = new String[st.countTokens() + 1];
                            Double[] dsplit = new Double[st.countTokens() + 1];
                            int index = 0;
                            while (st.hasMoreTokens()) {
                                split[index] = st.nextToken();
                                dsplit[index] = Double.parseDouble(split[index]);
                                index++;
                            }

                            int noOfRows = (split.length - 1) / 8;
                            if ((split.length - 1) == 8 * getRecord()) {
                                Double[][] data = new Double[noOfRows][8];
                                int dataIndex = 0;
                                String temp = "";
                                for (int samplesRow = 0; samplesRow < noOfRows; samplesRow++) {
                                    for (int samplesColumn = 0; samplesColumn < 8; samplesColumn++) {
                                        if (dataIndex < split.length - 1) {
                                            data[samplesRow][samplesColumn] = Double.parseDouble(split[dataIndex]);
                                            double result = CalculateAll(data[samplesRow][samplesColumn], samplesColumn + 1);
                                            temp += String.valueOf(dX.format(result) + "\t");
                                            channel[samplesColumn] += result;
                                            dataIndex++;
                                        }
                                    }
                                    appendLog(temp + "\r");  ////////////latest change  point 1
                                    temp = "";
                                }
                                for (int i = 0; i < channel.length; i++) {
                                    channel[i] /= (noOfRows);
                                }
                                mChannel1.setText(dX.format(channel[0]));
                                channel1Val.setText(dX.format(channel[0]));

                                mChannel2.setText(dX.format(channel[1]));
                                channel2Val.setText(dX.format(channel[1]));

                                mChannel3.setText(dX.format(channel[2]));
                                channel3Val.setText(dX.format(channel[2]));

                                mChannel4.setText(dX.format(channel[3]));
                                channel4Val.setText(dX.format(channel[3]));

                                mChannel5.setText(dX.format(channel[4]));
                                channel5Val.setText(dX.format(channel[4]));

                                mChannel6.setText(dX.format(channel[5]));
                                channel6Val.setText(dX.format(channel[5]));

                                mChannel7.setText(dX.format(channel[6]));
                                channel7Val.setText(dX.format(channel[6]));

                                mChannel8.setText(dX.format(channel[7]));
                                channel8Val.setText(dX.format(channel[7]));
                                final Toast toast = Toast.makeText(getActivity(), "Data saving....", Toast.LENGTH_SHORT);
                                toast.show();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        toast.cancel();
                                    }
                                }, 200);
                                // update channels 1-8 with the new data without saving data in a text file
                            }
                            if ((split.length - 1) != 8 * getRecord()) {
                                CheckRunFile crf = new CheckRunFile("Save & View 2015/RecordCommands.txt");
                                new Thread(crf).start();
                            }
                            isSavingMulti = false;

                            sendM();
                            isRunning = false;

                        } else if ((!isSavingMulti) && (saveCountCont == 0)) {
                            // extract the message and save each word in 'split' array

                            saveCount = 0;
                            StringTokenizer st = new StringTokenizer(readMessage);
                            String[] split = new String[st.countTokens()];
                            int index = 0;
                            while (st.hasMoreTokens()) {
                                split[index] = st.nextToken();
                                index++;
                            }
                            int columns = (split.length - 1) / 8;
                            Double[][] data = new Double[columns][8];
                            int dataIndex = 0;
                            for (int samplesRow = 0; samplesRow < columns; samplesRow++) {
                                for (int samplesColumn = 0; samplesColumn < 8; samplesColumn++) {
                                    if (dataIndex < split.length - 1) {
                                        data[samplesRow][samplesColumn] = Double.parseDouble(split[dataIndex]);
                                        if (samplesColumn == 0) {
                                            mChannel1.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 1))));
                                            channel1Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 1))));
                                        }
                                        if (samplesColumn == 1) {
                                            mChannel2.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 2))));
                                            channel2Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 2))));
                                        }
                                        if (samplesColumn == 2) {
                                            mChannel3.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 3))));
                                            channel3Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 3))));
                                        }
                                        if (samplesColumn == 3) {
                                            mChannel4.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 4))));
                                            channel4Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 4))));
                                        }
                                        if (samplesColumn == 4) {
                                            mChannel5.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 5))));
                                            channel5Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 5))));
                                        }
                                        if (samplesColumn == 5) {
                                            mChannel6.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 6))));
                                            channel6Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 6))));
                                        }
                                        if (samplesColumn == 6) {
                                            mChannel7.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 7))));
                                            channel7Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 7))));
                                        }
                                        if (samplesColumn == 7) {
                                            mChannel8.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 8))));
                                            channel8Val.setText(String.valueOf(dX.format(CalculateAll(data[samplesRow][samplesColumn], 8))));
                                        }
                                        dataIndex++;
                                    }
                                }
                            }

                            if (split.length - 1 != 8) {
                                CheckRunFile crf = new CheckRunFile("Save & View 2015/RecordCommands.txt");
                                new Thread(crf).start();
                            }

                            if (autoSave && (split.length - 1 == 8)) {
                                AutoSave();
                            }
                            if (!isInit) {
                                sendM();

                            }
                        }

                    }
                    // Modify here to display in new intent
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        //RunInitFile("Save & View 2015/InitCommands.txt");
                        isInit = true;
                        checkingInitThread = new Thread(new CheckInitFile());
                        checkingInitThread.start();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /*
     * Based on the result of some activities, take the proper action
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }


    /**
     * Establish connection with other divice
     *
     * @param data   An {@link android.content.Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    /************************************************************************
     * Inflate actionbar for options
     ************************************************************************/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }


    /************************************************************************
     * Namn:appendLog
     * <p/>
     * Purpose: Append data to the local log file
     * <p/>
     * Input: Various option for user to select on actionbar
     * <p/>
     * Output: boolean
     * <p/>
     * Invocation: popupFilesaver
     ************************************************************************/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.connect_to_bluetooth: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                return true;
            }
            case R.id.disconnect_bluetooth: {
                if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    mChatService.stop();
                    Toast.makeText(getActivity(), "Bluetooth is disconnected", Toast.LENGTH_LONG).show();
                    mChannel1.setText("0.0");
                    mChannel2.setText("0.0");
                    mChannel3.setText("0.0");
                    mChannel4.setText("0.0");
                    mChannel5.setText("0.0");
                    mChannel6.setText("0.0");
                    mChannel7.setText("0.0");
                    mChannel8.setText("0.0");

                    channel1Val.setText("0.0");
                    channel2Val.setText("0.0");
                    channel3Val.setText("0.0");
                    channel4Val.setText("0.0");
                    channel5Val.setText("0.0");
                    channel6Val.setText("0.0");
                    channel7Val.setText("0.0");
                    channel8Val.setText("0.0");
                } else {

                    Toast.makeText(getActivity(), "Bluetooth is already disconnected", Toast.LENGTH_LONG).show();
                }

                return true;

            }
            case R.id.connect_to_server: {

                dialogBuilder = new AlertDialog.Builder(getActivity());
                final EditText inputText = new EditText(getActivity());
                dialogBuilder.setTitle("IP server");
                dialogBuilder.setMessage("Enter Server's IP address :");
                dialogBuilder.setView(inputText);
                dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String ip = inputText.getText().toString().trim();
                        mWifiChatService.connect(ip);
                        new Thread(new DBrequest()).start();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Connecting to the server is cancelled..", Toast.LENGTH_LONG).show();
                    }
                });


                AlertDialog dialog = dialogBuilder.create();
                dialog.show();


                return true;
            }
            case R.id.disconnect_server: {

                if (mWifiChatService.getState() == WifiChatService.STATE_CONNECTED) {
                    Constants.sensorList.clear();
                    mWifiChatService.stop();
                } else {

                    Toast.makeText(getActivity(), "Server is not connected..", Toast.LENGTH_LONG).show();
                }
                return true;
            }
            case R.id.graph: {
                Intent i = new Intent(getActivity(), GraphViewer.class);

                if (mConversationView.getVisibility() == View.VISIBLE) {
                    i.putExtra("channel1", EtChannel1.getText().toString());
                    i.putExtra("channel2", EtChannel2.getText().toString());
                    i.putExtra("channel3", EtChannel3.getText().toString());
                    i.putExtra("channel4", EtChannel4.getText().toString());
                    i.putExtra("channel5", EtChannel5.getText().toString());
                    i.putExtra("channel6", EtChannel6.getText().toString());
                    i.putExtra("channel7", EtChannel7.getText().toString());
                    i.putExtra("channel8", EtChannel8.getText().toString());
                } else {

                    i.putExtra("channel1", EditChannel1.getText().toString());
                    i.putExtra("channel2", EditChannel2.getText().toString());
                    i.putExtra("channel3", EditChannel3.getText().toString());
                    i.putExtra("channel4", EditChannel4.getText().toString());
                    i.putExtra("channel5", EditChannel5.getText().toString());
                    i.putExtra("channel6", EditChannel6.getText().toString());
                    i.putExtra("channel7", EditChannel7.getText().toString());
                    i.putExtra("channel8", EditChannel8.getText().toString());
                }
                startActivity(i);

                return true;
            }


            case R.id.filesave: {

                popupFilesaver();
                isfileSaver = false;
                return true;
            }

            case R.id.developer: {
                //develop mode to user mode
                if (mConversationView.getVisibility() == View.VISIBLE) {
                    mConversationView.setVisibility(View.GONE);
                    Linearlayout1.setVisibility(View.GONE);
                    Linearlayout2.setVisibility(View.VISIBLE);
                    EditChannel1.setText(EtChannel1.getText().toString());
                    EditChannel2.setText(EtChannel2.getText().toString());
                    EditChannel3.setText(EtChannel3.getText().toString());
                    EditChannel4.setText(EtChannel4.getText().toString());
                    EditChannel5.setText(EtChannel5.getText().toString());
                    EditChannel6.setText(EtChannel6.getText().toString());
                    EditChannel7.setText(EtChannel7.getText().toString());
                    EditChannel8.setText(EtChannel8.getText().toString());
                } else {
                    //user mode to developer mode
                    mConversationView.setVisibility(View.VISIBLE);
                    Linearlayout1.setVisibility(View.VISIBLE);
                    Linearlayout2.setVisibility(View.GONE);
                    EtChannel1.setText(EditChannel1.getText().toString());
                    EtChannel2.setText(EditChannel2.getText().toString());
                    EtChannel3.setText(EditChannel3.getText().toString());
                    EtChannel4.setText(EditChannel4.getText().toString());
                    EtChannel5.setText(EditChannel5.getText().toString());
                    EtChannel6.setText(EditChannel6.getText().toString());
                    EtChannel7.setText(EditChannel7.getText().toString());
                    EtChannel8.setText(EditChannel8.getText().toString());

                }

                return true;

            }



           /* case R.id.credits: {
                Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                dialog.setContentView(R.layout.credits);
                dialog.show();


                return true;
            }*/


            case R.id.exit: {
                if (Runcount == 2) {
                    timer.cancel();
                }
                if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
                    mChatService.stop();
                }
                if (mWifiChatService.getState() == WifiChatService.STATE_CONNECTED) {
                    mWifiChatService.stop();
                }

                int value = setChannelNameUp();
                //   File logFile = new File("sdcard/tio.csv");
                File logFile = new File("sdcard/Save & View 2015/Setup.txt");
                //  if (!logFile.exists())
                try {
                    logFile.createNewFile();
                    FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    if (Linearlayout1.getVisibility() == View.VISIBLE) {
                        bw.write(getChannelnameDev().replaceAll("\t", "\r\n") + "Run Period [ms]=" + value+"\r\nPort "+portNumber+"\r\n");
                    } else {
                        bw.write(getChannelname().replaceAll("\t", "\r\n") + "Run Period [ms]=" + value+"\r\nPort "+portNumber+"\r\n");
                    }
                    bw.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                FragmentActivity activity = getActivity();
                Toast.makeText(activity, "Application exited", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        }
        return false;
    }


    public String getChannelnameDev() {

        String channelname = EtChannel1.getText().toString() + "\t" + EtChannel2.getText().toString() + "\t"
                + EtChannel3.getText().toString() + "\t" + EtChannel4.getText().toString() + "\t" +
                EtChannel5.getText().toString() + "\t" + EtChannel6.getText().toString() + "\t"
                + EtChannel7.getText().toString() + "\t" + EtChannel8.getText().toString() + "\r\n";

        return channelname;
    }




     public String getChannelname() {

        String channelname = EditChannel1.getText().toString() + "\t" + EditChannel2.getText().toString() + "\t"
                + EditChannel3.getText().toString() + "\t" + EditChannel4.getText().toString() + "\t" +
                EditChannel5.getText().toString() + "\t" + EditChannel6.getText().toString() + "\t"
                + EditChannel7.getText().toString() + "\t" + EditChannel8.getText().toString() + "\r\n";

        return channelname;
    }


    /************************************************************************
     * Namn:popupFilesaver
     * <p/>
     * Purpose: Popup up a dialogue for use to enter desired save path
     * <p/>
     * Input: void
     * <p/>
     * Output: void
     * <p/>
     * Invocation:Runfile
     ************************************************************************/

    public boolean popupFilesaver() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String fullPath = userInput.getText().toString().trim();
                                isfileExisted = true;
                                isfileSaver = true;
                                File folder = new File("sdcard/Save & View 2015/");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                logFileFilesaver = new File("sdcard/Save & View 2015/" + fullPath + ".txt");
                                if (!logFileFilesaver.exists()) {
                                    try {
                                        logFileFilesaver.createNewFile();
                                        FileWriter fw = new FileWriter(logFileFilesaver.getAbsoluteFile());
                                        BufferedWriter bw = new BufferedWriter(fw);

                                        if (Linearlayout1.getVisibility() == View.VISIBLE) {
                                            bw.write(content + getChannelnameDev());
                                        }
                                        if (Linearlayout2.getVisibility() == View.VISIBLE) {
                                            bw.write(content + getChannelname());
                                        }
                                        bw.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (isSavingMulti && saveCountCont > 0) {
                                    CheckRunFile crf = new CheckRunFile("Save & View 2015/RecordCommands.txt");
                                    new Thread(crf).start();
                                    //      RunFile("Save & View 2015/RecordCommands.txt");
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        return true;
    }


    public boolean popupRecord() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.prompts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                isfileExisted = true;
                                isfileRecorded = true;
                                String fullPath = userInput.getText().toString().trim();
                                File folder = new File("sdcard/Save & View 2015/");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                logFileRecorder = new File("sdcard/Save & View 2015/" + fullPath + ".txt");
                                if (!logFileRecorder.exists()) {
                                    try {
                                        logFileRecorder.createNewFile();
                                        FileWriter fw = new FileWriter(logFileRecorder.getAbsoluteFile());
                                        BufferedWriter bw = new BufferedWriter(fw);

                                        if (Linearlayout1.getVisibility() == View.VISIBLE) {
                                            bw.write(content + getChannelnameDev());
                                        }
                                        if (Linearlayout2.getVisibility() == View.VISIBLE) {
                                            bw.write(content + getChannelname());
                                        }
                                        bw.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (isSavingMulti && saveCountCont > 0) {
                                    //        RunFile("Save & View 2015/RecordCommands.txt");

                                    CheckRunFile crf = new CheckRunFile("Save & View 2015/RecordCommands.txt");
                                    new Thread(crf).start();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        return true;
    }


    /************************************************************************
     * Namn:appendLog
     * <p/>
     * Purpose: Append data to the local log file
     * <p/>
     * Input: the data that is recived via bluetooth
     * <p/>
     * Output: void
     * <p/>
     * Invocation:void
     * Invocation:void
     ************************************************************************/
    public void appendLog(String data) {

        if ((!isfileExisted) || (isfileExisted && !isfileRecorded && !isfileSaver)) {
            //   File logFile = new File("sdcard/tio.csv");
            File logFile = new File("sdcard/Save & View 2015/Logdata.txt");
            if (!logFile.exists())
                try {
                    logFile.createNewFile();
                    FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    if (Linearlayout1.getVisibility() == View.VISIBLE) {
                        bw.write(content + getChannelnameDev());

                    }

                    if (Linearlayout2.getVisibility() == View.VISIBLE) {
                        bw.write(content + getChannelname());
                    }
                    bw.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            BufferedWriter buf = null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
            Date date = new Date();
            try {
                buf = new BufferedWriter(new FileWriter(logFile, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                buf.append(dateFormat.format(date) + "\t" + data);
                buf.newLine();
                buf.flush();
                //  buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isfileExisted && isfileRecorded) {

            BufferedWriter buf = null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
            Date date = new Date();
            try {
                buf = new BufferedWriter(new FileWriter(logFileRecorder, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                buf.append(dateFormat.format(date) + "\t" + data);
                buf.newLine();
                buf.flush();
                //buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //  isfileExisted = false;

        }
        if (isfileExisted && isfileSaver && !isfileRecorded) {
            BufferedWriter buf = null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
            Date date = new Date();
            try {
                buf = new BufferedWriter(new FileWriter(logFileFilesaver, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                buf.append(dateFormat.format(date) + "\t" + data);
                buf.newLine();
                buf.flush();
                //buf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Thread to send a request to the server for DB "data base"
     * when it is started, it will keep listening to the database button
     * in the 'GraphViewer' class. If that button is tabbed, it sends '$'
     * character to the server as a request for the DB
     */
    class DBrequest implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (Constants.isDbRequested[0]) {
                    mWifiChatService.write(new String("$   ").getBytes());
                    Constants.isDbRequested[0] = false;
                }
            }
        }
    }

    public void sendM() {
        sendMessage("[s\r");
    }

    private class CheckInitFile implements Runnable {
        @Override
        public void run() {
            File sdcard = Environment.getExternalStorageDirectory();
            //Get the text file
            String initfile = "";
            File file = new File(sdcard, "Save & View 2015/InitCommands.txt");
            File folder = new File("sdcard/Save & View 2015");
            if (!folder.exists()) {
                folder.mkdir();
            }
            try {
                initfile = readFromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] initLines = initfile.split("\n");
            sendMessage(initLines[0]);
            while (!checkingMSG.equals("OK")) {

                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[0]);
                }
            }
            checkingMSG = "";
            sendMessage(initLines[1]);
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[1]);
                }
            }
            checkingMSG = "";
            sendMessage(initLines[2]);
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[2]);
                }
            }
            checkingMSG = "";
            sendMessage(initLines[3]);
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[3]);
                }
            }
            checkingMSG = "";
            sendMessage(initLines[4]);
            while (!checkingMSG.contains("END")) {
                if (checkingMSG.contains("ERR")) {
                    sendMessage(initLines[4]);
                }
            }
            checkingMSG = "";
            sendMessage(initLines[5]);
            while (!checkingMSG.contains("END")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[5]);
                }
            }
        }

    }

    private class CheckRunFile implements Runnable {

        private String filename;

        public CheckRunFile(String filename) {
            // store parameter for later user
            this.filename = filename;
        }

        @Override
        public void run() {
            runfile(filename);
        }

        public void runfile(String filename) {

            File sdcard = Environment.getExternalStorageDirectory();
            String fileF = "";
            File file = new File(sdcard, filename);
            File folder = new File("sdcard/Save & View 2015");
            if (!folder.exists()) {
                folder.mkdir();
            }
            try {
                fileF = readFromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] initLines = fileF.split("\n");
            sendMessage(initLines[0].trim() + "\r");
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[0].trim() + "\r");
                }
            }
            checkingMSG = "";
            sendMessage(initLines[1].trim() + "\r");
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[1].trim() + "\r");
                }
            }
            checkingMSG = "";
            sendMessage(initLines[2].trim() + "\r");
            while (!checkingMSG.equals("OK")) {
                if (checkingMSG.equals("ERR")) {
                    sendMessage(initLines[2].trim() + "\r");
                }
            }

        }

    }
}
