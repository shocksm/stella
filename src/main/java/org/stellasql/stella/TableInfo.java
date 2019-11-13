package org.stellasql.stella;

import java.util.LinkedList;
import java.util.List;

import org.stellasql.stella.connection.ConnectionManager;

public class TableInfo
{
  private String name = "";
  private String schema = "";
  private String catalog = "";
  private String type = "";
  private List<ColumnInfo> columnInfoList = new LinkedList<ColumnInfo>();
  private List<IndexInfo> indexInfoList = new LinkedList<IndexInfo>();
  private LinkedList<PrimaryKeyInfo> pkInfoList = new LinkedList<PrimaryKeyInfo>();
  private boolean columnsLoaded = false;
  private boolean fullyLoaded = false;
  private String properName = "";
  private boolean useCatalogs = false;
  private boolean useSchemas = false;
  private String separator = "";

  public TableInfo(String name, String catalog, String schema, String type, boolean useCatalogs, boolean useSchemas, String separator)
  {
    this.name = name;
    this.catalog = catalog;
    this.schema = schema;
    this.type = type;

    this.useCatalogs = useCatalogs;
    this.useSchemas = useSchemas;
    this.separator = separator;
    setProperName();
  }

  public TableInfo(String name, String catalog, String schema, String type, ConnectionManager conManager)
  {
    this.name = name;
    this.catalog = catalog;
    this.schema = schema;
    this.type = type;

    useCatalogs = conManager.getUseCatalogs();
    useSchemas = conManager.getUseSchemas();
    separator  = conManager.getSeparator();
    setProperName();
  }

  private void setProperName()
  {
    properName = "";
    if (useCatalogs)
      properName = catalog + separator;
    if (useSchemas)
      properName += schema + separator;
    properName += name;
  }

  public String getProperName()
  {
    return properName;
  }

  public String getCatalog()
  {
    return catalog;
  }
  public void setCatalog(String catalog)
  {
    this.catalog = catalog;
    setProperName();
  }
  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name = name;
    setProperName();
  }
  public String getSchema()
  {
    return schema;
  }
  public void setSchema(String schema)
  {
    this.schema = schema;
    setProperName();
  }

  public boolean getColumnsLoaded()
  {
    return columnsLoaded;
  }

  public void setColumnsLoaded(boolean loaded)
  {
    columnsLoaded = loaded;
  }

  public void addColumn(ColumnInfo columnInfo)
  {
    columnInfoList.add(columnInfo);
  }

  public List<ColumnInfo> getColumns()
  {
    return columnInfoList;
  }

  public ColumnInfo getColumn(String name) {
  	for (ColumnInfo ci : columnInfoList) {
  		if (ci.getColumnName().equals(name)) {
  			return ci;
  		}
  	}

  	return null;
  }

  public void clearColumns()
  {
    columnInfoList.clear();
  }

  public String getType()
  {
    return type;
  }

  public void setFullyLoaded(boolean loaded)
  {
    fullyLoaded  = loaded;
  }

  public boolean getFullyLoaded()
  {
    return fullyLoaded;
  }

  public void addIndex(IndexInfo indexInfo)
  {
    indexInfoList.add(indexInfo);
  }

  public List<IndexInfo> getIndexes()
  {
    return indexInfoList;
  }

  public void addPrimaryKey(PrimaryKeyInfo pkInfo)
  {
    pkInfoList.add(pkInfo);
  }

	public List<PrimaryKeyInfo> getPrimaryKeys()
  {
    return pkInfoList;
  }

  @Override
  public String toString()
  {
    return catalog + "." + schema + "." + name;
  }

}
