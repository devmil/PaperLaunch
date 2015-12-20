package de.devmil.paperlaunch.storage;

public interface ITransactionAction {
    /**
     * gets called while an transaction is active.
     *
     * @param context provides access to database operations during the transaction
     */
    void execute(ITransactionContext transactionContext);
}
