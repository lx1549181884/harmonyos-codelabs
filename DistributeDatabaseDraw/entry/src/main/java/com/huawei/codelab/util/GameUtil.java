package com.huawei.codelab.util;

import com.huawei.codelab.MyApplication;
import com.huawei.codelab.bean.GameInfo;
import ohos.global.configuration.DeviceCapability;

import java.util.Timer;
import java.util.TimerTask;

public class GameUtil {
    public static final float BALL_RADIUS = 30;
    public static final float BALL_SPEED_INIT = 400;
    public static final float BOARD_WIDTH = 220;
    public static final float BOARD_HEIGHT = 25;
    public static final float BOARD_MARGIN = 300;

    private static final long TIMER_PERIOD = 50;
    private static Interface i = null;
    private static Timer timer = null;
    private static TimerTask timerTask = null;

    public interface Interface {
        GameInfo getGameInfo();

        void saveBall(GameInfo.Ball ball);

        void saveBoard(boolean isRedOrBlue, float x);

        void saveState(int state);
    }

    public static void moveBoard(boolean redOrBlue, float moveX) {
        if (i == null) {
            return;
        }
        GameInfo gameInfo = i.getGameInfo();
        float windowWidth = getWindowWidth();
        float oldValue = redOrBlue ? gameInfo.redBoardX : gameInfo.blueBoardX;
        float newValue = oldValue + moveX;
        if (newValue <= 0) {
            newValue = 0;
        } else if (newValue >= windowWidth - BOARD_WIDTH) {
            newValue = windowWidth - BOARD_WIDTH;
        }
        i.saveBoard(redOrBlue, newValue);
    }

    public static void init(Interface i) {
        GameUtil.i = i;
        i.saveState(0);
    }

    public static void start() {
        if (i == null) {
            return;
        }
        startTimer();
    }

    private static void startTimer() {
        stopTimer();
        i.saveState(1);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                GameInfo gameInfo = i.getGameInfo();

                float windowWidth = getWindowWidth();
                gameInfo.ball.x = gameInfo.ball.x + gameInfo.ball.speedX * TIMER_PERIOD / 1000;
                if (gameInfo.ball.x > windowWidth - BALL_RADIUS) { // 若小球超出屏幕右边
                    gameInfo.ball.x = windowWidth - BALL_RADIUS; // 矫正坐标
                    gameInfo.ball.speedX = -gameInfo.ball.speedX; // 改变速度方向
                } else if (gameInfo.ball.x < BALL_RADIUS) { // 若小球超出屏幕左边
                    gameInfo.ball.x = BALL_RADIUS; // 矫正坐标
                    gameInfo.ball.speedX = -gameInfo.ball.speedX; // 改变速度方向
                }

                float windowHeight = getWindowHeight();
                gameInfo.ball.y = gameInfo.ball.y + gameInfo.ball.speedY * TIMER_PERIOD / 1000;
                if (gameInfo.ball.y > windowHeight - BOARD_MARGIN - BOARD_HEIGHT - BALL_RADIUS // 若小球碰到红色板
                        && gameInfo.ball.y < windowHeight - BOARD_MARGIN - BOARD_HEIGHT
                        && gameInfo.ball.x > gameInfo.redBoardX
                        && gameInfo.ball.x < gameInfo.redBoardX + BOARD_WIDTH
                ) {
                    gameInfo.ball.y = windowHeight - BOARD_MARGIN - BOARD_HEIGHT - BALL_RADIUS; // 矫正坐标
                    gameInfo.ball.speedY = -gameInfo.ball.speedY; // 改变速度方向
                } else if (gameInfo.ball.y < BOARD_MARGIN + BOARD_HEIGHT + BALL_RADIUS // 若小球碰到蓝色板
                        && gameInfo.ball.y > BOARD_MARGIN + BOARD_HEIGHT
                        && gameInfo.ball.x > gameInfo.blueBoardX
                        && gameInfo.ball.x < gameInfo.blueBoardX + BOARD_WIDTH) {
                    gameInfo.ball.y = BOARD_MARGIN + BOARD_HEIGHT + BALL_RADIUS; // 矫正坐标
                    gameInfo.ball.speedY = -gameInfo.ball.speedY; // 改变速度方向
                }

                i.saveBall(gameInfo.ball); // 同步小球数据给其他端

                if (gameInfo.ball.y < 0) { // 若小球碰到屏幕顶端
                    stopTimer(); // 停止Timer
                    i.saveState(2); // 同步游戏状态2，代表红方胜
                } else if (gameInfo.ball.y > windowHeight) { // 若小球碰到屏幕底端
                    stopTimer(); // 停止Timer
                    i.saveState(3); // 同步游戏状态3，代表蓝方胜
                }
            }
        };
        timer.schedule(timerTask, 0, TIMER_PERIOD);
    }

    private static void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        timerTask = null;
    }

    // 获取屏幕的宽度
    public static float getWindowWidth() {
        return getDeviceCapability().width * getDeviceCapability().screenDensity / 160f;
    }

    // 获取屏幕的高度
    public static float getWindowHeight() {
        return getDeviceCapability().height * getDeviceCapability().screenDensity / 160f;
    }

    private static DeviceCapability getDeviceCapability() {
        return MyApplication.context.getResourceManager().getDeviceCapability();
    }
}
