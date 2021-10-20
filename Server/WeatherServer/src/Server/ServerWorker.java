/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
/**
 *
 * @author Barry, Kieran, Nathan and Finlay
 */



public class ServerWorker extends Thread{
    private ClientType clientType;
    private Server server;
    private Socket clientSocket;
    private String clientID;
    private String GPS;
    private String listeningTo;
    private boolean loggedIn;
    private DataInputStream input;
    private DataOutputStream output;
   
    

    public ServerWorker(Server pServer, Socket pClientSocket) {
        try {
            //initialise with the socket value for the client
            clientSocket = pClientSocket;
            server = pServer;
            loggedIn = false;
            GPS = null;
            listeningTo = null;
            
            output = new DataOutputStream(clientSocket.getOutputStream());
            input = new DataInputStream(clientSocket.getInputStream());

        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
    private void listenToClient(){
        try {
            String clientData = "";
            
            while ((clientData = input.readUTF()) != null) {
                
                String[] commandArray = clientData.split("\\|");

                if (commandArray != null && commandArray.length > 0) {
                    String instruction = commandArray[0];
                    
                    
                    switch (instruction){
                        case "INIT":
                            System.out.println("Client Initialising");
                            assignClientType(commandArray);
                            break;
                        case "LOGIN":
                            System.out.println("Client Logging In");
                            handleLogin(commandArray);
                            break;
                        case "SENSORDATA":
                            handleData(commandArray[1], clientData);
                            break;
                        case "DATAREQUEST":
                            handleDataRequest(commandArray);
                            break;
                        case "DATACOMPRESS":
                            handleDataCompress(commandArray);
                            break;
                        case "LOGOFF":
                        case "EXIT":
                            handleExit();
                            break;
                            
                    }
                }
            }
        } catch (EOFException ex) {
            System.out.println("shutdown");
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public boolean isLoggedIn(){
        return loggedIn;
    }
    
    @Override
    public String toString(){
        return clientID;
    }
    
    public String getDisplayName(){
        return clientID;
    }
    
    public ClientType getClientType(){
        return clientType;
    }

    private void assignClientType(String[] commandArray){
        if(commandArray.length >= 2 ){
            String connectionType = commandArray[1];
            if("SENSOR".equalsIgnoreCase(connectionType)){
                clientType = ClientType.Sensor;
                clientID = commandArray[2];
                GPS = commandArray[3];
                server.addClient(this);
                
                
                //send a message to all connected users to update the sensor list via listeners
                List<ServerWorker> clients = server.getConnectedClients();
                for(ServerWorker client : clients) {
                    client.send("SENSORADDED|" + clientID);
                }
            } else if("USER".equalsIgnoreCase(connectionType)){
                clientType = ClientType.User;
                clientID = commandArray[2];

            } 
        }
    }
    
    private boolean login(String username, String password){
        /**
         * This function reads the file containing all of the login details and then 
         * iterates through them all. Checking the received username,password pairing 
         * against each one. If there is a match it changes returns true and breaks the loop. 
         * If none return true this function will return false
         */
        
        ServerFileHandler loginfile = new ServerFileHandler("users.txt");
        List<String> lines = loginfile.readlines();

        String attempt = username + "|" + password;

        for( String line : lines){

            if(line.equalsIgnoreCase(attempt)){
                
                return true;
            }           
        }   
        return false;
    }
        
    private void handleLogin(String[] command){
        if (command.length == 3) {
            String username = command[1];
            String password = command[2];
            boolean loginSuccessful = login(username, password);
            if (loginSuccessful) {
                //sends messages for each sensor
                List<ServerWorker> sensors = server.getConnectedSensors();
                String message = "LOGIN SUCCESS";
                for(ServerWorker sensor : sensors) {
                    message += "|" + sensor.toString();  
                }
                send(message);
                clientID = username;
                loggedIn = true;
                server.addClient(this);
                System.out.println("User logged in succesfully: " + username);
                
                ServerFileHandler fieldfile = new ServerFileHandler("field.txt");
                List<String> fields = fieldfile.readlines();
                for(String field : fields) {
                    message = "FIELDDATA|" + field;  
                }
                send(message);
                
            } else {
                System.err.println("Login failed for " + username);
                send("LOGIN FAILED");
            }
        }
    }
    
    private void handleData(String sensorID, String data) {
        List<ServerWorker> clients = server.getConnectedClients();
        System.out.println(data);
        for(ServerWorker client : clients) {
            if(client.listeningTo.equals(sensorID)){
                client.send(data);
            }
        }
    }

    private void handleDataRequest(String[] commandArray) {
        listeningTo = commandArray[1];
        System.out.println("Handling Data Request");
        List<ServerWorker> sensors = server.getConnectedSensors();
        for(ServerWorker sensor : sensors) {
            if (sensor.getDisplayName().equals(listeningTo)){
                System.out.println("Data Request Sent");
                sensor.send("DATAREQUESTED");
            }
        }
    }
    
    private void handleDataCompress(String[] commandArray) {
        String sensorID = commandArray[1];
        System.out.println("Handling Data Compress Request");
        List<ServerWorker> sensors = server.getConnectedSensors();
        for(ServerWorker sensor : sensors) {
            if (sensor.getDisplayName().equals(sensorID)){
                System.out.println("Data Compress Request Sent");
                sensor.send("DATACOMPRESS");
            }
        }
    }
    
    
    private void handleExit() throws IOException{
        server.removeClient(this);
        loggedIn = false;
        clientSocket.close();
    }
    
    private void send(String message){
        try {
            if(this.clientType == ClientType.User){
                if (isLoggedIn() || (message.startsWith("LOGIN"))) {
                    System.out.println("Sending: " + message);
                    output.writeUTF(message);
                }
            } else {
                System.out.println("Sending: " + message);
                output.writeUTF(message);
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    @Override
    public void run() {
         listenToClient();
    }


}

