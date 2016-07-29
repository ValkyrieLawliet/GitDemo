package com.example.pintudemo;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    /**
     * 当前动画是否在执行中
     **/
    private boolean isAnimRun = false;

    /**
     * 判断游戏是否开始
     **/
    private boolean isGameStart = false;

    /**
     * 利用二维数组创建若干个游戏小方块
     */

    private ImageView[][] imageViews_arr = new ImageView[3][5];
    /**
     * 游戏主界面
     **/
    private GridLayout gridLayout_game;
    /**
     * 当前空方块的实例保存
     **/
    private ImageView iv_null_imageView;

    /**
     * 当前手势
     **/
    private GestureDetector mGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int type = getDirByGes(e1.getX(), e1.getY(), e2.getX(), e2.getY());
                changeByDir(type);
                return false;
            }
        });
        setContentView(R.layout.activity_main);

        /*初始化游戏的若干个小方块*/
        //获取一张大图
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.pintu)).getBitmap();
        int tuWandH = bitmap.getWidth() / 5; //每个游戏小方块的宽和高
        int ivWandH = getWindowManager().getDefaultDisplay().getWidth() / 5; //小方块的宽高应该是整个屏幕的宽/5
        for (int i = 0; i < imageViews_arr.length; i++) {
            for (int j = 0; j < imageViews_arr[0].length; j++) {
                //根据行列来切成若干个游戏小方块
                Bitmap bm = Bitmap.createBitmap(bitmap, j * tuWandH, i * tuWandH, tuWandH, tuWandH);
                imageViews_arr[i][j] = new ImageView(this);
                imageViews_arr[i][j].setImageBitmap(bm);  //设置每一个小方块的图案
                imageViews_arr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(ivWandH, ivWandH));
                imageViews_arr[i][j].setPadding(2, 2, 2, 2);  //设置方块之间的间距
                imageViews_arr[i][j].setTag(new GameData(i, j, bm));  //绑定自定义的数据
                imageViews_arr[i][j].setOnClickListener(new View.OnClickListener() { //设置方块的点击监听事件
                    @Override
                    public void onClick(View v) {
                        boolean flag = isHasNullImageView((ImageView) v);
                        // int x =((GameDate)v.getTag()).x;
                        // int y =((GameDate)v.getTag()).y;
                        // Toast.makeText(MainActivity.this,"x:"+x+";y:"+y,Toast.LENGTH_SHORT).show();

                        // Toast.makeText(MainActivity.this, "位置关系是否存在：" + flag, Toast.LENGTH_SHORT).show();
                        //如果存在相邻关系则调用改变方法
                        if (flag) {
                            changeDataByImageView((ImageView) v);

                        }

                    }

                });


            }

        }

        /*初始化游戏主界面，并添加若干个小方块*/
        gridLayout_game = (GridLayout) findViewById(R.id.gl_main_game);
        for (int i = 0; i < imageViews_arr.length; i++) {
            for (int j = 0; j < imageViews_arr[0].length; j++) {
                gridLayout_game.addView(imageViews_arr[i][j]);

            }
        }

        //设置最后一个方块是空的
        setNullImageView(imageViews_arr[2][4]);
        //停止四秒
        Toast.makeText(MainActivity.this, "图案即将被打乱顺序,希望你能重新拼回原形，Good Luck..", Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                randomMove();
                isGameStart = true;//开始状态
            }
        }, 3000);


    }

    /**
     * 利用动画结束之后，交换两个方块的数据
     *
     * @param mImageView 点击的方块
     */
    public void changeDataByImageView(final ImageView mImageView) {

        changeDataByImageView(mImageView, true);

    }


    /**
     * 利用动画结束之后，交换两个方块的数据
     *
     * @param mImageView 点击的方块
     * @param isAnim     true:有动画，false：无动画
     */
    public void changeDataByImageView(final ImageView mImageView, boolean isAnim) {
        if (isAnimRun) { //如果动画正在执行,则不做交换操作
            return;
        }
        if (!isAnim) {
            //不需要动画直接交换数据
            GameData gameDate = (GameData) mImageView.getTag();
            iv_null_imageView.setImageBitmap(gameDate.bm);
            GameData mNullGameData = (GameData) iv_null_imageView.getTag();
            mNullGameData.bm = gameDate.bm;
            mNullGameData.p_x = gameDate.p_x;
            mNullGameData.p_y = gameDate.p_y;
            //设置当前点击的为空方块
            setNullImageView(mImageView);
            if (isGameStart) { //游戏开始才去判断是否结束
                isGameOver();//成功时，会弹一个toast
            }
            return;

        }

        //创建一个动画，设置好方向，移动的距离
        TranslateAnimation translateAnimation = null;
        if (mImageView.getX() > iv_null_imageView.getX()) {  //当前点击的方块在空方块的下边
            //往上移动
            translateAnimation = new TranslateAnimation(0.1f, -mImageView.getWidth(), 0.1f, 0.1f);


        } else if (mImageView.getX() < iv_null_imageView.getX()) {  //当前点击的方块在空方块的上边
            //往下移动
            translateAnimation = new TranslateAnimation(0.1f, mImageView.getWidth(), 0.1f, 0.1f);


        } else if (mImageView.getY() > iv_null_imageView.getY()) {
            //往左移动
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, -mImageView.getWidth());


        } else if (mImageView.getY() < iv_null_imageView.getY()) {
            //往右移动
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, mImageView.getWidth());


        }


        //设置动画的时长

        translateAnimation.setDuration(70);

        //设置动画结束之后是否停留

        translateAnimation.setFillAfter(true);

        //设置动画结束之后要真正的把数据交换
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

                isAnimRun = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimRun = false;
                //动画结束后真正的交换数据
                mImageView.clearAnimation();
                GameData gameDate = (GameData) mImageView.getTag();
                iv_null_imageView.setImageBitmap(gameDate.bm);
                GameData mNullGameData = (GameData) iv_null_imageView.getTag();
                mNullGameData.bm = gameDate.bm;
                mNullGameData.p_x = gameDate.p_x;
                mNullGameData.p_y = gameDate.p_y;
                //设置当前点击的为空方块
                setNullImageView(mImageView);
                if (isGameStart) { //游戏开始才去判断是否结束
                    isGameOver();//成功时，会弹一个toast
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //点击的图片去执行动画
        mImageView.startAnimation(translateAnimation);

    }

    /**
     * 随机打乱顺序
     */
    public void randomMove() {

        //打乱的次数
        for (int i = 0; i < 10; i++) {
            //根据手势开始交换，无动画
            int type = (int) ((Math.random() * 4) + 1);
            changeByDir(type, false);

        }

    }

    /**
     * 判断解析结束
     */
    public void isGameOver() {
        boolean isGameOver = true;

        //要遍历每个游戏小方块
        for (int i = 0; i < imageViews_arr.length; i++) {

            for (int j = 0; j < imageViews_arr[0].length; j++) {

                //为空的方块数据不判断跳过
                if (imageViews_arr[i][j] == iv_null_imageView) {

                    continue;
                }

                GameData mGameData = (GameData) imageViews_arr[i][j].getTag();
                if (!mGameData.isTrue()) {
                    isGameOver = false;
                    break;

                }

            }

        }


        //根据一个开关变量决定游戏是否结束，结束给提示
        if (isGameOver) {

            Toast.makeText(MainActivity.this, "游戏结束,好棒棒！", Toast.LENGTH_SHORT).show();

        }

    }


    /**
     * 根据手势的方向，获取空方块相应的相邻位置,如果存在方块，那么进行数据交换移动
     *
     * @param type 1：上，2：下，3：左，4：右
     */
    public void changeByDir(int type) {
        changeByDir(type, true);
    }


    /**
     * 根据手势的方向，获取空方块相应的相邻位置,如果存在方块，那么进行数据交换移动
     *
     * @param type   1：上，2：下，3：左，4：右
     * @param isAnim true :有动画，false:没有动画
     */
    public void changeByDir(int type, boolean isAnim) {
        //根据当前空方块的位置
        GameData mGameData = (GameData) iv_null_imageView.getTag();
        //根据方向，设置相应的位置的坐标
        int new_x = mGameData.x;
        int new_y = mGameData.y;
        if (type == 1) {//要移动的方块在当前空方块的下边.这里的x，y值其实是二维数组的下标值
            new_x++;

        } else if (type == 2) {
            new_x--;
        } else if (type == 3) {
            new_y++;
        } else if (type == 4) {
            new_y--;
        }

        //判断这个新坐标，是否存在
        if (new_x >= 0 && new_x < imageViews_arr.length && new_y >= 0 && new_y < imageViews_arr[0].length) {
            //存在的话，开始移动。
            if (isAnim) {
                changeDataByImageView(imageViews_arr[new_x][new_y]);
            } else {

                changeDataByImageView(imageViews_arr[new_x][new_y], false);
            }

        } else {
            //什么也不做

        }


    }


    /**
     * 手势判断，是向左滑动还是向右滑动
     *
     * @param start_x 手势的起始点x
     * @param start_y 手势的起始点y
     * @param end_x   手势的终止点x
     * @param end_y   手势的终止点y
     * @return 1：上，2：下，3：左，4：右
     */
    public int getDirByGes(float start_x, float start_y, float end_x, float end_y) {
        boolean isLeftOrRight = (Math.abs(start_x - end_x) > Math.abs(start_y - end_y)) ? true : false; //是否是左右 (判断x轴变动的距离比y轴变动的距离大就是左右滑动)
        if (isLeftOrRight) {//左右
            boolean isLeft = start_x - end_x > 0 ? true : false;//到底是不是向左滑
            if (isLeft) {
                return 3;

            } else {
                return 4;
            }
        } else {//上下
            boolean isUp = start_y - end_y > 0 ? true : false;//到底是不是向上滑
            if (isUp) {
                return 1;

            } else {
                return 2;
            }

        }
    }


    /**
     * 设置某个方块为空方块
     *
     * @param nullImageView 当前要设置为空的方块实例
     */
    public void setNullImageView(ImageView nullImageView) {
        nullImageView.setImageBitmap(null);
        iv_null_imageView = nullImageView;
    }

    /**
     * 判断当前点击的方块，是否与空方块的位置是相邻关系
     *
     * @param imageView 所点击的方块
     * @return true：相邻，false：不相邻
     */
    private boolean isHasNullImageView(ImageView imageView) {
        //分别获取当前空方块的位置与点击方块的位置，通过x，y 两边都差1的方式判断

        GameData mNullGameDate = (GameData) iv_null_imageView.getTag();

        GameData mGameDate = (GameData) imageView.getTag();


        if (mNullGameDate.y == mGameDate.y && mGameDate.x + 1 == mNullGameDate.x) {//当前点击的方块在空方块的上边
            return true;

        } else if (mNullGameDate.y == mGameDate.y && mGameDate.x - 1 == mNullGameDate.x) {//当前点击的方块在空方块的下边
            return true;

        } else if (mNullGameDate.y + 1 == mGameDate.y && mGameDate.x == mNullGameDate.x) {//当前点击的方块在空方块的左边

            return true;
        } else if (mNullGameDate.y - 1 == mGameDate.y && mGameDate.x == mNullGameDate.x) {//当前点击的方块在空方块的右边
            return true;

        }
        return false;

    }

    /**
     * 每个游戏小方块的上要绑定的数据
     */
    class GameData {
        /**
         * 每个小方块的实际位置x
         **/
        public int x = 0;
        /**
         * 每个小方块的实际位置y
         **/
        public int y = 0;
        /**
         * 每个小方块的图片
         **/
        public Bitmap bm;
        /**
         * 每个小方块图片的位置x
         **/
        public int p_x = 0;
        /**
         * 每个小方块图片的位置y
         **/
        public int p_y = 0;

        public GameData(int x, int y, Bitmap bm) {
            this.x = x;
            this.y = y;
            this.bm = bm;
            this.p_x = x;
            this.p_y = y;
        }

        /**
         * 判断每个小方块的位置是否是正确的
         *
         * @return true：是正确的，false：不正确
         */
        public boolean isTrue() {
            if (x == p_x && y == p_y) {
                return true;

            }
            return false;

        }
    }


}
