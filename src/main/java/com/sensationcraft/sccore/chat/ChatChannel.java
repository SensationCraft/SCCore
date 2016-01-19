package com.sensationcraft.sccore.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatChannel {

	SHOUT('s'),
	PUBLIC('p'),
	ALLY('a'),
	FACTION('f'),
	NONE('n');

	private final char code;

	public ChatChannel next(){
		ChatChannel[] values = ChatChannel.values();
		return values[(this.ordinal()+1) % (values.length-1)];
	}

	@Override
	public String toString(){
		return Character.toString(this.getCode());
	}

}
