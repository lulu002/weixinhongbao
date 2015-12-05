package com.peace.weixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.peace.help.LogHelp;

public class RobMoney extends AccessibilityService {
	String TAG = "RobMoney";
	static final String WECHAT_PACKAGENAME = "com.tencent.mm";
	/** * 拆红包类 */
	static final String WECHAT_RECEIVER_CALSS = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
	/** * 红包详情类 */
	static final String WECHAT_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
	/** * 微信主界面或者是聊天界面 */
	static final String WECHAT_LAUNCHER = "com.tencent.mm.ui.LauncherUI";

	static Map<String, AccessibilityNodeInfo> redMap = new HashMap<String, AccessibilityNodeInfo>();

	static List<AccessibilityNodeInfo> redLList = new ArrayList<AccessibilityNodeInfo>();

	private List<String> fetchedRedList = new ArrayList<String>();

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// Log.i(TAG, "event = " + event.toString());
		int eventType = event.getEventType();
		Log.i(TAG,
				"eventType = "
						+ AccessibilityEvent.eventTypeToString(eventType));
		switch (eventType) {
		// 第一步：监听通知栏消息
		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
			List<CharSequence> texts = event.getText();
			if (!texts.isEmpty()) {
				for (CharSequence text : texts) {
					String content = text.toString();
					// Log.i(TAG, "text:" + content);
					if (content.contains("[微信红包]")) {
						// 模拟打开通知栏消息
						if (event.getParcelableData() != null
								&& event.getParcelableData() instanceof Notification) {
							Notification notification = (Notification) event
									.getParcelableData();
							PendingIntent pendingIntent = notification.contentIntent;
							try {
								pendingIntent.send();
							} catch (CanceledException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			break;
		// 第二步：监听是否进入微信红包消息界面
		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
		case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
			// String className = event.getClassName().toString();
			// if (className.equals(WECHAT_LAUNCHER)) {
			// // 开始抢红包
			// getPacket();
			// } else if (className.equals(WECHAT_RECEIVER_CALSS)) {
			// // 开始打开红包
			// openPacket();
			// }

			try {
				getPacket(event.getSource());

			} catch (Exception e) {
				LogHelp.e(TAG, e);
			}
			break;
		}
	}

	private int getPacket(AccessibilityNodeInfo nodeInfo) {
		if (nodeInfo == null)
			return -1;
		openPacket(nodeInfo);
		List<AccessibilityNodeInfo> fetchNodes = nodeInfo
				.findAccessibilityNodeInfosByText("领取红包");

		if (null == fetchNodes || fetchNodes.isEmpty())
			return -1;
		for (int i = fetchNodes.size() - 1; i >= 0; i--) {
			AccessibilityNodeInfo info = fetchNodes.get(i);
			if (null == info) {
				continue;
			}
			Log.i(TAG, "event = " + info.toString());
			// 这里有一个问题需要注意，就是需要找到一个可以点击的View
			Log.i(TAG, "Click" + ",isClick:" + info.isClickable());
			info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			AccessibilityNodeInfo parent = info.getParent();
			while (parent != null) {
				Log.i(TAG, "parent isClick:" + parent.isClickable());
				if (parent.isClickable()) {
					parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					break;
				}
				parent = parent.getParent();
			}
			openPacket(nodeInfo);
			// String id = getHongbaoHash(info);
			// LogHelp.i(TAG, "id = " + id);
		}
		return -1;
	}

	@SuppressLint("NewApi")
	private int openPacket(AccessibilityNodeInfo nodeInfo) {
		// AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		// if (nodeInfo != null) {
		// List<AccessibilityNodeInfo> list = nodeInfo
		// .findAccessibilityNodeInfosByText("抢红包");
		// if (null != list && list.size() > 0) {
		// for (AccessibilityNodeInfo n : list) {
		// if (null != n) {
		// n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		// }
		// }
		// }
		//
		// }

		if (nodeInfo == null)
			return -1;

		/* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”、“手慢了”和“过期” */
		List<AccessibilityNodeInfo> failureNoticeNodes = new ArrayList<AccessibilityNodeInfo>();
		failureNoticeNodes.addAll(nodeInfo
				.findAccessibilityNodeInfosByText("红包详情"));
		failureNoticeNodes.addAll(nodeInfo
				.findAccessibilityNodeInfosByText("手慢了"));
		failureNoticeNodes.addAll(nodeInfo
				.findAccessibilityNodeInfosByText("过期"));
		if (!failureNoticeNodes.isEmpty()) {
			return 0;
		}

		/* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
		List<AccessibilityNodeInfo> successNoticeNodes = nodeInfo
				.findAccessibilityNodeInfosByText("拆红包");
		// List<AccessibilityNodeInfo> preventNoticeNodes = nodeInfo
		// .findAccessibilityNodeInfosByText("领取红包");
		if (null != successNoticeNodes && !successNoticeNodes.isEmpty()) {
			for (int i = successNoticeNodes.size() - 1; i >= 0; i--) {
				AccessibilityNodeInfo openNode = successNoticeNodes.get(i);
				Log.i(TAG, "拆红包 = " + openNode.getContentDescription());
				openNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				AccessibilityNodeInfo parent = openNode.getParent();
				while (parent != null) {
					Log.i(TAG, "parent isClick:" + parent.isClickable());
					if (parent.isClickable()) {
						parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
						break;
					}
					parent = parent.getParent();
				}
			}

			return 0;
		}
		return -1;
	}

	@SuppressLint("NewApi")
	private void getPacket() {
		AccessibilityNodeInfo rootNode = getRootInActiveWindow();
		if (null != rootNode) {
			recycle(rootNode);
		}
	}

	/**
	 * 打印一个节点的结构
	 * 
	 * @param info
	 */
	@SuppressLint("NewApi")
	public void recycle(AccessibilityNodeInfo info) {
		if (info.getChildCount() == 0) {
			if (info.getText() != null) {
				if ("领取红包".equals(info.getText().toString())) {
					// 这里有一个问题需要注意，就是需要找到一个可以点击的View
					Log.i(TAG, "Click" + ",isClick:" + info.isClickable());
					info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					AccessibilityNodeInfo parent = info.getParent();
					while (parent != null) {
						Log.i(TAG, "parent isClick:" + parent.isClickable());
						if (parent.isClickable()) {
							parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
							break;
						}
						parent = parent.getParent();
					}

				}
			}

		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if (info.getChild(i) != null) {
					recycle(info.getChild(i));
				}
			}
		}
	}

	@Override
	public void onInterrupt() {
	}

	public List<String> getFetchedRedList() {
		if (null == fetchedRedList) {
			fetchedRedList = new ArrayList<String>();
		}
		return fetchedRedList;
	}

	private String getHongbaoHash(AccessibilityNodeInfo node) {
		/* 获取红包上的文本 */
		String content;
		try {
			AccessibilityNodeInfo i = node.getParent().getChild(0);
			content = i.getText().toString();
		} catch (NullPointerException npr) {
			return null;
		}

		return content + "@" + getNodeId(node);

		// return content + "@" + node.get;
	}

	private String getNodeId(AccessibilityNodeInfo node) {
		/* 用正则表达式匹配节点Object */
		Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
		Matcher objHashMatcher = objHashPattern.matcher(node.toString());

		// AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
		objHashMatcher.find();

		return objHashMatcher.group(0);
	}
}
