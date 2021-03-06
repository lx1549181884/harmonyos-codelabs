/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License,Version 2.0 (the "License");
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

package com.huawei.cookbook;

import com.huawei.cookbook.database.FormInfo;
import com.huawei.cookbook.database.MovieInfo;
import com.huawei.cookbook.slice.MainAbilitySlice;
import com.huawei.cookbook.slice.MoviesDetailAbilitySlice;
import com.huawei.cookbook.util.CommonUtils;
import com.huawei.cookbook.util.DatabaseUtils;
import com.huawei.cookbook.util.LogUtils;

import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.FormException;
import ohos.aafwk.ability.ProviderFormInfo;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.LayoutScatter;
import ohos.event.intentagent.IntentAgent;
import ohos.event.intentagent.IntentAgentConstant;
import ohos.event.intentagent.IntentAgentHelper;
import ohos.event.intentagent.IntentAgentInfo;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * MainAbility
 */
public class MainAbility extends Ability {
    private static final int DEFAULT_DIMENSION_2X2 = 2;
    private static final int DIMENSION_2X4 = 3;
    private static final int WIDTH = 30;
    private static final int HIGHT = 30;

    private static final int EVENT_MESSAGE_NORMAL = 1;

    private static final long PERIOD = 10000L;

    private static final int STRENTH = 10;

    private static final String REG = "....";
    private static final String RESOURCE_IMAGE_ID = "resourceImageId";
    private static final String RESOURCE_TITLE_ID = "resourceTitleId";
    private static final String RESOURCE_INTRODUCTION_ID = "reousceIntroductionId";
    private static final String RESOUECE_AGENT_ID = "resoueceAgentId";
    private static final String ACTION_DETAIL = "detail";

