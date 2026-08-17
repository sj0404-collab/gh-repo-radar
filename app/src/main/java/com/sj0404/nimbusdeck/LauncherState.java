package com.sj0404.nimbusdeck;

/** Immutable snapshot shown on the dashboard. */
final class LauncherState {
    final boolean online;
    final boolean serviceInstalled;

    LauncherState(boolean online, boolean serviceInstalled) {
        this.online = online;
        this.serviceInstalled = serviceInstalled;
    }
}
