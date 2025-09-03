package Shared.Models;

public class CountryCode {
    private String country;
    private String code;
    private String iso;
    private String format; // New field for the phone number format

    public CountryCode(){

    }
    // Getters
    public String getCountry() {
        return country;
    }

    public String getCode() {
        return code;
    }

    public String getIso() {
        return iso;
    }

    public String getFormat() {
        return format;
    }

    // Setters
    public void setCountry(String country) {
        this.country = country;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public void setFormat(String format) {
        this.format = format;
    }
    @Override
    public String toString() {
        if (code == null || country == null) return "Invalid Country Code";
        return "+" + code + " " + country;
    }
}