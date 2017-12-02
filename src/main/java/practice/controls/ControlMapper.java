package practice.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import practice.listeners.*;
import net.java.games.input.*;

public class ControlMapper extends JDialog {
	private static final long serialVersionUID = -3293305934784136424L;

	private static final Dimension PREF_D = new Dimension(200, 400);
	private static final Dimension TEXT_D = new Dimension(100, 17);

	private static ContWrapper[] controllerList = refreshList();
	private CompWrapper[] list = new CompWrapper[SNESButton.values().length];

	static final ContWrapper[] refreshList() {
		ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
		ArrayList<ContWrapper> ret = new ArrayList<ContWrapper>();
		for (Controller c : env.getControllers()) {
			Controller.Type t = c.getType();

			if (t.equals(Controller.Type.GAMEPAD) ||
					t.equals(Controller.Type.KEYBOARD)) {
				Component[] comp = c.getComponents();
				CompWrapper[] use = new CompWrapper[12];
				int i = 0;
				ControllerType type = ControllerType.inferType(c);

				defaultMappings :
				for (SNESButton s : SNESButton.values()) {
					for (Component x : comp) {
						if (x.getIdentifier() == s.getDefaultButton(type)) {
							use[i++] = new CompWrapper(x);
							continue defaultMappings;
						}
					} // end components
				} // end buttons
				ret.add(new ContWrapper(c, use));
			} // end valid type if
		} // end controller loop

		ContWrapper[] r = new ContWrapper[ret.size()];
		int i = 0;
		for (ContWrapper a : ret) {
			r[i++] = a;
		}
		return r;
	}

	// default controller
	public static final ControllerHandler defaultController;
	private static ContWrapper keyboard;

	static {
		for (ContWrapper c : controllerList) {
			if (c.c.getType().equals(Controller.Type.KEYBOARD)) {
				keyboard = c;
				break;
			}
		}

		defaultController = makeControllerHandler(keyboard);
	}

	JComboBox<ContWrapper> curBox;
	JPanel comboArea = new JPanel();
	JPanel compArea = new JPanel();
	ControlCustomizer customizer = new ControlCustomizer();
	ContWrapper activeController;

	public ControlMapper(JFrame frame) {
		super(frame, "Configure");
		activeController = controllerList[0];
		initialize();
	}

	private final void initialize() {
		this.setPreferredSize(PREF_D);
		this.setMinimumSize(PREF_D);
		this.setResizable(false);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 4;
		c.ipady = 2;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = -1;

		newComboBox();
		c.gridy++;
		c.gridx = 0;
		this.add(comboArea, c);

		compArea.setLayout(new GridBagLayout());
		setComponentArea();
		c.gridy++;
		c.gridx = 0;
		this.add(compArea, c);

		// apply
		JButton confirm = new JButton("Apply");
		c.gridy++;
		c.gridx = 0;
		this.add(confirm, c);

		JTextField no = new JTextField("");
		no.setEditable(false);
		no.setHighlighter(null);
		no.setForeground(null);
		no.setBackground(null);
		no.setBorder(null);
		no.setHorizontalAlignment(SwingConstants.CENTER);
		no.setSize(TEXT_D);
		c.gridy++;
		this.add(no, c);

		confirm.addActionListener(
			arg0 -> {
				boolean okToGo = true;
				dupeSearch :
				for (CompWrapper e : list) {
					dupeMatch :
					for (CompWrapper k : list) {
						if (e == k) {
							continue dupeMatch;
						}
						if (e.c == k.c) {
							okToGo = false;
							break dupeSearch;
						}
					} // end loop 1
				} // end loop 2
				if (okToGo) {
					no.setText("");
					no.setForeground(null);
					no.setBackground(null);
					fireRemapEvent();
				} else {
					no.setText("DUPLICATE KEYS");
					no.setForeground(Color.WHITE);
					no.setBackground(Color.RED);
				}
			});

		customizer.addComponentPollListener(
			arg0 -> {
				CompWrapper f = focusedDude();
				if (f != null) {
					f.setComp(arg0.comp);
					repaint();
				}
			});
		customizer.setController(activeController.c);
	}

	private void setComponentArea() {
		compArea.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 4;
		c.ipady = 2;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = -1;

		int i = 0;
		for (SNESButton b : SNESButton.values()) {
			JLabel lbl = new JLabel(b.name);
			CompWrapper k = activeController.list[i];
			list[i++] = k;
			c.gridy++;
			c.gridx = 0;
			compArea.add(lbl, c);
			c.gridx = 1;
			compArea.add(k.text, c);
		}
	}

	private ItemListener boxRead = arg0 -> {
		activeController = (ContWrapper) curBox.getSelectedItem();
		setControlWrapper(activeController);
	};

	private void newComboBox() {
		if (curBox != null) {
			curBox.removeItemListener(boxRead);
		}
		comboArea.removeAll();
		curBox = new JComboBox<ContWrapper>(controllerList);
		comboArea.add(curBox);
		setControlWrapper(curBox.getItemAt(0));
		curBox.addItemListener(boxRead);
		revalidate();
	}

	private static ControllerHandler makeControllerHandler(ContWrapper w) {
		ControllerHandler ret = null;
		Class<? extends ControllerHandler> hClass = w.t.dType.handler;
		try {
			Constructor<? extends ControllerHandler> ctor = hClass.getDeclaredConstructor(Controller.class, Component[].class);
			ret = ctor.newInstance((Controller) w.c, (Component[]) w.getList());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void setControlWrapper(ContWrapper c) {
		activeController = c;
		customizer.setController(activeController.c);
		repaint();
	}

	private CompWrapper focusedDude() {
		if (activeController == null) {
			return null;
		}
		CompWrapper ret = null;
		for (CompWrapper c : activeController.list) {
			if (c.active) {
				ret = c;
				break;
			}
		}
		return ret;
	}

	public void setRunning(boolean r) {
		customizer.setRunning(r);
	}

	/*
	 * Events for being done
	 */
	private List<RemapListener> doneListen = new ArrayList<RemapListener>();
	public synchronized void addRemapListener(RemapListener s) {
		doneListen.add(s);
	}

	private synchronized void fireRemapEvent() {
		RemapEvent te = new RemapEvent(this, makeControllerHandler(activeController));
		Iterator<RemapListener> listening = doneListen.iterator();
		while(listening.hasNext()) {
			(listening.next()).eventReceived(te);
		}
	}

	static class ContWrapper {
		final Controller c;
		final CompWrapper[] list;
		final ControllerType t;

		ContWrapper(Controller c, CompWrapper[] list) {
			this.c = c;
			this.list = list;
			t = ControllerType.inferType(c);
		}

		public String toString() {
			return c.getName();
		}

		Component[] getList() {
			Component[] ret = new Component[12];
			for (int i = 0; i < 12; i++) {
				ret[i] = list[i].c;
			}
			return ret;
		}
	}

	static class CompWrapper {
		Component c;
		final JTextField text;
		boolean active;

		CompWrapper(Component c) {
			this.c = c;
			text = new JTextField();
			text.setPreferredSize(TEXT_D);
			text.setMinimumSize(TEXT_D);
			text.setHorizontalAlignment(SwingConstants.CENTER);

			setComp(c);

			text.setEditable(false);
			text.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent arg0) {
					text.setBackground(Color.YELLOW);
					active = true;
				}

				public void focusLost(FocusEvent arg0) {
					text.setBackground(null);
					active = false;
				}});
		}

		public void setComp(Component c) {
			this.c = c;
			text.setText(c.getName());
		}
	}
}