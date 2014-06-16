package project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.newdawn.slick.geom.Vector2f;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class Connection implements Runnable {

	NXTComm nxtComm;
	NXTInfo nxtInfo;
	String name = "DHL-ONE";
	String address = "00165310C79D";

	private List<LocationData> points = new ArrayList<LocationData>();

	public List<LocationData> getPoints(){
		List<LocationData> l;
		synchronized (points) {
			l = new ArrayList<>(points);
		}
		return l;
	}

	public int bytesToInt(byte[] bs) {
		int i = 0;

		i = (bs[0] << 24) & 0xff000000 | (bs[1] << 16) & 0x00ff0000
				| (bs[2] << 8) & 0x0000ff00 | (bs[3]) & 0x000000ff;

		return i;

	}

	public int readNextInt(InputStream in) {
		byte[] wi = new byte[4];
		try {
			in.read(wi);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytesToInt(wi);
	}

	public float readNextFloat(InputStream in) {
		int t = readNextInt(in);
		return Float.intBitsToFloat(t);
	}

	@Override
	public void run() {
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
		} catch (NXTCommException nce) {
		}
		nxtInfo = new NXTInfo();
		nxtInfo.name = name;
		nxtInfo.deviceAddress = address;

		try {
			nxtComm.open(nxtInfo);
		} catch (NXTCommException e1) {
			e1.printStackTrace();
		}

		OutputStream out = nxtComm.getOutputStream();
		InputStream in = nxtComm.getInputStream();

		try {
			while (true) {
				{
					int which = readNextInt(in);

					if (which == 1) {
						int x = readNextInt(in);
						int y = readNextInt(in);

						System.out.println("(x,y) = " + x + ", " + y);
					}

					if (which == 2) {
						int type = readNextInt(in);
						float arcRadius = readNextFloat(in);
						float turned = readNextFloat(in);
						float distance = readNextFloat(in);
						System.out.println("Event. Type = " + type
								+ " arcRadius= " + arcRadius + " turned= "
								+ turned + " distance=" + distance);
					}

					if (which == 3) {
						int id = readNextInt(in);
						float x = readNextFloat(in);
						float y = readNextFloat(in);
						int dir = readNextInt(in);

						synchronized(points){
							LocationData d = new LocationData();
							d.id = id;
							d.x = x;
							d.y = y;
							d.dir = dir;
							points.add(d);
						}

						System.out.println("Location. id = " + id + ",  x ="
								+ x + ",  y = " + y + ", dir = " + dir);
					}

					if (which == 4) {
						System.out.println("null visited");
					}

					if (which == 5) {
						System.out.println("bigger than 50");
					}
					if (which == 6) {
						System.out.println("smaller than 50");
					}
				}

				Thread.sleep(10);
			}
		} catch (Exception e) {
			System.exit(0);
		}
	}

}