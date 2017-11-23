package Practice;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import Practice.Listeners.*;

import static Practice.Item.ITEM_COUNT;
import static Practice.MenuGameConstants.*;

// TODO: https://github.com/snes9xgit/snes9x
public class MenuGame extends Container {
	private static final long serialVersionUID = -4474643068621537992L;

	// local vars
	private ItemSlot[] list = new ItemSlot[20];
	private ItemSlot[] listAtTurn;
	private int target;
	private int loc;
	private ArrayList<Integer> pickFrom;

	private ScoreCard ref;

	// gameplay
	final GameMode mode; // current game mode
	final Difficulty dif; // current difficulty
	int currentTurn; // current turn, based on difficulty
	int currentRound;
	final int maxTurn;
	final int maxRound;
	int scoreForGame = 0;
	ArrayList<PlayerMovement> movesMade = new ArrayList<PlayerMovement>();
	PlayerMovement[] bestMoves;

	final boolean randoAllStarts;
	final boolean showOpt;
	BufferedImage minMoveOverlay;

	ItemLister chosen; // list of chosen items
	final Timer waiter = new Timer();
	boolean studying = false;

	final Controller controls;
	final int KEY_UP;
	final int KEY_DOWN;
	final int KEY_RIGHT;
	final int KEY_LEFT;
	final int KEY_START;

	// end gameplay

	public MenuGame(Controller controls, GameMode gameMode, Difficulty difficulty, int rounds) {
		initialize();
		mode = gameMode;
		dif = difficulty;
		maxTurn = dif.roundLength(mode);
		currentRound = dif.roundCount(rounds, mode);
		maxRound = currentRound;
		currentTurn = maxTurn;
		randoAllStarts = dif.randomizesStart(mode);
		showOpt = dif.showOptimalPath;
		this.controls = controls;
		KEY_UP = controls.T_UP;
		KEY_DOWN = controls.T_DOWN;
		KEY_RIGHT = controls.T_RIGHT;
		KEY_LEFT = controls.T_LEFT;
		KEY_START = controls.T_START;
		addKeys();
	}

	private void makeNewCard() {
		int min = calcMinMoves();
		ref = new ScoreCard(dif, min, bestMoves, listAtTurn);
	}

	public void start() {
		randomizeMenu();
		if (mode == GameMode.STUDY) {
			holdOn();
		} else {
			randomizeGoal();
			fireTurnEvent(null);
			makeNewCard();
		}
	}

	public void holdOn() {
		ref = null;
		studying = true;
		fireTurnEvent(null);
		waiter.schedule(new OpTask(
			() -> {
					randomizeGoal();
					studying = false;
					fireTurnEvent(null);
					makeNewCard();
				}),
			dif.studyTime);
	}

