package me.auri.discordintegration;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
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

    private Collection<String> eventList = Collections.synchronizedCollection((Queue<String>) (new LinkedList<String>()));

    private boolean queueBlocker = false;
    
    public void sendEvent(String event, String content) {
    	if(Core.isDebug())
    		System.out.println("Adding to queue: " + event + ": " + content);
        int escapeCounter = 0;
        while(queueBlocker && escapeCounter < 1000) {
        	try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        	escapeCounter++;
        }
        if(escapeCounter >= 1000) {
        	System.out.println("[EST] Waited one second for queue to open but it never opened. Canceling this event!");
        	return;
        }
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
            System.out.println("[EST] I/O error: " + e.getMessage());
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


                if(!queueBlocker) {
                	queueBlocker = true;
                	Iterator<String> it = eventList.iterator();

                    while(it.hasNext()) {
                        String send = it.next();

                        System.out.println("Sending: " + send);
                        sendData(send);

                        it.remove();
                    }
                    queueBlocker = false;
                }

                // if(eventList.peek() != null) {
                //     System.out.println("Sending: " + eventList.peek());
                //     sendData(eventList.poll());
                // }

            }

            // TODO: Not encrypted?
            outToServer.writeBytes("ConnectionCloseEvent:" + syncname + TERMINATOR);
            outToServer.flush();

            socket.close();
 
        } catch (ConcurrentModificationException ex) {
        	
        	System.out.println(ChatColor.DARK_RED + "[EST] FFS, IT HAPPENED AGAIN! - FIX THIS BS M8! (ConcurrentModificationException)");
        	System.out.println("[EST] Restarting threads...");
        	// Reconnect
            Core.reconnectDIThreads();
        	
        } catch (UnknownHostException ex) {
 
            System.out.println("[EST] Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("[EST] I/O error: " + ex.getMessage());
            // Reconnect
            Core.reconnectDIThreads();
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
    
    public Collection<String> getEventQueue() {
        return eventList;
    }

    public void setEventQueue(Collection<String> q) {
        this.eventList.addAll(q);
    }

	public boolean isConnected() {
        if(socket == null) return false;
		return socket.isConnected();
	}

}