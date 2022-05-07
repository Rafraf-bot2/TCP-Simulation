package com.dev.tcp;

/**
 * Enum qui représente l'état de la connection entre le client et le serveur.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum State {
    NONE, //0 - la connexion n'est pas encore établie
    SYN_SEND, //1 - le client à envoyé le paquet SYN, il attend le SYN+ACK
    SYN_RECV,  //2 - le serveur à envoyé le SYN+ACK, il attend le ACK
    ESTABLISHED, //3 - Le 3way à réussi
    FIN_SEND, //4 - Le serveur à fini d'envoyer les données
    FIN_RECV,
    FIN_ACK, //
    DISCONNECTED, //
}
