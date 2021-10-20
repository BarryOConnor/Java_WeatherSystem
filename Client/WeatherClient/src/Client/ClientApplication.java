/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;


/**
 *
 * @author Barry, Kieran, Nathan and Finlay
 */
public class ClientApplication {
    
    private final String ID;
    private DataInputStream input;
    private DataOutputStream output;
    private InetAddress serverAddress;
    private Socket serverSocket;
    private Thread messageThread;
    
    private final ArrayList<SensorListener> sensorListeners = new ArrayList<>();
    
    //Login Screen
    // Variables declaration - do not modify    
    private final ClientLoginGUI loginScreen;
    private final ClientGUI clientScreen;

    
    private ClientApplication(String pID) {
        ID = pID;
        
        loginScreen = new ClientLoginGUI(this);
        clientScreen = new ClientGUI(this);
        loginScreen.setLocationRelativeTo(null);
        clientScreen.setLocationRelativeTo(null);
        loginScreen.setVisible(true);  
        
        if(connectToServer()){
            identifyAsClient();
        }

    }
        
    private void identifyAsClient (){
        //send an identifier to show this is a Client and display the reply
        String idString = "INIT|USER|" + ID ;
        sendData(idString);
    }
    

    public void handleLogin(String username, String password){
        if(username.isBlank() || password.isBlank()){
            return;
        }
        try {
            String command = "LOGIN|" + username + "|" + password;
            sendData(command);
        
            String loginResult = input.readUTF();
            System.out.println(loginResult);
            if("LOGIN FAILED".equalsIgnoreCase(loginResult)){
                System.out.println("login failed");
            } else {
                System.out.println("login successful");
                String[] serverCommand = loginResult.split("\\|");
                if(serverCommand.length > 1){
                    for(int loopcount = 1; loopcount < serverCommand.length; loopcount ++){
                        handleAddSensor(serverCommand[loopcount]);
                    }
                    requestData(serverCommand[1]);
                }
                
                loginScreen.setVisible(false);
                clientScreen.setVisible(true);
                startServerMessageThread();
            }

        } catch (IOException ex) {
            System.err.println("Unable to find server");
        }
    }
    
    public void handleLogoff(){
        String command = "LOGOFF";
        sendData(command);
        messageThread.interrupt();

        loginScreen.setVisible(true);
        clientScreen.setVisible(false);
        
        System.exit(0);
    }
    
    public void requestData(String sensorID) {
        String idString = "DATAREQUEST|" + sensorID;
        sendData(idString);
    }
    
    public void compressRequest(String sensorID) {
        String idString = "DATACOMPRESS|" + sensorID;
        sendData(idString);
    }
    
    
    private boolean connectToServer(){
        try {            
            serverAddress = InetAddress.getByName("localhost");        
            serverSocket = new Socket(serverAddress, 9090);
            //set up input and output streams
            input = new DataInputStream(serverSocket.getInputStream());
            output = new DataOutputStream(serverSocket.getOutputStream());
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ClientApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    private void sendData(String data){
        try {
            //send an identifier to show this is a Client and display the reply
            output.writeUTF(data);
        } catch (IOException ex) {
            Logger.getLogger(ClientApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    

    
    
    private void startServerMessageThread() {
        messageThread = new Thread() {            
            @Override
            public void run() {
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
                if (serverCommand != null && serverCommand.length > 0) {
                    String instruction = serverCommand[0];
                    String sensorID = serverCommand[1];
                    switch (instruction){
                        case "SENSORADDED":
                            System.out.println("Sensor Added");
                            handleAddSensor(sensorID);
                            break;
                        case "SENSORREMOVED":
                            System.out.println("Sensor Removed");
                            handleRemoveSensor(sensorID);
                            break; 
                        case "SENSORDATA":
                            System.out.println("Sensor Sent Data");
                            handleSensorData(serverMessage);
                            break; 
                        case "FIELDDATA":
                            System.out.println("Field Data");
                            handleFieldData(serverCommand);
                            break;
                    }
                }
            }
        } catch (EOFException ex) {
            try {
                serverSocket.close();
            } catch (IOException ex1) {
                Logger.getLogger(ClientApplication.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleAddSensor(String sensorID) {
        for(SensorListener listener : sensorListeners) {
            listener.sensorAdded(sensorID);
        }
    }

    private void handleRemoveSensor(String sensorID) {
        for(SensorListener listener : sensorListeners) {
            listener.sensorRemoved(sensorID);
        }
    }
    
    private void handleSensorData(String sensorData) {
        for(SensorListener listener : sensorListeners) {
            listener.sensorEmitData(sensorData);
        }
    }
    
    private void handleFieldData(String[] serverMessage) {
        clientScreen.updateField(serverMessage[1], serverMessage[2], serverMessage[3], serverMessage[4]); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void addSensorListener(SensorListener listener){
        sensorListeners.add(listener);
    }
    
    public void removeSensorListener(SensorListener listener){
        sensorListeners.remove(listener);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        ClientApplication client = new ClientApplication("Client 1"); // This weather station is working fine
        client.addSensorListener(new SensorListener() {
            @Override
            public void sensorAdded(String sensorID) {
                client.clientScreen.addSensor(sensorID);
                System.out.println("ONLINE: " + sensorID);
            }

            @Override
            public void sensorRemoved(String sensorID) {
                client.clientScreen.removeSensor(sensorID);
                System.out.println("OFFLINE: " + sensorID);
            }
            @Override
            public void sensorEmitData(String datastring) {
                client.clientScreen.processSensorData(datastring);
                System.out.println("DATA: " + datastring);
            }
        });
    }

   

    
}
