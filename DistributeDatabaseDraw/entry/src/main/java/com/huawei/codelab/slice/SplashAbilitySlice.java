package com.huawei.codelab.slice;

import com.huawei.codelab.MainAbility;
import com.huawei.codelab.ResourceTable;
import com.huawei.codelab.util.Util;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;

public class SplashAbilitySlice extends AbilitySlice {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_splash);
        getUITaskDispatcher().delayDispatch(() -> {
            Util.start(getAbility(), MainAbility.class);
            terminateAbility();
        }, 2000);
    }
}