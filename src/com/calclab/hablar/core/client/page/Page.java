package com.calclab.hablar.core.client.page;

import com.calclab.hablar.core.client.mvp.Display;
import com.calclab.hablar.core.client.mvp.Presenter;
import com.calclab.hablar.core.client.page.PagePresenter.Visibility;

/**
 * All page presenters must implements this interface
 * 
 */
public interface Page<T extends Display> extends Presenter<T> {

    /**
     * All pages have a unique PageID reference
     */
    public String getId();

    public PageState getState();

    public String getType();

    public Visibility getVisibility();

    /**
     * request visibility
     */
    @Deprecated
    public void requestFocus();

    @Deprecated
    public void requestHide();

    public void setVisibility(Visibility visibility);

    void requestVisibility(Visibility newVisibility);

}