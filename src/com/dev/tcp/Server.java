package com.dev.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {
    private static final int PORT = 6500;
    private final String DATA_FILE = "data.in";
    private static final int MAX_BUFF_SIZE = 512;

    private static State state = State.NONE;
    private static DatagramSocket serverSocket;

    private static int ackNum = 0;
    private static int synNum = 0;
    private static int windowSize = 0;


    public static void main(String[] args) {
        try {
            serverSocket = new DatagramSocket(PORT);
            System.out.println("Liaison socket-port réussie" + "\n");
        } catch (SocketException e) {
            System.out.println("Liaison socket-port échouée");
            e.printStackTrace();
            return;
        }
        servExec();
    }

    private static void servExec() {
        while(true){
            byte[] buff = new byte[MAX_BUFF_SIZE];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                serverSocket.receive(packet);
                InetAddress clientAdr = packet.getAddress();
                int clientPort = packet.getPort();
                Packet tcpPacket = Packet.toPacket(new String(buff));

                if (state == State.NONE) { //Si la connexion est au début
                    if(tcpPacket.getSynFlag()){ //le paquet reçu doit etre un paquet SYN
                        System.out.println("Three way handshake 1/3");
                        System.out.println("** Paquet SYN reçu : ");
                        System.out.println("\t** Valeur de SYN = " + tcpPacket.getSynNum());
                        System.out.println("\t** Valeur de ACK = " + tcpPacket.getAckNum());

                        //On prépare un paquet ACK+SYN
                        System.out.println("** Creation du paquet ACK+SYN");
                        Packet ackSynPacket = new Packet();

                        System.out.println("\t** Flag ACK => true");
                        //On met le flag ACK à vrai
                        ackSynPacket.setAckFlag(true);
                        //On met la valeur du num ACK à SYN + 1
                        ackNum = tcpPacket.getSynNum() + 1;
                        System.out.println("\t** Valeur ACK => " + ackNum);
                        ackSynPacket.setAckNum(ackNum);

                        //On met le flag SYN à vrai
                        System.out.println("\t** Flag SYN => true");
                        ackSynPacket.setSynFlag(true);
                        //On donne une valeur aléatoire au num SYN
                        synNum = Utility.getRandomNumberInRange(1, 5000);
                        System.out.println("\t** Valeur SYN => " + synNum);
                        ackSynPacket.setSynNum(synNum);

                        //On envoie le paquet ACK+SYN
                        Utility.sendPacket(serverSocket, clientAdr, clientPort, ackSynPacket.toString());
                        state = State.SYN_RECV;
                        System.out.println("Three way Handshake 2/3");
                    }
                }
                    else if (state == State.SYN_RECV) { //Etape 2 du 3way
                        if(tcpPacket.getAckFlag() && tcpPacket.getAckNum() == synNum + 1) { //le paquet reçu doit etre un ACK et la valeur de ACK doit etre SYN+1
                            System.out.println("** Paquet ACK reçu : ");
                            System.out.println("\t** Valeur  de ACK = " + tcpPacket.getAckNum());
                            System.out.println("\t** Valeur de SYN = " + tcpPacket.getSynNum() + "\n");

                            state = State.ESTABLISHED;
                            System.out.println("Three way Handshake 3/3");

                            //On recupere les données qu'on veut envoyer

                            //On envoie
                        }
                    }
                    else if (state == State.ESTABLISHED) {
                        windowSize = tcpPacket.getWindowSize();
                    }
                    else if (state == State.FIN_SEND) {
                        System.out.println("Fin de l'envoi");
                    }


            } catch (IOException e) {
                System.out.println("Reception du paquet échouée");
                e.printStackTrace();
            }
        }
    }

}
