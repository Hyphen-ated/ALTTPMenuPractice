package Practice;

import java.awt.Graphics;

import javax.swing.JComponent;

public class ItemSlot extends JComponent {
	private static final long serialVersionUID = -4130293452712063127L;

	private final Item mine;
	private ImageNamePair i;

	ItemSlot(Item item) {
		this.setSize(MenuGame.BLOCK_D);
		this.setMinimumSize(MenuGame.BLOCK_D);
		this.setMaximumSize(MenuGame.BLOCK_D);
		this.setPreferredSize(MenuGame.BLOCK_D);
		this.mine = item;
	}

	public void setRandomItem() {
		i = mine.getRandomItem();
	}

	public void paint(Graphics g) {
		if (isEnabled()) {
			g.drawImage(i.img, 0, 0, null);
		}
	}
}