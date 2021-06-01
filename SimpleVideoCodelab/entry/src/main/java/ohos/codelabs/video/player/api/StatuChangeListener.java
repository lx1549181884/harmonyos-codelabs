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

package ohos.codelabs.video.player.api;

import ohos.codelabs.video.player.constant.PlayerStatu;

/**
 * StatuChangeListener interface
 *
 * @since 2021-04-04
 *
 */
public interface StatuChangeListener {
    /**
     * statuCallback
     *
     * @param statu statu
     */
    void statuCallback(PlayerStatu statu);
}
