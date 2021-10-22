/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.codelab.component;

import com.huawei.codelab.bean.GameInfo;
import com.huawei.codelab.util.GameUtil;
import ohos.agp.components.Component;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.utils.Color;
import ohos.app.Context;

/**
 * Drawl
 *
 * @since 2021-04-06
 */
public class DrawPoint extends Component implements Component.DrawTask {
    private GameInfo gameInfo;
    private Paint paint;
    private boolean isLocal;

    public DrawPoint(Context context, boolean isLocal) {
        super(context);
        this.isLocal = isLocal;
        init();
    }

    public void setDrawParams(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        invalidate();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        addDrawTask(this);
    }

    @Override
    public void onDraw(Component component, Canvas canvas) {
        if (gameInfo == null) {
            return;
        }
        float windowWidth = GameUtil.getWindowWidth();
        float windowHeight = GameUtil.getWindowHeight();
        // 如果是远端，以屏幕中心为圆心，旋转180度
        if (!isLocal) {
            canvas.rotate(180, windowWidth / 2, windowHeight / 2);
        }
        // 画红色板子
        canvas.drawRect(gameInfo.redBoardX,
                windowHeight - GameUtil.BOARD_MARGIN - GameUtil.BOARD_HEIGHT,
                gameInfo.redBoardX + GameUtil.BOARD_WIDTH,
                windowHeight - GameUtil.BOARD_MARGIN,
                paint,
                new Color(Color.getIntColor("#cb3636"))
        );
        // 画蓝色板子
        canvas.drawRect(gameInfo.blueBoardX,
                GameUtil.BOARD_MARGIN,
                gameInfo.blueBoardX + GameUtil.BOARD_WIDTH,
                GameUtil.BOARD_MARGIN + GameUtil.BOARD_HEIGHT,
                paint,
                new Color(Color.getIntColor("#0068b7"))
        );
        // 画球
        canvas.drawCircle(gameInfo.ball.x, gameInfo.ball.y, GameUtil.BALL_RADIUS, paint, new Color(Color.getIntColor("#ffcb57")));
    }
}