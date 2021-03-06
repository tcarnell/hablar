package com.calclab.hablar.roster.client.groups;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.calclab.emite.core.client.xmpp.stanzas.XmppURI;
import com.calclab.emite.im.client.roster.RosterGroup;
import com.calclab.emite.xtesting.RosterItemHelper;
import com.calclab.hablar.core.client.ui.menu.Menu;
import com.calclab.hablar.core.client.util.ImmediateRunCommandScheduler;
import com.calclab.hablar.roster.client.RosterConfig;
import com.calclab.hablar.testing.EmiteTester;
import com.calclab.hablar.testing.HablarTester;

public class RosterGroupPresenterTests {

    private RosterGroupDisplay display;
    private Menu<RosterItemPresenter> itemMenu;
    private RosterGroup group;

    @Before
    public void setup() {
	new EmiteTester();
	final HablarTester hablar = new HablarTester();
	itemMenu = hablar.newMenu();
	final RosterItemDisplay itemDisplay = hablar.newDisplay(RosterItemDisplay.class);
	display = hablar.newDisplay(RosterGroupDisplay.class);
	when(display.newRosterItemDisplay(anyString(), anyString())).thenReturn(itemDisplay);
	group = new RosterGroup("mygroup");
	final RosterConfig rosterConfig = new RosterConfig();
	new RosterGroupPresenter(group, itemMenu, display, rosterConfig, new ImmediateRunCommandScheduler());
	group.add(RosterItemHelper.createItem("test1", "name1", "mygroup"));
	group.add(RosterItemHelper.createItem("test2", "name2", "mygroup"));
	group.add(RosterItemHelper.createItem("test3", "name3", "mygroup"));
	rosterConfig.oneClickChat = true;
	Mockito.reset(display);
    }

    @Test
    public void shouldAddWhenItemAdded() {
	group.add(RosterItemHelper.createItem("test4", "name4", "mygroup"));
	verify(display, times(1)).add((RosterItemDisplay) anyObject());
    }

    @Test
    public void shouldRemoveWhenItemRemoved() {
	group.remove(XmppURI.uri("test1"));
	verify(display, times(1)).remove((RosterItemDisplay) anyObject());
    }

}
