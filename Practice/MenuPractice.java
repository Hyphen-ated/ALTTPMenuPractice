package Practice;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public class MenuPractice {
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
		final Dimension d = new Dimension(800, 350);
		final Dimension d2 = new Dimension(300, 300);
		final JFrame frame = new JFrame("Menu Simulator 2K17");

		final Font consolas = new Font("Consolas", Font.PLAIN, 12);
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
		ScoreTableModel model = new ScoreTableModel();
		scores.setModel(model);

		// scroll pane for score

		JScrollPane scoreScroll = new JScrollPane(scores,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scoreScroll.setFocusable(false);
		scoreScroll.setViewportBorder(null);
		scoreScroll.setBorder(null);
		scoreScroll.getViewport().setBorder(null);

		scores.setBorder(null);
		scores.setFont(consolas);
		scores.setBackground(null);
		scores.setFocusable(false);

		JLabel scoreTotal = new JLabel("Total score:", SwingConstants.RIGHT);
		JLabel hiscore = new JLabel("0", SwingConstants.RIGHT);
		hiscore.setFont(consolas);

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

		l.putConstraint(SpringLayout.WEST, scoreScroll, -370,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.EAST, scoreScroll, -5,
				SpringLayout.EAST, wrap);
		l.putConstraint(SpringLayout.NORTH, scoreScroll, 5,
				SpringLayout.NORTH, wrap);
		l.putConstraint(SpringLayout.SOUTH, scoreScroll, -5,
				SpringLayout.NORTH, scoreTotal);
		wrap.add(scoreScroll);

		// menu
		final JMenuBar menu = new JMenuBar();
		frame.setJMenuBar(menu);

		// file menu
		final JMenu fileMenu = new JMenu("File");
		menu.add(fileMenu);

		// exit
		final JMenuItem exit = new JMenuItem("Exit");
		ImageIcon mirror = new ImageIcon(
				MenuGame.class.getResource("/Practice/Images/Meta/mirror.png")
			);
		exit.setIcon(mirror);
		fileMenu.add(exit);
		exit.addActionListener(arg0 -> System.exit(0));

		// frame display
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
						model.addRow(new int[] {
								turn.val,
								tempScore,
								tempRef.moves,
								tempRef.minMoves,
								tempRef.finalTime,
								tempRef.startPresses - 1
						});
						totalScore.add(tempScore);
						hiscore.setText(Integer.toString(totalScore.val));
					}
					turn.increment();
			});

		frame.setVisible(true);
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