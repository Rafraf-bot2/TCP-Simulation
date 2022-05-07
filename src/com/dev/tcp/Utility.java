package com.dev.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Classe regroupant des méthodes annexes à l'échange client-serveur.
 */
@SuppressWarnings("ALL")
public class Utility {

    /**
     * Genere un nombre random compris entre min et max.
     * @param min borne inf
     * @param max born sup
     * @return min <= nombre random <= max
     */
    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max doit etre plus grand que min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * Envoie un datagrame.
     * @param socket socket qui envoie le datagrame
     * @param adr adresse de destination
     * @param port port de destination
     * @param msg contenu de data
     */
    public static void sendPacket(DatagramSocket socket, InetAddress adr, int port, String msg) {
        byte[] buff = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buff, buff.length, adr, port);
        try {
            socket.send(packet);
            System.out.println("\tPaquet envoyé : "+ Packet.toPacket(msg));
        } catch (IOException e) {
            System.out.println("Erreur d'envoi du paquet");
            e.printStackTrace();
        }
    }

    public static void displayPacket(Packet pkt, boolean isAccepted) {
        String accept = (isAccepted) ? "Paquet accepté : " : "Paquet reçu : ";
        System.out.println( accept + "SYNF=" + ((pkt.getSynFlag()) ? "1" : "0")
                +";SYNN="+pkt.getSynNum()
                +";ACKF="+((pkt.getAckFlag()) ? "1" : "0")
                +";ACKN="+pkt.getAckNum()
                +";FINF="+((pkt.getFinFlag()) ? "1" : "0")
                +";WINDOW="+pkt.getWindowSize()
                +";DATA="+pkt.getData().substring(0,4));
    }
}
