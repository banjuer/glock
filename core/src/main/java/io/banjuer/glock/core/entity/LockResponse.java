package io.banjuer.glock.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author guochengsen
 */
@NoArgsConstructor
@AllArgsConstructor
public @Data class LockResponse implements Serializable {

	private static final String LOCK_SUCCESS_MSG = "加锁成功";
	private static final String LOCK_ERROR_MSG = "加锁失败";
	private static final String UNLOCK_SUCCESS_MSG = "解锁成功";
	private static final String UNLOCK_ERROR_MSG = "解锁失败";

	private static final long serialVersionUID = 1069963785692520151L;
	private boolean success;
	private Integer count;
	private String msg;

	public static LockResponse lockSuccess(int count, String msg) {
		return new LockResponse(true, count, msg);
	}

	public static LockResponse lockSuccess(int count) {
		return new LockResponse(true, count, LOCK_SUCCESS_MSG);
	}

	public static LockResponse lockError() {
		return new LockResponse(false, -1, LOCK_ERROR_MSG);
	}

	public static LockResponse lockError(String msg) {
		return new LockResponse(false, -1, msg);
	}

	public static LockResponse unlockSuccess(int count) {
		return new LockResponse(true, count, UNLOCK_SUCCESS_MSG);
	}

	public static LockResponse unlockSuccess(int count, String msg) {
		return new LockResponse(true, count, msg);
	}

	public static LockResponse unlockError() {
		return new LockResponse(false, -1, UNLOCK_ERROR_MSG);
	}

	public static LockResponse unlockError(String msg) {
		return new LockResponse(false, -1, msg);
	}

}
