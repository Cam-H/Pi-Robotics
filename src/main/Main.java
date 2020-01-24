package main;

import io.MouseController;
import sensors.MPU6050;
import sensors.SensorController;

public class Main {
	
	public static void main(String[] args){
		SensorController sc = new SensorController();//The controller for the sensors

		MPU6050 demoMpu = new MPU6050(sc.getBus(), 0x68);//An mpu used specifically for the demo
		
		MouseController mc = new MouseController(500, 500);
		
		while(!demoMpu.isCalibrated()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while(true){
//			System.out.println(demoMpu.getAlpha() + " " + demoMpu.getBeta() + " " + demoMpu.getGamma());
			
			double x = 1.57 - demoMpu.getAlpha();
			double y = demoMpu.getBeta() - 1.57;
			
			x *= 10;
			y *= 10;
			
//			System.out.println(x + " " + y);
			mc.offsetMouse((int)x, (int)y);
			
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
