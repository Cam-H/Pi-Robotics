package io;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;

public class MouseController extends Thread {
	
	private Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
	
	private Robot robot;
	
	private int mx;
	private int my;
	
	public MouseController(int cx, int cy){
		mx = cx;
		my = cy;
		
		try{
			robot = new Robot();
			robot.mouseMove(cx, cy);
		}catch(AWTException e){}
		
		start();
	}
	
	public void run(){
		
		while(true){
			
			if(mx < 0){
				mx = 0;
			}
			
			if(mx > screenDimensions.width){
				mx = screenDimensions.width;
			}
			
			if(my < 0){
				my = 0;
			}
			
			if(my > screenDimensions.height){
				my = screenDimensions.height;
			}
			
			robot.mouseMove(mx, my);
			
			try {
				Thread.sleep(1000 / 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void offsetMouse(int dx, int dy){
		mx += dx;
		my += dy;
	}

}
