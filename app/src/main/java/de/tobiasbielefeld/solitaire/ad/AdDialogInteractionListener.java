package de.tobiasbielefeld.solitaire.ad;

public interface AdDialogInteractionListener {
    /** Called when the timer finishes without user's cancellation. */
    void onShowAd();

    /** Called when the user clicks the "No, thanks" button or press the back button. */
    void onCancelAd();
}
