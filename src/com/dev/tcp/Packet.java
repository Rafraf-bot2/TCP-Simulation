package com.dev.tcp;

/**
 * Classe implémentant un paquet TCP.
 * On ajoute les champs manquants à un datagramme UDP afin de le rendre fiable
 */
public class Packet {
    private int synNum;
    private int ackNum;
    private boolean synFlag;
    private boolean ackFlag;
    private boolean finFlag;
    private int windowSize;
    private String data;

    public Packet() {
        this.synNum = 0;
        this.ackNum = 0;
        this.synFlag = false;
        this.ackFlag = false;
        this.finFlag = false;
        this.windowSize = 0;
        this.data = null;
    }

    public Packet(int synNum, int ackNum, boolean synFlag,
                  boolean ackFlag, boolean finFlag, int windowSize,
                  String data) {
        this.synNum = synNum;
        this.ackNum = ackNum;
        this.synFlag = synFlag;
        this.ackFlag = ackFlag;
        this.finFlag = finFlag;
        this.windowSize = windowSize;
        this.data = data;
    }

    public int getSynNum() {
        return synNum;
    }

    public void setSynNum(int synNum1) {
        synNum = synNum1;
    }

    public int getAckNum() {
        return ackNum;
    }

    public void setAckNum(int ackNum1) {
        ackNum = ackNum1;
    }

    public boolean getSynFlag() {
        return synFlag;
    }

    public void setSynFlag(boolean synFlag1) {
        synFlag = synFlag1;
    }

    public boolean getAckFlag() {
        return ackFlag;
    }

    public void setAckFlag(boolean ackFlag1) {
        ackFlag = ackFlag1;
    }

    public boolean getFinFlag() {
        return finFlag;
    }

    public void setFinFlag(boolean finFlag1) {
        finFlag = finFlag1;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize1) {
        windowSize = windowSize1;
    }

    public String getData() {
        return data;
    }

    public void setData(String data1) {
        data = data1;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String toString() {
        return "SYNF=" + (synFlag ? 1 : 0) + ";" +
                "SYNN=" + synNum + ";" +
                "ACKF=" + (ackFlag ? 1 : 0) + ";" +
                "ACKN=" + ackNum + ";" +
                "FINF=" + (finFlag ? 1 : 0) + ";" +
                "WINDOW=" + windowSize + ";" +
                "DATA=" + data;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static Packet toPacket(String packetString) {
        Packet packet = new Packet();
        String[] packetTab = packetString.split(";");

        for (String field : packetTab) {

            String[] value = field.split("=");
            switch (value[0]) {
                case "SYNF":
                    packet.setSynFlag(value[1].equals("1"));
                    break;
                case "SYNN":
                    packet.setSynNum(Integer.parseInt(value[1]));
                    break;
                case "ACKF":
                    packet.setAckFlag(value[1].equals("1"));
                    break;
                case "ACKN":
                    packet.setAckNum(Integer.parseInt(value[1]));
                    break;
                case "FINF":
                    packet.setFinFlag(value[1].equals("1"));
                    break;
                case "WINDOW":
                    packet.setWindowSize(Integer.parseInt(value[1]));
                    break;
                case "DATA":
                    packet.setData(value[1]);
                    break;
            }
        }
        return packet;
    }

    public static void main(String[] args) {
        Packet p = new Packet(2, 2, true,
                true, false, 9,
                "caca");
        String pString = p.toString();
        Packet p2 = new Packet();

        System.out.println(p2);
        p2 = toPacket(pString);
        System.out.println(p2);
    }
}

