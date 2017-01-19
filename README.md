## 手势解锁

### 效果图

![](http://i.imgur.com/G4FUSxH.gif)

### 使用

设置回调监听：

    mLockView.setOnUnLockListener(new LockView.UnLockCallback() {
	/**
	 * 返回长度以及顺序（密码）
	 */
	 @ Override
	public void lockFinish(int length, String psw) {
		Toast.makeText(MainActivity.this, "length:" + length + "  psw:" + psw, Toast.LENGTH_SHORT).show();
		if ("1236".equals(psw)) {
			mLockView.setIsMeasureTrue(true);
		} else {
			mLockView.setIsMeasureTrue(false);
		}
	}
});
