/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WeatherSensor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Barry, Kieran, Nathan and Finlay
 */

public class WeatherSensor {
    
    private final String ID;
    private InetAddress serverAddress;
    private Socket serverSocket;
    private DataInputStream input;
    private DataOutputStream output;
    private Thread messageThread;
    
    private final ArrayList<DataListener> dataListeners = new ArrayList<>();
    /*********************************************
     * Weather Based Information
     *********************************************/
    private double temperature;//Temperature in Celcius (double to allow for comparison)
    private double windSpeed;//Wind Speed in KPH
    private int windDirection;//Wind Direction, 0-8 coresponds to cardinal directions going clockwise 0=N 1=NE 2=E...
    private double humidity;//Percent Humidity
    private double airPressure;//Air Pressure in Pascals
    private double precipitation;//Rainfall in mm
    private double snowDepth;


    /*********************************************
     * Status Based Information
     *********************************************/
    private double batteryLevel;
    private double storageLeft;
    private String GPS;
    
       
    private WeatherSensor(String pID, String pGPS ) {
        /*********************************************
        * Constructor for the class, initialises variables
        *********************************************/
        ID = pID;
        temperature = getRandomIntInRange(-5,35); //Temperature in Celcius (double to allow for comparison)
        windSpeed = getRandomNumberInRange(5,40); //Wind Speed in KPH
        windDirection = getRandomIntInRange(0,7); //Wind Direction, 0-8 coresponds to cardinal directions going clockwise 0=N 1=NE 2=E...
        humidity = getRandomNumberInRange(68,93); //Percent Humidity
        airPressure = getRandomNumberInRange(975,1050); //Air Pressure in Pascals
        precipitation = getRandomNumberInRange(0,31); //Rainfall in mm
        
        if(temperature >= 4){snowDepth = 0;}//If temp is above 4 degrees, snow = 0
        else {snowDepth = getRandomNumberInRange(0,3);}//Else generate Snow Measurement in CM

        /*********************************************
         * Status Based Information
         *********************************************/
        if("North Field Sensor".equalsIgnoreCase(ID)){batteryLevel = getRandomNumberInRange(10,15);} //fake that ID=3 has a low battery
        else {batteryLevel = getRandomNumberInRange(75,100);} //Else generate normal battery level
        
        if("South Field Sensor".equalsIgnoreCase(ID)){storageLeft = getRandomNumberInRange(5,10);} //fake that ID=4 has a low harddrive space
        else {storageLeft = getRandomNumberInRange(60,80);} //Else generate normal battery level
        
        GPS = pGPS;

        handleCommunication();
    }
    

