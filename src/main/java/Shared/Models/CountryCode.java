package Shared.Models;

// Class to map JSON structure
public class CountryCode {
    private String code;
    private String country;

    // Default constructor (needed for Gson)
    public CountryCode() {}

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        if (code == null || country == null) return "Invalid Country Code";
        return "+" + code + " " + country;
    }
}
