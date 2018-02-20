package com.sergiocruz.mostpopularmovies;

/**
 * Created by Sergio on 20/02/2018.
 */

public class Interfaces {

    OnInternetOffInterface internetOffInterface;

    Interfaces(OnInternetOffInterface internetOffInterface) {
        this.internetOffInterface = internetOffInterface;
    }

    interface OnInternetOffInterface {
        void onInternetOff();
    }
}
