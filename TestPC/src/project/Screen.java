package project;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

public class Screen extends BasicGame {
	Connection con;

	public Screen(String title, Connection t) {
		super(title);
		con = t;
	}

	@Override
	public void render(GameContainer arg0, Graphics g) throws SlickException {
		g.setColor(Color.green);
		for(LocationData d : con.getPoints()){
			float scx = (d.x / 3f) + arg0.getScreenWidth()/3f;
			float scy = arg0.getScreenHeight()/3f - (d.y / 3f);
			g.drawOval(scx, scy, 5, 5);
		}

	}

	@Override
	public void init(GameContainer arg0) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(GameContainer arg0, int arg1) throws SlickException {
		// TODO Auto-generated method stub

	}

}
