package com.huawei.codelab.util;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.IAbilityConnection;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.data.distributed.common.KvManagerConfig;
import ohos.data.distributed.common.KvManagerFactory;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/**
 * 工具类
 */
public class Util {
    private static final HiLogLabel LABEL = new HiLogLabel(HiLog.LOG_APP, 0, "lxlog");

    /**
     * 日志
     */
    public static void log(Object tag, Object... content) {
        String s = tag.getClass().getSimpleName();
        for (Object o : content) {
            s += " " + (o == null ? null : o.toString());
        }
        HiLog.info(LABEL, s);
    }

    /**
     * 吐司
     */
    public static void toast(Context context, String s) {
        new ToastDialog(context).setAlignment(LayoutAlignment.CENTER).setText(s).show();
    }

    /**
     * 启动Ability
     */
    public static void start(AbilityContext context, Class<? extends Ability> abilityClass) {
        start(context, abilityClass, 0, null);
    }

    /**
     * 启动Ability
     */
    public static void start(AbilityContext context, Class<? extends Ability> abilityClass, int requestCode, IntentParams params) {
        start(context, null, context.getBundleName(), abilityClass.getName(), requestCode, params);
    }

    /**
     * 启动Ability（可跨设备，跨app）
     */
    public static void start(AbilityContext context, String deviceId, String bundleName, String abilityName, int requestCode, IntentParams params) {
        letAbility(true, null, context, deviceId, bundleName, abilityName, requestCode, params);
    }

    /**
     * 停止Ability（可跨设备，跨app）
     */
    public static void stop(AbilityContext context, String deviceId, String bundleName, String abilityName) {
        letAbility(false, null, context, deviceId, bundleName, abilityName, 0, null);
    }


    /**
     * 连接Ability（可跨设备，跨app）
     */
    public static void connect(IAbilityConnection connection, AbilityContext context, String deviceId, String bundleName, String abilityName) {
        letAbility(true, connection, context, deviceId, bundleName, abilityName, 0, null);
    }

    /**
     * 启动/停止/连接 Ability（可跨设备，跨app）
     */
    public static void letAbility(boolean startOrStop, IAbilityConnection connection, AbilityContext context, String deviceId, String bundleName, String abilityName, int requestCode, IntentParams params) {
        Intent intent = new Intent();
        intent.setParams(params);
        String localDeviceId = KvManagerFactory.getInstance()
                .createKvManager(new KvManagerConfig(context)).getLocalDeviceInfo().getId();
        intent.setParam("localDeviceId", localDeviceId);
        Intent.OperationBuilder builder = new Intent.OperationBuilder()
                .withDeviceId(deviceId)
                .withBundleName(bundleName)
                .withAbilityName(abilityName);
        if (deviceId != null && !deviceId.isEmpty()) { // 传入其他设备id时，自动添加flag
            builder.withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE);
        }
        intent.setOperation(builder.build());
        if (connection == null) {
            if (startOrStop) {
                context.startAbility(intent, requestCode);
            } else {
                context.stopAbility(intent);
            }
        } else {
            context.connectAbility(intent, connection);
        }
    }
}
