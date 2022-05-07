package com.dev.tcp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static final int PORT = 6500;
    private final String DATA_FILE = "data.in";
    private static final int MAX_BUFF_SIZE = 512;
    private static final String INPUT_DATA_FILE_NAME = "data.in";
    private static String INPUT_DATA = "";
    private static List<Packet> buffer = new ArrayList<Packet>();

    private static State state = State.NONE;
    private static DatagramSocket serverSocket;

    private static int ackNum = 0;
    private static int synNum = 0;
    private static int windowSize = 0;
    private static int nbrPaquet=12;
    private  static  int i=0;
    private  static  int  index=0;
    private static int segment_initial = 0;

    public static void main(String[] args) throws FileNotFoundException {
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

    private static void servExec() throws FileNotFoundException {
        while(true){
            byte[] buff = new byte[MAX_BUFF_SIZE];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                serverSocket.receive(packet);
                InetAddress clientAdr = packet.getAddress();
                int clientPort = packet.getPort();
                Packet tcpPacket = Packet.toPacket(new String(buff));

                //wait for 2seconds then print packet then process
                Thread t = timerThread(2);
                t.start();
                try{t.join();}catch(InterruptedException ie){}

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
                        if(tcpPacket.getAckFlag() && tcpPacket.getAckNum() == (++synNum)) { //le paquet reçu doit etre un ACK et la valeur de ACK doit etre SYN+1
                            System.out.println("** Paquet ACK reçu : ");
                            System.out.println("\t** Valeur  de ACK = " + tcpPacket.getAckNum());
                            System.out.println("\t** Valeur de SYN = " + tcpPacket.getSynNum() + "\n");

                            state = State.ESTABLISHED;
                            System.out.println("Three way Handshake 3/3 \n");
                            System.out.println("====================================== \n");
                            //On recupere les données qu'on veut envoyer
                            Scanner in = new Scanner(new File(INPUT_DATA_FILE_NAME));
                            while(in.hasNextLine()){
                                INPUT_DATA += in.nextLine();
                            }
                            in.close();
                            nbrPaquet = Integer.parseInt(tcpPacket.getData().split(",")[0]);
                            INPUT_DATA = INPUT_DATA.substring(0,nbrPaquet);

                            segment_initial = synNum;

                            //On envoie les premier carctere
                            Packet datum = new Packet();
                            datum.setSynNum(synNum);
                            int index = datum.getSynNum()- segment_initial;
                            datum.setData(""+INPUT_DATA.charAt(index));
                            Utility.sendPacket(serverSocket,clientAdr,clientPort, datum.toString());

                        }
                    }
                    else if (state == State.ESTABLISHED) {
                    Utility.displayPacket(tcpPacket, false);
                        windowSize = tcpPacket.getWindowSize();
                        if(tcpPacket.getAckFlag()) {
                            if (windowSize == 0) //Si le buffer du client est full
                                continue;

                            synNum = tcpPacket.getAckNum();

                            for (int i = 0; i < windowSize; i++) {
                                Packet dataPacket = new Packet();
                                dataPacket.setSynNum(synNum + i);

                                int index = dataPacket.getSynNum() - segment_initial;

                                if (index == INPUT_DATA.length()) { //Si on a envoyé toutes les paquets
                                    System.out.println("Les paquets sont envoyés");
                                    dataPacket = new Packet();
                                    dataPacket.setFinFlag(true);
                                    state = State.FIN_SEND;
                                    System.out.println("Four way handshake 1/4");
                                } else { // sinon
                                    try {
                                        dataPacket.setData("" + INPUT_DATA.charAt(index));
                                    } catch (StringIndexOutOfBoundsException ignore) {}
                                }
                                Utility.sendPacket(serverSocket, clientAdr, clientPort, dataPacket.toString());
                            }
                        }
                }
                else if (state == State.FIN_SEND) {
                    if(tcpPacket.getFinFlag() && tcpPacket.getAckNum() == 0) {
                        System.out.println("Fourway handshake 2/4");
                        System.out.println("Fourway handshake 3/4");
                        state = State.FIN_ACK;

                        Packet finAck = new Packet();
                        finAck.setAckFlag(true);
                        Utility.sendPacket(serverSocket, clientAdr, clientPort, finAck.toString());
                        System.out.println("Fourway handshake 4/4");
                        break;
                    }
                }
                else if (state == State.FIN_ACK) {
                    System.out.println("Transmission terminée, shutting down...");
                     t = timerThread(10);
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException ignored) {}
                    return;
                }


            } catch (IOException e) {
                System.out.println("Reception du paquet échouée");
                e.printStackTrace();
            }
        }
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
