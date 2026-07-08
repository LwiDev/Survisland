package com.lwidev.survisland.menu;

import com.lwidev.survisland.Survisland;
import com.lwidev.survisland.game.AnnouncementService;
import com.lwidev.survisland.game.TimerService;
import com.lwidev.survisland.game.VoteService;
import com.lwidev.survisland.services.PauseManager;
import com.lwidev.survisland.teams.TeamManager;

/** Bundles the services every /menu page may need, so page constructors take one argument instead of five. */
public record MenuContext(Survisland plugin, TeamManager teamManager, AnnouncementService announcementService, TimerService timerService, VoteService voteService, PauseManager pauseManager) {
}
