package sensors;

/**
 * @Author Cam Hatherell
 * 
 * Interface for the various sensors used in this project, 
 * helping to maintain a common standard and allow for common
 * methods
 */


public interface Sensor {
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * The possible types of the sensor
	 */
	public enum Type{
		MPU6050, CONTACT;
		
		/**
		 * @Author Cam Hatherell
		 * 
		 * Returns the type of the sensor as a string
		 */
		
		@Override
		public String toString(){
			switch(this){
			case MPU6050:
				return "MPU6050";
			case CONTACT:
				return "CONTACT";
			default:
				return "UNKNOWN";
			}
		}
	}
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * Deactivates the sensor
	 */
	public abstract void deactivate();
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * Returns values pertaining to the specific sensor as a string
	 */
	public abstract String getData();
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * Returns whether the sensor is active
	 */
	public abstract boolean isActive();
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * Returns whether the sensor is calibrated
	 */
	public abstract boolean isCalibrated();
	
	/**
	 * @Author Cam Hatherell
	 * 
	 * Returns the type of the sensor
	 */
	public abstract Type getType();
	
}
