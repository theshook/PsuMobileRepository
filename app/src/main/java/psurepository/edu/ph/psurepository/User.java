package psurepository.edu.ph.psurepository;

public class User {
  String email;
  String password;
  String firstName;
  String lastName;
  String area;
  String timestamp;

  public User() {

  }

  public User(String email, String password, String firstName, String lastName, String area, String timestamp) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.area = area;
    this.timestamp = timestamp;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getArea() {
    return area;
  }

  public String getTimestamp() {
    return timestamp;
  }
}