	private final void initialize() {
		this.setPreferredSize(MENU_SIZE);
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
				if (ref == null) { // don't do anything unless we have a scoring object
					return;
				}
				int key = arg0.getExtendedKeyCode();
				// up
				if (key == KEY_UP) {
					movesMade.add(new PlayerMovement(loc, MOVE_UP));
					loc = moveUp(loc);
					ref.moves++;
					fireInputEvent(InputEvent.SNES_UP);
				// down
				} else if (key == KEY_DOWN) {
					movesMade.add(new PlayerMovement(loc, MOVE_DOWN));
					loc = moveDown(loc);
					ref.moves++;
					fireInputEvent(InputEvent.SNES_DOWN);
				// right
				} else if (key == KEY_RIGHT) {
					movesMade.add(new PlayerMovement(loc, MOVE_RIGHT));
					loc = moveRight(loc);
					ref.moves++;
					fireInputEvent(InputEvent.SNES_RIGHT);
				// left
				} else if (key == KEY_LEFT) {
					movesMade.add(new PlayerMovement(loc, MOVE_LEFT));
					loc = moveLeft(loc);
					ref.moves++;
					fireInputEvent(InputEvent.SNES_LEFT);
				// start TODO : remove space when controls are added properly
				} else if (key == KEY_START || key == KeyEvent.VK_SPACE) {
					movesMade.add(new PlayerMovement(loc, PRESS_START));
					pressStart();
					fireInputEvent(InputEvent.SNES_START);
				}
			}
		});
	}

	private void pressStart() {
		ref.startPresses++;
		if (target == loc) {
			nextTurn();
		} else {

		}
	}

	private void nextTurn() {
		currentTurn--;
		if (currentTurn == 0) {
			nextRound();
			return;
		}
		newTurn();
	}

	private void newTurn() {
		randomizeGoal();
		ref.setPlayerPath(movesMade.toArray(new PlayerMovement[movesMade.size()]));
		movesMade.clear();
		ScoreCard prevRef = ref;
		fireTurnEvent(prevRef);
		makeNewCard();
	}

	private void nextRound() {
		currentRound--;
		if (currentRound == 0) {
			fireTurnEvent(ref);
			fireGameOverEvent();
			return;
		}
		currentTurn = maxTurn;
		switch (mode) {
			case STUDY :
				randomizeMenu();
				holdOn();
				break;
			case BLITZ :
				randomizeMenu();
				newTurn();
				break;
			case COLLECT :
				addToMenu();
				newTurn();
				break;
		}
	}

	public int getScore() {
		return scoreForGame;
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

	private void randomizeMenu() {
		chosen = new ItemLister();
		listAtTurn = new ItemSlot[20];

		switch (mode) {
			case STUDY :
				currentTurn = dif.studyRoundLength;
				break;
			case COLLECT :
				currentTurn = dif.collectionRoundLength;
				break;
			default :
				currentTurn = 1;
				break;
		}

		// we need at least this many items
		final int itemsWanted;

		switch (mode) {
			case STUDY :
			case BLITZ :
			default :
				itemsWanted = (int) (Math.random() * 17); // choose between 0 and 16 items to add
				chosen.addRandomItems(itemsWanted);
				break;
			case COLLECT :
				// do nothing
				break;
		}

		// add items to lists
		pickFrom = new ArrayList<Integer>();

		for (int i = 0; i < ITEM_COUNT; i++) {
			if (chosen.get(i)) {
				list[i].setRandomItem();
				list[i].setEnabled(true);
				listAtTurn[i] = list[i].clone();
				pickFrom.add(i);
			} else {
				list[i].setEnabled(false);
			}
		}
	}

	/**
	 * Adds a single item to the menu
	 */
	private void addToMenu() {
		int i = chosen.addOneItem();
		if (i == -1) {
			randomizeMenu();
			randomizeGoal();
			return;
		}
		list[i].setRandomItem();
		list[i].setEnabled(true);
		listAtTurn[i] = list[i].clone();
		pickFrom.add(i);
	}

	private void randomizeGoal() {
		int randomIndex;
		if (
				randoAllStarts // check to see if we're changing start location each time
				// also randomize on the first turn
				|| (
					(currentTurn == maxTurn) &&
						(
						// when we're on the first turn of the first round, which should always randomize the goal
							(currentRound == maxRound) ||
						// or the first turn of a round not in collections mode
							(mode != GameMode.COLLECT) )
						)
			) {
			randomIndex = (int) (Math.random() * pickFrom.size());
			loc = pickFrom.get(randomIndex);
		}

		do {
			randomIndex = (int) (Math.random() * pickFrom.size());
			target = pickFrom.get(randomIndex);
		} while (target == loc);
	}

	public String getTarget() {
		if (studying) {
			return "Study the menu";
		}
		return list[target].getCurrentItem();
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(ZOOM, ZOOM);

		g2.drawImage(BACKGROUND, 0, 0, null);
		paintComponents(g2);

		if (!studying) {
			ItemPoint cursorLoc = ItemPoint.valueOf("SLOT_" + loc);
			g2.drawImage(CURSOR,
					ITEM_ORIGIN_X + cursorLoc.x - CURSOR_OFFSET,
					ITEM_ORIGIN_Y + cursorLoc.y - CURSOR_OFFSET,
					null);

			if (dif.showTargetCursor) {
				cursorLoc = ItemPoint.valueOf("SLOT_" + target);
				g2.drawImage(TARGET_CURSOR,
						ITEM_ORIGIN_X + cursorLoc.x - CURSOR_OFFSET,
						ITEM_ORIGIN_Y + cursorLoc.y - CURSOR_OFFSET,
						null);
			}
			if (showOpt) {
				g2.drawImage(minMoveOverlay, 0, 0, null);
			}
		}
	}

	private int calcMinMoves() {
		int[] arrowPlacement = new int[1];
		int moves = -1; // default to return for failure, just in case
		int goodPattern = -1;
		for (int pattern : ALL_POSSIBLE_MOVES) {
			int pos;
			moves = pattern >> COUNT_OFFSET;
			pos = loc;
			int newPos = pos;
			arrowPlacement = new int[moves];

			for (int i = 0; i < moves; i++) {
				int moveToken = (pattern >> (i * 2)) & 0b11;
				arrowPlacement[i] = newPos; // add to list of positions
				switch (moveToken) {
					case MOVE_UP :
						newPos = moveUp(pos);
						break;
					case MOVE_DOWN :
						newPos = moveDown(pos);
						break;
					case MOVE_RIGHT :
						newPos = moveRight(pos);
						break;
					case MOVE_LEFT :
						newPos = moveLeft(pos);
						break;
					default :
						newPos = pos;
						break;
				} // end switch
				pos = newPos;
			} // end moves for

			if (pos == target) {
				goodPattern = pattern;
				break;
			}
		}

		// make the best moves pattern
		if (goodPattern != -1) {
			bestMoves = new PlayerMovement[moves+1];
			for (int i = 0; i < moves; i++) {
				int moveToken = (goodPattern >> (i * 2)) & 0b11;
				bestMoves[i] = new PlayerMovement(arrowPlacement[i], moveToken);
			}
			bestMoves[moves] = new PlayerMovement(target, PRESS_START);
		}

		// make a single image of the optimal path, to be overlayed
		if (showOpt) {
			minMoveOverlay = PlayerMovement.drawOptimalPath(bestMoves, false);
		}
		// failure, just in case
		return moves;
	}

	/*
	 * Events for turn changes
	 */
	private List<TurnListener> turnListen = new ArrayList<TurnListener>();
	public synchronized void addTurnListener(TurnListener s) {
		turnListen.add(s);
	}

	private synchronized void fireTurnEvent(ScoreCard ref) {
		if (ref != null) {
			scoreForGame += ref.calcScore();
		}
		TurnEvent te = new TurnEvent(this, ref);
		Iterator<TurnListener> listening = turnListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	// for the first initialization
	synchronized void refresh() {
		TurnEvent te = new TurnEvent(this, null);
		Iterator<TurnListener> listening = turnListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	/*
	 * Events for snes inputs
	 */
	private List<InputListener> snesListen = new ArrayList<InputListener>();
	public synchronized void addInputListener(InputListener s) {
		snesListen.add(s);
	}

	private synchronized void fireInputEvent(int button) {
		InputEvent te = new InputEvent(this, button);
		Iterator<InputListener> listening = snesListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	/*
	 * Events for being done
	 */
	private List<GameOverListener> doneListen = new ArrayList<GameOverListener>();
	public synchronized void addGameOverListener(GameOverListener s) {
		doneListen.add(s);
	}

	private synchronized void fireGameOverEvent() {
		GameOverEvent te = new GameOverEvent(this);
		Iterator<GameOverListener> listening = doneListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	static class ItemLister {
		final boolean[] list = new boolean[20];
		int count = 0;
		int itemsChosen = 0;
		static final int C_SIZE = ITEM_CHOOSER.size();
		public ItemLister() {
			addRandomItems(4);
		}

		/**
		 * Adds items
		 * @param x
		 * @return Number of items actually added; will be less on overflows
		 */
		public int addRandomItems(int x) {
			int itemsWanted = itemsChosen + x;
			int i = 0;
			while (itemsChosen != itemsWanted) {
				if (itemsChosen == 20) {
					break;
				}
				addOneItem();
				i++;
			}
			return i;
		}

		/**
		 * Adds 1 item
		 * @return Index of item added
		 */
		public int addOneItem() {
			if (itemsChosen == 20) {
				return -1;
			}

			int rand;
			int toAdd;
			do {
				rand = (int) (Math.random() * C_SIZE);
				toAdd = ITEM_CHOOSER.get(rand);				
			} while (list[toAdd] == true);

			list[toAdd] = true;
			itemsChosen++;
			return toAdd;
		}
		public boolean get(int x) {
			return list[x];
		}
	}
}