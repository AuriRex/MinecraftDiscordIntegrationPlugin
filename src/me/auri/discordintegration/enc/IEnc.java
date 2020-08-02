package me.auri.discordintegration.enc;

public interface IEnc {
    
    public String encrypt(String input, Object[] args);

    public String decrypt(String input, Object[] args);

}