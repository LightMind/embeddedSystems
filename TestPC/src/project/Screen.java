package project;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

public class Screen extends BasicGame {
	Connection con;
	float xOffset = 100;
	float yOffset = 100;
	int index = 0;

	public Screen(String title, Connection t) {
		super(title);
		con = t;
	}

	@Override
	public void render(GameContainer arg0, Graphics g) throws SlickException {
		g.setColor(Color.white);
		g.drawString("Index: " + index, 20, 20);

		g.setColor(Color.red);
		float cx = con.x / 3f + xOffset;
		float cy = arg0.getScreenHeight() / 3f - (con.y / 3f);

		g.fillOval(cx - 2, cy - 2, 8, 8);

		for (LocationData d : con.getPoints()) {
			float scx = (d.x / 3f) + xOffset;
			float scy = arg0.getScreenHeight() / 3f - (d.y / 3f);
			g.drawOval(scx, scy, 5, 5);

			g.setColor(Color.white);
			int dir = d.dir;
			if ((dir & 1) == 1) {
				g.drawLine(scx, scy, scx + 10, scy);
			}
			if ((dir & 2) == 2) {
				g.drawLine(scx, scy, scx, scy - 10);
			}
			if ((dir & 4) == 4) {
				g.drawLine(scx, scy, scx - 10, scy);
			}
			if ((dir & 8) == 8) {
				g.drawLine(scx, scy, scx, scy + 10);
			}
			g.setColor(Color.cyan);
			g.drawString(" " + d.id, scx, scy);

		}
		g.setColor(Color.green);
		for (Edge e : con.getEdge()) {
			float x1 = e.x1 / 3f + xOffset;
			float y1 = arg0.getScreenHeight() / 3f - (e.y1 / 3f);
			float x2 = e.x2 / 3f + xOffset;
			float y2 = arg0.getScreenHeight() / 3f - (e.y2 / 3f);

			g.drawLine(x1, y1, x2, y2);
		}

	}

	@Override
	public void init(GameContainer arg0) throws SlickException {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(GameContainer gc, int arg1) throws SlickException {
		Input input = gc.getInput();

		if (input.isKeyPressed(Keyboard.KEY_RETURN)) {
			con.sendInteger(index);
		}

		if (input.isKeyPressed(Keyboard.KEY_LEFT)) {
			if (index > 0) {
				index--;
			}
		}
		if (input.isKeyPressed(Keyboard.KEY_RIGHT)) {
			index++;
		}

	}

}
