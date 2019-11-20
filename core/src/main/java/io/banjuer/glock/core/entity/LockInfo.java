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
public @Data class LockInfo implements Serializable {

	private static final long serialVersionUID = 4217634471736296164L;
	private String key;

	private int count;

	private String host;

	public LockInfo(String key, String host) {
		this.key = key;
		this.host = host;
	}

	public void increaseCount() {
		count += 1;
	}

}