    private MyEventHandler myHandler;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
        addActionRoute(ACTION_DETAIL, MoviesDetailAbilitySlice.class.getName());
        initData();
        initMyHandler();
    }

    /**
     * initData
     */
    private void initData() {
        // ??????????????????????????????
        Long movieCount = DatabaseUtils.queryMovieCount(this, null);
        if (movieCount == 0) {
            saveMovieInfo(CommonUtils.getMoviesData(this));
        }
    }

    /**
     * insert movies
     * @param movieInfoList movie list
     */
    private List<MovieInfo> saveMovieInfo(List<MovieInfo> movieInfoList) {
        if (movieInfoList.size() < 1 || movieInfoList == null) {
            return null;
        }
        DatabaseUtils.deletMovie(this, null);
        for (int index = 0; index < movieInfoList.size(); index++) {
            MovieInfo movieInfo = movieInfoList.get(index);
            DatabaseUtils.insertMovieInfo(this, movieInfo);
        }
        return movieInfoList;
    }

    private void initMyHandler() {
        myHandler = new MyEventHandler(EventRunner.current());
        long param = 0L;
        Object object = null;
        InnerEvent normalInnerEvent = InnerEvent.get(EVENT_MESSAGE_NORMAL, param, object);
        myHandler.sendEvent(normalInnerEvent, 0, EventHandler.Priority.IMMEDIATE);
    }

    private void saveFormInfo(Long formId, String formName, int dimension) {
        FormInfo form = new FormInfo(formId, formName, dimension);
        DatabaseUtils.insertForm(this, form);
    }

    private void invokeScheduleMethod() {
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    /**
                     * ??????????????????
                     */
                    public void run() {
                        getUITaskDispatcher().syncDispatch(() -> {
                            refeshData();
                        });
                    }
                },
                0, PERIOD
        );
    }

    /**
     * update forms
     */
    private void refeshData() {
        // ??????????????????
        List<FormInfo> formList = DatabaseUtils.queryForms(this, null);
        // ??????????????????
        List<MovieInfo> movieInfoList = DatabaseUtils.queryMovieList(this, null);
        if (formList == null && formList.size() < 1) {
            return;
        }
        for (FormInfo formInfo : formList) {
            ComponentProvider componentProvider = getComponentProvider(movieInfoList, formInfo.getDimension());
            try {
                initCardContent(componentProvider);
                updateForm(formInfo.getFormId(), componentProvider);
            } catch (FormException e) {
                LogUtils.error("FormException", "FormException");
            }
        }
    }

    // ??????ComponentProvider?????????????????????
    private ComponentProvider getComponentProvider(List<MovieInfo> movieInfoList, int dimension) {
        Random random = new Random();
        int movieTopId = random.nextInt(movieInfoList.size());
        int movieBottomId = random.nextInt(movieInfoList.size());
        while (movieBottomId == movieTopId) {
            movieBottomId = random.nextInt(movieInfoList.size());
        }
        ComponentProvider componentProvider = null;
        if (dimension == DIMENSION_2X4) {
            componentProvider = new ComponentProvider(ResourceTable.Layout_form_list_pattern_form_card_2_4, this);
            // ????????????id
            Map<String, Integer> leftResourceIds = getParamMap(ResourceTable.Id_movie_top_img_2x4,
                    ResourceTable.Id_movie_top_title_2x4, ResourceTable.Id_movie_top_introduction_2x4,
                    ResourceTable.Id_movie_top_2x4);
            // ???????????????????????????
            setComponentProvider(movieInfoList.get(movieTopId), componentProvider, true, leftResourceIds);
            // ???????????????id
            Map<String, Integer> rightResourceIds = getParamMap(ResourceTable.Id_movie_bottom_img_2x4,
                    ResourceTable.Id_movie_bottom_title_2x4, ResourceTable.Id_movie_bottom_introduction_2x4,
                    ResourceTable.Id_movie_bottom_2x4);
            // ???????????????????????????
            setComponentProvider(movieInfoList.get(movieBottomId), componentProvider, true, rightResourceIds);
        } else {
            componentProvider = new ComponentProvider(ResourceTable.Layout_form_list_pattern_form_card_2_2, this);
            // ????????????????????????????????????id
            Map<String, Integer> topResourceIds = getParamMap(0,
                    ResourceTable.Id_movie_top_title, ResourceTable.Id_movie_top_introduction,
                    ResourceTable.Id_movie_top);
            // ???????????????????????????????????????
            setComponentProvider(movieInfoList.get(movieTopId), componentProvider, false, topResourceIds);
            // ????????????????????????????????????id
            Map<String, Integer> bottomResourceIds = getParamMap(0,
                    ResourceTable.Id_movie_bottom_title, ResourceTable.Id_movie_bottom_introduction,
                    ResourceTable.Id_movie_bottom);
            // ???????????????????????????????????????
            setComponentProvider(movieInfoList.get(movieBottomId), componentProvider, false, bottomResourceIds);
        }

        return componentProvider;
    }

    /**
     * obtain resource ids Map
     * @param resourceImageId the Image component id
     * @param resoueceTitleId the title component id
     * @param resourceIntroductionId the introduction component id
     * @param resourceAgentId the agent component id
     * @return param map
     */
    private Map<String, Integer> getParamMap(int resourceImageId, int resoueceTitleId,
        int resourceIntroductionId, int resourceAgentId) {
        Map<String, Integer> leftResourceIds = new HashMap<>();
        leftResourceIds.put(RESOURCE_IMAGE_ID, resourceImageId);
        leftResourceIds.put(RESOURCE_TITLE_ID, resoueceTitleId);
        leftResourceIds.put(RESOURCE_INTRODUCTION_ID, resourceIntroductionId);
        leftResourceIds.put(RESOUECE_AGENT_ID, resourceAgentId);
        return leftResourceIds;
    }

    private void setComponentProvider(MovieInfo movieInfo, ComponentProvider componentProvider,
                        boolean isImage, Map<String, Integer> resourceIds) {
        if (isImage) {
            // ????????????
            componentProvider.setImageContent(resourceIds.get(RESOURCE_IMAGE_ID),movieInfo.getImgUrl().intValue());
        }
        // ????????????????????????????????????
        componentProvider.setText(resourceIds.get(RESOURCE_TITLE_ID),
                movieInfo.getTitle());
        componentProvider.setText(
                resourceIds.get(RESOURCE_INTRODUCTION_ID),
                movieInfo.getIntroduction().substring(0, STRENTH) + REG);
        // ??????????????????
        componentProvider.setIntentAgent(resourceIds.get(RESOUECE_AGENT_ID),
                getIntentAgent(movieInfo));
    }

    /**
     * MyEventHandler
     */
    class MyEventHandler extends EventHandler {
        MyEventHandler(EventRunner runner) throws IllegalArgumentException {
            super(runner);
        }

        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
            if (event == null) {
                return;
            }
            int eventId = event.eventId;
            switch (eventId) {
                case EVENT_MESSAGE_NORMAL:
                    invokeScheduleMethod();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDeleteForm(long formId) {
        super.onDeleteForm(formId);
        // ?????????????????????????????????
        DatabaseUtils.deleteForm(this, formId);
    }

    @Override
    protected ProviderFormInfo onCreateForm(Intent intent) {
        // ????????????
        String formName = intent.getStringParam(AbilitySlice.PARAM_FORM_NAME_KEY);
        // ??????id
        Long formId = intent.getLongParam(AbilitySlice.PARAM_FORM_IDENTITY_KEY, 0);
        // ????????????
        int dimension = intent.getIntParam(AbilitySlice.PARAM_FORM_DIMENSION_KEY, DEFAULT_DIMENSION_2X2);
        // ??????????????????????????????
        saveFormInfo(formId, formName, dimension);
        // ????????????????????????
        ProviderFormInfo formInfo = null;
        int layoutId = ResourceTable.Layout_form_list_pattern_form_card_2_2;
        if (dimension == DIMENSION_2X4) {
            layoutId = ResourceTable.Layout_form_list_pattern_form_card_2_4;
        }
        formInfo = new ProviderFormInfo(layoutId, this);
        List<MovieInfo> movieInfoList = DatabaseUtils.queryMovieList(this, null);
        if (movieInfoList == null || movieInfoList.isEmpty()) {
            movieInfoList = saveMovieInfo(CommonUtils.getMoviesData(this));
        }
        ComponentProvider componentProvider = getComponentProvider(movieInfoList, dimension);
        initCardContent(componentProvider);
        formInfo.mergeActions(componentProvider);
        return formInfo;
    }

    // ???????????? ???????????? ??????????????????
    private void initCardContent (ComponentProvider componentProvider) {
        // ??????2*2??????
        componentProvider.setVisibility(ResourceTable.Id_card2_hints, Component.INVISIBLE);
        componentProvider.setVisibility(ResourceTable.Id_card2_content, Component.VISIBLE);
        // ??????2*4??????
        componentProvider.setVisibility(ResourceTable.Id_card4_hints, Component.INVISIBLE);
        componentProvider.setVisibility(ResourceTable.Id_card4_content, Component.VISIBLE);
    }

    // ??????IntentAgent?????????????????????
    private IntentAgent getIntentAgent(MovieInfo movie) {
        // ???????????????????????????
        Intent intent = new Intent();
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName(getBundleName())
                .withAbilityName(MainAbility.class.getName())
                .withAction(ACTION_DETAIL)
                .build();
        // ?????????????????????id
        intent.setParam("movieId", movie.getId());
        intent.setOperation(operation);
        List<Intent> intentList = new ArrayList<>();
        intentList.add(intent);
        // ?????????????????????????????????????????????requestCode??????????????????????????????id??????requestCode
        IntentAgentInfo paramsInfo = new IntentAgentInfo(Math.toIntExact(movie.getId()),
                IntentAgentConstant.OperationType.START_ABILITY,
                IntentAgentConstant.Flags.UPDATE_PRESENT_FLAG, intentList, null);
        IntentAgent intentAgent = IntentAgentHelper.getIntentAgent(this, paramsInfo);
        return intentAgent;
    }

    @Override
    protected void onUpdateForm(long formId) {
        refeshData();
        super.onUpdateForm(formId);
    }
}