    private static double round(double value) {
        /*********************************************
        * Method to round double numbers and format
        * to 3 decimal places
        *********************************************/
        final int DECIMAL_PLACES = 3;

        BigDecimal bigDecimal = new BigDecimal(Double.toString(value));
        bigDecimal = bigDecimal.setScale(DECIMAL_PLACES,RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    private static double getRandomNumberInRange(int min, int max){
        /*********************************************
        * Method to generate random double numbers within a range
        *********************************************/
        return round((Math.random() * ((max - min) + 1)) + min);
    }

    private static int getRandomIntInRange(int min, int max){
        /*********************************************
        * Method to generate random integer numbers within a range
        *********************************************/
        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

 
    
    private void handleCommunication(){
        /*********************************************
        * Method which handles the communication from
        * the device.
        * Sets up a timed task to sens the data at intervals
        *********************************************/
         if(connectToServer()){

            identifyAsSensor();
            startServerMessageThread();
            // we want to run commands at set intervals for the demo
            //set up a timed task so we can run the contents
            TimerTask task = new TimerTask() {
                public void run() {
                    //pass some randomly generated sensor data and print the reply
                    send(generateWeatherData());
                }
            };
            //create a new timer, set for 5 second increments and run every 5 seconds
            Timer timer = new Timer("Timer");
            long delay = 5000L;
            timer.schedule(task, delay, delay);
        }
    }
    
    
    private void startServerMessageThread() {
        messageThread = new Thread() {            
            @Override
            public void run() {
                System.out.println("here");
                handleServerMessage();
            }
        };
        messageThread.start();
    }

    
    private void handleServerMessage() {

        String serverMessage;
        try{
            while ((serverMessage = input.readUTF()) != null) {
                String[] serverCommand = serverMessage.split("\\|");
                System.out.println(serverMessage);
                switch (serverMessage){
                    case "DATAREQUESTED":
                        System.out.println("Data Requested");
                        handleRequest();
                        break;
                    case "DATACOMPRESS":
                        System.out.println("Sensor Harddrive Compressed");
                        compressFileSytem();
                        break; 
                }
            }
        } catch (EOFException ex) {
            try {
                serverSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(WeatherSensor.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(WeatherSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean connectToServer(){
        /*********************************************
        * Method to connect to the server and set up various
        * connections returns true if everything is successful
        * false if not.
        *********************************************/
        try {            
            serverAddress = InetAddress.getByName("localhost");        
            serverSocket = new Socket(serverAddress, 9090);
            input = new DataInputStream(serverSocket.getInputStream());
            output = new DataOutputStream(serverSocket.getOutputStream());      
            
            return true;
        } catch (IOException ex) {
            System.err.println("unable to find the server");
        }
        return false;
    }
    
   
    private void identifyAsSensor (){
        /*********************************************
        * Method to identify the unit as a sensor with the server
        *********************************************/
        String idString = "INIT|SENSOR|" + ID + "|" + GPS;
        send(idString);

    }
    
    private void send(String message){
        /*********************************************
        * Method to send data to the server.
        *********************************************/
        try {
            output.writeUTF(message);
            System.out.println(message);
        } catch (IOException ex) {
            Logger.getLogger(WeatherSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void compressFileSytem (){
        /*********************************************
         * Method to fake compressing the file structure 
         * on the weather station hard drive, simply resets
         * the property to default value but could be used
         * to archive files.
         *********************************************/
        storageLeft = getRandomNumberInRange(75,100);
        System.out.println("Storage Compressed");
    }
    
    private String generateWeatherData (){
        /*********************************************
        * Method to fake the weather and status information sent to the server
        * All Data Generated Is based off of the historical data from the UK 
        * in the past 4 years.
        *********************************************/

        //array to match the cardinal directions with teh auto-generated int 
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        
        //Weather Based Information
        temperature = getRandomNumberInRange(-5,35);//Temperature in Celcius (double to allow for comparison)
        windSpeed = getRandomNumberInRange(5,40);//Wind Speed in KPH
        windDirection = getRandomIntInRange(0,7);//Wind Direction, 0-8 coresponds to cardinal directions going clockwise 0=N 1=NE 2=E...
        humidity = getRandomNumberInRange(68,93);//Percent Humidity
        airPressure = getRandomNumberInRange(975,1050);//Air Pressure in Pascals
        precipitation = getRandomNumberInRange(0,30);//Rainfall in mm
        
        if(temperature >= 4){snowDepth = 0;}//If temp is above 4 degrees, snow = 0
        else {snowDepth = getRandomNumberInRange(0,3);}//Else generate Snow Measurement in CM
       
        //generate the string
        return "SENSORDATA|" + ID + "|" 
            + GPS + "|"
            + batteryLevel + "|"
            + storageLeft + "|"
            + humidity + "|"
            + temperature + "|"
            + airPressure + "|"
            + precipitation + "|"
            + windSpeed + "|"
            + directions[windDirection] + "|"
            + snowDepth;
    }
    
    public void handleCompression(){
        for(DataListener listener : dataListeners) {
            listener.dataCompressionRequested();
        } 
    }

    public void handleRequest() {
        for(DataListener listener : dataListeners) {
            listener.dataRequested();
        }
    }
    
    public void sendUpdatedData(){
        send(generateWeatherData());
    }
    
    public void addDataListener(DataListener listener){
        dataListeners.add(listener);
    }
    
    public void removeSensorListener(DataListener listener){
        dataListeners.remove(listener);
    }
    
   
    public static void main (String[] args) throws IOException{

        /*********************************************
         * Simply creates 5 objects with different initialisation values
         * This is used to simulate 3 working units and 2 units with
         * potential issues which may need attention
         *********************************************/

        //WeatherSensor sensor = new WeatherSensor("East Field Sensor", "52.800790, -1.156150"); // This weather station is working fine
        //WeatherSensor sensor = new WeatherSensor("West Field Sensor", "52.854112, -1.149548"); // This weather station is working fine
        //WeatherSensor sensor = new WeatherSensor("North Field Sensor", "52.823891, -1.291537"); // This weather station will have a low battery (cannot be resolved from client GUI)
        //WeatherSensor sensor = new WeatherSensor("South Field Sensor", "52.894951, -1.063960"); // This weather station will have a full harddrive (may be resolved from client GUI)
WeatherSensor sensor = new WeatherSensor("Test", "52.894951, -1.063960");
        sensor.addDataListener(new DataListener() {
            @Override
            public void dataRequested() {
                sensor.sendUpdatedData();
                System.out.println("Requested Data Sent");
            }
            
            @Override
            public void dataCompressionRequested() {
                sensor.compressFileSytem();
                System.out.println("Requested Data Sent");
            }
        });
        
    } 
    
}

