package org.teamchat.App;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Johnny
 * Infinte Scroll per linear layout
 */
public abstract class InfiniteScroll extends RecyclerView.OnScrollListener {
    // L'indice corrente dei dati caricati
    private int currentPage = 0;
    // Il numero totali degli elementi dopo l'ultimo caricamento
    private int previousTotalItemCount = 0;
    // Vero se stiamo ancora caricando.
    private boolean loading = true;

    RecyclerView.LayoutManager mLayoutManager;

    public InfiniteScroll(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    // Attenzione al codice che si scrive qui dento,
    // perché verra eseguite molte volte durante lo scroll.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition;
        int totalItemCount = mLayoutManager.getItemCount();

        lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager)
                .findLastVisibleItemPosition();

        // Se il totale degli elementi è zero, ma il precendete non lo sia, errore quindi refresh
        // allo stato iniziale
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = 0;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0)
                this.loading = true;
        }

        // Se stiamo ancora caricando, guardiamo se il numero degli elementi è cambiato
        // se si, allora concludiamo il caricamento e aggiorniamo il l'ultimo totale
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // Se non stiamo caricando, controlliamo se abbiamo passato sopra al 5 elemento dalla fine
        // Notare il 5!!
        // Se vero, aumentiamo l'indice corrente, lanciamo la funzione di caricamento
        // e avvertiamo che stiamo caricando.
        // quindi nessun altro caricamento potra avvenire finche questo non sia finito.

        if (!loading && (lastVisibleItemPosition + 5) > totalItemCount) {
            currentPage++;
            onLoadMore(currentPage, totalItemCount);
            loading = true;
        }
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount);
}
