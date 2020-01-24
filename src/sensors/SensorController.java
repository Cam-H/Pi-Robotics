/**
 * @author Cam Hatherell
 * 
 * Handles initialization of all the rPi 
 * systems needed to control the various sensors
 */

package sensors;

import java.io.IOException;

import main.Main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorController {
	
	private I2CBus bus = null;//The I2CBus
	private GpioController gpio = null;//The Gpio controller

	public static final float refreshRate = 10f;//Rate at which to refresh the sensors in Hz
	
	/**
	 * @author Cam Hatherell
	 * 
	 * Initializes the I2CBus and Gpio Controller
	 */
	public SensorController(){
		
		try {
			initializeI2C();
		} catch (UnsupportedBusNumberException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initializeGPIO();
		
	}
	
	/**
	 * @author Cam Hatherell
	 * 
	 * Initializes the bus
	 * 
	 * @throws UnsupportedBusNumberException
	 * @throws IOException
	 */
	public void initializeI2C() throws UnsupportedBusNumberException, IOException{
        bus = I2CFactory.getInstance(I2CBus.BUS_1);
	}
	
	/**
	 * @author Cam Hatherell
	 * 
	 * Initializes the Gpio controller
	 */
	public void initializeGPIO(){
		gpio = GpioFactory.getInstance();
	}

	/**
	 * @author Cam Hatherell
	 * @return bus - The I2C bus
	 */
	public I2CBus getBus() {
		return bus;
	}
	
	/**
	 * @author Cam Hatherell
	 * @return gpio - The gpio controller
	 */
	public GpioController getGpio(){
		return gpio;
	}
	
	/**
	 * @author Cam Hatherell
	 * 
	 * Shuts down the gpio controller
	 */
	public void shutdownGPIO(){
//		//Runs through all the gpio-based sensors and deactivates them
//		for(Sensor sensor : Main.sensors){
//			if(sensor.getType() == Type.CONTACT){
//				sensor.deactivate();
//			}
//		}
//		
		gpio.shutdown();
			
	}
}
