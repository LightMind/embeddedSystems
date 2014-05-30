package project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.bcel.util.ByteSequence;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class MainProject {
	static NXTComm nxtComm;
	static NXTInfo nxtInfo;
	static String name = "DHL-ONE";
	static String address = "00165310C79D";
	
	
	public static int bytesToInt(byte[] bs){
		int i = 0;
		
		i = (bs[0] << 24)&0xff000000 |
			(bs[1] << 16)&0x00ff0000 |
			(bs[2] << 8) &0x0000ff00 |
			(bs[3])&0x000000ff;
		
		return i;
		
	}
	
	public static int readNextInt(InputStream in){
		 byte[] wi = new byte[4];	
		 try {
			in.read(wi);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return bytesToInt(wi);
	}
	
	public static float readNextFloat(InputStream in){
		int t  =  readNextInt(in);
		return Float.intBitsToFloat(t);		
	}
	
	 public static void main(String[] args) throws IOException, NXTCommException, InterruptedException{
		 try
	     {
	        nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
	     }
	     catch (NXTCommException nce) {
	     }
	     nxtInfo = new NXTInfo();
	     nxtInfo.name = name;
	     nxtInfo.deviceAddress = address;
	     
	     nxtComm.open(nxtInfo);
	     
	     OutputStream out = nxtComm.getOutputStream();
	     InputStream in = nxtComm.getInputStream();
	     
	     
	     while(true){
	    	 {
	    		 int which = readNextInt(in);
	    		 
	    		 if(which == 1){			    	
		    		 int x = readNextInt(in);		    				 
		    		 int y = readNextInt(in);
		    		 
			    	 System.out.println("(x,y) = " + x +", " + y );
	    		 } 
	    		 
	    		 if(which == 2){
	    			 int type = readNextInt(in);
	    			 float arcRadius = readNextFloat(in);
	    			 float turned = readNextFloat(in);
	    			 float distance = readNextFloat(in);	    			 
	    			 System.out.println("Event. Type = " + type + " arcRadius= " + arcRadius + " turned= " + turned + " distance=" + distance);
	    		 }
	    	 }
	    	 
	    	 Thread.sleep(10);
	     }	    
	 }
}
