/*------------------------------------------------------------------------------
 - Copyright (c) 2020. This code follow the GPL v3 license scheme.
 -----------------------------------------------------------------------------*/

package it.aequinoxio;/**
 * Callback per aggiornare il chiamante durante il download
 */
public interface DownloadUpdateCallback {
    void update (String message);
}
