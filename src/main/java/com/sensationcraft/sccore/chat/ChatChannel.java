package com.sensationcraft.sccore.chat;

import com.google.common.base.Predicate;

import lombok.Getter;

@Getter
public enum ChatChannel {

	SHOUT('s'),
	STAFF('t'),
	PUBLIC('p'),
	ALLY('a'),
	FACTION('f'),
	NONE('n');

	public static final Predicate<ChatChannel> NOT_NONE = input -> input != ChatChannel.NONE;
	public static final Predicate<ChatChannel> NOT_FACTION_NOT_NONE = input -> ChatChannel.NOT_NONE.apply(input) && input != ChatChannel.FACTION && input != ChatChannel.ALLY;
	private final char code;

	ChatChannel(char code) {
		this.code = code;
	}

	public ChatChannel next() {
		ChatChannel[] values = ChatChannel.values();
		return values[(this.ordinal() + 1) % (values.length)];
	}

	public ChatChannel next(Predicate<ChatChannel> when) {
		ChatChannel[] values = ChatChannel.values();
		for (int i = 1; i < values.length; i++) {
			ChatChannel next = values[(this.ordinal() + i) % (values.length)];
			if (when.apply(next))
				return next;
		}
		return this;
	}

	@Override
	public String toString() {
		return Character.toString(this.getCode());
	}

}
