package Practice;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

class ScoreTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5841696898807444151L;

	String[] columnNames = { "Turn",
			"Score",
			"Moves",
			"Optimal",
			"Time (ms)",
			"Penalties" };
	ArrayList<ScoreCard> data = new ArrayList<ScoreCard>();

	public ScoreTableModel() {
		super();
	}

	public void addRow(int turn, ScoreCard s) {
		int l = this.data.size();
		s.setTurn(turn);
		this.data.add(0, s);
		this.fireTableRowsInserted(l, l);
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return data.size();
	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).toArray()[columnIndex];
	}
}
