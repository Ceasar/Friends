package cjb;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Main extends Canvas implements Serializable{

	private static final long serialVersionUID = -419365737093637821L;
	private transient BufferStrategy strategy;
	private boolean gameRunning = true;
	ArrayList<Entity> entities = new ArrayList<Entity>();
	ArrayList<Updatable> updatables = new ArrayList<Updatable>();
	ArrayList<Drawable> drawables = new ArrayList<Drawable>();
	int midx = 0; int midy = 0;
	int pointerX = 0; int pointerY = 0;
	int refX = 0; int refY = 0;
	transient Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
	protected Dimension screensize = toolkit.getScreenSize();
	boolean buildMode = true;
	boolean connecting = false;
	boolean dragging = false;
	boolean scrolling = false;
	Entity selected = null;
	double scale = 1.0;
	long lastClick = 0;
	String filename;
	JFrame container;
	JPanel panel;

	public Main() {
		init();
	}
	
	public void init(){
		container = new JFrame("MyFriends");

		panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(screensize);
		setBounds(0,0,screensize.width,screensize.height);
		panel.setLayout(null);
		panel.add(this);

		setIgnoreRepaint(true);

		container.pack();
		container.setResizable(true);
		container.setVisible(true);

		container.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		addMouseListener(new MouseInputHandler(this));
		addMouseMotionListener(new MouseMotionHandler());
		addKeyListener(new KeyInputHandler(this));
		addMouseWheelListener(new MouseWheelHandler());

		requestFocus();

		createBufferStrategy(2);
		strategy = getBufferStrategy();
		midx = screensize.width/2;
		midy = screensize.height/2;
	}

	public void gameLoop() {
		Font f = new Font("Lucida Sans", Font.BOLD, 18);
		while (gameRunning) {
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setFont(f);
			g.setColor(Color.white);
			g.fillRect(0,0,screensize.width,screensize.height);
			g.scale(scale, scale);

			g.setColor(Color.black);
			if (selected != null){
				if (connecting){
					g.drawLine((int)selected.x, (int)selected.y, (int)(pointerX/scale), (int)(pointerY/scale));
				}
				if (dragging){
					selected.x = pointerX/scale; selected.y = pointerY/scale; selected.dx = selected.dy = 0;
				}
			}
			if (scrolling){
				for (int i=0;i<entities.size();i++) {entities.get(i).x -= refX - pointerX; entities.get(i).y -= refY - pointerY;}
				refX = pointerX; refY = pointerY;
			}
			//for (int i=0;i<updatables.size();i++) {if (!buildMode) updatables.get(i).update();}
			//for (int i=0;i<drawables.size();i++) {drawables.get(i).draw(g);}
			for (int i=0;i<entities.size();i++) {entities.get(i).update(); entities.get(i).draw(g);}

			g.setColor(Color.black);
			g.scale(1 / scale, 1 / scale);
			if (filename != null) g.drawString(filename, 10, 30);
//			if (damping) g.drawString("damping", 10, 30); else g.drawString("no damping", 10, 30);
//			if (connecting) g.drawString("connecting", 10, 90); else g.drawString("not connecting", 10, 90);
			g.dispose();
			if (gameRunning) strategy.show();

			try { Thread.sleep(15); } catch (Exception e) {}
		}
	}


	public static void main(String args[]) {
		String filename = "MyContacts.data";
		if(args.length > 0){
			filename = args[0];
		}
		Main time = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			time = (Main)in.readObject();
			in.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		if (time == null) {Main m =new Main(); m.filename = filename; m.gameLoop();}
		else {
			time.init();
			time.filename = filename;
			time.gameLoop();
		}
//		Main m =new Main(); m.gameLoop();
	}
	
//	public void load(Main main){
//		//gameRunning = false;
//		//m.strategy = null;
//		//container.dispose();
////		removeMouseListener(getMouseListeners()[0]);
////		removeMouseMotionListener(getMouseMotionListeners()[0]);
////		removeMouseWheelListener(getMouseWheelListeners()[0]);
////		removeKeyListener(getKeyListeners()[0]);
//		main.filename = filename;
//		filename = "nothing";
//		
//		main.init();
//		main.gameLoop();
//		
//	}


	private class MouseInputHandler extends MouseAdapter{
		Main m;
		public MouseInputHandler(Main m){
			this.m = m;
		}

		public void mousePressed(MouseEvent e){
			for (int i=0;i<entities.size();i++) {
				Entity entity = entities.get(i);
				double x = pointerX/scale - entity.x; double y = pointerY/scale - entity.y;
				double distance = Math.sqrt(x*x+y*y);
				if (distance < entity.radius){
					if (connecting && selected != null && entity instanceof Friend && selected instanceof Friend){
						Friend a = (Friend) selected; Friend b = (Friend) entity;
						a.friends.add(b); b.friends.add(a);
						Edge edge = new Edge(a, b, m);
						entities.add(edge); updatables.add(edge); drawables.add(edge);
						a.edges.add(edge); b.edges.add(edge);
						Collections.sort(entities);
					}
					else{
						dragging = true;
						if (selected != null){selected.selected = false; selected = null;}
						selected = entity;
						entity.selected = true;
					}
					return;
				}
			}
			if (System.currentTimeMillis() - lastClick < 200){
				Friend friend = new Friend(m,(int)(pointerX/scale),(int)(pointerY/scale));
				entities.add(friend); updatables.add(friend); drawables.add(friend);
				Collections.sort(entities);
			}
			else{
				//if (selected != null){selected.selected = false; selected = null;}
				scrolling = true; refX = e.getX(); refY = e.getY();
			}
			lastClick = System.currentTimeMillis();
		}

		public void mouseReleased(MouseEvent e){
			scrolling = false;
			dragging = false;
		}
		
	}
	private class MouseMotionHandler extends MouseMotionAdapter{
		public void mouseMoved(MouseEvent e){
			pointerX = e.getX(); pointerY = e.getY();
		}

		public void mouseDragged(MouseEvent e){
			pointerX = e.getX(); pointerY = e.getY();
		}
	}

	private class MouseWheelHandler implements MouseWheelListener{
		public void mouseWheelMoved(MouseWheelEvent e){
			if (scale - e.getWheelRotation() * .1 > 0) scale -= e.getWheelRotation() * .1;
		}
	}

	class KeyInputHandler extends KeyAdapter{
		Main m;

		public KeyInputHandler(Main m){
			this.m = m;
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_C) {connecting = !connecting;}
			if (e.getKeyCode() == KeyEvent.VK_D) {
				if (selected != null){
					if (selected.name != null){
						selected.description = (String)JOptionPane.showInputDialog(m,"Describe " + selected.name + "!","Description",JOptionPane.INFORMATION_MESSAGE,null,null,selected.description);
					}
					else{
						selected.description = (String)JOptionPane.showInputDialog(m,"Description:","Description",JOptionPane.INFORMATION_MESSAGE,null,null,selected.description);
					}
				}

			}
//			if (e.getKeyCode() == KeyEvent.VK_L) {
//				//Destroy current m
//				filename = (String)JOptionPane.showInputDialog(m,"Name of File","Load",JOptionPane.PLAIN_MESSAGE,null,null,null);
//				Main time = null;
//				FileInputStream fis = null;
//				ObjectInputStream in = null;
//				try{
//					fis = new FileInputStream(filename);
//					in = new ObjectInputStream(fis);
//					time = (Main)in.readObject();
//					in.close();
//				}
//				catch(IOException ex){
//					ex.printStackTrace();
//				}
//				catch(Exception ex){
//					ex.printStackTrace();
//				}
//				if (time == null) {}
//				else {
//					m.load(time);
//				}
//			}
			if (e.getKeyCode() == KeyEvent.VK_N) {
				if (selected != null){
					selected.name = (String)JOptionPane.showInputDialog(m,"Input Name","Name",JOptionPane.PLAIN_MESSAGE,null,null,selected.name);
				}

			}
			if (e.getKeyCode() == KeyEvent.VK_P) {buildMode = !buildMode;}
			if (e.getKeyCode() == KeyEvent.VK_Q) {
				if (selected != null){
					selected.solid = !selected.solid;
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_R) {
				if (selected != null){
					selected.radius = Integer.parseInt((String)JOptionPane.showInputDialog(m,"Radius","Radius",JOptionPane.PLAIN_MESSAGE,null,null,selected.radius));
				}

			}
			if (e.getKeyCode() == KeyEvent.VK_S) {
				if (filename != null){
					FileOutputStream fos = null;
					ObjectOutputStream out = null;
					try{
						fos = new FileOutputStream(filename);
						out = new ObjectOutputStream(fos);
						out.writeObject(m);
						out.close();
					}
					catch(IOException ex){
						ex.printStackTrace();
					}
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_X) {
				if (selected != null){
					updatables.remove(selected);
					drawables.remove(selected);
					entities.remove(selected);
				}
				if (selected instanceof Friend){
					Friend friend = (Friend) selected;
					Iterator<Edge> iter = friend.edges.iterator();
					while (iter.hasNext()){
						Edge edge = iter.next();
						updatables.remove(edge);
						drawables.remove(edge);
						entities.remove(edge);
					}
				}
				
			}

//			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//				entities.clear();
//				updatables.clear();
//				drawables.clear();
//				buildMode = true;
//			}
		} 

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}


}