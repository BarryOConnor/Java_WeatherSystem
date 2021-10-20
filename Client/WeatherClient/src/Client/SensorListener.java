/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

/**
 *
 * @author barry
 */
public interface SensorListener {
    public void sensorAdded(String sensorID);
    public void sensorRemoved(String sensorID);
    public void sensorEmitData(String datastring);
}
