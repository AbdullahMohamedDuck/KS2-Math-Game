package com.game.ks2mathgame.account;

class User {
    public String name, email;
    long id;
    User(String name, String email){
        id = System.currentTimeMillis();
        this.email = email;
        this.name = name;
    }
}
