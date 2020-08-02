package me.auri.discordintegration.enc;

public class EncModeAES extends EncMode {
    

    public String encrypt(String input, Object[] args) {
        return AES.encrypt(input, "" + args[0]);
    }

    public String decrypt(String input, Object[] args) {
        return AES.decrypt(input, "" + args[0]);
    }

}