/**
 * @author Cam Hatherell
 * 
 * A math class with a few extra functions that are useful in the 
 * program but are not implement in the base Math class
 */
package toolbox;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MyMath {
	
	/**
	 * Floors the given float to the given decimal place
	 * 
	 * @author Cam Hatherell
	 * 
	 * @param decimals - How many decimals should be in the returned value (maximum)
	 * @param value - The value to floor
	 * 
	 * @return output - The floored value
	 */
	public static float getFloatTo(int decimals, float value){
		return (float)((int)(value * Math.pow(10, decimals)) / Math.pow(10, decimals));
	}
	
	/**
	 * Floors the given double to the given decimal place
	 * 
	 * @author Cam Hatherell
	 * 
	 * @param decimals - How many decimals should be in the returned value (maximum)
	 * @param value - The value to floor
	 * 
	 * @return output - The floored value
	 */
	public static double getDoubleTo(int decimals, double value){
		return ((int)(value * Math.pow(10, decimals)) / Math.pow(10, decimals));
	}
	
	/**
	 * 
	 * Returns a byte array for the given float in big endian
	 * 
	 * @author Baeldung
	 * 
	 * @param value - input float
	 * @return buffer - The resulting byte buffer in Big Endian
	 */
	public static byte[] floatToByteArray(float value){
		int intBits = Float.floatToIntBits(value);
		
		return new byte[]{(byte) (intBits >> 24), (byte) (intBits >> 16),(byte) (intBits >> 8), (byte) (intBits)};
	}
	
	/**
	 * 
	 * Returns a float from a byte array, using a big endian conversion
	 * 
	 * @author Cam Hatherell
	 * 
	 * @param value - The byte array to transform
	 * @return The big-endian float
	 */
	public static float byteArrayToFloat(byte[] value){
		ByteBuffer bb = ByteBuffer.wrap(new byte[] {value[0],value[1] ,value[2], value[3]});
		bb.order(ByteOrder.BIG_ENDIAN);
		
		return bb.getFloat();
	}

}
