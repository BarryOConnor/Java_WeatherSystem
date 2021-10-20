/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Barry, Kieran, Nathan and Finlay
 */

public class Server extends Thread {
    private final int serverPort;
    private final ServerApplication GUI;
    private ServerSocket serverSocket;
    DataInputStream input;
    DataOutputStream output;
    
    private ArrayList<ServerWorker> connectedSensors = new ArrayList<>();
    private ArrayList<ServerWorker> connectedClients = new ArrayList<>();
      
    
    public Server(ServerApplication pGUI){
        GUI = pGUI;
        serverPort = 9090;
    }
    
    public void addClient(ServerWorker client){
        if(client.getClientType()==ClientType.Sensor){
            connectedSensors.add(client);
            GUI.addSensorToModel(client.getDisplayName());
        } else {
            connectedClients.add(client);
            GUI.addClientToModel(client.getDisplayName());
        }
    };
    
    public void removeClient(ServerWorker client){
        if(client.getClientType()==ClientType.Sensor){
            connectedSensors.remove(client);
            GUI.removeSensorFromModel(client.getDisplayName());
        } else {
            connectedClients.remove(client);
            GUI.removeClientFromModel(client.getDisplayName());
        }
    };
        
    public List<ServerWorker> getConnectedSensors() {
        return connectedSensors;
    }

    public List<ServerWorker> getConnectedClients() {
        return connectedClients;
    }
    
    @Override
    public void run(){
        try {
            serverSocket = new ServerSocket(serverPort);
            
            while(true){
                //print waiting message and define a variable for the client socket
                System.out.println("Waiting for client...");
                //accept the connection from the client/sensor app
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client Connected. Moving to thread...");
                ServerWorker workerThread = new ServerWorker( this, clientSocket);
                workerThread.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }        
        System.out.println("Server Stopped.") ;
    }

}
