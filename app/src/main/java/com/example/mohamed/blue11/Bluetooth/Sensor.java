package com.example.mohamed.blue11.Bluetooth;

import java.util.Date;

/*
 * Sensor class to save samples. Each sample is saved as an object
 * including channels 1-8 data as well as file name, date and time.
 */
public class Sensor {

    // Member fields
    private String fileName;
    private Date date;
    private double channel1;
    private double channel2;
    private double channel3;
    private double channel4;
    private double channel5;
    private double channel6;
    private double channel7;
    private double channel8;

    /*
     * Constructor of Sensor class as which each sample will be saved including the date
     */
    public Sensor(String fileName, Date date, double channel1, double channel2,
                  double channel3, double channel4, double channel5, double channel6,
                  double channel7, double channel8) {
        this.channel1 = channel1;
        this.channel2 = channel2;
        this.channel3 = channel3;
        this.channel4 = channel4;
        this.channel5 = channel5;
        this.channel6 = channel6;
        this.channel7 = channel7;
        this.channel8 = channel8;
        this.fileName = fileName;
        this.date = date;
    }

    /*
     * Constructor of Sensor class as which each sample will be saved
     */
    public Sensor(String fileName, double channel1, double channel2,
                  double channel3, double channel4, double channel5, double channel6,
                  double channel7, double channel8) {
        this.channel1 = channel1;
        this.channel2 = channel2;
        this.channel3 = channel3;
        this.channel4 = channel4;
        this.channel5 = channel5;
        this.channel6 = channel6;
        this.channel7 = channel7;
        this.channel8 = channel8;
        this.fileName = fileName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return the channel1
     */
    public double getChannel1() {
        return channel1;
    }

    /**
     * @return the channel2
     */
    public double getChannel2() {
        return channel2;
    }

    /**
     * @return the channel3
     */
    public double getChannel3() {
        return channel3;
    }

    /**
     * @return the channel4
     */
    public double getChannel4() {
        return channel4;
    }

    /**
     * @return the channel5
     */
    public double getChannel5() {
        return channel5;
    }

    /**
     * @return the channel6
     */
    public double getChannel6() {
        return channel6;
    }

    /**
     * @return the channel7
     */
    public double getChannel7() {
        return channel7;
    }

    /**
     * @return the channel8
     */
    public double getChannel8() {
        return channel8;
    }
}
