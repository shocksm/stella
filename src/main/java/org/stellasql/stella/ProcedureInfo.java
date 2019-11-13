package org.stellasql.stella;

import java.util.LinkedList;
import java.util.List;

import org.stellasql.stella.connection.ConnectionManager;

public class ProcedureInfo
{
  private String name = "";
  private String schema = "";
  private String catalog = "";
  private String remarks = "";
  private List<ProcedureColumnInfo> columnInfoList = new LinkedList<ProcedureColumnInfo>();
  private List<IndexInfo> indexInfoList = new LinkedList<IndexInfo>();
  private LinkedList<PrimaryKeyInfo> pkInfoList = new LinkedList<PrimaryKeyInfo>();
  private boolean columnsLoaded = false;
  private boolean fullyLoaded = false;
  private String properName = "";
  private boolean useCatalogs = false;
  private boolean useSchemas = false;
  private String separator = "";

  public ProcedureInfo(String name, String catalog, String schema, String remarks, boolean useCatalogs, boolean useSchemas, String separator)
  {
    this.name = name;
    this.catalog = catalog;
    this.schema = schema;
    this.remarks = remarks;

    this.useCatalogs = useCatalogs;
    this.useSchemas = useSchemas;
    this.separator = separator;
    setProperName();
  }

  public ProcedureInfo(String name, String catalog, String schema, String remarks, ConnectionManager conManager)
  {
    this.name = name;
    this.catalog = catalog;
    this.schema = schema;
    this.remarks = remarks;

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

  public void addColumn(ProcedureColumnInfo columnInfo)
  {
    columnInfoList.add(columnInfo);
  }

  public List<ProcedureColumnInfo> getColumns()
  {
    return columnInfoList;
  }

  public void clearColumns()
  {
    columnInfoList.clear();
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

  public String getRemarks()
  {
    return remarks;
  }

}
