/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package common;

import java.io.Serializable;

public class FamilyMember implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String relationship;
    private String icNumber;
    private String dateOfBirth;   
    
    public FamilyMember() {}
    
    public FamilyMember(String name, String relationship, String icNumber) {
        this.name = name;
        this.relationship = relationship;
        this.icNumber = icNumber;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getName() {
        return name;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setIcNumber(String icNumber) {
        this.icNumber = icNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
        @Override
    public String toString() {
        return name + " (" + relationship + ")";
    }
    
}
