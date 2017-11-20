package Practice;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import static Practice.Item.ITEM_COUNT;

public class MenuGame extends Container {
	private static final long serialVersionUID = 1L;

	// location within the menu image to start for calculating
	static final int ITEM_ORIGIN_X = 24;
	static final int ITEM_ORIGIN_Y = 16;

	static final int CURSOR_OFFSET = 8; // number of pixels to shift the cursor
	static final int BLOCK_SIZE = 24; // size in pixels of an item block (for offsets, not drawing)
	static final int ITEM_SIZE = 16; // size of the image itself
	static final Dimension BLOCK_D = new Dimension(BLOCK_SIZE, BLOCK_SIZE);

	static final Item[] ALL_ITEMS = Item.values(); // for easy access
	static final int MIN_ITEMS = 4;

	static final BufferedImage BACKGROUND;
	static final BufferedImage CURSOR;
	static final int BG_WIDTH = 152;
	static final int BG_HEIGHT = 120;

	static {
		BufferedImage temp;
		try {
			temp = ImageIO.read(MenuGame.class.getResourceAsStream("/Practice/images/menu background.png"));
		} catch (Exception e) {
			temp = new BufferedImage(BG_WIDTH, BG_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
		}
		BACKGROUND = temp;

		try {
			temp = ImageIO.read(MenuGame.class.getResourceAsStream("/Practice/images/menu cursor.png"));
		} catch (Exception e) {
			temp = new BufferedImage(32, 32, BufferedImage.TYPE_4BYTE_ABGR);
		}
		CURSOR = temp;
	}

	// local vars
	private ItemSlot[] list = new ItemSlot[20];
	private int target;
	private int loc;

	private ScoreCard ref;

	// draw size
	private int zoom = 2;

	public MenuGame() {
		initialize();
		addKeys();
		randomize();
	}

	private final void initialize() {
		this.setLayout(null);
		for (int i = 0; i < ITEM_COUNT; i++) {
			ItemSlot temp = new ItemSlot(ALL_ITEMS[i]);
			list[i] = temp;
			int r = i / 5;
			int c = i % 5;

			// add to container
			temp.setBounds(ITEM_ORIGIN_X + (c * BLOCK_SIZE),
					ITEM_ORIGIN_Y + (r * BLOCK_SIZE),
					ITEM_SIZE, ITEM_SIZE);
			this.add(temp);
		}
	}

	private final void addKeys() {
		this.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent arg0) {}
			public void keyReleased(KeyEvent arg0) {}

			public void keyPressed(KeyEvent arg0) {
				switch (arg0.getExtendedKeyCode() ) {
					case KeyEvent.VK_UP :
						loc = moveUp(loc);
						ref.moves++;
						fireInputEvent(InputEvent.SNES_UP);
						break;
					case KeyEvent.VK_DOWN :
						loc = moveDown(loc);
						ref.moves++;
						fireInputEvent(InputEvent.SNES_DOWN);
						break;
					case KeyEvent.VK_RIGHT :
						loc = moveRight(loc);
						ref.moves++;
						fireInputEvent(InputEvent.SNES_RIGHT);
						break;
					case KeyEvent.VK_LEFT :
						loc = moveLeft(loc);
						ref.moves++;
						fireInputEvent(InputEvent.SNES_LEFT);
						break;
					case KeyEvent.VK_D :
						pressStart();
						fireInputEvent(InputEvent.SNES_START);
						break;
				}
				repaint();
			}
			
		});
	}

	private void pressStart() {
		ref.startPresses++;
		if (target == loc) {
			//win();
			randomize();
		} else {
			
		}
	}

	/*
	 * Movement
	 */
	private int moveUp(int s) {
		int newLoc = (s + 15) % 20;
		if (!list[newLoc].isEnabled()) {
			return moveUp(newLoc);
		}
		return newLoc;
	}

	private int moveDown(int s) {
		int newLoc = (s + 5) % 20;
		if (!list[newLoc].isEnabled()) {
			return moveDown(newLoc);
		}
		return newLoc;
	}

	private int moveRight(int s) {
		int newLoc = (s + 1) % 20;
		if (!list[newLoc].isEnabled()) {
			return moveRight(newLoc);
		}
		return newLoc;
	}

	private int moveLeft(int s) {
		int newLoc = (s + 19) % 20;
		if (!list[newLoc].isEnabled()) {
			return moveLeft(newLoc);
		}
		return newLoc;
	}

	/**
	 * Forumala used to determine if an item is on
	 * @return
	 */
	private boolean chooseOn() {
		int x = (int) (Math.random() * 3);
		return x == 0;
	}

	private void randomize() {
		boolean[] chosen = new boolean[20];

		// we need at least this many items
		int itemsChosen = 0;
		boolean notEnoughItems = true;

		// choose slots to use
		while (notEnoughItems) {
			for (int i = 0; i < ITEM_COUNT; i++) {
				if (chosen[i]) {
					continue;
				}

				if (chooseOn()) {
					chosen[i] = true;
					itemsChosen++;

					if (itemsChosen >= MIN_ITEMS) {
						notEnoughItems = false;
					}
				}
			} // end for
		} // end while

		// add items to lists
		ArrayList<Integer> pickFrom = new ArrayList<Integer>();

		for (int i = 0; i < ITEM_COUNT; i++) {
			if (chosen[i]) {
				list[i].setRandomItem();
				list[i].setEnabled(true);
				pickFrom.add(i);
			} else {
				list[i].setEnabled(false);
			}
		}

		// choose begin item and target item
		int randomIndex = (int) (Math.random() * pickFrom.size());
		loc = pickFrom.remove(randomIndex);

		randomIndex = (int) (Math.random() * pickFrom.size());
		target = pickFrom.remove(randomIndex);

		ScoreCard prevRef = ref;

		ref = new ScoreCard();

		fireTurnEvent(prevRef);
		repaint();
		this.requestFocus();
	}

	public ItemSlot getTarget() {
		return list[target];
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(zoom, zoom);
		g2.drawImage(BACKGROUND, 0, 0, null);
		this.paintComponents(g2);
		ItemPoint cursorLoc = ItemPoint.valueOf("SLOT_" + loc);
		g2.drawImage(CURSOR,
				ITEM_ORIGIN_X + cursorLoc.x - CURSOR_OFFSET,
				ITEM_ORIGIN_Y + cursorLoc.y - CURSOR_OFFSET,
				null);
	}

	private int calcMinMoves() {
		return 3;
	}

	/*
	 * Events for turn changes
	 */
	private List<TurnListener> turnListen = new ArrayList<TurnListener>();
	public synchronized void addTurnListener(TurnListener s) {
		turnListen.add(s);
	}

	private synchronized void fireTurnEvent(ScoreCard ref) {
		TurnEvent te = new TurnEvent(this, ref);
		Iterator<TurnListener> listening = turnListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	/*
	 * Events for snes inputs
	 */
	private List<InputListener> targListen = new ArrayList<InputListener>();
	public synchronized void addInputListener(InputListener s) {
		targListen.add(s);
	}

	private synchronized void fireInputEvent(int button) {
		InputEvent te = new InputEvent(this, button);
		Iterator<InputListener> listening = targListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	// main
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doTheGUI();
			}
		});
	}

	// GUI
	public static void doTheGUI() {
		// try to set LaF
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e2) {
				// do nothing
		} //end System

		// fast and long-lasting tooltips
		ToolTipManager.sharedInstance().setInitialDelay(100);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE); // 596:31:23.647

		final IntHolder turn = new IntHolder();
		final IntHolder totalScore = new IntHolder();

		// main window
		final Dimension d = new Dimension(700, 300);
		final Dimension d2 = new Dimension(300, 300);
		final JFrame frame = new JFrame("Menu Simulator 2K17");

		final Container wrap = frame.getContentPane();
		SpringLayout l = new SpringLayout();
		wrap.setLayout(l);

		// game
		MenuGame instance = new MenuGame();

		l.putConstraint(SpringLayout.WEST, instance, 5,
				SpringLayout.WEST, wrap);
		l.putConstraint(SpringLayout.NORTH, instance, 5,
				SpringLayout.NORTH, wrap);
		frame.add(instance);
		instance.setSize(d2);
		instance.setPreferredSize(d2);

		// target
		JLabel targ = new JLabel("--");
		targ.setFocusable(false);
		l.putConstraint(SpringLayout.WEST, targ, 5,
				SpringLayout.EAST, instance);
		l.putConstraint(SpringLayout.NORTH, targ, 5,
				SpringLayout.NORTH, instance);
		wrap.add(targ);

		// scores
		JTable scores = new JTable();
		DefaultTableModel model = new DefaultTableModel(
				new String[]{ "Turn", "Moves", "Time", "Wrong starts" },
				0);
		scores.setModel(model);
		JScrollPane scoreScroll = new JScrollPane(scores,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scores.setBackground(null);
		scores.setFocusable(false);
		l.putConstraint(SpringLayout.WEST, scoreScroll, -250,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.EAST, scoreScroll, -5,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.NORTH, scoreScroll, 5,
				SpringLayout.NORTH, wrap);
		l.putConstraint(SpringLayout.SOUTH, scoreScroll, -5,
				SpringLayout.SOUTH, wrap);
		wrap.add(scoreScroll);

		frame.setSize(d);
		frame.setMinimumSize(d);
		frame.setLocation(150, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				instance.dispatchEvent(e);
			}

			public void keyReleased(KeyEvent e) {
				instance.dispatchEvent(e);
			}

			public void keyTyped(KeyEvent e) {
				instance.dispatchEvent(e);
			}});

		instance.addTurnListener(
			arg0 -> {
					targ.setText(instance.getTarget().getCurrentItem());
					if (turn.val > 0) {
						ScoreCard tempRef = arg0.score;
						int tempScore = tempRef.finalScore;
						model.addRow(new Integer[] {
								turn.val,
								tempRef.moves,
								tempRef.finalTime,
								tempRef.startPresses - 1
						});
						totalScore.add(tempScore);
					}
					turn.increment();
			});

		instance.randomize();
		frame.setVisible(true);
	}

	private static enum ItemPoint {
		SLOT_0 (0),
		SLOT_1 (1),
		SLOT_2 (2),
		SLOT_3 (3),
		SLOT_4 (4),
		SLOT_5 (5),
		SLOT_6 (6),
		SLOT_7 (7),
		SLOT_8 (8),
		SLOT_9 (9),
		SLOT_10 (10),
		SLOT_11 (11),
		SLOT_12 (12),
		SLOT_13 (13),
		SLOT_14 (14),
		SLOT_15 (15),
		SLOT_16 (16),
		SLOT_17 (17),
		SLOT_18 (18),
		SLOT_19 (19);

		public final int x;
		public final int y;
		ItemPoint(int l) {
			x = (l % 5) * BLOCK_SIZE;
			y = (l / 5) * BLOCK_SIZE;
		}
	}

	public static class ScoreCard {
		int startPresses;
		int moves;
		final long startTime;
		long endTime;
		int finalScore;
		int finalTime;

		public ScoreCard() {
			startPresses = 0;
			moves = 0;
			startTime = System.nanoTime();
		}

		int calcScore(int minMoves) {
			// score for how long it took
			endTime = System.nanoTime(); // calculate end time on score request
			long timeDiff = endTime - startTime;
			int finalTime = (int) (timeDiff / 1000);
			int timeScore = (2000 - finalTime);

			// difference between moves made and optimal moves
			int diffScore = ( 500 * ( (minMoves + 1) - moves) );
			int moveBonus = (moves == minMoves) ? 1000 : 0; // bonus for being optimal

			// penalty for pressing start on the wrong item
			int startPenalty = 0;
			if (startPresses > 1) {
				startPenalty = startPresses * 500;
			}
			finalScore = 0 + diffScore + moveBonus - startPenalty;
			return finalScore;
		}
	}

	static class IntHolder {
		int val;

		IntHolder() {
			val = 0;
		}

		void add(int a) {
			val += a;
		}

		void increment() {
			val++;
		}
	}
}