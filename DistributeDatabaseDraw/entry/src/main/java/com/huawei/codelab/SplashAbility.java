package com.huawei.codelab;

import com.huawei.codelab.slice.SplashAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class SplashAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(SplashAbilitySlice.class.getName());
    }
}
