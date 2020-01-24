/**
 * @Author Cam Hatherell, Ulrich B
 * 
 * Handles the registers and data collection from the MPU6050 chips
 */

package sensors;

import java.io.IOException;

import toolbox.MyMath;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

public class MPU6050 extends Thread implements Sensor {
	
	private Type type = Type.MPU6050;//The type of sensor
	
	//The expected offsets of the sensor when it is oriented normally in space
	private static final short expectedXOffset = 0;
    private static final short expectedYOffset = 0;
    private static final short expectedZOffset = 16384;
	
	private I2CBus bus = null;//The I2C bus
    private I2CDevice mpu6050 = null;//The I2C device
    
    //The recorded offsets found after calibration of the sensors is complete
    private short xOffset;
    private short yOffset;
    private short zOffset;
    
    private double alpha;//x rotation
    private double beta;//y rotation
    private double gamma;//z rotation
    
    private boolean calibrated;//Whether the sensor is calibrated
    private boolean running;//Whether the sensor is currently operating

    /**
     * @author Cam Hatherell
     * 
     * Initializes the mpu6050 and associated variables
     * 
     * @param bus
     * @param address - The I2C address of the sensor
     */
    public MPU6050(I2CBus bus, int address){
    	
    	this.bus = bus;
    	
    	try {
			initialize(address);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	alpha = beta = gamma = 0;
    	
    	calibrated = false;
    	running = true;
    	
    	start();
    	
    }
    
    /**
     * @author Cam Hatherell
     * 
     * Initializes the mpu6050
     * 
     * @param address - The I2C address of the sensor
     */
    public void initialize(int address) throws IOException, InterruptedException {
        mpu6050 = bus.getDevice(address);

        configureMpu6050();
    }

    /**
     * Configures the mpu6050
     * 
     * @author Ulrich B
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    private void configureMpu6050() throws IOException, InterruptedException {

        //1 Waking the device up
        writeConfigRegisterAndValidate(
                "Waking up device",
                "Wake-up config succcessfully written: ",
                Registers.MPU6050_RA_PWR_MGMT_1,
                RegisterValues.MPU6050_RA_PWR_MGMT_1);

        //2 Configure sample rate
        writeConfigRegisterAndValidate(
                "Configuring sample rate",
                "Sample rate succcessfully written: ",
                Registers.MPU6050_RA_SMPLRT_DIV,
                RegisterValues.MPU6050_RA_SMPLRT_DIV);

        //3 Setting global config
        writeConfigRegisterAndValidate(
                "Setting global config (digital low pass filter)",
                "Global config succcessfully written: ",
                Registers.MPU6050_RA_CONFIG,
                RegisterValues.MPU6050_RA_CONFIG);

        //4 Configure Gyroscope
        writeConfigRegisterAndValidate(
                "Configuring gyroscope",
                "Gyroscope config successfully written: ",
                Registers.MPU6050_RA_GYRO_CONFIG,
                RegisterValues.MPU6050_RA_GYRO_CONFIG);

        //5 Configure Accelerometer
        writeConfigRegisterAndValidate(
                "Configuring accelerometer",
                "Accelerometer config successfully written: ",
                Registers.MPU6050_RA_ACCEL_CONFIG,
                RegisterValues.MPU6050_RA_ACCEL_CONFIG);

        //6 Configure interrupts
        writeConfigRegisterAndValidate(
                "Configuring interrupts",
                "Interrupt config successfully written: ",
                Registers.MPU6050_RA_INT_ENABLE,
                RegisterValues.MPU6050_RA_INT_ENABLE);

        //7 Configure low power operations
        writeConfigRegisterAndValidate(
                "Configuring low power operations",
                "Low power operation config successfully written: ",
                Registers.MPU6050_RA_PWR_MGMT_2,
                RegisterValues.MPU6050_RA_PWR_MGMT_2);
    }
    
    /**
     * Runs until the program is terminated, fetching data from the sensor all the while
     */
    public void run(){
    	
    	//Calibrating the sensor
    	System.out.println("Calibrating sensor...");
        
        calibrateSensorOffset();
        System.out.println("Calibrated");
        calibrated = true;
        
        System.out.println("Offsets: "
        		+"\nx Offset: "+xOffset
        		+"\ny Offset: "+yOffset
        		+"\nz Offset: "+zOffset); 
    	
    	while(running){//Loop forever
        	
    		//
        	short[] data = getSensorData();

        	float xg = (data[0] + xOffset) / 16384f;
        	float yg = (data[1] + yOffset) / 16384f;
        	float zg = (data[2] + zOffset) / 16384f;
        	
        	float fg = (float)Math.sqrt(Math.pow(xg, 2) + Math.pow(yg, 2) + Math.pow(zg, 2));
            
            alpha = Math.acos(xg / fg);
            beta = Math.acos(yg / fg);
            gamma = Math.acos(zg / fg);
            
            //Wait for the time defined in SensorController
            try {
				Thread.sleep((long)(1000 / SensorController.refreshRate));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    	
    }
    
    /**
     * Sets the run method up to stop upon the next iteration
     * 
     * @author Cam Hatherell
     */
    public void kill(){
    	running = false;
    }
    
    /**
     * Calibrates the sensor offset of the mpu
     * 
     * @author Cam Hatherell
     */
    private void calibrateSensorOffset(){
    	
    	int tests = 20;//The number of tests to run
    	
    	int x = 0;
    	int y = 0;
    	int z = 0;
    	
    	for(int i = 0; i < tests; i++){
    		short[] data = getSensorData();
    		
    		x += data[0];
    		y += data[1];
    		z += data[2];
    		
    		//Delay for a half-second so that all tests are not run near immediately (would make it redundant)
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	//Get the average between all the tests as offsets
    	xOffset = (short) (expectedXOffset - (x / tests));
    	yOffset = (short) (expectedYOffset - (y / tests));
    	zOffset = (short) (expectedZOffset - (z / tests));

    }
    
    /**
     * Gets the and returns the current acceleration values of the mpu6050 and returns them as a short array
     * 
     * @author Cam Hatherell
     * 
     * @return short[] - The values from the mpu6050
     */
    private short[] getSensorData(){
    	
    	//Set up the byte buffers
    	byte[] xBuffer = new byte[2], yBuffer = new byte[2], zBuffer = new byte[2];
    	
    	//Read in the data
    	try {
			mpu6050.read(Registers.ACCEL_XOUT_H, xBuffer, 0, 2);
			mpu6050.read(Registers.ACCEL_YOUT_H, yBuffer, 0, 2);
	    	mpu6050.read(Registers.ACCEL_ZOUT_H, zBuffer, 0, 2);
		} catch (IOException e) {
			e.printStackTrace();
		}
  
    	//Convert the byte arrays into shorts
    	short x = (short)(((xBuffer[0] & 0xFF) << 8) | (xBuffer[1] & 0xFF));
    	short y = (short)(((yBuffer[0] & 0xFF) << 8) | (yBuffer[1] & 0xFF));
    	short z = (short)(((zBuffer[0] & 0xFF) << 8) | (zBuffer[1] & 0xFF));
    	
    	return new short[]{x, y, z};
    }
    
    /**
     * Writes the specified data to the given register
     * 
     * @param register - The register to write to
     * @param data - The data to write
     * @throws IOException
     */
    private void writeRegister(byte register, byte data) throws IOException {
        mpu6050.write(register, data);
    }

    /**
     * 
     * Reads the specified register
     * 
     * @param register - The register to read
     * @return data - byte value 
     * @throws IOException
     */
    public byte readRegister(byte register) throws IOException {
        int data = mpu6050.read(register);
        return (byte) data;
    }

    /**
     * Reads data in from the mpu6050
     * 
     * @return data - byte value
     * @throws IOException
     */
    public byte readRegister() throws IOException {
        int data = mpu6050.read();
        return (byte) data;
    }

    /**
     * 
     * @author Ulrich B
     * 
     * @param initialText
     * @param successText
     * @param register
     * @param registerData
     * @throws IOException
     */
    public void writeConfigRegisterAndValidate(String initialText, String successText, byte register, byte registerData) throws IOException {
        System.out.println(initialText);
        writeRegister(register, registerData);
        byte returnedRegisterData = readRegister(register);
        if (returnedRegisterData == registerData) {
            System.out.println(successText + formatBinary(returnedRegisterData));
        } else {
            throw new RuntimeException("Tried to write " + formatBinary(registerData) + " to "
                    + register + ", but validiating value returned " + formatBinary(returnedRegisterData));
        }
    }
    
    /**
     * 
     * @author Ulrich B
     * 
     * @param b
     * @return
     */
    public String formatBinary(byte b) {
        String binaryString = Integer.toBinaryString(b);
        if (binaryString.length() > 8) {
            binaryString = binaryString.substring(binaryString.length() - 8);
        }
        if (binaryString.length() < 8) {
            byte fillingZeros = (byte) (8 - binaryString.length());
            for (int j = 1; j <= fillingZeros; j++) {
                binaryString = "0" + binaryString;
            }
        }
        return binaryString;
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the x-axis as a double, in radians
     */
    public double getAlpha(){
    	return alpha;
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the y-axis as a double, in radians
     */
    public double getBeta(){
    	return beta;
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the z-axis as a double, in radians
     */
    public double getGamma(){
    	return gamma;
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the x-axis as an int
     */
    public int getXRotation(){
    	return (int)Math.toDegrees(alpha);
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the y-axis as an int
     */
    public int getYRotation(){
    	return (int)Math.toDegrees(beta);
    }
    
    /**
     * 
     * @return rotation - The rotation of the sensor along the z-axis as an int
     */
    public int getZRotation(){
    	return (int)Math.toDegrees(gamma);
    }
    
    /**
     * 
     * @return calibrated - Whether the sensor is calibrated yet
     */
    public boolean calibrated(){
    	return calibrated;
    }
    
    /**
	 * Returns the relevant data associated with this sensor as a string
	 * 
	 * @author Cam Hatherell
	 */
	@Override
	public String getData() {
		return "Roll: "+ MyMath.getDoubleTo(2, alpha) + " | Pitch: "+ MyMath.getDoubleTo(2, beta) +" | Yaw: "+ MyMath.getDoubleTo(2, gamma);
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public boolean isCalibrated() {
		return calibrated;
	}

	/**
	 * Returns the type of sensor that this is
	 * 
	 * @author Cam Hatherell
	 */
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void deactivate() {}
	
    /**
     * 
     * The register pins on the MPU6050 chip
     * 
     * @author Ulrich B
     *
     */
	private static final class Registers{
		
		public static final byte MPU6050_RA_PWR_MGMT_1 = 107;
		public static final byte MPU6050_RA_SMPLRT_DIV = 25;
		public static final byte MPU6050_RA_CONFIG = 26;
		public static final byte MPU6050_RA_GYRO_CONFIG = 27;
		public static final byte MPU6050_RA_ACCEL_CONFIG = 28;
		public static final byte MPU6050_RA_INT_ENABLE = 56;
		public static final byte MPU6050_RA_PWR_MGMT_2 = 108;
		
		/*********************Reading in*****************/
		
//		public static final int PWR_MGMT_1 = 0x6B;
//		public static final int SMPLRT_DIV = 0x19;
//		public static final int CONFIG = 0x1A;
//		public static final int GYRO_CONFIG = 0x1B;
//		public static final int INT_ENABLE = 0x38;
		
		
		public static final int ACCEL_XOUT_H = 0x3B;
		public static final int ACCEL_YOUT_H = 0x3D;
		public static final int ACCEL_ZOUT_H = 0x3F;
		
//		public static final int GYRO_XOUT_H = 0x43;
//		public static final int GYRO_YOUT_H = 0x45;
//		public static final int GYRO_ZOUT_H = 0x47;
		
	}
	
	/**
	 * 
	 * The values to set the various registers on configuration
	 * 
	 * @author Ulrich B
	 *
	 */
	private static final class RegisterValues{
		
		/**
		* Just wakes the device up, because it sets the 
		* sleep bit to 0. Also sets
		* the clock source to internal.
		*/
		public static final byte MPU6050_RA_PWR_MGMT_1 = 0b00000000;
		/**
		* Sets the full scale range of the gyroscopes to ± 2000 °/s
		*/
		public static final byte MPU6050_RA_GYRO_CONFIG = 0b00011000;
		/**
		* Sets the smaple rate divider for the gyroscopes and 
		* accelerometers. This
		* means
		* acc-rate = 1kHz / 1+ sample-rate
		* and
		* gyro-rate = 8kHz /
		* 1+ sample-rate.
		* The concrete value 0 leaves the sample rate on
		* default, which means 1kHz for acc-rate and 
		* 8kHz for gyr-rate.
		*/
		public static final byte MPU6050_RA_SMPLRT_DIV = 0b00000000;
		/**
		* Setting the digital low pass filter to
		* Acc Bandwidth (Hz) = 184
		* Acc Delay (ms) = 2.0
		* Gyro Bandwidth (Hz) = 188
		* Gyro Delay (ms) = 1.9
		* Fs (kHz) = 1
		*
		*/
		public static final byte MPU6050_RA_CONFIG = 0b00000001;
		/**
		* Setting accelerometer sensitivity to ± 2g
		*/
		public static final byte MPU6050_RA_ACCEL_CONFIG = 0b00000000;
		/**
		* Disabling FIFO buffer
		*/
//		public static final byte MPU6050_RA_FIFO_EN = 0b00000000;
		/**
		* Disabling interrupts
		*/
		public static final byte MPU6050_RA_INT_ENABLE = 0b00000000;
		/**
		* Disabling standby modes
		*/
		public static final byte MPU6050_RA_PWR_MGMT_2 = 0b00000000;
		
	}	
}
