package com.expensesplitter.models;

import java.util.Objects;
// USER MODEL
public class User {

    private int    id;
    private String name;
    private String email;
    private String passwordHash;

    public User() {}

    public User(int id, String name, String email, String passwordHash) {
        this.id           = id;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
    }

    public User(String name, String email, String passwordHash) {
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
    }

    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getName()              { return name; }
    public void   setName(String name)   { this.name = name; }

    public String getEmail()             { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getPasswordHash()                    { return passwordHash; }
    public void   setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}