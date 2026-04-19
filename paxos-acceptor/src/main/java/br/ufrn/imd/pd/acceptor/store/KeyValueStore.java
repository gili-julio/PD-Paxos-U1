package br.ufrn.imd.pd.acceptor.store;

import java.util.Optional;

/**
 * Interface do armazenamento chave-valor do Acceptor.
 *
 * Persiste apenas valores cujo consenso Paxos ja foi atingido (Fase 2 ACCEPTED).
 * Cada Acceptor mantem sua propria copia local - a consistencia entre replicas
 * e garantida pelo algoritmo Paxos, nao por sincronizacao direta entre Acceptors.
 */
public interface KeyValueStore {

    void put(String key, String value);

    Optional<String> get(String key);

    boolean containsKey(String key);

    int size();
}
