package org.stellasql.stella.session;

import java.util.List;

import org.stellasql.stella.ProcedureInfo;
import org.stellasql.stella.TableInfo;

public interface DBObjectListener
{
  public void catalogDataAvailable(List tableTypes, List catalogs);

  public void tableDataAvailable(String catalog, String tableType, List tableInfoList);

  public void procedureDataAvailable(String catalog, List procedureInfoList);

  public void columnDataAvailable(TableInfo tableInfo);

  public void procedureColumnDataAvailable(ProcedureInfo procInfo);

  public void tableDetailDataAvailable(TableInfo tableInfo);

}
