package lab5;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import lejos.pc.comm.*;
import java.io.*;

public class PCController extends JFrame implements ActionListener
{
   private TextField nameField = new TextField(12);
   private TextField addressField = new TextField(20);

   private String name = "DHL-ONE"; 
   private String address = "00165310C79D";
   
   private TextField proportional = new TextField(10);
   private TextField integral = new TextField(10);
   private TextField derivative = new TextField(10);
   private TextField offset = new TextField(10);
     
   private NXTComm nxtComm;
   private NXTInfo nxtInfo;
   private InputStream is;
   private OutputStream os;
   private DataInputStream dis;
   private DataOutputStream dos;
   
   private JButton connectButton = new JButton("Connect");   
   private JButton setButton = new JButton("Set");
   private JButton goButton = new JButton("GO");


   /**
    * Constructor builds GUI
    */
   public PCController() 
   {		
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle("Control NXT");
      setSize(500,300);
      
      // holds labels and text fields
      JPanel p1 = new JPanel();  
      p1.add(new JLabel("Name:"));
      p1.add(nameField);
      nameField.setText(name);
      p1.add(new JLabel("Address:"));
      p1.add(addressField);
      addressField.setText(address);
     
      try
      {
         nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
      }
      catch (NXTCommException nce) {
      }
      nxtInfo = new NXTInfo();
     
      // holds connect button
      JPanel p2 = new JPanel();
      p2.add(connectButton);
      connectButton.addActionListener(this);
      
      // holds labels and text fields
      JPanel p3 = new JPanel();  
      p3.add(new JLabel("Proporitonal:"));
      p3.add(proportional);
      proportional.setText("28");
      p3.add(new JLabel("Integral:"));
      p3.add(integral);
      integral.setText("4");
      p3.add(new JLabel("Derivative"));
      p3.add(derivative);
      derivative.setText("33");
      p3.add(new JLabel("Offset"));
      p3.add(offset);
      derivative.setText("0");
      

      // holds go button
      JPanel p4 = new JPanel();
      p4.add(setButton);
      setButton.addActionListener(this);
      
      JPanel p6 = new JPanel();
      p6.add(goButton);
      goButton.addActionListener(this);
      
   
      // North area of the frame
      JPanel panel = new JPanel();  
      panel.setLayout(new GridLayout(5,1));
      panel.add(p1);
      panel.add(p2);
      panel.add(p3);
      panel.add(p4);
      panel.add(p6);
      add(panel,BorderLayout.NORTH);

   }
   /**
    * Required by action listener; 
    * only action is generated by the get Length button
    */	
   public void actionPerformed(ActionEvent e)
   {
      if(e.getSource()== connectButton)
      {
         String name = nameField.getText();
         String address = addressField.getText();
         nxtInfo.name = name;
         nxtInfo.deviceAddress = address;
         try
         {
            nxtComm.open(nxtInfo);
            is = nxtComm.getInputStream();
            os = nxtComm.getOutputStream();
      	    dis = new DataInputStream(is);
      	    dos = new DataOutputStream(os);
         }
         catch (Exception ex) {
         }
      }	  
      
      if(e.getSource() == goButton){
    	  try
          {
     	     dos.writeByte(1);
             dos.flush();
          }
          catch (Exception ex) {
          } 
      }
	   
      if(e.getSource()== setButton)
      {
    	  System.out.println("Sending.");
    	 try
         {
    	    String ps = proportional.getText();
    	    byte p = Byte.parseByte(ps);    
    	    String iStr = integral.getText();
    	    byte i = Byte.parseByte(iStr);    
    	    String dStr = derivative.getText();
    	    byte d = Byte.parseByte(dStr);  
    	    String offStr = offset.getText();
    	    byte o = Byte.parseByte(offStr);  
    	    
    	    byte[] aray = new byte[4];
    	    aray[0] = p;
    	    aray[1] = i;
    	    aray[2] = d;
    	    aray[3] = o;
    	    dos.write(aray);
            dos.flush();
            System.out.println("" + p + " " + i + " " + d);
         }
         catch (Exception ex) {
        	 System.err.println(ex);
         }           
      }
   }
   
   /**
    * Initialize the display Frame
    */		
   public static void main(String[] args)
   {
      PCController frame = new PCController();
      frame.setVisible(true);
   }
}	