package com.calclab.hablar.user.client.presence;

import static com.calclab.hablar.user.client.HablarUser.i18n;

import java.util.ArrayList;
import java.util.List;

import com.calclab.emite.core.client.xmpp.stanzas.Presence;
import com.calclab.emite.core.client.xmpp.stanzas.Presence.Show;
import com.calclab.emite.im.client.presence.PresenceManager;
import com.calclab.emite.im.client.presence.events.OwnPresenceChangedEvent;
import com.calclab.emite.im.client.presence.events.OwnPresenceChangedHandler;
import com.calclab.emite.xep.storage.client.IQResponse;
import com.calclab.hablar.core.client.mvp.HablarEventBus;
import com.calclab.hablar.core.client.page.PagePresenter;
import com.calclab.hablar.core.client.ui.icon.Icons;
import com.calclab.hablar.core.client.ui.icon.PresenceIcon;
import com.calclab.hablar.core.client.ui.menu.Menu;
import com.calclab.hablar.core.client.ui.menu.SimpleAction;
import com.calclab.hablar.user.client.EditorPage;
import com.calclab.hablar.user.client.storedpresence.StoredPresence;
import com.calclab.hablar.user.client.storedpresence.StoredPresenceManager;
import com.calclab.hablar.user.client.storedpresence.StoredPresences;
import com.calclab.suco.client.events.Listener;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

public class PresencePage extends PagePresenter<PresenceDisplay> implements EditorPage<PresenceDisplay> {
    public static final String TYPE = "Presence";
    public static final String ACTION_ID_AVAILABLE = "PresencePage-AvailableStatus";
    public static final String ACTION_ID_AVAILABLE_CUSTOM = "PresencePage-AvailableStatus-Custom";
    public static final String ACTION_ID_BUSY = "PresencePage-BusyStatus";
    public static final String ACTION_ID_BUSY_CUSTOM = "PresencePage-BusyStatus-Custom";
    public static final String ACTION_CLEAR_CUSTOM = "PresencePage-ClearCustom";
    private static int id = 0;
    private final PresenceManager presenceManager;
    private final StoredPresenceManager storedPresenceManager;
    private final Menu<PresencePage> statusMenu;
    private final ArrayList<SimpleAction<PresencePage>> defaultActions;
    private SimpleAction<PresencePage> clearCustomsAction;

    public PresencePage(final PresenceManager presenceManager, final StoredPresenceManager storageManager,
	    final HablarEventBus eventBus, final PresenceDisplay display) {
	super(TYPE, "" + ++id, eventBus, display);
	this.presenceManager = presenceManager;
	storedPresenceManager = storageManager;

	defaultActions = new ArrayList<SimpleAction<PresencePage>>();
	statusMenu = new Menu<PresencePage>(display.newStatusMenuDisplay("hablar-StatusItemMenu"));

	final String title = i18n().presencePageTitle();
	model.init(Icons.BUDDY, title, title);
	display.setStatusIcon(Icons.BUDDY_OFF);
	display.setPageTitle(i18n().presencePageTitle());
	createDefActions();
	updateMenu();

	final ClickHandler handler = new ClickHandler() {
	    @Override
	    public void onClick(final ClickEvent event) {
		event.preventDefault();
		final Element element = event.getRelativeElement();
		statusMenu.setTarget(PresencePage.this);
		statusMenu.show(element.getAbsoluteLeft(), element.getAbsoluteTop());
	    }
	};
	display.getMenu().addClickHandler(handler);
	display.getIcon().addClickHandler(handler);

	display.getStatus().addKeyDownHandler(new KeyDownHandler() {
	    @Override
	    public void onKeyDown(final KeyDownEvent event) {
		if (event.getNativeKeyCode() == 13) {
		    updateStatus(display);
		}
	    }

	});
	display.getStatusFocus().addBlurHandler(new BlurHandler() {
	    @Override
	    public void onBlur(final BlurEvent event) {
		updateStatus(display);
	    }
	});

	presenceManager.addOwnPresenceChangedHandler(new OwnPresenceChangedHandler() {
	    @Override
	    public void onOwnPresenceChanged(final OwnPresenceChangedEvent event) {
		final Presence presence = event.getCurrentPresence();
		showPresence(presence.getStatus(), presence.getShow());
	    }
	});
    }

