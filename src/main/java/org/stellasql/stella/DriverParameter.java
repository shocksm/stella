package org.stellasql.stella;

public class DriverParameter
{
  private String name;
  private String value;
  private boolean required = false;

  public DriverParameter(String name, String value, boolean required)
  {
    this.name = name;
    this.value = value;
    this.required = required;
  }

  public String getName()
  {
    return name;
  }

  public boolean getRequired()
  {
    return required;
  }

  public String getValue()
  {
    return value;
  }



}
