package lab3;

import embedded.DataLogger;
import lejos.nxt.*;
/**
 * A simple sound sensor sampling program
 * that samples the sensor with a fixed sample interval
 * and store the values in a flash file "Sample.txt".
 * 
 * The sound sensor should be connected to port 1. 
 * 
 * @author  Ole Caprani
 * @version 2.09.08
 */
public class SoundSampling 
{
   public static void main(String [] args) throws Exception
   {
       SoundSensor s = new SoundSensor(SensorPort.S1);
       DataLogger dl = new DataLogger("Sample.txt");
       int soundLevel;
	   
       LCD.drawString("Level: ", 0, 0);

       while (! Button.ESCAPE.isDown())
       {		   
           soundLevel = s.readValue();
           LCD.drawInt(soundLevel,3,7,0);
		   
           dl.writeSample(soundLevel);

           Thread.sleep(5);
       }
       dl.close();
       LCD.clear();
       LCD.drawString("Program stopped", 0, 0);
       Thread.sleep(2000);
   }
}
