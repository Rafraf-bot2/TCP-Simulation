package com.dev.tcp;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private static final int PORT = 6502;
    private static final int SERVER_PORT = 6500;
    private static InetAddress server_adr ;
    private static final int MAX_BUFF_SIZE = 512;
    private static List<Packet> buffer = new ArrayList<Packet>();

    private static State state = State.NONE;
    private static DatagramSocket clientSocket;

    private static int ackNum = 0;
    private static int synNum = 0;
    private static int windowSize = 0;
    private static String data = "";

    public static void main(String[] args) {

        try {
            server_adr = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println("Erreur d'adresse de serveur");
            e.printStackTrace();
            return;
        }
        try {
            clientSocket = new DatagramSocket(PORT);
            //clientSocket.setSoTimeout();
            System.out.println("Liaison socket-port réussie" + "\n");
            clientSocket.setSoTimeout(10000);
        } catch (SocketException e) {
            System.out.println("Liaison socket-port échouée");
            e.printStackTrace();
            return;
        }

        Packet synPacket = new Packet();
        System.out.println("** Creation du paquet SYN");

        synPacket.setSynFlag(true);
        System.out.println("\t** Flag SYN => true");

        synNum = Utility.getRandomNumberInRange(1, 5000);
        synPacket.setSynNum(synNum);
        System.out.println("\t** Valeur de SYN = " + synNum);

        sendPacket(synPacket.toString());
        state = State.SYN_SEND;
        System.out.println("Three way handshake 1/3");
        clientExec();

    }

    private static void clientExec() {
        while(true) {
            byte[] buff = new byte[MAX_BUFF_SIZE];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                clientSocket.receive(packet);
                Packet tcpPacket = Packet.toPacket(new String(buff));
                if(state == State.SYN_SEND) { //le paquet doit etre un ACK+SYN
                    if(tcpPacket.getAckNum() == synNum + 1) { //la valeur de ACK doit etre SYN+1
                        System.out.println("** Paquet ACK+SYN reçu : ");
                        System.out.println("\t** Valeur de SYN = " + tcpPacket.getSynNum());
                        System.out.println("\t** Valeur de ACK = " + tcpPacket.getAckNum() + "\n");

                        System.out.println("Three way Handshake 2/3");

                        System.out.println("** Creation du paquet ACK");
                        Packet ackPacket = new Packet();

                        synNum = tcpPacket.getAckNum();
                        System.out.println("\t** Flag SYN => true");
                        ackPacket.setSynFlag(true);
                        System.out.println("\t** Valeur SYN => " + synNum);
                        ackPacket.setSynNum(synNum);

                        ackNum = tcpPacket.getSynNum() + 1;
                        System.out.println("\t** Flag ACK => true");
                        ackPacket.setAckFlag(true);
                        System.out.println("\t** Valeur ACK => " + ackNum);
                        ackPacket.setAckNum(ackNum);

                        windowSize = 4;
                        ackPacket.setWindowSize(windowSize);
                        System.out.println("\t** Valeur de WindowRCV => " + windowSize);

                        data = "42";
                        ackPacket.setData(data);
                        System.out.println("\t** Valeur de paquets total voulus  => " + data);

                        sendPacket(ackPacket.toString());
                        state = State.ESTABLISHED;

                        System.out.println("Three way Handshake 3/3");
                    }
                }
                else if (state == State.ESTABLISHED) {
                    System.out.println("eeee");
                }

            } catch (SocketTimeoutException e) {
                System.out.println("Le serveur est injoignable : " + e);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendPacket(String msg) {
        Utility.sendPacket(clientSocket, server_adr, SERVER_PORT, msg);
    }
}
