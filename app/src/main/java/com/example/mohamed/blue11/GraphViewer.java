package com.example.mohamed.blue11;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import android.widget.TimePicker;

import com.example.mohamed.blue11.Bluetooth.Constants;
import com.example.mohamed.blue11.Bluetooth.Sensor;
import com.example.mohamed.blue11.Filter.Filtration;
//import com.example.mohamed.blue11.Filter.Filtration;

public class GraphViewer extends AppCompatActivity {

    private TimeSeries Timeseries1;
    private TimeSeries Timeseries2;
    private TimeSeries Timeseries3;
    private TimeSeries Timeseries4;
    private TimeSeries Timeseries5;
    private TimeSeries Timeseries6;
    private TimeSeries Timeseries7;
    private TimeSeries Timeseries8;

    private XYSeries XYseries1;
    private XYSeries XYseries2;
    private XYSeries XYseries3;
    private XYSeries XYseries4;
    private XYSeries XYseries5;
    private XYSeries XYseries6;
    private XYSeries XYseries7;
    private XYSeries XYseries8;

    private CheckBox c1;
    private CheckBox c2;
    private CheckBox c3;
    private CheckBox c4;
    private CheckBox c5;
    private CheckBox c6;
    private CheckBox c7;
    private CheckBox c8;

    private boolean isChartValues1;
    private boolean isChartValues2;
    private boolean isChartValues3;
    private boolean isChartValues4;
    private boolean isChartValues5;
    private boolean isChartValues6;
    private boolean isChartValues7;
    private boolean isChartValues8;

    private boolean isFilled;

    private XYMultipleSeriesRenderer multiRenderer;
    private XYMultipleSeriesDataset dataset;

    private XYSeriesRenderer series1Renderer;
    private XYSeriesRenderer series2Renderer;
    private XYSeriesRenderer series3Renderer;
    private XYSeriesRenderer series4Renderer;
    private XYSeriesRenderer series5Renderer;
    private XYSeriesRenderer series6Renderer;
    private XYSeriesRenderer series7Renderer;
    private XYSeriesRenderer series8Renderer;

    private GraphicalView mChart;
    public FileDialog dialog;
    private Bundle bundle;
    private Bundle bundle2;
    private boolean graphCheck;

    private EditText userInput1, userInput2;
    private String endTime, startTime;
    private String text;
    private String valuesFromDatabase;
    private String firstDateFromDatabase;
    private String lastDateFromDatabase;
    private ArrayList<Sensor> sensorList = new ArrayList<Sensor>();


    private TimePicker timePicker1, timePicker2;

