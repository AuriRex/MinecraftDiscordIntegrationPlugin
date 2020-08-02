package me.auri.discordintegration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.auri.discordintegration.enc.EncMode;

public class EventReceiverThread extends Thread {

    String host = "127.0.0.1";
    int port = 11001;
    String syncname = "TestServer";

    private EncMode enc = new EncMode();
    private Object[] enc_args = null;

    public static char TERMINATOR = '\n';

    EventReceiverThread(String host, int port, String syncname) {
        this.host = host;
        this.port = port;
        this.syncname = syncname;
    }

    EventReceiverThread(String host, int port, String syncname, EncMode mode, Object[] enc_args) {
        this.host = host;
        this.port = port;
        this.syncname = syncname;
        this.enc = mode;
        this.enc_args = enc_args;
    }

    private Socket socket;
    private DataOutputStream outToServer;
    private InputStreamReader reader;
    private StringBuilder data;
    private int character;
    private String ret;


    private boolean running = true;

    public String handleRequest(String req) {

        if(req.startsWith("DiscordChatEvent: ")) {

            String[] _req = req.replaceFirst("DiscordChatEvent: ", "").split(";");

            String name = _req[0];
            String msg = _req[1];

            Bukkit.broadcastMessage(Core.var.get("discordintegration.chat.prefix") + name + Core.var.get("discordintegration.chat.suffix") + Core.var.get("discordintegration.chat.color") + msg);

            return "rec";
        }

        if (req.startsWith("PlayingStatusEvent: ")) {

            String playerList = "";
            for (Player ply : Bukkit.getOnlinePlayers()) {
                playerList += ";" + ply.getName();
            }
            playerList = playerList.substring(1);

            return playerList;
        }

        return "TODO: Not Implemented";
    }

    public void run() {

        System.out.println(ChatColor.RED + "Starting EventReceiverThread!");

        try {

            socket = new Socket(host, port);

            outToServer = new DataOutputStream(socket.getOutputStream());

            InputStream input = socket.getInputStream();
            reader = new InputStreamReader(input/* , Charset.forName("UTF-16LE") */);

            outToServer.writeBytes("Minecraft:" + syncname + ":false" + TERMINATOR);
            outToServer.flush();

            data = new StringBuilder();

            while ((character = reader.read()) != -1) {
                if ((char) character == TERMINATOR)
                    break;
                data.append((char) character);
            }

            System.out.println(ChatColor.RED + "EventReceiverThread: " + ChatColor.GOLD + data);
            String decrypted_data = "";
            while (running) {


                data = new StringBuilder();

                while ((character = reader.read()) != -1) {
                    if ((char) character == TERMINATOR)
                        break;
                    if(!running) {
                        socket.close();
                        return;
                    }
                    data.append((char) character);
                }

                decrypted_data = enc.decrypt(data.toString(), enc_args);

                if(decrypted_data.toString().startsWith("ConnectionCloseEvent")) {
                    Core.closeDIThreads();
                    return;
                }

                System.out.println("[ERT] Received: " + decrypted_data);
                ret = handleRequest(decrypted_data);

                outToServer.writeBytes(enc.encrypt(ret, enc_args) + TERMINATOR);
                outToServer.flush();

            }

            socket.close();
 
        } catch (UnknownHostException ex) {
 
            System.out.println(ChatColor.RED + "[ERT] Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println(ChatColor.RED + "[ERT] I/O error: " + ex.getMessage());
        }


    }

    public void stopNow() {
        running = false;
    }

	public void close() {
        stopNow();
        try {
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}