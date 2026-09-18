package bankmanagement.model;

public class Customer {

    private int customerId;
    private String name;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private String email;
    private String idNumber;

    public Customer(int customerId, String name, String dateOfBirth,
                    String gender, String address, String phone,
                    String email, String idNumber) {

        this.customerId = customerId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.idNumber = idNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void displayCustomerDetails() {
        System.out.println("Customer ID   : " + customerId);
        System.out.println("Name          : " + name);
        System.out.println("Date of Birth : " + dateOfBirth);
        System.out.println("Gender        : " + gender);
        System.out.println("Address       : " + address);
        System.out.println("Phone         : " + phone);
        System.out.println("Email         : " + email);
        System.out.println("ID Number     : " + idNumber);
    }
}
