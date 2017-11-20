package Practice;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ScoreTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5841696898807444151L;

	String[] columnNames = { "Turn",
			"Score",
			"Moves",
			"<html>Optimal<br />path</html>",
			"Time",
			"<html>Wrong<br />starts</html>" };
	ArrayList<int[]> data = new ArrayList<int[]>();

	public ScoreTableModel() {
		super();
	}

	public void addRow(int[] data) {
		int l = this.data.size();
		this.data.add(data);
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

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return (int) (data.get(rowIndex)[columnIndex]);
	}
}
