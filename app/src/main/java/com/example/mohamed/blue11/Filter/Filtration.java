package com.example.mohamed.blue11.Filter;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//
import com.example.mohamed.blue11.Bluetooth.Constants;
import com.example.mohamed.blue11.Bluetooth.Sensor;
import com.example.mohamed.blue11.FileDialog;
import com.example.mohamed.blue11.GraphViewer;
import com.example.mohamed.blue11.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;


public class Filtration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final int[] mRadioButtonsId = {R.id.LP_RadioBtn, R.id.HP_RadioBtn,
            R.id.BP_RadioBtn, R.id.BS_RadioBtn, R.id.MV_RadioBtn};
    DecimalFormat dX = new DecimalFormat("##0.00000", new DecimalFormatSymbols(Locale.US));
    DateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
    File file;
    Spinner spinnerMin_Stopband_Atten, spinnerTran_Band_Level, spinnerFilter_Point;
    String cutoff, min_stopband_atten, filter_point;
    GraphViewer gv = new GraphViewer();
    File mPath = new File(Environment.getExternalStorageDirectory()
            + "//DIR//");
    boolean isLowpass, isHighpass, isBandpass, isBandstop, isMovingAverage;
    String filterSpec = "";
    ArrayAdapter<CharSequence> MinStopAtten_adapter, LP_and_HP_adapter, BP_and_BS_adapter, MV_adapter;
    // Mutex to share between the threads waiting for the result.
    Object mutex = new Object();
    private double[] h =//moving average coefficients
            {
            };
    private ArrayList<Sensor> sensorListFiltered = new ArrayList<Sensor>();
    //Buttons
    private Button apply_btn, select_btn, save_btn;
    private RadioButton LP_RadioBtn, HP_RadioBtn, BP_RadioBtn, BS_RadioBtn, MV_RadioBtn;
    private FileDialog dialog;
    private String fileName;
    private int filterTaps;
    private TextView min_stopband_atten_TxTView, trans_band_level_TxTView, filter_Point_TxTView;
    // Listener for radio buttons
    private View.OnClickListener radioListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RadioButton clicked_radioButton = (RadioButton) view;
            int radioButtonId = clicked_radioButton.getId();
            // Clear other unclicked radio buttons and set spinners according to the selected radio button
            for (int i : mRadioButtonsId) {
                if (i != radioButtonId) {
                    RadioButton rb = (RadioButton) findViewById(i);
                    if (rb.isChecked()) {
                        rb.setChecked(false);
                    }
                }
                switch (radioButtonId) {
                    case R.id.LP_RadioBtn: // Lowpass seleceted
                        // Set LP dropdown menu
                        spinnerMin_Stopband_Atten.setEnabled(true);
                        spinnerTran_Band_Level.setEnabled(true);
                        spinnerTran_Band_Level.setAdapter(LP_and_HP_adapter);
                        spinnerFilter_Point.setEnabled(false);
                        break;
                    case R.id.HP_RadioBtn: // Highpass seleceted
                        // Set HP dropdown menu
                        spinnerMin_Stopband_Atten.setEnabled(true);
                        spinnerTran_Band_Level.setEnabled(true);
                        spinnerTran_Band_Level.setAdapter(LP_and_HP_adapter);
                        spinnerFilter_Point.setEnabled(false);
                        break;
                    case R.id.BP_RadioBtn: //Bandpass selected
                        // Set BP dropdown menu
                        spinnerMin_Stopband_Atten.setEnabled(true);
                        spinnerTran_Band_Level.setEnabled(true);
                        spinnerTran_Band_Level.setAdapter(BP_and_BS_adapter);
                        spinnerFilter_Point.setEnabled(false);
                        break;
                    case R.id.BS_RadioBtn: // Bandpass selected
                        // Set BS dropdown menu
                        spinnerMin_Stopband_Atten.setEnabled(true);
                        spinnerTran_Band_Level.setEnabled(true);
                        spinnerTran_Band_Level.setAdapter(BP_and_BS_adapter);
                        spinnerFilter_Point.setEnabled(false);
                        break;
                    case R.id.MV_RadioBtn: // Moving Average selected
                        // Set Moving Average dropdown menu
                        spinnerMin_Stopband_Atten.setEnabled(false);
                        spinnerTran_Band_Level.setEnabled(false);
                        spinnerFilter_Point.setEnabled(true);
                        break;
                }
            }
        }
    };

    // set the layout 'Filter Settings'
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.filtration_layout);
        isLowpass = true;
        //get references to all layout objects
        min_stopband_atten_TxTView = (TextView) findViewById(R.id.Min_SB_Atten_TextView);
        // Radio buttons
        LP_RadioBtn = (RadioButton) findViewById(R.id.LP_RadioBtn);
        HP_RadioBtn = (RadioButton) findViewById(R.id.HP_RadioBtn);
        BP_RadioBtn = (RadioButton) findViewById(R.id.BP_RadioBtn);
        BS_RadioBtn = (RadioButton) findViewById(R.id.BS_RadioBtn);
        MV_RadioBtn = (RadioButton) findViewById(R.id.MV_RadioBtn);

        // Buttons
        apply_btn = (Button) findViewById(R.id.apply_btn);
        select_btn = (Button) findViewById(R.id.select_btn);
        select_btn.setEnabled(false);
        save_btn = (Button) findViewById(R.id.filtration_btn);
        save_btn.setEnabled(false);

        // Spinners of the dropdown menus
        spinnerMin_Stopband_Atten = (Spinner) findViewById(R.id.Min_SB_Atten_Spinner);
        spinnerTran_Band_Level = (Spinner) findViewById(R.id.Trans_Band_Level_Spinner);
        spinnerFilter_Point = (Spinner) findViewById(R.id.Filter_Point_Spinner);
        spinnerFilter_Point.setEnabled(false);

        spinnerMin_Stopband_Atten.setOnItemSelectedListener(this);
        spinnerTran_Band_Level.setOnItemSelectedListener(this);
        spinnerFilter_Point.setOnItemSelectedListener(this);

        // Create ArrayAdapters using the string array and a default spinner layout
        MinStopAtten_adapter = ArrayAdapter.createFromResource(this,
                R.array.attenuation_array, android.R.layout.simple_spinner_item);
        LP_and_HP_adapter = ArrayAdapter.createFromResource(this,
                R.array.cut_off_LP_and_HP, android.R.layout.simple_spinner_dropdown_item);
        BP_and_BS_adapter = ArrayAdapter.createFromResource(this,
                R.array.cut_off_BP_and_BS, android.R.layout.simple_spinner_dropdown_item);
        MV_adapter = ArrayAdapter.createFromResource(this,
                R.array.MV_length, android.R.layout.simple_spinner_dropdown_item);

        // Specify the layout to use when the list of choices appears
        MinStopAtten_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        LP_and_HP_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// Both LP and HP use the same spinner list
        BP_and_BS_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// Both BP and BS use the same spinner list
        MV_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerMin_Stopband_Atten.setAdapter(MinStopAtten_adapter);
        spinnerTran_Band_Level.setAdapter(LP_and_HP_adapter);
        spinnerFilter_Point.setAdapter(MV_adapter);

        clearUnSelectedRadioButtons();
        dialog = new FileDialog(this, mPath);

        // Set 'SELECT' button
        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorListFiltered.clear();
                dialog.setFileEndsWith(".txt");
                dialog.addFileListener(new FileDialog.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        Log.d(getClass().getName(), "selected file " + file.toString());
                        String fileString = file.toString();
                        fileName = fileString.substring(fileString.lastIndexOf("/") + 1, fileString.lastIndexOf("."));
                        //  System.out.println("file name is"+fileName);
                        file = new File(file.toString());
                        String message = null;
                        try {
                            message = readFromFile(file);
                            //   System.out.println("Message: "+message);
                            Log.d("Application", message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        StringTokenizer st = new StringTokenizer(message);

                        if (st.hasMoreTokens()) {
                            st = new StringTokenizer(message);
                            new Thread(new DoFiltration(st)).start();
                        }

                        save_btn.setEnabled(true);
                    }
                });

                dialog.showDialog();
            }
        });


    }

    // clear unchecked Radio Buttons
    private void clearUnSelectedRadioButtons() {
        for (int i : mRadioButtonsId) {
            RadioButton rb = (RadioButton) findViewById(i);
            rb.setOnClickListener(radioListener);
            if (i == R.id.LP_RadioBtn)
                rb.setChecked(true);
            else if (rb.isChecked())
                rb.setChecked(false);
        }
    }

    // Based on user's selections for LPF, set the following:
    // filter length, stop band attenuation, cutoff frequencies and the coefficients' groups to use
    public void set_LP_FilterCoefficients() {
        switch (Integer.parseInt(min_stopband_atten.substring(0, 2))) {
            case 60:
                filterTaps = 63;
                switch (cutoff) {
                    case "5Hz-45Hz":
                        h = Constants.LP60_h1;
                        filterSpec = "LP60dB_5Hz-45Hz";
                        break;
                    case "55Hz-95Hz":
                        h = Constants.LP60_h2;
                        filterSpec = "LP60dB_55Hz-95Hz";
                        break;
                    case "105Hz-145Hz":
                        h = Constants.LP60_h3;
                        filterSpec = "LP60dB_105Hz-145Hz";
                        break;
                    case "155Hz-195Hz":
                        h = Constants.LP60_h4;
                        filterSpec = "LP60dB_155Hz-195Hz";
                        break;
                    case "205Hz-245Hz":
                        h = Constants.LP60_h5;
                        filterSpec = "LP60dB_205Hz-245Hz";
                        break;
                    case "255Hz-295Hz":
                        h = Constants.LP60_h6;
                        filterSpec = "LP60dB_255Hz-295Hz";
                        break;
                    case "305Hz-345Hz":
                        h = Constants.LP60_h7;
                        filterSpec = "LP60dB_305Hz-345Hz";
                        break;
                    case "355Hz-395Hz":
                        h = Constants.LP60_h8;
                        filterSpec = "LP60dB_355Hz-395Hz";
                        break;
                    case "405Hz-445Hz":
                        h = Constants.LP60_h9;
                        filterSpec = "LP60dB_405Hz-445Hz";
                        break;
                    case "455Hz-495Hz":
                        h = Constants.LP60_h10;
                        filterSpec = "LP60dB_455Hz-495Hz";
                        break;
                }
                break;
            case 80:
                filterTaps = 78;
                switch (cutoff) {
                    case "5Hz-45Hz":
                        h = Constants.LP80_h1;
                        filterSpec = "LP80dB_5Hz-45Hz";
                        break;
                    case "55Hz-95Hz":
                        h = Constants.LP80_h2;
                        filterSpec = "LP80dB_55Hz-95Hz";
                        break;
                    case "105Hz-145Hz":
                        h = Constants.LP80_h3;
                        filterSpec = "LP80dB_105Hz-145Hz";
                        break;
                    case "155Hz-195Hz":
                        h = Constants.LP80_h4;
                        filterSpec = "LP80dB_155Hz-195Hz";
                        break;
                    case "205Hz-245Hz":
                        h = Constants.LP80_h5;
                        filterSpec = "LP80dB_205Hz-245Hz";
                        break;
                    case "255Hz-295Hz":
                        h = Constants.LP80_h6;
                        filterSpec = "LP80dB_255Hz-295Hz";
                        break;
                    case "305Hz-345Hz":
                        h = Constants.LP80_h7;
                        filterSpec = "LP80dB_305Hz-345Hz";
                        break;
                    case "355Hz-395Hz":
                        h = Constants.LP80_h8;
                        filterSpec = "LP80dB_355Hz-395Hz";
                        break;
                    case "405Hz-445Hz":
                        h = Constants.LP80_h9;
                        filterSpec = "LP80dB_405Hz-445Hz";
                        break;
                    case "455Hz-495Hz":
                        h = Constants.LP80_h10;
                        filterSpec = "LP80dB_455Hz-495Hz";
                        break;
                }
                break;
        }
    }

    // Based on user's selections for HPF, set the following:
    // filter length, stop band attenuation, cutoff frequencies and the coefficients' groups to use
    private void set_HP_FilterCoefficients() {
        switch (Integer.parseInt(min_stopband_atten.substring(0, 2))) {
            case 60:
                filterTaps = 65;
                switch (cutoff) {
                    case "5Hz-45Hz":
                        h = Constants.HP60_h1;
                        filterSpec = "HP60dB_5Hz-45Hz";
                        break;
                    case "55Hz-95Hz":
                        h = Constants.HP60_h2;
                        filterSpec = "HP60dB_55Hz-95Hz";
                        //filterTaps = 65;
                        break;
                    case "105Hz-145Hz":
                        h = Constants.HP60_h3;
                        filterSpec = "HP60dB_105Hz-145Hz";
                        //filterTaps = 63;
                        break;
                    case "155Hz-195Hz":
                        h = Constants.HP60_h4;
                        filterSpec = "HP60dB_155Hz-195Hz";
                        break;
                    case "205Hz-245Hz":
                        h = Constants.HP60_h5;
                        filterSpec = "HP60dB_205Hz-245Hz";
                        break;
                    case "255Hz-295Hz":
                        h = Constants.HP60_h6;
                        filterSpec = "HP60dB_255Hz-295Hz";
                        break;
                    case "305Hz-345Hz":
                        h = Constants.HP60_h7;
                        filterSpec = "HP60dB_305Hz-345Hz";
                        break;
                    case "355Hz-395Hz":
                        h = Constants.HP60_h8;
                        filterSpec = "HP60dB_355Hz-395Hz";
                        break;
                    case "405Hz-445Hz":
                        h = Constants.HP60_h9;
                        filterSpec = "HP60dB_405Hz-445Hz";
                        break;
                    case "455Hz-495Hz":
                        h = Constants.HP60_h10;
                        filterSpec = "HP60dB_455Hz-495Hz";
                        break;
                }
                break;
            case 80:
                filterTaps = 83;
                switch (cutoff) {
                    case "5Hz-45Hz":
                        h = Constants.HP80_h1;
                        filterSpec = "HP80dB_5Hz-45Hz";
                        break;
                    case "55Hz-95Hz":
                        h = Constants.HP80_h2;
                        filterSpec = "HP80dB_55Hz-95Hz";
                        break;
                    case "105Hz-145Hz":
                        h = Constants.HP80_h3;
                        filterSpec = "HP80dB_105Hz-145Hz";
                        break;
                    case "155Hz-195Hz":
                        h = Constants.HP80_h4;
                        filterSpec = "HP80dB_155Hz-195Hz";
                        break;
                    case "205Hz-245Hz":
                        h = Constants.HP80_h5;
                        filterSpec = "HP80dB_205Hz-245Hz";
                        break;
                    case "255Hz-295Hz":
                        h = Constants.HP80_h6;
                        filterSpec = "HP80dB_255Hz-295Hz";
                        break;
                    case "305Hz-345Hz":
                        h = Constants.HP80_h7;
                        filterSpec = "HP80dB_305Hz-345Hz";
                        break;
                    case "355Hz-395Hz":
                        h = Constants.HP80_h8;
                        filterSpec = "HP80dB_355Hz-395Hz";
                        break;
                    case "405Hz-445Hz":
                        h = Constants.HP80_h9;
                        filterSpec = "HP80dB_405Hz-445Hz";
                        break;
                    case "455Hz-495Hz":
                        h = Constants.HP80_h10;
                        filterSpec = "HP80dB_455Hz-495Hz";
                        //filterTaps = 65;
                        break;
                }
                break;
        }
    }

    // Based on user's selections for BPF, set the following:
    // filter length, stop band attenuation, cutoff frequencies and the coefficients' groups to use
    private void set_BP_FilterCoefficients() {
        switch (Integer.parseInt(min_stopband_atten.substring(0, 2))) {
            case 60:
                filterTaps = 63;
                switch (cutoff) {
                    case "0Hz-30Hz,70Hz-100Hz":
                        filterTaps = 84;
                        h = Constants.BP60_h1;
                        filterSpec = "BP60dB_0Hz-30Hz,70Hz-100Hz";
                        break;
                    case "55Hz-95Hz,105hz-145Hz":
                        h = Constants.BP60_h2;
                        filterSpec = "BP60dB_55Hz-95Hz,105hz-145Hz";
                        break;
                    case "105Hz-145Hz,155Hz-195Hz":
                        h = Constants.BP60_h3;
                        filterSpec = "BP60dB_105Hz-145Hz,155Hz-195Hz";
                        break;
                    case "155Hz-195Hz,205Hz-245Hz":
                        h = Constants.BP60_h4;
                        filterSpec = "BP60dB_155Hz-195Hz,205Hz-245Hz";
                        break;
                    case "205Hz-245Hz,255Hz-295Hz":
                        h = Constants.BP60_h5;
                        filterSpec = "BP60dB_205Hz-245Hz,255Hz-295Hz";
                        break;
                    case "255Hz-295Hz,305Hz-345Hz":
                        h = Constants.BP60_h6;
                        filterSpec = "BP60dB_255Hz-295Hz,305Hz-345Hz";
                        break;
                    case "305Hz-345Hz,355Hz-395Hz":
                        h = Constants.BP60_h7;
                        filterSpec = "BP60dB_305Hz-345Hz,355Hz-395Hz";
                        break;
                    case "355Hz-395Hz,405Hz-445Hz":
                        h = Constants.BP60_h8;
                        filterSpec = "BP60dB_355Hz-395Hz,405Hz-445Hz";
                        break;
                    case "405Hz-445Hz,455Hz-495Hz":
                        h = Constants.BP60_h9;
                        filterSpec = "BP60dB_405Hz-445Hz,455Hz-495Hz";
                        break;
                }
                break;
            case 80:
                filterTaps = 78;
                switch (cutoff) {
                    case "0Hz-30Hz,70Hz-100Hz":
                        filterTaps = 104;
                        h = Constants.BP80_h1;
                        filterSpec = "BP80dB_0Hz-30Hz,70Hz-100Hz";
                        break;
                    case "55Hz-95Hz,105hz-145Hz":
                        h = Constants.BP80_h2;
                        filterSpec = "BP80dB_55Hz-95Hz,105hz-145Hz";
                        break;
                    case "105Hz-145Hz,155Hz-195Hz":
                        h = Constants.BP80_h3;
                        filterSpec = "BP80dB_105Hz-145Hz,155Hz-195Hz";
                        break;
                    case "155Hz-195Hz,205Hz-245Hz":
                        h = Constants.BP80_h4;
                        filterSpec = "BP80dB_155Hz-195Hz,205Hz-245Hz";
                        break;
                    case "205Hz-245Hz,255Hz-295Hz":
                        h = Constants.BP80_h5;
                        filterSpec = "BP80dB_205Hz-245Hz,255Hz-295Hz";
                        break;
                    case "255Hz-295Hz,305Hz-345Hz":
                        h = Constants.BP80_h6;
                        filterSpec = "BP80dB_255Hz-295Hz,305Hz-345Hz";
                        break;
                    case "305Hz-345Hz,355Hz-395Hz":
                        h = Constants.BP80_h7;
                        filterSpec = "BP80dB_305Hz-345Hz,355Hz-395Hz";
                        break;
                    case "355Hz-395Hz,405Hz-445Hz":
                        h = Constants.BP80_h8;
                        filterSpec = "BP80dB_355Hz-395Hz,405Hz-445Hz";
                        break;
                    case "405Hz-445Hz,455Hz-495Hz":
                        h = Constants.BP80_h9;
                        filterSpec = "BP80dB_405Hz-445Hz,455Hz-495Hz";
                        break;
                }
                break;
        }
    }

    // Based on user's selections for BSF, set the following:
    // filter length, stop band attenuation, cutoff frequencies and the coefficients' groups to use
    private void set_BS_FilterCoefficients() {
        switch (Integer.parseInt(min_stopband_atten.substring(0, 2))) {
            case 60:
                filterTaps = 65;
                switch (cutoff) {
                    case "0Hz-30Hz,70Hz-100Hz":
                        filterTaps = 87;
                        h = Constants.BS60_h1;
                        filterSpec = "BS60dB_0Hz-30Hz,70Hz-100Hz";
                        break;
                    case "55Hz-95Hz,105hz-145Hz":
                        h = Constants.BS60_h2;
                        filterSpec = "BS60dB_55Hz-95Hz,105hz-145Hz";
                        break;
                    case "105Hz-145Hz,155Hz-195Hz":
                        h = Constants.BS60_h3;
                        filterSpec = "BS60dB_105Hz-145Hz,155Hz-195Hz";
                        break;
                    case "155Hz-195Hz,205Hz-245Hz":
                        h = Constants.BS60_h4;
                        filterSpec = "BS60dB_155Hz-195Hz,205Hz-245Hz";
                        break;
                    case "205Hz-245Hz,255Hz-295Hz":
                        h = Constants.BS60_h5;
                        filterSpec = "BS60dB_205Hz-245Hz,255Hz-295Hz";
                        break;
                    case "255Hz-295Hz,305Hz-345Hz":
                        h = Constants.BS60_h6;
                        filterSpec = "BS60dB_255Hz-295Hz,305Hz-345Hz";
                        break;
                    case "305Hz-345Hz,355Hz-395Hz":
                        h = Constants.BS60_h7;
                        filterSpec = "BS60dB_305Hz-345Hz,355Hz-395Hz";
                        break;
                    case "355Hz-395Hz,405Hz-445Hz":
                        h = Constants.BS60_h8;
                        filterSpec = "BS60dB_355Hz-395Hz,405Hz-445Hz";
                        break;
                    case "405Hz-445Hz,455Hz-495Hz":
                        h = Constants.BS60_h9;
                        filterSpec = "BS60dB_405Hz-445Hz,455Hz-495Hz";
                        break;
                }
                break;
            case 80:
                filterTaps = 83;
                switch (cutoff) {
                    case "0Hz-30Hz,70Hz-100Hz":
                        filterTaps = 109;
                        h = Constants.BS80_h1;
                        filterSpec = "BS80dB_0Hz-30Hz,70Hz-100Hz";
                        break;
                    case "55Hz-95Hz,105hz-145Hz":
                        h = Constants.BS80_h2;
                        filterSpec = "BS80dB_55Hz-95Hz,105hz-145Hz";
                        break;
                    case "105Hz-145Hz,155Hz-195Hz":
                        h = Constants.BS80_h3;
                        filterSpec = "BS80dB_105Hz-145Hz,155Hz-195Hz";
                        break;
                    case "155Hz-195Hz,205Hz-245Hz":
                        h = Constants.BS80_h4;
                        filterSpec = "BS80dB_155Hz-195Hz,205Hz-245Hz";
                        break;
                    case "205Hz-245Hz,255Hz-295Hz":
                        h = Constants.BS80_h5;
                        filterSpec = "BS80dB_205Hz-245Hz,255Hz-295Hz";
                        break;
                    case "255Hz-295Hz,305Hz-345Hz":
                        h = Constants.BS80_h6;
                        filterSpec = "BS80dB_255Hz-295Hz,305Hz-345Hz";
                        break;
                    case "305Hz-345Hz,355Hz-395Hz":
                        h = Constants.BS80_h7;
                        filterSpec = "BS80dB_305Hz-345Hz,355Hz-395Hz";
                        break;
                    case "355Hz-395Hz,405Hz-445Hz":
                        h = Constants.BS80_h8;
                        filterSpec = "BS80dB_355Hz-395Hz,405Hz-445Hz";
                        break;
                    case "405Hz-445Hz,455Hz-495Hz":
                        h = Constants.BS80_h9;
                        filterSpec = "BS80dB_445Hz,455Hz-495Hz";
                        break;
                }
                break;
        }
    }

    // Based on user's selections for MAF, set the following:
    // filter length and the coefficients' groups to use
    private void set_MV_FilterCoefficients() {
        switch (filter_point) {
            case "10":
                h = Constants.MV_h10;
                filterSpec = "MV_FP10";
                break;
            case "20":
                h = Constants.MV_h20;
                filterSpec = "MV_FP20";
                break;
            case "30":
                h = Constants.MV_h30;
                filterSpec = "MV_FP30";
                break;
            case "40":
                h = Constants.MV_h40;
                filterSpec = "MV_FP40";
                break;
            case "50":
                h = Constants.MV_h50;
                filterSpec = "MV_FP50";
                break;
        }
        filterTaps = Integer.parseInt(filter_point);
    }

    // Set the corresponding coefficients for the selected filter once 'APPLY FILTER SETTINGS' button is clicked
    public void applyFilterSettings(View view) {
        if (LP_RadioBtn.isChecked()) {
            set_LP_FilterCoefficients();
        } else if (HP_RadioBtn.isChecked()) {
            set_HP_FilterCoefficients();
        } else if (BP_RadioBtn.isChecked()) {
            set_BP_FilterCoefficients();
        } else if (BS_RadioBtn.isChecked()) {
            set_BS_FilterCoefficients();
        } else if (MV_RadioBtn.isChecked()) {
            set_MV_FilterCoefficients();
        }
        select_btn.setEnabled(true);
        if(!filterSpec.equals("")){
            Toast.makeText(getBaseContext(),filterSpec,Toast.LENGTH_SHORT).show();
        }
    }

    //get filter parameters (Min. Stopband Atten., cutoff or Filter Point)
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()) {
            case R.id.Min_SB_Atten_Spinner:
                min_stopband_atten = spinner.getSelectedItem().toString();
                break;
            case R.id.Trans_Band_Level_Spinner:
                cutoff = spinner.getSelectedItem().toString();
                break;
            case R.id.Filter_Point_Spinner:
                filter_point = spinner.getSelectedItem().toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // read file and return its content
    public String readFromFile(File file) throws IOException {

        String data = "";
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);

        try {
            in.read(bytes);
        } finally {
            in.close();
        }

        data = new String(bytes);

        return data;
    }

    public void onComplete() {
        synchronized (mutex) {
            // It is done so we notify the waiting threads
            mutex.notifyAll();
        }
    }


    // Multiple (child) threads to handle 8 different channels, each will be invoked by the (parent) thread 'DoFiltration'.

    class Channel1Thread implements Runnable {
        private int index1 = 0;
        private double[] x1 = new double[filterTaps];

        @Override
        public void run() {
        }

        public Double filterData(double x_in) {
            double y1 = 0.0;
            //double y1_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x1[index1] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y1 += h[i] * x1[((filterTaps - i) + index1) % filterTaps];
            }
            //y1 = Math.pow(10,(y1_logarithmic/20));
            // Increment the input buffer index1 to the next location
            index1 = (index1 + 1) % filterTaps;

            return y1;
        }
    }

    class Channel2Thread implements Runnable {
        private int index2 = 0;
        private double[] x2 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y2 = 0.0;
            //double y_logarithmic = 0.0;

            // Store the current input, overwriting the oldest input
            x2[index2] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y2 += h[i] * x2[((filterTaps - i) + index2) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;
            // Increment the input buffer index1 to the next location
            index2 = (index2 + 1) % filterTaps;
            return y2;
        }
    }

    class Channel3Thread implements Runnable {
        private int index3 = 0;
        private double[] x3 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y3 = 0.0;
            //double y,y_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x3[index3] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y3 += h[i] * x3[((filterTaps - i) + index3) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index1 to the next location
            index3 = (index3 + 1) % filterTaps;
            return y3;
        }
    }

    class Channel4Thread implements Runnable {
        private int index4 = 0;
        private double[] x4 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y4 = 0.0;
            //double y_logarithmic = 0.0;

            // Store the current input, overwriting the oldest input
            x4[index4] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y4 += h[i] * x4[((filterTaps - i) + index4) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index1 to the next location
            index4 = (index4 + 1) % filterTaps;
            return y4;
        }
    }

    class Channel5Thread implements Runnable {
        private int index5 = 0;
        private double[] x5 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y5 = 0.0;
            //double y,y_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x5[index5] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y5 += h[i] * x5[((filterTaps - i) + index5) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index1 to the next location
            index5 = (index5 + 1) % filterTaps;
            return y5;
        }
    }

    class Channel6Thread implements Runnable {
        private int index6 = 0;
        private double[] x6 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y6 = 0.0;
            //double y,y_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x6[index6] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y6 += h[i] * x6[((filterTaps - i) + index6) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index to the next location
            index6 = (index6 + 1) % filterTaps;
            return y6;
        }
    }

    class Channel7Thread implements Runnable {
        private int index7 = 0;
        private double[] x7 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y7 = 0.0;
            //double y_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x7[index7] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y7 += h[i] * x7[((filterTaps - i) + index7) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index1 to the next location
            index7 = (index7 + 1) % filterTaps;
            return y7;
        }
    }

    class Channel8Thread implements Runnable {
        private int index8 = 0;
        private double[] x8 = new double[filterTaps];

        @Override
        public void run() {

        }

        public Double filterData(double x_in) {
            double y8 = 0.0;
            //double y_logarithmic = 0.0;
            // Store the current input, overwriting the oldest input
            x8[index8] = x_in;

            // Multiply the filter coefficients by the previous inputs and sum
            for (int i = 0; i < filterTaps; i++) {
                y8 += h[i] * x8[((filterTaps - i) + index8) % filterTaps];
            }
            //y = Math.pow(10,(y_logarithmic/20))+3;

            // Increment the input buffer index1 to the next location
            index8 = (index8 + 1) % filterTaps;
            return y8;
        }
    }

    // Main thread of filtration (parent)
    class DoFiltration implements Runnable {
        Filtration.Channel1Thread ch1 = new Filtration.Channel1Thread();
        Thread chThread1 = new Thread(ch1);
        Filtration.Channel2Thread ch2 = new Filtration.Channel2Thread();
        Thread chThread2 = new Thread(ch2);
        Filtration.Channel3Thread ch3 = new Filtration.Channel3Thread();
        Thread chThread3 = new Thread(ch3);
        Filtration.Channel4Thread ch4 = new Filtration.Channel4Thread();
        Thread chThread4 = new Thread(ch4);
        Filtration.Channel5Thread ch5 = new Filtration.Channel5Thread();
        Thread chThread5 = new Thread(ch5);
        Filtration.Channel6Thread ch6 = new Filtration.Channel6Thread();
        Thread chThread6 = new Thread(ch6);
        Filtration.Channel7Thread ch7 = new Filtration.Channel7Thread();
        Thread chThread7 = new Thread(ch7);
        Filtration.Channel8Thread ch8 = new Filtration.Channel8Thread();
        Thread chThread8 = new Thread(ch8);
        private StringTokenizer st = new StringTokenizer("");

        // Save filtered data
        public void saveFiltrationData(View v) {
            Toast.makeText(getBaseContext(), "Applying Filtration, please wait..", Toast.LENGTH_LONG).show();
            new Thread(new SaveFilteredData()).start();
            synchronized(mutex) {
                // Wait until being notified
                try {
                    mutex.wait();
                    Toast.makeText(getBaseContext(), "Done!", Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            select_btn.setEnabled(false);
            save_btn.setEnabled(false);
        }

        public DoFiltration(StringTokenizer st) {
            this.st = st;
        }

        @Override
        public void run() {
            chThread1.start();
            chThread2.start();
            chThread3.start();
            chThread4.start();
            chThread5.start();
            chThread6.start();
            chThread7.start();
            chThread8.start();
            while (st.hasMoreTokens()) {
                String isDate = st.nextToken();
                // Check if string is date yyyy-mm-dd and Validate year
                // 19|20
                // month!>12&&month!=0 and day!>31 && day!=0

                DateFormat ft2 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss,SSS");
                if (isDate
                        .matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])$")) {
                    try {
                        sensorListFiltered.add(new Sensor(
                                "addlater", // Filename
                                ft2.parse(isDate + ":" + st.nextToken()),// Time
                                ch1.filterData(Double.parseDouble(st.nextToken())),// Channel1
                                ch2.filterData(Double.parseDouble(st.nextToken())), // Channel2
                                ch3.filterData(Double.parseDouble(st.nextToken())), // Channel3
                                ch4.filterData(Double.parseDouble(st.nextToken())), // Channel4
                                ch5.filterData(Double.parseDouble(st.nextToken())), // Channel5
                                ch6.filterData(Double.parseDouble(st.nextToken())), // Channel6
                                ch7.filterData(Double.parseDouble(st.nextToken())), // Channel7
                                ch8.filterData(Double.parseDouble(st.nextToken()))));// Channel8
                    } catch (NumberFormatException e) {
                        // e.printStackTrace();
                    } catch (ParseException e) {
                        //  e.printStackTrace();
                    }
                }
            }
        }
    }

    // Separate thread to save filtered data
    class SaveFilteredData implements Runnable {
        @Override
        public void run() {
            synchronized (mutex) {
            File folder = new File("sdcard/Save & View 2015/Filtered data");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File logFile = new File("sdcard/Save & View 2015/Filtered data/" + fileName + "_"+filterSpec + ".txt");
//            if(logFile.exists()) {
//                logFile.delete();
//            }
            try {
                logFile.createNewFile();
                FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("Date/Time\tChannel 1\tChannel 2\tChannel 3\tChannel 4\tChannel 5\tChannel 6\tChannel 7\tChannel 8\r\n");
                for (int i = 0; i < sensorListFiltered.size(); i++) {
                    // System.out.println("date is "+ft.format(new Date()));
                    bw.write(ft.format(new Date()) + "\t" + dX.format(sensorListFiltered.get(i).getChannel1()) + "\t" +
                            dX.format(sensorListFiltered.get(i).getChannel2()) + "\t" + dX.format(sensorListFiltered.get(i).getChannel3()) + "\t"
                            + dX.format(sensorListFiltered.get(i).getChannel4()) + "\t" + dX.format(sensorListFiltered.get(i).getChannel5()) + "\t"
                            + dX.format(sensorListFiltered.get(i).getChannel6()) + "\t" + dX.format(sensorListFiltered.get(i).getChannel7()) + "\t"
                            + dX.format(sensorListFiltered.get(i).getChannel8()) + "\r\n");
                }
                bw.close();

                    // It is done so we notify the waiting threads
                    onComplete();
                } catch(IOException e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
