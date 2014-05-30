package project;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;

public class GraphBotMain {

	public static void main(String[] args) throws Exception {
		escapeSetup();
		GraphBot gb = new GraphBot();
		gb.run();
	}
	
	
	public static void escapeSetup(){
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			@Override
			public void buttonReleased(Button b) {System.exit(0);}
			
			@Override
			public void buttonPressed(Button b) {}
		});
	}

}
