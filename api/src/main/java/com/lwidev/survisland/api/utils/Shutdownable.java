package com.lwidev.survisland.api.utils;

/** A manager/service holding scheduled tasks or resources that must be released on plugin disable. */
public interface Shutdownable {
    void shutdown();
}
