package com.sensationcraft.sccore.helprequests;

import java.util.UUID;

import lombok.Getter;

/**
 * Created by Anml on 1/18/16.
 */

@Getter
public class HelpRequest {

	private UUID creator;
	private String message;

	public HelpRequest(UUID creator, String message) {
		this.creator = creator;
		this.message = message;
	}

}