    private void addCustomPresenceActions() {
	storedPresenceManager.get(new Listener<IQResponse>() {
	    @Override
	    public void onEvent(final IQResponse response) {
		if (response.isSuccess()) {
		    final List<StoredPresence> presences = StoredPresences.parse(response).get();
		    if (presences.size() > 0) {
			for (final StoredPresence presence : presences) {
			    final Show show = presence.getShow();
			    statusMenu.addAction(createCustomAction(presence.getStatus(), null, PresenceIcon.get(true,
				    show), presence.getStatus(), show));
			}
			statusMenu.addAction(clearCustomsAction);
		    }
		}
	    }
	});
    }

    private SimpleAction<PresencePage> createAction(final String label, final String actionId, final String iconType,
	    final String status, final Show show) {
	final boolean hasStatus = status != null;
	return new SimpleAction<PresencePage>(label, actionId, iconType) {
	    @Override
	    public void execute(final PresencePage target) {
		setPresence(status, show);
		display.setStatusEnabled(hasStatus);
		display.setStatusFocused(hasStatus);
	    }
	};
    }

    public SimpleAction<PresencePage> createCustomAction(final String title, final String id, final String icon,
	    final String status, final Show show) {
	return new SimpleAction<PresencePage>(title, id, icon) {
	    @Override
	    public void execute(final PresencePage target) {
		setPresence(status, show);
	    }
	};
    }

    private void createDefActions() {
	final String buddyIconOn = Icons.BUDDY_ON;
	final String buddyIconDnd = Icons.BUDDY_DND;
	final String closeIcon = Icons.CLOSE;

	defaultActions.add(createAction(i18n().available(), ACTION_ID_AVAILABLE, buddyIconOn, null, Show.chat));
	defaultActions.add(createAction(i18n().availableCustom(), ACTION_ID_AVAILABLE_CUSTOM, buddyIconOn, "",
		Show.chat));
	defaultActions.add(createAction(i18n().busy(), ACTION_ID_BUSY, buddyIconDnd, null, Show.dnd));
	defaultActions.add(createAction(i18n().busyCustom(), ACTION_ID_BUSY_CUSTOM, buddyIconDnd, "", Show.dnd));
	clearCustomsAction = new SimpleAction<PresencePage>(i18n().clearCustom(), ACTION_CLEAR_CUSTOM, closeIcon) {
	    @Override
	    public void execute(final PresencePage target) {
		storedPresenceManager.clearAll();
		updateMenu();
	    }
	};
    }

    @Override
    public void saveData() {
    }

    private void setPresence(final String status, final Show show) {
	showPresence(status, show);
	if (statusNotEmpty(status)) {
	    storedPresenceManager.add(status, show, new Listener<IQResponse>() {
		@Override
		public void onEvent(final IQResponse response) {
		    if (response.isSuccess()) {
			updateMenu();
		    }
		}
	    });
	}
	final Presence presence = presenceManager.getOwnPresence();
	presence.setStatus(status);
	presence.setShow(show);
	presenceManager.changeOwnPresence(presence);
    }

    private void setShowIcon(final Show show) {
	display.setStatusIcon(PresenceIcon.get(true, show));
    }

    @Override
    public void showData() {
	final Presence presence = presenceManager.getOwnPresence();
	showPresence(presence.getStatus(), presence.getShow());
    }

    private void showPresence(final String status, final Show show) {
	setShowIcon(show);
	display.getStatusText().setText(status);
    }

    private boolean statusNotEmpty(final String status) {
	return (status != null) && !status.isEmpty();
    }

    private void updateMenu() {
	statusMenu.clear();
	for (final SimpleAction<PresencePage> action : defaultActions) {
	    statusMenu.addAction(action);
	}
	addCustomPresenceActions();
    }

    private void updateStatus(final PresenceDisplay display) {
	setPresence(display.getStatusText().getText(), presenceManager.getOwnPresence().getShow());
    }

}
