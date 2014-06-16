package project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import org.apache.bcel.util.ByteSequence;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class MainProject {

	 public static void main(String[] args) throws IOException, NXTCommException, InterruptedException, SlickException{
		 Connection t = new Connection();
		 Thread thread = new Thread(t);
		 thread.start();
		 
		 Screen sc = new Screen("test",t);
		 AppGameContainer gc = new AppGameContainer(sc,1024,720,false);
		 gc.setTargetFrameRate(5);
		 gc.start();
	 }
}

