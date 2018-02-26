package com.sergiocruz.mostpopularmovies.Utils;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Sergio on 26/02/2018.
 */

public class CoordinatorBehaviors extends CoordinatorLayout.Behavior<ImageView> {
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        return dependency instanceof Toolbar;
    }

    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView avatar, View dependency) {
        modifyAvatarDependingDependencyState(avatar, dependency);
        return true;
    }

    private void modifyAvatarDependingDependencyState(ImageView avatar, View dependency) {
        //  avatar.setY(dependency.getY());
        //  avatar.setBlahBlah(dependency.blah / blah);
    }
}

