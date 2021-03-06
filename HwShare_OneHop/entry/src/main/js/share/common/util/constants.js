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
const data = {
  loginErrorCode: {
    FAILED: -1,
    SIGN_IN_AUTH: 2002,
    SIGN_IN_NETWORK_ERROR: 2005,
    SIGN_IN_AUTH_SERVER_FAILED: 2009,
    SIGN_IN_CANCELLED: 2012
  },
  loginPermissions: [
    'https://www.huawei.com/auth/account/base.profile/accesstoken',
    'idtoken',
    'https://www.huawei.com/auth/account/base.profile/serviceauthcode'
  ],
  settingBundleName: 'com.android.settings',
  settingActivityName: 'com.android.settings.HWSettings',
  settingAction: 'android.settings.APPLICATION_DETAILS_SETTINGS'
};

export default data;
