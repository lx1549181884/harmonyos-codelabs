/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.codelab.slice;

import com.huawei.codelab.MainAbility;
import com.huawei.codelab.ResourceTable;
import com.huawei.codelab.bean.GameInfo;
import com.huawei.codelab.component.DeviceSelectDialog;
import com.huawei.codelab.component.DrawPoint;
import com.huawei.codelab.util.GameUtil;
import com.huawei.codelab.util.GsonUtil;
import com.huawei.codelab.util.LogUtils;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.bundle.IBundleManager;
import ohos.data.distributed.common.*;
import ohos.data.distributed.user.SingleKvStore;
import ohos.multimodalinput.event.TouchEvent;

import java.util.List;

import static ohos.agp.components.ComponentContainer.LayoutConfig.MATCH_PARENT;
import static ohos.security.SystemPermission.DISTRIBUTED_DATASYNC;

/**
 * MainAbilitySlice
 *
 * @since 2021-04-06
 */
public class MainAbilitySlice extends AbilitySlice {
    private static final String TAG = MainAbilitySlice.class.getName();
    private static final int PERMISSION_CODE = 20201203;
    private static final int DELAY_TIME = 10;
    private static final String STORE_ID_KEY = "storeId";
    private static final String COLOR_INDEX_KEY = "colorIndex";
    private static final String IS_FORM_LOCAL_KEY = "isFormLocal";
    private static final String GAME_INFO_STATE = "gameInfoState";
    private static final String GAME_INFO_RED_BOARD_X = "gameInfoRedBoardX";
    private static final String GAME_INFO_BLUE_BOARD_X = "gameInfoBlueBoardX";
    private static final String GAME_INFO_BALL = "gameInfoBall";
    private static String storeId;
    private DependentLayout canvas;
    private Image transform;
    private KvManager kvManager;
    private SingleKvStore singleKvStore;
    private Text title;
    private Button btn;
    private DrawPoint drawl;
    private boolean isLocal;
    private GameUtil.Interface gameUtilInterface;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        storeId = STORE_ID_KEY + System.currentTimeMillis();
        isLocal = !intent.getBooleanParam(IS_FORM_LOCAL_KEY, false);
        findComponentById();
        requestPermission();
        initView(intent);
        initDatabase();
        initDraw();
        gameUtilInterface = new GameUtil.Interface() {
            @Override
            public GameInfo getGameInfo() {
                return MainAbilitySlice.this.getGameInfo();
            }

            @Override
            public void saveBall(GameInfo.Ball ball) {
                MainAbilitySlice.this.saveBall(ball);
            }

            @Override
            public void saveBoard(boolean isRedOrBlue, float x) {
                MainAbilitySlice.this.saveBoard(isRedOrBlue, x);
            }

            @Override
            public void saveState(int state) {
                MainAbilitySlice.this.saveState(state);
            }
        };
        GameUtil.init(gameUtilInterface);
    }

    private void initView(Intent intent) {
        if (!isLocal) {
            storeId = intent.getStringParam(STORE_ID_KEY);
        }
        title.setText(isLocal ? "本地端" : "远程端");
        transform.setVisibility(isLocal ? Component.VISIBLE : Component.INVISIBLE);
    }

    private void requestPermission() {
        if (verifySelfPermission(DISTRIBUTED_DATASYNC) != IBundleManager.PERMISSION_GRANTED) {
            if (canRequestPermission(DISTRIBUTED_DATASYNC)) {
                requestPermissionsFromUser(new String[]{DISTRIBUTED_DATASYNC}, PERMISSION_CODE);
            }
        }
    }

    private void findComponentById() {
        if (findComponentById(ResourceTable.Id_canvas) instanceof DependentLayout) {
            canvas = (DependentLayout) findComponentById(ResourceTable.Id_canvas);
        }
        if (findComponentById(ResourceTable.Id_transform) instanceof Image) {
            transform = (Image) findComponentById(ResourceTable.Id_transform);
        }
        if (findComponentById(ResourceTable.Id_title) instanceof Text) {
            title = (Text) findComponentById(ResourceTable.Id_title);
        }
        if (findComponentById(ResourceTable.Id_btn) instanceof Button) {
            btn = (Button) findComponentById(ResourceTable.Id_btn);
            if (isLocal) {
                btn.setClickedListener(component -> {
                    initGameInfo();
                    GameUtil.start();
                });
            }
        }
        transform.setClickedListener(component -> {
            DeviceSelectDialog dialog = new DeviceSelectDialog(MainAbilitySlice.this);
            dialog.setListener(deviceIds -> {
                if (deviceIds != null && !deviceIds.isEmpty()) {
                    // 启动远程页面
                    startRemoteFas(deviceIds);
                    // 同步远程数据库
                    singleKvStore.sync(deviceIds, SyncMode.PUSH_ONLY);
                }
                dialog.hide();
            });
            dialog.show();
        });
    }

    private void initDraw() {
        drawl = new DrawPoint(this);
        drawl.setHeight(MATCH_PARENT);
        drawl.setWidth(MATCH_PARENT);
        canvas.addComponent(drawl);

        if (isLocal) {
            initGameInfo();
        } else {
            drawPoints();
        }

        drawl.setTouchEventListener(new Component.TouchEventListener() {
            int lastX = 0;

            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                int currentX = (int) touchEvent.getPointerPosition(touchEvent.getIndex()).getX();
                LogUtils.info(TAG, "onTouchEvent action=" + touchEvent.getAction() + " lastX=" + lastX + " currentX=" + currentX);
                switch (touchEvent.getAction()) {
                    case TouchEvent.PRIMARY_POINT_DOWN:
                        lastX = currentX;
                        break;
                    case TouchEvent.POINT_MOVE:
                    case TouchEvent.PRIMARY_POINT_UP:
                        int diffX = currentX - lastX;
                        lastX = currentX;
                        GameUtil.moveBoard(isLocal, diffX);
                        break;
                }
                return true;
            }
        });
    }

    // 获取数据库中的数据，并在画布上画出来
    private void drawPoints() {
        getUITaskDispatcher().delayDispatch(() -> {
            GameInfo gameInfo = getGameInfo();
            drawl.setDrawParams(gameInfo);
            if (isLocal) {
                switch (gameInfo.state) {
                    case 0:
                        btn.setText("点击开始");
                        btn.setVisibility(Component.VISIBLE);
                        break;
                    case 2:
                        btn.setText("红方胜，点击重新开始");
                        btn.setVisibility(Component.VISIBLE);
                        break;
                    case 3:
                        btn.setText("蓝方胜，点击重新开始");
                        btn.setVisibility(Component.VISIBLE);
                        break;
                    case 1:
                    default:
                        btn.setText("");
                        btn.setVisibility(Component.HIDE);
                        break;
                }
            } else {
                switch (gameInfo.state) {
                    case 2:
                        btn.setText("红方胜");
                        btn.setVisibility(Component.VISIBLE);
                        break;
                    case 3:
                        btn.setText("蓝方胜");
                        btn.setVisibility(Component.VISIBLE);
                        break;
                    default:
                        btn.setText("");
                        btn.setVisibility(Component.HIDE);
                        break;
                }
            }
        }, DELAY_TIME);
    }

    /**
     * Receive database messages
     *
     * @since 2021-04-06
     */
    private class KvStoreObserverClient implements KvStoreObserver {
        @Override
        public void onChange(ChangeNotification notification) {
            LogUtils.info(TAG, "data changed......");
            drawPoints();
        }
    }

    private void initDatabase() {
        // 创建分布式数据库管理对象
        KvManagerConfig config = new KvManagerConfig(this);
        kvManager = KvManagerFactory.getInstance().createKvManager(config);
        // 创建分布式数据库
        Options options = new Options();
        options.setCreateIfMissing(true).setEncrypt(false).setKvStoreType(KvStoreType.SINGLE_VERSION);
        singleKvStore = kvManager.getKvStore(options, storeId);
        // 订阅分布式数据变化
        KvStoreObserver kvStoreObserverClient = new KvStoreObserverClient();
        singleKvStore.subscribe(SubscribeType.SUBSCRIBE_TYPE_ALL, kvStoreObserverClient);
    }

    /**
     * Starting Multiple Remote Fas
     *
     * @param deviceIds deviceIds
     */
    private void startRemoteFas(List<String> deviceIds) {
        Intent[] intents = new Intent[deviceIds.size()];
        for (int i = 0; i < deviceIds.size(); i++) {
            Intent intent = new Intent();
            intent.setParam(IS_FORM_LOCAL_KEY, true);
            intent.setParam(COLOR_INDEX_KEY, i + 1);
            intent.setParam(STORE_ID_KEY, storeId);
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId(deviceIds.get(i))
                    .withBundleName(getBundleName())
                    .withAbilityName(MainAbility.class.getName())
                    .withFlags(Intent.FLAG_ABILITYSLICE_MULTI_DEVICE)
                    .build();
            intent.setOperation(operation);
            intents[i] = intent;
        }
        startAbilities(intents);
    }

    @Override
    protected void onStop() {
        super.onStop();
        kvManager.closeKvStore(singleKvStore);
    }

    private GameInfo getGameInfo() {
        GameInfo gameInfo = new GameInfo();
        gameInfo.state = singleKvStore.getInt(GAME_INFO_STATE);
        gameInfo.redBoardX = singleKvStore.getFloat(GAME_INFO_RED_BOARD_X);
        gameInfo.blueBoardX = singleKvStore.getFloat(GAME_INFO_BLUE_BOARD_X);
        gameInfo.ball = GsonUtil.jsonToBean(singleKvStore.getString(GAME_INFO_BALL), GameInfo.Ball.class);
        return gameInfo;
    }

    private void initGameInfo() {
        GameInfo gameInfo = new GameInfo();
        saveState(gameInfo.state);
        saveBoard(true, gameInfo.redBoardX);
        saveBoard(false, gameInfo.blueBoardX);
        saveBall(gameInfo.ball);
    }

    // 0准备开始，1开始，2红方胜，3蓝方胜
    private void saveState(int state) {
        singleKvStore.putInt(GAME_INFO_STATE, state);
    }

    private void saveBoard(boolean isRedOrBlue, float x) {
        singleKvStore.putFloat(isRedOrBlue ? GAME_INFO_RED_BOARD_X : GAME_INFO_BLUE_BOARD_X, x);
    }

    private void saveBall(GameInfo.Ball ball) {
        singleKvStore.putString(GAME_INFO_BALL, GsonUtil.objectToString(ball));
    }
}