package embedded;

import lejos.nxt.*;
import java.io.*;

/**
 * A simple data logger that can be used to sample a sequence of integer data
 * values in a flash file. The file is a text consosting of a sequence of lines
 * with a time value and a sample data value separated by a comma.
 * 
 * When data has been sampled the flash file can be transfered to a PC by means
 * of the tool nxjbrowse.
 * 
 * @author Ole Caprani
 * @version 4.02.13
 */
public class DataLogger {
	private File f;
	private FileOutputStream fos;
	private int startTime;

	public DataLogger(String fileName) {
		try {
			f = new File(fileName);
			if (!f.exists()) {
				f.createNewFile();
			} else {
				f.delete();
				f.createNewFile();
			}

			fos = new FileOutputStream(f);
		} catch (IOException e) {
			LCD.drawString(e.getMessage(), 0, 0);
			System.exit(0);
		}
		startTime = (int) System.currentTimeMillis();
	}

	public void start() {
		startTime = (int) System.currentTimeMillis();
	}

	public void writeSample(int time, int sample) {

		try {
			Integer timeInt = new Integer(time);
			String timeString = timeInt.toString();

			Integer sampleInt = new Integer(sample);
			String sampleString = sampleInt.toString();

			for (int i = 0; i < timeString.length(); i++) {
				fos.write((byte) timeString.charAt(i));
			}

			// Separate items with comma
			fos.write((byte) (','));

			for (int i = 0; i < sampleString.length(); i++) {
				fos.write((byte) sampleString.charAt(i));
			}

			// New line
			fos.write((byte) ('\r'));
			fos.write((byte) ('\n'));

		} catch (IOException e) {
			LCD.drawString(e.getMessage(), 0, 0);
			System.exit(0);
		}
	}
	
	public void writeSample(int time, long sample) {

		try {
			Integer timeInt = new Integer(time);
			String timeString = timeInt.toString();

			Long sampleLong = new Long(sample);
			String sampleString = sampleLong.toString();

			for (int i = 0; i < timeString.length(); i++) {
				fos.write((byte) timeString.charAt(i));
			}

			// Separate items with comma
			fos.write((byte) (','));

			for (int i = 0; i < sampleString.length(); i++) {
				fos.write((byte) sampleString.charAt(i));
			}

			// New line
			fos.write((byte) ('\r'));
			fos.write((byte) ('\n'));

		} catch (IOException e) {
			LCD.drawString(e.getMessage(), 0, 0);
			System.exit(0);
		}
	}

	public void writeSample(int sample) {
		writeSample((int) System.currentTimeMillis() - startTime, sample);
	}

	public void close() {
		try {
			fos.close();
		} catch (IOException e) {
			LCD.drawString(e.getMessage(), 0, 0);
			System.exit(0);
		}
	}

	public void writeSample(long sample) {
		writeSample((int) System.currentTimeMillis() - startTime, sample);		
	}
}
