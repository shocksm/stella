package org.stellasql.stella.gui.statement;

import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.stellasql.stella.ApplicationData;
import org.stellasql.stella.ColumnInfo;
import org.stellasql.stella.TableInfo;

public class InsertBuilder {
	private final static Logger logger = LogManager.getLogger(InsertBuilder.class);

	protected TableInfo tableInfo = null;
	protected LinkedList<ColumnValue> columnData = new LinkedList<ColumnValue>();

	public InsertBuilder(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}

	public void addColumnValue(ColumnInfo columnInfo, Object value)
  {
		ColumnValue cv = new ColumnValue(columnInfo, value);
		columnData.add(cv);
  }

	public String buildStatement()
  {
    StringBuffer sbuf = new StringBuffer();
    sbuf.append("INSERT INTO ");
    sbuf.append(tableInfo.getProperName());

    StringBuffer columns = new StringBuffer();
    StringBuffer values = new StringBuffer();

    for (ColumnValue cv : columnData)
    {
      if (columns.length() > 0) {
      	columns.append(", ");
      }

      columns.append(cv.getName());

      if (values.length() > 0) {
      	values.append(", ");
      }

      values.append(cv.getValue());
    }

    sbuf.append("\n(").append(columns).append(")\n");
    sbuf.append("VALUES");
    sbuf.append("\n(").append(values).append(")");
    sbuf.append(ApplicationData.getInstance().getQuerySeparator());

    return sbuf.toString();
  }



	protected class ColumnValue {
		private ColumnInfo columnInfo;
		private Object value;

		protected ColumnValue(ColumnInfo columnInfo, Object value) {
			this.columnInfo = columnInfo;
			this.value = value;
		}

		public String getName() {
			return columnInfo.getColumnName();
		}

		public String getValue()
    {
			if (value == null) {
				return null;
			}

      if (DataTypeUtil.isCharacterType(columnInfo.getDataType()))
      {
        return "'" + value.toString() + "'";
      }
      else if (DataTypeUtil.isTimestampType(columnInfo.getDataType()) && value instanceof Date)
      {
	        return DataTypeUtil.formatAsTimestamp((Date)value);
      }
      else if (DataTypeUtil.isDateType(columnInfo.getDataType()) && value instanceof Date)
      {
      	return DataTypeUtil.formatAsDate((Date)value);
      }
      else if (DataTypeUtil.isTimeType(columnInfo.getDataType()) && value instanceof Date)
      {
      	return DataTypeUtil.formatAsTime((Date)value);
      }
	  	else if (!(value instanceof Number) && !(value instanceof Boolean)) {
	  		logger.warn("Column " + columnInfo.getColumnName() + " is not formated. Returning as String. Data Type: " + columnInfo.getDataType() + " Class: " + value.getClass().getName());
	  	}

      return value.toString();
    }

	}
}
