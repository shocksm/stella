package org.stellasql.stella;

public class RegistrationData
{
  private String keyCode = "";
  private String name = "";
  private String address = "";
  private String email = "";

  public String getAddress()
  {
    return address;
  }
  public void setAddress(String address)
  {
    this.address = address;
  }
  public String getEmail()
  {
    return email;
  }
  public void setEmail(String email)
  {
    this.email = email;
  }
  public String getKeyCode()
  {
    return keyCode;
  }
  public void setKeyCode(String keyCode)
  {
    this.keyCode = keyCode;
  }
  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name = name;
  }

}