    int[] colors = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(255, 153, 0),
            Color.rgb(0, 204, 0),
            Color.rgb(255, 255, 0),
            Color.rgb(102, 51, 0),
            Color.rgb(0, 0, 255),
            Color.rgb(102, 0, 102),
            Color.rgb(255, 0, 255)};

    /**
     * Setup checkbox
     * Return void
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.graph_viewer);

        c1 = (CheckBox) findViewById(R.id.checkBox1);
        c2 = (CheckBox) findViewById(R.id.checkBox2);
        c3 = (CheckBox) findViewById(R.id.checkBox3);
        c4 = (CheckBox) findViewById(R.id.checkBox4);
        c5 = (CheckBox) findViewById(R.id.checkBox5);
        c6 = (CheckBox) findViewById(R.id.checkBox6);
        c7 = (CheckBox) findViewById(R.id.checkBox7);
        c8 = (CheckBox) findViewById(R.id.checkBox8);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        OptionTh oTh = new OptionTh(item);
        oTh.run();
        boolean selectedOption = oTh.selectedOption();
        return selectedOption;
    }


    class OptionTh implements Runnable{

        private MenuItem item;
        public OptionTh(MenuItem item){
            this.item = item;
        }
        @Override
        public void run() {

        }

        public boolean selectedOption(){
            switch (item.getItemId()) {

                case R.id.filter: {
                    Intent i = new Intent(getApplication(), Filtration.class);
                    startActivity(i);
                    return true;
                }


                case R.id.fillUnfill: {

                    if (isFilled == true) {

                        series1Renderer.setFillPoints(false);
                        series2Renderer.setFillPoints(false);
                        series3Renderer.setFillPoints(false);
                        series4Renderer.setFillPoints(false);
                        series5Renderer.setFillPoints(false);
                        series6Renderer.setFillPoints(false);
                        series7Renderer.setFillPoints(false);
                        series8Renderer.setFillPoints(false);
                        isFilled = false;

                    } else {

                        series1Renderer.setFillPoints(true);
                        series2Renderer.setFillPoints(true);
                        series3Renderer.setFillPoints(true);
                        series4Renderer.setFillPoints(true);
                        series5Renderer.setFillPoints(true);
                        series6Renderer.setFillPoints(true);
                        series7Renderer.setFillPoints(true);
                        series8Renderer.setFillPoints(true);
                        isFilled = true;
                    }

                    mChart.repaint();
                    return true;
                }

                case R.id.chartValues1: {

                    if (isChartValues1 == true) {
                        series1Renderer.setDisplayChartValues(false);
                        isChartValues1 = false;
                    } else {
                        series1Renderer.setDisplayChartValues(true);
                        isChartValues1 = true;
                    }
                    mChart.repaint();

                    return true;
                }
                case R.id.chartValues2: {
                    if (isChartValues2 == true) {
                        series2Renderer.setDisplayChartValues(false);
                        isChartValues2 = false;
                    } else {
                        series2Renderer.setDisplayChartValues(true);
                        isChartValues2 = true;
                    }
                    mChart.repaint();

                    return true;
                }
                case R.id.chartValues3: {
                    if (isChartValues3 == true) {
                        series3Renderer.setDisplayChartValues(false);
                        isChartValues3 = false;
                    } else {
                        series3Renderer.setDisplayChartValues(true);
                        isChartValues3 = true;
                    }
                    mChart.repaint();

                    return true;

                }
                case R.id.chartValues4: {
                    if (isChartValues4 == true) {
                        series4Renderer.setDisplayChartValues(false);
                        isChartValues4 = false;
                    } else {
                        series4Renderer.setDisplayChartValues(true);
                        isChartValues4 = true;
                    }
                    mChart.repaint();

                    return true;
                }
                case R.id.chartValues5: {
                    if (isChartValues5 == true) {
                        series5Renderer.setDisplayChartValues(false);
                        isChartValues5 = false;
                    } else {
                        series5Renderer.setDisplayChartValues(true);
                        isChartValues5 = true;
                    }
                    mChart.repaint();

                    return true;
                }
                case R.id.chartValues6: {
                    if (isChartValues6 == true) {
                        series6Renderer.setDisplayChartValues(false);
                        isChartValues6 = false;
                    } else {
                        series6Renderer.setDisplayChartValues(true);
                        isChartValues6 = true;
                    }
                    mChart.repaint();

                    return true;
                }
                case R.id.chartValues7: {
                    if (isChartValues7 == true) {
                        series7Renderer.setDisplayChartValues(false);
                        isChartValues7 = false;
                    } else {
                        series7Renderer.setDisplayChartValues(true);
                        isChartValues7 = true;
                    }
                    mChart.repaint();

                    return true;
                }

                case R.id.chartValues8: {
                    if (isChartValues8 == true) {
                        series8Renderer.setDisplayChartValues(false);
                        isChartValues8 = false;
                    } else {
                        series8Renderer.setDisplayChartValues(true);
                        isChartValues8 = true;
                    }
                    mChart.repaint();

                    return true;

                }
            }
            return false;
        }
    }
    /**
     * OnclickListener openFile button
     * Read data from file
     * Argument: View
     * Methode calls: initChartlines,enableDisableCheckbox
     * Return: Void
     */

    SimpleDateFormat ft = new SimpleDateFormat(
            "yyyy-MM-dd:HH:mm:ss,SSS");
    StringTokenizer st;


    public void openFile(View v) {
        graphCheck = false;
        sensorList.clear();
        // reading text from file
        final String path = null;
        File mPath = new File(Environment.getExternalStorageDirectory()
                + "//DIR//");
        dialog = new FileDialog(this, mPath);
        dialog.setFileEndsWith(".txt");
        dialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File files) {
                Log.d(getClass().getName(), "selected file " + files.toString());

                File file = new File(files.toString());
                String message = null;

                try {
                    message = readFromFile(file);
                    Log.d("Application", message);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if (st != null && st.hasMoreTokens()) {

                    st = new StringTokenizer(message);

                    while (st.hasMoreTokens()) {
                        String isDate = st.nextToken();
                        // Check if string is date yyyy-mm-dd and Validate year
                        // 19|20
                        // month!>12&&month!=0 and day!>31 && day!=0

                        if (isDate
                                .matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])$")) {
                            try {
                                sensorList.add(new Sensor(
                                        "addlater", // Filename
                                        ft.parse(isDate + ":" + st.nextToken()),// Time
                                        Double.parseDouble(st.nextToken()),// Channel1
                                        Double.parseDouble(st.nextToken()), // Channel2
                                        Double.parseDouble(st.nextToken()), // Channel3
                                        Double.parseDouble(st.nextToken()), // Channel4
                                        Double.parseDouble(st.nextToken()), // Channel5
                                        Double.parseDouble(st.nextToken()), // Channel6
                                        Double.parseDouble(st.nextToken()), // Channel7
                                        Double.parseDouble(st.nextToken())));// Channel8
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                openChart();
                enableDisableCheckBox(true);
                setCheckBox(true);
            }
        });

        dialog.showDialog();
    }


    public void plotRecord(View v) {

        new Record().run();
    }

    class Record implements Runnable {

        @Override
        public void run() {
            sensorList.clear();
            graphCheck = true;
            // reading text from file
            final String path = null;
            File mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");


            dialog = new FileDialog(GraphViewer.this, mPath);
            dialog.setFileEndsWith(".txt");

            dialog.addFileListener(new FileDialog.FileSelectedListener() {

                public void fileSelected(File files) {
                    Log.d(getClass().getName(), "selected file " + files.toString());

                    //    long startTime = System.currentTimeMillis();
                    File file = new File(files.toString());
                    String message = null;
                    try {
                        message = readFromFile(file);
                        // Log.d("Application", message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //  SimpleDateFormat ft = new SimpleDateFormat(
                    //        "yyyy-MM-dd:HH:mm:ss,SSS");
                    StringTokenizer st = new StringTokenizer(message);
                    //    if (st.hasMoreTokens()) {
                    while (st.hasMoreTokens()) {
                        String isDate = st.nextToken();


                        //System.out.println("is Date "+isDate);
                        // Check if string is date yyyy-mm-dd and Validate year
                        // 19|20
                        // month!>12&&month!=0 and day!>31 && day!=0

                        if (isDate
                                .matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])$")) {
                            try {
                                {
                                    sensorList.add(new Sensor(
                                            "addlater", // Filename
                                            ft.parse(isDate + ":" + st.nextToken()),// Time
//                                        filter(Double.parseDouble(st.nextToken())),// Channel1
//                                        filter(Double.parseDouble(st.nextToken())), // Channel2
//                                        filter(Double.parseDouble(st.nextToken())), // Channel3
//                                        filter(Double.parseDouble(st.nextToken())), // Channel4
//                                        filter(Double.parseDouble(st.nextToken())), // Channel5
//                                        filter(Double.parseDouble(st.nextToken())), // Channel6
//                                        filter(Double.parseDouble(st.nextToken())), // Channel7
//                                        filter(Double.parseDouble(st.nextToken()))));// Channel8

                                            Double.parseDouble(st.nextToken()),// Channel1
                                            Double.parseDouble(st.nextToken()), // Channel2
                                            Double.parseDouble(st.nextToken()), // Channel3
                                            Double.parseDouble(st.nextToken()), // Channel4
                                            Double.parseDouble(st.nextToken()), // Channel5
                                            Double.parseDouble(st.nextToken()), // Channel6
                                            Double.parseDouble(st.nextToken()), // Channel7
                                            Double.parseDouble(st.nextToken())));// Channel8


                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //               long estimatedTime = System.currentTimeMillis() - startTime;

                    openChart();
                    enableDisableCheckBox(true);
                    setCheckBox(true);

                }
            });
            dialog.showDialog();
        }
    }

    public boolean getInterval() {
        final Context context = this;


        LayoutInflater inflater = getLayoutInflater();
        //    LayoutInflater li = LayoutInflater.from(getApplication());
        View promptsView = inflater.inflate(R.layout.prompts2, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setView(promptsView);

        userInput1 = (EditText) promptsView
                .findViewById(R.id.DatabaseTimeS);
        userInput2 = (EditText) promptsView
                .findViewById(R.id.DatabaseTimeE);

        userInput1.setText(firstDateFromDatabase);
        userInput2.setText(lastDateFromDatabase);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startTime = userInput1.getText().toString();  //record starting
                                endTime = userInput2.getText().toString();   //record ending time
                                if (startTime.matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])[\\ \\/.]([01]?\\d|2[0-3]):([0-5]?\\d):([0-5]?\\d)$")
                                        || endTime.matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])[\\ \\/.]([01]?\\d|2[0-3]):([0-5]?\\d):([0-5]?\\d)$")) {
                                    plotDatabase();
                                } else {
                                    Toast.makeText(getBaseContext(), "Incorrect format. It should be in\n yyyy-mm-dd hh:mm:ss", Toast.LENGTH_LONG).show();
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

        return false;
    }

    /**
     * OnclickListener openDatabase button
     * Read data from database
     * Argument: View
     * Methode calls: initChartlines,enableDisableCheckbox
     * Return: Void
     */

    public void openDatabase(View v) throws IOException {

        Constants.isDbRequested[0] = true;
        graphCheck = false;
        sensorList.clear();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (Constants.sensorList.size() > 0) {

            valuesFromDatabase = Constants.sensorList.get(Constants.sensorList.size() - 1).toString();

            if (!valuesFromDatabase.contains("Empty")) {

                SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                StringTokenizer st = new StringTokenizer(valuesFromDatabase);
                int i = 0;

                StringBuilder stringBuilder = new StringBuilder();

                if (st.hasMoreTokens()) {
                    while (st.hasMoreTokens()) {
                        String isDate = st.nextToken();
                        // Check if string is date yyyy-mm-dd and Validate year
                        // 19|20
                        // month!>12&&month!=0 and day!>31 && day!=0
                        if (isDate
                                .matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])$")) {

                            try {
                                sensorList.add(new Sensor(
                                        "addlater", // Filename
                                        ft.parse(isDate + " " + st.nextToken()),// Time
                                        Double.parseDouble(st.nextToken()),// Channel1
                                        Double.parseDouble(st.nextToken()), // Channel2
                                        Double.parseDouble(st.nextToken()), // Channel3
                                        Double.parseDouble(st.nextToken()), // Channel4
                                        Double.parseDouble(st.nextToken()), // Channel5
                                        Double.parseDouble(st.nextToken()), // Channel6
                                        Double.parseDouble(st.nextToken()), // Channel7
                                        Double.parseDouble(st.nextToken())));// Channel8

                                stringBuilder.append(ft.format(sensorList.get(i).getDate()));
                                stringBuilder.append(" " + sensorList.get(i).getChannel1());
                                stringBuilder.append(" " + sensorList.get(i).getChannel2());
                                stringBuilder.append(" " + sensorList.get(i).getChannel3());
                                stringBuilder.append(" " + sensorList.get(i).getChannel4());
                                stringBuilder.append(" " + sensorList.get(i).getChannel5());
                                stringBuilder.append(" " + sensorList.get(i).getChannel6());
                                stringBuilder.append(" " + sensorList.get(i).getChannel7());
                                stringBuilder.append(" " + sensorList.get(i).getChannel8() + " ");
                                i++;
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                text = stringBuilder.toString();

                firstDateFromDatabase = ft.format(sensorList.get(0).getDate());
                lastDateFromDatabase = ft.format(sensorList.get(sensorList.size() - 1).getDate());
                getInterval();
            } else {
                Toast.makeText(getBaseContext(), "The Database is empty", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getBaseContext(), "Not connected to database", Toast.LENGTH_LONG).show();
        }
    }

    public void plotDatabase() {

        sensorList.clear();

        SimpleDateFormat ft = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");


        Date startDate = null, endDate = null;
        try {
            startDate = ft.parse(startTime);
            endDate = ft.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        StringTokenizer st = new StringTokenizer(text);


        if (st.hasMoreTokens()) {
            while (st.hasMoreTokens()) {
                String isDate = st.nextToken();
                // Check if string is date yyyy-mm-dd and Validate year
                // 19|20
                // month!>12&&month!=0 and day!>31 && day!=0
                if (isDate
                        .matches("^(19|20)\\d\\d[\\-\\/.](0[1-9]|1[012])[\\-\\/.](0[1-9]|[12][0-9]|3[01])$")) {
                    isDate = isDate + " " + st.nextToken();

                    try {

                        if ((ft.parse(isDate).after(startDate) && ft.parse(isDate).before(endDate))
                                || (ft.parse(isDate).equals(startDate) || ft.parse(isDate).equals(endDate))) {
                            try {
                                sensorList.add(new Sensor(
                                        "addlater", // Filename
                                        ft.parse(isDate),// Time
                                        Double.parseDouble(st.nextToken()),// Channel1
                                        Double.parseDouble(st.nextToken()), // Channel2
                                        Double.parseDouble(st.nextToken()), // Channel3
                                        Double.parseDouble(st.nextToken()), // Channel4
                                        Double.parseDouble(st.nextToken()), // Channel5
                                        Double.parseDouble(st.nextToken()), // Channel6
                                        Double.parseDouble(st.nextToken()), // Channel7
                                        Double.parseDouble(st.nextToken())));// Channel8
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        openChart();
        enableDisableCheckBox(true);
        setCheckBox(true);

    }

    /**
     * Checbox listener
     * Argument: View
     * Return: void
     */

    public void checkBoxListener(View v) {

        if (c1.isChecked()) {
            series1Renderer.setColor(colors[0]);
        } else {
            series1Renderer.setColor(Color.TRANSPARENT);
        }
        if (c2.isChecked()) {
            series2Renderer.setColor(colors[1]);
        } else {
            series2Renderer.setColor(Color.TRANSPARENT);
        }

        if (c3.isChecked()) {
            Log.d("Application", "C3 is checked");
            series3Renderer.setColor(colors[2]);
        } else {
            series3Renderer.setColor(Color.TRANSPARENT);
        }

        if (c4.isChecked()) {
            Log.d("Application", "C4 is checked");
            series4Renderer.setColor(colors[3]);
        } else {
            series4Renderer.setColor(Color.TRANSPARENT);
        }

        if (c5.isChecked()) {
            Log.d("Application", "C5 is checked");
            series5Renderer.setColor(colors[4]);
        } else {
            series5Renderer.setColor(Color.TRANSPARENT);
        }
        if (c6.isChecked()) {
            Log.d("Application", "C6 is checked");
            series6Renderer.setColor(colors[5]);
        } else {
            series6Renderer.setColor(Color.TRANSPARENT);
        }
        if (c7.isChecked()) {
            Log.d("Application", "C7 is checked");
            series7Renderer.setColor(colors[6]);
        } else {
            series7Renderer.setColor(Color.TRANSPARENT);
        }
        if (c8.isChecked()) {
            Log.d("Application", "C8 is checked");
            series8Renderer.setColor(colors[7]);
        } else {
            series8Renderer.setColor(Color.TRANSPARENT);
        }
        mChart.repaint();
    }

    /**
     * Enable checkbox
     * Argument: Boolean
     * Return: void
     */

    public void enableDisableCheckBox(Boolean state) {
        c1.setEnabled(state);
        c2.setEnabled(state);
        c3.setEnabled(state);
        c4.setEnabled(state);
        c5.setEnabled(state);
        c6.setEnabled(state);
        c7.setEnabled(state);
        c8.setEnabled(state);
    }


    public void setCheckBox(boolean state) {
        c1.setChecked(state);
        c2.setChecked(state);
        c3.setChecked(state);
        c4.setChecked(state);
        c5.setChecked(state);
        c6.setChecked(state);
        c7.setChecked(state);
        c8.setChecked(state);
    }

    class OpenChart implements Runnable{

        @Override
        public void run() {
            bundle = getIntent().getExtras();

            if (graphCheck == true) {
                XYseries1 = new XYSeries(bundle.getString("channel1"));
                XYseries2 = new XYSeries(bundle.getString("channel2"));
                XYseries3 = new XYSeries(bundle.getString("channel3"));
                XYseries4 = new XYSeries(bundle.getString("channel4"));
                XYseries5 = new XYSeries(bundle.getString("channel5"));
                XYseries6 = new XYSeries(bundle.getString("channel6"));
                XYseries7 = new XYSeries(bundle.getString("channel7"));
                XYseries8 = new XYSeries(bundle.getString("channel8"));

                int counter = 1;

                for (int i = 0; i < sensorList.size(); i++) {

                    XYseries1.add(counter, sensorList.get(i).getChannel1());
                    XYseries2.add(counter, sensorList.get(i).getChannel2());
                    XYseries3.add(counter, sensorList.get(i).getChannel3());
                    XYseries4.add(counter, sensorList.get(i).getChannel4());
                    XYseries5.add(counter, sensorList.get(i).getChannel5());
                    XYseries6.add(counter, sensorList.get(i).getChannel6());
                    XYseries7.add(counter, sensorList.get(i).getChannel7());
                    XYseries8.add(counter, sensorList.get(i).getChannel8());
                    counter++;
                }
            } else {
                // Creating an TimeSeries for Channel1
                Timeseries1 = new TimeSeries(bundle.getString("channel1"));
                // Creating an TimeSeries for Channel2
                Timeseries2 = new TimeSeries(bundle.getString("channel2"));
                // Creating an TimeSeries for Channel3
                Timeseries3 = new TimeSeries(bundle.getString("channel3"));
                // Creating an TimeSeries for Channel4
                Timeseries4 = new TimeSeries(bundle.getString("channel4"));
                // Creating an TimeSeries for Channel5
                Timeseries5 = new TimeSeries(bundle.getString("channel5"));
                // Creating an TimeSeries for Channel6
                Timeseries6 = new TimeSeries(bundle.getString("channel6"));
                // Creating an TimeSeries for Channel7
                Timeseries7 = new TimeSeries(bundle.getString("channel7"));
                // Creating an TimeSeries for Channel8
                Timeseries8 = new TimeSeries(bundle.getString("channel8"));

                // ADDing data from textfile for all the channels
                for (int i = 0; i < sensorList.size(); i++) {

                    Timeseries1.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel1());

                    Timeseries2.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel2());

                    Timeseries3.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel3());

                    Timeseries4.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel4());

                    Timeseries5.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel5());

                    Timeseries6.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel6());

                    Timeseries7.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel7());

                    Timeseries8.add(sensorList.get(i).getDate(), sensorList.get(i)
                            .getChannel8());
                }
            }

            // Creating a dataset to hold each series
            dataset = new XYMultipleSeriesDataset();

            if (graphCheck == true) {
                // Adding Channel1 Series to the dataset
                dataset.addSeries(XYseries1);
                // Adding Channel2 Series to the dataset
                dataset.addSeries(XYseries2);
                // Adding Channel3 Series to the dataset
                dataset.addSeries(XYseries3);
                // Adding Channel4 Series to the dataset
                dataset.addSeries(XYseries4);
                // Adding Channel5 Series to the dataset
                dataset.addSeries(XYseries5);
                // Adding Channel6 Series to the dataset
                dataset.addSeries(XYseries6);
                // Adding Channel7 Series to the dataset
                dataset.addSeries(XYseries7);
                // Adding Channel8 Series to the dataset
                dataset.addSeries(XYseries8);
            } else {
                // Adding Channel1 Series to the dataset
                dataset.addSeries(Timeseries1);
                // Adding Channel2 Series to the dataset
                dataset.addSeries(Timeseries2);
                // Adding Channel3 Series to the dataset
                dataset.addSeries(Timeseries3);
                // Adding Channel4 Series to the dataset
                dataset.addSeries(Timeseries4);
                // Adding Channel5 Series to the dataset
                dataset.addSeries(Timeseries5);
                // Adding Channel6 Series to the dataset
                dataset.addSeries(Timeseries6);
                // Adding Channel7 Series to the dataset
                dataset.addSeries(Timeseries7);
                // Adding Channel8 Series to the dataset
                dataset.addSeries(Timeseries8);
            }
            // Creating XYSeriesRenderer to customize Channel1
            series1Renderer = new XYSeriesRenderer();
            series1Renderer.setColor(colors[0]);
            //series1Renderer.setLineWidth(2f);
            series1Renderer.setDisplayChartValues(false);
            // Setting lien graph point style circle
            series1Renderer.setPointStyle(PointStyle.CIRCLE);
            // Setting stroke of the line chart to solid
            //series1Renderer.setStroke(BasicStroke.SOLID);
            isChartValues1 = false;


            // Creating XYSeriesRenderer to customize Channel2
            series2Renderer = new XYSeriesRenderer();
            series2Renderer.setColor(colors[1]);
            series2Renderer.setDisplayChartValues(false);
            series2Renderer.setDisplayChartValuesDistance(10);
            series2Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues2 = false;

            // Creating XYSeriesRenderer to customize Channel3
            series3Renderer = new XYSeriesRenderer();
            series3Renderer.setColor(colors[2]);
            series3Renderer.setDisplayChartValues(false);
            series3Renderer.setDisplayChartValuesDistance(10);
            series3Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues3 = false;

            // Creating XYSeriesRenderer to customize Channel4
            series4Renderer = new XYSeriesRenderer();
            series4Renderer.setColor(colors[3]);
            series4Renderer.setDisplayChartValues(false);
            series4Renderer.setDisplayChartValuesDistance(10);
            series4Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues4 = false;

            // Creating XYSeriesRenderer to customize Channel5
            series5Renderer = new XYSeriesRenderer();
            series5Renderer.setColor(colors[4]);
            series5Renderer.setDisplayChartValues(false);
            series5Renderer.setDisplayChartValuesDistance(10);
            series5Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues5 = false;

            // Creating XYSeriesRenderer to customize Channel6
            series6Renderer = new XYSeriesRenderer();
            series6Renderer.setColor(colors[5]);
            series6Renderer.setDisplayChartValues(false);
            series6Renderer.setDisplayChartValuesDistance(10);
            series6Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues6 = false;

            // Creating XYSeriesRenderer to customize Channel7
            series7Renderer = new XYSeriesRenderer();
            series7Renderer.setColor(colors[6]);
            series7Renderer.setDisplayChartValues(false);
            series7Renderer.setDisplayChartValuesDistance(10);
            series7Renderer.setPointStyle(PointStyle.CIRCLE);
            isChartValues7 = false;

            // Creating XYSeriesRenderer to customize Channel8
            series8Renderer = new XYSeriesRenderer();
            series8Renderer.setColor(colors[7]);
            series8Renderer.setDisplayChartValues(false);
            series8Renderer.setDisplayChartValuesDistance(10);
            series8Renderer.setPointStyle(PointStyle.CIRCLE);
            //series8Renderer.setStroke(BasicStroke.SOLID);
            isChartValues8 = false;
            isFilled = false;

            // Creating a XYMultipleSeriesRenderer to customize whole chart
            multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setChartTitle("Signals");
            multiRenderer.setChartTitleTextSize(20f);
            multiRenderer.setZoomButtonsVisible(true);
            multiRenderer.setLabelsTextSize(20f);
            multiRenderer.setLegendTextSize(20f);
            multiRenderer.setInScroll(true);
            multiRenderer.setYLabelsColor(0, Color.BLACK);
            multiRenderer.setYLabelsAlign(Paint.Align.LEFT);
            multiRenderer.setMargins(new int[]{30, 0, 30, 0});
            //multiRenderer.setPointSize(4f);
            multiRenderer.setScale(2f);
            if (graphCheck == true) {

                multiRenderer.setXLabels(5);
            } else {
                multiRenderer.setXLabels(1);
            }
            multiRenderer.setXLabelsAlign(Paint.Align.LEFT);
            multiRenderer.setAntialiasing(false);
            multiRenderer.setXRoundedLabels(false);

            multiRenderer.setClickEnabled(true);
            multiRenderer.setSelectableBuffer(20);

            multiRenderer.addSeriesRenderer(series1Renderer);
            multiRenderer.addSeriesRenderer(series2Renderer);
            multiRenderer.addSeriesRenderer(series3Renderer);
            multiRenderer.addSeriesRenderer(series4Renderer);
            multiRenderer.addSeriesRenderer(series5Renderer);
            multiRenderer.addSeriesRenderer(series6Renderer);
            multiRenderer.addSeriesRenderer(series7Renderer);
            multiRenderer.addSeriesRenderer(series8Renderer);

            //this part is used to display graph on the xml
            LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart);

            //remove any views before u paint the chart
            chartContainer.removeAllViews();

            if (graphCheck == false) {
                // Creating Timechart
                mChart = (GraphicalView) ChartFactory.getTimeChartView(
                        getBaseContext(), dataset, multiRenderer, "yyyy-MM-dd" + "\n" + "HH:mm:ss");
            } else {
                // Creating XY chart
                mChart = ChartFactory.getLineChartView(getBaseContext(), dataset, multiRenderer);
            }

            // adding the view to the linearlayout
            chartContainer.addView(mChart);

            mChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Format formatter = new SimpleDateFormat("yyyy-MM-dd" + "\n" + "HH:mm:ss,SSS");

                    SeriesSelection seriesSelection = mChart
                            .getCurrentSeriesAndPoint();

                    if (seriesSelection == null) {

                    } else {
                        if (!graphCheck) {

                            String data = "Data : ";
                            String date = "Date : ";

                            // Getting the clicked Date ( x value )
                            long clickedDateSeconds = (long) seriesSelection
                                    .getXValue();
                            Date clickedDate = new Date(clickedDateSeconds);
                            String strDate = formatter.format(clickedDate);

                            // Getting the y value
                            double amount = (double) seriesSelection.getValue();

                            // Displaying Toast Message
                            Toast.makeText(getBaseContext(),
                                    date + strDate + "\n"
                                            + data + amount, Toast.LENGTH_LONG).show();
                        } else {

                            String data = "Data : ";
                            // Getting the y value
                            double amount = (double) seriesSelection.getValue();

                            // Displaying Toast Message
                            Toast.makeText(getBaseContext(), data + amount, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }
    public void openChart() {
        new OpenChart().run();
    }

    /**
     * Read a file
     * Argument: File
     * Return: String
     */

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
}