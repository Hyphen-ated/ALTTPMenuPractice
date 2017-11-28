package Practice.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import Practice.ScoreCard;
import Practice.Controls.ControlMapper;
import Practice.Controls.ControllerHandler;
import Practice.Controls.SNESControllable;
import Practice.Listeners.SNESInputEvent;
import Practice.Listeners.SNESInputListener;

import static Practice.MenuGameConstants.*;

public class MenuPractice implements SNESControllable {
	static final String VERSION = "v0.9-beta";

	static final Dimension D = new Dimension((BG_WIDTH + 5) * ZOOM, (BG_HEIGHT + (24 * 5)) * ZOOM);
	static final Dimension CHART_D = new Dimension(450, 500);
	static final Font CONSOLAS = new Font("Consolas", Font.PLAIN, 12);

	static final DefaultTableCellRenderer PLAIN_TABLE = new ScoreTableRenderer(false);
	static final ScoreTableRenderer SCORE_TABLE = new ScoreTableRenderer(true);

	static final String HOW_TO_PLAY;
	static final String DATA_PATH = "/Practice/GUI/howtoplay.html";

	static {
		StringBuilder ret = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							MenuPractice.class.getResourceAsStream(DATA_PATH),
							StandardCharsets.UTF_8)
					);
			String line;
			while ((line = br.readLine()) != null) {
				ret.append(line);
			}
			br.close();
		} catch (Exception e) {
			ret.append("OOPS");
		}
		HOW_TO_PLAY = ret.toString();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// try to get the controller environment
					// if this fails, it means the user is missing some files,
					// so tell the user to add those files there
					@SuppressWarnings("unused")
					Object o = ControlMapper.defaultController;
				} catch (ExceptionInInitializerError e) {
					MenuPractice.showWarning();
					return;
				}
				new MenuPractice().doTheGUI();
				//MenuPractice.showWarning();
			}
		});
	}

	// GUI
	public void doTheGUI() {
		// try to set LaF
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e2) {
				// do nothing
		} //end System

		final IntHolder turn = new IntHolder();
		turn.increment();
		final IntHolder totalScore = new IntHolder();

		// main window
		final JFrame frame = new JFrame("Menu Simulator 2K17 " + VERSION);

		Container wrap = frame.getContentPane();
		SpringLayout l = new SpringLayout();
		wrap.setLayout(l);
		wrap.setBackground(Color.BLACK);
		frame.setBackground(Color.BLACK);

		// a little thing for dialogs to hook onto for display
		// intentionally constrained to be 0px in size
		JPanel hooker = new JPanel();
		hooker.setBackground(null);

		l.putConstraint(SpringLayout.EAST, hooker, 0,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.NORTH, hooker, 0,
				SpringLayout.NORTH, wrap);
		l.putConstraint(SpringLayout.WEST, hooker, 0,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.SOUTH, hooker, 0,
				SpringLayout.NORTH, wrap);
		wrap.add(hooker);

		// controller
		ControllerHandler controls = ControlMapper.defaultController;
		// game
		GameContainer gamePlayer = new GameContainer();
		gamePlayer.setController(controls);

		l.putConstraint(SpringLayout.WEST, gamePlayer, 5,
				SpringLayout.WEST, wrap);
		l.putConstraint(SpringLayout.EAST, gamePlayer, -5,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.NORTH, gamePlayer, 5,
				SpringLayout.NORTH, wrap);
		l.putConstraint(SpringLayout.SOUTH, gamePlayer, 0,
				SpringLayout.SOUTH, wrap);
		wrap.add(gamePlayer);

		// scores
		JDialog scoreFrame = new JDialog(frame, "Performance scores");
		scoreFrame.setSize(CHART_D);
		scoreFrame.setMinimumSize(CHART_D);
		wrap = scoreFrame.getContentPane();
		l = new SpringLayout();
		wrap.setLayout(l);

		// table
		JTable scores = new JTable();
		ScoreTableModel model = new ScoreTableModel();
		scores.setModel(model);

		scores.setDefaultRenderer(Number.class, SCORE_TABLE);
		scores.setAutoCreateRowSorter(true);
		scores.setColumnSelectionAllowed(false);
		scores.setCellSelectionEnabled(false);
		scores.setRowSelectionAllowed(true);

		// scroll pane for score
		JScrollPane scoreScroll = new JScrollPane(scores,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scoreScroll.setFocusable(false);
		scoreScroll.setViewportBorder(null);
		scoreScroll.setBorder(null);
		scoreScroll.getViewport().setBorder(null);

		scores.setBorder(null);
		scores.setFont(CONSOLAS);
		scores.getTableHeader().setResizingAllowed(false);

		// hiscore
		JLabel scoreTotal = new JLabel("Total score:", SwingConstants.RIGHT);
		JLabel hiscore = new JLabel("0", SwingConstants.RIGHT);
		hiscore.setFont(CONSOLAS);
		scoreTotal.setFont(CONSOLAS);

		l.putConstraint(SpringLayout.WEST, scoreTotal, 0,
				SpringLayout.HORIZONTAL_CENTER, scoreScroll);
		l.putConstraint(SpringLayout.SOUTH, scoreTotal, -5,
				SpringLayout.SOUTH, wrap);
		wrap.add(scoreTotal);

		l.putConstraint(SpringLayout.EAST, hiscore, -10,
				SpringLayout.EAST, scoreScroll);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, hiscore, 0,
				SpringLayout.VERTICAL_CENTER, scoreTotal);
		wrap.add(hiscore);

		// clear data
		JButton clear = new JButton("Reset");
		clear.setFocusable(false);

		DialogTask clearData = (b) -> {
			hiscore.setText("0");
			totalScore.set(0);
			turn.set(1);
			model.clear();
		};

		clear.addActionListener(arg0 -> clearData.switchWindow(false));

		l.putConstraint(SpringLayout.EAST, clear, -10,
				SpringLayout.WEST, scoreTotal);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, clear, 0,
				SpringLayout.VERTICAL_CENTER, scoreTotal);
		wrap.add(clear);

		// analysis
		TurnAnalyzer analysis = new TurnAnalyzer(frame);
		JButton analyze = new JButton("Recap");

		DialogTask showAnalysis = (b) -> {
				int selectedRow = scores.getSelectedRow();
				if (selectedRow != -1) {
					analysis.setRef(model.getRow(selectedRow));
				}
				if (analysis.isVisible()) {
					if (b) {
						analysis.setVisible(false);
					}
				} else {
					if (scoreFrame.isVisible()) {
						analysis.setLocation(hiscore.getLocationOnScreen().x,
								scoreScroll.getLocationOnScreen().y);
					} else {
						analysis.setLocation(hooker.getLocationOnScreen());
					}
					analysis.setVisible(true);
				}
			};

		analyze.setFocusable(false);
		analyze.addActionListener(arg0 -> showAnalysis.switchWindow(false));

		ListSelectionModel scoreSel = scores.getSelectionModel();
		scoreSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scoreSel.addListSelectionListener(
			arg0 -> {
				if (!scoreFrame.isVisible()) {
					return;
				}
				int selectedRow = scores.getSelectedRow();
				if (analysis.isVisible()) {
					if (selectedRow == -1) {
						analysis.setVisible(false);
					} else {
						analysis.setRef(model.getRow(selectedRow));
					}
				}
			});

		l.putConstraint(SpringLayout.EAST, analyze, -10,
				SpringLayout.WEST, clear);
		l.putConstraint(SpringLayout.VERTICAL_CENTER, analyze, 0,
				SpringLayout.VERTICAL_CENTER, scoreTotal);
		wrap.add(analyze);

		// scores in wrap
		l.putConstraint(SpringLayout.WEST, scoreScroll, 10,
				SpringLayout.WEST, wrap);
		l.putConstraint(SpringLayout.EAST, scoreScroll, -5,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.NORTH, scoreScroll, 5,
				SpringLayout.NORTH, wrap);
		l.putConstraint(SpringLayout.SOUTH, scoreScroll, -5,
				SpringLayout.NORTH, scoreTotal);
		wrap.add(scoreScroll);

		// how to play
		Dimension helpD = new Dimension(400, 450);
		JDialog howPlayFrame = new JDialog(frame, "How to play");
		SpringLayout hhh = new SpringLayout();

		howPlayFrame.setLayout(hhh);
		howPlayFrame.setPreferredSize(helpD);
		howPlayFrame.setMinimumSize(helpD);
		Container howWrap = howPlayFrame.getContentPane();

		// HTML in java is dumb
		StyleSheet styleSheet = new StyleSheet();
		HTMLDocument htmlDocument;
		HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
		styleSheet.addRule(HOW_TO_PLAY);

		htmlEditorKit.setStyleSheet(styleSheet);
		htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
		JTextPane helpPane = new JTextPane();

		helpPane.setBackground(null);
		helpPane.setEditorKit(htmlEditorKit);
		helpPane.setDocument(htmlDocument);
		helpPane.setEditable(false);

		try {
			htmlDocument.insertBeforeEnd(htmlDocument.getRootElements()[0].getElement(0), HOW_TO_PLAY);
		} catch (Exception e) {}

		// actual display
		JScrollPane helpScroll = new JScrollPane(helpPane,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		hhh.putConstraint(SpringLayout.WEST, helpScroll, 0,
				SpringLayout.WEST, howWrap);
		hhh.putConstraint(SpringLayout.EAST, helpScroll, 0,
				SpringLayout.EAST, howWrap);
		hhh.putConstraint(SpringLayout.NORTH, helpScroll, 0,
				SpringLayout.NORTH, howWrap);
		hhh.putConstraint(SpringLayout.SOUTH, helpScroll, 0,
				SpringLayout.SOUTH, howWrap);
		howWrap.add(helpScroll);

		// input config
		ControlMapper remap = new ControlMapper(frame);

		// credits
		Dimension creditsD = new Dimension((CREDITS.getWidth() + 2) * ZOOM, (CREDITS.getHeight() + 15) * ZOOM);
		final JDialog aboutFrame = new JDialog(frame, "About");
		aboutFrame.setPreferredSize(creditsD);
		aboutFrame.setMinimumSize(creditsD);
		aboutFrame.setResizable(false);
		aboutFrame.getContentPane().setBackground(Color.BLACK);

		int pos = 0;
		Graphics credits = CREDITS.getGraphics();
		drawSmall(credits, makeWordImageSmall("ABOUT"), 2, pos++);
		drawSmall(credits, makeWordImageSmall("MENU SIM " + VERSION), 1, pos++);

		pos++;
		drawSmall(credits, makeWordImageSmall("ACKNOWLEDGEMENTS:"), 1, pos++);

		pos++;
		drawSmall(credits, makeWordImageSmall("WRITTEN BY:"), 2, pos++);
		drawSmall(credits, makeWordImageSmall("FATMANSPANDA"), 4, pos++);

		pos++;
		drawSmall(credits, makeWordImageSmall("RESOURCES:"), 2, pos++);
		drawSmall(credits, makeWordImageSmall("ZARBY89"), 4, pos++);

		pos++;
		drawSmall(credits, makeWordImageSmall("TESTING = FEEDBACK:"), 2, pos++);
		String[] testers = new String[] {
				"Candide",
				"Harb",
				"IHNN"
		};

		for (int i = 0; i < testers.length; i++) {
			drawSmall(credits, makeWordImageSmall(testers[i]), 4, pos++);
		}

		pos++;
		drawSmall(credits, makeWordImageSmall("LISTED FOR TRADITION:"), 2, pos++);
		drawSmall(credits, makeWordImageSmall("MIKETRETHEWEY"), 4, pos++);

		@SuppressWarnings("serial")
		final JComponent aboutDisplay = new JComponent() {
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.scale(ZOOM, ZOOM);
				g2.drawImage(CREDITS, 0, 2, null);
			}
		};
		aboutDisplay.setBackground(null);
		aboutFrame.add(aboutDisplay);

		// menu
		final JMenuBar menu = new JMenuBar();
		frame.setJMenuBar(menu);

		// file menu
		final JMenu fileMenu = new JMenu("File");
		menu.add(fileMenu);

		// remap keys
		final JMenuItem mapper = new JMenuItem("Configure keybinds");
		ImageIcon mitts = new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/Mitts.png"));
		mapper.setIcon(mitts);
		fileMenu.add(mapper);

		mapper.addActionListener(
			arg0 -> {
				if (remap.isVisible()) {
					remap.requestFocus();
				} else {
					remap.setLocation(hooker.getLocationOnScreen());
					remap.setVisible(true);
				}
			});

		remap.addRemapListener(
			arg0 -> {
				controls.kill();
				gamePlayer.setController(arg0.map);
				addToController(arg0.map);
				remap.setVisible(false);
			});

		fileMenu.addSeparator();

		// exit
		final JMenuItem exit = new JMenuItem("Exit");
		ImageIcon mirror = new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/Mirror.png"));
		exit.setIcon(mirror);
		fileMenu.add(exit);
		exit.addActionListener(arg0 -> System.exit(0));

		// scores menu
		final JMenu scoresMenu = new JMenu("Performance");
		menu.add(scoresMenu);

		// show scores
		final JMenuItem scoreShow = new JMenuItem("Performance chart");
		ImageIcon boots = new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/Boots.png"));
		scoreShow.setIcon(boots);

		DialogTask showScores = (b) -> {
				if (scoreFrame.isVisible()) {
					if (b) {
						scoreFrame.setVisible(false);
					} else {
						scoreFrame.requestFocus();
					}
				} else {
					scoreFrame.setLocation(hooker.getLocationOnScreen());
					scoreFrame.setVisible(true);
				}
			};

		scoreShow.addActionListener(arg0 -> showScores.switchWindow(false));
		scoresMenu.add(scoreShow);

		// colors
		boolean[] colors = new boolean[] { true }; // JCheckBoxMenuItem is stupid
		final JMenuItem colorful = new JMenuItem("Performance highlighting");
		ImageIcon lampOn = new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/Lamp.png"));
		ImageIcon lampOff = new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/Lamp dark.png"));
		colorful.setIcon(lampOn);

		DialogTask colorize = (b) -> {
				colors[0] = !colors[0];
				if (colors[0]) {
					colorful.setIcon(lampOn);
					scores.setDefaultRenderer(Number.class, SCORE_TABLE);
				} else {
					colorful.setIcon(lampOff);
					scores.setDefaultRenderer(Number.class, PLAIN_TABLE);
				}
				if (scoreFrame.isVisible()) {
					scoreFrame.repaint();
				}
			};

		colorful.addActionListener(arg0 -> colorize.switchWindow(false));

		scoresMenu.add(colorful);

		// help menu
		final JMenu helpMenu = new JMenu("Help");
		menu.add(helpMenu);

		// how to play
		final JMenuItem howToPlay = new JMenuItem("How to play");
		ImageIcon compass = new ImageIcon(
				MenuPractice.class.getResource("/Practice/Images/Meta/Compass.png")
			);
		howToPlay.setIcon(compass);
		helpMenu.add(howToPlay);
		howToPlay.addActionListener(
			arg0 -> {
				if (howPlayFrame.isVisible()) {
					howPlayFrame.requestFocus();
				} else {
					howPlayFrame.setLocation(hooker.getLocationOnScreen());
					howPlayFrame.setVisible(true);
				}
			});

		// about
		final JMenuItem about = new JMenuItem("About");
		ImageIcon map = new ImageIcon(
				MenuPractice.class.getResource("/Practice/Images/Meta/Map.png")
			);
		about.setIcon(map);
		helpMenu.add(about);
		about.addActionListener(
			arg0 -> {
				if (aboutFrame.isVisible()) {
					aboutFrame.requestFocus();
				} else {
					aboutFrame.setLocation(hooker.getLocationOnScreen());
					aboutFrame.setVisible(true);
				}
			});

		// set icon
		ImageIcon ico =
			new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/ico big.png"));
		ImageIcon icoTask =
			new ImageIcon(MenuPractice.class.getResource("/Practice/Images/Meta/ico small.png"));

		ArrayList<Image> icons = new ArrayList<Image>();
		icons.add(ico.getImage());
		icons.add(icoTask.getImage());
		frame.setIconImages(icons);

		// frame display
		frame.setSize(D);
		frame.setMinimumSize(D);
		frame.setResizable(false);
		frame.setLocation(150, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// listeners for gameplay area
		gamePlayer.addTurnListener(
			arg0 -> {
					ScoreCard tempRef = arg0.score;
					if (tempRef != null) {
						int tempScore = tempRef.finalScore;
						model.addRow(turn.val, tempRef);
						totalScore.add(tempScore);
						hiscore.setText(Integer.toString(totalScore.val));
						turn.increment();
					}
					gamePlayer.repaint();
			});

		gamePlayer.addGameStartListener(
			arg0 -> {
				scores.clearSelection();
				scores.setRowSelectionAllowed(false);
				scores.setFocusable(false);
				analysis.setVisible(false);
				analyze.setEnabled(false);
				clear.setEnabled(false);
			});

		gamePlayer.addGameOverListener(
			arg0 -> {
				scores.setFocusable(true);
				scores.setRowSelectionAllowed(true);
				analyze.setEnabled(true);
				clear.setEnabled(true);
			});

		// add snes input stuff here
		MoveSel moveSel = (a) -> {
				int b = a < 0 ? -1 : 1;
				int scoresRow = scores.getSelectedRow();
				int maxRow = scores.getRowCount();
				if (maxRow == 0) {
					return;
				}
				if (scoresRow == -1) {
					scores.setRowSelectionInterval(0, 0);
					return;
				}
				int newSel = a + b;
				if (newSel < 0) {
					newSel = 0;
				} else if (newSel == maxRow) {
					newSel = maxRow - 1;
				}
				scores.setRowSelectionInterval(newSel, newSel);
			};

		addToController(controls);
		this.addSNESInputListener(
			arg0 -> {
					if (arg0.getSource() == this) {
						return;
					}
					switch (arg0.getKey()) {
						case SNESInputEvent.SNES_SELECT :
							showScores.switchWindow(true);
							break;
						case SNESInputEvent.SNES_X :
							showAnalysis.switchWindow(true);
							break;
						case SNESInputEvent.SNES_R :
							moveSel.moveDir(1);
							break;
						case SNESInputEvent.SNES_L :
							moveSel.moveDir(-1);
							break;
						case SNESInputEvent.SNES_Y | SNESInputEvent.SNES_L :
							clearData.switchWindow(true);
							break;
						case SNESInputEvent.SNES_X | SNESInputEvent.SNES_L :
							colorize.switchWindow(true);
							break;
					}
				});

		frame.setVisible(true);
	}

	private List<SNESInputListener> snesListen = new ArrayList<SNESInputListener>();
	public synchronized void addSNESInputListener(SNESInputListener s) {
		snesListen.add(s);
	}

	public synchronized void fireSNESInputEvent(SNESInputEvent e) {
		Iterator<SNESInputListener> listening = snesListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(e);
		}
	}

	/**
	 * Warning with instructions for failed library loading
	 */
	public static void showWarning() {
		// try to set LaF
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e2) {
				// do nothing
		} //end System
		JFrame x = new JFrame();
		x.setLayout(new BorderLayout());
		x.setTitle("Libraries missing");
		x.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		x.add(new JLabel(
				String.join("",
						new String[] {
								"<html>",
								"<div style=\"padding: 13px; font-size: 10px;\">",
								"Your Java Runtime Environment does not contain all necessary libraries to run JInput.",
								"<br /><br />",
								"To remedy this, copy the files from the folder <tt>lib</tt> in this directory ",
								"into your JRE's <tt>bin</tt> folder.",
								"<br /><br />",
								"JRE bin location:",
								"<br />",
								"\t\t<div style=\"text-indent:20px; font-weight: bold;\"><tt>",
								System.getProperty("java.home"),
								System.getProperty("file.separator"),
								"bin",
								"</tt></div>",
								"<br />",
								"Alternatively, you may open <tt>JInputLibrarySetup.jar</tt> ",
								"as an administrator in command prompt to automatically copy those files to the correct location.",
								"<br /><br />",
								"The application will halt when you close this window.",
								"</div>",
								"</html>"
				})),
				BorderLayout.NORTH);
		x.setMinimumSize(new Dimension(500,300));
		x.setLocation(200, 200);
		x.setVisible(true);
	}

	// implement snes inputs in doTheGUI
	public void addSNESInput() {}

	// this class should never actually have exclusive focus of its controller
	public void whineToMommy() {}
	public void shutUp() {}

	// for holding ints
	static class IntHolder {
		int val;

		IntHolder() {
			val = 0;
		}

		void set(int a) {
			val = a;
		}

		void add(int a) {
			val += a;
		}

		void increment() {
			val++;
		}
	}

	static interface DialogTask {
		void switchWindow(boolean cameFromController);
	}

	static interface MoveSel {
		void moveDir(int i);
	}
}