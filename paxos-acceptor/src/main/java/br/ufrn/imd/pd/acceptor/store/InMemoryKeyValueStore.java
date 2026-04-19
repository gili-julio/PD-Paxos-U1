package br.ufrn.imd.pd.acceptor.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementacao em memoria do KeyValueStore.
 *
 * Usa ConcurrentHashMap para suportar leituras e escritas concorrentes
 * sem lock global. Thread-safe para as operacoes definidas na interface.
 *
 * Em uma implementacao de producao, este store poderia ser substituido por
 * uma versao persistente (arquivo, banco de dados, etc.) sem modificar
 * nenhum codigo que depende da interface KeyValueStore.
 */
public class InMemoryKeyValueStore implements KeyValueStore {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryKeyValueStore.class);

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String value) {
        store.put(key, value);
        logger.debug("KV store: PUT key='{}' value='{}'", key, value);
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public boolean containsKey(String key) {
        return store.containsKey(key);
    }

    @Override
    public int size() {
        return store.size();
    }
}
