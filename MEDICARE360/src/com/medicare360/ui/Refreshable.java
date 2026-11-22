package com.medicare360.ui;

/** Implemented by every screen that must reload its data from the database whenever it is shown. */
public interface Refreshable {
    void refresh();
}
