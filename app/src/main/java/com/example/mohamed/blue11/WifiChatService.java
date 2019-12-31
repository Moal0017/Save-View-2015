package com.example.mohamed.blue11;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;

import com.example.mohamed.blue11.Bluetooth.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;


/**
 * Created by Mohamed on 2015-08-14.
 */

/**
 * This class does all the work for setting up and managing wifi
 * connections with the server. It has a thread for connecting
 * with the server, and a thread for performing data transmissions when connected.
 */
public class WifiChatService extends ActionBarActivity {

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;          // we're doing nothing
    public static final int STATE_LISTEN = 1;        // now listening for incoming connections
    public static final int STATE_CONNECTING = 2;    // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;     // now connected to a remote device

    // Member fields
    private int nState;
    Handler nHandler;
    private ConnectThread nConnectThread;
    private ConnectedThread nConnectedThread;

  public String  channelnames;


    public int setPortName() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "/Save & View 2015/Setup.txt");
        try {
            channelnames = readFromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = channelnames.split("\n");

        String port=lines[9];

        StringTokenizer st = new StringTokenizer(port);
        String[] portNumber = new String[st.countTokens() + 1];

        int index = 0;
        while (st.hasMoreTokens()) {
            portNumber[index] = st.nextToken();
            index++;
        }
           Object pn=Integer.parseInt(portNumber[1]);
        if (pn instanceof Integer) {
            return (Integer)pn;
        } else {
           return 8080;
        }
    }




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






    /**
     * Constructor. Prepares a new WifiChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public WifiChatService(Context context, Handler handler) {
        nHandler = handler;
        nState = STATE_NONE;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        nState = state;
        nHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return nState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (nConnectThread != null) {
            nConnectThread.cancel();
            nConnectThread = null;
        }

        if (nConnectedThread != null) {
            nConnectedThread.cancel();
            nConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /*
     * Send the log file that includes all data samples
     */
    public void writeFile(File file) {
        ConnectedThread r;
        synchronized (this) {
            if (nState != STATE_CONNECTED) return;
            r = nConnectedThread;
        }
        r.writeFileBytes(file);
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with the server. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {

        private Socket nnSocket;
        private String ip = "";
        private int port = setPortName();
        public ConnectThread(String ip){
            this.ip = ip;
        }
        @Override
        public void run() {

            // Make a connection to the server
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                nnSocket = new Socket(ip, port);
            } catch (IOException e) {
                e.printStackTrace();
                // Close the socket
                try {
                    if (nnSocket != null)
                        nnSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                connectionFailed();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (WifiChatService.this) {
                nConnectThread = null;
            }
            // Start the connected thread
            connected(nnSocket);
        }

        public void cancel() {
            try {
                if (nnSocket != null)
                    nnSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with the server.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final Socket nnSocket;
        private final InputStream nnInStream;
        private final OutputStream nnOutStream;
        private DataOutputStream dao;
        private DataInputStream dis;

        public ConnectedThread(Socket socket) {
            nnSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            dao = null;
            dis = null;
            // Get the TCP/IP input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                dao = new DataOutputStream(nnSocket.getOutputStream());
                dis = new DataInputStream(nnSocket.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
            nnInStream = tmpIn;
            nnOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer;
            int length;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    length = dis.readInt();
                    buffer = new byte[length];
                    for (int i = 0; i < length; i++) {
                        buffer[i] = dis.readByte();
                    }
                    Constants.sensorList.add(new String(buffer));
                    // Send the obtained bytes to the UI Activity
                    //nHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    // Start the service over to restart listening mode
                    WifiChatService.this.start();
                    break;
                }
            }
        }

        /*
         * Send the log file to the server as bytes through the output stream.
         */
        public void writeFileBytes(File file) {

            long size = file.length();
            byte[] buffer = new byte[(int) size];
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                int count;
                while ((count = in.read(buffer)) > 0) {
                    nnOutStream.write(buffer, 0, count);
                }
                // when the file is successfully sent, send a notification to the UI Activity.
                if (count == -1) {
                    Message message = nHandler.obtainMessage(Constants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TOAST, "Log file is sent to the server..");
                    message.setData(bundle);
                    nHandler.sendMessage(message);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                nnOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                nHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeInteger(String data){
            try {
                dao.writeInt(data.length());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                nnSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized void connected(Socket socket) {
        if (nConnectThread != null) {
            nConnectThread.cancel();
            nConnectThread = null;
        }

        if (nConnectedThread != null) {
            nConnectedThread.cancel();
            nConnectedThread = null;
        }

        nConnectedThread = new ConnectedThread(socket);
        nConnectedThread.start();

        // Send the name of the connected server back to the UI Activity
        Message msg = nHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, "Server");
        msg.setData(bundle);
        nHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void connect(String ip) {
        if (nState == STATE_CONNECTING) {
            if (nConnectThread != null) {
                nConnectThread.cancel();
                nConnectThread = null;
            }
        }

        if (nConnectedThread != null) {
            nConnectedThread.cancel();
            nConnectedThread = null;
        }

        nConnectThread = new ConnectThread(ip);
        nConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (nState != STATE_CONNECTED) return;
            r = nConnectedThread;
        }
        r.write(out);
    }

    public void writeInteger(String data) {
        ConnectedThread r;
        synchronized (this) {
            if (nState != STATE_CONNECTED) return;
            r = nConnectedThread;
        }
        r.writeInteger(data);
    }

    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = nHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Server connection was lost");
        msg.setData(bundle);
        nHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        WifiChatService.this.start();
    }

    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = nHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect the server");
        msg.setData(bundle);
        nHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        WifiChatService.this.start();
    }
}
