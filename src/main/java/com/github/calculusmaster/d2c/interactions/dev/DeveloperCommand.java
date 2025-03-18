package com.github.calculusmaster.d2c.interactions.dev;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DeveloperCommand
{
	SHUTDOWN("Bot Shutdown"),

	;

	@Getter private final String name;
}
