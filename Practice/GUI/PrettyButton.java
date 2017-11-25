package Practice.GUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.AbstractButton;

import Practice.MenuGameConstants;

public class PrettyButton extends AbstractButton {
	private static final long serialVersionUID = 34033459696182882L;

	static final int ZOOM = MenuGameConstants.ZOOM;

	private BufferedImage bgCur;
	private BufferedImage bg;
	private BufferedImage bgPress;
	private BufferedImage bgDisabled;

	private BufferedImage dispCur;
	private BufferedImage disp;
	private BufferedImage dispHover;
	private BufferedImage dispAbled;

	private Dimension d;

	private boolean enabled;
	final MouseListener mouse;
	boolean hasMouse = false;

	public PrettyButton(int length) {
		d = new Dimension(((length + 2) * 8) * ZOOM, 24 * ZOOM);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

		bg = MenuGameConstants.makePrettyBorder(length);
		bgPress = MenuGameConstants.makePrettyBorderInset(length);
		bgDisabled = MenuGameConstants.makePrettyBorderDisabled(length);

		mouse = new MouseListener() {
			public void mousePressed(MouseEvent arg0) {
				bgCur = bgPress;
				repaint();
			}

			public void mouseClicked(MouseEvent arg0) {

			}

			public void mouseEntered(MouseEvent arg0) {
				dispCur = dispHover;
				repaint();
			}

			public void mouseExited(MouseEvent arg0) {
				dispCur = disp;
				repaint();
			}

			public void mouseReleased(MouseEvent arg0) {
				bgCur = bg;
				repaint();
				if (enabled & PrettyButton.this.contains(arg0.getPoint())) {
					PrettyButton.this.fireActionPerformed(new ActionEvent(this, 0, ""));
				}
			}
		};

		setFocusable(false);
	}

	public void setText(String text) {
		disp = MenuGameConstants.makeWordImage(text, 0);
		dispHover = MenuGameConstants.makeWordImage(text, 1);
		dispAbled = MenuGameConstants.makeWordImage(text, 2);
		dispCur = enabled ? disp : dispAbled;
		repaint();
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(ZOOM, ZOOM);
		g2.drawImage(bgCur, 0, 0, null);
		g2.drawImage(dispCur, 8, 8, null);
	}

	public void setEnabled(boolean b) {
		enabled = b;
		if (b) {
			if (!hasMouse) {
				this.addMouseListener(mouse);
				hasMouse = true;
			}
			bgCur = bg;
			dispCur = disp;
		} else {
			if (hasMouse) {
				this.removeMouseListener(mouse);
				hasMouse = false;
			}
			bgCur = bgDisabled;
			dispCur = dispAbled;
		}
		repaint();
	}
}