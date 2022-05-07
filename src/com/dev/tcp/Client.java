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
    private static String DATA = "";

    //donne pour le nombre n de paquet vouli
    private static String data = "";
    private static int segment_initial = 0;

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
                //wait for 2seconds then print packet then process
                Thread t = timerThread(2);
                t.start();
                try{t.join();}catch(InterruptedException ie){}

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

                        data = "8,";
                        ackPacket.setData(data);
                        System.out.println("\t** Valeur de paquets total voulus  => " + data);

                        sendPacket(ackPacket.toString());
                        state = State.ESTABLISHED;
                        System.out.println("Three way Handshake 3/3 \n");
                        System.out.println("====================================== \n");
                        synNum =  segment_initial = ackNum;
                        synNum--;
                    }
                }
                else if (state == State.ESTABLISHED) {
                    Packet ack = new Packet();

                    if(tcpPacket.getSynNum() > synNum) { //si le numseq du paquet reçu est superieur au numsec du dernier paquet accepté
                        Utility.displayPacket(tcpPacket, true);
                        buffer.add(tcpPacket);

                        ack.setAckFlag(true);
                        ack.setAckNum(tcpPacket.getSynNum()+1);
                        ack.setWindowSize(windowSize- buffer.size());
                        sendPacket(ack.toString());
                    }

                    if(buffer.size() == windowSize || tcpPacket.getFinFlag()) {
                        char[] contenu = new char[buffer.size()];
                        for(Packet pkt : buffer) {
                            if(pkt.getSynNum() <= synNum)
                                continue;
                            contenu[pkt.getSynNum() - synNum - 1] = pkt.getData().charAt(0);
                        }

                        for (int i = 0; i < buffer.size(); i++) {
                            if(contenu[i] == '\0')
                                break;
                            else
                                DATA += "" + contenu[i];
                        }

                        System.out.println("Donnée traitée : " + DATA);

                        buffer.clear();

                        if(tcpPacket.getFinFlag()) {
                            System.out.println("Fourway Handshake 1/4");
                            state = State.FIN_RECV;

                            ack = new Packet();
                            ack.setFinFlag(true);
                            ack.setAckFlag(true);
                            sendPacket(ack.toString());
                            System.out.println("Fourway handshake 2/4");
                            System.out.println("Fourway handshake 3/4");

                        } else {
                            ack.setAckNum(segment_initial+DATA.length());
                            ack.setWindowSize(windowSize- buffer.size());
                            sendPacket(ack.toString());
                        }
                    }
                    synNum = segment_initial + DATA.length() - 1;
                }

            else if(state == State.FIN_RECV){
                if(tcpPacket.getAckFlag() && tcpPacket.getAckNum()==0){
                    System.out.println("Fourway handshake 4/4");
                    System.out.println("Transmission terminée, shutting down...");
                    t = timerThread(30);
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException ignored) {}
                    return;
                }
            }

            } catch (SocketTimeoutException e) {
                System.out.println("Le serveur est injoignable : " + e.getMessage());
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendPacket(String msg) {
        Utility.sendPacket(clientSocket, server_adr, SERVER_PORT, msg);
    }

    private static Thread timerThread(final int seconds){
        return new Thread(new Runnable(){
            @Override
            public void run(){
                try{

                    Thread.sleep(seconds*1000);
                }catch(InterruptedException ie){

                }
            }
        });
    }
}


