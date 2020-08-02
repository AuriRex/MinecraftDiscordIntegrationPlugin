package me.auri.discordintegration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.ChatColor;

import me.auri.discordintegration.enc.EncMode;

public class EventSenderThread extends Thread {

    String host = "127.0.0.1";
    int port = 11001;
    String syncname = "TestServer";

    private EncMode enc = new EncMode();
    private Object[] enc_args = null;

    public static char TERMINATOR = '\n';

    EventSenderThread(String host, int port, String syncname) {
        this.host = host;
        this.port = port;
        this.syncname = syncname;
    }

    EventSenderThread(String host, int port, String syncname, EncMode mode, Object[] enc_args) {
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

    private boolean running = true;

    private Queue<String> eventList = new LinkedList<>();

    public void sendEvent(String event, String content) {
        System.out.println("Adding to queue: " + event + ": " + content);
        eventList.add(event + ": " + content);
    }

    private String sendData(String payloadData) {
        String ret = "";

        try {
            outToServer.writeBytes(enc.encrypt(payloadData, enc_args) + TERMINATOR);
            outToServer.flush();

            data = new StringBuilder();

            while ((character = reader.read()) != -1) {
                if ((char) character == TERMINATOR)
                    break;
                data.append((char) character);
            }

            // System.out.println(data);
            ret = enc.decrypt(data.toString(), enc_args);

        } catch (IOException e) {
            
            //e.printStackTrace();
            System.out.println("I/O error: " + e.getMessage());
            // Reconnect
            Core.reconnectDIThreads();
        }

        return ret;
    }

    public void run() {

        System.out.println(ChatColor.RED + "Starting EventSenderThread!");

        try {

            socket = new Socket(host, port);

            outToServer = new DataOutputStream(socket.getOutputStream());

            InputStream input = socket.getInputStream();
            reader = new InputStreamReader(input/* , Charset.forName("UTF-16LE") */);

            outToServer.writeBytes("Minecraft:" + syncname + ":true" + TERMINATOR);
            outToServer.flush();

            data = new StringBuilder();

            while ((character = reader.read()) != -1) {
                if ((char) character == TERMINATOR)
                    break;
                data.append((char) character);
            }

            System.out.println(ChatColor.RED + "EventSenderThread: " + ChatColor.GOLD + data);

            while (running) {

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }   

                Iterator<String> it = eventList.iterator();

                while(it.hasNext()) {
                    String send = it.next();

                    System.out.println("Sending: " + send);
                    sendData(send);

                    it.remove();
                }
                   
                

                // if(eventList.peek() != null) {
                //     System.out.println("Sending: " + eventList.peek());
                //     sendData(eventList.poll());
                // }

            }

            outToServer.writeBytes("ConnectionCloseEvent:" + syncname + TERMINATOR);
            outToServer.flush();

            socket.close();
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
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
    
    public Queue<String> getEventQueue() {
        return eventList;
    }

    public void setEventQueue(Queue<String> q) {
        this.eventList.addAll(q);
    }

	public boolean isConnected() {
        if(socket == null) return false;
		return socket.isConnected();
	}

}