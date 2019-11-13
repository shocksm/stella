package org.stellasql.stella.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.IndexInfo;
import org.stellasql.stella.PrimaryKeyInfo;
import org.stellasql.stella.TableInfo;
import org.stellasql.stella.gui.custom.CustomTable;
import org.stellasql.stella.gui.custom.TableData;
import org.stellasql.stella.gui.util.FontSetter;
import org.stellasql.stella.gui.util.StellaImages;

public class TableInfoDialog {
	private Shell shell = null;
	private TabFolder tabFolder = null;
	private CustomTable<ColumnInfo> tableColumns = null;
	private CustomTable<IndexInfo> tableIndexes = null;
	private CustomTable<PrimaryKeyInfo> tablePrimaryKey = null;

	private boolean indexTablepainted = false;
	private boolean primaryKeyTablepainted = false;


	public TableInfoDialog(Shell parent, TableInfo tableInfo) {
		shell = new Shell(parent, SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
		shell.setImage(StellaImages.getInstance().getAppSmallImage());
		shell.setText("Table Info for " + tableInfo.getName());
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);

		tabFolder = new TabFolder(shell, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		tabFolder.setLayoutData(gridData);

		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Columns");
		Composite wrapper = new Composite(tabFolder, SWT.NONE);
		wrapper.setLayout(new GridLayout());
		tableColumns = new CustomTable<ColumnInfo>(wrapper);
		tableColumns.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		item.setControl(wrapper);

		item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Indexes");
		wrapper = new Composite(tabFolder, SWT.NONE);
		wrapper.setLayout(new GridLayout());
		tableIndexes = new CustomTable<IndexInfo>(wrapper);
		tableIndexes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		item.setControl(wrapper);

		item = new TabItem(tabFolder, SWT.NONE);
		item.setText("Primary Key");
		wrapper = new Composite(tabFolder, SWT.NONE);
		wrapper.setLayout(new GridLayout());
		tablePrimaryKey = new CustomTable<PrimaryKeyInfo>(wrapper);
		tablePrimaryKey.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		item.setControl(wrapper);

		setFonts();


		tableColumns.setTableData(new ColumnTableData(tableInfo.getColumns()));
		tableIndexes.setTableData(new IndexTableData(tableInfo.getIndexes()));
		tablePrimaryKey.setTableData(new PrimaryKeyData(tableInfo.getPrimaryKeys()));

		tableIndexes.addListener(SWT.PaintItem, event -> {
			if (!indexTablepainted) {
				indexTablepainted = true;
				tableIndexes.calcColumnWidths();
			}
		});
		tablePrimaryKey.addListener(SWT.PaintItem, event -> {
			if (!primaryKeyTablepainted) {
				primaryKeyTablepainted = true;
				tablePrimaryKey.calcColumnWidths();
			}
		});
	}

	private void setFonts() {
		FontSetter.setFont(tabFolder, ApplicationData.getInstance().getGeneralFont());
		FontSetter.setFont(tableColumns, ApplicationData.getInstance().getGeneralFont());
		FontSetter.setFont(tableIndexes, ApplicationData.getInstance().getGeneralFont());
		FontSetter.setFont(tablePrimaryKey, ApplicationData.getInstance().getGeneralFont());
	}

	public void open() {
		shell.setSize(1200, 600);
		int x = shell.getParent().getSize().x / 2 - shell.getSize().x / 2;
		int y = shell.getParent().getSize().y / 2 - shell.getSize().y / 2;

		x += shell.getParent().getLocation().x;
		y += shell.getParent().getLocation().y;
		if (y < 0)
			y = 0;
		if (x < 0)
			x = 0;

		shell.setLocation(x, y);
		tableColumns.calcColumnWidths();
		tableIndexes.calcColumnWidths();
		tablePrimaryKey.calcColumnWidths();
		shell.open();


	}


	private abstract class BaseTableData<T> implements TableData<T> {
		protected List<T> rows;

		public BaseTableData(List<T> rows) {
			this.rows = rows;
		}

		@Override
		public String getCellAsString(int row, int column) {
			Object obj = getCell(row, column);
			return getStringValue(obj);
		}

		public String getStringValue(Object obj) {
			if (obj == null)
				return "";
			else
				return obj.toString();
		}

		@Override
		public String getNullString() {
			return "";
		}

		@Override
		public T getRowObject(int row) {
			return rows.get(row);
		}

		@Override
		public int getIndexOfRowObject(T obj) {
			return rows.indexOf(obj);
		}

		protected abstract Object getColumnObject(T arg1, int columnIndex);

	}

	private class ColumnTableData extends BaseTableData<ColumnInfo> {
		private final String[] columnNames = {"Name", "Type", "Size", "Decimal Digits", "Nullable", "Default"};
		protected TableDataComparator<ColumnInfo> tdComparator = null;

		public ColumnTableData(List<ColumnInfo> rows) {
			super(rows);
			tdComparator = new TableDataComparator<ColumnInfo>(this);
		}

		@Override
		public String[] getColumnNames() {
			return columnNames;
		}

		@Override
		public String getColumnToolTip(int columnIndex) {
			if (columnIndex == 0)
				return "Name of the column";
			else if (columnIndex == 1)
				return "Data type name";
			else if (columnIndex == 2)
				return "For char or date types this is the maximum number of characters,\nfor numeric or decimal types this is precision";
			else if (columnIndex == 3)
				return "The number of fractional digits";
			else if (columnIndex == 4)
				return "Is NULL allowed.";
			else if (columnIndex == 5)
				return "Default value";
			else
				return null;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		protected Object getColumnObject(ColumnInfo arg, int column) {
			ColumnInfo columnInfo = arg;
			Object obj = null;

			if (column == 0)
				obj = columnInfo.getColumnName();
			else if (column == 1)
				obj = columnInfo.getTypeName();
			else if (column == 2)
				obj = new Integer(columnInfo.getColumnSize());
			else if (column == 3)
				obj = new Integer(columnInfo.getDecimalDigits());
			else if (column == 4)
				obj = new Boolean(columnInfo.getNullable());
			else if (column == 5)
				obj = columnInfo.getDefault();
			else if (column == 6)
				obj = columnInfo.getOrderIndex();

			return obj;
		}

		@Override
		public Object getCell(int row, int column) {
			return getColumnObject(rows.get(row), column);
		}

		@Override
		public void sort(int columnIndex, int dir) {
			tdComparator.setSortColumn(columnIndex);
			tdComparator.setSortDirection(dir);
			Collections.sort(rows, tdComparator);
		}

	}

	private class IndexTableData extends BaseTableData<IndexInfo> {
		private final String[] columnNames = {"Name", "Non Unique", "Qualifier", "Type", "Ordinal Position", "Column Name", "Asc or Desc", "Cardinality", "Pages", "Filter Condition"};
		protected TableDataComparator<IndexInfo> tdComparator = null;

		public IndexTableData(List<IndexInfo> rows) {
			super(rows);
			tdComparator = new TableDataComparator<IndexInfo>(this);
		}

		@Override
		public String[] getColumnNames() {
			return columnNames;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public String getColumnToolTip(int columnIndex) {
			if (columnIndex == 0)
				return "Name of the index";
			else if (columnIndex == 1)
				return "Can the index values be non-unique";
			else if (columnIndex == 2)
				return "index catalog";
			else if (columnIndex == 3)
				return "Type of index";
			else if (columnIndex == 4)
				return "Column sequence number within index";
			else if (columnIndex == 5)
				return "Column name the index is on";
			else if (columnIndex == 6)
				return "'A' => ascending, 'D' => descending, may be null if sort sequence is not supported";
			else if (columnIndex == 7)
				return "When Type is Statistic, then this is the number of rows in the table;\notherwise, it is the number of unique values in the index";
			else if (columnIndex == 8)
				return "When Type is Statisic then this is the number of pages used for the table,\notherwise it is the number of pages used for the current index";
			else if (columnIndex == 9)
				return "Filter condition, if any";
			else
				return null;
		}

		@Override
		protected Object getColumnObject(IndexInfo arg, int column) {
			IndexInfo indexInfo = arg;
			Object obj = null;

			if (column == 0)
				obj = indexInfo.getName();
			else if (column == 1)
				obj = new Boolean(indexInfo.getNonUnique());
			else if (column == 2)
				obj = indexInfo.getQualifier();
			else if (column == 3)
				obj = indexInfo.getType();
			else if (column == 4)
				obj = new Integer(indexInfo.getOrdinalPosition());
			else if (column == 5)
				obj = indexInfo.getColumnName();
			else if (column == 6)
				obj = indexInfo.getAscOrDesc();
			else if (column == 7)
				obj = new Integer(indexInfo.getCardinality());
			else if (column == 8)
				obj = new Integer(indexInfo.getPages());
			else if (column == 9)
				obj = indexInfo.getFilterCondition();
			else if (column == 10)
				obj = indexInfo.getOrderIndex();

			return obj;
		}

		@Override
		public Object getCell(int row, int column) {
			return getColumnObject(rows.get(row), column);
		}

		@Override
		public void sort(int columnIndex, int dir) {
			tdComparator.setSortColumn(columnIndex);
			tdComparator.setSortDirection(dir);
			Collections.sort(rows, tdComparator);
		}
	}

	private class PrimaryKeyData extends BaseTableData<PrimaryKeyInfo> {
		private final String[] columnNames = {"Column Name", "Key Seq", "PK Name"};
		protected TableDataComparator<PrimaryKeyInfo> tdComparator = null;

		public PrimaryKeyData(List<PrimaryKeyInfo> rows) {
			super(rows);
			tdComparator = new TableDataComparator<PrimaryKeyInfo>(this);
		}

		@Override
		public String[] getColumnNames() {
			return columnNames;
		}

		@Override
		public String getColumnToolTip(int columnIndex) {
			if (columnIndex == 0)
				return "Name of the column";
			else if (columnIndex == 1)
				return "Sequence number within primary key";
			else if (columnIndex == 2)
				return "Primary key name";
			else
				return null;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		protected Object getColumnObject(PrimaryKeyInfo arg, int column) {
			PrimaryKeyInfo pkInfo = arg;
			Object obj = null;

			if (column == 0)
				obj = pkInfo.getColumnName();
			else if (column == 1)
				obj = new Integer(pkInfo.getKeySeq());
			else if (column == 2)
				obj = pkInfo.getPkName();
			else if (column == 3)
				obj = pkInfo.getOrderIndex();

			return obj;
		}

		@Override
		public Object getCell(int row, int column) {
			return getColumnObject(rows.get(row), column);
		}

		@Override
		public void sort(int columnIndex, int dir) {
			tdComparator.setSortColumn(columnIndex);
			tdComparator.setSortDirection(dir);
			Collections.sort(rows, tdComparator);
		}
	}

	private class TableDataComparator<T> implements Comparator<T> {
		private int columnIndex = -1;
		private int direction = -1;
		private BaseTableData<T> baseTableData = null;

		public TableDataComparator(BaseTableData<T> baseTableData) {
			this.baseTableData = baseTableData;
		}

		public void setSortDirection(int dir) {
			direction = dir;
		}

		public void setSortColumn(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public int compare(T arg1, T arg2) {
			Object obj1 = baseTableData.getColumnObject(arg1, columnIndex);
			Object obj2 = baseTableData.getColumnObject(arg2, columnIndex);

			int val = 0;

			if (obj1 == null && obj2 != null)
				val = 1;
			else if (obj1 != null && obj2 == null)
				val = -1;
			else if (obj1 == null && obj2 == null)
				val = 0;
			else if (obj1 instanceof Number)
				val = Float.compare(((Number) obj1).floatValue(), ((Number) obj2).floatValue());
			else
				val = baseTableData.getStringValue(obj1).compareToIgnoreCase(baseTableData.getStringValue(obj2));

			if (direction == SWT.DOWN)
				val = -val;

			return val;
		}
	}


}
