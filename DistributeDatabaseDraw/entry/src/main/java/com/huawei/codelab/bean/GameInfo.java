package com.huawei.codelab.bean;

import com.huawei.codelab.util.GameUtil;

public class GameInfo {
    public int state = 0; // 0准备开始，1开始，2红方胜，3蓝方胜
    public float redBoardX = (GameUtil.getWindowWidth() - GameUtil.BOARD_WIDTH) / 2;
    public float blueBoardX = redBoardX;
    public Ball ball = new Ball();

    public static class Ball {
        public float x = GameUtil.getWindowWidth() / 2;
        public float y = GameUtil.getWindowHeight() - GameUtil.BOARD_MARGIN - GameUtil.BOARD_HEIGHT - GameUtil.BALL_RADIUS;
        public float speedX = GameUtil.BALL_SPEED_INIT;
        public float speedY = -GameUtil.BALL_SPEED_INIT;
    }
}
